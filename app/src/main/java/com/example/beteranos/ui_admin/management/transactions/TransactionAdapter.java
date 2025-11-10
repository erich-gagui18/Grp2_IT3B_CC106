package com.example.beteranos.ui_admin.management.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    private final OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClicked(Transaction transaction);
    }

    public TransactionAdapter(OnTransactionClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.bind(transaction, listener);
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        // Views for the item layout
        private final TextView tvCustomerName, tvBarberName, tvServices, tvDate, tvAmount, tvStatus;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US);

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvBarberName = itemView.findViewById(R.id.tv_barber_name);
            tvServices = itemView.findViewById(R.id.tv_services);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }

        public void bind(Transaction transaction, OnTransactionClickListener listener) {
            tvCustomerName.setText("Customer: " + transaction.getCustomerName());
            tvBarberName.setText("Barber: " + transaction.getBarberName());
            tvServices.setText("Services: " + transaction.getServices());

            // Format the date
            if (transaction.getReservationTime() != null) {
                tvDate.setText(dateFormat.format(transaction.getReservationTime()));
            } else {
                tvDate.setText("N/A");
            }

            tvAmount.setText(transaction.getFormattedFinalPrice());

            // Status coloring logic
            String status = transaction.getStatus();
            if (status != null) {
                tvStatus.setText(status.toUpperCase(Locale.US));

                switch (status.toLowerCase(Locale.US)) {
                    case "completed":
                    case "done":
                        tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                        break;
                    case "scheduled":
                        tvStatus.setBackgroundResource(R.drawable.bg_status_scheduled);
                        break;
                    case "cancelled":
                    case "failed":
                        tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                        break;
                    case "pending":
                    default:
                        tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                        break;
                }
            } else {
                // Handle null status gracefully
                tvStatus.setText("UNKNOWN");
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            }

            // Set click listener on the whole item view
            itemView.setOnClickListener(v -> listener.onTransactionClicked(transaction));
        }
    } // End of TransactionViewHolder class

    // ⭐️ FIX: DIFF_CALLBACK MUST be a static member of the OUTER class ⭐️
    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Transaction>() {
                @Override
                public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    // Uses the unique reservation ID (String) for identity
                    return oldItem.getReservationId().equals(newItem.getReservationId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    // Uses the overridden equals method on the Transaction model
                    return oldItem.equals(newItem);
                }
            };
} // End of TransactionAdapter class