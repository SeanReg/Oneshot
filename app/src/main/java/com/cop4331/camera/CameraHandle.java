package com.cop4331.camera;

import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sean on 9/9/2016.
 */
public class CameraHandle {
    /**
     * Static reference to the single instance of the CameraHandle class - Singleton
     */
    private static CameraHandle mCameraHandle = null;

    //Device and session references
    private CameraCaptureSession  mCaptureSession     = null;
    private CameraDevice          mCameraDevice       = null;
    private CameraCharacterizer   mCharacterizer      = null;

    Surface[] mRecordSurfaces = null;

    private ImageReader mImageReader = null;

    //Threads
    private Handler       mRequestHandler = null;
    private HandlerThread mRequestThread = null;

    //Callback listners
    private CameraStatusCallback  mCameraStatsListener = null;

    /**
     * Interface to get the status of the camera
     */
    public interface CameraStatusCallback {
        public void onConnected();
        public void onEnded();
        public void onImageCaptured(Image capturedImage);
        public void onResolutionPicked(Size resolution);
    }

    //Private - only one instance - Singleton design
    private CameraHandle() {
    }

    /**
     * The default instance of the CameraHandle
     * @return the default instance of the CameraHandle
     */
    public static CameraHandle getInstance() {
        if (mCameraHandle == null) mCameraHandle = new CameraHandle();
        return mCameraHandle;
    }

    /**
     * Attempts to open and connect to the system's camera device
     * @param manager the system's CameraManager
     * @param cameraType a CameraCharacterizer constant denoting the desired type of camera (Front or Back)
     * @throws CameraAccessException thrown if there was an error with opening the camera device
     * @throws SecurityException user has not granted permissions for the camera
     */
    public void openCamera(CameraManager manager, int cameraType) throws CameraAccessException, SecurityException {
        if (mCameraDevice != null) {
            stop();
        }

        try {
            //Ask for access to the camera
            mCharacterizer = new CameraCharacterizer(manager, cameraType);
            manager.openCamera(mCharacterizer.getCameraId(), mCameraListener, null);

            //Start the thread - used later by the capture session
            createCameraThread();
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the camera has been opened and is connected
     * @return true if the camera device is opened otherwise false
     */
    public boolean getCameraConnected() {
        return (mCameraDevice != null);
    }

    /**
     * Begins an active camera feed that draws frames to the specified surfaces
     * @param previewSize the Size of the preview surface - null if no preview
     * @param recordSurfaces a list of surfaces to draw the camera frames to
     * @throws IllegalStateException if the camera has not yet been opened
     * @throws CameraAccessException if there was an error when attempting to communicate with the camera device
     * @throws IllegalArgumentException if there was an error when accessing the camera device
     */
    public void startFeed(@Nullable Size previewSize, final Surface... recordSurfaces) throws IllegalStateException, IllegalArgumentException, CameraAccessException {
        if (mCameraDevice == null) throw new IllegalStateException("Camera device not opened!");

        if (recordSurfaces.length == 0) throw new IllegalArgumentException("Must have at least one surface!");

        try {
            //Create an image reader for still capturing
            Size resolution = mCharacterizer.getMaxResolution(ImageFormat.JPEG);
            mImageReader = ImageReader.newInstance(resolution.getWidth(), resolution.getHeight(), ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(mImageReaderListener, null);

            if (mCameraStatsListener != null) {
                //Alert of the best resolution fit for the TextureView
                mCameraStatsListener.onResolutionPicked(mCharacterizer.getMinFitResolution(previewSize));
            }

            //Add the image reader to the list of draw surfaces
            mRecordSurfaces = recordSurfaces;
            ArrayList<Surface> surfaceTargets = new ArrayList<Surface>(Arrays.asList(recordSurfaces));
            surfaceTargets.add(mImageReader.getSurface());

            //Start a capture session with the specified output surfaces
            mCameraDevice.createCaptureSession(surfaceTargets, mCaptureSessionListener, null);
        } catch(CameraAccessException e) {
            //Stop the camera and notify the caller
            stop();
            throw e;
        }
    }

    /**
     * Begins an  image capture from the current camera feed. The resulting image is returned by the CameraStatusListener
     * @throws IllegalStateException thrown when the camera feed has not been started. Also thrown if a CameraStatusListener has not
     * been registered for the CameraHandle
     * @throws CameraAccessException thrown if there was an error with opening the camera device
     */
    public void captureImage() throws IllegalStateException, CameraAccessException {
        if (mCaptureSession == null) throw new IllegalStateException("Camera feed has not been started!");
        if (mCameraStatsListener == null) throw new IllegalStateException("CameraStatusListener has not been registered");

        try {
            //Build a capture request
            CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            request.addTarget(mImageReader.getSurface());

            //Set auto focus mode
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            request.set(CaptureRequest.JPEG_ORIENTATION, 0);

            //Stop old preview request and do capture request
            mCaptureSession.stopRepeating();
            mCaptureSession.capture(request.build(), mCapturedListener, null);
        } catch(CameraAccessException e) {
            //Stop the camera and notify the caller
            stop();
            throw e;
        }
    }

    /**
     * Stops any current camera feeds and releases the camera device and capture session
     */
    public void stop() {

        //Cleanup session
        if (mCaptureSession != null) {
            mCaptureSession.close();
        }

        //Release device
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }

        //Tell the CameraStatusListener that the camera has been stopped
        if (mCameraStatsListener != null) {
            mCameraStatsListener.onEnded();
        }

        stopCameraThread();

        mCameraDevice        = null;
        mCaptureSession      = null;
        //mCameraStatsListener = null;
    }

    /**
     * Create a thread and a message handler to control the capture session
     */
    private void createCameraThread() {
        if (mRequestThread != null) stopCameraThread();

        //Create a thread for the capture session to be handled on
        mRequestThread = new HandlerThread("CameraRequests");
        mRequestThread.start();
        mRequestHandler = new Handler(mRequestThread.getLooper());
    }

    /**
     * End the camera thread if it is currently running
     */
    private void stopCameraThread() {
        if (mRequestThread == null) return;

        //Ask looper to stop
        mRequestThread.quitSafely();
        try {
            //Block until stopped
            mRequestThread.join();
        }catch (InterruptedException e) {
            //Interrupt triggered
        }

        mRequestThread       = null;
        mRequestHandler      = null;
    }

    /**
     * Registers a callback listner to get the status of the camera
     * @param cameraStatus a callback listner to get the status of the camera
     */
    public void setCameraStatusListener(CameraStatusCallback cameraStatus) {
        mCameraStatsListener = cameraStatus;
    }

    /**
     * Callback when the camera device's status has been changed - Opened or Closed
     */
    private CameraDevice.StateCallback mCameraListener = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //Camera device has been opened
            mCameraDevice = camera;

            if (mCameraStatsListener != null) {
                //Alert the callback listener that the camera has been connected
                mCameraStatsListener.onConnected();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    /**
     * Callback to handle the completion of the image capture request
     */
    private CameraCaptureSession.CaptureCallback mCapturedListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            //End the camera session since an image has been captured
            stop();
        }
    };

    /**
     * Callback when the ImageReader has received the captured image from the CaptureSession
     */
    private ImageReader.OnImageAvailableListener mImageReaderListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //Reader has newly captured image

            //Alert caller that their image has been captured
            if (mCameraStatsListener != null) {
                mCameraStatsListener.onImageCaptured(reader.acquireLatestImage());
            }
        }
    };

    /**
     * Callback after the CaptureSession has been setup
     */
    private CameraCaptureSession.StateCallback mCaptureSessionListener = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mCaptureSession = session;

            try {
                //Start a continuous capture request to draw frames to the surfaces
                CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                for(Surface recSurface : mRecordSurfaces) {
                    request.addTarget(recSurface);
                }
                request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                //Request continuous frames from the camera
                mCaptureSession.setRepeatingRequest(request.build(), null, mRequestHandler);
            } catch (CameraAccessException e) {
                //Stop the camera
                stop();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            //Capture session failed to open
        }
    };
}
