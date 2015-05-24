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

import java.util.Collection;

/**
 * Main android activity for the application.
 *
 * @author Pierre Rousseau
 */
public class Visualisation extends View {

    protected Drawable floorPlanImage;
    private final android.graphics.Point screenSize = new android.graphics.Point();
    private Point totalDrawSize;
    private Point probabilisticPoint = new Point();
    private Point particlePoint = new Point();
    private Point inertialPoint = new Point();
    private Point bestPoint = new Point();
    private Collection<RoomInfo> rooms;
    private Collection<Particle> particles;
    private static final float RADIUS = 5;
    private final Paint probabilisticPaint = new Paint();
    private final Paint particlePaint = new Paint();
    private final Paint inertialPaint = new Paint();
    private final Paint roomPaint = new Paint();
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
        screenSize.y -= 300;

        particlePaint.setColor(Color.RED);
        roomPaint.setColor(Color.BLUE);
        roomPaint.setStyle(Paint.Style.STROKE);
        roomPaint.setStrokeWidth(5);

        probabilisticPaint.setColor(Color.RED);
        inertialPaint.setColor(Color.GREEN);

        clear();
    }

    /**
     * Assigns the floor plan image.
     * @param floorPlanImage
     */
    public void setFloorPlan(Drawable floorPlanImage) {
        this.floorPlanImage = floorPlanImage;
        floorPlanImage.setBounds(0, 0, screenSize.x, screenSize.y);
    }

    public void setRooms(Collection<RoomInfo> rooms) {
        this.rooms = rooms;
        this.invalidate();
    }

    public void setParticles(Collection<Particle> particles) {
        this.particles = particles;
        this.invalidate();
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

        if(floorPlanImage != null) {
            floorPlanImage.draw(canvas);
        }

        if(rooms != null) {
            for (RoomInfo r : rooms) {
                canvas.drawRect(r.getDrawArea(), roomPaint);
            }
        }

        if(particles != null) {
            for (Particle p : particles) {
                Point pixel = locationToPixel(p.getPoint());
                canvas.drawCircle(pixel.getXfl(), pixel.getYfl(), RADIUS, particlePaint);
            }
        }

        canvas.drawCircle(probabilisticPoint.getXfl(), probabilisticPoint.getYfl(), RADIUS, probabilisticPaint);
        canvas.drawCircle(particlePoint.getXfl(), particlePoint.getYfl(), RADIUS + 2, particlePaint);
        canvas.drawCircle(inertialPoint.getXfl(), inertialPoint.getYfl(), RADIUS, inertialPaint);
        canvas.drawCircle(bestPoint.getXfl(), bestPoint.getYfl(), RADIUS + 2, roomPaint);
    }

    private Point locationToPixel(Point p) {
        if (screenSize == null || totalDrawSize == null)
            return p;

        return new Point(
                p.getX() * (screenSize.x / totalDrawSize.getX()),
                p.getY() * (screenSize.y / totalDrawSize.getY())
        );
    }

    public android.graphics.Point getScreenSize() {
        return screenSize;
    }

    public void setTotalDrawSize(Point totalDrawSize) {
        this.totalDrawSize = totalDrawSize;
    }
}
