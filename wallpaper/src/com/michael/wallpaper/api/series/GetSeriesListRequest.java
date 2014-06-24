package com.michael.wallpaper.api.series;

import com.jesson.android.internet.core.annotations.RequiredParam;
import com.jesson.android.internet.core.annotations.RestMethodUrl;
import com.michael.wallpaper.api.BelleRequestBase;
import com.michael.wallpaper.setting.Setting;

/**
 * Created by zhangdi on 14-3-11.
 */

@RestMethodUrl("series/list")
public class GetSeriesListRequest extends BelleRequestBase<GetSeriesListResponse> {

    @RequiredParam("mode")
    public int mode = Setting.getInstace().getMode();

    public GetSeriesListRequest() {
        setIgnoreResult(true);
    }

}
