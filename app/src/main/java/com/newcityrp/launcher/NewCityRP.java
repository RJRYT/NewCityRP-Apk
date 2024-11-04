package com.newcityrp.launcher;

import android.app.Application;

public class NewCityRP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Set GlobalExceptionHandler as the default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
    }
}