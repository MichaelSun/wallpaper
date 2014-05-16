package com.michael.qrcode.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import com.michael.qrcode.R;

/**
 * Created by zhangdi on 14-1-6.
 */
public class SoundHelper {

    private static SoundHelper gSoundHelper = new SoundHelper();

    private SoundPool mBeepSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);

    private int mScanCompletedId;

    private int mNotifyId;

    private SoundHelper() {

    }

    public static SoundHelper getInstance() {
        return gSoundHelper;
    }

    public void init(Context context) {
        mScanCompletedId = mBeepSoundPool.load(context, R.raw.qrcode_completed, 0);
        mNotifyId = mBeepSoundPool.load(context, R.raw.notify, 0);
    }

    public void playNotify() {
        mBeepSoundPool.play(mNotifyId, 1, 1, 0, 0, 1);
    }

    public void playScanCompleted() {
        mBeepSoundPool.play(mScanCompletedId, 1, 1, 0, 0, 1);
    }
}
