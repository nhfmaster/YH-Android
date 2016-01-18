package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONObject;

import java.io.File;

import android.net.ConnectivityManager;
import android.content.Context;
import android.net.NetworkInfo;
import android.util.Log;


/**
 * Created by lijunjie on 16/1/14.
 */
public class BaseActivity extends Activity {

    protected WebView mWebView;
    protected JSONObject user;
    protected String urlString;
    protected String assetsPath;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
        if ((new File(userConfigPath)).exists()) {
            user = FileUtil.readConfigFile(userConfigPath);
            assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    public void longLog(String Tag, String str) {
        if (str.length() > 200) {
            Log.i(Tag, str.substring(0, 200));
            longLog(Tag, str.substring(200));
        } else
            Log.i(Tag, str);
    }

}
