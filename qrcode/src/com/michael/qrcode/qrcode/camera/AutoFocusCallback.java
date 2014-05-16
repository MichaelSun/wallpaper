package com.michael.qrcode.qrcode.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import com.michael.qrcode.qrcode.scan.BarcodeScanner;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: di.zhang
 * Date: 13-12-9
 * Time: 下午4:21
 * To change this template use File | Settings | File Templates.
 */
final class AutoFocusCallback implements Camera.AutoFocusCallback {

    private static final String TAG = AutoFocusCallback.class.getSimpleName();

    private static final boolean DEBUG = true & BarcodeScanner.DEBUG;

    private static final long AUTO_FOCUS_INTERVAL_MS = 1000L;
    private Handler mAutoFocusHandler = new Handler();

    private static final Collection<String> FOCUS_MODES_CALLING_AF;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private boolean mActive = false;
    private boolean mUseAutoFocus = false;
    private Camera mCamera = null;

    AutoFocusCallback(Camera camera) {
        this.mCamera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        mUseAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        if (DEBUG)
            Log.i(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + mUseAutoFocus);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera camera) {
        if (mActive) {
            mAutoFocusHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            }, AUTO_FOCUS_INTERVAL_MS);
        }
    }

    synchronized void start() {
        if (mUseAutoFocus) {
            mActive = true;
            try {
                mCamera.autoFocus(this);
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                if (DEBUG)
                    Log.w(TAG, "Unexpected exception while focusing", re);
            }
        }
    }

    synchronized void stop() {
        if (mUseAutoFocus) {
            try {
                mCamera.cancelAutoFocus();
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                if (DEBUG)
                    Log.w(TAG, "Unexpected exception while cancelling focusing", re);
            }
        }
        mActive = false;
        mAutoFocusHandler.removeCallbacksAndMessages(null);
    }

}
