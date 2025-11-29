package com.example.beteranos.ui_admin.management.products;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.databinding.DialogAddProductBinding;
import com.example.beteranos.databinding.FragmentManageProductsBinding;
import com.example.beteranos.models.Product;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageProductsFragment extends Fragment implements ProductsManagementAdapter.OnProductActionListener {

    private FragmentManageProductsBinding binding;
    private ManageProductsViewModel viewModel;
    private ProductsManagementAdapter adapter;

    // Dialog tracking
    private AlertDialog currentDialog;
    private DialogAddProductBinding currentDialogBinding;
    private byte[] selectedImageBytes;

    // Activity result launcher for picking image
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManageProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ManageProductsViewModel.class);
        setupRecyclerView();
        observeViewModel();

        // Image picker
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                (ActivityResultCallback<Uri>) uri -> {
                    if (uri == null || currentDialogBinding == null) return;
                    try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        byte[] buf = new byte[4096];
                        int r;
                        while ((r = in.read(buf)) != -1) baos.write(buf, 0, r);
                        selectedImageBytes = baos.toByteArray();
                        Bitmap bmp = BitmapFactory.decodeByteArray(selectedImageBytes, 0, selectedImageBytes.length);
                        currentDialogBinding.imagePreview.setImageBitmap(bmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show();
                    }
                });

        // FAB
        binding.fabAddProduct.setOnClickListener(v -> {
            if (currentDialog == null || !currentDialog.isShowing()) showAddOrEditDialog(null);
        });

        // initial fetch
        viewModel.fetchProducts();
    }

    private void setupRecyclerView() {
        adapter = new ProductsManagementAdapter(this);
        // ⭐️ ID Match: productsRecyclerView matches products_recycler_view in XML
        binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.productsRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.productList.observe(getViewLifecycleOwner(), products -> {
            List<Product> safe = products == null ? new ArrayList<>() : new ArrayList<>(products);
            adapter.submitList(safe);
            boolean empty = safe.isEmpty();

            // ⭐️ ID Match: emptyListText matches empty_list_text in XML
            binding.emptyListText.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.productsRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            // ⭐️ ID Match: progressBar matches progress_bar in XML
            binding.progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
            binding.fabAddProduct.setEnabled(!(loading != null && loading));
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearToastMessage();
            }
        });
    }

    private void showAddOrEditDialog(@Nullable Product existing) {
        currentDialogBinding = DialogAddProductBinding.inflate(LayoutInflater.from(requireContext()));
        // ... (rest of validation logic remains the same) ...
        currentDialogBinding.nameLayout.setError(null);
        currentDialogBinding.priceLayout.setError(null);
        currentDialogBinding.stockLayout.setError(null);
        currentDialogBinding.descriptionLayout.setError(null);
        selectedImageBytes = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(currentDialogBinding.getRoot());

        if (existing != null) {
            builder.setTitle("Edit Product");
            currentDialogBinding.nameEditText.setText(existing.getName());
            currentDialogBinding.descriptionEditText.setText(existing.getDescription());
            currentDialogBinding.priceEditText.setText(String.format(Locale.US, "%.2f", existing.getPrice()));
            currentDialogBinding.stockEditText.setText(String.valueOf(existing.getStock()));
            if (existing.getImageBytes() != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                currentDialogBinding.imagePreview.setImageBitmap(BitmapFactory.decodeByteArray(existing.getImageBytes(), 0, existing.getImageBytes().length, opts));
                selectedImageBytes = existing.getImageBytes();
            }
            builder.setPositiveButton("Save", null);
        } else {
            builder.setTitle("Add New Product");
            builder.setPositiveButton("Add", null);
        }
        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());

        currentDialog = builder.create();

        // Image pick button
        currentDialogBinding.pickImageBtn.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        currentDialog.setOnShowListener(dialogInterface -> {
            currentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                // validate
                String name = currentDialogBinding.nameEditText.getText().toString().trim();
                String desc = currentDialogBinding.descriptionEditText.getText().toString().trim();
                String priceStr = currentDialogBinding.priceEditText.getText().toString().trim();
                String stockStr = currentDialogBinding.stockEditText.getText().toString().trim();

                boolean isValid = true;

                if (TextUtils.isEmpty(name)) {
                    currentDialogBinding.nameLayout.setError("Name is required");
                    isValid = false;
                } else currentDialogBinding.nameLayout.setError(null);

                if (TextUtils.isEmpty(priceStr)) {
                    currentDialogBinding.priceLayout.setError("Price is required");
                    isValid = false;
                } else currentDialogBinding.priceLayout.setError(null);

                if (TextUtils.isEmpty(stockStr)) {
                    currentDialogBinding.stockLayout.setError("Stock is required");
                    isValid = false;
                } else currentDialogBinding.stockLayout.setError(null);

                if (TextUtils.isEmpty(desc)) {
                    currentDialogBinding.descriptionLayout.setError("Description is required");
                    isValid = false;
                } else currentDialogBinding.descriptionLayout.setError(null);

                BigDecimal price = BigDecimal.ZERO;
                int stock = 0;
                if (isValid) {
                    try {
                        price = new BigDecimal(priceStr);
                        if (price.compareTo(BigDecimal.ZERO) <= 0) {
                            currentDialogBinding.priceLayout.setError("Price must be positive");
                            isValid = false;
                        }
                    } catch (NumberFormatException ex) {
                        currentDialogBinding.priceLayout.setError("Invalid price");
                        isValid = false;
                    }
                    try {
                        stock = Integer.parseInt(stockStr);
                        if (stock < 0) {
                            currentDialogBinding.stockLayout.setError("Stock cannot be negative");
                            isValid = false;
                        }
                    } catch (NumberFormatException ex) {
                        currentDialogBinding.stockLayout.setError("Invalid stock");
                        isValid = false;
                    }
                }

                if (!isValid) return;

                if (existing != null) {
                    viewModel.updateProduct(existing.getId(), name, desc, price.doubleValue(), stock, selectedImageBytes);
                } else {
                    viewModel.addProduct(name, desc, price.doubleValue(), stock, selectedImageBytes);
                }
                currentDialog.dismiss();
                currentDialog = null;
                currentDialogBinding = null;
                selectedImageBytes = null;
            });
        });

        currentDialog.setOnDismissListener(d -> {
            currentDialogBinding = null;
            currentDialog = null;
            selectedImageBytes = null;
        });

        currentDialog.show();
    }

    @Override
    public void onEditClick(Product product) {
        if (currentDialog == null || !currentDialog.isShowing()) showAddOrEditDialog(product);
    }

    @Override
    public void onDeleteClick(Product product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Delete '" + product.getName() + "'?")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.productsRecyclerView.setAdapter(null);
        }
        if (currentDialog != null && currentDialog.isShowing()) currentDialog.dismiss();
        binding = null;
    }
}