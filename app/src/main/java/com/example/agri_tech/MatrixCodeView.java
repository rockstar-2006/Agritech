package com.example.agri_tech;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class MatrixCodeView extends View {

    private char[] characters = "アァイィウヴエカガキギクグケゲコゴサザシジスズセゼソゾタチヅテデトナニヌネノハバパヒビピフブプヘベペホマミムメモヤユヨラリルレロワヲンABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private int textSize = 26;
    private int columns;
    private float[] yPositions;
    private Paint paint = new Paint();
    private Random random = new Random();

    public MatrixCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.parseColor("#39FF14")); // Matrix green
        paint.setTextSize(textSize);
        paint.setFakeBoldText(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        columns = w / textSize;
        yPositions = new float[columns];
        for (int i = 0; i < columns; i++) {
            yPositions[i] = random.nextInt(h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK); // Background black
        for (int i = 0; i < columns; i++) {
            char c = characters[random.nextInt(characters.length)];
            canvas.drawText(String.valueOf(c), i * textSize, yPositions[i], paint);
            yPositions[i] += textSize;

            if (yPositions[i] > getHeight() || random.nextInt(100) > 95) {
                yPositions[i] = 0;
            }
        }
        postInvalidateDelayed(30);
    }
}
