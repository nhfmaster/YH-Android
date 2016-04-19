package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.intfocus.yh_android.util.AppManager;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.TypedObject;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lijunjie on 16/1/14.
 */
public class BaseActivity extends Activity {

    PullToRefreshWebView pullToRefreshWebView;
    android.webkit.WebView mWebView;
    JSONObject user;
    int userID = 0;
    String urlString;
    String assetsPath;
    private String sharedPath;
    private String relativeAssetsPath;
    private String urlStringForDetecting;
    String urlStringForLoading;
    JSONObject logParams = new JSONObject();
    private ProgressDialog mProgressDialog;
//    final static ArrayList<Activity> mActivities = new ArrayList<>(3);

    Context mContext;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PushAgent.getInstance(this).onAppStart();

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        System.out.println("runningActivity:" + runningActivity);
//        mActivities.add(this);
        // for (Activity a : mActivities) {
        //    System.out.println("mActivityName: " + a.toString());
        // }

//         finishLoginActivityWhenInMainAcitivty(this);

        PushAgent.getInstance(this).onAppStart();

        PushAgent.getInstance(this).onAppStart();

        mContext = BaseActivity.this;
        sharedPath = FileUtil.sharedPath(mContext);
        assetsPath = sharedPath;
        urlStringForDetecting = URLs.HOST;
        relativeAssetsPath = "assets";
        urlStringForLoading = String.format("file:///%s/loading/loading.html", sharedPath);

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
        if ((new File(userConfigPath)).exists()) {
            try {
                user = FileUtil.readConfigFile(userConfigPath);
                if (user.has("is_login") && user.getBoolean("is_login")) {
                    userID = user.getInt("user_id");
                    assetsPath = FileUtil.dirPath(mContext, URLs.HTML_DIRNAME);
                    urlStringForDetecting = String.format(URLs.API_DEVICE_STATE_PATH, URLs.HOST, user.getInt("user_device_id"));
                    relativeAssetsPath = "../../Shared/assets";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        RefWatcher refWatcher = YHApplication.getRefWatcher(mContext);
        refWatcher.watch(this);
    }

    @Override
    public void onDestroy() {
//        mActivities.remove(this);
        System.out.println("activityDestroy: " + this.toString());
        fixInputMethodManager();
        super.onDestroy();
    }

    private void fixInputMethodManager() {
        final Object imm = getSystemService(Context.INPUT_METHOD_SERVICE);

        final TypedObject windowToken
                = new TypedObject(getWindow().getDecorView().getWindowToken(), IBinder.class);

        windowToken.invokeMethodExceptionSafe(imm, "windowDismissed", windowToken);

        final TypedObject view
                = new TypedObject(null, View.class);

        view.invokeMethodExceptionSafe(imm, "startGettingWindowFocus", view);
    }

//    public static void finishAll() {
//        for (Activity activity : mActivities) {
//            activity.finish();
//        }
//        mActivities.clear();
//    }

//    void finishLoginActivityWhenInMainAcitivty(Activity activity) {
//        if (activity.getClass().toString().contains("MainActivity")) {
//            for (Activity a : mActivities) {
//                if (a.getClass().toString().contains("LoginActivity")) {
//                    a.finish();
//                    Log.i("finishLoginActivity", mActivities.toString());
//                }
//            }
//        }
//    }

    /*
     * ********************
     * WebView Setting
     * ********************
     */
    android.webkit.WebView initRefreshWebView() {
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

    void setPullToRefreshWebView(boolean isAllow) {
        if (!isAllow) {
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


    private class pullToRefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // 如果这个地方不使用线程休息的话，刷新就不会显示在那个 PullToRefreshListView 的 UpdatedLabel 上面

            /*
             *  下拉浏览器刷新时，删除响应头文件，相当于无缓存刷新
             */
            if (urlString != null && !urlString.isEmpty()) {
                String urlKey = urlString.contains("?") ? TextUtils.split(urlString, "?")[0] : urlString;
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
    final Runnable mRunnableForDetecting = new Runnable() {
        @Override
        public void run() {
            Map<String, String> response = HttpUtil.httpGet(urlStringForDetecting, new HashMap<String, String>());
            int statusCode = Integer.parseInt(response.get("code"));
            if (statusCode == 200 && !urlStringForDetecting.equals(URLs.HOST)) {
                Log.i("StatusCode", response.get("body"));
                try {
                    JSONObject json = new JSONObject(response.get("body"));
                    statusCode = json.getBoolean("device_state") ? 200 : 401;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.i("Detecting", response.get("code"));

            Message message = mHandlerForDetecting.obtainMessage();
            message.what = statusCode;
            mHandlerForDetecting.sendMessage(message);
        }
    };

    private final Handler mHandlerForDetecting = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 200:
                case 304:
                    new Thread(mRunnableWithAPI).start();
                    break;
                case 400:
                case 408:
                    showWebViewForWithoutNetwork();
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

    private final Runnable mRunnableWithAPI = new Runnable() {
        @Override
        public void run() {
            Log.i("httpGetWithHeader", String.format("url: %s, assets: %s, relativeAssets: %s", urlString, assetsPath, relativeAssetsPath));
            Map<String, String> response = ApiHelper.httpGetWithHeader(urlString, assetsPath, relativeAssetsPath);

            Message message = mHandlerWithAPI.obtainMessage();
            message.what = Integer.parseInt(response.get("code"));
            message.obj = response.get("path");

            Log.i("mRunnableWithAPI", String.format("code: %s, path: %s", response.get("code"), response.get("path")));

            mHandlerWithAPI.sendMessage(message);
        }
    };

    private final Handler mHandlerWithAPI = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 200:
                case 304:
                    String localHtmlPath = String.format("file:///%s", (String) message.obj);
                    Log.i("localHtmlPath", localHtmlPath);
                    mWebView.loadUrl(localHtmlPath);
                    break;
                case 400:
                case 408:
                    showWebViewForWithoutNetwork();
                    break;
                default:
                    String msg = String.format("访问服务器失败（%d)", message.what);
                    Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    final Runnable mRunnableForLogger = new Runnable() {
        @Override
        public void run() {
            try {
                if (!logParams.getString("action").equals("登录") && !logParams.getString("action").equals("解屏"))
                    return;

                ApiHelper.actionLog(mContext, logParams);
                System.out.println("logParams: " + logParams.get("action").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /*
     * deprecate idea
     *
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
    */

    private void showWebViewForWithoutNetwork() {
        urlStringForLoading = String.format("file:///%s/loading/network_400.html", FileUtil.sharedPath(mContext));
        mWebView.loadUrl(urlStringForLoading);
    }


    private void showDialogForDeviceForbided() {
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

    void initColorView(List<ImageView> colorViews) {
        String[] colors = {"#ffffff", "#ffcd0a", "#fd9053", "#dd0929", "#016a43", "#9d203c", "#093db5", "#6a3906", "#192162", "#000000"};
        String userIDStr = String.format("%d", userID);
        int numDiff = colorViews.size() - userIDStr.length();
        numDiff = numDiff < 0 ? 0 : numDiff;

        for (int i = 0; i < colorViews.size(); i++) {
            int colorIndex = 0;
            if (i >= numDiff) {
                colorIndex = Character.getNumericValue(userIDStr.charAt(i - numDiff));
            }
            colorViews.get(i).setBackgroundColor(Color.parseColor(colors[colorIndex]));
        }
    }

    void longLog(String Tag, String str) {
        if (str.length() > 200) {
            Log.i(Tag, str.substring(0, 200));
            longLog(Tag, str.substring(200));
        } else {
            Log.i(Tag, str);
        }
    }


    void modifiedUserConfig(JSONObject configJSON) {
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
    final View.OnClickListener mCheckUpgradeListener = new View.OnClickListener() {
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

    void checkUpgrade(final boolean isShowToast) {
        UpdateManagerListener updateManagerListener = new UpdateManagerListener() {
            @Override
            public void onUpdateAvailable(final String result) {
                Log.i("PGYER", result);

                String message;
                String versionCode = "-1";
                try {
                    JSONObject response = new JSONObject(result);
                    message = response.getString("message");
                    if (message.isEmpty()) {
                        JSONObject responseData = response.getJSONObject("data");
                        message = responseData.getString("releaseNote");
                        versionCode = responseData.getString("versionCode");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    message = e.getMessage();
                }

                // 偶数时为正式版本
                if (Integer.parseInt(versionCode) % 2 == 1) {
                    if (isShowToast) {
                        Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
                    }
                    return;
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
                if (isShowToast) {
                    Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
                }
            }
        };

        PgyUpdateManager.register(BaseActivity.this, updateManagerListener);
    }

    /**
     * app升级后，清除缓存头文件
     */
    void checkVersionUpgrade(String assetsPath) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionConfigPath = String.format("%s/%s", assetsPath, URLs.CURRENT_VERSION__FILENAME);

            String localVersion = "new-installer";
            boolean isUpgrade = true;
            if ((new File(versionConfigPath)).exists()) {
                localVersion = FileUtil.readFile(versionConfigPath);
                if (localVersion.equals(packageInfo.versionName)) {
                    isUpgrade = false;
                }
            }

            if (isUpgrade) {
                Log.i("VersionUpgrade", String.format("%s => %s remove %s/%s", localVersion, packageInfo.versionName, assetsPath, URLs.CACHED_HEADER_FILENAME));

                /*
                 * 用户报表数据js文件存放在公共区域
                 */
                String headerPath = String.format("%s/%s", sharedPath, URLs.CACHED_HEADER_FILENAME);
                File headerFile = new File(headerPath);
                if (headerFile.exists()) {
                    headerFile.delete();
                }

                FileUtil.writeFile(versionConfigPath, packageInfo.versionName);
            }
        } catch (PackageManager.NameNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测服务器端静态文件是否更新
     * to do
     */
    void checkAssetsUpdated(boolean shouldReloadUIThread) {
        checkAssetUpdated(shouldReloadUIThread, "loading", false);
        checkAssetUpdated(shouldReloadUIThread, "fonts", true);
        checkAssetUpdated(shouldReloadUIThread, "images", true);
        checkAssetUpdated(shouldReloadUIThread, "stylesheets", true);
        checkAssetUpdated(shouldReloadUIThread, "javascripts", true);
    }

    private boolean checkAssetUpdated(boolean shouldReloadUIThread, String assetName, boolean isInAssets) {
        boolean isShouldUpdateAssets = false;

        try {
            String assetZipPath = String.format("%s/%s.zip", sharedPath, assetName);

            if (!(new File(assetZipPath)).exists()) {
                isShouldUpdateAssets = true;
            }

            String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
            JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);
            String localKeyName = String.format("local_%s_md5", assetName);
            String keyName = String.format("%s_md5", assetName);
            if (!isShouldUpdateAssets && !userJSON.getString(localKeyName).equals(userJSON.getString(keyName))) {
                isShouldUpdateAssets = true;
            }

            if (!isShouldUpdateAssets) {
                return false;
            }

            Log.i("checkAssetUpdated", String.format("%s: %s != %s", assetZipPath, userJSON.getString(localKeyName), userJSON.getString(keyName)));
            // instantiate it within the onCreate method
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(String.format("更新%s库", assetName));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);

            // execute this when the downloader must be fired
            final DownloadAssetsTask downloadTask = new DownloadAssetsTask(mContext, shouldReloadUIThread, assetName, isInAssets);
            downloadTask.execute(String.format(URLs.API_ASSETS_PATH, URLs.HOST, assetName), assetZipPath);

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }


    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    class DownloadAssetsTask extends AsyncTask<String, Integer, String> {
        private final Context context;
        private PowerManager.WakeLock mWakeLock;
        private final boolean isReloadUIThread;
        private final String assetFilename;
        private final boolean isInAssets;

        public DownloadAssetsTask(Context context, boolean shouldReloadUIThread, String assetFilename, boolean isInAssets) {
            this.context = context;
            this.isReloadUIThread = shouldReloadUIThread;
            this.assetFilename = assetFilename;
            this.isInAssets = isInAssets;
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(params[1]);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();

            if (result != null) {
                Toast.makeText(context, "静态资源更新失败", Toast.LENGTH_LONG).show();
            } else {
                FileUtil.checkAssets(mContext, assetFilename, isInAssets);
                if (isReloadUIThread) {
                    new Thread(mRunnableForDetecting).start();
                }
            }
        }
    }

    class JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void refreshBrowser() {
            new Thread(mRunnableForDetecting).start();
        }

    }
}
