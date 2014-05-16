package com.michael.qrcode.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.michael.qrcode.R;
import com.michael.qrcode.utils.CropUtils;
import com.michael.qrcode.view.CropImageView;
import com.michael.qrcode.view.ToastHelper;
import com.plugin.common.utils.CustomThreadPool;
import com.plugin.common.utils.image.ExifHelper;
import com.plugin.common.utils.image.ImageUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by di.zhang 13-12-13 上午10:23.
 */
public class ImageCropActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 图片来源: From.CAMERA.ordinal(), From.GALLERY.ordinal()
     */
    public static final String EXTRA_PHOTO_FROM_INT = "extra_photo_from_int";

    /**
     * 裁剪图片大小，宽高相等，默认960
     */
    public static final String EXTRA_CROP_SIZE = "extra_crop_size";

    /**
     * 裁剪后的图片路径, 裁剪成功后，setResult中返回
     */
    public static final String EXTRA_CROP_PATH = "cropPath";

    public static enum From {
        CAMERA, GALLERY
    }

    private static final int REQUEST_GALLERY = 10000;
    private static final int REQUEST_CAMERA = 20000;

    private String mTempCameraPath;

    private int mCropSize = 960;

    private CropImageView mPhotoView;

    private String mSourcePhotoPath;
    private String mCropPath;
    private Bitmap mSourceBitmap;

    private Rect mCropRect;

    private CropHandler mCropHandler;
    private static final int MESSAGE_LOAD_PHOTO = 20001;
    private static final int MESSAGE_CROP_PHOTO = 20002;
    private static final int LOAD_IMAGE_SUCCESS = 0;
    private static final int LOAD_IMAGE_NOT_SUPPORT = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        getActionBar().hide();

        if (getIntent() == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        mPhotoView = (CropImageView) findViewById(R.id.image_view);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.use_btn).setOnClickListener(this);

        mCropHandler = new CropHandler(this);

        mCropSize = getIntent().getIntExtra(EXTRA_CROP_SIZE, 960);
        From from = From.values()[getIntent().getIntExtra(EXTRA_PHOTO_FROM_INT, 0)];
        if (from == From.CAMERA) {
            selectFromCamera();
        } else {
            selectFromGallery();
        }
    }

    private void selectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void selectFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File tempFile = CropUtils.getTempCameraFile(this);
        if (tempFile != null) {
            mTempCameraPath = tempFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                mSourcePhotoPath = mTempCameraPath;
                if (!TextUtils.isEmpty(mSourcePhotoPath)) {
                    CustomThreadPool.asyncWork(new Runnable() {
                        @Override
                        public void run() {
                            mSourceBitmap = ImageUtils.loadBitmapWithSizeOrientation(mSourcePhotoPath);
                            mCropHandler.obtainMessage(MESSAGE_LOAD_PHOTO, LOAD_IMAGE_SUCCESS, 0, null).sendToTarget();
                        }
                    });
                }
            } else {
                finish();
            }
        } else if (requestCode == REQUEST_GALLERY) {
            if (resultCode == RESULT_OK && data != null) {
                final Uri photoUri = data.getData();
                if (photoUri == null) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }
                CustomThreadPool.asyncWork(new Runnable() {
                    @Override
                    public void run() {
                        mSourcePhotoPath = CropUtils.getImagePathFromUri(ImageCropActivity.this, photoUri);
                        if (!TextUtils.isEmpty(mSourcePhotoPath) && (mSourcePhotoPath.endsWith(".png") || mSourcePhotoPath.endsWith(".jpg") || mSourcePhotoPath.endsWith(".jpeg")
                                                                         || mSourcePhotoPath.endsWith(".PNG") || mSourcePhotoPath.endsWith(".JPG") || mSourcePhotoPath.endsWith(".JPEG"))) {
                            mSourceBitmap = ImageUtils.loadBitmapWithSizeOrientation(mSourcePhotoPath);
                            mCropHandler.obtainMessage(MESSAGE_LOAD_PHOTO, LOAD_IMAGE_SUCCESS, 0, null).sendToTarget();
                        } else {
                            mCropHandler.obtainMessage(MESSAGE_LOAD_PHOTO, LOAD_IMAGE_NOT_SUPPORT, 0, null).sendToTarget();
                        }
                    }
                });
            } else {
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.use_btn:
                if (!TextUtils.isEmpty(mSourcePhotoPath)) {
                    mCropRect = mPhotoView.getCropArea();
                    CustomThreadPool.asyncWork(mCropRunnable);
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            case R.id.cancel_btn:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    private static class CropHandler extends Handler {
        private final WeakReference<ImageCropActivity> mActivity;

        public CropHandler(ImageCropActivity activity) {
            mActivity = new WeakReference<ImageCropActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ImageCropActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }

            switch (msg.what) {
                case MESSAGE_LOAD_PHOTO:
                    if (msg.arg1 == LOAD_IMAGE_NOT_SUPPORT) {
                        ToastHelper.showMessage(activity, R.string.image_not_support);
                        activity.setResult(RESULT_CANCELED);
                        activity.finish();
                    } else {
                        activity.mPhotoView.setImageBitmap(activity.mSourceBitmap);
                    }
                    break;
                case MESSAGE_CROP_PHOTO:
                    if (TextUtils.isEmpty(activity.mCropPath)) {
                        activity.setResult(RESULT_CANCELED);
                        activity.finish();
                    } else {
                        Intent data = new Intent();
                        data.putExtra(EXTRA_CROP_PATH, activity.mCropPath);
                        activity.setResult(RESULT_OK, data);
                        activity.finish();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Runnable mCropRunnable = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(mSourcePhotoPath)) {
                int rotation = ExifHelper.getRotationFromExif(mSourcePhotoPath);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mSourcePhotoPath, options);
                int width = options.outWidth;
                int height = options.outHeight;

                Rect regionRect = new Rect();
                int realWidth = mPhotoView.getRealWidth();
                int realHeight = mPhotoView.getRealHeight();

                if (rotation == 0) {
                    regionRect.set(mCropRect.left * width / realWidth, mCropRect.top * height / realHeight, mCropRect.right * width / realWidth, mCropRect.bottom * height / realHeight);
                } else if (rotation == 90) {
                    regionRect.left = mCropRect.top * width / realHeight;
                    regionRect.right = mCropRect.bottom * width / realHeight;
                    regionRect.top = (realWidth - mCropRect.right) * height / realWidth;
                    regionRect.bottom = (realWidth - mCropRect.left) * height / realWidth;
                } else if (rotation == 180) {
                    regionRect.left = (realWidth - mCropRect.right) * width / realWidth;
                    regionRect.right = (realWidth - mCropRect.left) * width / realWidth;
                    regionRect.top = (realHeight - mCropRect.bottom) * height / realHeight;
                    regionRect.bottom = (realHeight - mCropRect.top) * height / realHeight;
                } else if (rotation == 270) {
                    regionRect.left = (realHeight - mCropRect.bottom) * width / realHeight;
                    regionRect.right = (realHeight - mCropRect.top) * width / realHeight;
                    regionRect.top = mCropRect.left * height / realWidth;
                    regionRect.bottom = mCropRect.right * height / realWidth;
                }
                mCropPath = CropUtils.cropPhoto(ImageCropActivity.this, mSourcePhotoPath, regionRect, mCropSize * mCropSize * 4);
            }
            mCropHandler.sendEmptyMessage(MESSAGE_CROP_PHOTO);
        }
    };

    private void LOGD(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Log.d(ImageCropActivity.class.getSimpleName(), msg);
        }
    }
}