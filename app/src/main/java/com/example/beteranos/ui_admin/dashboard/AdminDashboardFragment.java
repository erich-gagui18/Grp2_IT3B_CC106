package com.example.beteranos.ui_admin.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair; // Import Pair
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminDashboardBinding;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis; // --- ADD THIS IMPORT ---
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

// Material Date Picker Imports
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminDashboardViewModel viewModel;

    // --- Add state holders for filters ---
    private Long selectedStartDate = null;
    private Long selectedEndDate = null;
    private SortType currentSortType = SortType.POPULARITY; // Default
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up filter listeners
        setupFilterListeners();

        // Start observing ViewModel data
        observeViewModel();

        // Load data with default filters
        binding.toggleSort.check(R.id.btn_sort_popularity); // Set default check
        loadData();
    }

    // --- NEW: Helper method to load data based on current filter state ---
    private void loadData() {
        Log.d("AdminDashboardFragment", "loadData called. Sort: " + currentSortType);
        // Call both fetch methods. The ViewModel will handle the loading state.
        viewModel.fetchHaircutRanking(selectedStartDate, selectedEndDate, currentSortType);
        viewModel.fetchBarberRanking(selectedStartDate, selectedEndDate, currentSortType);
    }

    // --- NEW: Method to set up all UI listeners ---
    private void setupFilterListeners() {
        // Date Range Picker Listener
        binding.btnSelectDateRange.setOnClickListener(v -> {
            showDateRangePicker();
        });

        // Sort Toggle Listener
        binding.toggleSort.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_sort_popularity) {
                    currentSortType = SortType.POPULARITY;
                } else if (checkedId == R.id.btn_sort_name) {
                    currentSortType = SortType.NAME;
                }
                // Reload data with the new sort
                loadData();
            }
        });
    }

    // --- NEW: Method to show the Material Date Range Picker ---
    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a Date Range");

        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Adjust for local time zone from UTC
            TimeZone timeZone = TimeZone.getDefault();
            long offset = timeZone.getOffset(selection.first);

            selectedStartDate = selection.first + offset;
            selectedEndDate = selection.second + offset;

            String dateText = dateFormat.format(new Date(selectedStartDate)) + " - " +
                    dateFormat.format(new Date(selectedEndDate));
            binding.btnSelectDateRange.setText(dateText);

            loadData();
        });

        datePicker.show(getParentFragmentManager(), datePicker.toString());
    }

    private void observeViewModel() {
        // Observe greeting text (if you still want it)
        viewModel.getText().observe(getViewLifecycleOwner(), text ->
                binding.textDashboard.setText(text) // This view is now hidden, but logic is fine
        );

        // --- ADDED: Observe loading state ---
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            // Show/hide progress bars
            binding.haircutProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.barberProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            // Hide charts while loading to prevent "flicker"
            binding.chartHaircutRanking.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
            binding.chartBarberRanking.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        // Observe Haircut Ranking
        viewModel.getHaircutRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                setupHorizontalBarChart(binding.chartHaircutRanking, rankingMap, "Haircut Choices");
            } else if (binding != null) {
                binding.chartHaircutRanking.clear();
                binding.chartHaircutRanking.setNoDataText("No data to display");
                binding.chartHaircutRanking.invalidate();
            }
        });

        // Observe Barber Ranking
        viewModel.getBarberRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                setupHorizontalBarChart(binding.chartBarberRanking, rankingMap, "Barber Bookings");
            } else if (binding != null) {
                binding.chartBarberRanking.clear();
                binding.chartBarberRanking.setNoDataText("No data to display");
                binding.chartBarberRanking.invalidate();
            }
        });
    }

    // --- THIS METHOD IS UPDATED ---
    private void setupHorizontalBarChart(HorizontalBarChart barChart, Map<String, Integer> dataMap, String label) {
        if (dataMap == null || dataMap.isEmpty()) { // Add null check
            barChart.clear();
            barChart.setNoDataText("No data available for this filter.");
            barChart.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        List<Map.Entry<String, Integer>> reversedData = new ArrayList<>(dataMap.entrySet());
        Collections.reverse(reversedData);

        for (Map.Entry<String, Integer> entry : reversedData) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData barData = new BarData(dataSets);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.setFitBars(true);

        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);

        // --- START OF ALIGNMENT FIX ---
        xAxis.setYOffset(10f); // Add a small padding below the labels
        // --- END OF FIX ---

        // --- START OF ALIGNMENT FIX ---
        YAxis axisLeft = barChart.getAxisLeft(); // Get the left Y-axis
        axisLeft.setEnabled(true);
        axisLeft.setAxisMinimum(0f);
        axisLeft.setDrawGridLines(true);
        axisLeft.setTextSize(11f); // Set text size for consistency

        // This is the key: set a fixed horizontal offset for the Y-axis labels
        // Experiment with 70f to find the best value for your app
        axisLeft.setXOffset(70f);
        // --- END OF FIX ---

        barChart.getAxisRight().setEnabled(false);

        barChart.setDrawValueAboveBar(true);
        barChart.setTouchEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1000);

        barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}