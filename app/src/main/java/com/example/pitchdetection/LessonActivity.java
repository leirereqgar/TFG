package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

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

public class LessonActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    CameraBridgeViewBase camera_bridge_view;
    BaseLoaderCallback base_loader_callback;

    // Imagen original, en hsv y solo con color amarillo
    Mat src, hsv, yellow;

    Rect rectangle; // DEBUG

    // Limites superior e inferior del color amarillo en el espacio hsv
    Scalar high_limit, low_limit;
    MatOfPoint2f approx_curve;
    double max_area = 500;

    private int request_permission_code = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        camera_bridge_view = findViewById(R.id.cameraViewer);
        camera_bridge_view.setVisibility(SurfaceView.VISIBLE);
        //Descomentar para camara frontal
        camera_bridge_view.setCameraIndex(0);
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

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Obtener frame en color
        src = inputFrame.rgba();
        // Convertir al espacio de color hsv
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
        // Con los limites definidos, aislar el color amarillo de la imagen
        Core.inRange(hsv, low_limit, high_limit, yellow);

        // Extraer todos los contornos que se pueden encontrar
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(yellow, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        int i = 0;
        for (MatOfPoint contour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approx_curve, 0.02 * Imgproc.arcLength(curve, true), true);

            int n_vertices = (int) approx_curve.total();
            double area = Imgproc.contourArea(contour);
            // Descartar los contornos que no son rectangulos y con boundingRect conseguir los elementos distintivos
            if(Math.abs(area)>=max_area && n_vertices == 4 && n_vertices <= 6) {
                rectangle = Imgproc.boundingRect(contour);
                Imgproc.circle(src, new Point(rectangle.x, rectangle.y),8, new Scalar(255,255,255),-1);
                Imgproc.line(src,
                        new Point(rectangle.x, rectangle.y+rectangle.height/2),
                        new Point(rectangle.x+rectangle.width, rectangle.y+rectangle.height/2),
                        new Scalar(0,0,0),8);
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
}