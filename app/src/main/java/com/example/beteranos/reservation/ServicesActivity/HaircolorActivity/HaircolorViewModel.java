package com.example.beteranos.reservation.ServicesActivity.HaircolorActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HaircolorViewModel extends ViewModel {

    private final MutableLiveData<String> selectedHaircolor = new MutableLiveData<>();

    public void selectHaircolor(String haircolor) {
        selectedHaircolor.setValue(haircolor);
    }

    public LiveData<String> getSelectedHaircolor() {
        return selectedHaircolor;
    }
}