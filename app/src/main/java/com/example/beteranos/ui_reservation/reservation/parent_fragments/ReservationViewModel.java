package com.example.beteranos.ui_reservation.reservation.parent_fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReservationViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ReservationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is reservation fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
