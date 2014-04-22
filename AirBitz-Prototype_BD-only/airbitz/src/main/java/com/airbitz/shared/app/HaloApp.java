package com.airbitz.shared.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by dannyroa on 3/28/14.
 */
public class HaloApp extends Application {

    private static HaloApp instance;
    protected static Context context;

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return instance;
    }

    public static HaloApp getInstance() {
        return instance;
    }



}
