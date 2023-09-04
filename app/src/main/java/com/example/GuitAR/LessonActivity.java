package com.example.GuitAR;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
    ///////////////////////////////////////////////////////////////////
    //VARIABLES PARA OPENCV
    ///////////////////////////////////////////////////////////////////
    CameraBridgeViewBase camera_bridge_view;
    BaseLoaderCallback base_loader_callback;
    Mat src, hsv, yellow_img, gray, dst, lines; // Imagen original, en hsv y solo con color amarillo: para el marcador
    Rect marker; // Almacena los valores del marcador (esquina superior, ancho y alto)
    Boolean marker_found = false;
    Scalar high_limit, low_limit; // Limites superior e inferior del color amarillo en el espacio hsv
    MatOfPoint2f approx_curve;
    private final int min_frets = 8;
    ArrayList<double[]> frets = new ArrayList<>(); // Array para los trastes y las cuerdas

    ///////////////////////////////////////////////////////////////////
    // TIMERS PARA EJECUTAR LAS DETECCIONES
    ///////////////////////////////////////////////////////////////////
    Timer find_marker_task;
    Timer detect_fret_task;

    ///////////////////////////////////////////////////////////////////
    // VARIABLES PARA LOS ACORDES Y LAS LECCIONES
    ///////////////////////////////////////////////////////////////////
    NoteNameEnum chord_name  = NoteNameEnum.NO_NOTE;
    ChordTypeEnum chord_type = ChordTypeEnum.NoChord;
    int [] chord             = new int [2];
    ArrayList<Scalar> colors;
    Scalar black = new Scalar(35,28,35);
    Scalar white = new Scalar(254,249,255);
    Scalar orange = new Scalar(246,160,8);
    private Lesson info; // Donde se encuentran los datos de la leccion
    Bundle extras; // Recoger datos pasados de la actividad anterior
    String lesson_name;
    int index = 0;

    ///////////////////////////////////////////////////////////////////
    // PASOS PARA CREAR Y ASOCIAR EL SERVICIO DE RECONOCIMIENTO DE ACORDES
    ///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    // METODOS DE APPCOMPATACTIVITY
    ///////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initLesson();
        initColors();
        configCamera();
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
        Intent service_intent = new Intent(this, ChordRecognitionService.class);
        stopService(service_intent);
        unbindService(connection);
        connected = false;

        detect_fret_task.cancel();
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
        detect_fret_task.cancel();
        find_marker_task.cancel();
    }

    ///////////////////////////////////////////////////////////////////
    // METODOS HEREDADOS DE CameraBridgeViewBase.CvCameraListenerV2
    ///////////////////////////////////////////////////////////////////
    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    /**
     * onCameraFrame : en cada frame se localiza el marcador y se dibujan los elementos necesarios
     * @param inputFrame : frame recibido desde la camara
     * @return frame con todos los elementos dibujados
     */
    static boolean first_frame = true;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        chord = service.getChord();
        //System.out.println(chord_name);
        translateChord();

        // Obtener frame en color, se usara dentro de las funciones
        src  = inputFrame.rgba().clone();
        gray = inputFrame.rgba().clone();
        // Voltear la imagen en el eje y para que actue como un espejo
        Core.flip(src, src, 1);

        if(first_frame) {
            first_frame = false;
            
            calibrate(inputFrame.gray());

            return src;
        }

        printActualChordInfo();

        //Imgproc.circle(src, new Point(marker.x, marker.y),20, new Scalar(255,0,0),-1);
        Imgproc.ellipse(src, new Point(marker.x, marker.y), new Size(10,30),0,0,360,orange,20);
        for (int i = 0; i < frets.size(); i++) {
            Point pt = new Point(frets.get(i)[0], frets.get(0)[1]-10);
            Imgproc.ellipse(src, pt, new Size(10,30),0,0,360,orange,20);
            pt.x -= 10;
            pt.y += 5;
            Imgproc.putText(src, Integer.toString(i), pt, 0,1,white, 4);
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

        return src;
    }

    ///////////////////////////////////////////////////////////////////
    // METODOS PARA PROCESADO DE LA IMAGEN
    ///////////////////////////////////////////////////////////////////
    private int th_lower, th_hihger;
    private void calibrate(Mat gray) {
        int lower = 0, higher = 255;
        int min_lower = lower, max_higher = higher;
        int i = 2;
        int max_lines = 0;

        while (higher > lower) {
            int l = countLines(gray, lower, higher);

            if(l > max_lines) {
                max_lines = l;
                min_lower = lower;
                max_higher = higher;
            }
            higher -= i;
            lower  += i;
            i *= i;
        }

        th_lower = min_lower;
        th_hihger = max_higher;
        Log.i("th", "lower: " + th_lower + "  higher " + th_hihger);
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
        Imgproc.Canny(yellow_img, yellow_img, 10, 100);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size((2*2) + 1, (2*2)+1));
        Imgproc.dilate(yellow_img, yellow_img, kernel);
        Rect min = new Rect(new double[]{0,0,yellow_img.cols()/2d,yellow_img.rows()});
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(yellow_img.submat(min), contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        //marker_found = false;

        for (MatOfPoint contour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approx_curve, 0.02 * Imgproc.arcLength(curve, true), true);
            int n_vertices = (int) approx_curve.total();
            double area = Imgproc.contourArea(contour);
            double old_area = marker.width * marker.height;
            if(Math.abs(area) >= 1000  && n_vertices == 4 && area >= old_area) {
                marker = Imgproc.boundingRect(contour);
                marker_found = true;
                // DEBUG
                Imgproc.rectangle(src, new Point(marker.x, marker.y),
                        new Point(marker.x+ marker.width, marker.y+ marker.height),
                        new Scalar(0,0,255), 3, Imgproc.LINE_8);
            }
        }
    }

    public int countLines(Mat gray, int l, int h) {
        //Calcular el angulo del lado del marcador
        double marker_slope = calcSlope(marker.x, marker.y,
                marker.x + marker.width, marker.y);

        dst = new Mat();
        Imgproc.Canny(gray, dst, l, h, 3);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size((2*2) + 1, (2*2)+1));
        Imgproc.dilate(dst, dst, kernel);
        Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150);

        return lines.rows();
    }

    private final double rule_cte = 17.817;
    private Double scale_length = null;
    /**
     * detectLines : detecta las lineas que corresponden a los trastes
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
    private void detectLines() {
        //Calcular el angulo del lado del marcador
        double marker_slope = calcSlope(marker.x, marker.y,
                                marker.x + marker.width, marker.y);

        dst = new Mat();
        Imgproc.cvtColor(gray, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(gray, dst, 50, 200, 3, false);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size((2*2) + 1, (2*2)+1));
        Imgproc.dilate(dst, dst, kernel);
        Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150);

        ArrayList<double[]> possible_frets = new ArrayList<>();
        possible_frets.add(new double[]{marker.x+ marker.width, marker.y,0,0});
        for (int i = 0; i < lines.rows(); i++) {
            double rho = lines.get(i, 0)[0],
                    theta = lines.get(i, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b*rho;
            double line_slope = calcSlope(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)),
                                          Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
            // Solo añadir la linea a los trastes si es paralela al marcador
            if (arePerpendicular(marker_slope, line_slope)){
                // Comprobar si hay otra linea demasiado cerca, tiene coordenadas negativas o
                // esta a la "izquierda" del marcador, si no estamos en ninguno de estos casos
                // se aniade
                boolean add = true;
                for (int j = 0; j < possible_frets.size() && add; j++) {
                    if (Math.abs(possible_frets.get(j)[0] - x0) < 150 || x0 < 0
                            || Double.compare(marker.x + marker.width, x0) > 0) {
                        add = false;
                    }
                }

                if (add) {
                    possible_frets.add(new double[]{x0, y0, a, b});
                }
            }
        }

        possible_frets.sort(Comparator.comparingDouble(d -> d[0]));
        double variation = 0;
        if(!frets.isEmpty())
            variation = Math.abs(frets.get(1)[0] - possible_frets.get(1)[0]);
        if(frets.isEmpty() || variation >= 100) {
            checkFrets(possible_frets);
            frets = possible_frets;
        }
    }

    private void checkFrets(ArrayList<double[]> possible_frets) {
        int size = possible_frets.size();
        ArrayList<double[]> frets_copy;
        ArrayList<double[]> candidates= new ArrayList<>();
        candidates.add(possible_frets.get(0));
        candidates.add(possible_frets.get(1));
        int i = 1, max = 2;
        while (i < size) {
            frets_copy =  new ArrayList<>(possible_frets);
            double d = Math.abs(possible_frets.get(i)[0] - possible_frets.get(0)[0]);
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

        if(candidates.size() >= 2) {
            possible_frets = candidates;
            scale_length = (possible_frets.get(1)[0] - possible_frets.get(0)[0]) * rule_cte;
            //System.out.println("cambio");
        }
        else {
            possible_frets.clear();
            possible_frets.add(candidates.get(0));
        }

        calcFrets(possible_frets);
    }
    private void checkFret(int i, double l, double dist,ArrayList<double[]> frets_copy, ArrayList<double[]> candidates) {
        int j = i+1;
        boolean cont = true;
        if (j != (frets_copy.size()-1)){
            while (j < frets_copy.size() && cont) {
                double d = Math.abs(frets_copy.get(j)[0] - frets_copy.get(i)[0]);
                if (Math.abs(d - (l - dist) / rule_cte) < 5) {
                    candidates.add(frets_copy.get(j));
                    checkFret(j, l, d + dist, frets_copy,candidates);
                    cont = false;
                } else {
                    frets_copy.remove(j);
                }
            }
        }
    }
    private void calcFrets(ArrayList<double[]> possible_frets) {
        double actual_lenght, diff, x;

        if(scale_length == null){
            actual_lenght = 25.5 * getResources().getDisplayMetrics().xdpi;
            for (int i = 1; i < min_frets; i++) {
                diff = actual_lenght/rule_cte;
                actual_lenght -= diff;
                x = diff + possible_frets.get(i-1)[0];

                frets.add(new double[]{x, marker.y,0,0});
            }

        }
        else if (possible_frets.size() < min_frets) {
            actual_lenght = scale_length;

            for (int i = 1; i < possible_frets.size(); i++) {
                actual_lenght -= possible_frets.get(i)[0] - possible_frets.get(i-1)[0];
            }

            for (int i = possible_frets.size()-1; i < min_frets; i++) {
                diff = actual_lenght/rule_cte;
                actual_lenght-=diff;
                x = diff + possible_frets.get(i)[0];

                possible_frets.add(new double[] {x, marker.y,0,0});
            }

            frets = possible_frets;
        }
    }

    /**
     * calcSlope : calcula el angulo de la recta que forman los puntos
     * @param x1 : coordenada x del primer punto
     * @param y1 : coordenada y del primer punto
     * @param x2 : coordenada x del segundo punto
     * @param y2 : coordenada y del segundo punto
     * @return   : angulo de la recta en radianes
     */
    private double calcSlope(double x1, double y1, double x2, double y2) {
        return (y2 - y1) / (x2 - x1);
    }

    /**
     * areParallel   : comprueba si dos pendientes son iguales
     * @param p1     : pendiente 1
     * @param p2     : pendiente 2
     * @return       : true si son iguales, false si no
     */
    private boolean areParallel(double p1, double p2) {
        return Math.abs(p1-p2) <= 0.01;
    }

    /**
     * areParallel   : comprueba si dos rectas son perpendiculares con sus pendientes
     * @param m1     : pendiente 1
     * @param m2     : pendiente 2
     * @return       : true si son perpendiculares, false si no
     */
    private boolean arePerpendicular(double m1, double m2){
        return Math.abs(m1-1/m2) <= 0.1;
    }

    ///////////////////////////////////////////////////////////////////
    // METODOS DE INICIALIZACION
    ///////////////////////////////////////////////////////////////////
    private void initLesson() {
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
            case "Shake It Off":
                info = new ShakeItOff();
                System.out.println(lesson_name);
                break;
            case "I Gotta Feelin":
                info = new IGottaFeelin();
                System.out.println(lesson_name);
                break;
            case "Zombie":
                info = new Zombie();
                System.out.println(lesson_name);
                break;
            case "Accidentaly In Love":
                info = new AccidentalyInLove();
                System.out.println(lesson_name);
                break;
        }
    }

    private void initColors() {
        colors = new ArrayList<>();
        colors.add(new Scalar(50, 161, 181)); // Blue 1
        colors.add(new Scalar(246, 212, 94)); // Yellow 2
        colors.add(new Scalar(249, 119, 166));// Pink 3
        colors.add(new Scalar(254, 249, 255));// White 4
    }

    private void configCamera(){
        camera_bridge_view = findViewById(R.id.cameraViewer);
        camera_bridge_view.setVisibility(SurfaceView.VISIBLE);
        //Descomentar para camara frontal
        //camera_bridge_view.setMaxFrameSize(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().heightPixels);
        camera_bridge_view.setCameraIndex(1);
        camera_bridge_view.setCvCameraViewListener(this);
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
                    marker = new Rect(new double[] {0,0,0,0});

                    createTimers();
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
    }

    private void createTimers() {
        /*
         * La deteccion de trastes se hace con un Timer,
         * la ejecucion empieza despues de crearse
         * la actividad.
         * Se ejecuta la funcion cada 5 segundos
         */
        find_marker_task = new Timer();
        find_marker_task.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    findMarker();
                } catch (Exception e){}
            }
        },0, 1000);

        detect_fret_task = new Timer();
        detect_fret_task.schedule(new TimerTask() {
            @Override
            public void run() {
                if(marker_found)
                    try{
                        detectLines();
                    }catch (Exception e){}
            }
        }, 0, 1000);
    }


    ///////////////////////////////////////////////////////////////////
    // METODOS REFERENTES A LOS ACORDES
    ///////////////////////////////////////////////////////////////////
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
            double x = (frets.get(n.getFret() - 1)[0] + frets.get(n.getFret())[0]) / 2 + 10; // Calcular la posicion entre trastes

            double interval = marker.height / 6.0;

            if (n.getString() == 0) {
                Imgproc.line(src,
                        new Point(x + 5, marker.y),
                        new Point(x + 5,
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

    /**
     * printActualChordInfo: muestra por pantalla cual es el nombre del acorde que hay que tocar
     * y el indice para llevar la cuenta de los aciertos
     */
    private void printActualChordInfo() {
        Imgproc.rectangle(src, new Point(0,0), new Point(500,210),black,-1);
        Imgproc.putText(src, info.getChord(index).toString(),
                new Point(10,100), 0,4,white, 4);
        Imgproc.putText(src, index+"", new Point(10,200), 0,4, white,4);
    }
}