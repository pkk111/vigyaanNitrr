package com.pk.vigyaan.currencydetector;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

import static android.content.ContentValues.TAG;
import static android.content.Context.WINDOW_SERVICE;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private Context context;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        this.mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
//            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//
//                }
//            });
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Utils.showLongToast(context, "Camrea released");
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager)context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0) {
            parameters.setPreviewSize(h, w);
            mCamera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90) {
            parameters.setPreviewSize(w, h);
        }

        if(display.getRotation() == Surface.ROTATION_180) {
            parameters.setPreviewSize(h, w);
        }

        if(display.getRotation() == Surface.ROTATION_270) {
            parameters.setPreviewSize(w, h);
            mCamera.setDisplayOrientation(180);
        }
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}

