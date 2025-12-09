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
        void onToggleVisibilityClick(Barber barber);
    }

    public BarbersManagementAdapter(@NonNull OnBarberActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_barber, parent, false);
        return new BarberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        Barber barber = getItem(position);
        holder.bind(barber, listener);
    }

    static class BarberViewHolder extends RecyclerView.ViewHolder {
        private final ImageView barberImage;
        private final TextView nameText, specText;
        private final TextView dayOffText;
        private final ImageButton editButton, deleteButton, toggleButton;

        public BarberViewHolder(@NonNull View itemView) {
            super(itemView);
            barberImage = itemView.findViewById(R.id.barber_image);
            nameText = itemView.findViewById(R.id.barber_name_text);
            specText = itemView.findViewById(R.id.barber_spec_text);
            dayOffText = itemView.findViewById(R.id.barber_day_off_text);

            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            toggleButton = itemView.findViewById(R.id.toggle_visibility_button);
        }

        public void bind(Barber barber, OnBarberActionListener listener) {
            nameText.setText(barber.getName());
            specText.setText(barber.getSpecialization());

            // ⭐️ UPDATED: Bind Day Off AND Schedule Time ⭐️
            String dayOff = (barber.getDayOff() != null && !barber.getDayOff().isEmpty()) ? barber.getDayOff() : "None";
            String start = (barber.getStartTime() != null) ? barber.getStartTime() : "N/A";
            String end = (barber.getEndTime() != null) ? barber.getEndTime() : "N/A";

            // Format: "Day Off: Monday | 8:00 am - 7:00 pm"
            String scheduleDisplay = String.format("Day Off: %s\n%s - %s", dayOff, start, end);
            dayOffText.setText(scheduleDisplay);

            // Visibility Logic
            if (barber.isActive()) {
                toggleButton.setImageResource(R.drawable.ic_visibility);
                toggleButton.setAlpha(1.0f);
            } else {
                toggleButton.setImageResource(R.drawable.ic_visibility_off);
                toggleButton.setAlpha(0.5f);
            }

            // Image Loading
            String imageUrl = barber.getImageUrl();
            Object modelToLoad;

            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equalsIgnoreCase("null")) {
                modelToLoad = imageUrl;
            } else {
                modelToLoad = R.drawable.barber_sample;
            }

            Glide.with(itemView.getContext())
                    .load(modelToLoad)
                    .placeholder(R.drawable.barber_sample)
                    .error(R.drawable.barber_sample)
                    .into(barberImage);

            // Click Listeners
            editButton.setOnClickListener(v -> listener.onEditClick(barber));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(barber));
            toggleButton.setOnClickListener(v -> listener.onToggleVisibilityClick(barber));
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
                    return Objects.equals(oldItem.getName(), newItem.getName()) &&
                            Objects.equals(oldItem.getSpecialization(), newItem.getSpecialization()) &&
                            Objects.equals(oldItem.getDayOff(), newItem.getDayOff()) &&
                            Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl()) &&
                            oldItem.isActive() == newItem.isActive() &&
                            // ⭐️ UPDATED: Check for Time Changes
                            Objects.equals(oldItem.getStartTime(), newItem.getStartTime()) &&
                            Objects.equals(oldItem.getEndTime(), newItem.getEndTime());
                }
            };
}