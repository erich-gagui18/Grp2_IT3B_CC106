package com.example.beteranos.ui_admin.management.products;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton; // Changed View to ImageButton for clarity

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Product;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductsManagementAdapter extends ListAdapter<Product, ProductsManagementAdapter.ViewHolder> {

    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    private final OnProductActionListener listener;

    public ProductsManagementAdapter(OnProductActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductsManagementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure this layout name matches your XML file exactly (item_product.xml)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsManagementAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText, priceText, stockText;
        private final ImageView imageView;
        private final ImageButton editBtn, deleteBtn; // Changed to ImageButton to match XML

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // ⭐️ Verify these IDs match the XML exactly!
            nameText = itemView.findViewById(R.id.product_name_text);
            priceText = itemView.findViewById(R.id.product_price_text);
            stockText = itemView.findViewById(R.id.product_stock_text);
            imageView = itemView.findViewById(R.id.product_image_view);
            editBtn = itemView.findViewById(R.id.btn_edit_product);
            deleteBtn = itemView.findViewById(R.id.btn_delete_product);

            // Null check is a good safety practice, though strictly unnecessary if XML is valid
            if (editBtn != null) {
                editBtn.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) listener.onEditClick(getItem(pos));
                });
            }

            if (deleteBtn != null) {
                deleteBtn.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) listener.onDeleteClick(getItem(pos));
                });
            }
        }

        void bind(Product p) {
            nameText.setText(p.getName());

            // Use the utility method from Product class if available, or formatting here
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "PH")); // Philippines Peso
            priceText.setText(nf.format(p.getPrice()));

            stockText.setText("Stock: " + p.getStock());

            byte[] img = p.getImageBytes();
            if (img != null && img.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(img, 0, img.length);
                imageView.setImageBitmap(bmp);
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Product>() {
                @Override
                public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.equals(newItem); // Relies on the equals() method we fixed in Product.java
                }
            };
}