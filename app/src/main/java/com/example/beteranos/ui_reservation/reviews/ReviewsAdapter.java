package com.example.beteranos.ui_reservation.reviews;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.beteranos.R;
import com.example.beteranos.models.Review;
import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.SharedImageCache;

import java.text.SimpleDateFormat;
import java.util.Arrays; // Needed for Arrays.equals
import java.util.Locale;
import java.util.Objects;

public class ReviewsAdapter extends ListAdapter<Review, ReviewsAdapter.ReviewViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public ReviewsAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = getItem(position);
        holder.bind(review);
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView customerName, barberName, reviewDate, reviewComment;
        private final RatingBar ratingIndicator;
        private final ImageView reviewImage; // ⭐️ New Field

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.tv_customer_name);
            barberName = itemView.findViewById(R.id.tv_barber_name);
            reviewDate = itemView.findViewById(R.id.tv_review_date);

            // ⭐️ UPDATED IDs to match the new XML layout
            reviewComment = itemView.findViewById(R.id.tv_comment);
            ratingIndicator = itemView.findViewById(R.id.rating_bar);

            // ⭐️ Bind the new ImageView
            reviewImage = itemView.findViewById(R.id.iv_review_image);
        }

        public void bind(Review review) {
            customerName.setText(review.getCustomerName());
            barberName.setText("for " + review.getBarberName());
            ratingIndicator.setRating(review.getRating());
            reviewDate.setText(DATE_FORMAT.format(review.getCreatedAt()));

            // Comment Logic
            if (review.getComment() != null && !review.getComment().isEmpty()) {
                reviewComment.setText(review.getComment());
                reviewComment.setVisibility(View.VISIBLE);
            } else {
                reviewComment.setVisibility(View.GONE);
            }

            // ⭐️ IMAGE LOADING LOGIC ⭐️
            byte[] imageBytes = review.getReviewImage();
            if (imageBytes != null && imageBytes.length > 0) {
                reviewImage.setVisibility(View.VISIBLE);

                // Load with Glide + Round Corners for better UI
                Glide.with(itemView.getContext())
                        .load(imageBytes)
                        .transform(new CenterCrop(), new RoundedCorners(24)) // 24 = matches corner radius
                        .into(reviewImage);

                // Click to view Full Screen
                reviewImage.setOnClickListener(v -> {
                    // Store bytes in static cache to pass large data
                    SharedImageCache.putReceiptBytes(review.getReviewId(), imageBytes);

                    Intent intent = new Intent(itemView.getContext(), FullImageActivity.class);
                    // Use Review ID as the key
                    intent.putExtra(FullImageActivity.EXTRA_RECEIPT_KEY, review.getReviewId());
                    itemView.getContext().startActivity(intent);
                });

            } else {
                reviewImage.setVisibility(View.GONE);
                reviewImage.setOnClickListener(null);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Review> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Review>() {
                @Override
                public boolean areItemsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
                    return oldItem.getReviewId() == newItem.getReviewId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
                    // Check all visual fields including image
                    return oldItem.getRating() == newItem.getRating() &&
                            Objects.equals(oldItem.getComment(), newItem.getComment()) &&
                            Arrays.equals(oldItem.getReviewImage(), newItem.getReviewImage());
                }
            };
}