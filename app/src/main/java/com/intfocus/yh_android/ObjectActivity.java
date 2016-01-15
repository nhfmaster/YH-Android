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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import android.content.Intent;
import android.util.Log;

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
        mWebView.requestFocus();
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
            switch(message.what) {
                case 200:
                case 304:
                    String htmlPath = (String)message.obj;
                    Log.i("FilePath", htmlPath);
                    mWebView.loadUrl(String.format("file:///" + htmlPath));
                    break;
                default:
                    Toast.makeText(ObjectActivity.this, "访问服务器失败", Toast.LENGTH_SHORT).show();;
                    break;
            }
        }

    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Map<String, String> response = ApiHelper.httpGetWithHeader(urlString, assetsPath, "../../Shared/assets");
            Message message = mHandler.obtainMessage();
            message.what =  Integer.parseInt(response.get("code").toString());

            String[] codes = new String[] {"200", "304"};
            if(Arrays.asList(codes).contains(response.get("code").toString())) {
                message.obj = response.get("path").toString();
            }
            mHandler.sendMessage(message);
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
                        Log.i("PARAMS", params.toString());
                        ApiHelper.writeComment(user.getInt("user_id"), objectType, objectID, params);


                        new Thread(runnable).start();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
