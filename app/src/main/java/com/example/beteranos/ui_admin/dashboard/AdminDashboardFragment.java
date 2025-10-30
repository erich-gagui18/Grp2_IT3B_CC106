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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminDashboardBinding;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminDashboardViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe greeting text
        viewModel.getText().observe(getViewLifecycleOwner(), text ->
                binding.textDashboard.setText(text)
        );

        // Optional: show username if passed via arguments
        if (getArguments() != null && getArguments().containsKey("username")) {
            String username = getArguments().getString("username");
            binding.textDashboard.setText("Welcome to the Dashboard, " + username + "!");
        }

        // Observe Haircut Ranking
        viewModel.getHaircutRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                setupHorizontalBarChart(binding.chartHaircutRanking, rankingMap, "Haircut Choices");
            } else if (binding != null) {
                binding.chartHaircutRanking.clear();
                binding.chartHaircutRanking.invalidate();
            }
        });

        // Observe Barber Ranking
        viewModel.getBarberRanking().observe(getViewLifecycleOwner(), rankingMap -> {
            if (rankingMap != null && binding != null) {
                setupHorizontalBarChart(binding.chartBarberRanking, rankingMap, "Barber Bookings");
            } else if (binding != null) {
                binding.chartBarberRanking.clear();
                binding.chartBarberRanking.invalidate();
            }
        });

        // Fetch data
        viewModel.fetchHaircutRanking();
        viewModel.fetchBarberRanking();
    }

    private void setupHorizontalBarChart(HorizontalBarChart barChart, Map<String, Integer> dataMap, String label) {
        if (dataMap.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("No data available yet.");
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

        barChart.getAxisLeft().setEnabled(true);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setDrawGridLines(true);

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
