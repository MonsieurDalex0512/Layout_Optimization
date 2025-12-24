package com.example.layout_optimization.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * View này cố tình làm chậm quá trình vẽ (Render) để giả lập tải nặng.
 * Sử dụng để test hiệu năng trên các máy cấu hình cao.
 */
public class SlowView extends View {
    
    public SlowView(Context context) {
        super(context);
    }
    
    public SlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Kích thước nhỏ thôi, chủ yếu là để chạy logic onDraw
        setMeasuredDimension(1, 1);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // --- GIẢ LẬP RENDERING CỰC NẶNG ---
        // Sleep 5ms mỗi lần vẽ View này.
        // Nếu layout có 10 items hiển thị -> tổng delay = 50ms -> 20 FPS
        try {
            Thread.sleep(5); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
