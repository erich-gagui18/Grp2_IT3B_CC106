package com.example.beteranos.ui_admin.management.promos;

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
import com.example.beteranos.models.Promo;

import java.util.Locale;
import java.util.Objects;

public class PromosManagementAdapter extends ListAdapter<Promo, PromosManagementAdapter.PromoViewHolder> {

    private final OnPromoActionListener listener;

    public interface OnPromoActionListener {
        void onEditClick(Promo promo);
        void onDeleteClick(Promo promo);
    }

    public PromosManagementAdapter(@NonNull OnPromoActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_promo, parent, false);
        return new PromoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promo promo = getItem(position);
        holder.bind(promo, listener);
    }

    static class PromoViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText, descText;
        private final ImageButton editButton, deleteButton;

        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.promo_name_text);
            descText = itemView.findViewById(R.id.promo_desc_text);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Promo promo, OnPromoActionListener listener) {
            nameText.setText(promo.getPromoName());

            // Show description or a discount summary
            if (promo.getDescription() != null && !promo.getDescription().isEmpty()) {
                descText.setText(promo.getDescription());
            } else {
                descText.setText(String.format(Locale.US, "%d%% off services", promo.getDiscountPercentage()));
            }

            editButton.setOnClickListener(v -> listener.onEditClick(promo));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(promo));
        }
    }

    private static final DiffUtil.ItemCallback<Promo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Promo>() {
                @Override
                public boolean areItemsTheSame(@NonNull Promo oldItem, @NonNull Promo newItem) {
                    return oldItem.getPromoId() == newItem.getPromoId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Promo oldItem, @NonNull Promo newItem) {
                    // Check all fields that are displayed
                    return oldItem.getPromoName().equals(newItem.getPromoName()) &&
                            Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                            oldItem.getDiscountPercentage() == newItem.getDiscountPercentage();
                }
            };
}