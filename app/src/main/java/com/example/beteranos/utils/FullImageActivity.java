package com.example.beteranos.utils; // You might put this in a 'utils' package

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R;

/**
 * Activity to display an image (like a payment receipt) in full screen.
 * We use a static reference/cache key passed via Intent to avoid IPC limit errors.
 */
public class FullImageActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIPT_KEY = "receipt_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image); // You must create this XML layout

        ImageView fullImageView = findViewById(R.id.full_screen_image_view);

        // Use the default action bar to show a back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // 1. Retrieve the unique key for the data
        int reservationId = getIntent().getIntExtra(EXTRA_RECEIPT_KEY, -1);

        if (reservationId != -1) {
            // 2. Retrieve the byte array from the ViewModel's static cache/map
            byte[] receiptBytes = SharedImageCache.getReceiptBytes(reservationId);

            if (receiptBytes != null && receiptBytes.length > 0) {
                try {
                    // 3. Convert byte[] to Bitmap and display
                    Bitmap bitmap = BitmapFactory.decodeByteArray(receiptBytes, 0, receiptBytes.length);
                    if (bitmap != null) {
                        fullImageView.setImageBitmap(bitmap);
                        // Optional: Integrate a third-party library here for pinch-to-zoom (like PhotoView)
                    } else {
                        Toast.makeText(this, "Failed to decode image data.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading image.", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Image data not found.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid request key.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Handle the back button on the action bar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}