package com.example.layout_optimization;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.layout_optimization.fragments.ComparisonFragment;
import com.example.layout_optimization.fragments.MetricsFragment;
import com.example.layout_optimization.fragments.ProfilerFragment;
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
    private static final String TAG_METRICS = "Metrics";
    private static final String TAG_PROFILER = "Profiler";
    private static final String TAG_REPORT = "Report";
    
    private Fragment activeFragment;
    private ComparisonFragment comparisonFragment;
    private MetricsFragment metricsFragment;
    private ProfilerFragment profilerFragment;
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
            metricsFragment = new MetricsFragment();
            profilerFragment = new ProfilerFragment();
            reportFragment = new ReportFragment();
            
            // Add all fragments but hide them except the first one
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, comparisonFragment, TAG_COMPARISON)
                .add(R.id.fragment_container, metricsFragment, TAG_METRICS).hide(metricsFragment)
                .add(R.id.fragment_container, profilerFragment, TAG_PROFILER).hide(profilerFragment)
                .add(R.id.fragment_container, reportFragment, TAG_REPORT).hide(reportFragment)
                .commit();
                
            activeFragment = comparisonFragment;
        } else {
            // Restore fragments if activity was recreated
            comparisonFragment = (ComparisonFragment) getSupportFragmentManager().findFragmentByTag(TAG_COMPARISON);
            metricsFragment = (MetricsFragment) getSupportFragmentManager().findFragmentByTag(TAG_METRICS);
            profilerFragment = (ProfilerFragment) getSupportFragmentManager().findFragmentByTag(TAG_PROFILER);
            reportFragment = (ReportFragment) getSupportFragmentManager().findFragmentByTag(TAG_REPORT);
            
            // Find visible fragment
            if (comparisonFragment != null && comparisonFragment.isVisible()) activeFragment = comparisonFragment;
            else if (metricsFragment != null && metricsFragment.isVisible()) activeFragment = metricsFragment;
            else if (profilerFragment != null && profilerFragment.isVisible()) activeFragment = profilerFragment;
            else if (reportFragment != null && reportFragment.isVisible()) activeFragment = reportFragment;
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
                } else if (itemId == R.id.nav_metrics) {
                    targetFragment = metricsFragment;
                    title = "Metrics Trực tiếp";
                } else if (itemId == R.id.nav_profiler) {
                    targetFragment = profilerFragment;
                    title = "Công cụ Profiler";
                } else if (itemId == R.id.nav_report) {
                    targetFragment = reportFragment;
                    title = "Báo cáo Phân tích";
                }
                
                if (targetFragment != null) {
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
        // Start monitoring globally
        performanceMonitor.startTracking();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        performanceMonitor.stopTracking();
    }
}
