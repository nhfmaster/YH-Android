package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.intfocus.yh_android.util.ApiUtil;
import com.intfocus.yh_android.util.URLs;

import org.OpenUDID.OpenUDID_manager;

public class LoginActivity extends Activity {

	private WebView mWebView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.initialize();

        /*
         *  初始化OpenUDID, 设备唯一化
         */
        OpenUDID_manager.sync(getApplicationContext());
        
        showWebView();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void showWebView() {
		try {
            mWebView.requestFocus();
			
			mWebView.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return false;
				}
			});

            mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");
            mWebView.loadUrl(URLs.LOGIN_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private class JavaScriptInterface {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void login(final String username, String password) {
            if(username.length() > 0 && password.length() > 0) {
                try {
                    String info = ApiUtil.authentication(username, URLs.MD5(password));
                    if (info.compareTo("success") == 0) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, info, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(LoginActivity.this, "请输入用户名与密码", Toast.LENGTH_SHORT).show();
            }
        }

        public String HtmlcallJava() {
            return "Html call Java";
        }

        public String HtmlcallJava2(final String param) {
            return "Html call Java : " + param;
        }

        public void JavacallHtml() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript: showFromHtml()");
                    Toast.makeText(LoginActivity.this, "clickBtn", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void JavacallHtml2() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript: showFromHtml2('IT-homer blog')");
                    Toast.makeText(LoginActivity.this, "clickBtn2", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void callHandler(String tag, Object obj, Object cb) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            LoginActivity.this.startActivity(intent);
        }
    }
}
