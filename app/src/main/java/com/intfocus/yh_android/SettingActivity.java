package com.intfocus.yh_android;

import com.intfocus.yh_android.LockActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.content.pm.PackageManager.NameNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.content.DialogInterface;

import com.pgyersdk.update.PgyUpdateManager;

import android.app.AlertDialog;
import android.widget.Toast;

import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.UpdateManagerListener;

import java.util.Iterator;
import java.io.IOException;

public class SettingActivity extends LockableActivity {

    private TextView mUserID;
    private TextView mRoleID;
    private TextView mGroupID;
    private TextView mChangePWD;
    private TextView mCheckUpgrade;
    private TextView mAppName;
    private TextView mAppVersion;
    private TextView mDeviceID;
    private TextView mApiDomain;
    private Switch mLockSwitch;
    private TextView mChangeLock;
    private Button mLogout;
    private View.OnClickListener mOnBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
        }
    };

    /*
    * 修改密码
    */
    private View.OnClickListener mChangePWDListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SettingActivity.this, ResetPasswordActivity.class);
            SettingActivity.this.startActivity(intent);
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
                    Log.i("Upgrade", result);

                    String message = "服务器获取信息失败。";
                    try {
                        JSONObject response = new JSONObject(result);
                        message = response.getString("message");
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
        }
    };

    /*
     *  Switch 锁屏开关
     */
    CompoundButton.OnCheckedChangeListener mSwitchLockListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub

            try {
                JSONObject configJSON = new JSONObject();
                configJSON.put("use_gesture_password", isChecked);

                modifiedUserConfig(configJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String message = String.format("TODO: 锁屏%s", isChecked ? "开启" : "禁用");
            Toast.makeText(SettingActivity.this, message, Toast.LENGTH_SHORT).show();
            //if (isChecked) {
                //Intent intent = new Intent(SettingActivity.this, LockActivity.class);
                //startActivity(intent);
            //}
        }
    };

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
        mLockSwitch = (Switch) findViewById(R.id.lock_switch);
        mChangeLock = (TextView) findViewById(R.id.change_lock);
        mLogout = (Button) findViewById(R.id.logout);

        mChangeLock.setOnClickListener(mChangeLockListener);
        mChangePWD.setOnClickListener(mChangePWDListener);
        mLogout.setOnClickListener(mLogoutListener);
        mCheckUpgrade.setOnClickListener(mCheckUpgradeListener);
        mLockSwitch.setOnCheckedChangeListener(mSwitchLockListener);

        initializeUI();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }


    private void modifiedUserConfig(JSONObject configJSON) {
        try {
            String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
            JSONObject json = FileUtil.readConfigFile(userConfigPath);

            Iterator it = configJSON.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                json.put(key, configJSON.get(key));
            }

            FileUtil.writeFile(userConfigPath, json.toString());

            String settingsConfigPath = FileUtil.dirPath(URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
            FileUtil.writeFile(settingsConfigPath, json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
