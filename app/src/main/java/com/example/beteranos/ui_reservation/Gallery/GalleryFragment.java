package com.example.beteranos.ui.Gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.GalleryAdapter; // The adapter created previously
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentGalleryBinding; // Assuming you set up View Binding for this fragment

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Ensure you have a layout file named fragment_gallery.xml
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 1. Get the RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.gallery_recycler_view);

        // 2. Set the layout manager (e.g., a grid of 2 columns)
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 3. Prepare the image data (REPLACE THESE with your actual image resources)
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.haircut1);

        images.add(R.drawable.haircut2);
        images.add(R.drawable.haircut3);
        images.add(R.drawable.haircut4);
        images.add(R.drawable.haircut5);
        images.add(R.drawable.haircut6);

        // Add more images as needed...

        // 4. Create and set the Adapter
        GalleryAdapter adapter = new GalleryAdapter(images);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}