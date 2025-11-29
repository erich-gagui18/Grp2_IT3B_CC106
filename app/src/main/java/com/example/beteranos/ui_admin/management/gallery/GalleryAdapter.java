package com.example.beteranos.ui_admin.management.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beteranos.R;
import com.example.beteranos.models.Gallery;

public class GalleryAdapter extends ListAdapter<Gallery, GalleryAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Gallery image);
    }

    private final OnDeleteClickListener deleteListener;

    public GalleryAdapter(OnDeleteClickListener deleteListener) {
        super(DIFF_CALLBACK);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We will create this layout in the next step
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_gallery_image);
            btnDelete = itemView.findViewById(R.id.btn_delete_image);

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    deleteListener.onDeleteClick(getItem(pos));
                }
            });
        }

        void bind(Gallery image) {
            byte[] data = image.getImageData();
            if (data != null && data.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageView.setImageBitmap(bmp);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Gallery> DIFF_CALLBACK = new DiffUtil.ItemCallback<Gallery>() {
        @Override
        public boolean areItemsTheSame(@NonNull Gallery oldItem, @NonNull Gallery newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Gallery oldItem, @NonNull Gallery newItem) {
            return oldItem.equals(newItem);
        }
    };
}