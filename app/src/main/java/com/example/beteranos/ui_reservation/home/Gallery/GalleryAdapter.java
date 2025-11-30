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
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Gallery item);
    }

    public GalleryAdapter(List<Gallery> imageList, OnItemClickListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    public void updateData(List<Gallery> newImages) {
        this.imageList = newImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ⭐️ Use the polished square layout
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
            holder.imageView.setOnClickListener(v -> listener.onItemClick(image));
        } else {
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
            holder.imageView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_gallery_image);
        }
    }
}