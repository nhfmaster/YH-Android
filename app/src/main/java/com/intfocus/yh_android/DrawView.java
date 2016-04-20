package com.intfocus.yh_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import java.util.ArrayList;

public class DrawView extends View {
    private Paint brush = new Paint();
    private Paint brushBlack = new Paint();
    private Paint brushRed  = new Paint();
    private Paint brushBlue = new Paint();
    private Path path = new Path();
    public Button btnEraseAll;
    public Button btnRed;
    public Button btnBlue;
    public LayoutParams btnEraseAllParams;
    public LayoutParams btnBlueAllParams;
    public LayoutParams btnRedAllParams;
    private ArrayList<Path> savePath;

    public DrawView(Context context) {
        super(context);

        brushBlack.setAntiAlias(true);
        brushBlack.setColor(Color.BLACK);
        brushBlack.setStyle(Paint.Style.STROKE);
        brushBlack.setStrokeJoin(Paint.Join.ROUND);
        brushBlack.setStrokeWidth(5f);
        brush = brushBlack;

        btnEraseAll = new Button(context);
        btnBlue = new Button(context);
        btnRed = new Button(context);
        btnEraseAll = new Button(context);

        btnBlueAllParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnBlue.setText("Blue");
        btnBlue.setWidth(100);
        btnBlue.setX(600.00F);
        btnBlue.setLayoutParams(btnBlueAllParams);
        btnBlue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                brushBlue.setAntiAlias(true);
                brushBlue.setColor(Color.BLUE);
                brushBlue.setStyle(Paint.Style.STROKE);
                brushBlue.setStrokeJoin(Paint.Join.ROUND);
                brushBlue.setStrokeWidth(5f);
                brush = brushBlue;
            }
        });

        btnRedAllParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnRed.setText("Red");
        btnRed.setWidth(100);
        btnRed.setLayoutParams(btnRedAllParams);
        btnRed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                brushRed.setAntiAlias(true);
                brushRed.setColor(Color.RED);
                brushRed.setStyle(Paint.Style.STROKE);
                brushRed.setStrokeJoin(Paint.Join.ROUND);
                brushRed.setStrokeWidth(5f);
                brush = brushRed;
            }
        });

        btnEraseAllParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnEraseAll.setText("Erase");
        btnEraseAll.setWidth(150);
        btnEraseAll.setX(300.00F);
        btnEraseAll.setLayoutParams(btnEraseAllParams);
        btnEraseAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // reset the path
                path.reset();
                // invalidate the view
                postInvalidate();

            }
        });
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
                break;
            default:
                return false;
        }
        // Force a view to draw.
        postInvalidate();
        return false;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            canvas.drawPath(path, brush);
            canvas.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
