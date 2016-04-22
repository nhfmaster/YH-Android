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
import java.util.HashMap;
import java.util.Map;

public class DrawView extends View {
    private Paint brush = new Paint();
    Path path = new Path();
    public Button btnEraseAll;
    public Button btnRed;
    public Button btnBlue;
    public LayoutParams btnEraseAllParams;
    public LayoutParams btnBlueAllParams;
    public LayoutParams btnRedAllParams;
    private Map<Path, Integer> colorsMap = new HashMap<>();
    private int selectedColor;
    ArrayList<Path> savePath = new ArrayList<>();
    int i = 0;

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

        btnBlueAllParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        btnBlue.setText("Blue");
        btnBlue.setWidth(100);
        btnBlue.setX(600.00F);
        btnBlue.setLayoutParams(btnBlueAllParams);
        btnBlue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = Color.BLUE;
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
                selectedColor = Color.RED;
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
}


