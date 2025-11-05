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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US);

    public TransactionAdapter() {
        super(DIFF_CALLBACK);
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
        holder.bind(transaction);
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText, amountText, customerText, barberText, servicesText;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_transaction_date);
            amountText = itemView.findViewById(R.id.tv_transaction_amount);
            customerText = itemView.findViewById(R.id.tv_customer_name);
            barberText = itemView.findViewById(R.id.tv_barber_name);
            servicesText = itemView.findViewById(R.id.tv_services_list);
        }

        public void bind(Transaction transaction) {
            dateText.setText(DATE_FORMAT.format(transaction.getReservationTime()));
            amountText.setText(String.format(Locale.US, "₱%.2f", transaction.getTotalAmount()));
            customerText.setText("Customer: " + transaction.getCustomerName());
            barberText.setText("Barber: " + transaction.getBarberName());
            servicesText.setText("Services: " + transaction.getServices());
        }
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Transaction>() {
                @Override
                public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    return oldItem.getReservationId() == newItem.getReservationId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    // Simple check, can be expanded if transactions are editable
                    return oldItem.getTotalAmount() == newItem.getTotalAmount() &&
                            oldItem.getCustomerName().equals(newItem.getCustomerName());
                }
            };
}