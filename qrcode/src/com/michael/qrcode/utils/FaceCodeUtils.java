package com.michael.qrcode.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import com.plugin.common.utils.files.DiskManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhangdi on 14-1-7.
 */
public class FaceCodeUtils {

    public static String saveFaceCodeToDisk(Context context, Bitmap bitmap) {
        if (bitmap != null && context != null) {
            try {
                String path = getFaceCodePath(context);
                if (!TextUtils.isEmpty(path)) {
                    FileOutputStream fos = new FileOutputStream(new File(path));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    return path;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取脸码保存路径
     */
    public static String getFaceCodePath(Context context) {
        String savePath = DiskManager.tryToFetchCachePathByTypeBinding(DiskManager.DiskCacheType.BASE) + "/face_barcode";
        File cacheDir = savePath == null ? context.getCacheDir() : new File(savePath);
        if (!cacheDir.isDirectory() || !cacheDir.canWrite()) {
            cacheDir.delete();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File f = new File(cacheDir, System.currentTimeMillis() + ".jpg");
        if (f.exists()) {
            f.delete();
        }
        return f.getAbsolutePath();
    }
}
