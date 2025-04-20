package com.example.agri_tech;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BallView extends View {

    private class Ball {
        float x, y, radius;
        float speedY, speedX;
        boolean hasLanded = false;
        boolean isExpanding = false;
        Paint paint;

        Ball(float x, float y, float radius, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;

            // Ball's downward speed — gravity-like fall
            this.speedY = new Random().nextInt(10) + 25;

            // Slight horizontal movement (optional) - mostly 0 to fall straight
            this.speedX = new Random().nextFloat() * 2 - 1;

            // Ball color and style
            this.paint = new Paint();
            this.paint.setColor(color);
            this.paint.setAntiAlias(true);
        }

        void update() {
            if (!hasLanded) {
                // Update vertical position
                y += speedY;

                // Simulate gravity
                speedY += 2.5f;  // ⭐ Fall Speed (Gravity Effect)

                // When hitting the bottom
                if (y >= getHeight() - radius) {
                    y = getHeight() - radius;
                    hasLanded = true;

                    // Bounce upward after hitting bottom
                    speedY = -(new Random().nextInt(60) + 35);  // ⭐ Bounce Speed
                }
            } else if (!isExpanding) {
                // While bouncing
                y += speedY;
                x += speedX;

                speedY += 2.2f;  // ⭐ Gravity during bounce
                speedX *= 0.95; // Friction on X

                // When hitting the bottom again
                if (y >= getHeight() - radius) {
                    y = getHeight() - radius;
                    speedY *= -0.4f; // Reduce bounce height

                    // Randomly trigger expansion
                    if (new Random().nextBoolean()) {
                        isExpanding = true;
                    }
                }
            } else {
                // ⭐ Expansion logic — faster now
                radius += 40;  // Increase radius quickly for fast expansion
            }
        }
    }

    private final List<Ball> balls = new ArrayList<>();
    private final Handler handler = new Handler();

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handler.postDelayed(updateRunnable, 16);
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            for (Ball b : balls) {
                b.update();
            }
            invalidate();
            handler.postDelayed(this, 16);
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        balls.clear();

        // Create balls that fall straight from top
        for (int i = 0; i < 10; i++) {
            float x = new Random().nextInt(w - 200) + 100; // ⭐ Ball fall centered
            float y = -new Random().nextInt(600); // Offscreen above

            // Big green ball
            balls.add(new Ball(x, y, 100, 0xFF1F8A70)); // Green color
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Ball b : balls) {
            canvas.drawCircle(b.x, b.y, b.radius, b.paint);
        }
    }
}
