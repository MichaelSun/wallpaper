/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michael.qrcode.qrcode.decode;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.michael.qrcode.qrcode.scan.BarcodeScanner;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This does all the heavy lifting of decoding the images.
 */
public class Decoder {

    private static final String TAG = Decoder.class.getSimpleName();

    private static final boolean DEBUG = true & BarcodeScanner.DEBUG;

    private DecodeThread mDecodeThread;

    /**
     * @param callback            decode callback with success and fail
     * @param decodeFormats
     * @param baseHints
     * @param characterSet
     * @param resultPointCallback
     */
    public Decoder(DecodeCallback callback,
                   Collection<BarcodeFormat> decodeFormats,
                   Map<DecodeHintType, ?> baseHints,
                   String characterSet,
                   ResultPointCallback resultPointCallback) {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        if (baseHints != null) {
            hints.putAll(baseHints);
        }
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
            decodeFormats.addAll(EnumSet.of(BarcodeFormat.QR_CODE));
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        if (resultPointCallback != null) {
            hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
        }
        if (DEBUG)
            Log.i(TAG, "Hints: " + hints);

        mDecodeThread = new DecodeThread(callback, hints);
        mDecodeThread.start();
    }

    public void decode(byte[] bytes, int width, int height, Rect dstRect) {
        Handler handler = mDecodeThread.getHandler();
        Message message = Message.obtain(handler, DecodeHandler.WHAT_SCAN, width, height, dstRect);
        Bundle bundle = new Bundle();
        bundle.putByteArray(DecodeHandler.SOURCE_BYTE_ARRAY, bytes);
        message.setData(bundle);
        message.sendToTarget();
    }

    public void decode(Bitmap bitmap) {
        Handler handler = mDecodeThread.getHandler();
        Message message = Message.obtain(handler, DecodeHandler.WHAT_DECODE_BITMAP, 0, 0, bitmap);
        message.sendToTarget();
    }

    public void decode(String path) {
        Handler handler = mDecodeThread.getHandler();
        Message message = Message.obtain(handler, DecodeHandler.WHAT_DECODE_PATH, 0, 0, path);
        message.sendToTarget();
    }

    public void quit() {
        Handler handler = mDecodeThread.getHandler();
        Message message = Message.obtain(handler, DecodeHandler.WHAT_QUIT);
        message.sendToTarget();

        try {
            mDecodeThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * decode thread
     */
    static class DecodeThread extends Thread {

        private DecodeCallback mCallback;
        private Map<DecodeHintType, Object> mHints;

        private DecodeHandler mDecodeHandler;
        private CountDownLatch mHandlerInitLatch = new CountDownLatch(1);

        public DecodeThread(DecodeCallback callback, Map<DecodeHintType, Object> hints) {
            mCallback = callback;
            mHints = hints;
        }

        public DecodeHandler getHandler() {
            try {
                mHandlerInitLatch.await();
            } catch (InterruptedException ie) {
                // continue?
            }
            return mDecodeHandler;
        }

        @Override
        public void run() {
            Looper.prepare();
            mDecodeHandler = new DecodeHandler(mCallback, mHints);
            mHandlerInitLatch.countDown();
            Looper.loop();
        }
    }

}
