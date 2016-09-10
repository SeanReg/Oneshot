package com.cop4331.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.cop4331.com.cop4331.permissions.PermissionRequester;
import com.cop4331.oneshot.R;

public class CameraActivity extends AppCompatActivity {

    private PermissionRequester mPermission    = null;
    private SurfaceView         mCameraView    = null;
    private ImageButton         mCaptureButton = null;
    private int                 mCameraType    = CameraCharacterizer.FRONT_CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraView = (SurfaceView)findViewById(R.id.cameraView);
        mCameraView.getHolder().addCallback(mCameraSurfaceListener);

        mCaptureButton = (ImageButton)findViewById(R.id.captureButton);
        mCaptureButton.setOnClickListener(mCaptureListener);

        ImageButton switchCam = (ImageButton)findViewById(R.id.switchCamera);
        switchCam.setOnClickListener(mSwitchCameraListener);
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

    private void startCamera(int cameraType) {
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

    private SurfaceHolder.Callback mCameraSurfaceListener = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            startCamera(mCameraType);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

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
                camHandler.startFeed(mCameraView.getHolder().getSurface());
            }catch (CameraAccessException e) {
                finish();
            }
        }

        @Override
        public void onEnded() {

        }

        @Override
        public void onImageCaptured(Image capturedImage) {
            Log.d("CameraActivity", "Got image from ImageReader");
        }
    };

    private ImageButton.OnClickListener mCaptureListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                CameraHandle.getInstance().captureImage();
            }catch (CameraAccessException e) {
                finish();
            }
        }
    };

    private ImageButton.OnClickListener mSwitchCameraListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCameraType == CameraCharacterizer.BACK_CAMERA) {
                startCamera(CameraCharacterizer.FRONT_CAMERA);
            } else {
                startCamera(CameraCharacterizer.BACK_CAMERA);
            }
        }
    };


}
