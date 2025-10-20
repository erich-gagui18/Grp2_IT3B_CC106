package com.example.beteranos.ui_admin.management;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminManagementViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AdminManagementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Admin Reservations fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}