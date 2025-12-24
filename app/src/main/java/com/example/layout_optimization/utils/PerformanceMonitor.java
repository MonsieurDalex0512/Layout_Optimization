package com.example.layout_optimization.utils;

import android.os.Trace;
import android.util.Log;
import android.view.Choreographer;

import com.example.layout_optimization.models.PerformanceMetrics;

import java.util.ArrayList;
import java.util.List;

public class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";
    
    // SINGLETON INSTANCE
    private static PerformanceMonitor instance;
    
    // Metrics storage
    private List<Long> frameTimesNanos = new ArrayList<>();
    private int jankCount = 0;
    private int totalFrames = 0;
    private long startTime;
    private boolean isTracking = false;
    
    // Session Label (New feature)
    private String currentSessionMode = "Chưa xác định";
    
    // Choreographer for frame timing
    private Choreographer choreographer;
    private Choreographer.FrameCallback frameCallback;
    
    // Listeners
    private OnMetricsUpdateListener listener;
    
    public interface OnMetricsUpdateListener {
        void onMetricsUpdated(float fps, int jankCount, float avgFrameTime);
    }
    
    // Private constructor
    private PerformanceMonitor() {
        choreographer = Choreographer.getInstance();
    }
    
    // Global access point
    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }
    
    // --- NEW: Set Session Mode ---
    public void setSessionMode(String modeName) {
        this.currentSessionMode = modeName;
        // Optional: Auto-reset when mode changes to ensure clean data
        reset(); 
    }
    
    public String getSessionMode() {
        return currentSessionMode;
    }
    // -----------------------------
    
    public void startTracking() {
        if (isTracking) return;
        
        isTracking = true;
        if (startTime == 0) {
            reset();
        }
        
        try {
            Trace.beginSection("PerformanceMonitoring");
        } catch (Exception e) {}
        
        frameCallback = new Choreographer.FrameCallback() {
            private long lastFrameTime = 0;
            
            @Override
            public void doFrame(long frameTimeNanos) {
                if (!isTracking) return;
                
                if (lastFrameTime != 0) {
                    long frameTime = frameTimeNanos - lastFrameTime;
                    // Lọc frame time không hợp lệ (lớn hơn 500ms coi như pause/resume)
                    if (frameTime < 500_000_000) { 
                        frameTimesNanos.add(frameTime);
                        totalFrames++;
                        
                        float frameTimeMs = frameTime / 1_000_000f;
                        if (frameTimeMs > 16.67f) {
                            jankCount++;
                        }
                        
                        if (totalFrames % 10 == 0 && listener != null) {
                            float fps = calculateFPS();
                            float avgFrameTime = calculateAvgFrameTime();
                            listener.onMetricsUpdated(fps, jankCount, avgFrameTime);
                        }
                    } else {
                        // Reset lastFrameTime if gap is too big to avoid skewed stats
                        lastFrameTime = frameTimeNanos;
                        choreographer.postFrameCallback(this);
                        return;
                    }
                }
                
                lastFrameTime = frameTimeNanos;
                choreographer.postFrameCallback(this);
            }
        };
        
        choreographer.postFrameCallback(frameCallback);
    }
    
    public void stopTracking() {
        isTracking = false;
        if (frameCallback != null) {
            choreographer.removeFrameCallback(frameCallback);
        }
        try {
            Trace.endSection();
        } catch (Exception e) {}
    }
    
    public void reset() {
        startTime = System.nanoTime();
        jankCount = 0;
        totalFrames = 0;
        frameTimesNanos.clear();
        // Reset luôn listener để UI cập nhật về 0 ngay
        if (listener != null) {
             listener.onMetricsUpdated(0, 0, 0);
        }
    }
    
    private float calculateFPS() {
        if (frameTimesNanos.isEmpty()) return 60.0f; // Default ideal
        
        // Calculate based on recent window (e.g. last 60 frames ~ 1 sec)
        int windowSize = Math.min(frameTimesNanos.size(), 60);
        long totalTimeNs = 0;
        for (int i = 0; i < windowSize; i++) {
            totalTimeNs += frameTimesNanos.get(frameTimesNanos.size() - 1 - i);
        }
        
        if (totalTimeNs == 0) return 60.0f;

        float avgFrameTimeSec = (totalTimeNs / (float)windowSize) / 1_000_000_000f;
        if (avgFrameTimeSec == 0) return 60.0f;
        
        return 1.0f / avgFrameTimeSec;
    }
    
    private float calculateAvgFrameTime() {
        if (frameTimesNanos.isEmpty()) return 0;
        
        long sum = 0;
        int windowSize = Math.min(frameTimesNanos.size(), 100); // Last 100 frames avg
        for (int i = 0; i < windowSize; i++) {
            sum += frameTimesNanos.get(frameTimesNanos.size() - 1 - i);
        }
        return (sum / (float)windowSize) / 1_000_000f; // Convert to ms
    }
    
    public PerformanceMetrics getMetrics() {
        float fps = calculateFPS();
        float avgFrameTime = calculateAvgFrameTime();
        float jankPercentage = (totalFrames > 0) ? (jankCount * 100f / totalFrames) : 0;
        
        return new PerformanceMetrics(fps, avgFrameTime, jankCount, jankPercentage, totalFrames);
    }
    
    public void setListener(OnMetricsUpdateListener listener) {
        this.listener = listener;
    }
    
    public void recordJank() {
        jankCount++;
    }
}
