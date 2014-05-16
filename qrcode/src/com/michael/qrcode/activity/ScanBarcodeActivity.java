package com.michael.qrcode.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.Result;
import com.michael.qrcode.R;
import com.michael.qrcode.dao.model.ScanHistory;
import com.michael.qrcode.model.ScanHistoryHelper;
import com.michael.qrcode.qrcode.decode.DecodeCallback;
import com.michael.qrcode.qrcode.decode.Decoder;
import com.michael.qrcode.qrcode.scan.BarcodeScanner;
import com.michael.qrcode.utils.CropUtils;
import com.michael.qrcode.utils.TipsDialog;
import com.michael.qrcode.view.MMAlert;
import com.michael.qrcode.view.ScanBorderView;

public class ScanBarcodeActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 模式: ScanMode.NORMAL.ordinal(), ScanMode.TRY.ordinal()
     */
    public static final String EXTRA_SCAN_MODEL_INT = "extra_scan_mode_int";

    public static enum ScanMode {
        NORMAL, TRY
    }

    private ScanMode mScanMode;

    private static final int REQUEST_GALLERY_DECODE = 10000;
    private static final int REQUEST_PHOTO = 20000;

    private ScanBorderView mScanBorderView;
    private BarcodeScanner mScanner;

    private TextView mTorchBtn;
    private View mGalleryBtn;
    private View mHistoryBtn;
//    private View mGenerateBtn;

    private View mBlackMaskView;
    private View mCameraSwitchTop;
    private View mCameraSwitchBottom;
    private Animation mSwitchTopOutAnim;
    private Animation mSwitchBottomOutAnim;
    private Animation mSwitchTopInAnim;
    private Animation mSwitchBottomInAnim;

    private boolean mTorchOn = false;
    private boolean mNotLogin = false;

    private ScanHistoryHelper mScanHistoryHelper;

    private AlertDialog mAlertDialog;

    private static final int WHAT_PARSE_VCARD_PROFILE_SUCCESS = 100;
    private static final int WHAT_PARSE_VCARD_SEED_SUCCESS = 200;
    private static final int WHAT_PARSE_VCARD_FAIL = 300;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_PARSE_VCARD_PROFILE_SUCCESS: {
                    TipsDialog.getInstance().dismiss();

//                    UserProfile.UserBase userbase = (UserProfile.UserBase) msg.obj;
//                    final int uid = msg.arg1;
//                    final String name = userbase == null ? null : userbase.name;
//                    postAfterCameraOff(new Runnable() {
//                        @Override
//                        public void run() {
//                            OthersVcardInfoActivity.start(ScanBarcodeActivity.this, uid, name, true);
//                        }
//                    });
                    break;
                }
                case WHAT_PARSE_VCARD_SEED_SUCCESS: {
                    TipsDialog.getInstance().dismiss();

//                    final int uid = msg.arg1;
//                    postAfterCameraOff(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent intent = new Intent(ScanBarcodeActivity.this, SeededUserActivity.class);
//                            intent.putExtra(SeededUserActivity.EXTRA_USER_ID, uid);
//                            startActivity(intent);
//                        }
//                    });
                    break;
                }
                case WHAT_PARSE_VCARD_FAIL: {
//                    TipsDialog.getInstance().dismiss();
//                    ToastHelper.showMessage(ScanBarcodeActivity.this, R.string.scan_parse_shorturl_fail);
                    mScanner.startScanning();
                    break;
                }
            }
        }
    };

    private DecodeCallback mScanDecodeCallback = new DecodeCallback() {
        @Override
        public void onDecodeSuccess(final Result result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    decodeSuccess(result);
                }
            });
        }

        @Override
        public void onDecodeFail() {

        }
    };

    private DecodeCallback mFileDecodeCallback = new DecodeCallback() {
        @Override
        public void onDecodeSuccess(final Result result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (result != null) {
                        decodeSuccess(result);
                    }
                }
            });
        }

        @Override
        public void onDecodeFail() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ScanBarcodeActivity.this, R.string.scan_barcode_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Decoder mFileDecoder = null;

    public void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        initBannerAd();
        initSplashAd();

        if (getIntent() != null) {
            mScanMode = ScanMode.values()[getIntent().getIntExtra(EXTRA_SCAN_MODEL_INT, 0)];
        }

        // init views
        mGalleryBtn = findViewById(R.id.scan_gallery);
//        mGenerateBtn = findViewById(R.id.generate_face_code);
        mHistoryBtn = findViewById(R.id.scan_history);
        mTorchBtn = (TextView) findViewById(R.id.scan_torch);
        mGalleryBtn.setOnClickListener(this);
//        mGenerateBtn.setOnClickListener(this);
        mHistoryBtn.setOnClickListener(this);
        mTorchBtn.setOnClickListener(this);
        findViewById(R.id.back_layout).setOnClickListener(this);

        if (ScanMode.TRY == mScanMode) {
            mHistoryBtn.setVisibility(View.GONE);
        } else {
            mHistoryBtn.setVisibility(View.VISIBLE);
        }

        mScanBorderView = (ScanBorderView) findViewById(R.id.scan_border);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder holder = surfaceView.getHolder();
        mScanner = new BarcodeScanner(getApplicationContext(), holder, mScanDecodeCallback, null);
        mScanner.setTorch(mTorchOn);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int borderWidth = getResources().getDimensionPixelOffset(R.dimen.scan_border_width);
        int borderHeight = getResources().getDimensionPixelOffset(R.dimen.scan_border_height);
        int topOffset = (screenHeight - borderHeight) / 2;
        int leftOffset = (screenWidth - borderWidth) / 2;
        Rect framingRect = new Rect(topOffset, leftOffset, topOffset + borderHeight, leftOffset + borderWidth);
        mScanner.setFramingRect(framingRect);

        mBlackMaskView = findViewById(R.id.mask);
        mCameraSwitchTop = findViewById(R.id.camera_switch_top);
        mCameraSwitchBottom = findViewById(R.id.camera_switch_bottom);
        ViewGroup.LayoutParams topLp = mCameraSwitchTop.getLayoutParams();
        topLp.height = (int) (screenHeight / 2 + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34, getResources().getDisplayMetrics()));
        ViewGroup.LayoutParams bottomLp = mCameraSwitchBottom.getLayoutParams();
        bottomLp.height = screenHeight / 2;

        mScanHistoryHelper = new ScanHistoryHelper(getApplicationContext());
    }

    @Override
    public void onResume() {
        mBlackMaskView.setVisibility(View.VISIBLE);
        mCameraSwitchTop.setVisibility(View.VISIBLE);
        mCameraSwitchBottom.setVisibility(View.VISIBLE);
        mSwitchTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.anim_camera_switch_top_out);
        mSwitchBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.anim_camera_switch_bottom_out);
        mSwitchTopOutAnim.setAnimationListener(mCameraSwitchOnAnimListener);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBlackMaskView.setVisibility(View.GONE);
                mCameraSwitchTop.startAnimation(mSwitchTopOutAnim);
                mCameraSwitchBottom.startAnimation(mSwitchBottomOutAnim);
                mScanner.onResume();
                mScanBorderView.startAnimator();
            }
        }, 220);

        super.onResume();

        if (interstitial.isReady()) {
            interstitial.show();
        }
    }

    /**
     * 相机开启动画listener
     */
    private Animation.AnimationListener mCameraSwitchOnAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mCameraSwitchTop.setVisibility(View.GONE);
            mCameraSwitchBottom.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    @Override
    public void onPause() {
        dismissDialog();
        mHandler.removeCallbacksAndMessages(null);

        mScanner.onPause();
        mScanBorderView.stopAnimator();

        if (mFileDecoder != null) {
            mFileDecoder.quit();
            mFileDecoder = null;
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_gallery:
                postAfterCameraOff(new Runnable() {
                    @Override
                    public void run() {
                        selectFaceBarcodeFromGallery();
                    }
                });
                break;
//            case R.id.generate_face_code:
//                if (mScanMode == ScanMode.TRY) {
//                    showMenu();
//                } else {
//                    if (TextUtils.isEmpty(SettingManager.getInstance().getAvatar())) {
//                        showMenu();
//                    } else {
//                        postAfterCameraOff(new Runnable() {
//                            @Override
//                            public void run() {
//                                startActivity(new Intent(ScanBarcodeActivity.this, FaceCodeActivity.class));
//                            }
//                        });
//                    }
//                }
//                break;
            case R.id.scan_history:
                postAfterCameraOff(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScanBarcodeActivity.this, ScanHistoryActivity.class));
                    }
                });
                break;
            case R.id.scan_torch:
                if (mTorchOn) {
                    Drawable drawable = getResources().getDrawable(R.drawable.actionbar_torch_on);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    mTorchBtn.setCompoundDrawables(drawable, null, null, null);
                    mTorchBtn.setText(R.string.scan_torch_on);
                } else {
                    Drawable drawable = getResources().getDrawable(R.drawable.actionbar_torch_off);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    mTorchBtn.setCompoundDrawables(drawable, null, null, null);
                    mTorchBtn.setText(R.string.scan_torch_off);
                }
                mTorchOn = !mTorchOn;
                mScanner.setTorch(mTorchOn);
                break;
            case R.id.back_layout:
                postAfterCameraOff(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(0, R.anim.slide_out_to_bottom);
                    }
                });
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            postAfterCameraOff(new Runnable() {
                @Override
                public void run() {
                    finish();
                    overridePendingTransition(0, R.anim.slide_out_to_bottom);
                }
            });
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_DECODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                String path = CropUtils.getImagePathFromUri(this, selectedImage);
                if (mFileDecoder == null) {
                    mFileDecoder = new Decoder(mFileDecodeCallback, null, null, null, null);
                }
                mFileDecoder.decode(path);
            }
        }
//        else if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK && data != null) {
//            final String cropPath = data.getStringExtra(ImageCropActivity.EXTRA_CROP_PATH);
//            if (!TextUtils.isEmpty(cropPath)) {
//                Intent intent = new Intent(this, FaceCodeActivity.class);
//                if (mScanMode == ScanMode.NORMAL) {
//                    intent.putExtra(FaceCodeActivity.EXTRA_MODE_INT, FaceCodeActivity.Mode.NORMAL.ordinal());
//                } else {
//                    intent.putExtra(FaceCodeActivity.EXTRA_MODE_INT, FaceCodeActivity.Mode.TRY.ordinal());
//                }
//                intent.putExtra(FaceCodeActivity.EXTRA_AVATAR_PATH, cropPath);
//                startActivity(intent);
//            }
//        }
    }

    /**
     * 从相册选择图片进行扫码
     */
    private void selectFaceBarcodeFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_GALLERY_DECODE);
    }

    /**
     * 弹出菜单
     */
    private void showMenu() {
        MMAlert.showAlert(ScanBarcodeActivity.this, "", getResources().getStringArray(R.array.generate_face_barcode_array), null, new MMAlert.OnAlertSelectId() {
            @Override
            public void onClick(int whichButton) {
//                switch (whichButton) {
//                    case 0:
//                        postAfterCameraOff(new Runnable() {
//                            @Override
//                            public void run() {
//                                Intent intent1 = new Intent(ScanBarcodeActivity.this, ImageCropActivity.class);
//                                intent1.putExtra(ImageCropActivity.EXTRA_PHOTO_FROM_INT, ImageCropActivity.From.CAMERA.ordinal());
//                                startActivityForResult(intent1, REQUEST_PHOTO);
//                            }
//                        });
//                        break;
//                    case 1:
//                        postAfterCameraOff(new Runnable() {
//                            @Override
//                            public void run() {
//                                Intent intent2 = new Intent(ScanBarcodeActivity.this, ImageCropActivity.class);
//                                intent2.putExtra(ImageCropActivity.EXTRA_PHOTO_FROM_INT, ImageCropActivity.From.GALLERY.ordinal());
//                                startActivityForResult(intent2, REQUEST_PHOTO);
//                            }
//                        });
//                        break;
//                }
            }
        });
    }

    private void postAfterCameraOff(final Runnable runnable) {
        mScanner.onPause();
        mScanBorderView.stopAnimator();

        mCameraSwitchTop.setVisibility(View.VISIBLE);
        mCameraSwitchBottom.setVisibility(View.VISIBLE);
        mSwitchTopInAnim = AnimationUtils.loadAnimation(this, R.anim.anim_camera_switch_top_in);
        mSwitchBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.anim_camera_switch_bottom_in);
        mCameraSwitchTop.startAnimation(mSwitchTopInAnim);
        mCameraSwitchBottom.startAnimation(mSwitchBottomInAnim);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBlackMaskView.setVisibility(View.VISIBLE);
                if (runnable != null) runnable.run();
            }
        }, 400);
    }

    /**
     * 扫码成功
     *
     * @param result
     */
    private void decodeSuccess(Result result) {
        if (result == null)
            return;
        final String content = result.getText();
        if (!TextUtils.isEmpty(content)) {
            ScanHistory history = new ScanHistory();
            history.setContent(content);
            history.setTimestamp(System.currentTimeMillis());
            history.setTitle("扫描结果");
            mScanHistoryHelper.insert(history);
        }

//        if (content != null && (content.startsWith("http://") || content.startsWith("https://"))) {
        if (!TextUtils.isEmpty(content)) {
            showDialog(this, getString(R.string.scan_dialog_access_url), content, getString(R.string.scan_dialog_access), new Runnable() {
                @Override
                public void run() {
                    try {
                        String startUrl = content;
                        if (!content.startsWith("http://") && !content.startsWith("https://")) {
                            startUrl = "http://" + content;
                        }

                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(Uri.parse(startUrl));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }, getString(R.string.scan_dialog_cancel), null);
        }
//        } else {
//            showDialog(this, getString(R.string.scan_dialog_access_url), content, getString(R.string.scan_dialog_access), new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Intent intent = new Intent();
//                        intent.setAction("android.intent.action.VIEW");
//                        intent.setData(Uri.parse(content));
//                        startActivity(intent);
//                    } catch (ActivityNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, getString(R.string.scan_dialog_cancel), null);
//        }
    }

    /**
     * 扫码成功弹出dialog
     */
    private AlertDialog showDialog(Context context, String title, String message, String ok, final Runnable okRunnable, String cancel, final Runnable cancelRunnable) {
        dismissDialog();
        mAlertDialog = new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton(ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (okRunnable != null)
                    okRunnable.run();
            }
        }).setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cancelRunnable != null)
                    cancelRunnable.run();
            }
        }).create();

        mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mScanner != null)
                    mScanner.startScanning();
            }
        });

        mAlertDialog.show();
        return mAlertDialog;
    }

    private void dismissDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    /**
     * 解析扫码出来的vcard
     *
     * @param content
     */
//    private void parseVcard(final String content) {
//        TipsDialog.getInstance().show(this, R.drawable.confirm_dialog_load, "", true, false);
//        CustomThreadPool.asyncWork(new Runnable() {
//                                       @Override
//                                       public void run() {
//                                           try {
//                                               Context context = ScanBarcodeActivity.this.getApplicationContext();
//                                               ResolveShortUrlRequest req1 = new ResolveShortUrlRequest(content);
//                                               ResolveShortUrlResponse resp1 = InternetUtils.request(context, req1);
//                                               if (resp1 != null && resp1.shortUrlInfo != null && resp1.shortUrlInfo.userId > 0) {
//                                                   int uid = resp1.shortUrlInfo.userId;
//                                                   int idt = resp1.shortUrlInfo.idt;
//                                                   // 是否领取
//                                                   boolean hasGot = false;
//                                                   if (resp1.shortUrlInfo.userBase != null) {
//                                                       ScanHistory scanHistory = new ScanHistory();
//                                                       scanHistory.setContent(content);
//                                                       scanHistory.setCategory(ScanHistory.CATEGORY_VCARD);
//                                                       scanHistory.setTimestamp(System.currentTimeMillis());
//                                                       scanHistory.setVcardId(resp1.shortUrlInfo.userBase.userId);
//                                                       scanHistory.setVcardName(resp1.shortUrlInfo.userBase.name);
//                                                       scanHistory.setVcardAvatar(resp1.shortUrlInfo.userBase.avatar);
//                                                       mScanHistoryHelper.insert(scanHistory);
//
//                                                       hasGot = resp1.shortUrlInfo.userBase.activation == 0 ? false : true;
//                                                   }
//                                                   if (SettingManager.getInstance().getUserId() > 0 && SettingManager.getInstance().getUserId() != uid) {
//                                                       mHandler.obtainMessage(WHAT_PARSE_VCARD_PROFILE_SUCCESS, uid, 0, resp1.shortUrlInfo.userBase).sendToTarget();
//                                                   } else {
//                                                       if (idt == 201 && !hasGot) {
//                                                           mHandler.obtainMessage(WHAT_PARSE_VCARD_SEED_SUCCESS, uid, 0, resp1.shortUrlInfo.userBase).sendToTarget();
//                                                       } else {
//                                                           mHandler.obtainMessage(WHAT_PARSE_VCARD_PROFILE_SUCCESS, uid, 0, resp1.shortUrlInfo.userBase).sendToTarget();
//                                                       }
//                                                   }
//                                                   return;
//                                               }
//                                           } catch (NetWorkException e) {
//                                               e.printStackTrace();
//                                           }
//                                           mHandler.sendEmptyMessage(WHAT_PARSE_VCARD_FAIL);
//                                       }
//                                   }
//        );
//    }
}
