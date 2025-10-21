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
        private final TextView statusText;
        private final TextView barberText;
        private final TextView serviceText;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_appointment_date);
            statusText = itemView.findViewById(R.id.tv_appointment_status);
            barberText = itemView.findViewById(R.id.tv_barber_name);
            serviceText = itemView.findViewById(R.id.tv_service_name);
        }

        public void bind(Appointment appointment) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.US);
            dateText.setText(sdf.format(appointment.getReservationTime()));
            barberText.setText("Barber: " + appointment.getBarberName());
            serviceText.setText("Service: " + appointment.getServiceName());
            statusText.setText(appointment.getStatus());

            int color;
            switch (appointment.getStatus().toLowerCase()) {
                case "pending":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                    break;
                case "scheduled":
                case "confirmed":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                    break;
                case "cancelled":
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
                    break;
                case "completed":
                    color = ContextCompat.getColor(itemView.getContext(), R.color.button_active_gray);
                    break;
                default:
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
                    break;
            }
            statusText.setTextColor(color);
        }
    }
}