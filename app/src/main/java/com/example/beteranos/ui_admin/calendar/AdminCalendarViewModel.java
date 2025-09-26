package com.example.beteranos.ui_admin.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminCalendarViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AdminCalendarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Admin Calendar fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}