package com.example.user.facedetectionapp;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    SurfaceView surfaceView;
    Camera mCamera;
    Bitmap bitmap;
    int mWidth, mHeight;
    FilteredImageView customView;
    ByteBuffer mPreviewBuffer;
    int camera = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customView = (FilteredImageView) findViewById(R.id.filteredView);
        surfaceView = (SurfaceView) findViewById(R.id.preview);
        surfaceView.getHolder().addCallback(this);


        safeCameraOpen(camera);

    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e("Joony", "failed to open Camera");
        }
        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private void startPreview(SurfaceHolder holder) {
        if( mCamera == null ) safeCameraOpen(camera);
        mCamera.stopPreview();
        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(640,480);
        mCamera.setParameters(p);
        mWidth  = p.getPreviewSize().width;
        mHeight = p.getPreviewSize().height;
        mPreviewBuffer = ByteBuffer.allocateDirect(mHeight*mWidth*3/2);
        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCamera.setDisplayOrientation(90);
        if(customView != null) {
            customView.setBitmap(bitmap);
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera.addCallbackBuffer(mPreviewBuffer.array());
        mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.startPreview();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void processImage(ByteBuffer buffer, int width, int height, Bitmap bitmap);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onResume() {
        super.onResume();
        safeCameraOpen(camera);
    }

    @Override
    protected void onPause() {
        releaseCameraAndPreview();
        super.onPause();

    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startPreview(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        startPreview(surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        mPreviewBuffer.rewind();
        processImage(mPreviewBuffer,mWidth, mHeight,bitmap);
        customView.invalidate();
        camera.addCallbackBuffer(mPreviewBuffer.array());
    }
}
