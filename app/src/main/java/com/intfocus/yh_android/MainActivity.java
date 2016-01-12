package com.intfocus.yh_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private WebView mWebView;
    private TabView mTabKPI;
    private TabView mTabAnalysis;
    private TabView mTabAPP;
    private TabView mTabMessage;
    private TabView mCurrentTab;

    private JSONObject user;
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

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
        user = FileUtil.readConfigFile(userConfigPath);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.initialize();

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
        switchToKPI();
    }

    private void switchToKPI() {
        /* 跳转到仪表盘界面 */
        try {
            String urlString = String.format(URLs.KPI_PATH, user.getString("role_id"), user.getString("group_id"));
            mWebView.loadUrl(String.format("%s%s", URLs.HOST, urlString));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void switchToAnalysis() {
        /* 跳转到分析界面 */
        try {
            String urlString = String.format(URLs.ANALYSE_PATH, user.getString("role_id"));
            mWebView.loadUrl(String.format("%s%s", URLs.HOST, urlString));
            /* 临时用的跳转接口 FIXME */
            Intent intent = new Intent(this, SubjectActivity.class);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void switchToAPP() {
        try {
            String urlString = String.format(URLs.APPLICATION_PATH, user.getString("role_id"));
            mWebView.loadUrl(String.format("%s%s", URLs.HOST, urlString));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void switchToMessage() {
        try {
            String urlString = String.format(URLs.MESSAGE_PATH, user.getString("role_id"), user.getString("user_id"));
            mWebView.loadUrl(String.format("%s%s", URLs.HOST, urlString));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
