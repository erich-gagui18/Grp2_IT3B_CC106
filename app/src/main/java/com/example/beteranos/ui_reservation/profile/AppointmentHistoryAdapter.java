package com.example.beteranos.ui_reservation.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentHistoryAdapter extends RecyclerView.Adapter<AppointmentHistoryAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_history, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText;
        private final TextView timeText; // ⭐️ NEW: TextView for the time
        private final TextView statusText;
        private final TextView barberText;
        private final TextView serviceText;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_appointment_date);
            timeText = itemView.findViewById(R.id.tv_appointment_time); // ⭐️ NEW: Find the time TextView
            statusText = itemView.findViewById(R.id.tv_appointment_status);
            barberText = itemView.findViewById(R.id.tv_barber_name);
            serviceText = itemView.findViewById(R.id.tv_service_name);
        }

        public void bind(Appointment appointment) {
            // 1. Format the Date part (e.g., November 07, 2025)
            SimpleDateFormat sdfDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            dateText.setText(sdfDate.format(appointment.getReservationTime()));

            // 2. Format the Time part (e.g., 03:23 AM)
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.US);
            timeText.setText(sdfTime.format(appointment.getReservationTime())); // ⭐️ Set the time ⭐️

            barberText.setText("Barber: " + appointment.getBarberName());
            serviceText.setText("Service: " + appointment.getServiceName());
            statusText.setText(appointment.getStatus());

            // ⭐️ UPDATED Status Logic: Uses custom R.color resources ⭐️
            // We set the status text color dynamically, relying on the XML drawable for the background.
            int textColorResId;
            int defaultColor = android.R.color.darker_gray;

            // Note: Since the SCHEDULED background is light green, its text color should be black.
            switch (appointment.getStatus().toLowerCase()) {
                case "pending":
                    textColorResId = R.color.status_pending; // Use custom color
                    break;
                case "scheduled":
                case "confirmed":
                    // Use black text for the light status_scheduled background for contrast
                    textColorResId = R.color.black;
                    break;
                case "cancelled":
                    textColorResId = R.color.status_cancelled; // Use custom color
                    break;
                case "completed":
                    textColorResId = R.color.status_completed; // Use custom color
                    break;
                default:
                    textColorResId = defaultColor;
                    break;
            }

            // Set the final status text color
            if (textColorResId == defaultColor) {
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), defaultColor));
            } else {
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorResId));
            }
        }
    }
}