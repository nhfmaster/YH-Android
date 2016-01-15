package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import org.OpenUDID.OpenUDID_manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LoginActivity extends BaseActivity {

	@Override
    @SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        /*
         *  初始化OpenUDID, 设备唯一化
         */
        OpenUDID_manager.sync(getApplicationContext());


        try {
            String htmlPath = String.format("file:///%s/loading/login.html", FileUtil.sharedPath());
            mWebView.loadUrl(htmlPath);

            /*
             *  解压表态资源
             */
            File file = new File(String.format("%s/loading", FileUtil.sharedPath()));
            if(!file.exists()) {
                unZip("loading.zip", FileUtil.sharedPath(), true);
            }
            file = new File(String.format("%s/assets", FileUtil.sharedPath()));
            if(!file.exists()) {
                unZip("assets.zip", FileUtil.sharedPath(), true);
            }

            /*
             * 检测登录界面，版本是否升级
             */
            checkVersionUpgrade(FileUtil.sharedPath());

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         *  加载服务器网页
         */
        if(isNetworkAvailable()) {
            new Thread(runnable).start();
        }
        else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
            builder1.setMessage("Write your message here.");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    public void checkVersionUpgrade(String assetsPath) {
        try {
            PackageInfo packageInfo  = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionConfigPath = String.format("%s/%s", assetsPath, URLs.CURRENT_VERSION__FILENAME);

            boolean isUpgrade = false;
            if((new File(versionConfigPath)).exists()) {
                String localVersion  = FileUtil.readFile(versionConfigPath);
                if(localVersion.compareTo(packageInfo.versionName) != 0) {
                    isUpgrade = true;
                    Log.i("VersionUpgrade", String.format("%s => %s remove %s/%s", localVersion, packageInfo.versionName, assetsPath, URLs.CACHED_HEADER_FILENAME));
                }
            }
            else {
                isUpgrade = true;
            }
            if(isUpgrade) {
                ApiHelper.clearResponseHeader(URLs.LOGIN_PATH, assetsPath);
                FileUtil.writeFile(versionConfigPath, packageInfo.versionName);
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch(message.what) {
                case 200:
                case 304:
                    urlString = (String)message.obj;
                    Log.i("FilePath", urlString);
                    mWebView.loadUrl(String.format("file:///" + urlString));
                    break;
                default:
                    Toast.makeText(LoginActivity.this, "访问服务器失败", Toast.LENGTH_SHORT).show();;
                    break;
            }
        }

    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Map<String, String> response = ApiHelper.httpGetWithHeader(URLs.LOGIN_PATH, FileUtil.sharedPath(), "assets");
            Message message = mHandler.obtainMessage();
            message.what =  Integer.parseInt(response.get("code").toString());

            String[] codes = new String[] {"200", "304"};
            if(Arrays.asList(codes).contains(response.get("code").toString())) {
                message.obj = response.get("path").toString();
            }
            mHandler.sendMessage(message);
        }
    };

    /**
     * 解压assets的zip压缩文件到指定目录
     * @param context上下文对象
     * @param assetName压缩文件名
     * @param outputDirectory输出目录
     * @param isReWrite是否覆盖
     * @throws IOException
     */
    public void unZip(String assetName, String outputDirectory, boolean isReWrite) throws IOException {
        // 创建解压目标目录
        File file = new File(outputDirectory);
        // 如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        // 打开压缩文件
        InputStream inputStream = getApplicationContext().getAssets().open(assetName);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // 读取一个进入点
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        // 使用1Mbuffer
        byte[] buffer = new byte[10*1024 * 1024];
        // 解压时字节计数
        int count = 0;
        // 如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            // 如果是一个目录
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者是文件不存在
                if (isReWrite || !file.exists()) {
                    file.mkdir();
                }
            } else {
                // 如果是文件
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者文件不存在，则解压文件
                if (isReWrite || !file.exists()) {
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, count);
                    }
                    fileOutputStream.close();
                }
            }
            // 定位到下一个文件入口
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    private class JavaScriptInterface {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void login(final String username, String password) {
            if(username.length() > 0 && password.length() > 0) {
                try {
                    String info = ApiHelper.authentication(username, URLs.MD5(password));
                    if (info.compareTo("success") == 0) {

                        // 检测用户空间，版本是否升级
                        assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);
                        checkVersionUpgrade(assetsPath);

                        // 跳转至主界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(intent);

                        // 登录界面，并未被销毁 - reload webview
                        new Thread(runnable).start();
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
    }

}
