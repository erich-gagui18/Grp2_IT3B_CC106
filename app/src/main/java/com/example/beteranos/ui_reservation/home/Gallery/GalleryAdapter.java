package com.example.beteranos.ui_reservation.home.Gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beteranos.R;
import com.example.beteranos.models.Gallery;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private List<Gallery> imageList;

    // Constructor accepts list of Gallery (Database Model)
    public GalleryAdapter(List<Gallery> imageList) {
        this.imageList = imageList;
    }

    public void updateData(List<Gallery> newImages) {
        this.imageList = newImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ⭐️ FIX: Inflate the specific customer layout we just created
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery_image_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gallery image = imageList.get(position);
        byte[] data = image.getImageData();

        if (data != null && data.length > 0) {
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            holder.imageView.setImageBitmap(bmp);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
        }

        // No need to hide delete button anymore, because it doesn't exist in the XML!
    }

    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        // View deleteBtn; // Optional if you are reusing the admin layout

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_gallery_image);
            // deleteBtn = itemView.findViewById(R.id.btn_delete_image);
        }
    }
}