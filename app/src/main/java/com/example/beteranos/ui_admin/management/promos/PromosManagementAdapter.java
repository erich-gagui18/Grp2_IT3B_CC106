package com.example.beteranos.ui_admin.management.promos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beteranos.R;
import com.example.beteranos.models.Promo;

import java.util.Arrays;
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
        private final ImageView promoImage;
        private final TextView nameText, descText;
        private final ImageButton editButton, deleteButton;

        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            promoImage = itemView.findViewById(R.id.promo_image);
            nameText = itemView.findViewById(R.id.promo_name_text);
            descText = itemView.findViewById(R.id.promo_desc_text);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Promo promo, OnPromoActionListener listener) {
            nameText.setText(promo.getPromoName());

            if (promo.getDescription() != null && !promo.getDescription().isEmpty()) {
                descText.setText(promo.getDescription());
            } else {
                descText.setText(String.format(Locale.US, "%d%% off services", promo.getDiscountPercentage()));
            }

            // --- ⭐️ FIX INTEGRATED: Copied logic from your PromoFragment ---
            byte[] imageBytes = promo.getImage();

            if (imageBytes != null && imageBytes.length > 0) {
                Glide.with(itemView.getContext())
                        .asBitmap() // <-- Force bitmap decoding
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_image_broken)
                        .error(R.drawable.ic_image_broken)
                        .into(promoImage);
            } else {
                // <-- Handle null/empty images
                Glide.with(itemView.getContext())
                        .load(R.drawable.ic_image_broken)
                        .into(promoImage);
            }
            // --- END OF FIX ---

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
                    // --- ⭐️ FIX: Use Objects.equals for Strings to prevent NullPointerException ---
                    return Objects.equals(oldItem.getPromoName(), newItem.getPromoName()) &&
                            Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                            Arrays.equals(oldItem.getImage(), newItem.getImage()) &&
                            oldItem.getDiscountPercentage() == newItem.getDiscountPercentage();
                }
            };
}