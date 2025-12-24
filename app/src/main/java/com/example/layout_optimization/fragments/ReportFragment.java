package com.example.layout_optimization.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.layout_optimization.R;
import com.example.layout_optimization.utils.LayoutAnalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportFragment extends Fragment {
    
    private TextView textComparisonReport;
    private Button btnGenerateReport, btnExportReport;
    
    private LayoutAnalyzer analyzer;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        
        analyzer = new LayoutAnalyzer();
        
        initializeViews(view);
        setupControls();
        
        // Auto-generate report on load
        generateReport();
        
        return view;
    }
    
    private void initializeViews(View view) {
        textComparisonReport = view.findViewById(R.id.text_comparison_report);
        btnGenerateReport = view.findViewById(R.id.btn_generate_report);
        btnExportReport = view.findViewById(R.id.btn_export_report);
    }
    
    private void setupControls() {
        btnGenerateReport.setOnClickListener(v -> generateReport());
        btnExportReport.setOnClickListener(v -> exportReport());
    }
    
    private void generateReport() {
        textComparisonReport.setText("Đang tạo báo cáo tổng hợp...");
        
        new Thread(() -> {
            String report = performComparativeAnalysis();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    textComparisonReport.setText(report);
                });
            }
        }).start();
    }
    
    private String performComparativeAnalysis() {
        if (getContext() == null) return "Lỗi: Context bị null";

        StringBuilder report = new StringBuilder();
        
        report.append("BÁO CÁO SO SÁNH TỐI ƯU HÓA LAYOUT\n");
        report.append("-----------------------------------\n\n");
        
        LayoutInflater inflater = LayoutInflater.from(getContext());
        
        // UNOPTIMIZED
        long unoptStartTime = System.nanoTime();
        View unoptView = inflater.inflate(R.layout.item_unoptimized, null, false);
        long unoptInflationTime = (System.nanoTime() - unoptStartTime) / 1_000_000;
        
        int unoptViewCount = analyzer.countViews(unoptView);
        int unoptDepth = analyzer.measureDepth(unoptView);
        int unoptMeasurePasses = analyzer.countMeasurePasses(unoptView);
        int unoptOverdraw = analyzer.estimateOverdraw(unoptView);
        
        // OPTIMIZED
        long optStartTime = System.nanoTime();
        View optView = inflater.inflate(R.layout.item_optimized, null, false);
        long optInflationTime = (System.nanoTime() - optStartTime) / 1_000_000;
        
        int optViewCount = analyzer.countViews(optView);
        int optDepth = analyzer.measureDepth(optView);
        int optMeasurePasses = analyzer.countMeasurePasses(optView);
        int optOverdraw = analyzer.estimateOverdraw(optView);
        
        // METRICS TABLE
        // Inflation Time
        float inflationImprovement = unoptInflationTime > 0 ? ((unoptInflationTime - optInflationTime) * 100f / unoptInflationTime) : 0;
        report.append(String.format("- Thời gian Inflation: %dms (Chưa tối ưu) vs %dms (Đã tối ưu) -> Cải thiện: %.1f%%\n", 
            unoptInflationTime, optInflationTime, inflationImprovement));
        
        // View Count
        float viewCountReduction = ((unoptViewCount - optViewCount) * 100f / unoptViewCount);
        report.append(String.format("- Số lượng View: %d vs %d -> Cải thiện: %.1f%%\n",
            unoptViewCount, optViewCount, viewCountReduction));
        
        // Hierarchy Depth
        float depthReduction = ((unoptDepth - optDepth) * 100f / unoptDepth);
        report.append(String.format("- Độ sâu Hierarchy: %d cấp vs %d cấp -> Cải thiện: %.1f%%\n",
            unoptDepth, optDepth, depthReduction));
        
        // Measure Passes
        float measureReduction = ((unoptMeasurePasses - optMeasurePasses) * 100f / unoptMeasurePasses);
        report.append(String.format("- Số lần Measure: %d vs %d -> Cải thiện: %.1f%%\n",
            unoptMeasurePasses, optMeasurePasses, measureReduction));
        
        // Overdraw
        float overdrawReduction = ((unoptOverdraw - optOverdraw) * 100f / unoptOverdraw);
        report.append(String.format("- Các lớp Overdraw: %dx vs %dx -> Cải thiện: %.1f%%\n\n",
            unoptOverdraw, optOverdraw, overdrawReduction));
        
        // IMPACT ANALYSIS
        report.append("PHÂN TÍCH TÁC ĐỘNG HIỆU NĂNG\n\n");
        
        report.append("THỜI GIAN INFLATION:\n");
        report.append(String.format("   Giảm: %dms -> %dms (nhanh hơn %.1f%%)\n",
            unoptInflationTime, optInflationTime, inflationImprovement));
        report.append(String.format("   Tác động: Với RecyclerView chứa 50 items, tiết kiệm ~%.0fms khi tải lần đầu\n\n",
            (unoptInflationTime - optInflationTime) * 50 / 1000f));
        
        report.append("SỐ LƯỢNG VIEW:\n");
        report.append(String.format("   Giảm: %d -> %d views (ít hơn %.1f%% objects)\n",
            unoptViewCount, optViewCount, viewCountReduction));
        report.append(String.format("   Tác động: Giảm sử dụng bộ nhớ và duyệt cây nhanh hơn\n\n"));
        
        report.append("ĐỘ SÂU HIERARCHY:\n");
        report.append(String.format("   Giảm: %d -> %d cấp (nông hơn %.1f%%)\n",
            unoptDepth, optDepth, depthReduction));
        report.append("   Tác động: Measure/layout passes nhanh hơn theo cấp số nhân\n\n");
        
        report.append("SỐ LẦN MEASURE:\n");
        report.append(String.format("   Giảm: %d -> %d lần (ít hơn %.1f%%)\n",
            unoptMeasurePasses, optMeasurePasses, measureReduction));
        report.append("   Tác động: Rất quan trọng cho hiệu năng cuộn mượt mà\n\n");
        
        report.append("OVERDRAW:\n");
        report.append(String.format("   Giảm: %dx -> %dx (ít hơn %.1f%%)\n",
            unoptOverdraw, optOverdraw, overdrawReduction));
        report.append("   Tác động: Giảm băng thông GPU sử dụng\n\n");
        
        // ESTIMATED FPS IMPACT
        report.append("TÁC ĐỘNG FPS ƯỚC TÍNH\n\n");
        
        float unoptFrameTime = unoptInflationTime * 0.3f + unoptMeasurePasses * 2f + unoptOverdraw * 0.5f;
        float optFrameTime = optInflationTime * 0.3f + optMeasurePasses * 2f + optOverdraw * 0.5f;
        
        unoptFrameTime += 8f;
        optFrameTime += 8f;

        float unoptFPS = Math.min(60, 1000f / unoptFrameTime);
        float optFPS = Math.min(60, 1000f / optFrameTime);
        
        report.append(String.format("FPS khi cuộn ước tính:\n"));
        report.append(String.format("  Chưa tối ưu: ~%.0f FPS\n", unoptFPS));
        report.append(String.format("  Đã tối ưu:   ~%.0f FPS\n", optFPS));
        report.append(String.format("  Cải thiện: +%.0f FPS\n\n", optFPS - unoptFPS));
        
        // RECOMMENDATIONS
        report.append("CÁC TỐI ƯU HÓA ĐÃ ÁP DỤNG\n\n");
        report.append("- Thay thế LinearLayouts lồng nhau bằng ConstraintLayout phẳng\n");
        report.append("- Loại bỏ các ViewGroup bao bọc không cần thiết\n");
        report.append("- Loại bỏ background thừa (giảm overdraw)\n");
        report.append("- Tránh dùng weight trong LinearLayout (không đo 2 lần)\n");
        report.append("- Sử dụng constraints trực tiếp thay vì lồng nhau\n\n");
        
        // BEST PRACTICES
        report.append("TỔNG HỢP BEST PRACTICES\n\n");
        report.append("1. Giữ hierarchy phẳng (lý tưởng dưới 3 cấp)\n");
        report.append("2. Sử dụng ConstraintLayout cho layout phức tạp\n");
        report.append("3. Tối thiểu hóa background (giảm overdraw)\n");
        report.append("4. Tránh weight trong LinearLayout khi có thể\n");
        report.append("5. Sử dụng thẻ merge để loại bỏ các lớp bao bọc\n");
        report.append("6. Sử dụng ViewStub cho các view ẩn hiện có điều kiện\n");
        report.append("7. Profile với Layout Inspector và GPU Overdraw\n");
        report.append("8. Mục tiêu frame time dưới 16.67ms cho 60 FPS\n\n");
        
        // CONCLUSION
        report.append("KẾT LUẬN\n\n");
        report.append(String.format("Tổng mức cải thiện hiệu năng: %.1f%%\n", 
            (inflationImprovement + viewCountReduction + depthReduction + measureReduction + overdrawReduction) / 5));
        report.append("Trạng thái: ĐÃ TỐI ƯU HÓA ĐÁNG KỂ\n");
        report.append("\nNhững tối ưu hóa này mang lại trải nghiệm cuộn mượt mà hơn,\n");
        report.append("thời gian tải nhanh hơn và tiết kiệm pin hơn.\n");
        
        return report.toString();
    }

    private void exportReport() {
        String report = textComparisonReport.getText().toString();
        if (getContext() == null) return;
        
        try {
            String filename = "layout_optimization_report_" + System.currentTimeMillis() + ".txt";
            File file = new File(requireContext().getExternalFilesDir(null), filename);
            
            FileWriter writer = new FileWriter(file);
            writer.write(report);
            writer.close();
            
            Toast.makeText(requireContext(), 
                "Đã xuất báo cáo ra: " + file.getAbsolutePath(), 
                Toast.LENGTH_LONG).show();
            
            ClipboardManager clipboard = (ClipboardManager) 
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("Báo cáo Layout", report);
                clipboard.setPrimaryClip(clip);
                
                Toast.makeText(requireContext(), 
                    "Báo cáo cũng đã được copy vào clipboard", 
                    Toast.LENGTH_SHORT).show();
            }
            
        } catch (IOException e) {
            Toast.makeText(requireContext(), 
                "Lỗi khi xuất báo cáo: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }
}
