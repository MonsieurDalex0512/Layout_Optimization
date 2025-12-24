package com.example.layout_optimization.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class UserCardView extends View {

    private Paint paintCircle;
    private Paint paintTextName;
    private Paint paintTextStatus;
    private Paint paintButton;
    private Paint paintButtonText;

    public UserCardView(Context context) {
        super(context);
        init();
    }

    public UserCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setColor(Color.parseColor("#4CAF50")); // Green circle

        paintTextName = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextName.setColor(Color.BLACK);
        paintTextName.setTextSize(40f); // Fixed: Use setTextSize method
        paintTextName.setFakeBoldText(true);

        paintTextStatus = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextStatus.setColor(Color.GRAY);
        paintTextStatus.setTextSize(30f); // Fixed: Use setTextSize method
        
        paintButton = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintButton.setColor(Color.LTGRAY);
        paintButton.setStyle(Paint.Style.STROKE);
        paintButton.setStrokeWidth(2f);
        
        paintButtonText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintButtonText.setColor(Color.BLUE);
        paintButtonText.setTextSize(35f); // Fixed: Use setTextSize method
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Fix height to simulate the XML layout height (~80dp)
        int desiredHeight = 240; // pixels approx
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, desiredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw Avatar Circle
        float cy = getHeight() / 2f;
        float cx = 80f;
        float radius = 50f;
        canvas.drawCircle(cx, cy, radius, paintCircle);
        
        // Draw Name
        float textX = 160f;
        float nameY = cy - 10f;
        canvas.drawText("User Name (Custom View)", textX, nameY, paintTextName);
        
        // Draw Status
        float statusY = cy + 30f;
        canvas.drawText("Online Status", textX, statusY, paintTextStatus);
        
        // Draw Fake Button
        float btnX = getWidth() - 200f;
        canvas.drawRect(btnX, cy - 30f, btnX + 150f, cy + 30f, paintButton);
        canvas.drawText("Follow", btnX + 30f, cy + 10f, paintButtonText);
    }
}
