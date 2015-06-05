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
    private Collection<RoomInfo> rooms;
    private Collection<Particle> particles;
    private Point estimatedPoint;
    private static final float RADIUS = 5;
    private final Paint particlePaint = new Paint();
    private final Paint estimatedPaint = new Paint();
    private final Paint roomPaint = new Paint();

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

        estimatedPaint.setColor(Color.GREEN);
        estimatedPaint.setStrokeWidth(10);

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

    public void setEstimatedPoint(Point point) {
        this.estimatedPoint = point;
        this.invalidate();
    }

    /**
     * Resets the points on the floor plan image and refreshes the view.
     */
    public void clear() {
        this.particles = null;
        this.rooms = null;
        this.estimatedPoint = null;
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

        if(estimatedPoint != null) {
            Point pixel = locationToPixel(estimatedPoint);
            canvas.drawCircle(pixel.getXfl(), pixel.getYfl(), RADIUS*5, estimatedPaint);
        }
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
