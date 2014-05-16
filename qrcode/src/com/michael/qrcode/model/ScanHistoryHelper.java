package com.michael.qrcode.model;

import android.content.Context;
import com.michael.qrcode.dao.model.DaoSession;
import com.michael.qrcode.dao.model.ScanHistory;
import com.michael.qrcode.dao.model.ScanHistoryDao;
import com.michael.qrcode.dao.utils.DaoUtils;

import java.util.List;

/**
 * Created by zhangdi on 13-12-20.
 */
public class ScanHistoryHelper {

    private ScanHistoryDao mScanHistoryDao;

    public ScanHistoryHelper(Context context) {
        DaoSession session = DaoUtils.getDaoSession(context);
        mScanHistoryDao = session.getScanHistoryDao();
    }

    /**
     * load scan histories before the lastScanHistory, if lastScanHistory is null, will return the newest scan histories
     *
     * @param lastScanHistory
     */
    public List<ScanHistory> load(ScanHistory lastScanHistory, int count) {
        List<ScanHistory> list;
        if (lastScanHistory == null) {
            list = mScanHistoryDao.queryBuilder().orderDesc(ScanHistoryDao.Properties.Timestamp).limit(count).build().forCurrentThread().list();
        } else {
            list = mScanHistoryDao.queryBuilder().where(ScanHistoryDao.Properties.Timestamp.lt(lastScanHistory.getTimestamp())).
                    orderDesc(ScanHistoryDao.Properties.Timestamp).limit(count).build().forCurrentThread().list();
        }
        return list;
    }

    public void insert(ScanHistory scanHistory) {
        if (scanHistory != null) {
            mScanHistoryDao.queryBuilder().where(ScanHistoryDao.Properties.Content.eq(scanHistory.getContent())).buildDelete().
                    executeDeleteWithoutDetachingEntities();
            mScanHistoryDao.insertOrReplace(scanHistory);
        }
    }

    public void clear() {
        mScanHistoryDao.deleteAll();
    }

}
