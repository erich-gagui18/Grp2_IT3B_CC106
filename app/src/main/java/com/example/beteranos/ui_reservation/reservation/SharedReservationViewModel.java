package com.example.beteranos.ui_reservation.reservation;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Service;
import com.example.beteranos.models.Barber;
import com.example.beteranos.models.Promo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedReservationViewModel extends ViewModel {

    // Customer Details
    public final MutableLiveData<String> firstName = new MutableLiveData<>();
    public final MutableLiveData<String> lastName = new MutableLiveData<>();
    public final MutableLiveData<String> phone = new MutableLiveData<>();

    // Service Selection (Multi-select)
    public final MutableLiveData<List<Service>> allServices = new MutableLiveData<>();
    public final MutableLiveData<List<Service>> selectedServices = new MutableLiveData<>(new ArrayList<>());

    // Barber Selection (Single-select)
    public final MutableLiveData<List<Barber>> allBarbers = new MutableLiveData<>();
    public final MutableLiveData<Barber> selectedBarber = new MutableLiveData<>();

    // Promo Selection (Single-select)
    public final MutableLiveData<List<Promo>> allPromos = new MutableLiveData<>();
    public final MutableLiveData<Promo> selectedPromo = new MutableLiveData<>();

    // Schedule Selection
    public final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    public final MutableLiveData<String> selectedTime = new MutableLiveData<>();

    public SharedReservationViewModel() {
        fetchServicesFromDB();
        fetchBarbersFromDB();
        fetchPromosFromDB();
    }

    public void setCustomerDetails(String fName, String lName, String pNum) {
        firstName.setValue(fName);
        lastName.setValue(lName);
        phone.setValue(pNum);
    }

    // --- Database Fetching Methods ---

    private void fetchServicesFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            List<Service> fetchedServices = new ArrayList<>();
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT service_id, service_name, price FROM services";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        fetchedServices.add(new Service(rs.getInt("service_id"), rs.getString("service_name"), rs.getDouble("price")));
                    }
                    allServices.postValue(fetchedServices);
                }
            } catch (SQLException e) {
                Log.e("SharedViewModel", "DB Error (Services): " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void fetchBarbersFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            List<Barber> fetchedBarbers = new ArrayList<>();
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT barber_id, name FROM barbers";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        fetchedBarbers.add(new Barber(rs.getInt("barber_id"), rs.getString("name")));
                    }
                    allBarbers.postValue(fetchedBarbers);
                }
            } catch (SQLException e) {
                Log.e("SharedViewModel", "DB Error (Barbers): " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void fetchPromosFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            List<Promo> fetchedPromos = new ArrayList<>();
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT promo_id, promo_name, description, image_name FROM promos WHERE is_active = 1";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        fetchedPromos.add(new Promo(rs.getInt("promo_id"), rs.getString("promo_name"), rs.getString("description"), rs.getString("image_name")));
                    }
                    allPromos.postValue(fetchedPromos);
                }
            } catch (SQLException e) {
                Log.e("SharedViewModel", "DB Error (Promos): " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    // --- Selection Management Methods ---

    public void addService(Service service) {
        List<Service> currentList = selectedServices.getValue();
        if (currentList != null && !currentList.contains(service)) {
            currentList.add(service);
            selectedServices.setValue(currentList);
        }
    }

    public void removeService(Service service) {
        List<Service> currentList = selectedServices.getValue();
        if (currentList != null) {
            currentList.remove(service);
            selectedServices.setValue(currentList);
        }
    }
}