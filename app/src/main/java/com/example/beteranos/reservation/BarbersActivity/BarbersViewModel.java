package com.example.beteranos.reservation.BarbersActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BarbersViewModel extends ViewModel {

    private final MutableLiveData<String> selectedBarbers = new MutableLiveData<>();

    public void selectBarbers(String barbers) {
        selectedBarbers.setValue(barbers);
    }

    public LiveData<String> getSelectedBarbers() {
        return selectedBarbers;
    }
}