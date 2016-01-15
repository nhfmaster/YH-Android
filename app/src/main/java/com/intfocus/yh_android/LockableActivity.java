package com.intfocus.yh_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by wiky on 1/15/16.
 */
public class LockableActivity extends Activity{

    private boolean mInApp;
    private boolean mShouldLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInApp =false;
        mShouldLock=true;
    }

    @Override
    protected void onRestart(){
        super.onRestart();

        if(mShouldLock){
            Intent intent=new Intent(this, LockActivity.class);
            startActivity(intent);
        }else{
            mInApp=false;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(mInApp){
            mShouldLock=false;
        }else{
            mShouldLock=true;
        }
    }


    @Override
    public void startActivity(Intent intent){
        super.startActivity(intent);
        mInApp =true;
    }
}
