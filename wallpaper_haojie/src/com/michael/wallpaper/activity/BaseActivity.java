package com.michael.wallpaper.activity;

import android.app.Activity;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.ads.InterstitialAd;

/**
 * Created by zhangdi on 14-3-5.
 */
public class BaseActivity extends Activity {

    protected AdView mAdView;

    protected InterstitialAd interstitial;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    protected void initSplashAd() {
        // 制作插页式广告。
        interstitial = new InterstitialAd(this, "a1538430fede130");

        // 创建广告请求。
        AdRequest adRequest = new AdRequest();

        // 开始加载插页式广告。
        interstitial.loadAd(adRequest);
    }

}
