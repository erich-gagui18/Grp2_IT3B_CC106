package com.example.beteranos.ui_admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AdminAppointmentAdapter extends ListAdapter<Appointment, AdminAppointmentAdapter.AppointmentViewHolder> {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

    // --- THIS IS THE MESSENGER (INTERFACE) ---
    private final OnAppointmentActionListener listener;
    public interface OnAppointmentActionListener {
        void onConfirmClicked(Appointment appointment);
        void onCancelClicked(Appointment appointment);
        void onMarkAsCompletedClicked(Appointment appointment);
    }
    // --- END INTERFACE ---

    // --- UPDATE CONSTRUCTOR to accept the listener ---
    public AdminAppointmentAdapter(OnAppointmentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View appointmentView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(appointmentView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = getItem(position);
        // --- Pass the listener to the bind method ---
        holder.bind(appointment, timeFormat, listener);
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeText, customerText, serviceText, barberText, statusText;
        private final Button btnConfirm, btnCancel; // --- ADD BUTTONS ---

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointment_time_text);
            customerText = itemView.findViewById(R.id.customer_name_text);
            serviceText = itemView.findViewById(R.id.service_name_text);
            barberText = itemView.findViewById(R.id.barber_name_text);
            statusText = itemView.findViewById(R.id.appointment_status_text);
            // --- FIND BUTTONS ---
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }

        public void bind(Appointment appointment, SimpleDateFormat timeFormat, OnAppointmentActionListener listener) {
            Context context = itemView.getContext();

            timeText.setText(timeFormat.format(appointment.getReservationTime()));
            customerText.setText(appointment.getCustomerName());
            serviceText.setText("Service: " + appointment.getServiceName());
            barberText.setText("Barber: " + appointment.getBarberName());

            int backgroundRes;
            String status = appointment.getStatus();
            statusText.setText(status);

            // --- Handle button and status visibility ---
            switch (status.toLowerCase()) {
                case "completed":
                    backgroundRes = R.drawable.rounded_status_completed;
                    btnConfirm.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    break;
                case "scheduled":
                case "confirmed":
                    backgroundRes = R.drawable.rounded_status_scheduled;

                    // Change the button text and action
                    btnConfirm.setText("Mark as Completed");
                    btnConfirm.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);

                    // Set listeners to the new actions
                    btnConfirm.setOnClickListener(v -> listener.onMarkAsCompletedClicked(appointment));
                    btnCancel.setOnClickListener(v -> listener.onCancelClicked(appointment));
                    break;
                case "cancelled":
                    backgroundRes = R.drawable.rounded_status_cancelled;
                    btnConfirm.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    break;
                default: // Assumes "Pending"
                    backgroundRes = R.drawable.rounded_status_pending;

                    btnConfirm.setText("Confirm");
                    btnConfirm.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);

                    // Set default listeners
                    btnConfirm.setOnClickListener(v -> listener.onConfirmClicked(appointment));
                    btnCancel.setOnClickListener(v -> listener.onCancelClicked(appointment));
                    break;
            }

            // Set the background resource dynamically
            statusText.setBackground(ContextCompat.getDrawable(context, backgroundRes));
            statusText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    private static final DiffUtil.ItemCallback<Appointment> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Appointment>() {
                @Override
                public boolean areItemsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                    return oldItem.getReservationId() == newItem.getReservationId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                    return oldItem.getStatus().equals(newItem.getStatus()) &&
                            oldItem.getReservationTime().equals(newItem.getReservationTime());
                }
            };
}