package com.intfocus.yh_android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ObjectActivity extends Activity {

    private TextView mTitle;
    private WebView mWebView;

    private View.OnClickListener mOnBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ObjectActivity.this.onBackPressed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);

        findViewById(R.id.back).setOnClickListener(mOnBackListener);
        findViewById(R.id.back_text).setOnClickListener(mOnBackListener);

        mTitle = (TextView) findViewById(R.id.title);
        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.initialize();
        mWebView.loadUrl("http://www.163.com");
    }
}
