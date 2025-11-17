package com.example.beteranos.ui_admin.management.promos;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.beteranos.R;
import com.example.beteranos.databinding.ItemManagePromoBinding;
import com.example.beteranos.models.Promo;

public class PromosManagementAdapter extends ListAdapter<Promo, PromosManagementAdapter.PromoViewHolder> {

    private final OnPromoActionListener listener;

    public interface OnPromoActionListener {
        void onEditClick(Promo promo);
        void onDeleteClick(Promo promo);
    }

    public PromosManagementAdapter(OnPromoActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagePromoBinding binding = ItemManagePromoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PromoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promo promo = getItem(position);
        holder.bind(promo, listener);
    }

    static class PromoViewHolder extends RecyclerView.ViewHolder {
        private final ItemManagePromoBinding binding;

        public PromoViewHolder(@NonNull ItemManagePromoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Promo promo, OnPromoActionListener listener) {
            binding.promoNameText.setText(promo.getPromoName());
            binding.promoDescriptionText.setText(promo.getDescription());
            binding.promoDiscountText.setText(promo.getDiscountPercentage() + "% OFF");

            // Load the image from byte[]
            Glide.with(itemView.getContext())
                    .load(promo.getImage()) // This loads the byte[]
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_broken)
                    .into(binding.promoImageView);

            binding.btnEdit.setOnClickListener(v -> listener.onEditClick(promo));
            binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(promo));
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
                    return oldItem.equals(newItem);
                }
            };
}