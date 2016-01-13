package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import android.content.Intent;

public class ObjectActivity extends Activity {

    private TextView mTitle;
    private WebView mWebView;
    private JSONObject user;
    private String assetsPath;
    private String urlString;

    private String bannerName;
    private int objectID;
    private int objectType;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);

        findViewById(R.id.back).setOnClickListener(mOnBackListener);
        findViewById(R.id.back_text).setOnClickListener(mOnBackListener);

        mTitle = (TextView) findViewById(R.id.title);
        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.initialize();
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");

        String userConfigPath = format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
        user       = FileUtil.readConfigFile(userConfigPath);
        assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);

        Intent intent = getIntent();
        bannerName = intent.getStringExtra("bannerName");
        objectID   = intent.getIntExtra("objectID", -1);
        objectType = intent.getIntExtra("objectType", -1);

        mTitle.setText(bannerName);

        String urlPath = String.format(URLs.COMMENT_PATH, objectID, objectType);
        urlString = String.format("%s%s", URLs.HOST, urlPath);


        mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath()));
        new Thread(runnable).start();
    }

    private View.OnClickListener mOnBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ObjectActivity.this.onBackPressed();
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            String htmlName = HttpUtil.UrlToFileName(urlString);
            String htmlPath = String.format("%s/%s", assetsPath, htmlName);

            mWebView.loadUrl(String.format("file:///" + htmlPath));
        }

    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
        try {
            Map<String, String> response = HttpUtil.httpGet(urlString);
            if (response.get("code").toString().compareTo("200") == 0) {
                String htmlName = HttpUtil.UrlToFileName(urlString);
                String htmlPath = String.format("%s/%s", assetsPath, htmlName);
                String htmlContent = response.get("body").toString();
                /*
                 *  /storage/emulated/0/Shared/{assets,loading}
                 *  /storage/emulated/0/user.plist
                 *  /storage/emulated/0/user-(user-id)/{config, HTML}
                 */
                htmlContent = htmlContent.replace("/javascripts/", "../../Shared/assets/javascripts/");
                htmlContent = htmlContent.replace("/stylesheets/", "../../Shared/assets/stylesheets/");
                htmlContent = htmlContent.replace("/images/", "../../Shared/assets/images/");
                FileUtil.writeFile(htmlPath, htmlContent);

                mHandler.obtainMessage().sendToTarget();
            }
            else {
                Toast.makeText(ObjectActivity.this, "访问服务器失败", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    };


    private class JavaScriptInterface {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void writeComment(final String content) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<String, String> params = new HashMap();
                        params.put("object_title", bannerName);
                        params.put("user_name", user.getString("user_name"));
                        params.put("content", content);
                        ApiHelper.writeComment(user.getInt("user_id"), objectType, objectID, params);

                        mHandler.obtainMessage();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
