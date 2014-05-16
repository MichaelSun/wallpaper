package com.michael.qrcode.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.ads.InterstitialAd;
import com.michael.qrcode.R;

/**
 * Created by michael on 14-5-13.
 */
public class BaseActivity extends Activity {

    protected ActionBar mActionBar;

    protected AdView mAdView;

    protected InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(android.R.id.content).setBackgroundColor(Color.parseColor("#f0eff3"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void enableHomeButton(String title) {
        mActionBar= getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void initBannerAd() {
        mAdView = new AdView(this, AdSize.BANNER, "a15375b21eae4bb");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.ad_content);
        // Add the adView to it
        layout.addView(mAdView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                   RelativeLayout.LayoutParams.WRAP_CONTENT));
        // Initiate a generic request to load it with an ad
        mAdView.loadAd(new AdRequest());
    }

    protected void initSplashAd() {
        // 制作插页式广告。
        interstitial = new InterstitialAd(this, "a15375b21eae4bb");
        // 创建广告请求。
        AdRequest adRequest = new AdRequest();
        // 开始加载插页式广告。
        interstitial.loadAd(adRequest);
    }
}