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

public class CustomViewDemoFragment extends Fragment {
    
    private LinearLayout containerXml, containerCustom;
    private TextView textResultXml, textResultCustom;
    private Button btnAnalyze;
    private LayoutAnalyzer analyzer;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_view_demo, container, false);
        
        analyzer = new LayoutAnalyzer();
        
        containerXml = view.findViewById(R.id.container_xml_layout);
        containerCustom = view.findViewById(R.id.container_custom_view);
        textResultXml = view.findViewById(R.id.text_result_xml);
        textResultCustom = view.findViewById(R.id.text_result_custom);
        btnAnalyze = view.findViewById(R.id.btn_analyze_custom);
        
        btnAnalyze.setOnClickListener(v -> analyze());
        
        return view;
    }
    
    private void analyze() {
        // Phân tích XML Layout
        // Note: containerXml chứa 1 view con là cái include layout
        View xmlLayoutRoot = containerXml.getChildAt(0); 
        int depthXml = analyzer.measureDepth(xmlLayoutRoot);
        int viewsXml = analyzer.countViews(xmlLayoutRoot);
        String hierarchyXml = analyzer.analyzeHierarchy(xmlLayoutRoot);
        
        textResultXml.setText(String.format(
            "Độ sâu: %d cấp\nSố lượng View: %d\n\nCấu trúc:\n%s", 
            depthXml, viewsXml, hierarchyXml));
            
        // Phân tích Custom View
        View customViewRoot = containerCustom.getChildAt(0);
        int depthCustom = analyzer.measureDepth(customViewRoot);
        int viewsCustom = analyzer.countViews(customViewRoot);
        String hierarchyCustom = analyzer.analyzeHierarchy(customViewRoot);
        
        textResultCustom.setText(String.format(
            "Độ sâu: %d cấp (Chỉ 1 cấp!)\nSố lượng View: %d (Chỉ 1 view!)\n\nCấu trúc:\n%s", 
            depthCustom, viewsCustom, hierarchyCustom));
    }
}
