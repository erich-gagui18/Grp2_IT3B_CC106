package com.example.beteranos.ui.reservation.ServicesActivity.HairwashActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HairwashViewModel extends ViewModel {

    private final MutableLiveData<String> selectedHairwash = new MutableLiveData<>();

    public void selectHairwash(String hairwash) {
        selectedHairwash.setValue(hairwash);
    }

    public LiveData<String> getSelectedHaircolor() {
        return selectedHairwash;
    }
}