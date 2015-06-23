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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerx = getWidth()/2;
        float centery = getHeight()/2;
        canvas.rotate((float) (compassAngle), centerx, centery);
        canvas.drawLine(centerx-100, centery, centerx+100, centery, compassPaint);
        canvas.drawLine(centerx+98, centery+2, centerx+70, centery-30, compassPaint);
        canvas.drawLine(centerx+98, centery-2, centerx+70, centery+30, compassPaint);

        /*  COMPASS
               270

          180        0

               90
         */
    }

    public void setCompassAngle(double angle) {
        compassAngle = angle;
        this.invalidate();
    }
}
