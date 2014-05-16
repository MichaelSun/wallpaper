package com.michael.qrcode.utils;

import android.text.TextUtils;

/**
 * Created by zhangdi on 13-12-31.
 */
public class ImageUrlConnector {

    /**
     * 头像原图(c)、300(l)、120(b)、72(m)
     * 二维码头像原图(c)、300(l)、120(b)、66(m)
     */
    public static enum Size {
        C,
        L,
        B,
        M
    }

    /**
     * 拼接头像url
     *
     * @param originUrl
     * @param size      头像原图(c)、300(l)、120(b)、72(m)
     *                  二维码头像原图(c)、300(l)、120(b)、66(m)
     * @return
     */
    public static String connectUrl(String originUrl, Size size) {
        if (!TextUtils.isEmpty(originUrl) && size != null) {
            String s = "c";
            if (size == Size.C) {
                s = "c";
            } else if (size == Size.L) {
                s = "l";
            } else if (size == Size.B) {
                s = "b";
            } else if (size == Size.M) {
                s = "m";
            }

            int lastDot = originUrl.lastIndexOf(".");
            if (lastDot != -1) {
                StringBuilder builder = new StringBuilder(originUrl);
                builder.insert(lastDot, s);
                return builder.toString();
            }
        }
        return originUrl;
    }
}
