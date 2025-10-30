package com.example.beteranos.ui_admin.reservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AdminAppointmentAdapter extends ListAdapter<Appointment, AdminAppointmentAdapter.ViewHolder> {

    private final OnAppointmentActionListener listener;

    public AdminAppointmentAdapter(OnAppointmentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = getItem(position);
        holder.bind(appointment);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeText, customerText, serviceText, barberText, statusText;
        private final Button approveBtn, rejectBtn;
        private final LinearLayout actionButtonsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointment_time_text);
            customerText = itemView.findViewById(R.id.customer_name_text);
            serviceText = itemView.findViewById(R.id.service_name_text);
            barberText = itemView.findViewById(R.id.barber_name_text);
            statusText = itemView.findViewById(R.id.appointment_status_text);

            approveBtn = itemView.findViewById(R.id.btn_approve);
            rejectBtn = itemView.findViewById(R.id.btn_reject);
            actionButtonsContainer = itemView.findViewById(R.id.action_buttons_container);

            // Wire up button click listeners
            approveBtn.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onApprove(getItem(position));
                }
            });

            rejectBtn.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onReject(getItem(position));
                }
            });
        }

        public void bind(Appointment appointment) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            timeText.setText(timeFormat.format(appointment.getReservationTime()));
            customerText.setText(appointment.getCustomerName());
            serviceText.setText("Service: " + appointment.getServiceName());
            barberText.setText("Barber: " + appointment.getBarberName());
            statusText.setText("Status: " + appointment.getStatus());

            // Show buttons only if appointment is pending
            if ("Pending".equalsIgnoreCase(appointment.getStatus())) {
                actionButtonsContainer.setVisibility(View.VISIBLE);
            } else {
                actionButtonsContainer.setVisibility(View.GONE);
            }
        }
    }

    public interface OnAppointmentActionListener {
        void onApprove(Appointment appointment);
        void onReject(Appointment appointment);
    }

    private static final DiffUtil.ItemCallback<Appointment> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Appointment>() {
                @Override
                public boolean areItemsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                    return oldItem.getReservationId() == newItem.getReservationId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
