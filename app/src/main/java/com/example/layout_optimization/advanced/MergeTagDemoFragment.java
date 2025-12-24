package com.example.layout_optimization.advanced;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.layout_optimization.R;

public class MergeTagDemoFragment extends Fragment {
    
    private LinearLayout containerBad;
    private LinearLayout containerGood;
    private Button btnAnalyze;
    private TextView textResult;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merge_demo, container, false);
        
        containerBad = view.findViewById(R.id.container_bad);
        containerGood = view.findViewById(R.id.container_good);
        btnAnalyze = view.findViewById(R.id.btn_analyze);
        textResult = view.findViewById(R.id.text_result);
        
        btnAnalyze.setOnClickListener(v -> analyzeHierarchy());
        
        return view;
    }
    
    private void analyzeHierarchy() {
        // --- 1. PHÂN TÍCH CÁCH CŨ (BAD) ---
        StringBuilder badTree = new StringBuilder();
        int badChildCount = containerBad.getChildCount();
        
        badTree.append("🔴 CÁCH CŨ:\n");
        badTree.append("Container (LinearLayout)\n");
        
        if (badChildCount > 0) {
            View wrapper = containerBad.getChildAt(0);
            
            // Vẽ cây cho Wrapper
            badTree.append("  └── ").append(wrapper.getClass().getSimpleName()).append(" (WRAPPER THỪA!)\n");
            
            if (wrapper instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) wrapper;
                for(int i=0; i<vg.getChildCount(); i++) {
                     View child = vg.getChildAt(i);
                     String prefix = (i == vg.getChildCount()-1) ? "      └── " : "      ├── ";
                     badTree.append(prefix).append(child.getClass().getSimpleName()).append("\n");
                }
                
                // Highlight wrapper
                highlightRedundantView(wrapper);
            }
        } else {
            badTree.append("  (Trống)\n");
        }
        
        // --- 2. PHÂN TÍCH CÁCH MERGE (GOOD) ---
        StringBuilder goodTree = new StringBuilder();
        int goodChildCount = containerGood.getChildCount();
        
        goodTree.append("🟢 CÁCH TỐI ƯU (Merge):\n");
        goodTree.append("Container (LinearLayout)\n");
        
        for(int i=0; i<goodChildCount; i++) {
             View child = containerGood.getChildAt(i);
             String prefix = (i == goodChildCount-1) ? "  └── " : "  ├── ";
             goodTree.append(prefix).append(child.getClass().getSimpleName()).append("\n");
        }
        goodTree.append("\n(Đã loại bỏ hoàn toàn lớp trung gian!)");

        textResult.setText(badTree.toString() + "\n\n----------------------------\n\n" + goodTree.toString());
        textResult.setTextColor(Color.BLACK);
    }
    
    private void highlightRedundantView(View view) {
        view.setBackgroundColor(Color.parseColor("#FF5252")); // Đỏ cam đậm
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 5f, -5f, 5f, -5f, 0f);
        rotate.setDuration(500);
        rotate.start();
        Toast.makeText(getContext(), "Đã vẽ sơ đồ cây Layout!", Toast.LENGTH_SHORT).show();
    }
}
