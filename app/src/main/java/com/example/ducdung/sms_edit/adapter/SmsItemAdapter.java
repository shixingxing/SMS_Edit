package com.example.ducdung.sms_edit.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.ducdung.sms_edit.R;
import com.example.ducdung.sms_edit.constant.CommonConstant;
import com.example.ducdung.sms_edit.model.SmsMessage;
import com.example.ducdung.sms_edit.util.MultiPathImageLoader;
import com.example.ducdung.sms_edit.util.TelephoneUtil;

import java.util.List;

/**
 * @Author Dung Pham
 *
 */
public class SmsItemAdapter extends BaseAdapter {
    Context context;
    List<SmsMessage> messages;
    String otherPhoto;
    String selfPhoto;

    public SmsItemAdapter(Context context, List<SmsMessage> messages) {
        this.context = context;
        this.messages = messages;

        selfPhoto = TelephoneUtil.getOwnPhoto(context);
    }

    public void setMessages(List<SmsMessage> messages) {
        this.messages = messages;
    }

    public void setOtherPhoto(String otherPhoto) {
        this.otherPhoto = otherPhoto;
    }

    public void setSelfPhoto(String selfPhoto) {
        this.selfPhoto = selfPhoto;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).msgId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SmsMessage message = (SmsMessage) getItem(position);

        //if (convertView == null) {
        if (message.type == 1) {
            convertView = View.inflate(context, R.layout.msg_item, null);
        } else {
            convertView = View.inflate(context, R.layout.msg_item_right, null);
        }
        //}

        TextView bodyView = (TextView) convertView.findViewById(R.id.content);
        TextView tsView = (TextView) convertView.findViewById(R.id.timestamp);

        MultiPathImageLoader.loadImage((ImageView) convertView.findViewById(R.id.avatar),
                message.type == 1 ? otherPhoto : selfPhoto );
        bodyView.setText(message.msg);
        String dateStr = CommonConstant.dateFormat.format(message.timestamp);
        tsView.setText(dateStr);

        return convertView;
    }
}
