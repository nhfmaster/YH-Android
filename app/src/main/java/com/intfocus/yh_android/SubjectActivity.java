package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.TextView;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;
import com.intfocus.yh_android.util.ApiHelper;

import org.json.JSONObject;
import static java.lang.String.*;
import android.util.Log;

import android.widget.Toast;

import java.util.Map;

public class SubjectActivity extends Activity {

    private TextView mTitle;
    private ImageView mComment;
    private WebView mWebView;
    private JSONObject user;
    private String assetsPath;

    private String urlString;
    private Boolean isInnerLink;
    private String reportID;

    private String bannerName;
    private int objectID;
    private int objectType;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        findViewById(R.id.back).setOnClickListener(mOnBackListener);
        findViewById(R.id.back_text).setOnClickListener(mOnBackListener);

        mTitle   = (TextView) findViewById(R.id.title);
        mComment = (ImageView) findViewById(R.id.comment);
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

        try {
            Intent intent     = getIntent();
            String link = intent.getStringExtra("link");

            bannerName  = intent.getStringExtra("bannerName");
            objectID    = intent.getIntExtra("objectID", -1);
            objectType  = intent.getIntExtra("objectType", -1);
            isInnerLink = !(link.startsWith("http://") || link.startsWith("https://"));
            urlString   = link;

            if(isInnerLink) {
                String urlPath = format(link.replace("%@", "%d"), user.getInt("group_id"));
                urlString      = String.format("%s%s", URLs.HOST, urlPath);
                reportID       = TextUtils.split(link, "/")[3];
            }

            mTitle.setText(bannerName);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath()));
        if(isInnerLink) {
            new Thread(runnable).start();
        }
        else {
            try {
                /*
                 * 外部链接传参: userid, timestamp
                 */
                String appendParams = String.format("?userid=%d&timestamp=%s", user.getInt("user_id"), URLs.TimeStamp);

                if(urlString.indexOf("?") == -1) {
                    urlString = String.format("%s%s", urlString, appendParams);
                }
                else {
                    urlString = urlString.replace("?", appendParams);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("OutLink", urlString);
            mWebView.loadUrl(urlString);
        }

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(SubjectActivity.this, ObjectActivity.class);
            intent.putExtra("bannerName", bannerName);
            intent.putExtra("objectID", objectID);
            intent.putExtra("objectType", objectType);

            SubjectActivity.this.startActivity(intent);
            }
        });
    }

    private View.OnClickListener mOnBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SubjectActivity.this.onBackPressed();
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

                ApiHelper.reportData(assetsPath, String.format("%d", user.getInt("group_id")), reportID);

                mHandler.obtainMessage().sendToTarget();
            }
            else {
                Toast.makeText(SubjectActivity.this, "访问服务器失败", Toast.LENGTH_LONG).show();
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
        public void storeTabIndex(final String pageName, final int tabIndex) {
        }

        @JavascriptInterface
        public int restoreTabIndex(final String pageName) {
            return 0;
        }
    }
}
