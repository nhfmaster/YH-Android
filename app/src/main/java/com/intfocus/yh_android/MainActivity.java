package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends Activity {

    private WebView mWebView;
    private TabView mTabKPI;
    private TabView mTabAnalysis;
    private TabView mTabAPP;
    private TabView mTabMessage;
    private TabView mCurrentTab;
    private JSONObject user;
    private String urlString;
    private String assetsPath;
    private int objectType;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
        user       = FileUtil.readConfigFile(userConfigPath);
        assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);

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

        try {
            String urlPath = String.format(URLs.KPI_PATH, user.getString("role_id"), user.getString("group_id"));
            //urlPath = "/mobile/test";
            urlString = String.format("%s%s", URLs.HOST, urlPath);
            Log.i("URL", urlString);


            objectType = 1;
            mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath()));
            new Thread(runnable).start();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        initTab();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @JavascriptInterface
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
    }

    @SuppressLint("SetJavaScriptEnabled")
    private View.OnClickListener mTabChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        if (v == mCurrentTab) {
            return;
        }
        mCurrentTab.setActive(false);
        mCurrentTab = (TabView) v;
        mCurrentTab.setActive(true);

        mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath()));

        try {
            String urlPath;
            switch(v.getId()) {
                case R.id.tab_kpi:
                    objectType = 1;
                    urlPath = String.format(URLs.KPI_PATH, user.getString("role_id"), user.getString("group_id"));
                    break;
                case R.id.tab_analysis:
                    objectType = 2;
                    urlPath = String.format(URLs.ANALYSE_PATH, user.getString("role_id"));
                    break;
                case R.id.tab_app:
                    objectType = 3;
                    urlPath = String.format(URLs.APPLICATION_PATH, user.getString("role_id"));
                    break;
                case R.id.tab_message:
                    objectType = 5;
                    urlPath = String.format(URLs.MESSAGE_PATH, user.getString("role_id"), user.getString("user_id"));
                    break;
                default:
                    urlPath = String.format(URLs.KPI_PATH, user.getString("role_id"), user.getString("group_id"));
                    break;
            }

            urlString = String.format("%s%s", URLs.HOST, urlPath);
            new Thread(runnable).start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
        String htmlName = HttpUtil.UrlToFileName(urlString);
        String htmlPath = String.format("%s/%s", assetsPath, htmlName);
        longLog("htmlPath",htmlPath);
        longLog("HTML", FileUtil.readFile(htmlPath));

        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");
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
                Toast.makeText(MainActivity.this, "访问服务器失败", Toast.LENGTH_LONG).show();
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
        public void pageLink(final String bannerName, final String link, final int objectID) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                String message =  String.format("%s\n%s\n%d", bannerName, link, objectID);
                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                longLog("JSClick", message);

                Intent intent = new Intent(MainActivity.this, SubjectActivity.class);
                intent.putExtra("bannerName", bannerName);
                intent.putExtra("link", link);
                intent.putExtra("objectID", objectID);
                intent.putExtra("objectType", objectType);
                MainActivity.this.startActivity(intent);
                }
            });
        }

        @JavascriptInterface
        public void storeTabIndex(final String pageName, final int tabIndex) {
        }

        @JavascriptInterface
        public int restoreTabIndex(final String pageName) {
            return 0;
        }
    }


    public static void longLog(String Tag, String str) {
        if(str.length() > 200) {
            Log.i(Tag, str.substring(0, 200));
            longLog(Tag, str.substring(200));
        }
        else {
            Log.i(Tag, str);
        }
    }
}
