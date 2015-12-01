package com.michael.wallpaper.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.jesson.android.widget.Toaster;
import com.michael.wallpaper.AppConfig;
import com.michael.wallpaper.R;
import com.michael.wallpaper.dao.model.Series;
import com.michael.wallpaper.fragment.NavigationDrawerFragment;
import com.michael.wallpaper.fragment.PhotoStreamFragment;
import com.michael.wallpaper.helper.SeriesHelper;
import com.michael.wallpaper.setting.Setting;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsManager;

import java.util.List;

public class MainActivity extends BaseActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private int mRefreshTime = 0;

    private Series mSeries;

//    private InterstitialAd iad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBannerAd();
        initInterstitialAd();
        initAppWall();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                                        getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        OffersManager.getInstance(getApplicationContext()).onAppLaunch();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (interstitial.isReady()) {
            interstitial.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        MobclickAgent.flush(getApplication());

        OffersManager.getInstance(getApplicationContext()).onAppExit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        List<Series> seriesList = SeriesHelper.getInstance().getSeriesList();
        if (position < 0 || position > seriesList.size() - 1) {
            return;
        }
        mSeries = seriesList.get(position);
        if (mSeries.getType() == -3) {
            OffersManager.getInstance(this).showOffersWall();
        } else if (mSeries.getType() == -2) {
            if (Setting.getInstace().getMode() != AppConfig.SERIES_MODE) {
                return;
            }
            int point = PointsManager.getInstance(this).queryPoints();
            if (point < 60) {
                showWallInfoDialog(point);
            } else {
                Setting.getInstace().setMode(1);
                Toaster.show(getApplicationContext(), "开启隐藏成功，请退出应用重新进入");
            }
        } else {
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getFragmentManager();
            String tag = String.valueOf(mSeries.getType());
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment == null) {
                fragment = PhotoStreamFragment.newInstance(mSeries);
            }
            fragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit();
        }
    }

    public void onSectionAttached(Series series) {
        mTitle = series.getTitle();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem item = menu.findItem(R.id.action_refresh);
            if (mSeries != null && mSeries.getType() < 0) {
                item.setVisible(false);
            } else {
                item.setVisible(true);
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_feedback) {
//            FeedbackAgent agent = new FeedbackAgent(this);
//            agent.startFeedbackActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBannerAd() {
        mAdView = new AdView(this, AdSize.BANNER, "a15368dc3248e7e");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.ad_content);
        // Add the adView to it
        layout.addView(mAdView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                   RelativeLayout.LayoutParams.WRAP_CONTENT));
        // Initiate a generic request to load it with an ad
        mAdView.loadAd(new AdRequest());
    }

    private void initInterstitialAd() {
        initSplashAd();
    }

    private void initAppWall() {
//        GdtAppwall.init(this, AppConfig.GDT_AD_APPID, AppConfig.GDT_AD_APPWALL_POSID, AppConfig.DEBUG);
    }

    public void onRefresh() {
        mRefreshTime += 1;
        if (mRefreshTime % 5 == 0) {
            if (interstitial.isReady()) {
                interstitial.show();
            }
        }

//        if (mSeries != null) {
//            HashMap<String, String> map = new HashMap<String, String>();
//            map.put("type", String.valueOf(mSeries.getType()));
//            map.put("title", mSeries.getTitle());
//            map.put("mode", String.valueOf(AppConfig.SERIES_MODE));
//            MobclickAgent.onEvent(this, "Refresh", map);
//        }
    }

    private void showWallInfoDialog(int currentUserPoint) {
        String tips = String.format(getString(R.string.offer_info_detail), 60, currentUserPoint);
        View view = this.getLayoutInflater().inflate(R.layout.offer_tips_view, null);
        TextView tv = (TextView) view.findViewById(R.id.tips);
        tv.setText(tips);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.tips_title).setView(view)
                                 .setPositiveButton(R.string.download_offer, new DialogInterface.OnClickListener() {

                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         OffersManager.getInstance(MainActivity.this).showOffersWall();
                                     }
                                 })
                                 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                     }
                                 }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

}