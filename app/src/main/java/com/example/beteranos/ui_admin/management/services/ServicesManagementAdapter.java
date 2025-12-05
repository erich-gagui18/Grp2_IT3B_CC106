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

    // ⭐️ FIX 1: Consolidate Interface (Only define it once)
    public interface OnServiceActionListener {
        void onEditClick(Service service);
        void onDeleteClick(Service service);
        void onToggleVisibilityClick(Service service); // ⭐️ New Action
    }

    // ⭐️ FIX 2: Add Constructor to initialize the listener
    public ServicesManagementAdapter(OnServiceActionListener listener) {
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
        private final ImageButton editButton, deleteButton, toggleButton; // ⭐️ Added toggleButton

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            // ⭐️ FIX 3: Match IDs to your item_manage_service.xml
            nameText = itemView.findViewById(R.id.service_name_text);
            priceText = itemView.findViewById(R.id.service_price_text);

            toggleButton = itemView.findViewById(R.id.toggle_visibility_button); // ⭐️ New
            editButton = itemView.findViewById(R.id.edit_button);     // Was btn_edit in old code
            deleteButton = itemView.findViewById(R.id.delete_button); // Was btn_delete in old code
        }

        public void bind(Service service, OnServiceActionListener listener) {
            nameText.setText(service.getServiceName());
            priceText.setText(String.format(Locale.US, "₱%.2f", service.getPrice()));

            // ⭐️ FIX 4: Handle Visibility Logic (Eye Icon)
            if (service.isActive()) {
                toggleButton.setImageResource(R.drawable.ic_visibility);
                toggleButton.setAlpha(1.0f);
            } else {
                toggleButton.setImageResource(R.drawable.ic_visibility_off);
                toggleButton.setAlpha(0.5f);
            }

            // Click Listeners
            toggleButton.setOnClickListener(v -> listener.onToggleVisibilityClick(service));
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
                    return oldItem.equals(newItem);
                }
            };
}