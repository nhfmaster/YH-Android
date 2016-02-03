package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;

import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private TabView mTabKPI;
    private TabView mTabAnalysis;
    private TabView mTabAPP;
    private TabView mTabMessage;
    private TabView mCurrentTab;
    private int objectType;


    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pullToRefreshWebView = (PullToRefreshWebView) findViewById(R.id.webview);
        initRefreshWebView();
        setPullToRefreshWebView(true);

        mWebView.requestFocus();
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");
        mWebView.loadUrl(urlStringForLoading);

        try {
            String urlPath = String.format(URLs.KPI_PATH, user.getString("role_id"), user.getString("group_id"));
            urlString = String.format("%s%s", URLs.HOST, urlPath);
            objectType = 1;

            new Thread(mRunnableForDetecting).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SettingActivity.class);
                mContext.startActivity(intent);

                /*
                 * 用户行为记录, 单独异常处理，不可影响用户体验
                 */
                try {
                    logParams = new JSONObject();
                    logParams.put("action", "点击/主页面/设置");
                    new Thread(mRunnableForLogger).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        initTab();

        List<ImageView> colorViews = new ArrayList<ImageView>();
        colorViews.add((ImageView) findViewById(R.id.colorView0));
        colorViews.add((ImageView) findViewById(R.id.colorView1));
        colorViews.add((ImageView) findViewById(R.id.colorView2));
        colorViews.add((ImageView) findViewById(R.id.colorView3));
        colorViews.add((ImageView) findViewById(R.id.colorView4));
        initColorView(colorViews);

        Intent intent = getIntent();
        if(intent.hasExtra("fromActivity") && intent.getStringExtra("fromActivity").contains("ConfirmPassCodeActivity")) {
            Log.i("FromActivity", intent.getStringExtra("fromActivity"));
            checkUpgrade(false);
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        Log.e("!!!!!", "RESTART!!!!!!!!!!");
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

            mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath(mContext)));

            try {
                String urlPath;
                switch (v.getId()) {
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
                new Thread(mRunnableForDetecting).start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "点击/主页面/标签栏");
                logParams.put("obj_type", objectType);
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private class JavaScriptInterface extends JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void pageLink(final String bannerName, final String link, final int objectID) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = String.format("%s\n%s\n%d", bannerName, link, objectID);
                    longLog("JSClick", message);

                    Intent intent = new Intent(MainActivity.this, SubjectActivity.class);
                    intent.putExtra("bannerName", bannerName);
                    intent.putExtra("link", link);
                    intent.putExtra("objectID", objectID);
                    intent.putExtra("objectType", objectType);
                    mContext.startActivity(intent);
                }
            });

            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "点击/主页面/浏览器");
                logParams.put("obj_id", objectID);
                logParams.put("obj_type", objectType);
                logParams.put("obj_title", bannerName);
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void storeTabIndex(final String pageName, final int tabIndex) {
            try {
                String filePath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

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
                String filePath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

                JSONObject config = new JSONObject();
                if ((new File(filePath).exists())) {
                    String fileContent = FileUtil.readFile(filePath);
                    config = new JSONObject(fileContent);
                }
                tabIndex = config.getInt(pageName);

            } catch (JSONException e) {
                // e.printStackTrace();
            }

            return tabIndex < 0 ? 0 : tabIndex;
        }

        @JavascriptInterface
        public void jsException(final String ex) {
            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "JS异常");
                logParams.put("obj_type", objectType);
                logParams.put("obj_title", String.format("主页面/%s", ex));
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
