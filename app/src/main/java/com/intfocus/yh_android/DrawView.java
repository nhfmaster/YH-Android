package com.intfocus.yh_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;

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
    public LayoutParams btnEraseParams;
    public LayoutParams btnBlueParams;
    public LayoutParams btnRedParams;
    public LayoutParams btnSaveParams;
    private Map<Path, Integer> colorsMap = new HashMap<>();
    private int selectedColor;
    ArrayList<Path> savePath = new ArrayList<>();

    private Intent intent = new Intent();

    public DrawView(Context context) {
        super(context);

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

        btnBlueParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnBlue.setBackgroundColor(Color.BLUE);
        btnBlue.setAlpha(0.5F);
        btnBlue.setWidth(100);
        btnBlue.setX(600.00F);
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
        btnRed.setAlpha(0.5F);
        btnRed.setWidth(100);
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
        btnEraseAll.setWidth(150);
        btnEraseAll.setX(300.00F);
        btnEraseAll.setLayoutParams(btnEraseParams);
        btnEraseAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // reset the path
                for (Path p : savePath) {
                    p.reset();
                }
                // invalidate the view
                postInvalidate();

            }
        });

        btnSaveParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnSave.setText("Save");
        btnSave.setWidth(100);
        btnSave.setX(300.00F);
        btnSave.setY(1000.00F);
        btnSave.setLayoutParams(btnRedParams);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBlue.setAlpha(0);
                btnRed.setAlpha(0);
                btnEraseAll.setAlpha(0);
                btnSave.setAlpha(0);
                saveBitmap(shot((Activity) getContext()));
                intent.setClass(getContext(), MainActivity.class);
                getContext().startActivity(intent);
            }
        });

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
