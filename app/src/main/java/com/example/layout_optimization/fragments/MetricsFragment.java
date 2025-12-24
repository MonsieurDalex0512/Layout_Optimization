package com.example.layout_optimization.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.layout_optimization.R;
import com.example.layout_optimization.models.PerformanceMetrics;
import com.example.layout_optimization.utils.MemoryTracker;
import com.example.layout_optimization.utils.PerformanceMonitor;
import com.example.layout_optimization.views.LineChartView;

import java.util.ArrayList;
import java.util.List;

public class MetricsFragment extends Fragment {
    
    // Real-time metric displays
    private TextView textFPS, textAvgFrameTime, textJankCount, textJankPercent;
    private TextView textMemoryUsage;
    private TextView textSessionMode; // New
    private ProgressBar progressFPS, progressJank, progressMemory;
    private Button btnResetMetrics;
    
    // Charts
    private LineChartView fpsChart; 
    
    // Monitoring tools
    private PerformanceMonitor monitor;
    private MemoryTracker memoryTracker;
    private Handler updateHandler;
    private Runnable updateRunnable;
    
    // Data storage for charts
    private List<Float> fpsHistory = new ArrayList<>();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metrics, container, false);
        
        // Initialize views
        initializeViews(view);
        
        // Initialize monitoring - USE SHARED INSTANCE
        monitor = PerformanceMonitor.getInstance();
        memoryTracker = new MemoryTracker(requireContext());
        
        // Setup Button
        btnResetMetrics.setOnClickListener(v -> {
            monitor.reset();
            fpsHistory.clear();
            updateDashboard(); // Force UI update immediately
            Toast.makeText(getContext(), "Đã đặt lại bộ đếm Metrics!", Toast.LENGTH_SHORT).show();
        });

        // Setup real-time updates (every 500ms)
        setupRealTimeUpdates();
        
        monitor.startTracking();
        
        return view;
    }
    
    private void initializeViews(View view) {
        textSessionMode = view.findViewById(R.id.text_session_mode);
        textFPS = view.findViewById(R.id.text_fps);
        textAvgFrameTime = view.findViewById(R.id.text_avg_frame_time);
        textJankCount = view.findViewById(R.id.text_jank_count);
        textJankPercent = view.findViewById(R.id.text_jank_percent);
        textMemoryUsage = view.findViewById(R.id.text_memory);
        
        progressFPS = view.findViewById(R.id.progress_fps);
        progressJank = view.findViewById(R.id.progress_jank);
        progressMemory = view.findViewById(R.id.progress_memory);
        
        fpsChart = view.findViewById(R.id.fps_chart);
        btnResetMetrics = view.findViewById(R.id.btn_reset_metrics);
    }
    
    private void setupRealTimeUpdates() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateDashboard();
                updateHandler.postDelayed(this, 500); // Update every 500ms
            }
        };
        updateHandler.post(updateRunnable);
    }
    
    private void updateDashboard() {
        if (!isAdded()) return;

        // Get current metrics from SHARED monitor
        PerformanceMetrics metrics = monitor.getMetrics();
        float memoryUsage = memoryTracker.getCurrentMemoryUsage();
        float maxMemory = memoryTracker.getMaxMemory();
        
        // Update Session Mode Display
        String currentMode = monitor.getSessionMode();
        textSessionMode.setText("Đang đo dữ liệu của: " + currentMode);
        if (currentMode.contains("ĐÃ TỐI ƯU")) {
            textSessionMode.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (currentMode.contains("CHƯA TỐI ƯU")) {
            textSessionMode.setTextColor(Color.parseColor("#F44336")); // Red
        } else {
            textSessionMode.setTextColor(Color.GRAY);
        }

        // Update FPS
        textFPS.setText(String.format("%.1f FPS", metrics.getFps()));
        progressFPS.setProgress((int) (metrics.getFps() * 100 / 60)); // 60 FPS = 100%
        setFPSColor(textFPS, metrics.getFps());
        
        // Add to history
        fpsHistory.add(metrics.getFps());
        if (fpsHistory.size() > 50) {
            fpsHistory.remove(0);
        }
        
        // Update chart
        if (fpsChart != null) {
            fpsChart.updateData(fpsHistory);
        }
        
        // Update frame time
        textAvgFrameTime.setText(String.format("%.2f ms", metrics.getAvgFrameTime()));
        setFrameTimeColor(textAvgFrameTime, metrics.getAvgFrameTime());
        
        // Update jank metrics
        textJankCount.setText(String.valueOf(metrics.getJankCount()));
        textJankPercent.setText(String.format("%.1f%%", metrics.getJankPercentage()));
        progressJank.setProgress((int) metrics.getJankPercentage());
        
        // Update memory
        textMemoryUsage.setText(String.format("%.1f / %.1f MB", memoryUsage, maxMemory));
        progressMemory.setProgress((int) (memoryUsage * 100 / maxMemory));
    }
    
    private void setFPSColor(TextView textView, float fps) {
        if (fps >= 55) {
            textView.setTextColor(Color.GREEN);
        } else if (fps >= 40) {
            textView.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else {
            textView.setTextColor(Color.RED);
        }
    }
    
    private void setFrameTimeColor(TextView textView, float frameTime) {
        if (frameTime <= 16.67f) {
            textView.setTextColor(Color.GREEN);
        } else if (frameTime <= 33f) {
            textView.setTextColor(Color.parseColor("#FFA500"));
        } else {
            textView.setTextColor(Color.RED);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
}
