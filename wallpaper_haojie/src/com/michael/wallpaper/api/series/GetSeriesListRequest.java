package com.michael.wallpaper.api.series;

import com.jesson.android.internet.core.annotations.RequiredParam;
import com.jesson.android.internet.core.annotations.RestMethodUrl;
import com.michael.wallpaper.AppConfig;
import com.michael.wallpaper.api.BelleRequestBase;

/**
 * Created by zhangdi on 14-3-11.
 */

@RestMethodUrl("series/list")
public class GetSeriesListRequest extends BelleRequestBase<GetSeriesListResponse> {

    @RequiredParam("mode")
    public int mode = AppConfig.SERIES_MODE;

    public GetSeriesListRequest() {
        setIgnoreResult(true);
    }

}
