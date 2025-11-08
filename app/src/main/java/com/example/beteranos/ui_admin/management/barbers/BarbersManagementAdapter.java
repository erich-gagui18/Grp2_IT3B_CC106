package com.example.beteranos.ui_admin.management.barbers;

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
import com.example.beteranos.models.Barber;
import java.util.Objects;

public class BarbersManagementAdapter extends ListAdapter<Barber, BarbersManagementAdapter.BarberViewHolder> {

    private final OnBarberActionListener listener;

    public interface OnBarberActionListener {
        void onEditClick(Barber barber);
        void onDeleteClick(Barber barber);
    }

    public BarbersManagementAdapter(@NonNull OnBarberActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure R.layout.item_manage_barber is the correct XML file name
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_barber, parent, false);
        return new BarberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        Barber barber = getItem(position);
        // Pass the listener reference
        holder.bind(barber, listener);
    }

    static class BarberViewHolder extends RecyclerView.ViewHolder {
        private final ImageView barberImage;
        private final TextView nameText, specText;
        private final TextView dayOffText; // TextView for Day Off
        private final ImageButton editButton, deleteButton;

        public BarberViewHolder(@NonNull View itemView) {
            super(itemView);
            barberImage = itemView.findViewById(R.id.barber_image);
            nameText = itemView.findViewById(R.id.barber_name_text);
            specText = itemView.findViewById(R.id.barber_spec_text);
            dayOffText = itemView.findViewById(R.id.barber_day_off_text); // Initialized TextView for Day Off
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Barber barber, OnBarberActionListener listener) {
            nameText.setText(barber.getName());
            specText.setText(barber.getSpecialization());

            // Bind the Day Off data
            String dayOff = barber.getDayOff();
            String dayOffDisplay = "Day Off: " + (dayOff != null && !dayOff.isEmpty() ? dayOff : "N/A");
            dayOffText.setText(dayOffDisplay);

            // 🔑 RECOMMENDED OPTIMIZATION: Robust Image Loading
            String imageUrl = barber.getImageUrl();

            // Determine the model to load: either the URL string or the resource ID.
            // Explicitly checking for null/empty/invalid URL before calling .load() with the URL
            // helps prevent the "Received null model" warning.

            Object modelToLoad;

            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equalsIgnoreCase("null")) {
                modelToLoad = imageUrl; // Use the actual URL
            } else {
                modelToLoad = R.drawable.barber_sample; // Use the local resource ID
            }

            Glide.with(itemView.getContext())
                    .load(modelToLoad)
                    .placeholder(R.drawable.barber_sample) // Show default while loading
                    .error(R.drawable.barber_sample)       // Show default if loading fails (only relevant if modelToLoad was a URL)
                    .into(barberImage);

            editButton.setOnClickListener(v -> listener.onEditClick(barber));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(barber));
        }
    }

    private static final DiffUtil.ItemCallback<Barber> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Barber>() {
                @Override
                public boolean areItemsTheSame(@NonNull Barber oldItem, @NonNull Barber newItem) {
                    return Objects.equals(oldItem.getBarberId(), newItem.getBarberId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Barber oldItem, @NonNull Barber newItem) {
                    // Safe comparison for all fields, including the new 'dayOff'
                    return Objects.equals(oldItem.getName(), newItem.getName()) &&
                            Objects.equals(oldItem.getSpecialization(), newItem.getSpecialization()) &&
                            Objects.equals(oldItem.getDayOff(), newItem.getDayOff()) &&
                            Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl());
                }
            };
}