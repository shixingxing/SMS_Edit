package com.example.ducdung.sms_edit.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ducdung.sms_edit.R;
import com.example.ducdung.sms_edit.constant.CommonConstant;
import com.example.ducdung.sms_edit.model.SmsThread;
import com.example.ducdung.sms_edit.util.MultiPathImageLoader;

import java.util.List;

public class ThreadItemAdapter extends BaseAdapter {
    Context context;
    List<SmsThread> threadList;

    public ThreadItemAdapter(Context context, List<SmsThread> threadList) {
        this.context = context;
        this.threadList = threadList;
    }

    public void setThreadList(List<SmsThread> threadList) {
        this.threadList = threadList;
    }

    @Override
    public int getCount() {
        return threadList.size();
    }

    @Override
    public Object getItem(int position) {
        return threadList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return threadList.get(position).theadId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SmsThread thread = (SmsThread) getItem(position);

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.msg_thread_item, null);
        }

        TextView fromView = (TextView) convertView.findViewById(R.id.from);
        TextView bodyView = (TextView) convertView.findViewById(R.id.content);
        TextView tsView = (TextView) convertView.findViewById(R.id.timestamp);

        MultiPathImageLoader.loadImage((ImageView) convertView.findViewById(R.id.avatar), thread.avatar);
        fromView.setText(TextUtils.isEmpty(thread.displayName) ? thread.from : thread.displayName);
        bodyView.setText(thread.content);
        String dateStr = CommonConstant.dateFormat.format(thread.timestamp);
        tsView.setText(dateStr);

        return convertView;
    }
}
