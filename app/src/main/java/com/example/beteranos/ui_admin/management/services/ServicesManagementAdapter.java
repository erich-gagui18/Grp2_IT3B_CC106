package com.example.beteranos.ui_admin.management.services;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Service;

import java.util.Locale;

public class ServicesManagementAdapter extends ListAdapter<Service, ServicesManagementAdapter.ServiceViewHolder> {

    private final OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onEditClick(Service service);
        void onDeleteClick(Service service);
    }

    public ServicesManagementAdapter(@NonNull OnServiceActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = getItem(position);
        holder.bind(service, listener);
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText, priceText;
        private final ImageButton editButton, deleteButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.service_name_text);
            priceText = itemView.findViewById(R.id.service_price_text);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Service service, OnServiceActionListener listener) {
            nameText.setText(service.getServiceName());
            priceText.setText(String.format(Locale.US, "₱%.2f", service.getPrice()));

            editButton.setOnClickListener(v -> listener.onEditClick(service));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(service));
        }
    }

    private static final DiffUtil.ItemCallback<Service> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Service>() {
                @Override
                public boolean areItemsTheSame(@NonNull Service oldItem, @NonNull Service newItem) {
                    return oldItem.getServiceId() == newItem.getServiceId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Service oldItem, @NonNull Service newItem) {
                    return oldItem.getServiceName().equals(newItem.getServiceName()) &&
                            oldItem.getPrice() == newItem.getPrice();
                }
            };
}