package com.michael.wallpaper.dao.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.michael.wallpaper.dao.model.DaoMaster;
import com.michael.wallpaper.dao.model.DaoSession;


public class DaoUtils {

    private static final String DATABASE_NAME = "sexy_belles";

    private static DaoSession sDaoSession;

    public synchronized static DaoSession getDaoSession(Context context) {
        if (sDaoSession == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DATABASE_NAME, null);
            SQLiteDatabase database = helper.getWritableDatabase();
            DaoMaster m = new DaoMaster(database);

            sDaoSession = m.newSession();
        }

        return sDaoSession;
    }
}
