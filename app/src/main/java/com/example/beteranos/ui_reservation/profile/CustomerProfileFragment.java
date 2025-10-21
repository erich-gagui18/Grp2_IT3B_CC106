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
import com.example.beteranos.CustomerLoginActivity;
import com.example.beteranos.MainActivity;
import com.example.beteranos.databinding.FragmentCustomerProfileBinding;
import com.example.beteranos.models.Customer;
import android.content.SharedPreferences;
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

        setupRecyclerView();

        // --- THIS IS THE FIX ---
        // Get the logged-in customer's ID (passed from LoginActivity)
        int customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);

        if (customerId != -1) {
            // Tell the ViewModel to load data from the database
            mViewModel.loadCustomerData(customerId);
            mViewModel.loadAppointmentHistory(customerId);
        }

        // Observe data changes from the ViewModel
        mViewModel.getCustomerData().observe(getViewLifecycleOwner(), this::updateProfileUI);
        mViewModel.getAppointmentHistory().observe(getViewLifecycleOwner(), appointments -> {
            adapter.setAppointments(appointments);
        });

        binding.btnLogout.setOnClickListener(v -> logout());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AppointmentHistoryAdapter();
        binding.rvAppointmentHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointmentHistory.setAdapter(adapter);
    }

    private void updateProfileUI(Customer customer) {
        if (customer != null) {
            String fullName = customer.getFirstName() + " " + customer.getLastName();
            binding.tvCustomerName.setText(fullName);
            binding.tvCustomerEmail.setText(customer.getEmail());
        }
    }

    private void logout() {
        // Clear the user's session data
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.clear();
        editor.apply();

        // Navigate back to the CustomerLoginActivity
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