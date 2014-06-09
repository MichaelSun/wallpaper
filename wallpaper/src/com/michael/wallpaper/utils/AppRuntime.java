package com.michael.wallpaper.utils;

import android.content.Context;
import android.os.Build;
import com.jesson.android.Jess;
import com.jesson.android.internet.InternetUtils;
import com.jesson.android.utils.CustomThreadPool;
import com.jesson.android.utils.UtilsRuntime;
import com.michael.wallpaper.api.stat.StatRequest;
import com.michael.wallpaper.api.stat.StatResponse;

/**
 * Created by michael on 14-5-13.
 */
public class AppRuntime {

    public static void updateStat(final Context context) {
        if (context != null) {
            CustomThreadPool.asyncWork(new Runnable() {
                @Override
                public void run() {
                    try {
                        StatRequest request = new StatRequest("百度",
                                                                 "5",
                                                                 UtilsRuntime.getVersionName(context),
                                                                 "android",
                                                                 UtilsRuntime.getIMEI(context),
                                                                 UtilsRuntime.getIMSI(context),
                                                                 UtilsRuntime.getCurrentPhoneNumber(context),
                                                                 UtilsRuntime.getLocalMacAddress(context),
                                                                 "0",
                                                                 Build.MODEL,
                                                                 UtilsRuntime.getIMEI(context),
                                                                 UtilsRuntime.getIMEI(context));
                        StatResponse response = InternetUtils.request(context, request);
                        if (response != null) {
                            Jess.LOGD("[[updateStat]] response : " + response);
                        } else {
                            Jess.LOGD("[[updateStat]] response null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

}
