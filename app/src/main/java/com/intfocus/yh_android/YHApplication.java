package com.intfocus.yh_android;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.intfocus.yh_android.screen_lock.ConfirmPassCodeActivity;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.crash.PgyCrashManager;

import org.OpenUDID.OpenUDID_manager;

import java.io.File;

/**
 * Created by lijunjie on 16/1/15.
 */
public class YHApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private Activity currentActivity;
    private Context mContext;

    //Create broadcast object
    BroadcastReceiver mybroadcast = new BroadcastReceiver() {

        //When Event is published, onReceive method is called
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.i("[BroadcastReceiver]", "MyReceiver");

            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                Log.i("[BroadcastReceiver]", "Screen ON");
                if(currentActivity != null && !currentActivity.getClass().toString().contains("ConfirmPassCodeActivity")) {
                    if(FileUtil.checkIsLocked(mContext)) {

                        Intent i = new Intent(getApplicationContext(), ConfirmPassCodeActivity.class);
                        i.putExtra("is_from_login", false);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    } else {
                        Log.i("[BroadcastReceiver]", "no setup screen lock function");
                    }

                } else {
                    Log.i("[BroadcastReceiver]", "already in ConfirmPassCodeActivity view");
                }
            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                Log.i("[BroadcastReceiver]", "Screen OFF");
            }

        }
    };

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        mContext = YHApplication.this;

        /*
         *  蒲公英平台，收集闪退日志
         */
        PgyCrashManager.register(this);

        /*
         *  初始化OpenUDID, 设备唯一化
         */
        OpenUDID_manager.sync(getApplicationContext());

        /*
         *  解压静态资源
         *  loading.zip, e433278b2f0835eaaaeb951cf9dfa363
         *  assets.zip, 490ecad478805d9455853865f4b53622
         */
        FileUtil.checkAssets(mContext, "loading");
        FileUtil.checkAssets(mContext, "assets");

          /*
         *  基本目录结构
         */
        File cachedFile = new File(String.format("%s/%s", FileUtil.basePath(mContext), URLs.CACHED_DIRNAME));
        if(!cachedFile.exists()) {
            cachedFile.mkdirs();
        }

        registerActivityLifecycleCallbacks(this);

        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        Log.i("SD", "---------------");
        // mounted
        Log.i("SD", Environment.getExternalStorageState());
        Log.i("SD", FileUtil.basePath(mContext));
        Log.i("SD", "---------------");
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(this);

        Log.i("YHApplication", "onTerminate");
        super.onTerminate();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i("YHApplication", "onActivityCreated - " + activity.getClass());

    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.i("YHApplication", "onActivityStarted - " + activity.getClass());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.i("YHApplication", "onActivityResumed - " + activity.getClass());

    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.i("YHApplication", "onActivityPaused - " + activity.getClass());

        /*
         * 进入待机状态，会优先触发puased， 然后stopped.
         * 如果用户使用app时，进入待机状态前的最后一个ctivit
         * 1. 如果用户已使用锁屏功能，则进入验证密码界面
         * 2. 如果未使用锁屏功能，则进入登录状态
         */
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i("YHApplication", "onActivityStopped - " + activity.getClass());
        /*
         * 进入待机状态，会优先触发puased， 然后stopped.
         * 如果用户使用app时，进入待机状态前的最后一个ctivit
         * 1. 如果用户已使用锁屏功能，则进入验证密码界面
         * 2. 如果未使用锁屏功能，则进入登录状态
         */
        currentActivity = activity;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.i("YHApplication", "onActivityDestroyed");
    }

}
