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

import com.example.beteranos.MainActivity;
import com.example.beteranos.databinding.FragmentCustomerProfileBinding;
import com.example.beteranos.ui_customer_login.CustomerLoginActivity;

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

        // --- Load session info from SharedPreferences ---
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        int customerId = userPrefs.getInt("customer_id", -1);

        if (isLoggedIn && customerId != -1) {
            // ✅ Logged in view visible, guest view hidden
            binding.loggedInView.setVisibility(View.VISIBLE);
            binding.guestView.setVisibility(View.GONE);

            setupRecyclerView();

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
            // ✅ Guest mode active — show solid white background covering all
            binding.loggedInView.setVisibility(View.GONE);
            binding.guestView.setVisibility(View.VISIBLE);

            // Ensure the guest_view covers all and has a white background
            binding.guestView.setBackgroundColor(requireContext().getColor(android.R.color.white));
            binding.guestView.bringToFront();

            binding.btnGoToLogin.setOnClickListener(v -> {
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

    private void logout() {
        // Clear user session
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.clear();
        editor.apply();

        // Return to main screen as guest
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
