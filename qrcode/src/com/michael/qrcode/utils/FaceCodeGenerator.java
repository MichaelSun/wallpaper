package com.michael.qrcode.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.michael.qrcode.qrcode.r.sdk.QRCodeFaceOptions;
import com.michael.qrcode.qrcode.r.sdk.QRCodeGenerator;
import com.michael.qrcode.qrcode.r.sdk.QRCodeGradientOptions;
import com.plugin.common.utils.image.ImageUtils;

import java.io.IOException;

/**
 * Created by zhangdi on 13-12-20.
 */
public class FaceCodeGenerator {

    public static final int DEFAULT_QR_SIZE = 960;

    public static final ErrorCorrectionLevel ERROR_LEVER = ErrorCorrectionLevel.H;

    public static Bitmap generate(Context context, String avatar, String qrContent, int id) {
        if (TextUtils.isEmpty(avatar) || TextUtils.isEmpty(qrContent)) {
            return null;
        }
        Bitmap face = ImageUtils.loadBitmapWithSizeOrientation(avatar);
        if (face == null) {
            return null;
        }
        final Bitmap outBitmap;

        switch (id) {
            case 1: {
                QRCodeGradientOptions opt = new QRCodeGradientOptions();
                opt.qrContent = qrContent;
                opt.defaultQRSize = DEFAULT_QR_SIZE;
                opt.startColor = Color.rgb(247, 4, 85);
                opt.endColor = Color.rgb(249, 85, 35);
                try {
                    opt.maskBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_mask1.png"));
                    opt.borderBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_border1.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                opt.frontBitmap = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            case 2: {
                QRCodeGradientOptions opt = new QRCodeGradientOptions();
                opt.qrContent = qrContent;
                opt.defaultQRSize = DEFAULT_QR_SIZE;
                opt.startColor = Color.rgb(39, 181, 42);
                opt.endColor = Color.rgb(28, 160, 138);
                try {
                    opt.maskBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_mask2.png"));
                    opt.borderBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_border2.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                opt.frontBitmap = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            case 3: {
                QRCodeGradientOptions opt = new QRCodeGradientOptions();
                opt.qrContent = qrContent;
                opt.defaultQRSize = DEFAULT_QR_SIZE;
                opt.startColor = Color.rgb(44, 189, 249);
                opt.endColor = Color.rgb(28, 81, 232);
                try {
                    opt.maskBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_mask3.png"));
                    opt.borderBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_border3.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                opt.frontBitmap = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            case 4: {
                QRCodeGradientOptions opt = new QRCodeGradientOptions();
                opt.qrContent = qrContent;
                opt.defaultQRSize = DEFAULT_QR_SIZE;
                opt.startColor = Color.rgb(241, 131, 7);
                opt.endColor = Color.rgb(251, 72, 19);
                try {
                    opt.maskBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_mask4.png"));
                    opt.borderBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_border4.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                opt.frontBitmap = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            case 5: {
                QRCodeFaceOptions opt = new QRCodeFaceOptions();
                opt.mQrContent = qrContent;
                opt.mSize = DEFAULT_QR_SIZE;
                opt.mColor = Color.rgb(214, 1, 143);
                opt.mFaceBmp = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            case 6: {
                QRCodeFaceOptions opt = new QRCodeFaceOptions();
                opt.mQrContent = qrContent;
                opt.mSize = DEFAULT_QR_SIZE;
                opt.mColor = Color.rgb(115, 115, 115);
                opt.mFaceBmp = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            case 7: {
                QRCodeFaceOptions opt = new QRCodeFaceOptions();
                opt.mQrContent = qrContent;
                opt.mSize = DEFAULT_QR_SIZE;
                opt.mColor = Color.rgb(28, 99, 209);
                opt.mFaceBmp = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            case 8: {
                QRCodeFaceOptions opt = new QRCodeFaceOptions();
                opt.mQrContent = qrContent;
                opt.mSize = DEFAULT_QR_SIZE;
                opt.mColor = Color.rgb(16, 125, 40);
                opt.mFaceBmp = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
            default: {
                // default is 2
                QRCodeGradientOptions opt = new QRCodeGradientOptions();
                opt.qrContent = qrContent;
                opt.defaultQRSize = DEFAULT_QR_SIZE;
                opt.startColor = Color.rgb(241, 131, 7);
                opt.endColor = Color.rgb(251, 72, 19);
                try {
                    opt.maskBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_mask2.png"));
                    opt.borderBitmap = BitmapFactory.decodeStream(context.getAssets().open("image/qrcode/gradient_border2.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                opt.frontBitmap = face;
                opt.errorLevel = ERROR_LEVER;
                outBitmap = QRCodeGenerator.createQRCode(opt);
                break;
            }
        }
        return outBitmap;
    }
}
