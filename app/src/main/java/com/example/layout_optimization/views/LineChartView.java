package com.example.layout_optimization.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {
    private List<Float> dataPoints = new ArrayList<>();
    private Paint linePaint;
    private Paint backgroundPaint;
    private Path path;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(2f);

        path = new Path();
    }

    public void updateData(List<Float> newData) {
        this.dataPoints = new ArrayList<>(newData);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints == null || dataPoints.size() < 2) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float padding = 20f;

        float maxVal = 65f; // Expecting FPS up to 60 usually
        float minVal = 0f;

        float xStep = (width - 2 * padding) / (dataPoints.size() - 1);
        float yScale = (height - 2 * padding) / (maxVal - minVal);

        path.reset();
        
        // Draw baseline (60 FPS)
        float y60 = height - padding - (60 * yScale);
        canvas.drawLine(padding, y60, width - padding, y60, backgroundPaint);

        path.moveTo(padding, height - padding - (dataPoints.get(0) * yScale));

        for (int i = 1; i < dataPoints.size(); i++) {
            float x = padding + i * xStep;
            float y = height - padding - (dataPoints.get(i) * yScale);
            path.lineTo(x, y);
        }

        canvas.drawPath(path, linePaint);
    }
}
