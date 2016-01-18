package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog.Builder;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by lijunjie on 16/1/18.
 */
public class ResetPasswordActivity extends BaseActivity {

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

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
        mWebView.loadUrl(String.format("file:///%s/loading/loading.html", FileUtil.sharedPath()));


        urlString = String.format("%s%s", URLs.HOST, URLs.RESET_PASSWORD_PATH);
        new Thread(runnable).start();
    }


    @SuppressLint("SetJavaScriptEnabled")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 200:
                case 304:
                    String htmlPath = (String) message.obj;
                    Log.i("FilePath", htmlPath);
                    mWebView.loadUrl(String.format("file:///" + htmlPath));
                    break;
                default:
                    Toast.makeText(ResetPasswordActivity.this, "访问服务器失败", Toast.LENGTH_SHORT).show();
                    ;
                    break;
            }
        }

    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Map<String, String> response = ApiHelper.httpGetWithHeader(urlString, assetsPath, "../../Shared/assets");
            Message message = mHandler.obtainMessage();
            message.what = Integer.parseInt(response.get("code").toString());

            String[] codes = new String[]{"200", "304"};
            if (Arrays.asList(codes).contains(response.get("code").toString())) {
                message.obj = response.get("path").toString();
            }
            mHandler.sendMessage(message);
        }
    };

    private class JavaScriptInterface {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void resetPassword(final String oldPassword, final String newPassword) {
            try {
                if (URLs.MD5(oldPassword).equals(user.get("password"))) {
                    Map<String, String> response = ApiHelper.resetPassword(user.get("user_id").toString(), URLs.MD5(newPassword));

                    JSONObject responseInfo = new JSONObject(response.get("body").toString());

                    Builder alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this);
                    alertDialog.setTitle("温馨提示");
                    alertDialog.setMessage(responseInfo.getString("info"));

                    if (response.get("code").equals("200")) {
                        alertDialog.setPositiveButton(
                                "重新登录",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setClass(ResetPasswordActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//它可以关掉所要到的界面中间的activity
                                        startActivity(intent);
                                    }
                                }
                        );
                        alertDialog.show();
                    } else {
                        alertDialog.setNegativeButton(
                                "好的",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }
                        );
                        alertDialog.show();
                    }

                } else {
                    Toast.makeText(ResetPasswordActivity.this, "原始密码输入有误", Toast.LENGTH_SHORT).show();
                    new Thread(runnable).start();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ResetPasswordActivity.this, "请退出，重新登录，再尝试", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
