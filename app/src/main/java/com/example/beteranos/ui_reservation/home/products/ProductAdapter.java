package com.example.beteranos.ui_reservation.home.products;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beteranos.R;
import com.example.beteranos.models.Product;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private final OnProductClickListener listener; // Click listener

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ⭐️ CRITICAL: Inflate the correct, polished layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_customer, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set Text
        holder.name.setText(product.getName());
        holder.price.setText(String.format(Locale.US, "₱%.2f", product.getPrice()));
        holder.stock.setText("Stock: " + product.getStock());

        // Handle Image
        byte[] imageBytes = product.getImageBytes();
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.image.setImageBitmap(bitmap);

            // Enable click only if image exists
            holder.image.setOnClickListener(v -> listener.onProductClick(product));
        } else {
            holder.image.setImageResource(R.drawable.ic_image_placeholder);
            holder.image.setOnClickListener(null); // Disable click on placeholder
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, stock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_product_image);
            name = itemView.findViewById(R.id.tv_product_name);
            price = itemView.findViewById(R.id.tv_product_price);
            stock = itemView.findViewById(R.id.tv_product_stock);
        }
    }
}