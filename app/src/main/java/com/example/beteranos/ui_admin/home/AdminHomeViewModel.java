package com.example.beteranos.ui_admin.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminHomeViewModel extends ViewModel {

    private final MutableLiveData<String> welcomeMessage;
    private final MutableLiveData<String> reservationsLabel;
    private final MutableLiveData<List<String>> selectedDateReservations;
    private final Map<String, List<String>> allReservations;

    public AdminHomeViewModel() {
        welcomeMessage = new MutableLiveData<>();
        welcomeMessage.setValue("Welcome Back!");

        reservationsLabel = new MutableLiveData<>();
        selectedDateReservations = new MutableLiveData<>();
        allReservations = new HashMap<>();

        loadSampleReservations();
        // Initially load today's reservations
        loadReservationsForDate(Calendar.getInstance().getTimeInMillis());
    }

    public void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            welcomeMessage.setValue("Welcome Back " + username);
        }
    }

    public void loadReservationsForDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateKey = sdf.format(dateInMillis);

        reservationsLabel.setValue("Reservations for " + selectedDateKey);

        if (allReservations.containsKey(selectedDateKey)) {
            selectedDateReservations.setValue(allReservations.get(selectedDateKey));
        } else {
            selectedDateReservations.setValue(new ArrayList<>());
        }
    }

    public LiveData<String> getWelcomeMessage() {
        return welcomeMessage;
    }

    public LiveData<String> getReservationsLabel() {
        return reservationsLabel;
    }

    public LiveData<List<String>> getSelectedDateReservations() {
        return selectedDateReservations;
    }

    private void loadSampleReservations() {
        // In a real app, this data would come from your database
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = sdf.format(cal.getTime());

        List<String> todayReservations = new ArrayList<>();
        todayReservations.add("10:00 AM - Haircut with Nico");
        todayReservations.add("11:30 AM - Shaving with Jonel");
        todayReservations.add("2:00 PM - Haircoloring with Daryl");
        allReservations.put(today, todayReservations);

        List<String> tomorrowReservations = new ArrayList<>();
        tomorrowReservations.add("9:00 AM - Haircut with Jimboy");
        tomorrowReservations.add("1:00 PM - Shaving with John");
        allReservations.put(tomorrow, tomorrowReservations);
    }
}