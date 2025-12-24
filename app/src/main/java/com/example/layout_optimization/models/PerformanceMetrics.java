package com.example.layout_optimization.models;

public class PerformanceMetrics {
    private float fps;
    private float avgFrameTime;
    private int jankCount;
    private float jankPercentage;
    private int totalFrames;
    
    public PerformanceMetrics(float fps, float avgFrameTime, int jankCount, 
                             float jankPercentage, int totalFrames) {
        this.fps = fps;
        this.avgFrameTime = avgFrameTime;
        this.jankCount = jankCount;
        this.jankPercentage = jankPercentage;
        this.totalFrames = totalFrames;
    }
    
    // Getters
    public float getFps() { return fps; }
    public float getAvgFrameTime() { return avgFrameTime; }
    public int getJankCount() { return jankCount; }
    public float getJankPercentage() { return jankPercentage; }
    public int getTotalFrames() { return totalFrames; }
    
    @Override
    public String toString() {
        return String.format(
            "FPS: %.1f | Frame Time: %.2fms | Jank: %d (%.1f%%)",
            fps, avgFrameTime, jankCount, jankPercentage
        );
    }
}
