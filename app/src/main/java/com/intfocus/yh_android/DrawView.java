package com.intfocus.yh_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.intfocus.yh_android.util.TencentUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrawView extends View {
    private Paint brush = new Paint();
    Path path = new Path();
    public Button btnEraseAll;
    public Button btnRed;
    public Button btnBlue;
    public Button btnSave;
    public Button btnDisplay;
    public Button btnCancel;
    public LayoutParams btnEraseParams;
    public LayoutParams btnBlueParams;
    public LayoutParams btnRedParams;
    public LayoutParams btnSaveParams;
    public LayoutParams btnDisplayParams;
    public LayoutParams btnCancelParams;
    private Map<Path, Integer> colorsMap = new HashMap<>();
    private int selectedColor;
    ArrayList<Path> savePath = new ArrayList<>();
    int i = 1;
    private IWXAPI wxApi;
    private static final int THUMB_SIZE = 150;

    private Intent intent = new Intent();

    public DrawView(Context context) {
        super(context);

        String APP_ID = "wx5a37b4326f4dd280";
        wxApi = WXAPIFactory.createWXAPI(getContext(), APP_ID, false);
        wxApi.registerApp(APP_ID);

        brush.setAntiAlias(true);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(5f);
        selectedColor = Color.BLACK;

        btnEraseAll = new Button(context);
        btnBlue = new Button(context);
        btnRed = new Button(context);
        btnEraseAll = new Button(context);
        btnSave = new Button(context);
        btnDisplay = new Button(context);
        btnCancel = new Button(context);

        btnBlueParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnBlue.setBackgroundColor(Color.BLUE);
        btnBlueParams.width = 50;
        btnBlueParams.height = 70;
        btnBlue.setAlpha(0);
        btnBlue.setX(230);
        btnBlue.setY(15);
        btnBlue.setLayoutParams(btnBlueParams);
        btnBlue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = Color.BLUE;
            }
        });

        btnRedParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnRed.setBackgroundColor(Color.RED);
        btnRed.setAlpha(0);
        btnRedParams.width = 50;
        btnRedParams.height = 70;
        btnRed.setX(170);
        btnRed.setY(15);
        btnRed.setLayoutParams(btnRedParams);
        btnRed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = Color.RED;
            }
        });

        btnEraseParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnEraseAll.setText("Erase");
        btnEraseAll.setAlpha(0);
        btnEraseAll.setX(280.00F);
        btnEraseAll.setLayoutParams(btnEraseParams);
        btnEraseAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                for (Path p : savePath) {
                    p.reset();
                }
                postInvalidate();

            }
        });

        btnCancelParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnCancel.setText("Cancel");
        btnCancel.setAlpha(0);
        btnCancel.setX(550);
        btnCancel.setLayoutParams(btnCancelParams);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(getContext(), MainActivity.class);
                getContext().startActivity(intent);
            }
        });

        btnSaveParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnSave.setText("Save");
        btnSave.setWidth(100);
        btnSave.setAlpha(0);
        btnSave.setX(430.00F);
        btnSave.setLayoutParams(btnSaveParams);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBlue.setAlpha(0);
                btnRed.setAlpha(0);
                btnEraseAll.setAlpha(0);
                btnSave.setAlpha(0);
                btnDisplay.setAlpha(0);
                btnCancel.setAlpha(0);
                saveBitmap(shot((Activity) getContext()));
                Bitmap bmp = shot((Activity) getContext());

                WXImageObject imgObj = new WXImageObject(bmp);
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                bmp.recycle();
                msg.thumbData = TencentUtil.bmpToByteArray(thumbBmp, true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("img");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneSession;
                wxApi.sendReq(req);
//                intent.setClass(getContext(), MainActivity.class);
//                getContext().startActivity(intent);
            }
        });

        btnDisplayParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnDisplay.setText("Display");
//        btnDisplay.setY(1000);
        btnDisplay.setLayoutParams(btnDisplayParams);
        btnDisplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i % 2 == 0) {
                    btnBlue.setAlpha(0);
                    btnBlue.setClickable(false);
                    btnRed.setAlpha(0);
                    btnRed.setClickable(false);
                    btnEraseAll.setAlpha(0);
                    btnEraseAll.setClickable(false);
                    btnSave.setAlpha(0);
                    btnSave.setClickable(false);
                    btnCancel.setAlpha(0);
                    btnCancel.setClickable(false);
                } else {
                    btnBlue.setAlpha(0.5F);
                    btnBlue.setClickable(true);
                    btnRed.setAlpha(0.5F);
                    btnRed.setClickable(true);
                    btnEraseAll.setAlpha(0.5F);
                    btnEraseAll.setClickable(true);
                    btnSave.setAlpha(0.5F);
                    btnSave.setClickable(true);
                    btnCancel.setAlpha(0.5F);
                    btnCancel.setClickable(true);
                }
                i++;
            }
        });

    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Path p : savePath) {
            brush.setColor(colorsMap.get(p));
            canvas.drawPath(p, brush);
        }
        brush.setColor(selectedColor);
        canvas.drawPath(path, brush);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float pointX = event.getX();
        float pointY = event.getY();

        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(pointX, pointY);

                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                savePath.add(path);
                colorsMap.put(path, selectedColor);
                path = new Path();
                invalidate();
                break;
            default:
                return false;
        }
        // Force a view to draw.
        postInvalidate();
        return false;
    }

    private Bitmap shot(Activity activity) {
        //View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        // 获取状态栏高度 /
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Log.i("TAG", "" + statusBarHeight);
        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public void saveBitmap(Bitmap bitmap) {
        File f = new File("/data/data/com.intfocus.yh_android/cache/", "1.jpg");
        if (f.exists()) {
            f.delete();
        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
