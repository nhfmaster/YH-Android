package com.intfocus.yh_android;

import com.intfocus.yh_android.util.URLs;

import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

public class LoginActivity extends Activity {

	private WebView mWebView = null;
	private Activity mActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = this;
		//setContentView(R.layout.activity_main);
        showWebView();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void showWebView() {
		try {
			mWebView = new WebView(this);
			setContentView(mWebView);
			
			mWebView.requestFocus();
			
			mWebView.setWebChromeClient(new WebChromeClient(){
				@Override
				public void onProgressChanged(WebView view, int progress){
					LoginActivity.this.setTitle("Loading...");
					LoginActivity.this.setProgress(progress);
					
					if(progress >= 80) {
						LoginActivity.this.setTitle("YH");
					}
				}
			});
			
			mWebView.setOnKeyListener(new View.OnKeyListener() {		// webview can go back
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
						mWebView.goBack();
						return true;
					}
					return false;
				}
			});
			
			WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("utf-8");

			mWebView.addJavascriptInterface(getHtmlObject(), "AndroidBridge");
			mWebView.loadUrl(URLs.Login);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Object getHtmlObject(){
		Object insertObj = new Object() {
			public String login(final String username, final String password) {
				String msg = String.format("%s-%s", username, password);
				Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
				return "";
			}
			public String HtmlcallJava(){
				return "Html call Java";
			}
			
			public String HtmlcallJava2(final String param){
				return "Html call Java : " + param;
			}
			
			public void JavacallHtml(){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mWebView.loadUrl("javascript: showFromHtml()");
						Toast.makeText(LoginActivity.this, "clickBtn", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			public void JavacallHtml2(){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mWebView.loadUrl("javascript: showFromHtml2('IT-homer blog')");
						Toast.makeText(LoginActivity.this, "clickBtn2", Toast.LENGTH_SHORT).show();
					}
				});
			}
		};
		
		return insertObj;
	}
}
