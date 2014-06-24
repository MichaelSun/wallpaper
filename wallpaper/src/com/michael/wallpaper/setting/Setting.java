package com.michael.wallpaper.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.michael.wallpaper.AppConfig;

/**
 * Created by michael on 14-6-24.
 */
public class Setting {

    public static Setting gSetting = new Setting();

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public static Setting getInstace() {
        return gSetting;
    }

    public void init(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSharedPreferences.edit();
    }

    private Setting() {
    }

    public void setMode(int mode) {
        mEditor.putInt("mode", mode).commit();
    }

    public int getMode() {
        return mSharedPreferences.getInt("mode", AppConfig.SERIES_MODE);
    }

}
