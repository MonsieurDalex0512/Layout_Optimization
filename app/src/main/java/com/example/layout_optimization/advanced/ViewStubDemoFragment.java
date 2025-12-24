package com.example.layout_optimization.advanced;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.layout_optimization.R;

public class ViewStubDemoFragment extends Fragment {
    
    private ViewStub heavyViewStub;
    private View inflatedView;
    private TextView textInflationTime;
    private Button btnLoadEager, btnLoadLazy;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewstub_demo, container, false);
        
        initializeViews(view);
        setupControls();
        
        return view;
    }
    
    private void initializeViews(View view) {
        heavyViewStub = view.findViewById(R.id.stub_heavy_view);
        textInflationTime = view.findViewById(R.id.text_inflation_time);
        btnLoadEager = view.findViewById(R.id.btn_load_eager);
        btnLoadLazy = view.findViewById(R.id.btn_load_lazy);
    }
    
    private void setupControls() {
        btnLoadEager.setOnClickListener(v -> loadEagerly());
        btnLoadLazy.setOnClickListener(v -> loadLazily());
    }
    
    private void loadEagerly() {
        // Ensure ViewStub wasn't already inflated
        if (heavyViewStub == null && inflatedView != null) {
            Toast.makeText(getContext(), "Resetting view for demo...", Toast.LENGTH_SHORT).show();
            ViewGroup container = (ViewGroup) getView().findViewById(R.id.container);
            container.removeView(inflatedView);
            inflatedView = null;
        }

        // Simulate eager loading without ViewStub
        long startTime = System.nanoTime();
        
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        ViewGroup container = (ViewGroup) getView().findViewById(R.id.container);
        
        // Remove ViewStub if it exists to simulate "it was never there, just direct view"
        // But for this demo logic, we just inflate into container
        
        View heavyView = inflater.inflate(R.layout.heavy_complex_view, container, true);
        
        long inflationTime = (System.nanoTime() - startTime) / 1_000_000;
        
        textInflationTime.setText(String.format(
            "⏱️ Eager Load (No ViewStub): %dms\n" +
            "Impact: Loaded immediately, delays startup", 
            inflationTime));
        textInflationTime.setTextColor(Color.RED);
    }
    
    private void loadLazily() {
        if (heavyViewStub == null) {
            Toast.makeText(requireContext(), "ViewStub already inflated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lazy load with ViewStub
        long startTime = System.nanoTime();
        
        inflatedView = heavyViewStub.inflate();
        heavyViewStub = null; // Can only inflate once
        
        long inflationTime = (System.nanoTime() - startTime) / 1_000_000;
        
        textInflationTime.setText(String.format(
            "⏱️ Lazy Load (ViewStub): %dms\n" +
            "Impact: Loaded on-demand, faster startup\n" +
            "Savings: Only loaded when needed", 
            inflationTime));
        textInflationTime.setTextColor(Color.GREEN);
    }
}
