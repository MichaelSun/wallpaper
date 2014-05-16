package com.michael.qrcode.qrcode.camera;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import com.michael.qrcode.qrcode.decode.Decoder;
import com.michael.qrcode.qrcode.scan.BarcodeScanner;

/**
 * Created with IntelliJ IDEA.
 * User: di.zhang
 * Date: 13-12-9
 * Time: 下午2:38
 * To change this template use File | Settings | File Templates.
 */
final class PreviewCallback implements Camera.PreviewCallback {

    private static final String TAG = PreviewCallback.class.getSimpleName();

    private static final boolean DEBUG = true & BarcodeScanner.DEBUG;

    private CameraManager mCameraManager;

    private CameraConfiguration mConfigManager;

    private Decoder mDecoder;

    PreviewCallback(CameraManager cameraManager, CameraConfiguration configManager, Decoder decodeThread) {
        mCameraManager = cameraManager;
        mConfigManager = configManager;
        mDecoder = decodeThread;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Point cameraResolution = mConfigManager.getCameraResolution();
        Rect rectInPreview = mCameraManager.getFramingRectInPreview();
        if (cameraResolution != null && mDecoder != null && rectInPreview != null) {
            mDecoder.decode(bytes, cameraResolution.x, cameraResolution.y, rectInPreview);
        } else {
            if (DEBUG)
                Log.d(TAG, "Got preview callback, but no handler or resolution available");
        }
    }
}
