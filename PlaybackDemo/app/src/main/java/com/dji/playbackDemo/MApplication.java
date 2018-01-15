package com.dji.playbackDemo;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private PlaybackDemoApplication playbackDemoApplication;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);

        Helper.install(MApplication.this);
        if (playbackDemoApplication == null) {
            playbackDemoApplication = new PlaybackDemoApplication();
            playbackDemoApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        playbackDemoApplication.onCreate();
    }
}