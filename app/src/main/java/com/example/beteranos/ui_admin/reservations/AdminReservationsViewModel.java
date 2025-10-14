package com.example.beteranos.ui_admin.reservations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class   AdminReservationsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AdminReservationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Admin Reservations fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}