package com.example.beteranos.ui_reservation.home.notifications; // <-- Correct package

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.R;
import com.example.beteranos.models.Notification;
import com.example.beteranos.ui_customer_login.CustomerLoginActivity;
import com.example.beteranos.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel notificationsViewModel;
    private NotificationAdapter adapter;
    private int customerId = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);

        if (customerId != -1) {
            // Logged-in user
            binding.guestView.setVisibility(View.GONE);
            setupRecyclerView();
            setupPullToRefresh();
            observeViewModel();
            notificationsViewModel.fetchNotifications(customerId);
        } else {
            // Guest user
            binding.swipeRefreshLayout.setVisibility(View.GONE);
            binding.guestView.setVisibility(View.VISIBLE);
            binding.loadingIndicator.setVisibility(View.GONE);

            binding.btnGoToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CustomerLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupPullToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            notificationsViewModel.fetchNotifications(customerId);
        });
    }

    private void observeViewModel() {
        notificationsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;

            if (isLoading && adapter.getCurrentList().isEmpty()) {
                binding.loadingIndicator.setVisibility(View.VISIBLE);
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.emptyState.setVisibility(View.GONE);
                binding.rvNotifications.setVisibility(View.GONE);
            } else {
                binding.loadingIndicator.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(isLoading);
            }
        });

        notificationsViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (binding == null) return;

            adapter.submitList(notifications);

            boolean isLoading = notificationsViewModel.getIsLoading().getValue() != null &&
                    notificationsViewModel.getIsLoading().getValue();

            if (!isLoading && (notifications == null || notifications.isEmpty())) {
                binding.rvNotifications.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvNotifications.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}