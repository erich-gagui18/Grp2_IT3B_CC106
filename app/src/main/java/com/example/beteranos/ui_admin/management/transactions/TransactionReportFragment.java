package com.example.beteranos.ui_admin.management.transactions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.SharedImageCache;
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

    private String currentStatusFilter = "All Sales";

    // ... (onCreateView, onViewCreated, setupRecyclerView, setupTabs, setupDateFilter, observeViewModel are all correct) ...
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


    // --- ⭐️ THIS METHOD IS UPDATED TO FIX THE CRASH ⭐️ ---
    @Override
    public void onTransactionClicked(Transaction transaction) {
        DialogTransactionDetailsBinding dialogBinding = DialogTransactionDetailsBinding.inflate(LayoutInflater.from(getContext()));

        // --- ⭐️ THIS IS THE FIX ⭐️ ---
        // Convert the String ID to an int.
        int reservationIdAsInt;
        try {
            reservationIdAsInt = Integer.parseInt(transaction.getReservationId());
        } catch (NumberFormatException e) {
            Log.e("TransactionReport", "Failed to parse Reservation ID: " + transaction.getReservationId());
            Toast.makeText(getContext(), "Error: Invalid transaction ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- ⭐️ END OF FIX ⭐️ ---

        // 2. Populate data
        dialogBinding.tvDetailCustomer.setText("Customer: " + transaction.getCustomerName());
        dialogBinding.tvDetailBarber.setText("Barber: " + transaction.getBarberName());
        dialogBinding.tvDetailDatetime.setText(dialogDateFormat.format(transaction.getReservationTime()));
        dialogBinding.tvDetailServices.setText("Services: " + transaction.getServices());

        dialogBinding.tvDetailTotal.setText(String.format(Locale.US, "₱%.2f", transaction.getTotalPrice()));
        dialogBinding.tvDetailDiscount.setText(String.format(Locale.US, "-₱%.2f", transaction.getDiscountAmount()));
        dialogBinding.tvDetailFinal.setText(transaction.getFormattedFinalPrice());
        dialogBinding.tvDetailDownpayment.setText(String.format(Locale.US, "₱%.2f", transaction.getDownPaymentAmount()));
        dialogBinding.tvDetailBalance.setText(transaction.getFormattedRemainingBalance());

        // 3. Get receipt data
        final byte[] receiptBytes = transaction.getPaymentReceiptBytes();

        if (receiptBytes != null && receiptBytes.length > 0) {
            dialogBinding.pbImageLoading.setVisibility(View.GONE);
            dialogBinding.tvNoReceipt.setVisibility(View.GONE);
            dialogBinding.ivPaymentReceipt.setVisibility(View.VISIBLE);

            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(receiptBytes, 0, receiptBytes.length);
                if (bitmap != null) {
                    dialogBinding.ivPaymentReceipt.setImageBitmap(bitmap);
                } else {
                    dialogBinding.ivPaymentReceipt.setVisibility(View.GONE);
                    dialogBinding.tvNoReceipt.setText("Failed to decode receipt image.");
                    dialogBinding.tvNoReceipt.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                dialogBinding.ivPaymentReceipt.setVisibility(View.GONE);
                dialogBinding.tvNoReceipt.setText("Error loading receipt.");
                dialogBinding.tvNoReceipt.setVisibility(View.VISIBLE);
            }

        } else {
            dialogBinding.pbImageLoading.setVisibility(View.GONE);
            dialogBinding.ivPaymentReceipt.setVisibility(View.GONE);
            dialogBinding.tvNoReceipt.setVisibility(View.VISIBLE);
            dialogBinding.tvNoReceipt.setText("No payment receipt uploaded.");
        }

        // 4. Set the click listener to open the full image
        dialogBinding.ivPaymentReceipt.setOnClickListener(v -> {
            if (receiptBytes != null && receiptBytes.length > 0) {

                // --- ⭐️ USE THE INT ID ⭐️ ---
                SharedImageCache.putReceiptBytes(reservationIdAsInt, receiptBytes);

                Intent intent = new Intent(getContext(), FullImageActivity.class);
                intent.putExtra(FullImageActivity.EXTRA_RECEIPT_KEY, reservationIdAsInt); // <-- Pass the int
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No receipt available.", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Conditional Button Logic
        String currentStatus = transaction.getStatus();
        boolean needsAction = false;
        String positiveButtonText = "";

        switch (currentStatus.toUpperCase(Locale.US)) {
            case "PENDING":
                needsAction = true;
                positiveButtonText = "Confirm Reservation";
                break;
            case "CONFIRMED":
                needsAction = true;
                positiveButtonText = "Mark Paid & Complete";
                break;
            case "COMPLETED":
            default:
                positiveButtonText = "OK";
                break;
        }

        if ("COMPLETED".equalsIgnoreCase(currentStatus) && transaction.getRemainingBalance() <= 0.0) {
            dialogBinding.tvBalanceLabel.setVisibility(View.GONE);
            dialogBinding.tvDetailBalance.setVisibility(View.GONE);
        } else {
            dialogBinding.tvBalanceLabel.setVisibility(View.VISIBLE);
            dialogBinding.tvDetailBalance.setVisibility(View.VISIBLE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Transaction #" + transaction.getReservationId()) // <-- Still show String ID here
                .setView(dialogBinding.getRoot());

        // 6. Set the correct button and action
        if (needsAction) {
            final String actionText = positiveButtonText;
            builder.setPositiveButton(actionText, (dialog, which) -> {
                if ("Confirm Reservation".equals(actionText)) {
                    viewModel.confirmReservation(transaction.getReservationId()); // <-- Pass String ID to VM
                } else if ("Mark Paid & Complete".equals(actionText)) {
                    viewModel.markTransactionCompletedAndPaid(transaction.getReservationId()); // <-- Pass String ID to VM
                }
                viewModel.fetchTransactions(selectedStartDate, selectedEndDate, currentStatusFilter);
            });
            builder.setNegativeButton("Close", null);
        } else {
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