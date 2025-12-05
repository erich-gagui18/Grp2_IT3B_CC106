package com.example.beteranos.ui_admin.management.promos;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beteranos.R;
import com.example.beteranos.databinding.ItemManagePromoBinding; // ⭐️ This will work after you save the XML
import com.example.beteranos.models.Promo;

public class PromosManagementAdapter extends ListAdapter<Promo, PromosManagementAdapter.PromoViewHolder> {

    private final OnPromoActionListener listener;

    public interface OnPromoActionListener {
        void onEditClick(Promo promo);
        void onDeleteClick(Promo promo);
        void onToggleVisibilityClick(Promo promo); // ⭐️ NEW
    }

    public PromosManagementAdapter(OnPromoActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Promo> DIFF_CALLBACK = new DiffUtil.ItemCallback<Promo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Promo oldItem, @NonNull Promo newItem) {
            return oldItem.getPromoId() == newItem.getPromoId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Promo oldItem, @NonNull Promo newItem) {
            return oldItem.equals(newItem); // Relies on your updated Promo.equals()
        }
    };

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagePromoBinding binding = ItemManagePromoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PromoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class PromoViewHolder extends RecyclerView.ViewHolder {
        private final ItemManagePromoBinding binding;

        public PromoViewHolder(ItemManagePromoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Promo promo) {
            binding.promoNameText.setText(promo.getPromoName());
            binding.promoDiscountText.setText(promo.getDiscountPercentage() + "% OFF");

            // Load Image
            if (promo.getImage() != null) {
                Glide.with(itemView.getContext())
                        .load(promo.getImage())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(binding.promoImage);
            } else {
                binding.promoImage.setImageResource(R.drawable.ic_image_placeholder);
            }

            // ⭐️ Toggle Icon Logic ⭐️
            if (promo.isActive()) {
                binding.toggleVisibilityButton.setImageResource(R.drawable.ic_visibility);
                binding.toggleVisibilityButton.setAlpha(1.0f);
            } else {
                binding.toggleVisibilityButton.setImageResource(R.drawable.ic_visibility_off); // Create this icon if missing
                binding.toggleVisibilityButton.setAlpha(0.5f); // Dim it to indicate hidden
            }

            // Click Listeners
            binding.editButton.setOnClickListener(v -> listener.onEditClick(promo));
            binding.deleteButton.setOnClickListener(v -> listener.onDeleteClick(promo));
            binding.toggleVisibilityButton.setOnClickListener(v -> listener.onToggleVisibilityClick(promo));
        }
    }
}