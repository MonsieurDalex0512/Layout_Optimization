package com.example.layout_optimization;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.layout_optimization.fragments.ComparisonFragment;
import com.example.layout_optimization.fragments.ReportFragment;
import com.example.layout_optimization.utils.PerformanceMonitor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private TextView fpsCounter; // Real-time FPS display
    private FloatingActionButton fabBenchmark;
    private PerformanceMonitor performanceMonitor;
    
    // Fragments tags
    private static final String TAG_COMPARISON = "Comparison";
    private static final String TAG_REPORT = "Report";
    // Tag cũ để cleanup
    private static final String TAG_PROFILER = "Profiler";
    private static final String TAG_METRICS = "Metrics";
    
    private Fragment activeFragment;
    private ComparisonFragment comparisonFragment;
    private ReportFragment reportFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Initialize views
        bottomNav = findViewById(R.id.bottom_navigation);
        fpsCounter = findViewById(R.id.fps_counter);
        fabBenchmark = findViewById(R.id.fab_benchmark);
        
        // Hide FAB
        fabBenchmark.setVisibility(View.GONE);
        
        // Initialize PerformanceMonitor (Singleton)
        performanceMonitor = PerformanceMonitor.getInstance();
        performanceMonitor.setListener((fps, jankCount, avgFrameTime) -> {
            // Update FPS counter on UI thread
            runOnUiThread(() -> {
                fpsCounter.setText(String.format("%.0f FPS", fps));
                if (fps >= 55) {
                    fpsCounter.setTextColor(Color.GREEN);
                } else if (fps >= 40) {
                    fpsCounter.setTextColor(Color.YELLOW);
                } else {
                    fpsCounter.setTextColor(Color.RED);
                }
            });
        });
        
        // Initialize Fragments
        if (savedInstanceState == null) {
            comparisonFragment = new ComparisonFragment();
            reportFragment = new ReportFragment();
            
            // Add all fragments but hide them except the first one
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, comparisonFragment, TAG_COMPARISON)
                .add(R.id.fragment_container, reportFragment, TAG_REPORT).hide(reportFragment)
                .commit();
                
            activeFragment = comparisonFragment;
        } else {
            // Restore fragments if activity was recreated
            comparisonFragment = (ComparisonFragment) getSupportFragmentManager().findFragmentByTag(TAG_COMPARISON);
            reportFragment = (ReportFragment) getSupportFragmentManager().findFragmentByTag(TAG_REPORT);
            
            // Cleanup old fragments if exists (to prevent crash/overlap)
            Fragment oldMetrics = getSupportFragmentManager().findFragmentByTag(TAG_METRICS);
            Fragment oldProfiler = getSupportFragmentManager().findFragmentByTag(TAG_PROFILER);
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            boolean changed = false;
            
            if (oldMetrics != null) { tx.remove(oldMetrics); changed = true; }
            if (oldProfiler != null) { tx.remove(oldProfiler); changed = true; }
            
            if (changed) tx.commit();

            // Find visible fragment
            if (comparisonFragment != null && comparisonFragment.isVisible()) activeFragment = comparisonFragment;
            else if (reportFragment != null && reportFragment.isVisible()) activeFragment = reportFragment;
            
            // FALLBACK: If no active fragment found, default to Comparison
            if (activeFragment == null) {
                if (comparisonFragment == null) comparisonFragment = new ComparisonFragment();
                activeFragment = comparisonFragment;
                getSupportFragmentManager().beginTransaction()
                    .show(comparisonFragment)
                    .commit();
            }
        }
        
        // Setup navigation using show/hide
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment targetFragment = null;
                String title = "";
                
                int itemId = item.getItemId();
                if (itemId == R.id.nav_comparison) {
                    targetFragment = comparisonFragment;
                    title = "So sánh Layout";
                } else if (itemId == R.id.nav_report) {
                    targetFragment = reportFragment;
                    title = "Báo cáo Phân tích";
                }
                
                if (targetFragment != null) {
                    if (activeFragment == null) activeFragment = comparisonFragment;
                    
                    getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(targetFragment)
                        .commit();
                    
                    activeFragment = targetFragment;
                    
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(title);
                    }
                    return true;
                }
                return false;
            }
        });
        
        // Setup FAB click listener
        fabBenchmark.setOnClickListener(v -> runFullBenchmark());
    }
    
    private void runFullBenchmark() {
        Toast.makeText(this, "Đang chạy bộ benchmark đầy đủ...", Toast.LENGTH_SHORT).show();
        // Switch to Report tab
        bottomNav.setSelectedItemId(R.id.nav_report);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        performanceMonitor.startTracking();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        performanceMonitor.stopTracking();
    }
}
