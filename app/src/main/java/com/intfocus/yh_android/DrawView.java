package com.intfocus.yh_android;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;

import java.io.InputStream;

public class DrawView extends View {
    private Paint brush = new Paint();
    private Path path = new Path();
    public Button btnEraseAll;
    public LayoutParams params;

    public DrawView(Context context) {
        super(context);

        brush.setAntiAlias(true);
        brush.setColor(Color.BLUE);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(5f);

        btnEraseAll = new Button(context);
        btnEraseAll.setText("Erase Everything!!");
        params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        btnEraseAll.setLayoutParams(params);
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

//    public DrawView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        initView(context, attrs);
//    }

//    private void initView(Context context, AttributeSet attributeSet) {
//        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.DrawView);
//        int bgColor = a.getColor(R.styleable.DrawView_backgroundColor, Color.TRANSPARENT);
//
//        brush.setAntiAlias(true);
//        brush.setColor(Color.BLUE);
//        brush.setStyle(Paint.Style.STROKE);
//        brush.setStrokeJoin(Paint.Join.ROUND);
//        brush.setStrokeWidth(5f);
//
//        btnEraseAll = new Button(context);
//        btnEraseAll.setText("Erase Everything!!");
//        params = new LayoutParams(LayoutParams.MATCH_PARENT,
//                LayoutParams.WRAP_CONTENT);
//        btnEraseAll.setLayoutParams(params);
//        btnEraseAll.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                // reset the path
//                path.reset();
//                // invalidate the view
//                postInvalidate();
//
//            }
//        });
//
//        a.recycle(); //释放TypedArray 资源
//
//        setBackgroundColor(bgColor);
//    }

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
        super.onDraw(canvas);
        Resources res = getResources();

        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.back);

        canvas.drawBitmap(bmp, 0, 0, brush);
        canvas.drawPath(path, brush);
    }
}
