package com.example.beteranos.ui_reservation.reviews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.models.Review;

import java.text.SimpleDateFormat;
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

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.tv_customer_name);
            barberName = itemView.findViewById(R.id.tv_barber_name);
            reviewDate = itemView.findViewById(R.id.tv_review_date);
            reviewComment = itemView.findViewById(R.id.tv_review_comment);
            ratingIndicator = itemView.findViewById(R.id.rating_bar_indicator);
        }

        public void bind(Review review) {
            customerName.setText(review.getCustomerName());
            barberName.setText("for " + review.getBarberName());
            ratingIndicator.setRating(review.getRating());
            reviewDate.setText(DATE_FORMAT.format(review.getCreatedAt()));

            if (review.getComment() != null && !review.getComment().isEmpty()) {
                reviewComment.setText(review.getComment());
                reviewComment.setVisibility(View.VISIBLE);
            } else {
                reviewComment.setVisibility(View.GONE);
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
                    return oldItem.getRating() == newItem.getRating() &&
                            Objects.equals(oldItem.getComment(), newItem.getComment());
                }
            };
}