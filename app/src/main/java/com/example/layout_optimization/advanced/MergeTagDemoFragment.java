package com.example.layout_optimization.advanced;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.layout_optimization.R;
import com.example.layout_optimization.utils.LayoutAnalyzer;

public class MergeTagDemoFragment extends Fragment {
    
    private LinearLayout containerBad, containerGood;
    private TextView textResultBad, textResultGood;
    private Button btnAnalyze;
    private LayoutAnalyzer analyzer;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merge_demo, container, false);
        
        analyzer = new LayoutAnalyzer();
        
        containerBad = view.findViewById(R.id.container_bad);
        containerGood = view.findViewById(R.id.container_good);
        textResultBad = view.findViewById(R.id.text_result_bad);
        textResultGood = view.findViewById(R.id.text_result_good);
        btnAnalyze = view.findViewById(R.id.btn_analyze_merge);
        
        btnAnalyze.setOnClickListener(v -> analyze());
        
        return view;
    }
    
    private void analyze() {
        // Phân tích Bad Container
        int depthBad = analyzer.measureDepth(containerBad);
        int viewsBad = analyzer.countViews(containerBad);
        String hierarchyBad = analyzer.analyzeHierarchy(containerBad);
        
        textResultBad.setText(String.format(
            "Độ sâu: %d cấp\nSố lượng View: %d\n\nCấu trúc:\n%s", 
            depthBad, viewsBad, hierarchyBad));
            
        // Phân tích Good Container
        int depthGood = analyzer.measureDepth(containerGood);
        int viewsGood = analyzer.countViews(containerGood);
        String hierarchyGood = analyzer.analyzeHierarchy(containerGood);
        
        textResultGood.setText(String.format(
            "Độ sâu: %d cấp (Nông hơn!)\nSố lượng View: %d (Ít hơn!)\n\nCấu trúc:\n%s", 
            depthGood, viewsGood, hierarchyGood));
    }
}
