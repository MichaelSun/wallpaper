package com.michael.wallpaper.api.belle;

import com.jesson.android.internet.core.annotations.RequiredParam;
import com.jesson.android.internet.core.annotations.RestMethodUrl;
import com.michael.wallpaper.api.BelleRequestBase;

/**
 * Created by zhangdi on 14-3-7.
 */
@RestMethodUrl("belle/list")
public class GetBelleListRequest extends BelleRequestBase<GetBelleListResponse> {

    @RequiredParam("type")
    public int type;

    @RequiredParam("id")
    public long id;

    @RequiredParam("count")
    public int count;

}
