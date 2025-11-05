package com.example.beteranos.ui_reservation.home.data_barber;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
// Import your Data Model:
import com.example.beteranos.ui_reservation.home.data_barber.DataModel_Barber;

import java.util.List;

public class BarberProfileAdapter extends RecyclerView.Adapter<BarberProfileAdapter.BarberViewHolder> {

    // List to hold the data that the RecyclerView will display
    private List<DataModel_Barber> barberList;

    // 1. Constructor: Used to pass the data list when creating the adapter
    public BarberProfileAdapter(List<DataModel_Barber> barberList) {
        this.barberList = barberList;
    }

    // 2. ViewHolder Class: Holds references to the views in card_barber_details.xml
    public static class BarberViewHolder extends RecyclerView.ViewHolder {

        // Match these variables to the IDs in card_barber_details.xml
        TextView barberName;
        TextView barberDescription;
        ImageView barberImage;

        public BarberViewHolder(View itemView) {
            super(itemView);
            // Link variables to the specific views using their IDs
            barberName = itemView.findViewById(R.id.barber_name);
            barberDescription = itemView.findViewById(R.id.barber_description);
            barberImage = itemView.findViewById(R.id.barber_image);
        }
    }

    // 3. onCreateViewHolder: Inflates the XML layout (the single card)
    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // R.layout.card_barber_details is the layout for a single list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_barber_details, parent, false);
        return new BarberViewHolder(view);
    }

    // 4. onBindViewHolder: Binds the data from the list to the views in the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        DataModel_Barber currentBarber = barberList.get(position);

        // Set the text and image using the getter methods from the DataModel
        holder.barberName.setText(currentBarber.getName());
        holder.barberDescription.setText(currentBarber.getDescription());
        holder.barberImage.setImageResource(currentBarber.getImageResourceId());

        // Optional: Add an item click listener here if needed
        // holder.itemView.setOnClickListener(v -> { /* Handle click */ });
    }

    // 5. getItemCount: Tells the RecyclerView how many items are in the list
    @Override
    public int getItemCount() {
        return barberList.size();
    }
}