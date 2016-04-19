package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.intfocus.yh_android.screen_lock.ConfirmPassCodeActivity;
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.AppManager;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.umeng.message.PushAgent;

import org.json.JSONObject;

public class LoginActivity extends BaseActivity {
    private ProgressDialog progressDialog = null;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PushAgent.getInstance(this).onAppStart();

        /*
         *  如果是从触屏界面过来，则直接进入主界面
         *  不是的话，相当于直接启动应用，则检测是否有设置锁屏
         */
        Intent intent = getIntent();
        if (intent.hasExtra("from_activity") && intent.getStringExtra("from_activity").contains("ConfirmPassCodeActivity")) {
            intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("from_activity", this.getClass().toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            LoginActivity.this.startActivity(intent);

            finish();
        } else if (FileUtil.checkIsLocked(mContext)) {
            intent = new Intent(this, ConfirmPassCodeActivity.class);
            intent.putExtra("is_from_login", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);

            finish();
        } else {
            /*
             *  检测版本更新
             *    1. 与锁屏界面互斥；取消解屏时，返回登录界面，则不再检测版本更新；
             *    2. 原因：如果解屏成功，直接进入MainActivity,会在BaseActivity#finishLoginActivityWhenInMainAcitivty中结束LoginActivity,若此时有AlertDialog，会报错误:Activity has leaked window com.android.internal.policy.impl.PhoneWindow$DecorView@44f72ff0 that was originally added here
             */
            checkUpgrade(false);
        }

        pullToRefreshWebView = (PullToRefreshWebView) findViewById(R.id.webview);
        initRefreshWebView();
        setPullToRefreshWebView(false);

        mWebView.requestFocus();
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");

        /*
         * 登录界面
         */
        urlStringForLoading = String.format("file:///%s/loading/login.html", FileUtil.sharedPath(mContext));
        mWebView.loadUrl(urlStringForLoading);

        /*
         * 检测登录界面，版本是否升级
         */
        checkVersionUpgrade(assetsPath);
    }

    private class JavaScriptInterface extends JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void login(final String username, String password) {
            if (username.length() > 0 && password.length() > 0) {

                try {
                    progressDialog.show(LoginActivity.this, "请稍等...", "获取数据中...", true);

                    String info = ApiHelper.authentication(mContext, username, URLs.MD5(password));

                    if (info.compareTo("success") == 0) {

                        // 检测用户空间，版本是否升级
                        assetsPath = FileUtil.dirPath(mContext, URLs.HTML_DIRNAME);
                        checkVersionUpgrade(assetsPath);

                        // 跳转至主界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("from_activity", this.getClass().toString());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        LoginActivity.this.startActivity(intent);

                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, info, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                /*
                 * 用户行为记录, 单独异常处理，不可影响用户体验
                 */
                try {
                    logParams = new JSONObject();
                    logParams.put("action", "登录");
                    new Thread(mRunnableForLogger).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(LoginActivity.this, "请输入用户名与密码", Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void jsException(final String ex) {
            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "JS异常");
                logParams.put("obj_title", String.format("登录页面/%s", ex));
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
