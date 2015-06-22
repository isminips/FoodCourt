package com.example.foodcourt.particles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.example.foodcourt.LocalizationActivity;

import java.util.Collection;
import java.util.List;

public class Visualisation extends View {

    protected Drawable floorPlanImage;
    private final android.graphics.Point screenSize = new android.graphics.Point();
    private Point totalDrawSize;
    private Collection<RoomInfo> rooms;
    private Cloud cloud;
    private String estimatedRoomRSSI;
    private RoomInfo estimatedRoom;
    private static final float RADIUS = 5;
    private final Paint particlePaint = new Paint();
    private final Paint estimatedPaint = new Paint();
    private final Paint roomPaint = new Paint();
    private Bitmap rssiIndicator;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        estimatedPaint.setTextSize(50);

        rssiIndicator = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_map);

        clear();
    }

    public void setFloorPlan(Drawable floorPlanImage) {
        this.floorPlanImage = floorPlanImage;
        floorPlanImage.setBounds(0, 0, screenSize.x, screenSize.y);
    }

    public void setRooms(Collection<RoomInfo> rooms) {
        this.rooms = rooms;
    }

    public void setCloud(Cloud cloud) {
        this.cloud = cloud;
    }

    public void setEstimatedRoom(RoomInfo room) {
        estimatedRoom = room;
    }

    public void setEstimatedRoomRSSI(String room) {
        estimatedRoomRSSI = room;
    }

    /**
     * Resets the points on the floor plan image and refreshes the view.
     */
    public void clear() {
        this.cloud = null;
        this.rooms = null;
        this.estimatedRoom = null;
        this.estimatedRoomRSSI = null;
        this.update();
    }

    public void update() {
        this.invalidate();
    }

    /**
     * Android onDraw.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        if(floorPlanImage != null) {
            floorPlanImage.draw(canvas);
        }

        // Draw the room borders
        if(rooms != null) {
            for (RoomInfo r : rooms) {
                if (r.isRoom() || (r.isAisle() && !r.isAislePlaceholder()))
                    canvas.drawRect(r.getDrawArea(), roomPaint);

                // Draw indicator for room estimated by RSSI
                if (estimatedRoomRSSI != null && estimatedRoomRSSI.length() != 0 && !r.isBlocked() && r.getName().equals(estimatedRoomRSSI)) {
                    canvas.drawBitmap(
                            rssiIndicator,
                            r.getDrawArea().left + (r.getDrawArea().width()/2) - (rssiIndicator.getWidth()/2),
                            r.getDrawArea().top + (r.getDrawArea().height()/2) - (rssiIndicator.getHeight()/2),
                            estimatedPaint);
                }
            }
        }

        if(cloud != null) {
            // Draw all particles
            for (Particle p : cloud.getParticles()) {
                Point pixel = locationToPixel(p.getPoint());
                canvas.drawCircle(pixel.getXfl(), pixel.getYfl(), RADIUS, particlePaint);
            }

            // Draw the estimated point (by particles) with the size bigger as the spread decreases
            Point pixel = locationToPixel(cloud.getEstimatedPosition());
            double spread = cloud.getSpread();
            if (spread < LocalizationActivity.CONVERGENCE_SIZE) {
                estimatedPaint.setColor(Color.GREEN);
            } else {
                estimatedPaint.setColor(0xFFFFCC00);
            }
            canvas.drawCircle(pixel.getXfl(), pixel.getYfl(), RADIUS * Math.min(10, Math.max(3, (float)(40 / spread))), estimatedPaint);
        }


        // Write the estimated room (by particles) in the upper right corner
        if(estimatedRoom != null) {
            canvas.drawText("Room:", getWidth() - getWidth() / 7, 50, estimatedPaint);
            canvas.drawText(estimatedRoom.getName(), getWidth() - getWidth() / 7, 150, estimatedPaint);
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
