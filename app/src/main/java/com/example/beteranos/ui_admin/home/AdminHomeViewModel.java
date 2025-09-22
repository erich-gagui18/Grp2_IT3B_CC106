package com.example.beteranos.ui_admin.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminHomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AdminHomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Admin Home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}