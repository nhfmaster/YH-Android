package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lijunjie on 16/1/14.
 */
public class BaseActivity extends Activity {

    protected PullToRefreshWebView pullToRefreshWebView;
    protected android.webkit.WebView mWebView;
    protected JSONObject user;
    protected int userID = 0;
    protected String urlString;
    protected String assetsPath;
    protected String relativeAssetsPath;
    protected String urlStringForDetecting;
    protected String urlStringForLoading;
    protected JSONObject logParams;
    protected static ArrayList<Activity> mActivities = new ArrayList<Activity>();

    protected Context mContext;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivities.add(this);
        finishLoginActivityWhenInMainAcitivty(this);

        mContext = BaseActivity.this;
        String sharedPath = FileUtil.sharedPath(mContext);
        assetsPath = sharedPath;
        urlStringForDetecting = URLs.HOST;
        relativeAssetsPath = "assets";
        urlStringForLoading = String.format("file:///%s/loading/login.html", sharedPath);

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
        if ((new File(userConfigPath)).exists()) {
            try {
                user = FileUtil.readConfigFile(userConfigPath);
                if (user.has("is_login") && user.getBoolean("is_login")) {
                    userID = user.getInt("user_id");
                    assetsPath = FileUtil.dirPath(mContext, URLs.HTML_DIRNAME);
                    String urlPath = String.format(URLs.API_DEVICE_STATE_PATH, user.getInt("user_device_id"));
                    urlStringForDetecting = String.format("%s%s", URLs.HOST, urlPath);
                    relativeAssetsPath = "../../Shared/assets";
                    urlStringForLoading = String.format("file:///%s/loading/loading.html", sharedPath);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivities.remove(this);
    }

    public static void finishAll() {
        for(Activity activity:mActivities) {
            activity.finish();
        }
    }

    private void finishLoginActivityWhenInMainAcitivty(Activity activity) {
        if(activity.getClass().toString().contains("MainActivity")) {
            for(Activity a:mActivities) {
                if(a.getClass().toString().contains("LoginActivity")) {
                    a.finish();
                    Log.i("finishLoginActivity", mActivities.toString());
                }
            }
        }
    }

    /*
     * ********************
     * WebView Setting
     * ********************
     */
    public android.webkit.WebView initRefreshWebView() {
        pullToRefreshWebView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        mWebView = pullToRefreshWebView.getRefreshableView();
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });

        initIndicator(pullToRefreshWebView);

        return mWebView;
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

    public void setPullToRefreshWebView(boolean isAllow) {
        if(!isAllow) {
            pullToRefreshWebView.setMode(PullToRefreshBase.Mode.DISABLED);
            return;
        }

        // 刷新监听事件
        pullToRefreshWebView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<android.webkit.WebView>() {
            @Override
            public void onRefresh(PullToRefreshBase<android.webkit.WebView> refreshView) {
                // 模拟加载任务
                new pullToRefreshTask().execute();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String label = simpleDateFormat.format(System.currentTimeMillis());
                // 显示最后更新的时间
                refreshView.getLoadingLayoutProxy()
                        .setLastUpdatedLabel(label);
            }
        });
    }


    protected class pullToRefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // 如果这个地方不使用线程休息的话，刷新就不会显示在那个 PullToRefreshListView 的 UpdatedLabel 上面

            /*
             *  下拉浏览器刷新时，删除响应头文件，相当于无缓存刷新
             */
            if(urlString != null && !urlString.isEmpty()) {
                String urlKey = urlString.indexOf("?") != -1 ? TextUtils.split(urlString, "?")[0] : urlString;
                ApiHelper.clearResponseHeader(urlKey, assetsPath);
            }
            new Thread(mRunnableForDetecting).start();



            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams.put("action", "刷新/浏览器");
                logParams.put("obj_title", urlString);
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Call onRefreshComplete when the list has been refreshed. 如果没有下面的函数那么刷新将不会停
            pullToRefreshWebView.onRefreshComplete();
        }
    }


    /*
     * ********************
     * WebView display UI
     * ********************
     */
    Runnable mRunnableForDetecting = new Runnable() {
        @Override
        public void run() {
            Map<String, String> response = HttpUtil.httpGet(urlStringForDetecting, new HashMap<String, String>());
            int statusCode = Integer.parseInt(response.get("code").toString());
            if(statusCode == 200) {
                Log.i("StatusCode", response.get("body").toString());
                try {
                    JSONObject json = new JSONObject(response.get("body").toString());
                    statusCode = json.getBoolean("device_state") ? 200 : 401;
                } catch(JSONException e) {
                    //e.printStackTrace();
                }
            }
            Log.i("Detecting", response.get("code").toString());

            Message message = mHandlerForDetecting.obtainMessage();
            message.what = statusCode;
            mHandlerForDetecting.sendMessage(message);
        }
    };

    protected Handler mHandlerForDetecting = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 200:
                    new Thread(mRunnableWithAPI).start();
                    break;
                case 400:
                    showDialogForWithoutNetwork();
                    break;
                case 401:
                    showDialogForDeviceForbided();
                    break;
                default:
                    Log.i("UnkownCode", urlStringForDetecting);
                    Log.i("UnkownCode", String.format("%d", message.what));
                    break;
            }
        }
    };

    Runnable mRunnableWithAPI = new Runnable() {
        @Override
        public void run() {
            Log.i("httpGetWithHeader", String.format("url: %s, assets: %s, relativeAssets: %s", urlString, assetsPath, relativeAssetsPath));
            Map<String, String> response = ApiHelper.httpGetWithHeader(urlString, assetsPath, relativeAssetsPath);


            Message message = mHandlerWithAPI.obtainMessage();
            message.what = Integer.parseInt(response.get("code").toString());
            message.obj = response.get("path").toString();

            Log.i("mRunnableWithAPI", String.format("code: %s, path: %s", response.get("code").toString(), response.get("path").toString()));

            /*
                String[] codes = new String[]{"200", "304"};
                if (Arrays.asList(codes).contains(response.get("code").toString())) {
                    message.obj = response.get("path").toString();
                }
            */

            mHandlerWithAPI.sendMessage(message);
        }
    };

    protected Handler mHandlerWithAPI = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 200:
                case 304:
                    String localHtmlPath = String.format("file:///%s", (String) message.obj);
                    Log.i("localHtmlPath", localHtmlPath);
                    mWebView.loadUrl(localHtmlPath);
                    break;
                default:
                    String msg = String.format("访问服务器失败（%d)", message.what);
                    Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Runnable mRunnableForLogger = new Runnable() {
        @Override
        public void run() {
            ApiHelper.actionLog(mContext, logParams);
        }
    };

    public void showDialogForWithoutNetwork() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle("温馨提示");
        alertDialog.setMessage("网络环境不稳定");

        alertDialog.setPositiveButton(
                "刷新",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(mRunnableForDetecting).start();
                    }
                }
        );
        alertDialog.setNegativeButton(
                "先这样",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        alertDialog.show();
    }


    public void showDialogForDeviceForbided() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle("温馨提示");
        alertDialog.setMessage("您被禁止在该设备使用本应用");

        alertDialog.setNegativeButton(
                "知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(mRunnableForDetecting).start();
                        dialog.dismiss();
                    }
                }
        );
        alertDialog.show();
    }

    public void initColorView(List<ImageView> colorViews) {
        String[] colors =  {"#ffffff", "#ffcd0a", "#fd9053", "#dd0929", "#016a43", "#9d203c", "#093db5", "#6a3906", "#192162", "#000000"};
        String userIDStr = String.format("%d", userID);
        int numDiff = colorViews.size() - userIDStr.length();
        numDiff = numDiff < 0 ? 0 : numDiff;

        for(int i=0; i < colorViews.size(); i++) {
            int colorIndex = 0;
            if(i >= numDiff) {
                colorIndex = Character.getNumericValue(userIDStr.charAt(i - numDiff));
            }
            colorViews.get(i).setBackgroundColor(Color.parseColor(colors[colorIndex]));
        }
    }

    public void longLog(String Tag, String str) {
        if (str.length() > 200) {
            Log.i(Tag, str.substring(0, 200));
            longLog(Tag, str.substring(200));
        } else {
            Log.i(Tag, str);
        }
    }


    protected void modifiedUserConfig(JSONObject configJSON) {
        try {
            String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
            JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);

            userJSON = ApiHelper.merge(userJSON, configJSON);
            FileUtil.writeFile(userConfigPath, userJSON.toString());

            String settingsConfigPath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
            FileUtil.writeFile(settingsConfigPath, userJSON.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * 检测版本更新
     * {"code":0,"message":"","data":{"lastBuild":"10","downloadURL":"","versionCode":"15","versionName":"0.1.5","appUrl":"http:\/\/www.pgyer.com\/yh-a","build":"10","releaseNote":"\u66f4\u65b0\u5230\u7248\u672c: 0.1.5(build10)"}}
     */
    protected View.OnClickListener mCheckUpgradeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkUpgrade(true);

            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "点击/设置页面/检测更新");
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected void checkUpgrade(final boolean isShowToast) {
        UpdateManagerListener updateManagerListener = new UpdateManagerListener() {

            @Override
            public void onUpdateAvailable(final String result) {
                Log.i("PGYER", result);

                String message = "服务器获取信息失败。";
                String versionCode = "-1", versionName = "-1";
                try {
                    JSONObject response = new JSONObject(result);
                    message = response.getString("message");
                    if(message.isEmpty()) {
                        JSONObject responseData = response.getJSONObject("data");
                        message = responseData.getString("releaseNote");
                        versionCode = responseData.getString("versionCode");
                        versionName = responseData.getString("versionName");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    message = e.getMessage();
                }

                // 偶数时为正式版本
                if(Integer.parseInt(versionCode) % 2 == 1) {
                    if(isShowToast) {
                        Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
                    }
                    return ;
                }


                // 将新版本信息封装到AppBean中
                final AppBean appBean = getAppBeanFromString(result);
                new AlertDialog.Builder(mContext)
                        .setTitle("版本更新")
                        .setMessage(message.isEmpty() ? "没有升级简介" : message)
                        .setPositiveButton(
                                "确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startDownloadTask(BaseActivity.this, appBean.getDownloadURL());
                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            }

            @Override
            public void onNoUpdateAvailable() {
                if(isShowToast) {
                    Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
                }
            }
        };

        PgyUpdateManager.register(BaseActivity.this, updateManagerListener);
    }


    protected class JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void refreshBrowser() {
            new Thread(mRunnableForDetecting).start();
        }

    }
}
