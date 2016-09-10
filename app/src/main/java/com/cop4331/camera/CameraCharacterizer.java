package com.cop4331.camera;

import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

/**
 * Created by Sean on 9/10/2016.
 */
public class CameraCharacterizer {
    public static final int FRONT_CAMERA = 1;
    public static final int  BACK_CAMERA = 2;

    private CameraManager mCameraManager = null;
    private int           mCurCamera     = 0;
    private String        mCurCameraId   = null;

    /**
     * Constructs a new CameraCharacterizer object that helps with getting the characteristics
     * for the camera
     * @param cameraManager the system's CameraManager
     * @param cameraType the desired type of camera - front facing camera or back camera
     * @throws IllegalArgumentException thrown if the provided cameraType is an invalid type
     */
    public CameraCharacterizer(CameraManager cameraManager, int cameraType) throws IllegalArgumentException {
        mCameraManager = cameraManager;
        setCurrentCamera(cameraType);
    }

    /**
     * Finds a cameraID with the specified camera characteristic
     * @param camCharacteristicsKey the type of characteristic to search for
     * @param camCharacteristics the desired setting for the camCharacteristicKey
     * @return a string containing the Camera ID that has the desired characteristic. Null if none was found
     */
    private String getCameraId(CameraCharacteristics.Key<Integer> camCharacteristicsKey, int camCharacteristics) {
        try {
            //Loop through camera devices
            for (String cameraId : mCameraManager.getCameraIdList()) {
                //Get characteristics of device
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                int checkChar = characteristics.get(camCharacteristicsKey);

                //See Characteristic id matches the desired Characteristic
                if (camCharacteristics == checkChar)
                    return cameraId;
            }
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Finds the Camera ID for the front facing camera
     * @return a string containing the Camera ID of the front facing camera. Null if none was found
     */
    private String getFrontFacingCameraId() {
        //Get the front facing camera ID
        return getCameraId(CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_FACING_FRONT);
    }

    /**
     * Finds the Camera ID for the back facing camera
     * @return a string containing the Camera ID of the back facing camera. Null if none was found
     */
    private String getBackFacingCameraId() {
        //Get the back facing camera ID
        return getCameraId(CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_FACING_BACK);
    }

    /**
     * Sets the current camera type that's being used by the CameraCharacterizer
     * @param cameraType the desired type of camera - front facing camera or back camera
     * @throws IllegalArgumentException thrown if the provided cameraType is an invalid type
     */
    private void setCurrentCamera(int cameraType) throws IllegalArgumentException {
        String id = null;
        switch (cameraType) {
            case FRONT_CAMERA:
                id = getFrontFacingCameraId();
                break;
            case BACK_CAMERA:
                id = getBackFacingCameraId();
                break;
            default:
                throw new IllegalArgumentException("Argument provided is not a type of camera!");
        }

        mCurCameraId = id;
        mCurCamera   = cameraType;
    }

    /**
     * Gets the maximum resolution for the specified image format
     * @param imageFormat the ImageFormat constant that the resolution will be used for
     * @return a Size containing the width and height of the maximum resolution supported by the camera
     * for the specified ImageFormat
     */
    public Size getMaxResolution(int imageFormat) {
        try {
            //if (mCurCameraId == null) throw new IllegalStateException("Current camera has not been set!");

            //Get the characteristic for resolution size
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCurCameraId);
            StreamConfigurationMap streamMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //Look at output sizes for specified image format
            Size[] sizes      = streamMap.getOutputSizes(imageFormat);
            Size   largestRes = null;
            if (sizes.length > 0) {
                //Find the largest resolution size
                largestRes = sizes[0];
                for (Size size : sizes) {
                    //Compare by total area
                    if (size.getHeight() * size.getWidth() > largestRes.getHeight() * largestRes.getWidth()) {
                        //This one is larger, so set it
                        largestRes = size;
                    }
                }
            }

            return largestRes;
        } catch (CameraAccessException e) {

        }

        return null;
    }

    /**
     * Gets the Id of the camera for the camera device that the CameraCharacterizer uses
     * @return a String containing the camera Id for the camera device type specified in the
     * CameraCharacterizer constructor
     */
    public String getCameraId() {
        return mCurCameraId;
    }
}
