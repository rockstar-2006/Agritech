package com.example.agri_tech;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class RadarView extends View {
    private int angle = 0;
    private int distance = 0;

    private Paint gridPaint, sweepPaint, dotPaint, glowPaint, textPaint;
    private int radius;

    private final Handler handler = new Handler();
    private final Runnable sweepRunnable = new Runnable() {
        @Override
        public void run() {
            angle += 2;
            if (angle > 180) angle = 0;
            invalidate();
            handler.postDelayed(this, 20); // ~50 FPS
        }
    };

    public RadarView(Context context) {
        super(context);
        init();
    }

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.BLACK);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.GREEN);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2);

        sweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sweepPaint.setColor(Color.GREEN);
        sweepPaint.setStrokeWidth(3);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(Color.RED);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(40);

        handler.post(sweepRunnable);
    }

    public void updateRadar(int angle, int distance) {
        this.angle = angle;
        this.distance = distance;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Take radius based on width (to maintain circular shape)
        radius = width / 2;

        // Radar center: bottom-center of screen
        int cx = width / 2;
        int cy = height / 2;
        // Draw radar arcs (perfect semicircles)
        for (int i = 1; i <= 4; i++) {
            float r = (radius / 4f) * i;
            RectF arcRect = new RectF(cx - r, cy - r, cx + r, cy + r);
            canvas.drawArc(arcRect, 180, 180, false, gridPaint);
        }

        // Draw radial lines every 30 degrees
        for (int a = 0; a <= 180; a += 30) {
            float endX = (float) (cx + radius * Math.cos(Math.toRadians(a)));
            float endY = (float) (cy - radius * Math.sin(Math.toRadians(a)));
            canvas.drawLine(cx, cy, endX, endY, gridPaint);
        }

        // Draw sweep line
        float sweepX = (float) (cx + radius * Math.cos(Math.toRadians(angle)));
        float sweepY = (float) (cy - radius * Math.sin(Math.toRadians(angle)));
        canvas.drawLine(cx, cy, sweepX, sweepY, sweepPaint);

        // Draw detected point (if in range)
        if (distance > 0 && distance <= 40) {
            float mappedDistance = map(distance, 0, 40, 0, radius);
            float px = (float) (cx + mappedDistance * Math.cos(Math.toRadians(angle)));
            float py = (float) (cy - mappedDistance * Math.sin(Math.toRadians(angle)));

            canvas.drawCircle(px, py, 25, glowPaint); // Glow
            canvas.drawCircle(px, py, 10, dotPaint);  // Red dot
        }

        // Draw angle & distance at top-left
        canvas.drawText("Angle: " + angle, 40, 80, textPaint);
        canvas.drawText("Distance: " + distance + " cm", 40, 140, textPaint);
    }

    private float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
