package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.pitchdetection.services.ChordRecognitionService;

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
import java.util.List;

import enums.ChordTypeEnum;
import enums.NoteNameEnum;

public class LessonActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    /*
     * VARIABLES PARA OPENCV
     */
    CameraBridgeViewBase camera_bridge_view;
    BaseLoaderCallback base_loader_callback;

    // Imagen original, en hsv y solo con color amarillo
    Mat src, hsv, yellow;

    // Almacena los valores del rectangulo (esquina superior, ancho y alto)
    Rect rectangle;

    // Limites superior e inferior del color amarillo en el espacio hsv
    Scalar high_limit, low_limit;
    MatOfPoint2f approx_curve;
    double min_area = 500;

    /*
     * VARIABLES PARA LOS ACORDES
     */
    // TODO: recibir datos del servicio y traducirlos a enums
    NoteNameEnum chord_name  = NoteNameEnum.A;
    ChordTypeEnum chord_type = ChordTypeEnum.Major;
    int [] chord             = new int [2];

    /*
     * VARIABLES DE LAS LECCIONES
     */
    // TODO: como planear las clases
    ArrayList<NoteNameEnum> chords_to_play;
    ArrayList<ChordTypeEnum> chords_types_to_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        camera_bridge_view = findViewById(R.id.cameraViewer);
        camera_bridge_view.setVisibility(SurfaceView.VISIBLE);
        //Descomentar para camara frontal
        camera_bridge_view.setCameraIndex(1); //DEBUG
        camera_bridge_view.setCvCameraViewListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //create camera listener callback
        base_loader_callback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        src = new Mat();
                        hsv = new Mat();
                        yellow = new Mat();
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

        //Asociar el service para ir escuchando las frecuencias
//        Intent service_intent = new Intent(this, ChordRecognitionService.class);
//        startService(service_intent);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //chord = ChordRecognitionService.getInstance().chord();
        //translateChord();

        // Obtener frame en color
        src = inputFrame.rgba();
        // Voltear la imagen en el eje y para que actue como un espejo
        Core.flip(src, src, 1);  //DEBUG
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
            if(Math.abs(area)>= min_area && n_vertices == 4 && n_vertices <= 6) {
                rectangle = Imgproc.boundingRect(contour);

                Log.e("anchura", rectangle.height + "");
                double intervalo = rectangle.height / 6.0;
                Imgproc.circle(src, new Point(rectangle.x+rectangle.width+400, rectangle.y+intervalo*4),40, new Scalar(166,119,249),-1);
                Imgproc.circle(src, new Point(rectangle.x+rectangle.width+400,rectangle.y+intervalo*3),40, new Scalar(210,192,93),-1);
                Imgproc.circle(src, new Point(rectangle.x+rectangle.width+400,rectangle.y+intervalo*2),40, new Scalar(94,212,246),-1);

                // DEBUG
//                Imgproc.line(src,
//                        new Point(rectangle.x, rectangle.y+rectangle.height/2),
//                        new Point(rectangle.x+rectangle.width+5000000, rectangle.y+rectangle.height/2),
//                        new Scalar(0,0,255),8);
            }
        }

        return src;
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

    private void drawChord() {

    }
}