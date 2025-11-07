package com.example.beteranos.ui_reservation.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.beteranos.ui_customer_login.CustomerLoginActivity;
import com.example.beteranos.MainActivity;
import com.example.beteranos.databinding.FragmentCustomerProfileBinding;
import com.example.beteranos.models.Customer; // This is not needed if you load from Prefs

import static android.content.Context.MODE_PRIVATE;

public class CustomerProfileFragment extends Fragment {

    private CustomerProfileViewModel mViewModel;
    private FragmentCustomerProfileBinding binding;
    private AppointmentHistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCustomerProfileBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(CustomerProfileViewModel.class);

        // --- OPTIMIZATION: Removed redundant setupRecyclerView() call from here ---

        // Get the logged-in customer's ID (from SharedPreferences, which is correct)
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        int customerId = userPrefs.getInt("customer_id", -1);

        if (isLoggedIn && customerId != -1) {
            // User is logged in
            binding.loggedInView.setVisibility(View.VISIBLE);
            binding.guestView.setVisibility(View.GONE);

            setupRecyclerView(); // <-- This call is correct

            // Load data from SharedPreferences
            String firstName = userPrefs.getString("first_name", "");
            String lastName = userPrefs.getString("last_name", "");
            String email = userPrefs.getString("email", "");
            String fullName = firstName + " " + lastName;

            binding.tvCustomerName.setText(fullName);
            binding.tvCustomerEmail.setText(email);

            // Load appointment history
            mViewModel.loadAppointmentHistory(customerId);
            mViewModel.getAppointmentHistory().observe(getViewLifecycleOwner(), appointments -> {
                adapter.setAppointments(appointments);
            });

            binding.btnLogout.setOnClickListener(v -> logout());

        } else {
            // User is a guest
            binding.loggedInView.setVisibility(View.GONE);
            binding.guestView.setVisibility(View.VISIBLE);

            // This line from your old code is not needed if you use the XML above
            // binding.guestView.setBackgroundColor(requireContext().getColor(android.R.color.white));

            binding.btnGoToLogin.setOnClickListener(v -> {
                // Send the guest back to the login screen
                Intent intent = new Intent(getActivity(), CustomerLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        }

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AppointmentHistoryAdapter();
        binding.rvAppointmentHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointmentHistory.setAdapter(adapter);
    }

    // This observer is no longer needed if you load from SharedPreferences
    // private void updateProfileUI(Customer customer) { ... }

    private void logout() {
        // Clear the user's session data
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.clear();
        editor.apply();

        // Navigate back to the MainActivity (which will now show as "Guest")
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}