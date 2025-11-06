package com.example.beteranos.ui_reservation.home.notifications; // <-- Correct package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Notification; // Import your new model

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.NotificationViewHolder> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);

    public NotificationAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = getItem(position);
        holder.bind(notification, dateFormat);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView title, body, time;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_icon);
            title = itemView.findViewById(R.id.tv_notification_title);
            body = itemView.findViewById(R.id.tv_notification_body);
            time = itemView.findViewById(R.id.tv_notification_time);
        }

        public void bind(Notification notification, SimpleDateFormat dateFormat) {
            title.setText(notification.getTitle());
            body.setText(notification.getBody());

            if (notification.getCreatedAt() != null) {
                time.setText(dateFormat.format(notification.getCreatedAt()));
            } else {
                time.setText("");
            }

            if (notification.isRead()) {
                title.setAlpha(0.7f);
                body.setAlpha(0.7f);
            } else {
                title.setAlpha(1.0f);
                body.setAlpha(1.0f);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Notification>() {
                @Override
                public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    return oldItem.getNotificationId() == newItem.getNotificationId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    return oldItem.isRead() == newItem.isRead() &&
                            Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                            Objects.equals(oldItem.getBody(), newItem.getBody());
                }
            };
}