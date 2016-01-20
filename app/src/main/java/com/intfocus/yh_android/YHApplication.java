package com.intfocus.yh_android;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.crash.PgyCrashManager;
import android.app.Application;
import android.util.Log;
import org.OpenUDID.OpenUDID_manager;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import org.apache.commons.io.FileUtils;

/**
 * Created by lijunjie on 16/1/15.
 */
public class YHApplication extends Application {

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
