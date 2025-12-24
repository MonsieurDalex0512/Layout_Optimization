package com.example.layout_optimization.utils;

import android.app.ActivityManager;
import android.content.Context;

public class MemoryTracker {
    private ActivityManager activityManager;
    private Context context;
    
    public MemoryTracker(Context context) {
        this.context = context;
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }
    
    /**
     * Get current app memory usage in MB
     */
    public float getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return usedMemory / (1024f * 1024f); // Convert to MB
    }
    
    /**
     * Get max available memory
     */
    public float getMaxMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() / (1024f * 1024f);
    }
    
    /**
     * Get available device memory
     */
    public float getAvailableMemory() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.availMem / (1024f * 1024f);
    }
    
    /**
     * Check if device is in low memory condition
     */
    public boolean isLowMemory() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.lowMemory;
    }
    
    /**
     * Get detailed memory info
     */
    public String getDetailedMemoryInfo() {
        return String.format(
            "App Memory: %.2f MB / %.2f MB\n" +
            "Device Available: %.2f MB\n" +
            "Low Memory: %s",
            getCurrentMemoryUsage(),
            getMaxMemory(),
            getAvailableMemory(),
            isLowMemory() ? "YES" : "NO"
        );
    }
}
