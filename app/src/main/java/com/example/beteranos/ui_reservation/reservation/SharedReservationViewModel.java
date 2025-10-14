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

    // --- THIS IS THE FIX ---
    // Variables and methods for handling services were missing.
    public final MutableLiveData<List<Service>> allServices = new MutableLiveData<>();
    public final MutableLiveData<List<Service>> selectedServices = new MutableLiveData<>(new ArrayList<>());

    // --- Barber Selection ---
    public final MutableLiveData<List<Barber>> allBarbers = new MutableLiveData<>();
    public final MutableLiveData<Barber> selectedBarber = new MutableLiveData<>();

    // --- Promo Selection ---
    public final MutableLiveData<List<Promo>> allPromos = new MutableLiveData<>();
    public final MutableLiveData<List<Promo>> selectedPromos = new MutableLiveData<>(new ArrayList<>());

    public SharedReservationViewModel() {
        fetchServicesFromDB();
        fetchBarbersFromDB();
        fetchPromosFromDB(); // Call the new method
    }

    public void setCustomerDetails(String fName, String lName, String pNum) {
        firstName.setValue(fName);
        lastName.setValue(lName);
        phone.setValue(pNum);
    }

    private void fetchServicesFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            List<Service> fetchedServices = new ArrayList<>();
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                conn = connectionClass.CONN();
                if (conn != null) {
                    String query = "SELECT service_id, service_name, price FROM services";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt("service_id");
                        String name = rs.getString("service_name");
                        double price = rs.getDouble("price");
                        fetchedServices.add(new Service(id, name, price));
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
                ConnectionClass connectionClass = new ConnectionClass();
                conn = connectionClass.CONN();
                if (conn != null) {
                    String query = "SELECT barber_id, name FROM barbers";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt("barber_id");
                        String name = rs.getString("name");
                        fetchedBarbers.add(new Barber(id, name));
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
                ConnectionClass connectionClass = new ConnectionClass();
                conn = connectionClass.CONN();
                if (conn != null) {
                    String query = "SELECT promo_id, promo_name, description FROM promos WHERE is_active = 1";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt("promo_id");
                        String name = rs.getString("promo_name");
                        String description = rs.getString("description");
                        fetchedPromos.add(new Promo(id, name, description));
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

    // Methods to manage promo checklist selection
    public void addPromo(Promo promo) {
        List<Promo> currentList = selectedPromos.getValue();
        if (currentList != null && !currentList.contains(promo)) {
            currentList.add(promo);
            selectedPromos.setValue(currentList);
        }
    }

    public void removePromo(Promo promo) {
        List<Promo> currentList = selectedPromos.getValue();
        if (currentList != null) {
            currentList.remove(promo);
            selectedPromos.setValue(currentList);
        }
    }

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