package com.michael.qrcode.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.michael.qrcode.R;
import com.michael.qrcode.dao.model.ScanHistory;
import com.plugin.common.utils.UtilsRuntime;

import java.util.List;

public class ScanHistoryAdapter extends BaseAdapter {

    public static interface OnLoadMoreListener {
        public void onLoadMore(ScanHistory lastScanHistory);
    }

//    private Activity mActivity;
    private LayoutInflater mLayoutInflater;

    private List<ScanHistory> mScanHistories;

    public ScanHistoryAdapter(Activity activity, List<ScanHistory> scanHistories) {
//        mActivity = activity;
//        if (!(mActivity instanceof OnLoadMoreListener)) {
//            throw new RuntimeException(activity.getLocalClassName() + " must implements ScanHistoryAdapter.OnLoadMoreListener.");
//        }
        mLayoutInflater = activity.getLayoutInflater();
        mScanHistories = scanHistories;
    }

    @Override
    public int getCount() {
        return mScanHistories == null ? 0 : mScanHistories.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // load more
//        if (position == getCount() - 1) {
//            if (mActivity instanceof OnLoadMoreListener) {
//                ((OnLoadMoreListener) mActivity).onLoadMore(mScanHistories.get(getCount() - 1));
//            }
//        }

        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.barcode_history_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScanHistory scanHistory = mScanHistories.get(position);

        holder.content.setText(scanHistory.getContent());
        holder.title.setText(scanHistory.getTitle());
        holder.time.setText(UtilsRuntime.formatTime(scanHistory.getTimestamp()));

        return convertView;
    }

    static final class ViewHolder {
        TextView title;
        TextView content;
        TextView time;

        ViewHolder(View root) {
            title = (TextView) root.findViewById(R.id.barcode_title);
            content = (TextView) root.findViewById(R.id.barcode_content);
            time = (TextView) root.findViewById(R.id.time_interval);
        }
    }

}