/**
 *
 */
package com.example.ducdung.sms_edit.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.app.ActivityCompat;

public class TelephoneUtil {
    private static final Uri uri = Uri.parse("content://sms");
    private static final String tag = TelephoneUtil.class.getSimpleName();
    private static String[] CONTACT_PROJ = new String[]{"_id", "display_name", "normalized_number", "photo_uri", "photo_thumb_uri"};
    private static String[] PROFILE_PROJ = new String[]{"_id", "display_name", "photo_thumb_uri"};
    private Context mContext;

    public TelephoneUtil(Context context) {
        this.mContext = context;
    }

    public static Cursor getContactProfile(Context context, String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }

        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        return cr.query(uri, CONTACT_PROJ, null, null, null);
    }

    public static Cursor getOwnProfile(Context context) {
        return getContactProfile(context, getOwnNumber(context));
    }

    public static String getOwnNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

        }
        return tm.getLine1Number();
    }

    public static String getOwnPhoto(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Profile.CONTENT_URI, PROFILE_PROJ, null, null, null);

        return CursorInspector.getContentByColumnIndex(cursor, 2);
    }

    public void createSms(String addr, String text, long time, int protocol, int read, int type) {
        ContentValues cv = new ContentValues();

        cv.put("body", text);
        cv.put("address", addr);
        cv.put("read", read);
        cv.put("type", type);
        cv.put("date", time);

        mContext.getContentResolver().insert(uri, cv);

        Log.i(tag, "created new sms");

        mContext.getContentResolver().delete(mContext.getContentResolver().insert(uri, cv), null, null);

        Log.i(tag, "refreshed list");
    }

    public String getThreadId(String dest) {
        Cursor c = mContext.getContentResolver().query(uri,
                new String[]{"thread_id"}, "address = ?",
                new String[]{dest}, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            return c.getString(0);
        }

        return null;
    }
}
