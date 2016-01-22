package com.intfocus.yh_android.screen_lock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.intfocus.yh_android.R;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by lijunjie on 16/1/22.
 */
public class InitPassCodeActivity extends Activity {

    // テキストの定数
    private final String TEXT_MAIN_CONFIRM = "已锁屏";
    private final String TEXT_SUB_CONFIRM = "请输入密码";
    private final String TEXT_MAIN_MISTAKE = "密码有误";
    private final String TEXT_SUB_MISTAKE = "确认密码输入正确";
    // 入力カウンター
    private byte counter = 0;
    // パスワード照合用変数
    private int password;
    // パスワードの入力、削除を処理するオブジェクト
    private StringBuilder stringBuilder;
    // パスコードを入力する毎に変わるTextView
    private TextView text_main_pass;
    private TextView text_sub_pass;
    // CircleのImageView
    private ImageView circle1;
    private ImageView circle2;
    private ImageView circle3;
    private ImageView circle4;
    // Canvas用のBitmap生成
    private Bitmap bitmapBlack = Bitmap.createBitmap(300, 300,
            Bitmap.Config.ARGB_8888);
    private Bitmap bitmapGlay = Bitmap.createBitmap(300, 300,
            Bitmap.Config.ARGB_8888);

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, InitPassCodeActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_pass_code);
        stringBuilder = new StringBuilder();
        initViews();
        initCircleCanvas();
    }

    private void initViews() {
        text_main_pass = (TextView) findViewById(R.id.text_main_pass);
        text_sub_pass = (TextView) findViewById(R.id.text_sub_pass);

        circle1 = (ImageView) findViewById(R.id.circle1);
        circle2 = (ImageView) findViewById(R.id.circle2);
        circle3 = (ImageView) findViewById(R.id.circle3);
        circle4 = (ImageView) findViewById(R.id.circle4);

        circle1.setImageBitmap(bitmapGlay);
        circle2.setImageBitmap(bitmapGlay);
        circle3.setImageBitmap(bitmapGlay);
        circle4.setImageBitmap(bitmapGlay);
    }

    private void initCircleCanvas() {
        // Canvasの作成:描画先のBitmapを与える
        Canvas canvas;
        canvas = new Canvas(bitmapBlack);
        // 円（青）の描画
        Paint paint;
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(150, 150, 148, paint);
        // 円（灰）の描画
        Canvas canvas2;
        canvas2 = new Canvas(bitmapGlay);
        Paint paint2;
        paint2 = new Paint();
        paint2.setColor(Color.parseColor("#f5f5f5"));
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);
        canvas2.drawCircle(150, 150, 148, paint2);
    }

    private void initStringBuilder() {
        stringBuilder.setLength(0);
        stringBuilder.trimToSize();
    }

    private void initCircleColor() {
        circle1.setImageBitmap(bitmapGlay);
        circle2.setImageBitmap(bitmapGlay);
        circle3.setImageBitmap(bitmapGlay);
        circle4.setImageBitmap(bitmapGlay);
    }


    public void put0(View view) {
        inputPassword("0");
    }

    public void put1(View view) {
        inputPassword("1");
    }

    public void put2(View view) {
        inputPassword("2");
    }

    public void put3(View view) {
        inputPassword("3");
    }

    public void put4(View view) {
        inputPassword("4");
    }

    public void put5(View view) {
        inputPassword("5");
    }

    public void put6(View view) {
        inputPassword("6");
    }

    public void put7(View view) {
        inputPassword("7");
    }

    public void put8(View view) {
        inputPassword("8");
    }

    public void put9(View view) {
        inputPassword("9");
    }

    public void onDelete(View view) {
        int length = stringBuilder.length();
        deleteCircleColor(length);
        if (length != 0)
            stringBuilder.deleteCharAt(length - 1);
    }

    private void deleteCircleColor(int length) {
        switch (length) {
            case 1:
                circle1.setImageBitmap(bitmapGlay);
                break;
            case 2:
                circle2.setImageBitmap(bitmapGlay);
                break;
            case 3:
                circle3.setImageBitmap(bitmapGlay);
                break;
            case 4:
                circle4.setImageBitmap(bitmapGlay);
                break;
            default:
                break;
        }
    }

    private void inputPassword(String password) {
        switch (stringBuilder.length()) {
            case 0:
                circle1.setImageBitmap(bitmapBlack);
                stringBuilder.append(password);
                break;
            case 1:
                circle2.setImageBitmap(bitmapBlack);
                stringBuilder.append(password);
                break;
            case 2:
                circle3.setImageBitmap(bitmapBlack);
                stringBuilder.append(password);
                break;
            case 3:
                circle4.setImageBitmap(bitmapBlack);
                stringBuilder.append(password);
                confirmPassword();
                break;
            default:
                break;
        }
    }

    private void confirmPassword() {
        // 確認に失敗したらもう一度最初から入力させる
        switch (counter) {
            case 0:
                //四つ目の円をしばらく表示するため
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        counter++;
                        text_main_pass.setText(TEXT_MAIN_CONFIRM);
                        text_sub_pass.setText(TEXT_SUB_CONFIRM);
                        InitPassCodeActivity.this.password = Integer
                                .parseInt(stringBuilder.toString());
                        initStringBuilder();
                        initCircleColor();
                    }
                }, 200);
                break;
            default:
                if (this.password == Integer.parseInt(stringBuilder.toString())) {
                    // パスワード入力が完了したらpasswordをプリファレンスに保存
                    Toast.makeText(InitPassCodeActivity.this, "设定成功", Toast.LENGTH_SHORT).show();
                    // PrefUtil.setBool(getApplicationContext(), AppConfig.PREF_KEY_IS_LOCKED, true);
                    // PrefUtil.setInt(getApplicationContext(), AppConfig.PREF_KEY_PASSWORD, password);

                    try {
                        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
                        JSONObject userJSON = new JSONObject();
                        if ((new File(userConfigPath)).exists()) {
                            userJSON = FileUtil.readConfigFile(userConfigPath);
                        }
                        userJSON.put("use_gesture_password", true);
                        userJSON.put("gesture_password", password);

                        Log.i("confirmPassword2", "yes");

                        FileUtil.writeFile(userConfigPath, userJSON.toString());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    finish();
                } else {
                    // もう一度やり直し
                    text_main_pass.setText(TEXT_MAIN_MISTAKE);
                    text_sub_pass.setText(TEXT_SUB_MISTAKE);
                    counter = 0;
                    password = 0;
                    initStringBuilder();
                    initCircleColor();

                    Log.i("confirmPassword2", "no");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.bitmapBlack = null;
        this.bitmapGlay = null;
        this.circle1 = null;
        this.circle2 = null;
        this.circle3 = null;
        this.circle4 = null;
        this.text_main_pass = null;
        this.text_sub_pass = null;
        if (this.stringBuilder != null) {
            initStringBuilder();
            this.stringBuilder = null;
        }
    }

    public void onCancel(View view) {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
