package com.example.layout_optimization.utils;

import java.util.LinkedList;

public class FPSMeter {
    private static final int FRAME_WINDOW = 60; // Calculate FPS over 60 frames
    
    private LinkedList<Long> frameTimes = new LinkedList<>();
    private long lastFrameTime = 0;
    
    public void recordFrame() {
        long currentTime = System.nanoTime();
        
        if (lastFrameTime != 0) {
            frameTimes.add(currentTime - lastFrameTime);
            
            // Keep only last FRAME_WINDOW frames
            if (frameTimes.size() > FRAME_WINDOW) {
                frameTimes.removeFirst();
            }
        }
        
        lastFrameTime = currentTime;
    }
    
    public float getCurrentFPS() {
        if (frameTimes.isEmpty()) {
            return 0;
        }
        
        long totalTime = 0;
        for (Long time : frameTimes) {
            totalTime += time;
        }
        float avgFrameTime = totalTime / (float) frameTimes.size();
        float avgFrameTimeSec = avgFrameTime / 1_000_000_000f;
        
        if (avgFrameTimeSec == 0) return 0;
        return 1.0f / avgFrameTimeSec;
    }
    
    public float getAverageFrameTime() {
        if (frameTimes.isEmpty()) {
            return 0;
        }
        
        long totalTime = 0;
        for (Long time : frameTimes) {
            totalTime += time;
        }
        
        return (totalTime / frameTimes.size()) / 1_000_000f; // ms
    }
    
    public void reset() {
        frameTimes.clear();
        lastFrameTime = 0;
    }
}
