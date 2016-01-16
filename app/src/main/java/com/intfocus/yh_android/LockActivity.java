package com.intfocus.yh_android;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LockActivity extends ActionBarActivity {

    private List<TextView> mTextViews =new ArrayList<TextView>(4);
    private EditText mEdit;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        mTextViews.add((TextView) findViewById(R.id.pwd0));
        mTextViews.add((TextView) findViewById(R.id.pwd1));
        mTextViews.add((TextView) findViewById(R.id.pwd2));
        mTextViews.add((TextView) findViewById(R.id.pwd3));
        mPassword="";

        mEdit = (EditText) findViewById(R.id.input);
        mEdit.requestFocus();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard(mEdit);
            }
        }, 300);
        mEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                        int value = keyCode - KeyEvent.KEYCODE_0;
                        mPassword += String.valueOf(value);
                        checkPassword();
                    } else if (keyCode == KeyEvent.KEYCODE_DEL && mPassword.length() > 0) {
                        mPassword = mPassword.substring(0, mPassword.length() - 2);
                        updatePassword();
                    }
                }
                return true;
            }
        });
    }

    private void checkPassword(){
        if(mPassword.length()<4){
            updatePassword();
            return;
        }
        /* TODO 检查密码 */
        if(mPassword.equals("1234")){
            hideKeyboard(mEdit);
            finish();
        }else{
            mPassword="";
            updatePassword();
        }
    }

    private void updatePassword(){
        for (int i=0;i<mPassword.length();i++){
            mTextViews.get(i).setText("*");
        }
        for (int i=mPassword.length();i<4;i++){
            mTextViews.get(i).setText("");
        }
    }

    protected void showKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) LockActivity.this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    protected void hideKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) LockActivity.this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
