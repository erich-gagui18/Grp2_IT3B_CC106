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
        private final TextView timeText;
        private final TextView statusText;
        private final TextView barberText;
        private final TextView serviceText;
        private final TextView locationText; // ⭐️ NEW Field

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_appointment_date);
            timeText = itemView.findViewById(R.id.tv_appointment_time);
            statusText = itemView.findViewById(R.id.tv_appointment_status);
            barberText = itemView.findViewById(R.id.tv_barber_name);
            serviceText = itemView.findViewById(R.id.tv_service_name);
            locationText = itemView.findViewById(R.id.tv_appointment_location); // ⭐️ Find ID
        }

        public void bind(Appointment appointment) {
            // 1. Set Date and Time
            SimpleDateFormat sdfDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            // Check for null time to prevent crashes
            if (appointment.getReservationTime() != null) {
                dateText.setText(sdfDate.format(appointment.getReservationTime()));
                SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.US);
                timeText.setText(sdfTime.format(appointment.getReservationTime()));
            } else {
                dateText.setText("N/A");
                timeText.setText("");
            }

            // 2. Set Details
            barberText.setText("Barber: " + appointment.getBarberName());
            serviceText.setText("Service: " + appointment.getServiceName());

            // --- ⭐️ NEW: Location Logic ⭐️ ---
            String location = appointment.getServiceLocation();
            if (location == null || location.isEmpty()) location = "Barbershop"; // Default

            if ("Home Service".equalsIgnoreCase(location)) {
                String address = appointment.getHomeAddress();
                if (address != null && !address.trim().isEmpty()) {
                    // Display the specific address
                    locationText.setText("Home: " + address);
                } else {
                    // Fallback if address is missing
                    locationText.setText("Location: Home Service");
                }
            } else {
                // Display simple "Barbershop"
                locationText.setText("Location: Barbershop");
            }
            // --- ⭐️ END Location Logic ⭐️ ---


            // 3. Dynamic Status Styling
            statusText.setText(appointment.getStatus());
            int backgroundDrawableResId;
            int textColorResId;

            // Default to Scheduled style
            backgroundDrawableResId = R.drawable.rounded_status_scheduled;
            textColorResId = R.color.black;

            String status = appointment.getStatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "pending":
                        backgroundDrawableResId = R.drawable.rounded_status_pending;
                        textColorResId = R.color.black;
                        break;
                    case "cancelled":
                        backgroundDrawableResId = R.drawable.rounded_status_cancelled;
                        textColorResId = R.color.white;
                        break;
                    case "completed":
                        backgroundDrawableResId = R.drawable.rounded_status_completed;
                        textColorResId = R.color.white;
                        break;
                    case "scheduled":
                    case "confirmed":
                        backgroundDrawableResId = R.drawable.rounded_status_scheduled;
                        textColorResId = R.color.black;
                        break;
                }
            }

            statusText.setBackgroundResource(backgroundDrawableResId);
            statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorResId));
        }
    }
}