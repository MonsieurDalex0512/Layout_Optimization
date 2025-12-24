package com.example.layout_optimization.utils;

import android.content.Context;
import android.os.Trace;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LayoutAnalyzer {
    private static final String TAG = "LayoutAnalyzer";
    
    /**
     * Recursively count all views in hierarchy
     */
    public int countViews(View view) {
        if (!(view instanceof ViewGroup)) {
            return 1;
        }
        
        ViewGroup group = (ViewGroup) view;
        int count = 1; // Count self
        
        for (int i = 0; i < group.getChildCount(); i++) {
            count += countViews(group.getChildAt(i));
        }
        
        return count;
    }
    
    /**
     * Measure maximum hierarchy depth
     */
    public int measureDepth(View view) {
        if (!(view instanceof ViewGroup)) {
            return 1;
        }
        
        ViewGroup group = (ViewGroup) view;
        int maxChildDepth = 0;
        
        for (int i = 0; i < group.getChildCount(); i++) {
            int childDepth = measureDepth(group.getChildAt(i));
            maxChildDepth = Math.max(maxChildDepth, childDepth);
        }
        
        return maxChildDepth + 1;
    }
    
    /**
     * Estimate measure passes (simplified)
     * LinearLayout with weights = 2 passes minimum
     * RelativeLayout = up to 2 passes
     * ConstraintLayout = 1 pass
     */
    public int countMeasurePasses(View view) {
        if (!(view instanceof ViewGroup)) {
            return 0;
        }
        
        int passes = 1; // Base measure
        ViewGroup group = (ViewGroup) view;
        
        // Check for LinearLayout with weights
        if (view instanceof LinearLayout) {
            LinearLayout ll = (LinearLayout) view;
            boolean hasWeights = false;
            
            for (int i = 0; i < ll.getChildCount(); i++) {
                View child = ll.getChildAt(i);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                if (lp.weight > 0) {
                    hasWeights = true;
                    break;
                }
            }
            
            if (hasWeights) {
                passes = 2; // Double measure
            }
        }
        
        // Check for RelativeLayout
        if (view instanceof RelativeLayout) {
            passes = 2; // Often requires 2 passes
        }
        
        // ConstraintLayout is optimal
        if (view.getClass().getSimpleName().contains("ConstraintLayout")) {
            passes = 1;
        }
        
        // Recursively add child passes
        int maxChildPasses = 0;
        for (int i = 0; i < group.getChildCount(); i++) {
            int childPasses = countMeasurePasses(group.getChildAt(i));
            maxChildPasses = Math.max(maxChildPasses, childPasses);
        }
        
        return passes + maxChildPasses;
    }
    
    /**
     * Measure actual inflation time
     */
    public long measureInflationTime(Context context, int layoutResId, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        
        // Add trace marker
        Trace.beginSection("inflate_" + context.getResources().getResourceEntryName(layoutResId));
        
        long startTime = System.nanoTime();
        inflater.inflate(layoutResId, parent, false);
        long endTime = System.nanoTime();
        
        Trace.endSection();
        
        long inflationTimeMs = (endTime - startTime) / 1_000_000;
        
        Log.d(TAG, String.format("Inflation time for %s: %dms", 
            context.getResources().getResourceEntryName(layoutResId), 
            inflationTimeMs));
        
        return inflationTimeMs;
    }
    
    /**
     * Analyze and log full hierarchy
     */
    public String analyzeHierarchy(View view) {
        StringBuilder sb = new StringBuilder();
        analyzeHierarchyRecursive(view, 0, sb);
        return sb.toString();
    }
    
    private void analyzeHierarchyRecursive(View view, int depth, StringBuilder sb) {
        // Indentation
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        
        // View info
        sb.append(view.getClass().getSimpleName());
        sb.append(" [").append(view.getId()).append("]");
        
        // Background info (overdraw indicator)
        if (view.getBackground() != null) {
            sb.append(" *HAS_BACKGROUND*");
        }
        
        sb.append("\n");
        
        // Recurse children
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                analyzeHierarchyRecursive(group.getChildAt(i), depth + 1, sb);
            }
        }
    }
    
    /**
     * Calculate overdraw estimation
     */
    public int estimateOverdraw(View view) {
        return estimateOverdrawRecursive(view, 0);
    }
    
    private int estimateOverdrawRecursive(View view, int currentOverdraw) {
        int overdraw = currentOverdraw;
        
        // Count background as overdraw
        if (view.getBackground() != null && view.getBackground().getOpacity() != android.graphics.PixelFormat.TRANSPARENT) {
            overdraw++;
        }
        
        // Recurse and find max overdraw
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int maxChildOverdraw = overdraw;
            
            for (int i = 0; i < group.getChildCount(); i++) {
                int childOverdraw = estimateOverdrawRecursive(group.getChildAt(i), overdraw);
                maxChildOverdraw = Math.max(maxChildOverdraw, childOverdraw);
            }
            
            return maxChildOverdraw;
        }
        
        return overdraw;
    }
}
