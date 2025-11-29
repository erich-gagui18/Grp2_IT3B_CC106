package com.example.beteranos.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.sql.Timestamp;
import java.util.Objects;

public class Appointment implements Parcelable {

    private final int reservationId;
    private final String customerName;
    private final String serviceName;
    private final String barberName;
    private final Timestamp reservationTime;
    private final String status;
    private final byte[] paymentReceiptBytes;

    // --- ⭐️ NEW FIELDS FOR LOCATION ⭐️ ---
    private final String serviceLocation;
    private final String homeAddress;

    // --- 1. Full Constructor (Updated) ---
    public Appointment(int reservationId, String customerName, String serviceName,
                       String barberName, Timestamp reservationTime, String status,
                       byte[] paymentReceiptBytes,
                       String serviceLocation, String homeAddress) { // <-- Added arguments
        this.reservationId = reservationId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.barberName = barberName;
        this.reservationTime = reservationTime;
        this.status = status;
        this.paymentReceiptBytes = paymentReceiptBytes;

        // --- ⭐️ Initialize New Fields ⭐️ ---
        this.serviceLocation = serviceLocation;
        this.homeAddress = homeAddress;
    }

    // --- 2. Parcel Constructor (Updated) ---
    protected Appointment(Parcel in) {
        reservationId = in.readInt();
        customerName = in.readString();
        serviceName = in.readString();
        barberName = in.readString();

        long timeMillis = in.readLong();
        reservationTime = timeMillis == 0L ? null : new Timestamp(timeMillis);

        status = in.readString();

        // Read byte array
        int bytesSize = in.readInt();
        if (bytesSize > 0) {
            paymentReceiptBytes = new byte[bytesSize];
            in.readByteArray(paymentReceiptBytes);
        } else {
            paymentReceiptBytes = null;
        }

        // --- ⭐️ Read New Fields (Order matters!) ⭐️ ---
        serviceLocation = in.readString();
        homeAddress = in.readString();
    }

    // --- 3. writeToParcel (Updated) ---
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(reservationId);
        dest.writeString(customerName);
        dest.writeString(serviceName);
        dest.writeString(barberName);

        dest.writeLong(reservationTime != null ? reservationTime.getTime() : 0L);

        dest.writeString(status);

        // Write byte array
        if (paymentReceiptBytes != null) {
            dest.writeInt(paymentReceiptBytes.length);
            dest.writeByteArray(paymentReceiptBytes);
        } else {
            dest.writeInt(0);
        }

        // --- ⭐️ Write New Fields ⭐️ ---
        dest.writeString(serviceLocation);
        dest.writeString(homeAddress);
    }

    // --- 4. CREATOR (Unchanged) ---
    public static final Parcelable.Creator<Appointment> CREATOR = new Parcelable.Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // --- 5. Getters ---
    public int getReservationId() { return reservationId; }
    public String getCustomerName() { return customerName; }
    public String getServiceName() { return serviceName; }
    public String getBarberName() { return barberName; }
    public Timestamp getReservationTime() { return reservationTime; }
    public String getStatus() { return status; }
    public byte[] getPaymentReceiptBytes() { return paymentReceiptBytes; }

    // --- ⭐️ NEW GETTERS ⭐️ ---
    public String getServiceLocation() { return serviceLocation; }
    public String getHomeAddress() { return homeAddress; }

    // --- Utility Methods ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return reservationId == that.reservationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}