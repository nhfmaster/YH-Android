package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.Toast;

import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lijunjie on 16/1/14.
 */
public class BaseActivity extends Activity {

    protected WebView mWebView;
    protected JSONObject user;
    protected int userID = 0;
    protected String urlString;
    protected String assetsPath;
    protected String relativeAssetsPath;
    protected String urlStringForDetecting;
    protected String urlStringForLoading;
    private static ArrayList<Activity> mActivities = new ArrayList<Activity>();

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivities.add(this);
        finishLoginActivityWhenInMainAcitivty(this);

        String sharedPath = FileUtil.sharedPath();
        assetsPath = sharedPath;
        urlStringForDetecting = URLs.HOST;
        relativeAssetsPath = "assets";
        urlStringForLoading = String.format("file:///%s/loading/login.html", sharedPath);

        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
        if ((new File(userConfigPath)).exists()) {
            try {
                user = FileUtil.readConfigFile(userConfigPath);
                if (user.has("is_login") && user.getBoolean("is_login")) {
                    userID = user.getInt("user_id");
                    assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);
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
        } else
            Log.i(Tag, str);
    }


    protected void modifiedUserConfig(JSONObject configJSON) {
        try {
            String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
            JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);

            userJSON = ApiHelper.merge(userJSON, configJSON);
            FileUtil.writeFile(userConfigPath, userJSON.toString());

            String settingsConfigPath = FileUtil.dirPath(URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
            FileUtil.writeFile(settingsConfigPath, userJSON.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
