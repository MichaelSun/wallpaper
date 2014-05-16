package com.michael.qrcode.qrcode.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import com.michael.qrcode.qrcode.decode.Decoder;
import com.michael.qrcode.qrcode.scan.BarcodeScanner;

import java.io.IOException;

public class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    private static final boolean DEBUG = true & BarcodeScanner.DEBUG;

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 960; // = 1920/2
    private static final int MAX_FRAME_HEIGHT = 540; // = 1080/2

    private CameraConfiguration mConfigManager;
    private Decoder mDecoder;
    private Camera mCamera;
    private AutoFocusCallback mAutoFocusCallback;
    private Rect mFramingRect;
    private Rect mFramingRectInPreview;
    private boolean mPreviewing;

    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private PreviewCallback mPreviewCallback;

    public CameraManager(Context context, Decoder decoder) {
        this.mConfigManager = new CameraConfiguration(context);
        mDecoder = decoder;
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws java.io.IOException Indicates the camera driver failed to open.
     */
    public synchronized void openDriver(SurfaceHolder holder) throws IOException {
        try {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }

            int numCameras = Camera.getNumberOfCameras();
            if (numCameras == 0) {
                if (DEBUG)
                    Log.w(TAG, "No cameras!");
                throw new IOException();
            }

            int index = 0;
            while (index < numCameras) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(index, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    break;
                }
                index++;
            }
            if (index < numCameras) {
                if (DEBUG)
                    Log.i(TAG, "Opening camera #" + index);
                mCamera = Camera.open(index);
            } else {
                if (DEBUG)
                    Log.i(TAG, "No camera facing back; returning camera #0");
                mCamera = Camera.open(0);
            }

            if (mCamera == null) {
                throw new IOException();
            }

            mCamera.setPreviewDisplay(holder);
            setCameraParameters();
        } catch (Exception e) {
            e.printStackTrace();
            closeDriver();
        }
    }


    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            mFramingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        if (mCamera != null && !mPreviewing) {
            mCamera.startPreview();
            mPreviewing = true;
            mPreviewCallback = new PreviewCallback(this, mConfigManager, mDecoder);
            mAutoFocusCallback = new AutoFocusCallback(mCamera);
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        if (mAutoFocusCallback != null) {
            mAutoFocusCallback.stop();
            mAutoFocusCallback = null;
        }
        if (mCamera != null && mPreviewing) {
            mCamera.stopPreview();
            mPreviewing = false;
            mPreviewCallback = null;
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     */
    public synchronized void requestPreviewFrame() {
        if (mCamera != null && mPreviewing && mPreviewCallback != null) {
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
        }
    }

    public synchronized void setCameraParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
        try {
            mConfigManager.setCameraParameters(mCamera, false);
        } catch (RuntimeException re) {
            // Driver failed
            if (DEBUG) {
                Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
                Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
            }
            // Reset:
            if (parametersFlattened != null) {
                parameters = mCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    mCamera.setParameters(parameters);
                    mConfigManager.setCameraParameters(mCamera, true);
                } catch (RuntimeException re2) {
                    // Well, darn. Give up
                    if (DEBUG)
                        Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }
    }

    public synchronized void setTorch(boolean newSetting) {
        if (newSetting != mConfigManager.getTorchState(mCamera)) {
            if (mCamera != null) {
                if (mAutoFocusCallback != null) {
                    mAutoFocusCallback.stop();
                }
                mConfigManager.setTorch(mCamera, newSetting);
                if (mAutoFocusCallback != null) {
                    mAutoFocusCallback.start();
                }
            }
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public synchronized Rect getFramingRect() {
        if (mFramingRect == null) {
            if (mCamera == null) {
                return null;
            }
            Point screenResolution = mConfigManager.getScreenResolution();
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            if (DEBUG)
                Log.d(TAG, "Calculated framing rect: " + mFramingRect);
        }
        return mFramingRect;
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = resolution / 2; // Target 50% of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    public synchronized Rect getFramingRectInPreview() {
        if (mFramingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point cameraResolution = mConfigManager.getCameraResolution();
            Point screenResolution = mConfigManager.getScreenResolution();
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            mFramingRectInPreview = rect;
        }
        if (DEBUG)
            Log.d(TAG, "FramingRectInPreview:" + mFramingRectInPreview);
        return mFramingRectInPreview;
    }

    public synchronized void setManualFramingRect(Rect rect) {
        mFramingRect = new Rect(rect);
        mFramingRectInPreview = null;
    }

}