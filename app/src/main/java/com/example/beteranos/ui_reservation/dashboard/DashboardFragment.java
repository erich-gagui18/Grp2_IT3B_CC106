package com.example.beteranos.ui_reservation.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis; // Import YAxis
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import com.google.android.material.datepicker.MaterialDatePicker;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

enum SortType {
    POPULARITY,
    NAME
}

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;

    private Long selectedStartDate = null;
    private Long selectedEndDate = null;
    private SortType currentSortType = SortType.POPULARITY;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupFilterListeners();
        observeViewModel();

        binding.toggleSort.check(R.id.btn_sort_popularity);
        loadData();
    }

    private void loadData() {
        Log.d("DashboardFragment", "loadData called. Sort: " + currentSortType);
        dashboardViewModel.fetchHaircutRanking(selectedStartDate, selectedEndDate, currentSortType);
        dashboardViewModel.fetchBarberRanking(selectedStartDate, selectedEndDate, currentSortType);
    }

    private void setupFilterListeners() {
        binding.btnSelectDateRange.setOnClickListener(v -> {
            showDateRangePicker();
        });

        binding.toggleSort.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_sort_popularity) {
                    currentSortType = SortType.POPULARITY;
                } else if (checkedId == R.id.btn_sort_name) {
                    currentSortType = SortType.NAME;
                }
                loadData();
            }
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a Date Range");

        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            TimeZone timeZone = TimeZone.getDefault();
            long offset = timeZone.getOffset(selection.first);

            selectedStartDate = selection.first + offset;
            selectedEndDate = selection.second + offset;

            String dateText = dateFormat.format(new Date(selectedStartDate)) + " - " +
                    dateFormat.format(new Date(selectedEndDate));
            binding.btnSelectDateRange.setText(dateText);

            loadData();
        });

        datePicker.addOnNegativeButtonClickListener(v -> {
            // User cancelled
        });

        datePicker.addOnCancelListener(v -> {
            // User cancelled
        });

        datePicker.show(getParentFragmentManager(), datePicker.toString());
    }

    private void observeViewModel() {
        dashboardViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.haircutProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.barberProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            binding.chartHaircutRanking.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
            binding.chartBarberRanking.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        dashboardViewModel.getHaircutRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                Log.d("DashboardFragment", "Haircut Ranking Data Received: " + rankingMap.size());
                setupHorizontalBarChart(binding.chartHaircutRanking, rankingMap, "Haircut Choices");
            } else if (binding != null) {
                binding.chartHaircutRanking.clear();
                binding.chartHaircutRanking.setNoDataText("No data to display");
                binding.chartHaircutRanking.invalidate();
            }
        });

        dashboardViewModel.getBarberRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                Log.d("DashboardFragment", "Barber Ranking Data Received: " + rankingMap.size());
                setupHorizontalBarChart(binding.chartBarberRanking, rankingMap, "Barber Bookings");
            } else if (binding != null) {
                binding.chartBarberRanking.clear();
                binding.chartBarberRanking.setNoDataText("No data to display");
                binding.chartBarberRanking.invalidate();
            }
        });
    }

    private void setupHorizontalBarChart(HorizontalBarChart barChart, Map<String, Integer> dataMap, String descriptionLabel) {
        if (dataMap == null || dataMap.isEmpty()) {
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

        BarDataSet dataSet = new BarDataSet(entries, descriptionLabel);
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

        // --- Start of Alignment Fix ---
        // Give enough space for the longest expected label
        xAxis.setYOffset(10f); // Adjust if your labels are taller
        // --- End of Alignment Fix ---


        YAxis axisLeft = barChart.getAxisLeft(); // Get the left Y-axis
        axisLeft.setEnabled(true);
        axisLeft.setAxisMinimum(0f);
        axisLeft.setDrawGridLines(true);
        axisLeft.setTextSize(11f); // Added for consistency

        // --- Start of Alignment Fix ---
        // This is crucial. Set a fixed offset for the Y-axis labels.
        // You might need to adjust this value (e.g., 60f, 70f, 80f)
        // based on the longest label you expect ("Modern Mullet" is relatively long).
        axisLeft.setXOffset(70f); // Experiment with this value
        // --- End of Alignment Fix ---

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