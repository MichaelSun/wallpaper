package com.michael.wallpaper.api.belle;

import com.jesson.android.internet.core.annotations.RequiredParam;
import com.jesson.android.internet.core.annotations.RestMethodUrl;
import com.michael.wallpaper.api.BelleRequestBase;

/**
 * Created by zhangdi on 14-3-8.
 */
@RestMethodUrl("belle/random")
public class RandomGetBelleListRequest extends BelleRequestBase<RandomGetBelleListResponse> {

    @RequiredParam("type")
    public int type;

    @RequiredParam("count")
    public int count;

}
