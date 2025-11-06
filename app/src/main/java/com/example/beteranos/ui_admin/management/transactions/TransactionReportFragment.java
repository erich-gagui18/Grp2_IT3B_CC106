package com.example.beteranos.ui_admin.management.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.databinding.FragmentTransactionReportBinding;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionReportFragment extends Fragment {

    private FragmentTransactionReportBinding binding;
    private TransactionReportViewModel viewModel;
    private TransactionAdapter adapter;

    private Long selectedStartDate = null;
    private Long selectedEndDate = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionReportBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TransactionReportViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupDateFilter();
        observeViewModel();

        // Fetch initial data (all time)
        viewModel.fetchTransactions(null, null);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.transactionRecyclerView.setAdapter(adapter);
    }

    private void setupDateFilter() {
        binding.btnSelectDateRange.setOnClickListener(v -> {
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

                viewModel.fetchTransactions(selectedStartDate, selectedEndDate);
            });
            datePicker.show(getParentFragmentManager(), datePicker.toString());
        });
    }

    private void observeViewModel() {
        viewModel.transactions.observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            binding.emptyListText.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.totalSales.observe(getViewLifecycleOwner(), total -> {
            binding.tvTotalSales.setText(total);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.transactionRecyclerView.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}