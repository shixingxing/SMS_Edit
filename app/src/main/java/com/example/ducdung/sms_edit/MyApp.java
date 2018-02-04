package com.example.ducdung.sms_edit;

import android.app.Application;

/**
 * Created by Duc Dung on 1/18/2018.
 */

public class MyApp extends Application {
    public static final String CRASHLYTICS_KEY_CRASHES = "are_crashes_enabled";
    private static MyApp singleton;

    public MyApp getInstance() {
        return singleton;
    }

    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
