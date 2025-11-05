package com.example.beteranos.ui_reservation.home.data_barber;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RatingBar; // ⭐️ NEW: Import RatingBar ⭐️
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.ui_reservation.home.data_barber.DataModel_Barber;

import java.util.List;

public class BarberProfileAdapter extends RecyclerView.Adapter<BarberProfileAdapter.BarberViewHolder> {

    private List<DataModel_Barber> barberList;

    public BarberProfileAdapter(List<DataModel_Barber> barberList) {
        this.barberList = barberList;
    }

    public static class BarberViewHolder extends RecyclerView.ViewHolder {

        TextView barberName;
        TextView barberDescription;
        ImageView barberImage;
        RatingBar barberRating; // ⭐️ NEW: Reference for the RatingBar ⭐️

        public BarberViewHolder(View itemView) {
            super(itemView);

            // Link variables to the specific views using their IDs
            barberName = itemView.findViewById(R.id.barber_name);
            barberDescription = itemView.findViewById(R.id.barber_description);
            barberImage = itemView.findViewById(R.id.barber_image);
            barberRating = itemView.findViewById(R.id.barber_rating); // ⭐️ NEW: Link the RatingBar ID ⭐️
        }
    }

    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_barber_details, parent, false);
        return new BarberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        DataModel_Barber currentBarber = barberList.get(position);

        // Set the text and image using the getter methods from the DataModel
        holder.barberName.setText(currentBarber.getName());
        holder.barberDescription.setText(currentBarber.getDescription());
        holder.barberImage.setImageResource(currentBarber.getImageResourceId());

        // ⭐️ NEW: Set the rating using the data from the DataModel ⭐️
        holder.barberRating.setRating(currentBarber.getRating());

        // Optional: Add an item click listener here if needed
    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }
}