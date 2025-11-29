package com.example.beteranos.ui_reservation.home.data_barber; // ⭐️ Correct Package

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.beteranos.R;
import com.example.beteranos.databinding.ItemBarberProfileBinding;
import com.example.beteranos.models.Barber;

public class BarberProfileAdapter extends ListAdapter<Barber, BarberProfileAdapter.BarberViewHolder> {

    public BarberProfileAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBarberProfileBinding binding = ItemBarberProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BarberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class BarberViewHolder extends RecyclerView.ViewHolder {
        private final ItemBarberProfileBinding binding;

        public BarberViewHolder(@NonNull ItemBarberProfileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Barber barber) {
            binding.tvBarberName.setText(barber.getName());
            binding.tvSpecialization.setText(barber.getSpecialization());

            String dayOff = barber.getDayOff();
            if (dayOff != null && !dayOff.isEmpty() && !dayOff.equalsIgnoreCase("No day off")) {
                binding.tvDayOff.setText("Day Off: " + dayOff);
                binding.tvDayOff.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvDayOff.setVisibility(android.view.View.GONE);
            }

            Glide.with(itemView.getContext())
                    .load(barber.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(binding.ivBarberImage);
        }
    }

    private static final DiffUtil.ItemCallback<Barber> DIFF_CALLBACK = new DiffUtil.ItemCallback<Barber>() {
        @Override
        public boolean areItemsTheSame(@NonNull Barber oldItem, @NonNull Barber newItem) {
            return oldItem.getBarberId() == newItem.getBarberId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Barber oldItem, @NonNull Barber newItem) {
            return oldItem.equals(newItem);
        }
    };
}