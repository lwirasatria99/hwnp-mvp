package com.elabram.lm.wmshwnp;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics.Builder().core(
                new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        ).build());
    }
}
