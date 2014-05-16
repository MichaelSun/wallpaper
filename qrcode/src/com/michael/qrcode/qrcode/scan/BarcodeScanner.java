package com.michael.qrcode.qrcode.scan;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.michael.qrcode.qrcode.camera.CameraManager;
import com.michael.qrcode.qrcode.decode.DecodeCallback;
import com.michael.qrcode.qrcode.decode.Decoder;
import com.michael.qrcode.utils.SoundHelper;

import java.io.IOException;

/**
 * Created by di.zhang on 13-12-10.
 */
public class BarcodeScanner implements SurfaceHolder.Callback {

    private static final String TAG = BarcodeScanner.class.getSimpleName();

    public static final boolean DEBUG = true;

    private static final int RESCAN_DELAY_MILLS = 0;

    private Context mContext;

    private SurfaceHolder mSurfaceHolder;

    private CameraManager mCameraManager;

    private Decoder mDecoder;
    private DecodeCallback mDecodeCallback;
    private ResultPointCallback mResultPointCallback;

    private boolean mHasSurface = false;

    private boolean mNeedOpen = false;

    private Rect mFramingRect = null;

    private boolean mTorchSetting = false;

    // 重新扫描
    private static final int WHAT_RESCAN = 1000;
    private static final int WHAT_BEEP = 2000;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_RESCAN:
                    startScanning();
                    break;
                case WHAT_BEEP:
                    SoundHelper.getInstance().playScanCompleted();
                    break;

            }
        }
    };

    private DecodeCallback mInnerDecodeCallback = new DecodeCallback() {
        @Override
        public void onDecodeSuccess(Result result) {
            mHandler.sendEmptyMessage(WHAT_BEEP);
            if (mDecodeCallback != null) {
                mDecodeCallback.onDecodeSuccess(result);
            }
        }

        @Override
        public void onDecodeFail() {
            mHandler.sendEmptyMessageDelayed(WHAT_RESCAN, RESCAN_DELAY_MILLS);
            if (mDecodeCallback != null) {
                mDecodeCallback.onDecodeFail();
            }
        }
    };

    public BarcodeScanner(Context context, SurfaceHolder holder, DecodeCallback callback, ResultPointCallback resultPointCallback) {
        mContext = context;
        mSurfaceHolder = holder;
        mDecodeCallback = callback;
        mResultPointCallback = resultPointCallback;

        mSurfaceHolder.addCallback(this);
    }

    /**
     * 横屏时扫码框相对于屏幕的矩形区域
     *
     * @param rect
     */
    public void setFramingRect(Rect rect) {
        mFramingRect = rect;
    }

    /**
     * 在activity的onResume中调用
     */
    public void onResume() {
        openScanner();
    }

    /**
     * 在activity的onPause中调用
     */
    public void onPause() {
        closeScanner();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void startScanning() {
        if (mCameraManager != null) {
            mCameraManager.requestPreviewFrame();
        }
    }

    /**
     * 闪光灯
     *
     * @param newSetting
     */
    public void setTorch(boolean newSetting) {
        mTorchSetting = newSetting;
        if (mCameraManager != null) {
            mCameraManager.setTorch(newSetting);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHasSurface = true;
        if (mNeedOpen) {
            mNeedOpen = false;
            initCamera(mSurfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    private void openScanner() {
        if (mDecoder == null) {
            mDecoder = new Decoder(mInnerDecodeCallback, null, null, null, mResultPointCallback);
        }
        mCameraManager = new CameraManager(mContext, mDecoder);

        if (mHasSurface) {
            initCamera(mSurfaceHolder);
        } else {
            mNeedOpen = true;
        }

        if (mFramingRect != null) {
            mCameraManager.setManualFramingRect(mFramingRect);
        }
    }

    private void closeScanner() {
        if (mCameraManager != null) {
            mCameraManager.stopPreview();
            mCameraManager.closeDriver();
            mCameraManager = null;
        }
        if (mDecoder != null) {
            mDecoder.quit();
            mDecoder = null;
        }
        mNeedOpen = false;
    }

    private void initCamera(SurfaceHolder holder) {
        if (holder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        try {
            if (mCameraManager != null) {
                mCameraManager.openDriver(holder);
                mCameraManager.setTorch(mTorchSetting);
                mCameraManager.startPreview();
                mCameraManager.requestPreviewFrame();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
