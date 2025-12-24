package com.example.layout_optimization.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.layout_optimization.R;
import com.example.layout_optimization.utils.LayoutAnalyzer;

public class ProfilerFragment extends Fragment {
    
    private TextView textHierarchyAnalysis;
    private TextView textOverdrawEstimate;
    private TextView textMeasurePassCount;
    private TextView textTraceStatus;
    private Button btnStartTrace, btnStopTrace, btnAnalyzeLayout;
    private Spinner spinnerLayoutType;
    
    private LayoutAnalyzer analyzer;
    private boolean isTracing = false;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiler, container, false);
        
        analyzer = new LayoutAnalyzer();
        
        // Initialize views
        initializeViews(view);
        
        // Setup controls
        setupControls();
        
        return view;
    }
    
    private void initializeViews(View view) {
        textHierarchyAnalysis = view.findViewById(R.id.text_hierarchy_analysis);
        textOverdrawEstimate = view.findViewById(R.id.text_overdraw_estimate);
        textMeasurePassCount = view.findViewById(R.id.text_measure_passes);
        textTraceStatus = view.findViewById(R.id.text_trace_status);
        btnStartTrace = view.findViewById(R.id.btn_start_trace);
        btnStopTrace = view.findViewById(R.id.btn_stop_trace);
        btnAnalyzeLayout = view.findViewById(R.id.btn_analyze_layout);
        spinnerLayoutType = view.findViewById(R.id.spinner_layout_type);
    }
    
    private void setupControls() {
        // Systrace controls
        btnStartTrace.setOnClickListener(v -> startSystemTrace());
        btnStopTrace.setOnClickListener(v -> stopSystemTrace());
        
        // Layout analysis
        btnAnalyzeLayout.setOnClickListener(v -> analyzeCurrentLayout());
        
        // Layout type selector
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            new String[]{"Layout Chưa Tối Ưu", "Layout Đã Tối Ưu"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLayoutType.setAdapter(adapter);
    }
    
    private void startSystemTrace() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                // Method tracing
                Debug.startMethodTracing("layout_optimization_trace");
                isTracing = true;
                textTraceStatus.setText("Đang ghi...");
                textTraceStatus.setTextColor(Color.RED);
                btnStartTrace.setEnabled(false);
                btnStopTrace.setEnabled(true);
                
                Toast.makeText(requireContext(), 
                    "Đã bắt đầu Trace. Hãy thao tác với ứng dụng...", 
                    Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể bắt đầu trace: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProfilerFragment", "Error starting trace", e);
            }
        }
    }
    
    private void stopSystemTrace() {
        if (isTracing) {
            try {
                Debug.stopMethodTracing();
                isTracing = false;
                textTraceStatus.setText("Không ghi");
                textTraceStatus.setTextColor(Color.GRAY);
                btnStartTrace.setEnabled(true);
                btnStopTrace.setEnabled(false);
                
                String tracePath = requireContext().getExternalFilesDir(null) + 
                    "/layout_optimization_trace.trace";
                
                Toast.makeText(requireContext(), 
                    "Đã lưu Trace. Tìm file .trace trong thư mục data ứng dụng.", 
                    Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                 Log.e("ProfilerFragment", "Error stopping trace", e);
            }
        }
    }
    
    private void analyzeCurrentLayout() {
        // Get layout type from spinner
        boolean isOptimized = spinnerLayoutType.getSelectedItemPosition() == 1;
        
        // Inflate layout for analysis
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int layoutId = isOptimized ? R.layout.item_optimized : R.layout.item_unoptimized;
        
        // Measure inflation time
        long startTime = System.nanoTime();
        View itemView = inflater.inflate(layoutId, null, false);
        long inflationTime = (System.nanoTime() - startTime) / 1_000_000;
        
        // Analyze hierarchy
        int viewCount = analyzer.countViews(itemView);
        int depth = analyzer.measureDepth(itemView);
        int measurePasses = analyzer.countMeasurePasses(itemView);
        int overdraw = analyzer.estimateOverdraw(itemView);
        String hierarchy = analyzer.analyzeHierarchy(itemView);
        
        // Display results
        StringBuilder results = new StringBuilder();
        results.append("KẾT QUẢ PHÂN TÍCH\n\n");
        results.append(String.format("Layout: %s\n", isOptimized ? "ĐÃ TỐI ƯU" : "CHƯA TỐI ƯU"));
        results.append(String.format("Thời gian Inflation: %dms\n", inflationTime));
        results.append(String.format("Số lượng View: %d\n", viewCount));
        results.append(String.format("Độ sâu Hierarchy: %d cấp\n", depth));
        results.append(String.format("Số lần Measure: %d\n", measurePasses));
        results.append(String.format("Overdraw ước tính: %dx\n\n", overdraw));
        
        results.append("CÂY HIERARCHY\n");
        results.append(hierarchy);
        
        textHierarchyAnalysis.setText(results.toString());
        textHierarchyAnalysis.setTextColor(isOptimized ? Color.parseColor("#006400") : Color.RED); // Dark Green or Red
        
        // Update individual metrics
        textOverdrawEstimate.setText(String.format("%dx overdraw", overdraw));
        textOverdrawEstimate.setTextColor(overdraw <= 2 ? Color.GREEN : Color.RED);
        
        textMeasurePassCount.setText(String.format("%d lần", measurePasses));
        textMeasurePassCount.setTextColor(measurePasses == 1 ? Color.GREEN : Color.RED);
        
        // Log results
        Log.i("ProfilerFragment", results.toString());
    }
}
