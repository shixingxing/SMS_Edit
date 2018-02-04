package com.example.ducdung.sms_edit.util;

/**
 * Created by Duc Dung on 1/21/2018.
 */



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.ducdung.sms_edit.constant.CommonConstant;
import com.example.ducdung.sms_edit.constant.UriConstant;

public class SmsUtil {
    private static final String tag = "SmsUtil";
    private static final Uri uri = Uri.parse("content://sms");
    private Context mContext;

    public SmsUtil(Context context) {
        this.mContext = context;
    }

    public void createSms(String addr, String text, long time, int protocol, int read, int type) {
        ContentValues cv = new ContentValues();
        cv.put(CommonConstant.BODY, text);
        cv.put(CommonConstant.ADDR, addr);
        cv.put("read", Integer.valueOf(read));
        cv.put(CommonConstant.TYPE, Integer.valueOf(type));
        cv.put(CommonConstant.DATE, Long.valueOf(time));
        this.mContext.getContentResolver().insert(uri, cv);
        Log.i(tag, "created new sms");
        ContentValues cv2 = new ContentValues();
        cv2.put(CommonConstant.BODY, "test");
        cv2.put(CommonConstant.ADDR, addr);
        cv2.put("read", Integer.valueOf(read));
        cv2.put(CommonConstant.TYPE, Integer.valueOf(type));
        cv2.put(CommonConstant.DATE, Long.valueOf(time));
        this.mContext.getContentResolver().delete(this.mContext.getContentResolver().insert(uri, cv2),
                null, null);
        Log.i(tag, "refreshed list");
    }

    public String getThreadId(String dest) {
        Cursor c = this.mContext.getContentResolver().query(Uri.parse(UriConstant.THREAD_URI),
                new String[]{"thread_id"}, "address = ?", new String[]{dest}, null);
        if (c.getCount() <= 0) {
            return null;
        }
        c.moveToFirst();
        return c.getString(0);
    }
}
