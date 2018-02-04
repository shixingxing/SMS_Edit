package com.example.ducdung.sms_edit.util;

import android.database.Cursor;
import android.util.Log;


public class CursorInspector {
    static final String tag = CursorInspector.class.getSimpleName();

    public static void printFirstEntry(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int cols = cursor.getColumnCount();
            for (int i = 0; i < cols; i++) {
                Log.i(tag, i + " " + cursor.getColumnName(i) + " : " + cursor.getString(i));
            }
        }
    }

    public static void printColumns(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int cols = cursor.getColumnCount();
            for (int i = 0; i < cols; i++) {
                Log.i(tag, i + " " + cursor.getColumnName(i) + " : " + cursor.getString(i));
            }
            cursor.moveToFirst();
        }
    }

    public static void printContent(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int cols = cursor.getColumnCount();
            do {
                for (int i = 0; i < cols; i++) {
                    Log.i(tag, i + " " + cursor.getColumnName(i) + " : " + cursor.getString(i));
                }
            } while (cursor.moveToNext());
        }
    }

    public static String getContentByColumnIndex(Cursor cursor, int index) {
        try {
            cursor.moveToFirst();
            return cursor.getString(index);
        } catch (Exception e) {
            Log.w(tag, "cursor", e);
            return "";
        }
    }
}