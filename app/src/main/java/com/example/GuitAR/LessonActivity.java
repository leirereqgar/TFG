package com.example.GuitAR;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
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
import org.opencv.core.Size;
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
    Rect marker;
    Boolean marker_found = false;

    // Limites superior e inferior del color amarillo en el espacio hsv
    Scalar high_limit, low_limit;
    MatOfPoint2f approx_curve;
    double min_area = 300;

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
        colors.add(new Scalar(50, 161, 181)); // Blue 1
        colors.add(new Scalar(246, 212, 94)); // Yellow 2
        colors.add(new Scalar(249, 119, 166));// Pink 3
        colors.add(new Scalar(254, 249, 255));// White 4

        camera_bridge_view = findViewById(R.id.cameraViewer);
        camera_bridge_view.setMaxFrameSize(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().heightPixels);
        camera_bridge_view.setVisibility(SurfaceView.VISIBLE);
        //DisplayMetrics metrics = getResources().getDisplayMetrics();
        //Descomentar para camara frontal
        camera_bridge_view.setCameraIndex(0); //DEBUG
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
                    }, 0, 1000);
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
        startService(service_intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Desasociar el servicio para evitar errores en un futuro
        service.stopProcessing();
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
        src  = inputFrame.rgba();
        gray = inputFrame.rgba();
        // Voltear la imagen en el eje y para que actue como un espejo
        //Core.flip(src, src, 1);

        printActualChordInfo();

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
            service.stopProcessing();

            Intent change = new Intent(this, LessonSelectionScreen.class);
            startActivity(change);
        }

        return src;
    }

    /**
     * printActualChordInfo: muestra por pantalla cual es el nombre del acorde que hay que tocar
     * y el indice para llevar la cuenta de los aciertos
     */
    private void printActualChordInfo() {
        Imgproc.putText(src, info.getChord(index).toString(),
                new Point(10,100), 0,4,new Scalar(0,0,0), 4);
        Imgproc.putText(src, index+"", new Point(10,200), 0,4,new Scalar(0,0,0), 4);
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
        //Imgproc.Canny(yellow_img, yellow_img, 0,100);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(yellow_img, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approx_curve, 0.02 * Imgproc.arcLength(curve, true), true);
            int n_vertices = (int) approx_curve.total();
            double area = Imgproc.contourArea(contour);

            if(Math.abs(area) >= min_area && n_vertices == 4) {
                marker = Imgproc.boundingRect(contour);
                marker_found = true;
                // DEBUG
                Imgproc.rectangle(src, new Point(marker.x, marker.y),
                        new Point(marker.x+ marker.width, marker.y+ marker.height),
                        new Scalar(0,0,255), 3, Imgproc.LINE_8);
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
     *      - Si no es paralela al marcador
     *      - Si esta muy cerca de otra linea ya añadida
     *      - Si esta fuera del mastil
     *      - Tiene coordenada x negativa
     * 7.- Se ordenan todas las lineas de forma ascendente
     */
    private final double rule_cte = 17.817;
    private Double scale_length = null;
    private void detectParallelLines() {
        //Calcular el angulo del lado del marcador
        double alfa = calcAngle(marker.x+ marker.width, marker.y,
                                marker.x + marker.width, marker.y + marker.height);

        dst = new Mat();
        Imgproc.cvtColor(gray, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(gray, dst, 50, 200, 3, false);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size((2*2) + 1, (2*2)+1));
        Imgproc.dilate(dst, dst, kernel);
        Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150);

        frets = new ArrayList<>();
        frets.add(new double[]{marker.x+ marker.width, marker.y});
        //System.out.println("Marcador" + (rectangle.x));
        for (int i = 0; i < lines.rows(); i++) {
            double rho = lines.get(i, 0)[0],
                    theta = lines.get(i, 0)[1];
            double x0 = Math.cos(theta) * rho;

            // Solo añadir la linea a los trastes si es paralela al marcador
            if (compareAngles(alfa, theta)){
                // Comprobar si hay otra linea demasiado cerca, tiene coordenadas negativas o
                // esta a la "izquierda" del marcador, si no estamos en ninguno de estos casos
                // se aniade
                boolean add = true;
                for (int j = 0; j < frets.size() && add; j++) {
                    if (Math.abs(frets.get(j)[0] - x0) < 100 || x0 < 0
                      || Double.compare(marker.x + marker.width, x0) > 0) {
                        //System.out.println(x0);
                        add = false;
                    }
                }

                if (add) {
                    frets.add(new double[]{x0, marker.y});
                }
            }
        }

        frets.sort(Comparator.comparingDouble(d -> d[0]));

        checkFrets();
        calcFrets();
    }

    /**
     * calcRealDistance : calcula la distancia real entre dos puntos en cm (solo en una coordenada)
     * @param x1        : coordenada x del primer punto
     * @param x2        : coordenada x del segundo punto
     * @return          : distancia real en cm de ambos puntos
     */
    private double calcRealDistance(double x1, double x2) {
        int dpi = getResources().getDisplayMetrics().densityDpi;
        double inches2cm = 2.54;
        return Math.abs(x2 - x1)*inches2cm/440;
    }

    private void checkFrets() {
        int size = frets.size();
        ArrayList<double[]> frets_copy;
        ArrayList<double[]> candidates= new ArrayList<>();
        candidates.add(frets.get(0));
        candidates.add(frets.get(1));
        int i = 1, max = 2;
        while (i < size) {
            frets_copy =  new ArrayList<>(frets);
            double d = Math.abs(frets.get(i)[0] - frets.get(0)[0]);
            //System.out.println(i + " " + (i-1)+ " " + d + "");
            double l = d * rule_cte;
            checkFret(i,l,d,frets_copy,candidates);

            frets_copy.remove(i);
            size--;

            if(candidates.size() >= max){
                max = candidates.size();
            }
            else {
                candidates.clear();
                candidates.add(frets_copy.get(0));
                candidates.add(frets_copy.get(i));
            }
        }

        if(candidates.size() > 2) {
            frets = candidates;
            scale_length = (frets.get(1)[0] - frets.get(0)[0]) * rule_cte;
            System.out.println("cambio");
        }
        else {
            frets.clear();
            frets.add(candidates.get(0));
        }
    }
    private void checkFret(int i, double l, double dist,ArrayList<double[]> frets_copy, ArrayList<double[]> candidates) {
        int j = i+1;
        boolean cont = true;
        if (j != (frets_copy.size()-1)){
            while (j < frets_copy.size() && cont) {
                double d = Math.abs(frets_copy.get(j)[0] - frets_copy.get(i)[0]);
                //System.out.println(j + " " + i + " " + d + " backtracking " + (l - dist) / rule_cte);
                if (Math.abs(d - (l - dist) / rule_cte) < 5) {
                    System.out.println("siguiente");
                    candidates.add(frets_copy.get(j));
                    checkFret(j, l, d + dist, frets_copy,candidates);
                    cont = false;
                } else {
                    System.out.println("elimina" + j);
                    frets_copy.remove(j);
                }
            }
        }
    }

    private final int min_frets = 6;
    private void calcFrets() {
        double actual_lenght, diff, x;

        if(scale_length == null){
            scale_length = 25.5 * getResources().getDisplayMetrics().densityDpi;
            actual_lenght = scale_length;
            for (int i = 1; i < min_frets; i++) {
                diff = actual_lenght/rule_cte;
                actual_lenght -= diff;
                x = diff + frets.get(i-1)[0];

                frets.add(new double[]{x, marker.y});
            }

        } else if (frets.size() < 6) {
            actual_lenght = scale_length;

            for (int i = 1; i < frets.size(); i++) {
                actual_lenght -= frets.get(i)[0] - frets.get(i-1)[0];
            }

            for (int i = frets.size()-1; i < min_frets; i++) {
                diff = actual_lenght/rule_cte;
                actual_lenght-=diff;
                x = diff + frets.get(i)[0];

                frets.add(new double[] {x, marker.y});
            }
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
     * @param p1     : pendiente 1
     * @param p2     : pendiente 2
     * @return       : true si son iguales, false si no
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
        if(!frets.isEmpty()){
            double x = (frets.get(n.getFret() - 1)[0] + frets.get(n.getFret())[0]) / 2; // Calcular la posicion entre trastes

            double interval = marker.height / 6.0;

            if (n.getString() == 0) {
                Imgproc.line(src,
                        new Point(x, marker.y),
                        new Point(x,
                                marker.y + marker.height),
                        c, 10);
            } else {
                Imgproc.circle(src,
                        new Point(x,
                                (marker.y + marker.height) - interval * (n.getString())),
                        20, c, -1);
            }
        }
    }
}