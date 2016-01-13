package com.intfocus.yh_android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

public class SettingActivity extends Activity {

    private TextView mUserID;
    private TextView mRoleID;
    private TextView mGroupID;
    private TextView mChangePWD;
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
    private View.OnClickListener mLogoutListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /* 退出登录 TODO */
        }
    };
    private View.OnClickListener mChangePWDListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /* 修改密码 TODO */
        }
    };
    private View.OnClickListener mChangeLockListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /* 修改锁屏密码 TODO */
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

        initialize();
    }

    private void initialize() {
        /* 初始化界面内容 TODO */
        mUserID.setText("UserID");
    }
}
