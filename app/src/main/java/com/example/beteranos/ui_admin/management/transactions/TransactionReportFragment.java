package com.example.beteranos.ui_admin.management.transactions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
import com.example.beteranos.databinding.DialogTransactionDetailsBinding;
import com.example.beteranos.databinding.FragmentTransactionReportBinding;
import com.example.beteranos.models.Transaction;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionReportFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private FragmentTransactionReportBinding binding;
    private TransactionReportViewModel viewModel;
    private TransactionAdapter adapter;

    private Long selectedStartDate = null;
    private Long selectedEndDate = null;
    private final SimpleDateFormat dialogDateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
    private final SimpleDateFormat titleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private String currentStatusFilter = "All Sales"; // Default filter

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
        setupTabs();
        observeViewModel();

        viewModel.fetchTransactions(null, null, currentStatusFilter);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this);
        binding.transactionRecyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatusFilter = tab.getText().toString();
                viewModel.fetchTransactions(selectedStartDate, selectedEndDate, currentStatusFilter);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
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

                String dateText = titleDateFormat.format(new Date(selectedStartDate)) + " - " +
                        titleDateFormat.format(new Date(selectedEndDate));
                binding.btnSelectDateRange.setText(dateText);

                viewModel.fetchTransactions(selectedStartDate, selectedEndDate, currentStatusFilter);
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
    public void onTransactionClicked(Transaction transaction) {
        // 1. Inflate the layout
        DialogTransactionDetailsBinding dialogBinding = DialogTransactionDetailsBinding.inflate(LayoutInflater.from(getContext()));

        // 2. Populate data
        dialogBinding.tvDetailCustomer.setText(transaction.getCustomerName());
        dialogBinding.tvDetailBarber.setText(transaction.getBarberName());
        dialogBinding.tvDetailDatetime.setText(dialogDateFormat.format(transaction.getReservationTime()));
        dialogBinding.tvDetailServices.setText(transaction.getServices());

        dialogBinding.tvDetailTotal.setText(String.format(Locale.US, "₱%.2f", transaction.getTotalPrice()));
        dialogBinding.tvDetailDiscount.setText(String.format(Locale.US, "-₱%.2f", transaction.getDiscountAmount()));
        dialogBinding.tvDetailFinal.setText(transaction.getFormattedFinalPrice());
        dialogBinding.tvDetailDownpayment.setText(String.format(Locale.US, "₱%.2f", transaction.getDownPaymentAmount()));
        dialogBinding.tvDetailBalance.setText(transaction.getFormattedRemainingBalance());

        // 3. Conditional Button Logic
        String currentStatus = transaction.getStatus();
        boolean needsAction = false;
        String positiveButtonText = "";

        // Determine the correct action based on the current status
        switch (currentStatus.toUpperCase(Locale.US)) {
            case "PENDING":
                needsAction = true;
                positiveButtonText = "Confirm Reservation"; // Action: Move to Confirmed
                break;
            case "CONFIRMED":
                needsAction = true;
                positiveButtonText = "Mark Paid & Complete"; // Action: Move to Completed
                break;
            case "COMPLETED":
            default:
                positiveButtonText = "OK"; // No action needed
                break;
        }

        // Conditionally hide the balance labels if the transaction is complete and paid
        if ("COMPLETED".equalsIgnoreCase(currentStatus) && transaction.getRemainingBalance() <= 0.0) {
            dialogBinding.tvBalanceLabel.setVisibility(View.GONE);
            dialogBinding.tvDetailBalance.setVisibility(View.GONE);
        } else {
            dialogBinding.tvBalanceLabel.setVisibility(View.VISIBLE);
            dialogBinding.tvDetailBalance.setVisibility(View.VISIBLE);
        }

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Transaction #" + transaction.getReservationId())
                .setView(dialogBinding.getRoot());

        // 4. Set the correct button and action
        if (needsAction) {
            // ⭐️ FIX: Create a final copy of the variable for use in the lambda ⭐️
            final String actionText = positiveButtonText;

            builder.setPositiveButton(actionText, (dialog, which) -> {
                if ("Confirm Reservation".equals(actionText)) {
                    // Call new ViewModel logic
                    viewModel.confirmReservation(transaction.getReservationId());
                } else if ("Mark Paid & Complete".equals(actionText)) {
                    // Call existing ViewModel logic
                    viewModel.markTransactionCompletedAndPaid(transaction.getReservationId());
                }
            });
            // A "Close" button is useful if an action is present
            builder.setNegativeButton("Close", null);
        } else {
            // For "Completed" status, the only button is "OK"
            builder.setPositiveButton(positiveButtonText, null);
        }

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}