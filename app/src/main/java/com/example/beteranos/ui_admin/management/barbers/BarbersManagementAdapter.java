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

import com.example.beteranos.R;
import com.example.beteranos.models.Barber;

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
        private final ImageButton editButton, deleteButton;

        public BarberViewHolder(@NonNull View itemView) {
            super(itemView);
            barberImage = itemView.findViewById(R.id.barber_image);
            nameText = itemView.findViewById(R.id.barber_name_text);
            specText = itemView.findViewById(R.id.barber_spec_text);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Barber barber, OnBarberActionListener listener) {
            nameText.setText(barber.getName());
            specText.setText(barber.getSpecialization());

            // TODO: (TODO #9) Handle image loading here
            // Example: Glide.with(itemView.getContext()).load(barber.getImageUrl()).into(barberImage);
            // For now, it uses the placeholder from the XML.

            editButton.setOnClickListener(v -> listener.onEditClick(barber));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(barber));
        }
    }

    private static final DiffUtil.ItemCallback<Barber> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Barber>() {
                @Override
                public boolean areItemsTheSame(@NonNull Barber oldItem, @NonNull Barber newItem) {
                    return oldItem.getBarberId() == newItem.getBarberId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Barber oldItem, @NonNull Barber newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            java.util.Objects.equals(oldItem.getSpecialization(), newItem.getSpecialization());
                }
            };
}