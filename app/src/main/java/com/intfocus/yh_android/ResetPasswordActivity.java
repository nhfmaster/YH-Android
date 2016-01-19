package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.Iterator;
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
        mWebView.loadUrl(urlStringForLoading);


        urlString = String.format("%s%s", URLs.HOST, URLs.RESET_PASSWORD_PATH);
        new Thread(mRunnableForDetecting).start();
    }

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
                                        try {
                                            JSONObject configJSON = new JSONObject();
                                            configJSON.put("is_login", false);

                                            modifiedUserConfig(configJSON);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

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
                    new Thread(mRunnableForDetecting).start();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ResetPasswordActivity.this, "请退出，重新登录，再尝试", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
