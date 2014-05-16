package com.michael.qrcode.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.michael.qrcode.R;
import com.michael.qrcode.adapter.ScanHistoryAdapter;
import com.michael.qrcode.dao.model.ScanHistory;
import com.michael.qrcode.model.ScanHistoryHelper;
import com.plugin.common.utils.CustomThreadPool;

import java.util.ArrayList;
import java.util.List;

public class ScanHistoryActivity extends BaseActivity {

    private ScanHistoryAdapter mScanHistoryAdapter;
    private List<ScanHistory> mScanHistories = new ArrayList<ScanHistory>();

    private ScanHistoryHelper mScanHistoryHelper;

    private ListView mListView;

    private TextView mTextView;

    private AlertDialog mAlertDialog = null;

    private static final int WHAT_LOAD_HISTORY = 1;

    // 分页大小
    private static final int PAGE_SIZE = 1000;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_history);

        initBannerAd();
        initSplashAd();

        enableHomeButton(getString(R.string.title_history));

        mScanHistoryHelper = new ScanHistoryHelper(getApplicationContext());
        initUI();

        loadScanHistory(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (interstitial.isReady()) {
            interstitial.show();
        }
    }

    private void initUI() {
        mListView = (ListView) findViewById(R.id.listview);
        mTextView = (TextView) findViewById(R.id.empty_view);
        mScanHistoryAdapter = new ScanHistoryAdapter(this, mScanHistories);
        mListView.setAdapter(mScanHistoryAdapter);
        mTextView.setVisibility(View.GONE);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < mScanHistories.size()) {
                    ScanHistory history = mScanHistories.get(position);
                    if (!TextUtils.isEmpty(history.getContent())) {
                        try {
                            String startUrl = history.getContent();
                            if (!history.getContent().startsWith("http://") && !history.getContent().startsWith("https://")) {
                                startUrl = "http://" + history.getContent();
                            }

                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setData(Uri.parse(startUrl));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * load scan histories before the lastScanHistory, if lastScanHistory is null, will return the newest scan histories
     *
     * @param lastScanHistory
     */
    private void loadScanHistory(final ScanHistory lastScanHistory) {
        CustomThreadPool.asyncWork(new Runnable() {
            @Override
            public void run() {
                List<ScanHistory> list = mScanHistoryHelper.load(lastScanHistory, PAGE_SIZE);
                if (list != null && list.size() > 0) {
                    Message msg = mHandler.obtainMessage(WHAT_LOAD_HISTORY, 0, 0, list);
                    msg.sendToTarget();
                }
            }
        });
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_LOAD_HISTORY) {
                List<ScanHistory> list = (List<ScanHistory>) msg.obj;
                if (list != null) {
                    mScanHistories.addAll(list);
                    mScanHistoryAdapter.notifyDataSetChanged();
                }
            }
        }
    };

}