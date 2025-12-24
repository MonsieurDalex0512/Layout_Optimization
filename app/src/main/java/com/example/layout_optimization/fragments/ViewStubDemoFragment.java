package com.example.layout_optimization.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.layout_optimization.R;

public class ViewStubDemoFragment extends Fragment {
    
    private LinearLayout contentContainer;
    private TextView textInflationTime;
    private Button btnLoadEager, btnLoadLazy, btnShowPanel, btnReset;
    
    // State Tracking
    private boolean isEagerMode = false;
    private View hiddenEagerView;
    private ViewStub hiddenStub;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewstub_demo, container, false);
        initializeViews(view);
        setupControls();
        
        // ĐỂ NÚT HIỆN SẴN (nhưng disable) để người dùng dễ thấy
        btnShowPanel.setVisibility(View.VISIBLE);
        btnShowPanel.setEnabled(false);
        
        return view;
    }
    
    private void initializeViews(View view) {
        contentContainer = view.findViewById(R.id.content_container);
        textInflationTime = view.findViewById(R.id.text_inflation_time);
        
        btnLoadEager = view.findViewById(R.id.btn_load_eager);
        btnLoadLazy = view.findViewById(R.id.btn_load_lazy);
        btnShowPanel = view.findViewById(R.id.btn_show_panel);
        btnReset = view.findViewById(R.id.btn_reset);
    }
    
    private void setupControls() {
        btnLoadEager.setOnClickListener(v -> setupEager());
        btnLoadLazy.setOnClickListener(v -> setupLazy());
        btnShowPanel.setOnClickListener(v -> showPanel());
        btnReset.setOnClickListener(v -> resetDemo());
    }
    
    private void resetDemo() {
        if (contentContainer != null) {
            contentContainer.removeAllViews();
        }
        hiddenEagerView = null;
        hiddenStub = null;
        
        // Reset trạng thái nút
        btnShowPanel.setVisibility(View.VISIBLE);
        btnShowPanel.setEnabled(false);
        btnShowPanel.setText("Hiện Panel VIP (User Click)");
        btnShowPanel.setAlpha(0.5f); 
        
        textInflationTime.setText("Hãy chọn Bước 1 để bắt đầu...");
        textInflationTime.setTextColor(Color.BLACK);
        textInflationTime.setBackgroundColor(Color.parseColor("#EEEEEE"));
    }
    
    private void setupEager() {
        resetDemo();
        isEagerMode = true;
        
        long startTime = System.nanoTime();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        
        try {
            hiddenEagerView = inflater.inflate(R.layout.heavy_complex_view, contentContainer, false);
            hiddenEagerView.setVisibility(View.GONE);
            contentContainer.addView(hiddenEagerView);
            
            long inflationTime = (System.nanoTime() - startTime) / 1_000_000;
            
            String hierarchyReport = getHierarchyReport();
            
            // CODE SNIPPET (Clean)
            String codeSnippet = 
                "<include layout=\"@layout/heavy_view\"\n" +
                "         android:visibility=\"gone\" />";
            
            textInflationTime.setText(String.format(
                "[CACH CU: VISIBILITY GONE]\n" +
                "Thoi gian khoi dong: %dms (Lang phi)\n\n" +
                "--- CODE XML ---\n%s\n\n" +
                "--- HIERARCHY THUC TE ---\n%s", 
                inflationTime, codeSnippet, hierarchyReport));
                
            textInflationTime.setTextColor(Color.RED);
            textInflationTime.setBackgroundColor(Color.parseColor("#FFEBEE"));
            
            btnShowPanel.setEnabled(true);
            btnShowPanel.setAlpha(1.0f);
            Toast.makeText(getContext(), "Đã xong Bước 1.", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            textInflationTime.setText("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupLazy() {
        resetDemo();
        isEagerMode = false;
        
        long startTime = System.nanoTime();
        
        hiddenStub = new ViewStub(getContext());
        hiddenStub.setLayoutResource(R.layout.heavy_complex_view);
        contentContainer.addView(hiddenStub);
        
        long inflationTime = (System.nanoTime() - startTime) / 1_000_000;
        
        String hierarchyReport = getHierarchyReport();
        
        // CODE SNIPPET (Clean)
        String codeSnippet = 
            "<ViewStub android:id=\"@+id/stub\"\n" +
            "          android:layout=\"@layout/heavy_view\" />";
        
        textInflationTime.setText(String.format(
            "[CACH MOI: VIEWSTUB]\n" +
            "Thoi gian khoi dong: %dms (Toi uu)\n\n" +
            "--- CODE XML ---\n%s\n\n" +
            "--- HIERARCHY THUC TE ---\n%s", 
            inflationTime, codeSnippet, hierarchyReport));
            
        textInflationTime.setTextColor(Color.parseColor("#2E7D32"));
        textInflationTime.setBackgroundColor(Color.parseColor("#E8F5E9"));
        
        btnShowPanel.setEnabled(true);
        btnShowPanel.setAlpha(1.0f);
        Toast.makeText(getContext(), "Đã xong Bước 1.", Toast.LENGTH_SHORT).show();
    }
    
    private void showPanel() {
        long startTime = System.nanoTime();
        try {
            if (isEagerMode) {
                if (hiddenEagerView != null) {
                    hiddenEagerView.setVisibility(View.VISIBLE);
                }
            } else {
                if (hiddenStub != null && hiddenStub.getParent() != null) {
                    hiddenStub.inflate();
                }
            }
            long showTime = (System.nanoTime() - startTime) / 1_000_000;
            String mode = isEagerMode ? "CACH CU" : "VIEWSTUB";
            
            String hierarchyReport = getHierarchyReport();
            
            textInflationTime.setText("[TRANG THAI: DA HIEN - " + mode + "]\n" +
                                      "Thoi gian hien: " + showTime + "ms\n\n" +
                                      "--- HIERARCHY SAU KHI HIEN ---\n" +
                                      hierarchyReport);
            
            btnShowPanel.setText("Đã hiển thị");
            btnShowPanel.setEnabled(false);
            btnShowPanel.setAlpha(0.5f);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getHierarchyReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Container\n");
        
        if (contentContainer != null) {
            int count = contentContainer.getChildCount();
            if (count == 0) sb.append("  (Trong)\n");
            
            for (int i = 0; i < count; i++) {
                View child = contentContainer.getChildAt(i);
                String name = child.getClass().getSimpleName();
                String visibility = "";
                switch (child.getVisibility()) {
                    case View.VISIBLE: visibility = "VISIBLE"; break;
                    case View.INVISIBLE: visibility = "INVISIBLE"; break;
                    case View.GONE: visibility = "GONE"; break;
                }
                
                sb.append("  +-- ").append(name).append(" [").append(visibility).append("]");
                
                if (child instanceof ViewStub) {
                    sb.append(" (NHE)");
                } else if (name.contains("CardView") || name.contains("LinearLayout")) {
                    sb.append(" (NANG!)");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
