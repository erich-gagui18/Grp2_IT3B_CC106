package com.example.beteranos.ui_reservation.reviews;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.beteranos.databinding.FragmentReviewsBinding;
import com.example.beteranos.models.Barber;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewsFragment extends Fragment {

    private ReviewsViewModel mViewModel;
    private FragmentReviewsBinding binding;
    private ReviewsAdapter adapter;
    private List<Barber> barberList; // To map selected barber name back to ID
    private int customerId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReviewsBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(ReviewsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get customer ID
        customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        // Handle guest users
        if (customerId == -1) {
            binding.tvGuestMessage.setVisibility(View.VISIBLE);
        } else {
            binding.tvGuestMessage.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        adapter = new ReviewsAdapter();
        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewsRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnSubmitReview.setOnClickListener(v -> {
            if (customerId == -1) {
                mViewModel.submitReview(customerId, 0, 0, ""); // ViewModel will handle the error toast
                return;
            }

            // Get data from form
            String selectedBarberName = binding.barberSpinnerDropdown.getText().toString();
            int rating = (int) binding.ratingBarInput.getRating();
            String comment = binding.commentEditText.getText().toString().trim();

            // Validate inputs
            if (selectedBarberName.isEmpty()) {
                Toast.makeText(getContext(), "Please select a barber", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rating == 0) {
                Toast.makeText(getContext(), "Please provide a rating", Toast.LENGTH_SHORT).show();
                return;
            }
            // Comment is optional

            // Find the ID of the selected barber
            int selectedBarberId = -1;
            if (barberList != null) {
                for (Barber barber : barberList) {
                    if (barber.getName().equals(selectedBarberName)) {
                        selectedBarberId = barber.getBarberId();
                        break;
                    }
                }
            }

            if (selectedBarberId == -1) {
                Toast.makeText(getContext(), "Invalid barber selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Submit to ViewModel
            mViewModel.submitReview(customerId, selectedBarberId, rating, comment);
        });
    }

    private void setupObservers() {
        // Observer for the list of all barbers (for the dropdown)
        mViewModel.allBarbers.observe(getViewLifecycleOwner(), barbers -> {
            if (barbers != null && !barbers.isEmpty()) {
                this.barberList = barbers; // Save the list for later ID lookup
                // Get just the names for the adapter
                List<String> barberNames = barbers.stream()
                        .map(Barber::getName)
                        .collect(Collectors.toList());
                ArrayAdapter<String> barberAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        barberNames
                );
                binding.barberSpinnerDropdown.setAdapter(barberAdapter);
            }
        });

        // Observer for the list of reviews
        mViewModel.reviewsList.observe(getViewLifecycleOwner(), reviews -> {
            adapter.submitList(reviews);
            if (reviews == null || reviews.isEmpty()) {
                binding.emptyListText.setVisibility(View.VISIBLE);
                binding.reviewsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyListText.setVisibility(View.GONE);
                binding.reviewsRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Observer for loading state
        mViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSubmitReview.setEnabled(!isLoading); // Disable button while loading
        });

        // Observer for toast messages (from submit, fetch errors, etc.)
        mViewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                mViewModel.clearToastMessage(); // Reset signal

                // If review was posted successfully, clear the form
                if (message.contains("success")) {
                    binding.barberSpinnerDropdown.setText("", false);
                    binding.ratingBarInput.setRating(0);
                    binding.commentEditText.setText("");
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}