package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.net.ConnectivityManager;
import android.content.Context;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by lijunjie on 16/1/14.
 */
public class BaseActivity extends Activity {

    protected WebView mWebView;
    protected JSONObject user;
    protected String urlString;
    protected String assetsPath;
    protected String relativeAssetsPath;
    protected String urlStringForDetecting;
    protected String urlStringForLoading;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);

        assetsPath = FileUtil.sharedPath();
        urlStringForDetecting = URLs.HOST;
        relativeAssetsPath = "assets";
        urlStringForLoading = String.format("file:///%s/loading/login.html", FileUtil.sharedPath());

        if ((new File(userConfigPath)).exists()) {
            try {
                user = FileUtil.readConfigFile(userConfigPath);
                if (user.getBoolean("is_login")) {
                    assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);
                    String urlPath = String.format(URLs.API_DEVICE_STATE_PATH, user.getInt("user_device_id"));
                    urlStringForDetecting = String.format("%s%s", URLs.HOST, urlPath);
                    relativeAssetsPath = "../../Shared/assets";
                    urlStringForLoading = String.format("file:///%s/loading/loading.html", FileUtil.sharedPath());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    Runnable mRunnableForDetecting = new Runnable() {
        @Override
        public void run() {
            Map<String, String> response = HttpUtil.httpGet(urlStringForDetecting, new HashMap<String, String>());
            int statusCode = Integer.parseInt(response.get("code").toString());
            if(statusCode == 200) {
                Log.i("StatusCode", response.get("body").toString());
                try {
                    JSONObject json = new JSONObject(response.get("body").toString());
                    statusCode = json.getBoolean("device_state") ? 200 : 401;
                } catch(JSONException e) {
                    //e.printStackTrace();
                }
            }
            Log.i("Detecting", response.get("code").toString());

            Message message = mHandlerForDetecting.obtainMessage();
            message.what = statusCode;
            mHandlerForDetecting.sendMessage(message);
        }
    };

    protected Handler mHandlerForDetecting = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 200:
                    new Thread(mRunnableWithAPI).start();
                    break;
                case 400:
                    showDialogForWithoutNetwork();
                    break;
                case 401:
                    showDialogForDeviceForbided();
                    break;
                default:
                    Log.i("UnkownCode", urlStringForDetecting);
                    Log.i("UnkownCode", String.format("%d", message.what));
                    break;
            }
        }
    };

    Runnable mRunnableWithAPI = new Runnable() {
        @Override
        public void run() {
            Log.i("httpGetWithHeader", String.format("url: %s, assets: %s, relativeAssets: %s", urlString, assetsPath, relativeAssetsPath));
            Map<String, String> response = ApiHelper.httpGetWithHeader(urlString, assetsPath, relativeAssetsPath);


            Message message = mHandlerWithAPI.obtainMessage();
            message.what = Integer.parseInt(response.get("code").toString());
            message.obj = response.get("path").toString();

            Log.i("mRunnableWithAPI", String.format("code: %s, path: %s", response.get("code").toString(), response.get("path").toString()));

            /*
                String[] codes = new String[]{"200", "304"};
                if (Arrays.asList(codes).contains(response.get("code").toString())) {
                    message.obj = response.get("path").toString();
                }
            */

            mHandlerWithAPI.sendMessage(message);
        }
    };

    protected Handler mHandlerWithAPI = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 200:
                case 304:
                    String localHtmlPath = String.format("file:///%s", (String) message.obj);
                    Log.i("localHtmlPath", localHtmlPath);
                    mWebView.loadUrl(localHtmlPath);
                    break;
                default:
                    String msg = String.format("访问服务器失败（%d)", message.what);
                    Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void showDialogForWithoutNetwork() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle("温馨提示");
        alertDialog.setMessage("网络环境不稳定");

        alertDialog.setPositiveButton(
                "刷新",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(mRunnableForDetecting).start();
                    }
                }
        );
        alertDialog.setNegativeButton(
                "先这样",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        alertDialog.show();
    }


    public void showDialogForDeviceForbided() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle("温馨提示");
        alertDialog.setMessage("您被禁止在该设备使用本应用");

        alertDialog.setNegativeButton(
                "知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(mRunnableForDetecting).start();
                        dialog.dismiss();
                    }
                }
        );
        alertDialog.show();
    }

    public void longLog(String Tag, String str) {
        if (str.length() > 200) {
            Log.i(Tag, str.substring(0, 200));
            longLog(Tag, str.substring(200));
        } else
            Log.i(Tag, str);
    }


    protected void modifiedUserConfig(JSONObject configJSON) {
        try {
            String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
            JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);

            userJSON = ApiHelper.merge(userJSON, configJSON);
            FileUtil.writeFile(userConfigPath, userJSON.toString());

            String settingsConfigPath = FileUtil.dirPath(URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
            FileUtil.writeFile(settingsConfigPath, userJSON.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected class JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void refreshBrowser() {
            new Thread(mRunnableForDetecting).start();
        }
    }
}
