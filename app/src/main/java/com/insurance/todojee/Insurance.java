package com.insurance.todojee;

import android.app.Application;
import android.content.Context;

public class Insurance extends Application {

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }


}
