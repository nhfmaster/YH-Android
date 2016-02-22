package com.intfocus.yh_android;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import com.intfocus.yh_android.screen_lock.ConfirmPassCodeActivity;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.crash.PgyCrashManager;
import com.squareup.leakcanary.LeakCanary;
import org.OpenUDID.OpenUDID_manager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lijunjie on 16/1/15.
 */
public class YHApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private Activity currentActivity;
    private Context mContext;

    /*
     *  手机待机再激活时发送开屏广播
     */
    BroadcastReceiver broadcastScreenOnAndOff = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && // 开屏状态
            BaseActivity.mActivities.size() > 0 && // 应用活动Activity数量大于零
            currentActivity != null && !currentActivity.getClass().toString().contains("ConfirmPassCodeActivity") && // 当前活动的Activity非解锁界面
            FileUtil.checkIsLocked(mContext)) { // 应用处于登录状态，并且开启了密码锁

            Intent i = new Intent(getApplicationContext(), ConfirmPassCodeActivity.class);
            i.putExtra("is_from_login", false);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = YHApplication.this;
        String sharedPath = FileUtil.basePath(mContext);

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

        /**
         *  静态文件放在共享文件夹内,以便与服务器端检测、更新
         *  刚升级过时，就不必须再更新，浪费用户流量
         */
        String assetsFileName = "assets.zip";
        String assetsZipPath = String.format("%s/%s", sharedPath, assetsFileName);
        if(!(new File(assetsZipPath)).exists()) {
            try {
                InputStream zipStream = mContext.getApplicationContext().getAssets().open(assetsFileName);
                FileOutputStream fos = new FileOutputStream(assetsZipPath);
                byte[] b = new byte[1024];
                while((zipStream.read(b)) != -1){
                    fos.write(b);
                }
                zipStream.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
         *  基本目录结构
         */
        File cachedFile = new File(String.format("%s/%s", sharedPath, URLs.CACHED_DIRNAME));
        if(!cachedFile.exists()) {
            cachedFile.mkdirs();
        }

        registerActivityLifecycleCallbacks(this);

        /*
         *  手机待机再激活时发送开屏广播
         */
        registerReceiver(broadcastScreenOnAndOff, new IntentFilter(Intent.ACTION_SCREEN_ON));
        //registerReceiver(broadcastScreenOnAndOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        /*
         *  监测内存泄漏
         */
        LeakCanary.install(this);
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
