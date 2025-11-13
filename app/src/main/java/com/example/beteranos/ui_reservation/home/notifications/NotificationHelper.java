package com.example.beteranos.ui_reservation.home.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.beteranos.R; // Assumes R is in the main package structure

/**
 * Utility class responsible for creating, configuring, and displaying system status bar notifications.
 * It ensures the Notification Channel is set up with high importance for Heads-up notifications
 * and handles the PendingIntent for user interaction.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "appointment_status_channel";
    private static final String CHANNEL_NAME = "Appointment Status Updates";
    private static final String CHANNEL_DESCRIPTION = "Alerts for confirmed, canceled, or completed appointments.";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        // Use Application Context to prevent memory leaks
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Create the channel immediately when the helper is instantiated
        createNotificationChannel();
    }

    /**
     * Creates the notification channel required for Android API 26 (Oreo) and higher.
     * Setting importance to IMPORTANCE_HIGH enables Heads-up notifications.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ⭐️ IMPORTANCE_HIGH is essential for heads-up (pop-up) style notifications
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Builds and displays a system notification that will pop up on the user's screen.
     * @param title The brief title of the notification (e.g., "Appointment Confirmed").
     * @param body The detailed message content.
     * @param pendingIntent The action to perform when the user taps the notification.
     */
    public void showNotification(String title, String body, PendingIntent pendingIntent) {
        // Assumes R.drawable.ic_menu is a valid placeholder icon
        // You should replace this with a dedicated small notification icon.
        int smallIcon = R.drawable.ic_notifications;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(body)
                // Use BigTextStyle to display the full body text prominently
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                // ⭐️ Setting priority to HIGH ensures it pops up on older Android versions (pre-Oreo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                // Set default vibration and sound for visual and audio cue
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        // Issue the notification using a unique ID (current time)
        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}