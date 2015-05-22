package com.example.foodcourt.particles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Main android activity for the application.
 *
 * @author Pierre Rousseau
 */
public class Visualisation extends View {

    protected Drawable floorPlanImage;
    private final int STATUS_BAR = 100;
    private final android.graphics.Point screenSize = new android.graphics.Point();
    private Point probabilisticPoint = new Point();
    private Point particlePoint = new Point();
    private Point inertialPoint = new Point();
    private Point bestPoint = new Point();
    private static final float RADIUS = 5;
    private final Paint probabilisticPaint = new Paint();
    private final Paint particlePaint = new Paint();
    private final Paint inertialPaint = new Paint();
    private final Paint bestPaint = new Paint();
    private static final double X_PIXELS = 1280.0 / 53.4;
    private static final double Y_PIXELS = 630.0 / 24.0;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor
     * @param context
     * @param attrs 
     */
    public Visualisation(Context context, AttributeSet attrs) {

        super(context, attrs);

        //Adjust to fill screen - not caring about aspect ratio at current time but could be issue later.
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(this.screenSize);

        probabilisticPaint.setColor(Color.RED);
        particlePaint.setColor(Color.BLUE);
        inertialPaint.setColor(Color.GREEN);
        bestPaint.setColor(Color.MAGENTA);

    }

    /**
     * Assigns the floor plan image.
     * @param floorPlanImage 
     */
    public void setFloorPlan(Drawable floorPlanImage) {
        this.floorPlanImage = floorPlanImage;
        floorPlanImage.setBounds(0, 0, screenSize.x, screenSize.y - STATUS_BAR);
    }

    /**
     * Draws the latest points on the floor plan and refreshes the view.
     * @param probabilisticPoint
     * @param particlePoint
     * @param inertialPoint
     * @param corridorPoint 
     */
    public void setPoint(Point probabilisticPoint, Point particlePoint, Point inertialPoint, Point corridorPoint) {

        this.probabilisticPoint = new Point(probabilisticPoint.getX() * X_PIXELS, probabilisticPoint.getY() * Y_PIXELS);
        this.particlePoint = new Point(particlePoint.getX() * X_PIXELS, particlePoint.getY() * Y_PIXELS);
        this.inertialPoint = new Point(inertialPoint.getX() * X_PIXELS, inertialPoint.getY() * Y_PIXELS);
        this.bestPoint = new Point(corridorPoint.getX() * X_PIXELS, corridorPoint.getY() * Y_PIXELS);
        this.invalidate();
    }

    /**
     * Resets the points on the floor plan image and refreshes the view.
     */
    public void clear() {
        this.probabilisticPoint = new Point();
        this.particlePoint = new Point();
        this.inertialPoint = new Point();
        this.bestPoint = new Point();
        this.invalidate();
    }

    /**
     * Android onDraw.
     * @param canvas 
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        floorPlanImage.draw(canvas);

        canvas.drawCircle(probabilisticPoint.getXfl(), probabilisticPoint.getYfl(), RADIUS, probabilisticPaint);
        canvas.drawCircle(particlePoint.getXfl(), particlePoint.getYfl(), RADIUS + 2, particlePaint);
        canvas.drawCircle(inertialPoint.getXfl(), inertialPoint.getYfl(), RADIUS, inertialPaint);
        canvas.drawCircle(bestPoint.getXfl(), bestPoint.getYfl(), RADIUS + 2, bestPaint);
    }
}
