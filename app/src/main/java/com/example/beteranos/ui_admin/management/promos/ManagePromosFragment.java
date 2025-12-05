package com.example.beteranos.ui_admin.management.promos;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.beteranos.R;
import com.example.beteranos.databinding.DialogAddPromoBinding;
import com.example.beteranos.databinding.FragmentManagePromosBinding;
import com.example.beteranos.models.Promo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ManagePromosFragment extends Fragment implements PromosManagementAdapter.OnPromoActionListener {

    private static final String TAG = "ManagePromosFragment";
    private FragmentManagePromosBinding binding;
    private AdminManagementPromosViewModel viewModel;
    private PromosManagementAdapter adapter;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView dialogPromoImageView;
    private byte[] tempImageBytes = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupImagePickerLauncher();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagePromosBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AdminManagementPromosViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeViewModel();

        binding.fabAddPromo.setOnClickListener(v -> {
            tempImageBytes = null;
            showAddOrEditDialog(null);
        });

        // Initial Fetch
        viewModel.fetchPromosFromDB();
    }

    private void setupRecyclerView() {
        adapter = new PromosManagementAdapter(this);
        binding.promosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.promosRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.allPromos.observe(getViewLifecycleOwner(), promos -> {
            adapter.submitList(promos);
            boolean isEmpty = (promos == null || promos.isEmpty());
            binding.emptyListText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.promosRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.fabAddPromo.setEnabled(!isLoading);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearToastMessage();
            }
        });
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                Bitmap bitmap = uriToBitmap(imageUri);
                                if (bitmap != null) {
                                    tempImageBytes = getBytesFromBitmap(bitmap, 80);
                                    if (dialogPromoImageView != null) {
                                        dialogPromoImageView.setImageBitmap(bitmap);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to process image", e);
                                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void showAddOrEditDialog(@Nullable Promo existingPromo) {
        DialogAddPromoBinding dialogBinding = DialogAddPromoBinding.inflate(LayoutInflater.from(getContext()));
        dialogPromoImageView = dialogBinding.promoImageView;

        if (existingPromo != null) {
            dialogBinding.nameEditText.setText(existingPromo.getPromoName());
            dialogBinding.descEditText.setText(existingPromo.getDescription());
            dialogBinding.discountEditText.setText(String.valueOf(existingPromo.getDiscountPercentage()));

            tempImageBytes = existingPromo.getImage();
            if (tempImageBytes != null) {
                Glide.with(this)
                        .load(tempImageBytes)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_broken)
                        .into(dialogPromoImageView);
            }
        } else {
            tempImageBytes = null;
        }

        dialogBinding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        String title = (existingPromo == null) ? "Add New Promo" : "Edit Promo";
        String positiveButton = (existingPromo == null) ? "Add" : "Save";

        builder.setTitle(title);
        builder.setView(dialogBinding.getRoot());
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton(positiveButton, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameEditText.getText().toString().trim();
                String desc = dialogBinding.descEditText.getText().toString().trim();
                String discountStr = dialogBinding.discountEditText.getText().toString().trim();

                boolean isValid = true;

                if (TextUtils.isEmpty(name)) {
                    dialogBinding.nameLayout.setError("Name is required");
                    isValid = false;
                } else {
                    dialogBinding.nameLayout.setError(null);
                }

                if (TextUtils.isEmpty(discountStr)) {
                    dialogBinding.discountLayout.setError("Discount is required");
                    isValid = false;
                } else {
                    dialogBinding.discountLayout.setError(null);
                }

                if (tempImageBytes == null) {
                    Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                    isValid = false;
                }

                if (!isValid) return;

                int discount = 0;
                try {
                    discount = Integer.parseInt(discountStr);
                } catch (NumberFormatException e) {
                    dialogBinding.discountLayout.setError("Invalid number");
                    return;
                }

                // ⭐️ Update Logic: Pass is_active as true for new, existing state for update ⭐️
                if (existingPromo != null) {
                    viewModel.updatePromo(existingPromo.getPromoId(), name, desc, discount, tempImageBytes, existingPromo.isActive());
                } else {
                    viewModel.addPromo(name, desc, discount, tempImageBytes);
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // --- Action Listeners ---
    @Override
    public void onEditClick(Promo promo) {
        showAddOrEditDialog(promo);
    }

    @Override
    public void onDeleteClick(Promo promo) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Promo")
                .setMessage("Are you sure you want to delete '" + promo.getPromoName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deletePromo(promo);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ⭐️ NEW: Toggle Visibility Action ⭐️
    @Override
    public void onToggleVisibilityClick(Promo promo) {
        String status = promo.isActive() ? "Hide" : "Show";
        new AlertDialog.Builder(requireContext())
                .setTitle(status + " Promo")
                .setMessage("Do you want to " + status.toLowerCase() + " this promo from customers?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.togglePromoVisibility(promo);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- Helper Methods ---
    private Bitmap uriToBitmap(Uri imageUri) {
        if (imageUri == null || getContext() == null) return null;
        ContentResolver resolver = getContext().getContentResolver();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, imageUri));
            } else {
                //noinspection deprecation
                return MediaStore.Images.Media.getBitmap(resolver, imageUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load bitmap from URI", e);
            return null;
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogPromoImageView = null;
        tempImageBytes = null;
    }
}