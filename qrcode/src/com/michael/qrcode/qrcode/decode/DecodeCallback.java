package com.michael.qrcode.qrcode.decode;

import com.google.zxing.Result;

/**
 * Created with IntelliJ IDEA.
 * User: di.zhang
 * Date: 13-12-10
 * Time: 上午11:25
 * To change this template use File | Settings | File Templates.
 */
public interface DecodeCallback {

    public void onDecodeSuccess(Result result);

    public void onDecodeFail();
}
