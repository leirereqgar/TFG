package com.example.GuitAR;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;
import com.example.GuitAR.lessons.*;
import com.example.GuitAR.services.ChordRecognitionService;
import com.example.GuitAR.services.ChordRecognitionService.ChordRecognitionBinder;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LessonActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    /*
     * VARIABLES PARA OPENCV
     */
    CameraBridgeViewBase camera_bridge_view;
    BaseLoaderCallback base_loader_callback;

    // Imagen original, en hsv y solo con color amarillo: para el marcador
    Mat src, hsv, yellow_img, gray, dst, lines;
    ArrayList<double[]> frets;

    // Almacena los valores del marcador (esquina superior, ancho y alto)
    Rect rectangle;
    Boolean marker_found = false;

    // Limites superior e inferior del color amarillo en el espacio hsv
    Scalar high_limit, low_limit;
    MatOfPoint2f approx_curve;
    double min_area = 250;

    Timer cronometro;

    /*
     * VARIABLES PARA RECONOCIMIENTO DE ACORDES
     */
    NoteNameEnum chord_name  = NoteNameEnum.NO_NOTE;
    ChordTypeEnum chord_type = ChordTypeEnum.NoChord;
    int [] chord             = new int [2];
    ArrayList<Scalar> colors;

    ChordRecognitionService service;
    boolean connected = false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ChordRecognitionBinder binder = (ChordRecognitionBinder) iBinder;
            service = binder.getService();
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("servicio desconectado");
            connected = false;
        }
    };

    /*
     * VARIABLES DE LAS LECCIONES
     */
    private Lesson info;
    Bundle extras;
    String lesson_name;
    int index = 0;

    // METODOS HEREDADOS DE AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        extras = getIntent().getExtras();
        lesson_name = extras.getString("lesson");
        switch (lesson_name) {
            case "Major":
                info = new Major();
                System.out.println(lesson_name);
                break;
            case "Minor":
                info = new Minor();
                System.out.println(lesson_name);
                break;
            case "Dominant":
                info = new Dominant();
                System.out.println(lesson_name);
                break;
            case "Progresion145 C":
                info = new Progression145C();
                System.out.println(lesson_name);
                break;
            case "Progresion1645 C":
                info = new Progression1645C();
                System.out.println(lesson_name);
                break;
            case "Progresion1514 C":
                info = new Progression1514C();
                System.out.println(lesson_name);
                break;
            case "Progresion145 E":
                info = new Progression145E();
                System.out.println(lesson_name);
                break;
        }

        colors = new ArrayList<>();
        colors.add(new Scalar(50, 161, 181));
        colors.add(new Scalar(246, 212, 94));
        colors.add(new Scalar(249, 119, 166));
        colors.add(new Scalar(254, 249, 255));

        camera_bridge_view = findViewById(R.id.cameraViewer);
        camera_bridge_view.setMaxFrameSize(1920,1080);
        camera_bridge_view.setVisibility(SurfaceView.VISIBLE);
        //Descomentar para camara frontal
        camera_bridge_view.setCameraIndex(1); //DEBUG
        camera_bridge_view.setCvCameraViewListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Mantener pantalla encendida para que no entre en suspension

        //Crear listener para la camara
        base_loader_callback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    src = new Mat();
                    hsv = new Mat();
                    yellow_img = new Mat();
                    gray = new Mat();
                    lines = new Mat();
                    frets = new ArrayList<>();
                    high_limit = new Scalar(100,255,255);
                    low_limit = new Scalar(80,100,20);
                    camera_bridge_view.enableView();
                    approx_curve = new MatOfPoint2f();

                    /*
                     * La deteccion de trastes se hace con un Timer,
                     * la ejecucion empieza despues de crearse
                     * la actividad.
                     * Se ejecuta la funcion cada 5 segundos
                     */
                    cronometro = new Timer();
                    cronometro.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("Detectando trastes");
                            if(marker_found)
                                try{
                                    detectParallelLines();
                                }catch (Exception e){}
                        }
                    }, 0, 5000);
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        //Asociar el service para ir escuchando las frecuencias
        Intent service_intent = new Intent(this, ChordRecognitionService.class);
        bindService(service_intent, connection, Context.BIND_AUTO_CREATE);
        bindService(service_intent, connection, Context.BIND_AUTO_CREATE);
        startService(service_intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Desasociar el servicio para evitar errores en un futuro
        Intent service_intent = new Intent(this, ChordRecognitionService.class);
        stopService(service_intent);
        unbindService(connection);
        connected = false;

        cronometro.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera_bridge_view != null) {
            camera_bridge_view.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "Ha ocurrido un problema", Toast.LENGTH_SHORT).show();
        } else {
            base_loader_callback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (camera_bridge_view != null) {
            camera_bridge_view.disableView();
        }
        cronometro.cancel();
    }


    // METODOS HEREDADOS DE CameraBridgeViewBase.CvCameraListenerV2
    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    /**
     * onCameraFrame : en cada frame se localiza el marcador y se dibujan los elementos necesarios
     * @param inputFrame
     * @return frame con todos los elementos dibujados
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        chord = service.getChord();
        translateChord();

        // Obtener frame en color, se usara dentro de las funciones
        src = inputFrame.rgba();
        // Voltear la imagen en el eje y para que actue como un espejo
        Core.flip(src, src, 1);

        Imgproc.putText(src, info.getChord(index).toString(),
                new Point(350,280), 1,4,new Scalar(0,0,0), 4);

        try {
            findMarker();
        } catch (Exception e) {}

        for (int i = 0; i < frets.size(); i++) {
            Point pt = new Point(frets.get(i)[0], frets.get(0)[1]);

            Imgproc.circle(src, pt, 20,new Scalar(0,255,0), -1);
        }


        if(marker_found &&
                frets.size() > info.getChord(index).numFrets() &&
                index < info.size()) {
            drawChord(info.getChord(index));

            if(chord_name == info.getChord(index).getName())
                index++;
        }

        if(index >= info.size()) {
            SharedPreferences sh = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sh.edit();
            editor.putString(lesson_name, "completed");
            editor.apply();

            Intent change = new Intent(this, LessonSelectionScreen.class);
            startActivity(change);
        }

        Imgproc.putText(src, index+"", new Point(170,280), 0,3,new Scalar(0,0,0), 4);

        return src;
    }


    /**
     * findMarker : encuentra el marcador donde inicia el mastil
     * Primero se cambia del espacio de color BGR al HSV y se queda solo con los pixeles
     * dentro de los limites superior e inferior definidos
     * Despues, se extraen los contrornos encontrados en la imagen.
     *          * Por cada contorno encontrado, se aproxima la forma para que tenga menos irregularidades y
     *          * se calcula el area d ela figura encontrada.
     *          * Si la figura tiene un area mayor a la minima y 4 vertices puede ser un marcador,
     *          * con boundingRect se cogen los elementos distintivos y se guarda como marcador.
     *
     */
    private void findMarker() {
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv, low_limit, high_limit, yellow_img);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(yellow_img, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approx_curve, 0.02 * Imgproc.arcLength(curve, true), true);
            int n_vertices = (int) approx_curve.total();
            double area = Imgproc.contourArea(contour);

            if(Math.abs(area) >= min_area && n_vertices == 4) {
                rectangle = Imgproc.boundingRect(contour);
                marker_found = true;
                // DEBUG
                Imgproc.rectangle(src, new Point(rectangle.x, rectangle.y),
                        new Point(rectangle.x+rectangle.width, rectangle.y+rectangle.height),
                        new Scalar(0,0,255), 3, Imgproc.LINE_8);
//                Imgproc.line(src,
//                        new Point(rectangle.x+ rectangle.width, rectangle.y),
//                        new Point(rectangle.x+rectangle.width, rectangle.y+rectangle.height),
//                        new Scalar(0,0,255),10);
            }
        }
    }

    /**
     * detectParallelLines : detecta las lineas que corresponden a los trastes
     * 1.- se calcula el angulo que tiene el lado del marcador.
     * 2.- Se inicializa donde se guardara el resultado
     * 3.- Se transforma el frame original (src) a banco y negro
     * 4.- Se aplica la funcion Canny para encontrar los bordes en la imagen
     * 5.- Se ejecuta HoughLines para quedarnos solo con las lineas rectas
     * 6.- Cuando estan las todas las lineas detectadas se descartan las que no sirven:
     *      - Si no es paralela al marcador no se añade
     *      - Si esta muy cerca de otra linea ya añadida se ignora
     * 7.- Se ordenan todas las lineas de forma ascendente y se eliminan las que son menores que
     * la posicion del marcador
     */
    private void detectParallelLines() {
        //Calcular el angulo del lado del marcador
        double alfa = calcAngle(rectangle.x+ rectangle.width, rectangle.y,
                                        rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        Rect marker = rectangle;
        dst = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(gray, dst, 50, 200, 3, false);
        Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150);

        frets = new ArrayList<>();
        frets.add(new double[]{rectangle.x+rectangle.width, rectangle.y});
        for (int i = 0; i < lines.rows(); i++) {
            double rho = lines.get(i, 0)[0],
                    theta = lines.get(i, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;

            // Solo añadir la linea a los trastes si es paralela al marcador
            if (compareAngles(alfa, theta)){
                System.out.println(theta);
                // Comprobar si hay otra linea demasiado cerca, si no es el caso se acaba añadiendo
                boolean add = true;
                for (int j = 0; j < frets.size() && add; j++) {
                    if (Math.abs(frets.get(j)[0] - x0) < 100) {
                        add = false;
                    }
                }

                if (add)
                    frets.add(new double[]{x0, y0, a, b});
            }
        }

//        for (int i = 0; i < frets.size(); i++) {
//            System.out.println(frets.get(i)[0]);
//        }
        frets.sort(Comparator.comparingDouble(d -> d[0]));
        //DEBUG
//        System.out.println("marcador:" + (marker.x+marker.width));
//        for (int i = 0; i < frets.size(); i++) {
//            System.out.println(frets.get(i)[0]);
//        }
//
//        System.out.println("aaaaaa");
        for (int i = 0; i < frets.size(); i++) {
            if(Double.compare(marker.x+ marker.width, frets.get(i)[0]) > 0) {
//                System.out.println(frets.get(i)[0]);
                frets.remove(i);
            }
        }

        //DEBUG
//        System.out.println("despues eliminar");
//        for (int i = 0; i < frets.size(); i++) {
//            System.out.println(frets.get(i)[0]);
//        }
    }

    //Intento de deteccion con hough lines probabilistico
    private void detectParallelLinesP() {
        //Calcular el angulo del lado del marcador
        double alfa = calcAngle(rectangle.x+ rectangle.width, rectangle.y,
                rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        /*
         * 1.- Se inicializa donde se guardara el resultado
         * 2.- Se transforma el frame original (src) a banco y negro
         * 3.- Se aplica la funcion Canny para encontrar los borden en la imagen
         * 4.- Se ejecuta HoughLines para quedarnos solo con las lineas rectas
         */
        dst = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(gray, dst, 50, 200, 3, false);
        // Para mejorar performance mirar hacerlo con la probabilistica
        Imgproc.HoughLinesP(dst, lines, 1, Math.PI/180, 100,100,10); // runs the actual detection

        /*
         * Una vez con todas las lineas detectadas se procesa la informacion para eliminar datos
         * innecesarios
         */
        frets = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            double[] l = lines.get(i,0);
            double line_angle = calcAngle(l[0], l[1], l[2], l[3]);

            // Solo añadir la linea a los trastes si es paralela al marcador
            if (compareAngles(alfa, line_angle)){
                System.out.println(line_angle);
                // Comprobar si hay otra linea demasiado cerca, si no es el caso se acaba añadiendo
                boolean add = true;
                for (int j = 0; j < frets.size() && add; j++) {
                    if (Math.abs(frets.get(j)[0] - l[0]) < 1) {
                        add = false;
                    }
                }

                if (add){
                    frets.add(l);
//                    Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
//                    Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
//                    Imgproc.line(src, pt1, pt2, new Scalar(0,255,0), 3, Imgproc.LINE_AA, 0);
                }
            }
        }

        /*
         * Para seguir eliminando lineas no deseadas y dejarlo para dibujar la informacion se ordena
         * el array de forma ascendente segun la coordenada x
         */
        frets.sort(Comparator.comparingDouble(d -> d[0]));

        // Por ultimo, eliminar lineas detectadas a la "izquierda" del marcador
        for (int i = 0; i < frets.size(); i++) {
            if(Double.compare(rectangle.x+ rectangle.width, frets.get(i)[0]) < 0)
                frets.remove(i);
        }
    }

    /**
     * calcAngle : calcula el angulo de la recta que forman los puntos
     * @param x1 : coordenada x del primer punto
     * @param y1 : coordenada y del primer punto
     * @param x2 : coordenada x del segundo punto
     * @param y2 : coordenada y del segundo punto
     * @return   : angulo de la recta en radianes
     */
    private double calcAngle(double x1, double y1, double x2, double y2) {
        double pendiente = (y2 - y1) / (x2 - x1);

        return Math.atan(pendiente);
    }

    /**
     * compareAngles : comprueba si dos pendientes son iguales
     * @param p1    : pendiente 1
     * @param p2    : pendiente 2
     * @return      : true si son iguales, false si no
     */
    private boolean compareAngles(double p1, double p2) {
        return Math.abs(p1-p2) <= 1.5;
    }

    /**
     * translateChord : traduce el array recibido por el servicio a los enums correspondientes
     */
    private void translateChord() {
        chord_name = NoteNameEnum.get(chord[0]);
        chord_type = ChordTypeEnum.fromInteger(chord[1]);
    }

    /**
     * drawChord : llama a drawNote por cada nota que lo compone con el color correspondiente
     * @param c acorde que dibujar
     */
    private void drawChord(Chord c) {
        for (int i = 0; i < c.size(); i++) {
            drawNote(c.get(i), colors.get(i));
        }
    }

    /**
     * drawNote : dibuja la figura correspondiente a la nota n, con el color c indicado.
     *      Si la cuerda indicada es 0 se dibuja una recta por ser una cejilla
     *      Si es cualquier otro numero se dibuja un circulo
     * @param n objeto con la cuerda y traste que se necesita
     * @param c color con el que dibujar la figura
     */
    private void drawNote(Note n, Scalar c) {
        double x = (frets.get(n.getFret()-1)[0]+frets.get(n.getFret())[0])/2; // Calcular la posicion entre trastes

        double interval = rectangle.height / 6.0;

        if (n.getString() == 0){
            Imgproc.line(src,
                    new Point(frets.get(n.getFret()-1)[0],
                            rectangle.y),
                    new Point(frets.get(n.getFret()-1)[0],
                            rectangle.y + rectangle.height),
                    c,
                    10);
        }
        else{
            Imgproc.circle(src,
                    new Point(x,
                            (rectangle.y + rectangle.height) - interval * (n.getString())),
                    20, c,-1);
        }
    }
}