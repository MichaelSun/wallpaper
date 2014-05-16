package com.michael.qrcode.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.plugin.common.utils.files.DiskManager;
import com.plugin.common.utils.image.ExifHelper;

import java.io.*;
import java.net.MalformedURLException;

/**
 * Created by zhangdi on 13-12-13.
 */
public class CropUtils {

    public static String getImagePathFromUri(Context context, Uri uri) {
        if (uri.toString().startsWith("content://com.android.gallery3d.provider")) {
            uri = Uri.parse(uri.toString().replace("com.android.gallery3d", "com.google.android.gallery3d"));
        }

        String imageFilePath = null;
        String schema = uri.getScheme();
        if (schema != null) {
            if (schema.equals("file")) {
                imageFilePath = uri.getPath();
            } else if (schema.equals("content")) {
                imageFilePath = getUriData(context, uri);
            } else {
                imageFilePath = saveImageByUrl(context, uri, "picasa_temp_file.jpg");
            }
        }

        return imageFilePath;
    }

    public static String getUriData(Context context, Uri uri) {
        String data = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            data = cursor.getString(column_index);
            cursor.close();
        }
        return data;
    }

    public static String saveImageByUrl(Context context, Uri uri, String tempFileName) {
        String savePath = DiskManager.tryToFetchCachePathByTypeBinding(DiskManager.DiskCacheType.TMP);
        File cacheDir = savePath == null ? context.getCacheDir() : new File(savePath);
        if (!cacheDir.isDirectory() && cacheDir.exists()) {
            cacheDir.delete();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File f = new File(cacheDir, tempFileName);
        String path;
        try {
            InputStream is = null;
            is = context.getContentResolver().openInputStream(uri);

            OutputStream os = new FileOutputStream(f);
            byte buf[] = new byte[8192];
            int length = 0;
            int totalCount = 0;
            while ((length = is.read(buf)) > 0) {
                os.write(buf, 0, length);
                totalCount += length;
            }
            os.close();
            is.close();

            path = totalCount > 0 ? f.getAbsolutePath() : null;
        } catch (MalformedURLException e) {
            path = null;
        } catch (IOException e) {
            path = null;
        }

        return path;
    }

    public static File getTempCameraFile(Context context) {
        String savePath = DiskManager.tryToFetchCachePathByTypeBinding(DiskManager.DiskCacheType.TMP);
        File cacheDir = (savePath == null) ? context.getCacheDir() : new File(savePath);
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
        return f;
    }

    public static String getCropPath(Context context) {
        String savePath = DiskManager.tryToFetchCachePathByTypeBinding(DiskManager.DiskCacheType.BASE) + "/crop";
        File cacheDir = savePath == null ? context.getCacheDir() : new File(savePath);
        if (!cacheDir.isDirectory() || !cacheDir.canWrite()) {
            cacheDir.delete();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File f = new File(cacheDir, "vcard" + System.currentTimeMillis() + ".jpg");
        if (f.exists()) {
            f.delete();
        }
        return f.getAbsolutePath();
    }

    public static String cropPhoto(Context context, String path, Rect rect, int maxMemorySize) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(path, true);
            BitmapFactory.Options ops = createBitmapFactoryOptions(path, rect.width(), rect.height(), maxMemorySize);
            Bitmap bitmap = decoder.decodeRegion(rect, ops);
            decoder.recycle();

            int rotation = ExifHelper.getRotationFromExif(path);
            if (bitmap != null) {
                if (rotation != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) rotation);
                    Bitmap tmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    if (tmp != null) {
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                }
                String cropPath = CropUtils.getCropPath(context);
                if (!TextUtils.isEmpty(cropPath)) {
                    File file = new File(cropPath);
                    file.setLastModified(System.currentTimeMillis());
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    return cropPath;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BitmapFactory.Options createBitmapFactoryOptions(String bitmapFilePath, int targetWidth, int targetHeight, int maxMemorySize) {
        BitmapFactory.Options ret = new BitmapFactory.Options();

        ret.inPurgeable = true;
        ret.inInputShareable = true;

        long fileMemorySize = targetWidth * targetHeight * 4;
        int sample = 1;
        if (fileMemorySize <= maxMemorySize) {
            sample = 1;
        } else if (fileMemorySize <= maxMemorySize * 4) {
            sample = 2;
        } else {
            long times = fileMemorySize / maxMemorySize;
            sample = (int) (Math.log(times) / Math.log(2.0)) + 1;
            int inSampleScale = (int) (Math.log(sample) / Math.log(2.0));
            sample = (int) Math.scalb(1, inSampleScale);

            long curFileMemorySize = (targetWidth / sample) * (targetHeight / sample) * 4;
            if (curFileMemorySize > maxMemorySize) {
                sample = sample * 2;
            }
        }

        ret.inSampleSize = sample;

        return ret;
    }

}
