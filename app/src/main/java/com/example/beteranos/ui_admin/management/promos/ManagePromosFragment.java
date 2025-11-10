package com.example.beteranos.ui_admin.management.promos;

import android.app.Activity;
import android.content.ContentResolver; // ⭐️ ADDED IMPORT
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap; // ⭐️ ADDED IMPORT
import android.graphics.ImageDecoder; // ⭐️ ADDED IMPORT
import android.net.Uri;
import android.os.Build; // ⭐️ ADDED IMPORT
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
import java.io.IOException; // ⭐️ ADDED IMPORT
import java.io.InputStream; // This is no longer used by the new method but is fine to keep


public class ManagePromosFragment extends Fragment implements PromosManagementAdapter.OnPromoActionListener {

    private FragmentManagePromosBinding binding;
    private AdminManagementPromosViewModel viewModel;
    private PromosManagementAdapter adapter;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private byte[] selectedImageBytes = null;
    private DialogAddPromoBinding dialogBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⭐️ UPDATED LAUNCHER CALLBACK
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null && dialogBinding != null) {

                            // 1. Convert the selected Uri to a Bitmap
                            Bitmap bitmap = uriToBitmap(imageUri);

                            if (bitmap != null) {
                                // 2. Display the preview in the dialog
                                Glide.with(this)
                                        .load(bitmap) // Load the bitmap for preview
                                        .centerCrop()
                                        .into(dialogBinding.promoImagePreview);

                                // 3. Convert the Bitmap to a compressed byte[] for saving
                                // Using 80% quality JPEG is a good balance for size vs. quality
                                selectedImageBytes = getBytesFromBitmap(bitmap, 80);
                            } else {
                                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagePromosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminManagementPromosViewModel.class);
        setupRecyclerView();
        binding.fabAddPromo.setOnClickListener(v -> {
            showAddOrEditDialog(null);
        });
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new PromosManagementAdapter(this);
        binding.promosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.promosRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.allPromos.observe(getViewLifecycleOwner(), promos -> {
            adapter.submitList(promos);
            if (promos == null || promos.isEmpty()) {
                binding.emptyListText.setVisibility(View.VISIBLE);
                binding.promosRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyListText.setVisibility(View.GONE);
                binding.promosRecyclerView.setVisibility(View.VISIBLE);
            }
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

    private void showAddOrEditDialog(@Nullable Promo existingPromo) {
        dialogBinding = DialogAddPromoBinding.inflate(LayoutInflater.from(getContext()));
        selectedImageBytes = null; // Reset on each dialog open

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogBinding.getRoot());

        if (existingPromo != null) {
            builder.setTitle("Edit Promo");
            dialogBinding.nameEditText.setText(existingPromo.getPromoName());
            dialogBinding.descEditText.setText(existingPromo.getDescription());
            dialogBinding.discountEditText.setText(String.valueOf(existingPromo.getDiscountPercentage()));

            // This logic is correct: It loads the existing bytes from the promo object
            selectedImageBytes = existingPromo.getImage();
            if (selectedImageBytes != null) {
                Glide.with(this)
                        .load(selectedImageBytes) // Load existing bytes
                        .error(R.drawable.ic_image_broken)
                        .into(dialogBinding.promoImagePreview);
            }

            builder.setPositiveButton("Save", null);
        } else {
            builder.setTitle("Add New Promo");
            builder.setPositiveButton("Add", null);
        }

        dialogBinding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent); // This now triggers the new callback logic
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameEditText.getText().toString().trim();
                String description = dialogBinding.descEditText.getText().toString().trim();
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

                // This check is now robust.
                // It checks 'selectedImageBytes' which is set EITHER from the existing promo
                // OR from the new image picker result.
                if (selectedImageBytes == null) {
                    Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                    isValid = false;
                }

                if (!isValid) return;

                int discount = 0;
                try {
                    discount = Integer.parseInt(discountStr);
                    if (discount < 0 || discount > 99) {
                        dialogBinding.discountLayout.setError("Must be between 0-99");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    dialogBinding.discountLayout.setError("Invalid number");
                    isValid = false;
                }

                if (isValid) {
                    if (existingPromo != null) {
                        viewModel.updatePromo(existingPromo.getPromoId(), name, description, discount, selectedImageBytes);
                    } else {
                        viewModel.addPromo(name, description, discount, selectedImageBytes);
                    }
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // ⭐️ DELETED old convertUriToBytes method ⭐️

    // --------------------------------------------------------------------
    // ⭐️ NEW HELPER METHODS (Replaced old convertUriToBytes) ⭐️
    // --------------------------------------------------------------------

    /**
     * Converts an image Uri (from the gallery/picker) into a Bitmap object.
     * This handles different Android API levels.
     *
     * @param imageUri The Uri of the selected image.
     * @return A Bitmap object, or null if conversion fails.
     */
    private Bitmap uriToBitmap(Uri imageUri) {
        if (imageUri == null || getContext() == null) {
            return null;
        }

        ContentResolver contentResolver = getContext().getContentResolver();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Use new API for Android 9 (Pie) and above
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, imageUri);
                return ImageDecoder.decodeBitmap(source);
            } else {
                // Use deprecated API for older versions
                //noinspection deprecation
                return MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
            }
        } catch (IOException e) {
            Log.e("ManagePromosFragment", "Failed to load bitmap from URI", e);
            Toast.makeText(getContext(), "Failed to read image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Converts a Bitmap object into a compressed byte array (byte[]).
     *
     * @param bitmap The Bitmap to convert.
     * @param quality The compression quality (0-100). 80 is a good default.
     * @return The compressed image data as a byte array.
     */
    private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Use JPEG for good compression. Use PNG if you need transparency (larger files).
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        return outputStream.toByteArray();
    }

    // --------------------------------------------------------------------
    // END OF NEW HELPER METHODS
    // --------------------------------------------------------------------

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogBinding = null; // Good practice to clear dialog binding too
    }
}