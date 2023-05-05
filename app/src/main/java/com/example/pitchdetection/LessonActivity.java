package com.example.pitchdetection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pitchdetection.enums.ChordTypeEnum;
import com.example.pitchdetection.enums.NoteNameEnum;
import com.example.pitchdetection.lessons.Chord;
import com.example.pitchdetection.lessons.Lesson;
import com.example.pitchdetection.lessons.Major;
import com.example.pitchdetection.lessons.Minor;
import com.example.pitchdetection.lessons.Note;
import com.example.pitchdetection.services.ChordRecognitionService;
import com.example.pitchdetection.services.ChordRecognitionService.ChordRecognitionBinder;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LessonActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    /*
     * VARIABLES PARA OPENCV
     */
    CameraBridgeViewBase camera_bridge_view;
    BaseLoaderCallback base_loader_callback;

    // Imagen original, en hsv y solo con color amarillo: para el marcador
    Mat src, hsv, yellow;

    // Imagen en escala de grises y matrices para los trastes:
    Mat gray, dst, lines;
    ArrayList<double[]> frets;

    // Almacena los valores del marcador (esquina superior, ancho y alto)
    Rect rectangle;
    Boolean marker_found = false;

    // Limites superior e inferior del color amarillo en el espacio hsv
    Scalar high_limit, low_limit;
    MatOfPoint2f approx_curve;
    double min_area = 500;

    /*
     * VARIABLES PARA LOS ACORDES
     */
    NoteNameEnum chord_name  = NoteNameEnum.A;
    ChordTypeEnum chord_type = ChordTypeEnum.Major;
    int [] chord             = new int [2];
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
            connected = false;
        }
    };

    /*
     * VARIABLES DE LAS LECCIONES
     */
    // TODO: como planear las clases
    ArrayList<NoteNameEnum> chords_to_play;
    ArrayList<ChordTypeEnum> chords_types_to_play;
    private Lesson info;
    Bundle extras;
    String lesson_name;

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
                break;
            case "Minor":
                info = new Minor();
                break;
        }

        camera_bridge_view = findViewById(R.id.cameraViewer);
        camera_bridge_view.setVisibility(SurfaceView.VISIBLE);
        //Descomentar para camara frontal
        camera_bridge_view.setCameraIndex(1); //DEBUG
        camera_bridge_view.setCvCameraViewListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Mantener pantalla encendida para que no entre en suspension

        //Crear listener para la camara
        base_loader_callback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        src = new Mat();
                        hsv = new Mat();
                        yellow = new Mat();
                        gray = new Mat();
                        lines = new Mat();
                        frets = new ArrayList<>();
                        high_limit = new Scalar(100,255,255);
                        low_limit = new Scalar(80,100,100);
                        camera_bridge_view.enableView();
                        approx_curve = new MatOfPoint2f();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
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
        unbindService(connection);
        connected = false;
        Intent service_intent = new Intent(this, ChordRecognitionService.class);
        stopService(service_intent);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        chord = service.getChord();
        translateChord();
        //Log.e("Acorde: ", chord_name.toString() + " " + chord_type.toString());

        // Obtener frame en color, se usara dentro de las funciones
        src = inputFrame.rgba();
        // Voltear la imagen en el eje y para que actue como un espejo
        Core.flip(src, src, 1);

        findMarker();

        // Encontrar los trastes buscando las lineas paralelas a el lado del marcador.
        if(marker_found) {
//            Log.i("Angulo marcador", (calcMarkerAngle(rectangle.x+ rectangle.width, rectangle.y,
//                    rectangle.x + rectangle.width, rectangle.y + rectangle.height)*180/Math.PI) + "");
            detectParallelLines();
        }

        if(marker_found && frets.size() > 1)
            drawNote(info.getChord(0).get(0));


        return src;
    }

    private void findMarker() {
        // Convertir al espacio de color hsv
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
        // Con los limites definidos, aislar el color amarillo de la imagen
        Core.inRange(hsv, low_limit, high_limit, yellow);

        // Extraer todos los contornos que se pueden encontrar
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(yellow, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approx_curve, 0.02 * Imgproc.arcLength(curve, true), true);
            int n_vertices = (int) approx_curve.total();
            double area = Imgproc.contourArea(contour);

            // Descartar los contornos que no son rectangulos y con boundingRect conseguir los elementos distintivos
            if(Math.abs(area)>= min_area && n_vertices == 4) {
                rectangle = Imgproc.boundingRect(contour);
                marker_found = true;
                // DEBUG
                Imgproc.line(src,
                        new Point(rectangle.x+ rectangle.width, rectangle.y),
                        new Point(rectangle.x+rectangle.width, rectangle.y+rectangle.height),
                        new Scalar(0,0,255),8);
            }
        }
    }

    private void detectParallelLines() {
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
        Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150); // runs the actual detection

        /*
        * Una vez con todas las lineas detectadas se procesa la informacion para eliminar datos
        * innecesarios
         */
        frets = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            double rho = lines.get(i, 0)[0],
                    theta = lines.get(i, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;
            System.out.println(x0 + "    " + y0);

            // Solo plantearse añadir la linea a los trastes si es paralela al marcador
            if (compareAngle(alfa, theta)){
                System.out.println("iguales");

                // Comprobar si hay otra linea demasiado cerca, si no es el caso se acaba añadiendo
                boolean add = true;
                for (int j = 0; j < frets.size() && add; j++) {
                    if (Math.abs(frets.get(j)[0] - x0) < 100) {
                        add = false;
                    }
                }

                if (add){
                    frets.add(new double[]{x0, y0});
                    Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
                    Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
                    Imgproc.line(src, pt1, pt2, new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
                }
            }
        }

        /*
        * Para seguir eliminando lineas no deseadas y dejarlo para dibujar la informacion se ordena
        * el array de forma ascendente segun la coordenada x
         */
        Collections.sort(frets, new Comparator<double[]>() {
            @Override
            public int compare(double[] d1, double[] d2) {
                return Double.compare(d1[0], d2[0]);
            }
        });

        // Por ultimo, eliminar lineas detectadas a la "izquierda" del marcador
        for (int i = 0; i < frets.size(); i++) {
            if(Double.compare(rectangle.x+ rectangle.width, frets.get(i)[0]) < 0)
                frets.remove(i);
        }
    }

    /**
     * caclAngle : calcula el angulo de la recta que forman los puntos
     * @param x1 : coordenada x del primer punto
     * @param y1 : coordenada y del primer punto
     * @param x2 : coordenada x del segundo punto
     * @param y2 : coordenada y del segundo punto
     * @return   : angulo de la recta en radianes
     */
    private double calcAngle(double x1, double y1, double x2, double y2) {
        double pendiente = 0;

        pendiente = (y2 - y1) / (x2 - x1);

        return Math.atan(pendiente);
    }

    /**
     * compareAngle : comprueba si dos angulos son iguales
     * @param p1    : angulo 1
     * @param p2    : angulo 2
     * @return      : true si son iguales, false si no
     */
    private boolean compareAngle(double p1, double p2) {
        boolean equals = false;
        double tolerance = 75 * Math.PI / 180;

        equals = Math.abs(p1-p2) >= tolerance;

        return equals;
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
    }

    private void translateChord() {
        chord_name = NoteNameEnum.fromInteger(chord[0]);
        chord_type = ChordTypeEnum.fromInteger(chord[1]);
    }

    private void drawChord(Chord c) {
        for (int i = 0; i < c.size(); i++) {
            drawNote(c.get(i));
        }
    }

    private void drawNote(Note n) {
        int x = rectangle.x + rectangle.width;
        int y = rectangle.y;

        double interval = rectangle.height / 6.0;

        Imgproc.circle(src,
                new Point(x + frets.get(n.getFret())[0],
                          y + interval * n.getString()),
                40, new Scalar(166,119,249),-1);
    }

    private double calcFretDistance(int n) {
        return 650 - (650 / Math.pow(2, n/12.0));
    }
}