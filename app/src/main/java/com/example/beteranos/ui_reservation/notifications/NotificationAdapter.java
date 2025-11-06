package com.example.beteranos.ui_reservation.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Appointment> notifications = new ArrayList<>();

    public void setNotifications(List<Appointment> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView title, message, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_status_icon);
            title = itemView.findViewById(R.id.tv_notification_title);
            message = itemView.findViewById(R.id.tv_notification_message);
            time = itemView.findViewById(R.id.tv_notification_time);
        }

        public void bind(Appointment notification) {
            String status = notification.getStatus();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            String date = sdf.format(notification.getReservationTime());

            title.setText("Appointment " + status);
            message.setText("Your booking with " + notification.getBarberName() + " on " + date);
            time.setText(""); // You could calculate time ago here if needed

            int iconRes;
            int colorRes;

            switch (status.toLowerCase()) {
                case "pending":
                    iconRes = R.drawable.ic_time_notif;
                    colorRes = android.R.color.holo_orange_dark;
                    break;
                case "scheduled":
                case "confirmed":
                    iconRes = R.drawable.ic_check_notif;
                    colorRes = android.R.color.holo_green_dark;
                    break;
                case "cancelled":
                    iconRes = R.drawable.ic_close_notif;
                    colorRes = android.R.color.holo_red_dark;
                    break;
                default: // "Completed" etc.
                    iconRes = R.drawable.ic_notifications;
                    colorRes = android.R.color.darker_gray;
                    break;
            }
            icon.setImageResource(iconRes);
            title.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        }
    }
}