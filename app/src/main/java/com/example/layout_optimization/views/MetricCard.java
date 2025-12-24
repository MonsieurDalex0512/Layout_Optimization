package com.example.layout_optimization.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.layout_optimization.R;

public class MetricCard extends CardView {
    
    private TextView textTitle;
    private TextView textValue;
    
    public MetricCard(Context context) {
        super(context);
        init(context, null);
    }
    
    public MetricCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public MetricCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_metric_card, this, true);
        
        textTitle = findViewById(R.id.text_title);
        textValue = findViewById(R.id.text_value);
        
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MetricCard);
            String title = a.getString(R.styleable.MetricCard_title);
            String value = a.getString(R.styleable.MetricCard_value);
            a.recycle();
            
            if (title != null) textTitle.setText(title);
            if (value != null) textValue.setText(value);
        }
    }
    
    public void setTitle(String title) {
        textTitle.setText(title);
    }
    
    public void setValue(String value) {
        textValue.setText(value);
    }
}
