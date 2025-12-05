package com.example.beteranos.ui_reservation.home.data_barber;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.beteranos.R;
import com.example.beteranos.models.Barber;

import java.util.ArrayList;
import java.util.List;

public class BarberProfileAdapter extends RecyclerView.Adapter<BarberProfileAdapter.BarberViewHolder> {

    private List<Barber> barberList = new ArrayList<>();

    public void submitList(List<Barber> barbers) {
        this.barberList = barbers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barber_profile, parent, false);
        return new BarberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        holder.bind(barberList.get(position));
    }

    @Override
    public int getItemCount() {
        return barberList != null ? barberList.size() : 0;
    }

    static class BarberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBarberImage;
        TextView tvName, tvSpecialization, tvDayOff;

        public BarberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBarberImage = itemView.findViewById(R.id.iv_barber_image);
            tvName = itemView.findViewById(R.id.tv_barber_name);
            tvSpecialization = itemView.findViewById(R.id.tv_specialization);
            tvDayOff = itemView.findViewById(R.id.tv_day_off);
        }

        public void bind(Barber barber) {
            tvName.setText(barber.getName());

            // ⭐️ FIX: Check for null specialization and display "N/A"
            tvSpecialization.setTypeface(null, Typeface.ITALIC);

            String specialization = barber.getSpecialization();
            if (specialization == null || specialization.trim().isEmpty()) {
                specialization = "N/A";
            }
            tvSpecialization.setText("Specializes: " + specialization);

            // ⭐️ ROBUST IMAGE LOADING with barber_sample DEFAULT ⭐️
            String imageUrl = barber.getImageUrl();
            boolean isValidUrl = imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("/");

            if (isValidUrl) {
                try {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.barber_sample)
                            .error(R.drawable.barber_sample)
                            .fallback(R.drawable.barber_sample)
                            .centerCrop()
                            .into(ivBarberImage);
                } catch (Exception e) {
                    ivBarberImage.setImageResource(R.drawable.barber_sample);
                }
            } else {
                ivBarberImage.setImageResource(R.drawable.barber_sample);
            }

            // Day Off Logic (Green for Available, Red for Day Off)
            String dayOff = barber.getDayOff();
            boolean isInvalidData = dayOff != null && (dayOff.startsWith("content:") || dayOff.startsWith("/"));
            boolean hasSpecificDayOff = dayOff != null && !dayOff.isEmpty()
                    && !dayOff.equalsIgnoreCase("No day off")
                    && !isInvalidData;

            if (hasSpecificDayOff) {
                // RED Style
                String fullText = "Day Off: " + dayOff;
                tvDayOff.setText(fullText);
                tvDayOff.setBackgroundResource(R.drawable.rounded_status_cancelled);
            } else {
                // GREEN Style
                tvDayOff.setText("No Day Off");
                tvDayOff.setBackgroundResource(R.drawable.rounded_status_green);
            }
            tvDayOff.setVisibility(View.VISIBLE);
        }
    }
}