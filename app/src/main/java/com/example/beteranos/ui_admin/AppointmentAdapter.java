package com.example.beteranos.ui_admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beteranos.R;
import com.example.beteranos.models.Appointment;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private final List<Appointment> appointments;

    public AppointmentAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeText, customerText, serviceText, barberText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointment_time_text);
            customerText = itemView.findViewById(R.id.customer_name_text);
            serviceText = itemView.findViewById(R.id.service_name_text);
            barberText = itemView.findViewById(R.id.barber_name_text);
        }

        public void bind(Appointment appointment) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            timeText.setText(timeFormat.format(appointment.getReservationTime()));
            customerText.setText(appointment.getCustomerName());
            serviceText.setText("Service: " + appointment.getServiceName());
            barberText.setText("Barber: " + appointment.getBarberName());
        }
    }
}