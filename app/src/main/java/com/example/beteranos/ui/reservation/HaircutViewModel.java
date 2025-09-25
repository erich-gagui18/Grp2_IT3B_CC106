package com.example.beteranos.ui.reservation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HaircutViewModel extends ViewModel {

    private final MutableLiveData<String> selectedHaircut = new MutableLiveData<>();

    public void selectHaircut(String haircut) {
        selectedHaircut.setValue(haircut);
    }

    public LiveData<String> getSelectedHaircut() {
        return selectedHaircut;
    }
}
