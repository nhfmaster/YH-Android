package com.intfocus.yh_android;

import org.OpenUDID.*;

import com.intfocus.yh_android.util.ApiUtil;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class LoginActivity extends Activity {

	private WebView mWebView = null;
	private Activity mActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        OpenUDID_manager.sync(getApplicationContext());
        
		mActivity = this;
		//setContentView(R.layout.activity_main);
        showWebView();
 
        PackageManager manager = this.getPackageManager();
        PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
	        String packageName = info.packageName;  //包名
	        int versionCode = info.versionCode;  //版本号
	        String versionName = info.versionName;   //版本名
	        Log.d("PM", packageName);
	        Log.d("PM", versionName);
	        Log.d("DEVICE",android.os.Build.MODEL);
	        Log.d("DEVICE",android.os.Build.BOARD);
	        Log.d("DEVICE",android.os.Build.DEVICE);
	        Log.d("ID",android.os.Build.ID);
	        Log.d("BRAND",android.os.Build.BRAND);
	        Log.d("HOST",android.os.Build.DISPLAY);
	        Log.d("HOST",android.os.Build.FINGERPRINT);
	        Log.d("HOST",android.os.Build.HARDWARE);
	        Log.d("HOST",android.os.Build.HOST);
	        Log.d("SERIAL",android.os.Build.MANUFACTURER);
	        Log.d("SERIAL",android.os.Build.SERIAL);
	        Log.d("USER",android.os.Build.USER);
	        
	        String dir = FileUtil.dirPath("hello");
	        Log.d("dirPath", dir);
	        
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			mWebView.loadUrl(URLs.UILogin);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Object getHtmlObject(){
		Object insertObj = new Object() {
			public String login(final String username, String password) {
				try {
					password = URLs.MD5(password);
					String msg = String.format("%s-%s", username, password);
					Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
					ApiUtil.authentication(username, password);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
