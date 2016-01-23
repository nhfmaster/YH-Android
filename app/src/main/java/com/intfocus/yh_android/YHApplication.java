package com.intfocus.yh_android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.intfocus.yh_android.screen_lock.ConfirmPassCodeActivity;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.crash.PgyCrashManager;

import org.OpenUDID.OpenUDID_manager;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lijunjie on 16/1/15.
 */
public class YHApplication extends Application implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        /*
         *  蒲公英平台，收集闪退日志
         */
        PgyCrashManager.register(this);

        /*
         *  初始化OpenUDID, 设备唯一化
         */
        OpenUDID_manager.sync(getApplicationContext());

        /*
         *  解压表态资源
         *  loading.zip, e433278b2f0835eaaaeb951cf9dfa363
         *  assets.zip, 490ecad478805d9455853865f4b53622
         */
        checkAssets("loading");
        checkAssets("assets");

          /*
         *  基本目录结构
         */
        File cachedFile = new File(String.format("%s/%s", FileUtil.basePath(), URLs.CACHED_DIRNAME));
        if(!cachedFile.exists()) {
            cachedFile.mkdirs();
        }

        /*
         *  是否启用锁屏
         */
        if (FileUtil.checkIsLocked()) {
            Log.i("screen_lock", "lock it");

            Intent intent = new Intent(this, ConfirmPassCodeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        } else {
            Log.i("screen_lock", "not lock");
        }

        registerActivityLifecycleCallbacks(this);


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

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i("YHApplication", "onActivityStopped - " + activity.getClass());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.i("YHApplication", "onActivityDestroyed");
    }

    private void checkAssets(String fileName) {
        try {
            String zipName = String.format("%s.zip", fileName);
            InputStream zipStream = getApplicationContext().getAssets().open(zipName);
            String MD5String = FileUtil.MD5(zipStream);
            String keyName = String.format("%s_md5", fileName);

            String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
            boolean isShouldUnZip = true;
            JSONObject userJSON = new JSONObject();
            if((new File(userConfigPath)).exists()) {
                userJSON = FileUtil.readConfigFile(userConfigPath);
                if(userJSON.has(keyName) && userJSON.getString(keyName).compareTo(MD5String) == 0) {
                    isShouldUnZip = false;
                }
            }

            if(isShouldUnZip) {
                File file = new File(String.format("%s/%s", FileUtil.sharedPath(), fileName));
                if(file.exists()) {
                    Log.i("deleteDirectory", file.getAbsolutePath());
                    FileUtils.deleteDirectory(file);
                }

                zipStream = getApplicationContext().getAssets().open(zipName);
                FileUtil.unZip(zipStream, FileUtil.sharedPath(), true);
                Log.i("unZip", String.format("%s, %s", zipName, MD5String));

                userJSON.put(keyName, MD5String);
                FileUtil.writeFile(userConfigPath, userJSON.toString());
            }

            zipStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
