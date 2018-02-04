package com.example.ducdung.sms_edit.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class DialogBuilder {

    public static void showAlert(final Activity activity, final String title, final String msg, final boolean cancelAble) {
        final Resources res = activity.getResources();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                TextView textView = new TextView(activity);
                textView.setPadding(72, 72, 72, 72);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setClickable(true);
                textView.setText(Html.fromHtml(msg));

                builder.setTitle(title).setView(textView).setCancelable(cancelAble)
                        .setPositiveButton(res.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
            }
        });
    }
}
