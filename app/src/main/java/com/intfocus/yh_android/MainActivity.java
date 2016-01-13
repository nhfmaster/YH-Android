package com.intfocus.yh_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
        user = FileUtil.readConfigFile(userConfigPath);
        assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.initialize();

        mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath()));


        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                MainActivity.this.startActivity(intent);
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

        try {
            mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath()));

            String urlPath = String.format(URLs.KPI_PATH, user.getString("role_id"), user.getString("group_id"));
            urlString = String.format("%s%s", URLs.HOST, urlPath);

            new Thread(runnable).start();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

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
                        urlPath = String.format(URLs.KPI_PATH, user.getString("role_id"), user.getString("group_id"));
                        break;
                    case R.id.tab_analysis:
                        urlPath = String.format(URLs.ANALYSE_PATH, user.getString("role_id"));
                        break;
                    case R.id.tab_app:
                        urlPath = String.format(URLs.APPLICATION_PATH, user.getString("role_id"));
                        break;
                    case R.id.tab_message:
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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            String htmlName = HttpUtil.UrlToFileName(urlString);
            String htmlPath = String.format("%s/%s", assetsPath, htmlName);
            Log.i("htmlPath",htmlPath);
            Log.i("HTML", FileUtil.readFile(htmlPath));
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

        ;
    };

}
