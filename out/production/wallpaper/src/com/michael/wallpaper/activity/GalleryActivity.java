package com.michael.wallpaper.activity;

import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.jesson.android.widget.Toaster;
import com.michael.wallpaper.R;
import com.michael.wallpaper.adapter.GalleryAdapter;
import com.michael.wallpaper.helper.CollectHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class GalleryActivity extends BaseActivity {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_PHOTO_URI_LIST = "photo_uri_list";
    private static final String EXTRA_POSITION = "position";

    private TextView mPaginationTv;

    private ViewPager mViewPager;

    private GalleryAdapter mPagerAdapter;

    private ArrayList<String> mPhotoUriList;
    private String mTitle;
    private int mPosition;
    private int mSwitchCount = 0;

    private Intent mShareIntent;

    private CollectHelper mCollectHelper;

//    private InterstitialAd iad;

    private static final int WHAT_SAVE_SUCCESS = 1000;
    private static final int WHAT_SAVE_FAIL = 2000;
    private static final int WHAT_WALLPAPER_SUCCESS = 3000;
    private static final int WHAT_WALLPAPER_FAIL = 4000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", mTitle);
            switch (msg.what) {
                case WHAT_SAVE_SUCCESS:
                    Toaster.show(GalleryActivity.this, R.string.save_gallery_success);
                    map.put("ifSuccess", "success");
//                    MobclickAgent.onEvent(GalleryActivity.this, "SaveGallery", map);
                    break;
                case WHAT_SAVE_FAIL:
                    Toaster.show(GalleryActivity.this, R.string.save_gallery_fail);
                    map.put("ifSuccess", "fail");
//                    MobclickAgent.onEvent(GalleryActivity.this, "SaveGallery", map);
                    break;
                case WHAT_WALLPAPER_SUCCESS:
                    Toaster.show(GalleryActivity.this, R.string.set_wallpaper_success);
                    map.put("ifSuccess", "success");
//                    MobclickAgent.onEvent(GalleryActivity.this, "Wallpaper", map);
                    break;
                case WHAT_WALLPAPER_FAIL:
                    Toaster.show(GalleryActivity.this, R.string.set_wallpaper_success);
                    map.put("ifSuccess", "fail");
//                    MobclickAgent.onEvent(GalleryActivity.this, "Wallpaper", map);
                    break;
            }

            if (interstitial.isReady()) {
                interstitial.show();
            }
        }
    };

    public static void startViewLarge(Context context, String title, ArrayList<String> uriList, int position) {
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putStringArrayListExtra(EXTRA_PHOTO_URI_LIST, uriList);
        intent.putExtra(EXTRA_POSITION, position);
        context.startActivity(intent);

//        MobclickAgent.onEvent(context, "ViewLarge", title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_large);

        this.initSplashAd();

        initBanner();

        if (savedInstanceState != null) {
            mPhotoUriList = savedInstanceState.getStringArrayList(EXTRA_PHOTO_URI_LIST);
            mTitle = savedInstanceState.getString(EXTRA_TITLE);
            mPosition = savedInstanceState.getInt(EXTRA_POSITION);
        } else {
            mPhotoUriList = getIntent().getStringArrayListExtra(EXTRA_PHOTO_URI_LIST);
            mTitle = getIntent().getStringExtra(EXTRA_TITLE);
            mPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        }

        getActionBar().setTitle(mTitle);
        mShareIntent = getDefaultIntent();

        mPaginationTv = (TextView) findViewById(R.id.pagination);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new GalleryAdapter(this, mPhotoUriList);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                mPaginationTv.setText((position + 1) + "/" + mPagerAdapter.getCount());

                if (++mSwitchCount % 10 == 0) {
//                    iad.loadAd();
                }

                String url = getCurrentUrl();
                if (!TextUtils.isEmpty(url)) {
                    File file = ImageLoader.getInstance().getDiscCache().get(url);
                    mShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                ActionBar actionBar = getActionBar();
                if (actionBar.isShowing()) {
                    actionBar.hide();
                }
            }
        });

        mViewPager.setCurrentItem(mPosition);
        mPaginationTv.setText((mPosition + 1) + "/" + mPagerAdapter.getCount());

        mCollectHelper = new CollectHelper(this);
//
//        iad = new InterstitialAd(this, AppConfig.GDT_AD_APPID, AppConfig.GDT_AD_INTERSTITIAL_POSID);
//        iad.setAdListener(new InterstitialAdListener() {
//            @Override
//            public void onFail() {
//
//            }
//
//            @Override
//            public void onBack() {
//
//            }
//
//            @Override
//            public void onAdReceive() {
//                iad.closePopupWindow();
//                iad.show();
//            }
//        });

    }

    private void initBanner() {
        mAdView = new AdView(this, AdSize.BANNER, "a15368dc3248e7e");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.ad_content);
        // Add the adView to it
        layout.addView(mAdView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                          RelativeLayout.LayoutParams.WRAP_CONTENT));
        // Initiate a generic request to load it with an ad
        mAdView.loadAd(new AdRequest());
    }

    @Override
    protected void onStop() {
        super.onStop();
        ImageLoader.getInstance().stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(EXTRA_PHOTO_URI_LIST, mPhotoUriList);
        outState.putString(EXTRA_TITLE, mTitle);
        outState.putInt(EXTRA_POSITION, mPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getMenuInflater().inflate(R.menu.view_large, menu);
        ShareActionProvider shareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
        shareActionProvider.setShareIntent(mShareIntent);
        shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
//                MobclickAgent.onEvent(GalleryActivity.this, "Share", mTitle);
                return true;
            }
        });
        shareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_collect);
        String url = mPhotoUriList.get(mPosition);
        if (mCollectHelper.isCollected(url)) {
            item.setTitle(R.string.action_cancel_collect);
        } else {
            item.setTitle(R.string.action_collect);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.action_save: {
                saveToGallery();
                return true;
            }
            case R.id.action_wallpaper: {
                setWallpaper();
                return true;
            }
            case R.id.action_collect: {
                String url = getCurrentUrl();
                if (mCollectHelper.isCollected(url)) {
//                    MobclickAgent.onEvent(GalleryActivity.this, "Collect", "collect");
                    mCollectHelper.cancelCollectBelle(url);
                    Toaster.show(this, R.string.cancel_collect_success);
                } else {
//                    MobclickAgent.onEvent(GalleryActivity.this, "Collect", "cancel collect");
                    mCollectHelper.collectBelle(url);
                    Toaster.show(this, R.string.collect_success);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent getDefaultIntent() {
        mShareIntent = new Intent(Intent.ACTION_SEND);
        mShareIntent.setType("image/*");
        return mShareIntent;
    }

    private String getCurrentUrl() {
        if (mPosition >= 0 && mPosition < mPhotoUriList.size()) {
            return mPhotoUriList.get(mPosition);
        }
        return null;
    }

    private void saveToGallery() {
        new Thread() {
            @Override
            public void run() {
                String url = mPhotoUriList.get(mPosition);
                if (!TextUtils.isEmpty(url)) {
                    try {
                        File file = ImageLoader.getInstance().getDiscCache().get(url);
                        if (file != null && file.exists()) {
                            ContentResolver cr = getContentResolver();
                            String uri = MediaStore.Images.Media.insertImage(cr, file.getAbsolutePath(), "belle" + url, "belle" + url);

                            String data = null;
                            String[] projection = {MediaStore.Images.Media.DATA};
                            Cursor cursor = getContentResolver().query(Uri.parse(uri), projection, null, null, null);
                            if (cursor != null) {
                                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                                cursor.moveToFirst();
                                data = cursor.getString(column_index);
                                cursor.close();
                            }
                            if (data != null) {
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(data))));
                                mHandler.sendEmptyMessage(WHAT_SAVE_SUCCESS);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
                        System.gc();
                    }
                }
                mHandler.sendEmptyMessage(WHAT_SAVE_FAIL);
            }
        }.start();
    }

    private void setWallpaper() {
        new Thread() {
            @Override
            public void run() {
                String url = mPhotoUriList.get(mPosition);
                if (!TextUtils.isEmpty(url)) {
                    File file = ImageLoader.getInstance().getDiscCache().get(url);
                    if (file != null && file.exists()) {
                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(GalleryActivity.this);
                        try {
                            wallpaperManager.setStream(new FileInputStream(file));
                            mHandler.sendEmptyMessage(WHAT_WALLPAPER_SUCCESS);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                mHandler.sendEmptyMessage(WHAT_WALLPAPER_FAIL);
            }
        }.start();

    }

}
