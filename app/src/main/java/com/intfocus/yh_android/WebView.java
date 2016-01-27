package com.intfocus.yh_android;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;

/**
 * Created by wiky on 1/11/16.
 * android.webkit.WebView
 * com.handmark.pulltorefresh.library.PullToRefreshWebView
 */
public class WebView extends android.webkit.WebView {
    public WebView(Context context) {
        super(context);
    }

    public WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void initialize(PullToRefreshWebView pullToRefreshWebView) {
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });

        initIndicator(pullToRefreshWebView);
    }

    private void initIndicator(PullToRefreshWebView pullToRefreshWebView) {
        ILoadingLayout startLabels = pullToRefreshWebView
                .getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("请继续下拉...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在刷新...");// 刷新时
        startLabels.setReleaseLabel("放了我，我就刷新...");// 下来达到一定距离时，显示的提示

        ILoadingLayout endLabels = pullToRefreshWebView.getLoadingLayoutProxy(
                false, true);
        endLabels.setPullLabel("请继续下拉");// 刚下拉时，显示的提示
        endLabels.setRefreshingLabel("正在刷新");// 刷新时
        endLabels.setReleaseLabel("放了我，我就刷新");// 下来达到一定距离时，显示的提示
    }
}
