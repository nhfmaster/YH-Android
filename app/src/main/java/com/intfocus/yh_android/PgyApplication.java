package com.intfocus.yh_android;

import com.pgyersdk.crash.PgyCrashManager;
import android.app.Application;

/**
 * Created by lijunjie on 16/1/15.
 */

public class PgyApplication extends Application {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        PgyCrashManager.register(this);
    }
}