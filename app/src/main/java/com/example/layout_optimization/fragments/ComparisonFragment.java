package com.example.layout_optimization.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layout_optimization.R;
import com.example.layout_optimization.advanced.MergeTagDemoFragment;
import com.example.layout_optimization.advanced.ViewStubDemoFragment;
import com.example.layout_optimization.adapters.OptimizedAdapter;
import com.example.layout_optimization.adapters.UnoptimizedAdapter;
import com.example.layout_optimization.models.Item;
import com.example.layout_optimization.models.PerformanceMetrics;
import com.example.layout_optimization.utils.LayoutAnalyzer;
import com.example.layout_optimization.utils.PerformanceMonitor;
import com.example.layout_optimization.views.MetricCard;

import java.util.ArrayList;
import java.util.List;

public class ComparisonFragment extends Fragment {
    private RecyclerView recyclerView;
    private Switch switchOptimize;
    private Button btnMeasure;
    private Button btnAdvanced; // New Button
    private UnoptimizedAdapter unoptimizedAdapter;
    private OptimizedAdapter optimizedAdapter;
    
    // Metric Cards
    private MetricCard metricInflation, metricViewCount, metricDepth;
    private MetricCard metricMeasure, metricLayout, metricDraw;
    private TextView textAnalysis;
    
    // Realtime Metrics Views
    private TextView rtFps, rtFrameTime, rtJank;
    private Handler updateHandler;
    private Runnable updateRunnable;
    
    private PerformanceMonitor monitor;
    private LayoutAnalyzer analyzer;
    
    private List<Item> dummyItems;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comparison, container, false);
        
        // Initialize tools
        analyzer = new LayoutAnalyzer();
        monitor = PerformanceMonitor.getInstance();
        
        // Setup data
        dummyItems = createDummyItems();
        
        // Initialize Views
        recyclerView = view.findViewById(R.id.recycler_comparison);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        switchOptimize = view.findViewById(R.id.switch_optimize);
        btnMeasure = view.findViewById(R.id.btn_measure);
        textAnalysis = view.findViewById(R.id.text_analysis);
        btnAdvanced = view.findViewById(R.id.btn_open_advanced);
        
        // Realtime Metrics Views
        rtFps = view.findViewById(R.id.rt_fps);
        rtFrameTime = view.findViewById(R.id.rt_frame_time);
        rtJank = view.findViewById(R.id.rt_jank);
        
        // Metric Cards
        metricInflation = view.findViewById(R.id.metric_inflation);
        metricViewCount = view.findViewById(R.id.metric_view_count);
        metricDepth = view.findViewById(R.id.metric_depth);
        metricMeasure = view.findViewById(R.id.metric_measure);
        metricLayout = view.findViewById(R.id.metric_layout);
        metricDraw = view.findViewById(R.id.metric_draw);
        
        // Initialize Adapters
        unoptimizedAdapter = new UnoptimizedAdapter(getContext(), dummyItems);
        optimizedAdapter = new OptimizedAdapter(getContext(), dummyItems);
        
        // Setup Listeners
        switchOptimize.setOnCheckedChangeListener((buttonView, isChecked) -> switchAdapter(isChecked));
        
        btnMeasure.setOnClickListener(v -> {
            boolean isOptimized = switchOptimize.isChecked();
            switchAdapter(isOptimized);
        });
        
        // --- HANDLE ADVANCED MENU BUTTON CLICK ---
        btnAdvanced.setOnClickListener(this::showAdvancedMenu);
        // -----------------------------------------
        
        // Setup Realtime Polling Loop
        setupRealtimeUpdates();
        
        // Initial load check
        if (savedInstanceState == null) {
            boolean current = switchOptimize.isChecked();
            switchOptimize.setText(current ? "Đã tối ưu" : "Chưa tối ưu");
            monitor.setSessionMode(current ? "ĐÃ TỐI ƯU" : "CHƯA TỐI ƯU");
            switchAdapter(current);
        }
        
        return view;
    }
    
    private void setupRealtimeUpdates() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateRealtimeMetrics();
                updateHandler.postDelayed(this, 200); // Update every 200ms for responsiveness
            }
        };
    }
    
    private void updateRealtimeMetrics() {
        if (!isAdded()) return;
        // DEFENSIVE CHECK: Ensure everything is ready
        if (monitor == null || rtFps == null || rtFrameTime == null || rtJank == null) return;
        
        PerformanceMetrics metrics = monitor.getMetrics();
        if (metrics == null) return;
        
        // Update FPS
        float fps = metrics.getFps();
        rtFps.setText(String.format("%.0f", fps));
        if (fps >= 55) rtFps.setTextColor(Color.parseColor("#4CAF50")); // Green
        else if (fps >= 40) rtFps.setTextColor(Color.parseColor("#FFA500")); // Orange
        else rtFps.setTextColor(Color.parseColor("#F44336")); // Red
        
        // Update Frame Time
        float frameTime = metrics.getAvgFrameTime();
        rtFrameTime.setText(String.format("%.1f ms", frameTime));
        
        // Update Jank
        int jank = metrics.getJankCount();
        rtJank.setText(String.format("%d", jank));
        if (jank > 0) rtJank.setTextColor(Color.parseColor("#F44336"));
        else rtJank.setTextColor(Color.parseColor("#4CAF50"));
    }
    
    private void showAdvancedMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            Fragment demoFragment = null;
            int id = item.getItemId();
            
            if (id == R.id.action_viewstub) {
                 demoFragment = new ViewStubDemoFragment();
            } else if (id == R.id.action_merge) {
                 demoFragment = new MergeTagDemoFragment();
            }
            
            if (demoFragment != null) {
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, demoFragment)
                    .addToBackStack(null)
                    .commit();
                return true;
            }
            return false;
        });
        popup.show();
    }
    
    private List<Item> createDummyItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            items.add(new Item(
                "Mục #" + i,
                "Mô tả dài để test hiệu năng measure layout. Layout chưa tối ưu sẽ bị lag khi scroll nhanh danh sách này.",
                0, // Đã xóa icon (thay bằng 0)
                "Vừa xong"
            ));
        }
        return items;
    }
    
    private void switchAdapter(boolean optimized) {
        monitor.stopTracking(); 

        switchOptimize.setText(optimized ? "Đã tối ưu" : "Chưa tối ưu");
        String modeName = optimized ? "ĐÃ TỐI ƯU" : "CHƯA TỐI ƯU";
        
        monitor.reset();
        monitor.setSessionMode(modeName);

        recyclerView.setAdapter(null);
        
        // Setup Scroll Listener để chỉ đo khi cuộn
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    monitor.stopTracking();
                } else {
                    monitor.startTracking();
                }
            }
        });
        
        long startTime = System.nanoTime();
        
        if (optimized) {
            recyclerView.setAdapter(optimizedAdapter);
            textAnalysis.setText("Chế độ: ĐÃ TỐI ƯU (ConstraintLayout, Hierarchy Phẳng)");
        } else {
            recyclerView.setAdapter(unoptimizedAdapter);
            textAnalysis.setText("Chế độ: CHƯA TỐI ƯU (Nested LinearLayouts, Weights)");
        }
        
        long inflationTime = (System.nanoTime() - startTime) / 1_000_000;
        
        updateMetrics(optimized, inflationTime);
    }
    
    private void updateMetrics(boolean optimized, long inflationTime) {
        recyclerView.post(() -> {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
            if (holder != null) {
                View itemView = holder.itemView;
                int viewCount = analyzer.countViews(itemView);
                int depth = analyzer.measureDepth(itemView);
                int measurePasses = analyzer.countMeasurePasses(itemView);
                
                // BENCHMARK THỰC TẾ (Real Benchmark)
                float avgLayoutTime = benchmarkMeasureLayout(itemView);
                int overdraw = analyzer.estimateOverdraw(itemView);
                
                metricInflation.setValue(inflationTime + "ms");
                metricViewCount.setValue(String.valueOf(viewCount));
                metricDepth.setValue(depth + " cấp");
                metricMeasure.setValue(measurePasses + " lần");
                
                // Cập nhật số liệu thực
                metricLayout.setTitle("T.gian Layout (Avg)");
                metricLayout.setValue(String.format("%.2f ms", avgLayoutTime));
                
                // Thay thế Draw Time bằng Overdraw Score
                metricDraw.setTitle("Điểm Overdraw");
                metricDraw.setValue(overdraw + "x");
            }
        });
    }
    
    // Hàm benchmark thực tế: Chạy Measure/Layout 50 lần để lấy trung bình
    private float benchmarkMeasureLayout(View view) {
        long totalTime = 0;
        int iterations = 50;
        
        // Giả lập specs như trong RecyclerView (Width cố định, Height wrap)
        int widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        
        for (int i = 0; i < iterations; i++) {
            view.forceLayout(); // Bắt buộc layout lại
            long start = System.nanoTime();
            view.measure(widthSpec, heightSpec);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            totalTime += (System.nanoTime() - start);
        }
        
        // Trả về trung bình (ms)
        return (totalTime / (float)iterations) / 1_000_000f;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (updateHandler != null) {
            updateHandler.post(updateRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
}
