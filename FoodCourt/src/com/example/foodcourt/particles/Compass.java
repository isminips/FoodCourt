package com.example.foodcourt.particles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class Compass extends View {

    private final android.graphics.Point screenSize = new android.graphics.Point();
    private double compassAngle;
    private final Paint compassPaint = new Paint();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor
     * @param context
     * @param attrs
     */
    public Compass(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Adjust to fill screen - not caring about aspect ratio at current time but could be issue later.
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(this.screenSize);
        screenSize.y -= 300;

        compassPaint.setColor(Color.DKGRAY);
        compassPaint.setStrokeWidth(10);
        compassPaint.setTextSize(40);
    }

    /**
     * Android onDraw.
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerx = getWidth()/2;
        float centery = getHeight()/2;
        canvas.rotate((float) -compassAngle, centerx, centery);
        canvas.drawLine(centerx, centery+100, centerx, centery-100, compassPaint);
        canvas.drawText("N", centerx-13, centery - 115, compassPaint);
        canvas.drawText("S", centerx-13, centery + 135, compassPaint);
    }

    public void setCompassAngle(double angle) {
        compassAngle = angle;
        this.invalidate();
    }
}
