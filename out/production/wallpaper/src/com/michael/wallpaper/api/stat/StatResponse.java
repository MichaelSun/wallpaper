package com.michael.wallpaper.api.stat;

import com.jesson.android.internet.core.ResponseBase;
import com.jesson.android.internet.core.json.JsonProperty;

/**
 * Created by michael on 14-5-13.
 */
public class StatResponse extends ResponseBase {

    @JsonProperty("msg")
    public String msg;

    @JsonProperty("url")
    public String url;

    @Override
    public String toString() {
        return "StatResponse{" +
                   "msg='" + msg + '\'' +
                   ", url='" + url + '\'' +
                   "} " + super.toString();
    }
}
