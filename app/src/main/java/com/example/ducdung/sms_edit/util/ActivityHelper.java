package com.example.ducdung.sms_edit.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

/**
 * helper class wrapped with common activity operations
 *
 * @Author yangw
 * @Date 18/7/15 11:01 AM.
 */
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

public class ActivityHelper {
    private static final String tag = ActivityHelper.class.getSimpleName();

    public static void startAndFinish(Activity activity, Class<?> clazz, boolean finish) {
        start(activity, clazz);
        if (finish) {
            activity.finish();
        }
    }

    public static void start(Activity activity, Class<?> clazz) {
        activity.startActivity(new Intent(activity, clazz));
    }

    public static void startWithBundle(Activity activity, Class<?> clazz, Bundle bundle, boolean finish) {
        Intent intent = new Intent(activity, clazz);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        if (finish) {
            activity.finish();
        }
    }

    public static void startIntentActivityAndFinish(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.finish();
    }

    protected static void delayFinish(Activity activity, long delay) {
        final Activity activity2 = activity;
        new CountDownTimer(delay, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                activity2.finish();
            }
        }.start();
    }

    @TargetApi(19)
    public static void startChangeSmsDiagActivity(Activity activity, String packageToChange) {
        Log.i(tag, "change to " + packageToChange);
        Intent intent = new Intent("android.provider.Telephony.ACTION_CHANGE_DEFAULT");
        intent.putExtra("package", packageToChange);
        if (activity != null) {
            activity.startActivity(intent);
        }
    }
}