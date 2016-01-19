package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.TextView;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.intfocus.yh_android.util.ApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.String.*;

import android.util.Log;

import java.io.File;

import java.io.IOException;

public class SubjectActivity extends LockableActivity {

    private TextView mTitle;
    private ImageView mComment;

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

        mTitle = (TextView) findViewById(R.id.title);
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
        mWebView.loadUrl(urlStringForLoading);

        try {
            Intent intent = getIntent();
            String link = intent.getStringExtra("link");

            bannerName = intent.getStringExtra("bannerName");
            objectID = intent.getIntExtra("objectID", -1);
            objectType = intent.getIntExtra("objectType", -1);
            isInnerLink = !(link.startsWith("http://") || link.startsWith("https://"));
            urlString = link;

            if (isInnerLink) {
                String urlPath = format(link.replace("%@", "%d"), user.getInt("group_id"));
                urlString = String.format("%s%s", URLs.HOST, urlPath);
                reportID = TextUtils.split(link, "/")[3];
            }

            mTitle.setText(bannerName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isInnerLink) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ApiHelper.reportData(String.format("%d", user.getInt("group_id")), reportID);

                        new Thread(mRunnableForDetecting).start();
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            try {
                /*
                 * 外部链接传参: userid, timestamp
                 */
                String appendParams = String.format("?userid=%d&timestamp=%s", user.getInt("user_id"), URLs.TimeStamp);

                if (urlString.indexOf("?") == -1) {
                    urlString = String.format("%s%s", urlString, appendParams);
                } else {
                    urlString = urlString.replace("?", appendParams);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("OutLink", urlString);
            mWebView.loadUrl(urlString);
        }

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubjectActivity.this, CommentActivity.class);
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


    private class JavaScriptInterface {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void storeTabIndex(final String pageName, final int tabIndex) {
            try {
                String filePath = FileUtil.dirPath(URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

                JSONObject config = new JSONObject();
                if ((new File(filePath).exists())) {
                    String fileContent = FileUtil.readFile(filePath);
                    config = new JSONObject(fileContent);
                }
                config.put(pageName, tabIndex);

                FileUtil.writeFile(filePath, config.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public int restoreTabIndex(final String pageName) {
            int tabIndex = 0;
            try {
                String filePath = FileUtil.dirPath(URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

                JSONObject config = new JSONObject();
                if ((new File(filePath).exists())) {
                    String fileContent = FileUtil.readFile(filePath);
                    config = new JSONObject(fileContent);
                }
                tabIndex = config.getInt(pageName);
            } catch (JSONException e) {
                //e.printStackTrace();
            }

            return tabIndex < 0 ? 0 : tabIndex;
        }
    }
}
