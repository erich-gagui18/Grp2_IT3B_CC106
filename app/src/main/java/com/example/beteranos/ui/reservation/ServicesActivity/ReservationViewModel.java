<<<<<<<< HEAD:app/src/main/java/com/example/beteranos/reservation/ServicesActivity/ReservationViewModel.java
package com.example.beteranos.reservation.ServicesActivity;
========
package com.example.beteranos.ui.reservation.ServicesActivity;
>>>>>>>> f32b940 (Merge pull request #7 from erich-gagui18/ck):app/src/main/java/com/example/beteranos/ui/reservation/ServicesActivity/ReservationViewModel.java

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
