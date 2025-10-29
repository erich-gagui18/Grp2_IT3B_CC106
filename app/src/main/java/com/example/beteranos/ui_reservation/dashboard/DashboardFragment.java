package com.example.beteranos.ui_reservation.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // For colors
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

// MPAndroidChart Imports
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import com.example.beteranos.R; // Ensure R is imported
import com.example.beteranos.databinding.FragmentDashboardBinding; // Use your actual binding class name

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    // Remove adapter instances:
    // private HaircutRankingAdapter haircutRankingAdapter;
    // private BarberRankingAdapter barberRankingAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // No need to set up RecyclerViews
        // setupRankingLists();

        observeViewModel();

        // Fetch data
        dashboardViewModel.fetchHaircutRanking();
        dashboardViewModel.fetchBarberRanking();
    }

    private void observeViewModel() {
        // Observe haircut ranking data and update the chart
        dashboardViewModel.getHaircutRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                Log.d("DashboardFragment", "Haircut Ranking Data Received: " + rankingMap.size());
                setupHorizontalBarChart(binding.chartHaircutRanking, rankingMap, "Haircut Choices");
            } else if (binding != null) {
                binding.chartHaircutRanking.clear(); // Clear chart if data is null
                binding.chartHaircutRanking.invalidate();
            }
        });

        // Observe barber ranking data and update the chart
        dashboardViewModel.getBarberRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                Log.d("DashboardFragment", "Barber Ranking Data Received: " + rankingMap.size());
                setupHorizontalBarChart(binding.chartBarberRanking, rankingMap, "Barber Bookings");
            } else if (binding != null) {
                binding.chartBarberRanking.clear(); // Clear chart if data is null
                binding.chartBarberRanking.invalidate();
            }
        });
    }

    // --- Method to setup and display data on a HorizontalBarChart ---
    private void setupHorizontalBarChart(HorizontalBarChart barChart, Map<String, Integer> dataMap, String descriptionLabel) {
        if (dataMap.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("No data available yet.");
            barChart.invalidate();
            return;
        }

        // 1. Prepare Data Entries
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;
        // Data comes ordered (LinkedHashMap), but we need to reverse for horizontal chart
        // (MPAndroidChart plots horizontal bars from bottom up)
        List<Map.Entry<String, Integer>> reversedData = new ArrayList<>(dataMap.entrySet());
        Collections.reverse(reversedData);

        for (Map.Entry<String, Integer> entry : reversedData) {
            entries.add(new BarEntry(index, entry.getValue().floatValue())); // index = y, value = x
            labels.add(entry.getKey());
            index++;
        }

        // 2. Create DataSet
        BarDataSet dataSet = new BarDataSet(entries, descriptionLabel);
        // Use a color from your resources
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        // 3. Create BarData object
        BarData barData = new BarData(dataSets);
        barData.setBarWidth(0.6f); // Adjust bar width

        // 4. Configure Chart Appearance
        barChart.setData(barData);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars

        // Description
        Description description = new Description();
        description.setEnabled(false); // Hide the description label
        barChart.setDescription(description);

        // X Axis (Labels - now on the left for HorizontalBarChart)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set labels
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Labels at the bottom of the chart (left side)
        xAxis.setGranularity(1f); // Only show integer values
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false); // Hide vertical grid lines
        xAxis.setTextSize(11f);


        // Y Axis Left (Values - now along the bottom)
        barChart.getAxisLeft().setEnabled(true); // Show values at bottom
        barChart.getAxisLeft().setAxisMinimum(0f); // Start values from 0
        barChart.getAxisLeft().setDrawGridLines(true); // Show horizontal grid lines

        // Y Axis Right (Hide)
        barChart.getAxisRight().setEnabled(false);

        // General settings
        barChart.setDrawValueAboveBar(true);
        barChart.setTouchEnabled(false); // Disable touch interactions if not needed
        barChart.getLegend().setEnabled(false); // Hide the legend
        barChart.animateY(1000); // Add animation

        // 5. Refresh Chart
        barChart.invalidate();
    }


    // Remove setupRankingLists() method as it's no longer used

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up binding
    }
}