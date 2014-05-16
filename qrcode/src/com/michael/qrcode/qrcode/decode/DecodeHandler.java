/*
 * Copyright (C) 2010 ZXing authors
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
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.michael.qrcode.qrcode.scan.BarcodeScanner;

import java.util.Map;

class DecodeHandler extends Handler {

    private static final boolean DEBUG = true & BarcodeScanner.DEBUG;

    private static final String TAG = DecodeHandler.class.getSimpleName();

    static final String SOURCE_BYTE_ARRAY = "source_byte_array";

    static final int WHAT_DECODE_BITMAP = 1;
    static final int WHAT_DECODE_PATH = 2;
    static final int WHAT_SCAN = 3;
    static final int WHAT_QUIT = 4;

    static final int MAX_BITMAP_SIZE = 200 * 200 * 4;

    private DecodeCallback mCallback;
    private final MultiFormatReader mMultiFormatReader;
    private boolean mRunning = true;

    DecodeHandler(DecodeCallback callback, Map<DecodeHintType, Object> hints) {
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);
        this.mCallback = callback;
    }

    @Override
    public void handleMessage(Message message) {
        if (!mRunning) {
            return;
        }
        switch (message.what) {
            case WHAT_SCAN:
                Bundle bundle = message.getData();
                if (bundle != null) {
                    byte[] bytes = bundle.getByteArray(SOURCE_BYTE_ARRAY);
                    decode(bytes, message.arg1, message.arg2, (Rect) message.obj);
                }
                break;
            case WHAT_DECODE_BITMAP:
                decode((Bitmap) message.obj);
                break;
            case WHAT_DECODE_PATH:
                decode((String) message.obj);
                break;
            case WHAT_QUIT:
                mRunning = false;
                mCallback = null;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height, Rect rect) {
        if (data == null || rect == null) {
            if (DEBUG)
                Log.d(TAG, "Cannot find barcode : data or rect is null");
            if (mCallback != null) {
                mCallback.onDecodeFail();
            }
        }
        long start = System.currentTimeMillis();
        Result rawResult = null;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                mMultiFormatReader.reset();
            }
        }

        if (rawResult != null) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            if (DEBUG) {
                Log.d(TAG, "Found barcode in " + (end - start) + " ms");
                Log.d(TAG, "Found barcode: " + rawResult.toString());
            }

            if (mCallback != null) {
                mCallback.onDecodeSuccess(rawResult);
            }
        } else {
            if (mCallback != null) {
                mCallback.onDecodeFail();
            }
        }
    }


    /**
     * Decode the bitmap, and time how long it took.
     *
     * @param bitmap The bitmap.
     */
    private void decode(Bitmap bitmap) {
        if (bitmap == null) {
            if (DEBUG)
                Log.d(TAG, "cannot find barcode, because the bitmap is null");
            return;
        }
        long start = System.currentTimeMillis();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        Result rawResult = null;
        if (pixels != null && width > 0 && height > 0) {
//            convertRGB2YUV(pixels, width, height);
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            if (source != null) {
                BinaryBitmap bbm = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    rawResult = mMultiFormatReader.decode(bbm);
                } catch (ReaderException re) {
                    // continue
                } finally {
                    mMultiFormatReader.reset();
                }
            }
        }

        if (rawResult != null) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            if (DEBUG)
                Log.d(TAG, "Found barcode in " + (end - start) + " ms");

            if (mCallback != null) {
                mCallback.onDecodeSuccess(rawResult);
            }
        } else {
            if (mCallback != null) {
                mCallback.onDecodeFail();
            }
        }
    }

    /**
     * Decode with the local path, and time how long it took.
     *
     * @param path The path.
     */
    private void decode(String path) {
        Bitmap bitmap = loadBitmap(path);
        decode(bitmap);
    }

    private Bitmap loadBitmap(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPurgeable = true;
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);
        int width = opt.outWidth;
        int height = opt.outHeight;

        BitmapFactory.Options newOpt = new BitmapFactory.Options();
        long fileSize = width * height * 4;
        if (fileSize <= MAX_BITMAP_SIZE) {
            newOpt.inSampleSize = 1;
        } else if (fileSize <= MAX_BITMAP_SIZE * 4) {
            newOpt.inSampleSize = 2;
        } else {
            long times = fileSize / MAX_BITMAP_SIZE;
            newOpt.inSampleSize = (int) (Math.log(times) / Math.log(2.0)) + 1;
        }
        newOpt.inPurgeable = true;
        newOpt.inInputShareable = true;
        newOpt.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, newOpt);
    }

    private void convertRGB2YUV(int[] pixels, int width, int height) {
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
    }

}
