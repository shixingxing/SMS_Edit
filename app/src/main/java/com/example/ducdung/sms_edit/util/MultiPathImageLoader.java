package com.example.ducdung.sms_edit.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.example.ducdung.sms_edit.R;
import com.example.ducdung.sms_edit.constant.CommonConstant;

public class MultiPathImageLoader {
    static final String PREFIX_DRAWABLE = "drawable://";
    static final String PREFIX_DB = "file://";
    static final String PREFIX_CP = "content://";
    static final String PREFIX_WEB = "http";

    static final String tag = MultiPathImageLoader.class.getSimpleName();
    static Drawable fallbackDrawable;

    public static void loadImage(final ImageView imageView, String imagePath) {
        Context context = imageView.getContext();
        Resources res = context.getResources();

        // load default picture if empty
        if (TextUtils.isEmpty(imagePath)) {
            imageView.setImageResource(R.drawable.default_head);
        } else {
            if (imagePath.startsWith(PREFIX_DRAWABLE)) {
                int drawId = res.getIdentifier(imagePath.split("//")[1], "drawable", CommonConstant.APP_NS);
                imageView.setImageDrawable(res.getDrawable(drawId));
            } else if (imagePath.startsWith(PREFIX_DB)) {

            }
            // from content provider, e.g., content://com.android.contacts/contacts/6/photo
            else if (imagePath.startsWith(PREFIX_CP)) {
                imageView.setImageURI(Uri.parse(imagePath));
            } else if (imagePath.startsWith(PREFIX_WEB)) {
//                loadKeyExchageUrlImage(imageView, imagePath, roleId);
            }
        }
    }

    public static void loadDefaultImage(ImageView imageView) {
        imageView.setImageDrawable(fallbackDrawable);
    }
}
