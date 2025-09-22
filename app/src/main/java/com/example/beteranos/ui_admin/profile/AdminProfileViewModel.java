package com.example.beteranos.ui_admin.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AdminProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Admin Profile fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}