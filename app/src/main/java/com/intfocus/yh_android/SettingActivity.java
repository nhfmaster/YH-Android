package com.intfocus.yh_android;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.intfocus.yh_android.screen_lock.InitPassCodeActivity;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class SettingActivity extends BaseActivity {

    private TextView mUserID;
    private TextView mRoleID;
    private TextView mGroupID;
    private TextView mChangePWD;
    private TextView mCheckUpgrade;
    private TextView mAppName;
    private TextView mAppVersion;
    private TextView mDeviceID;
    private TextView mApiDomain;
    private TextView mStorageType;
    private Switch mLockSwitch;
    private TextView mChangeLock;
    private Button mLogout;

    private String screenLockInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.back).setOnClickListener(mOnBackListener);
        findViewById(R.id.back_text).setOnClickListener(mOnBackListener);

        mUserID = (TextView) findViewById(R.id.user_id);
        mRoleID = (TextView) findViewById(R.id.role_id);
        mGroupID = (TextView) findViewById(R.id.group_id);
        mChangePWD = (TextView) findViewById(R.id.change_pwd);
        mCheckUpgrade = (TextView) findViewById(R.id.check_upgrade);
        mAppName = (TextView) findViewById(R.id.app_name);
        mAppVersion = (TextView) findViewById(R.id.app_version);
        mDeviceID = (TextView) findViewById(R.id.device_id);
        mApiDomain = (TextView) findViewById(R.id.api_domain);
        mChangeLock = (TextView) findViewById(R.id.change_lock);
        mStorageType = (TextView) findViewById(R.id.storage_type);
        mLogout = (Button) findViewById(R.id.logout);
        mLockSwitch = (Switch) findViewById(R.id.lock_switch);
        screenLockInfo = "取消锁屏成功";
        mLockSwitch.setChecked(FileUtil.checkIsLocked(mContext));

        mChangeLock.setOnClickListener(mChangeLockListener);
        mChangePWD.setOnClickListener(mChangePWDListener);
        mLogout.setOnClickListener(mLogoutListener);
        mCheckUpgrade.setOnClickListener(mCheckUpgradeListener);
        mLockSwitch.setOnCheckedChangeListener(mSwitchLockListener);

        initializeUI();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        // Get the Camera instance as the activity achieves full user focus

        mLockSwitch.setChecked(FileUtil.checkIsLocked(mContext));
    }

    private void initializeUI() {
        /*
         * 初始化界面内容
         */
        try {
            mUserID.setText(user.getString("user_name"));
            mRoleID.setText(user.getString("role_name"));
            mGroupID.setText(user.getString("group_name"));

            mAppName.setText(getApplicationName(SettingActivity.this));
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                mAppVersion.setText(packageInfo.versionName);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            mDeviceID.setText(TextUtils.split(android.os.Build.MODEL, " - ")[0]);
            mApiDomain.setText(URLs.HOST.replace("http://", ""));

            mStorageType.setText(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? "SD卡" : "手机");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    private View.OnClickListener mOnBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "返回/设置页面");
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            SettingActivity.this.onBackPressed();
        }
    };

    /*
     * 退出登录
     */
    private View.OnClickListener mLogoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                JSONObject configJSON = new JSONObject();
                configJSON.put("is_login", false);

                modifiedUserConfig(configJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent();
            intent.setClass(SettingActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//它可以关掉所要到的界面中间的activity
            startActivity(intent);


            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "退出登录");
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /*
    * 修改密码
    */
    private View.OnClickListener mChangePWDListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, ResetPasswordActivity.class);
            mContext.startActivity(intent);

            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "点击/设置页面/修改密码");
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /*
     * 修改锁屏密码
     */
    private View.OnClickListener mChangeLockListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(SettingActivity.this, "TODO: 修改锁屏密码", Toast.LENGTH_SHORT).show();
        }
    };

    /*
     * 检测版本更新
     * {"code":0,"message":"","data":{"lastBuild":"10","downloadURL":"","versionCode":"15","versionName":"0.1.5","appUrl":"http:\/\/www.pgyer.com\/yh-a","build":"10","releaseNote":"\u66f4\u65b0\u5230\u7248\u672c: 0.1.5(build10)"}}
     */
    private View.OnClickListener mCheckUpgradeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UpdateManagerListener updateManagerListener = new UpdateManagerListener() {

                @Override
                public void onUpdateAvailable(final String result) {

                    String message = "服务器获取信息失败。";
                    try {
                        JSONObject response = new JSONObject(result);
                        message = response.getString("message");
                        if(message.isEmpty()) {
                            JSONObject responseData = response.getJSONObject("data");
                            if(responseData.has("releaseNote")) {
                                message = responseData.getString("releaseNote");
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        message = e.getMessage();
                    }

                    // 将新版本信息封装到AppBean中
                    final AppBean appBean = getAppBeanFromString(result);
                    new AlertDialog.Builder(SettingActivity.this)
                            .setTitle("版本更新")
                            .setMessage(message.isEmpty() ? "没有升级简介" : message)
                            .setPositiveButton(
                                    "确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startDownloadTask(SettingActivity.this, appBean.getDownloadURL());
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
                    Toast.makeText(SettingActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
                }
            };

            PgyUpdateManager.register(SettingActivity.this, updateManagerListener);

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

    /*
     *  Switch 锁屏开关
     */
    CompoundButton.OnCheckedChangeListener mSwitchLockListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            if(isChecked) {

                startActivity(InitPassCodeActivity.createIntent(mContext));
            } else {
                try {
                    String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
                    if((new File(userConfigPath)).exists()) {
                        JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);
                        userJSON.put("use_gesture_password", false);
                        if(!userJSON.has("gesture_password")) {
                            userJSON.put("gesture_password", "");
                        }

                        FileUtil.writeFile(userConfigPath, userJSON.toString());
                        String settingsConfigPath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
                        FileUtil.writeFile(settingsConfigPath, userJSON.toString());
                    }

                    Toast.makeText(mContext, screenLockInfo, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", String.format("点击/设置页面/%s锁屏", isChecked ? "开启" : "禁用"));
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
