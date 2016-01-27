package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.URLs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends BaseActivity {

    private TextView mTitle;

    private String bannerName;
    private int objectID;
    private int objectType;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        findViewById(R.id.back).setOnClickListener(mOnBackListener);
        findViewById(R.id.back_text).setOnClickListener(mOnBackListener);

        mTitle = (TextView) findViewById(R.id.title);

        pullToRefreshWebView = (PullToRefreshWebView) findViewById(R.id.webview);
        initRefreshWebView();
        setPullToRefreshWebView(true);

        mWebView.requestFocus();
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");
        mWebView.loadUrl(urlStringForLoading);

        Intent intent = getIntent();
        bannerName = intent.getStringExtra("bannerName");
        objectID   = intent.getIntExtra("objectID", -1);
        objectType = intent.getIntExtra("objectType", -1);

        mTitle.setText(bannerName);

        String urlPath = String.format(URLs.COMMENT_PATH, objectID, objectType);
        urlString = String.format("%s%s", URLs.HOST, urlPath);

        new Thread(mRunnableForDetecting).start();


        List<ImageView> colorViews = new ArrayList<ImageView>();
        colorViews.add((ImageView) findViewById(R.id.colorView0));
        colorViews.add((ImageView) findViewById(R.id.colorView1));
        colorViews.add((ImageView) findViewById(R.id.colorView2));
        colorViews.add((ImageView) findViewById(R.id.colorView3));
        colorViews.add((ImageView) findViewById(R.id.colorView4));
        initColorView(colorViews);
    }

    private View.OnClickListener mOnBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CommentActivity.this.onBackPressed();
        }
    };

    private class JavaScriptInterface extends JavaScriptBase  {
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


                        new Thread(mRunnableForDetecting).start();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
