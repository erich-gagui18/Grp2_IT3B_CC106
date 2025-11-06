package com.example.beteranos.ui_reservation.home.data_barber;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
// Import your Data Model and Adapter:
import com.example.beteranos.ui_reservation.home.data_barber.DataModel_Barber;
import com.example.beteranos.ui_reservation.home.data_barber.BarberProfileAdapter;

import java.util.ArrayList;
import java.util.List;

public class BarberProfileFragment extends Fragment {

    // You can remove or comment out the mViewModel if it's not used:
    // private BarberProfileViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout containing the RecyclerView (fragment_barber_profile.xml)
        return inflater.inflate(R.layout.fragment_barber_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Prepare your data list (Inputting names, descriptions, images, and RATING)
        List<DataModel_Barber> barbers = new ArrayList<>();

        // Add Barber 1: Name (Buzz Cut)
        barbers.add(new DataModel_Barber(
                "Name",
                "A buzz cut is a very short haircut done with clippers, usually the same length all around or with slight variations. It’s simple, low-maintenance, and clean-looking.",
                R.drawable.buzzcut, // ⚠️ Ensure this drawable ID exists
                4.5f // Rating
        ));

        // Add Barber 2: Sidney (Low Fade)
        barbers.add(new DataModel_Barber(
                "Sidney",
                "A low fade is recommended for people who want a clean, modern look but still prefer a haircut that’s not too drastic on the sides.",
                R.drawable.haircut1, // ⚠️ Ensure this drawable ID exists
                4.8f // Rating
        ));

        // Add Barber 3: Jose (Mid Taper Fade)
        barbers.add(new DataModel_Barber(
                "Jose",
                "A mid taper fade is recommended if you want a haircut that’s sharper than a low fade but not as bold as a high fade.",
                R.drawable.haircut3, // ⚠️ Ensure this drawable ID exists
                4.2f // Rating
        ));

        // Add Barber 4: Miguel (Mullet)
        barbers.add(new DataModel_Barber(
                "Miguel",
                "A mullet is recommended if you want a hairstyle that’s bold, trendy, and attention-grabbing. It’s not for everyone, but it suits people who like to stand out and show personality.",
                R.drawable.haircut4, // ⚠️ Ensure this drawable ID exists
                5.0f // Rating
        ));


        // 2. Setup the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.barbers_recycler_view);

        // Set the LayoutManager (vertical scrolling list)
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Set the Adapter
        BarberProfileAdapter adapter = new BarberProfileAdapter(barbers);
        recyclerView.setAdapter(adapter);
    }
}