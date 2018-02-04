package com.example.ducdung.sms_edit.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

public class BaseLoaderWrapper extends CursorLoader {
    private Context context;

    public BaseLoaderWrapper(Context context) {
        super(context);

        this.context = context;
    }

    public BaseLoaderWrapper(Context context, Uri uri, String[] projection,
                             String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);

        this.context = context;
    }

    @Override
    public Cursor loadInBackground() {
        try {
            return super.loadInBackground();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }
}
