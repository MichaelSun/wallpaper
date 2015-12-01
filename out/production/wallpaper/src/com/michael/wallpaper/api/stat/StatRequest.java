package com.michael.wallpaper.api.stat;

import com.jesson.android.internet.core.annotations.RequiredParam;
import com.jesson.android.internet.core.annotations.RestMethodUrl;
import com.michael.wallpaper.api.BelleRequestBase;

/**
 * Created by michael on 14-5-13.
 */

@RestMethodUrl("http://sflashlight.5helper.com:8080/beastat/bizhi.jsp")
public class StatRequest extends BelleRequestBase<StatResponse> {

    @RequiredParam("coop")
    private String coop;

    @RequiredParam("product")
    private String product;

    @RequiredParam("version")
    private String version;

    @RequiredParam("platform")
    private String platform;

    @RequiredParam("imei")
    private String imei;

    @RequiredParam("imsi")
    private String imsi;

    @RequiredParam("mobilenum")
    private String mobilenum;

    @RequiredParam("mac")
    private String mac;

    @RequiredParam("network")
    private String network;

    @RequiredParam("device")
    private String device;

    @RequiredParam("serialno")
    private String serialno;

    @RequiredParam("androidid")
    private String androidid;

    public StatRequest(String coop, String product, String version, String platform, String imei, String imsi, String mobilenum, String mac, String network, String device, String serialno, String androidid) {
        this.coop = coop;
        this.product = product;
        this.version = version;
        this.platform = platform;
        this.imei = imei;
        this.imsi = imsi;
        this.mobilenum = mobilenum;
        this.mac = mac;
        this.network = network;
        this.device = device;
        this.serialno = serialno;
        this.androidid = androidid;
    }
}
