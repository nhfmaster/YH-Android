package com.intfocus.yh_android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends ActionBarActivity {

    private WebView mWebView;
    private TabView mTabKPI;
    private TabView mTabAnalysis;
    private TabView mTabAPP;
    private TabView mTabMessage;
    private TabView mCurrentTab;
    private View.OnClickListener mTabChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mCurrentTab) {
                return;
            }
            mCurrentTab.setActive(false);
            mCurrentTab = (TabView) v;
            mCurrentTab.setActive(true);
            int id = v.getId();
            if (id == R.id.tab_kpi) {
                switchToKPI();
            } else if (id == R.id.tab_analysis) {
                switchToAnalysis();
            } else if (id == R.id.tab_app) {
                switchToAPP();
            } else if (id == R.id.tab_message) {
                switchToMessage();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });

        initTab();
    }

    private void initTab() {
        mTabKPI = (TabView) findViewById(R.id.tab_kpi);
        mTabAnalysis = (TabView) findViewById(R.id.tab_analysis);
        mTabAPP = (TabView) findViewById(R.id.tab_app);
        mTabMessage = (TabView) findViewById(R.id.tab_message);

        mTabKPI.setOnClickListener(mTabChangeListener);
        mTabAnalysis.setOnClickListener(mTabChangeListener);
        mTabAPP.setOnClickListener(mTabChangeListener);
        mTabMessage.setOnClickListener(mTabChangeListener);

        mCurrentTab = mTabKPI;
        mCurrentTab.setActive(true);
        switchToKPI();
    }

    private void switchToKPI() {
        /* 跳转到仪表盘界面 */
        mWebView.loadUrl("http://www.baidu.com");
    }

    private void switchToAnalysis() {
        /* 跳转到分析界面 */
        mWebView.loadUrl("http://www.163.com");
    }

    private void switchToAPP() {
        /* 跳转到应用界面 */
    }

    private void switchToMessage() {
        /* 跳转到消息界面 */
    }

}
