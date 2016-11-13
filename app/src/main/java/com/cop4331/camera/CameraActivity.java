package com.cop4331.camera;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.cop4331.com.cop4331.permissions.PermissionRequester;
import com.cop4331.image_manipulation.AmendedBitmap;
import com.cop4331.image_manipulation.ImageManipulateTest;
import com.cop4331.oneshot.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    private PermissionRequester mPermission    = null;
    private TextureView         mCameraView    = null;
    private CameraCharacterizer.CameraType mCameraType    = CameraCharacterizer.CameraType.FRONT_CAMERA;

    private ToggleButton mFlashButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraView = (TextureView)findViewById(R.id.cameraView);
        mCameraView.setSurfaceTextureListener(mCameraSurfaceListener);

        ImageButton captureButton = (ImageButton)findViewById(R.id.captureButton);
        captureButton.setOnClickListener(mCaptureListener);

        ImageButton switchCam = (ImageButton)findViewById(R.id.switchCamera);
        switchCam.setOnClickListener(mSwitchCameraListener);

        mFlashButton = (ToggleButton)findViewById(R.id.flashButton);
    }

    @Override
    protected void onPause() {
        super.onPause();

        CameraHandle.getInstance().stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startCamera(CameraCharacterizer.CameraType cameraType) {
        CameraHandle camHandler = CameraHandle.getInstance();

        //if (camHandler.getCameraConnected()) return;

        mCameraType = cameraType;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        camHandler.setCameraStatusListener(mCameraStatus);

        try {
            camHandler.openCamera(manager, cameraType);
        } catch (SecurityException e) {
            //Need to request permissions again
            mPermission = new PermissionRequester(this);
            mPermission.setResultListener(mCameraPermissionListener);
            mPermission.requestPermission(Manifest.permission.CAMERA);
        } catch (CameraAccessException e2) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermission.onPermissionResult(requestCode, permissions, grantResults);
    }

    private TextureView.SurfaceTextureListener mCameraSurfaceListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            startCamera(mCameraType);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private PermissionRequester.ResultListener mCameraPermissionListener = new PermissionRequester.ResultListener() {
        @Override
        public void onAccessGranted(String permission) {
            //Camera granted - redo connection
            startCamera(mCameraType);
        }

        @Override
        public void onAccessDenied(String permission) {
            //User doesn't want to use their camera - leave activity
            finish();
        }
    };

    private CameraHandle.CameraStatusCallback mCameraStatus = new CameraHandle.CameraStatusCallback() {
        @Override
        public void onConnected() {
            CameraHandle camHandler = CameraHandle.getInstance();

            //Start the camera preview feed
            try {
                Surface previewSurface = new Surface(mCameraView.getSurfaceTexture());
                camHandler.startFeed(previewSurface);

                Size previewSize = new Size(mCameraView.getWidth(), mCameraView.getHeight());
                if (!screenIsLandscape()) {
                    previewSize = new Size(previewSize.getHeight(), previewSize.getWidth());
                }

                fixViewAspect(mCameraView, camHandler.getSupportedResolution(previewSize));
            }catch (CameraAccessException e) {
                finish();
            }
        }

        @Override
        public void onEnded() {

        }

        @Override
        public void onImageCaptured(AmendedBitmap capturedImage) {
            Log.d("CameraActivity", "Got image from ImageReader");
            mCameraView.setVisibility(View.INVISIBLE);

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("bitmap", Context.MODE_PRIVATE);
            if (!directory.exists()) {
                directory.mkdir();
            }

            FileOutputStream fStream = null;
            File filePath = null;
            try {
                filePath = File.createTempFile("bitmap", ".png", directory);
                fStream = new FileOutputStream(filePath);
                capturedImage.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fStream);
                fStream.close();

                Intent imgEditIntent = new Intent(getApplicationContext(), ImageManipulateTest.class);
                imgEditIntent.putExtra("Bitmap", filePath.getAbsoluteFile());
                startActivity(imgEditIntent);
            } catch (IOException e) {

            }

        }
    };

    private boolean screenIsLandscape() {
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    private void fixViewAspect(View fixView, Size resolution) {
        if (!screenIsLandscape()) {
            resolution = new Size(resolution.getHeight(), resolution.getWidth());
        }

/*        int scaledWidth = 0;
        int scaledHeight = 0;
        //Find the width or height constraint
        if (fixView.getWidth() > fixView.getHeight()) {
            scaledWidth  = fixView.getWidth();
            //Scale the height proportionately with the width
            scaledHeight = (int)(fixView.getWidth() * aspectRatio);
        } else {
            //Scale the width proportionately with the height
            scaledWidth  = (int)(fixView.getHeight() * aspectRatio);
            scaledHeight = fixView.getHeight();
        }*/
        if (fixView.getWidth() > resolution.getWidth() || fixView.getHeight() > resolution.getHeight()) {
            //Get the aspect ratio of the camera resolution
            double aspectRatio = resolution.getHeight() / (double)resolution.getWidth();

            int scaledWidth = 0;
            int scaledHeight = 0;
            //Find the width or height constraint
            if (fixView.getWidth() > fixView.getHeight()) {
                scaledWidth  = fixView.getWidth();
                //Scale the height proportionately with the width
                scaledHeight = (int)(fixView.getWidth() * aspectRatio);
            } else {
                //Scale the width proportionately with the height
                scaledWidth  = (int)(fixView.getHeight() * aspectRatio);
                scaledHeight = fixView.getHeight();
            }

            fixView.setScaleX((float)scaledWidth / fixView.getWidth());
            fixView.setScaleX((float)scaledHeight / fixView.getHeight());
        } else {
            //Set the new scales of the TextureView
            fixView.setScaleX((float)resolution.getWidth() / fixView.getWidth());
            fixView.setScaleY((float)resolution.getHeight() / fixView.getHeight());
        }
    }

    private ImageButton.OnClickListener mCaptureListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                CameraHandle.getInstance().captureImage(mFlashButton.isChecked());
            }catch (CameraAccessException e) {
                finish();
            }
        }
    };

    private ImageButton.OnClickListener mSwitchCameraListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCameraType.getCameraType() == CameraCharacterizer.CameraType.BACK_CAMERA.getCameraType()) {
                startCamera(CameraCharacterizer.CameraType.FRONT_CAMERA);
            } else {
                startCamera(CameraCharacterizer.CameraType.BACK_CAMERA);
            }
        }
    };


}
