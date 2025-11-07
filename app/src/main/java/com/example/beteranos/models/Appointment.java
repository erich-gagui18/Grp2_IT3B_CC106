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

    // --- 🔑 CORE CHANGE: Using byte[] for BLOB data instead of String URL ---
    private final byte[] paymentReceiptBytes;

    // --- 1. Full Constructor (Used when fetching from the database) ---
    public Appointment(int reservationId, String customerName, String serviceName,
                       String barberName, Timestamp reservationTime, String status,
                       byte[] paymentReceiptBytes) { // <-- Updated argument type
        this.reservationId = reservationId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.barberName = barberName;
        this.reservationTime = reservationTime;
        this.status = status;
        this.paymentReceiptBytes = paymentReceiptBytes; // Store the byte array
    }

    // --- 2. Parcel Constructor (Used by Android to read the object) ---
    protected Appointment(Parcel in) {
        reservationId = in.readInt();
        customerName = in.readString();
        serviceName = in.readString();
        barberName = in.readString();

        // Read the timestamp (long). Check for 0L if it was written as null/0.
        long timeMillis = in.readLong();
        reservationTime = timeMillis == 0L ? null : new Timestamp(timeMillis);

        status = in.readString();

        // --- 🔑 Parcelable Update for byte[] ---
        // Reads the size of the byte array, then the array itself.
        int bytesSize = in.readInt();
        if (bytesSize > 0) {
            paymentReceiptBytes = new byte[bytesSize];
            in.readByteArray(paymentReceiptBytes);
        } else {
            paymentReceiptBytes = null;
        }
    }

    // --- 3. writeToParcel (Used by Android to write the object) ---
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(reservationId);
        dest.writeString(customerName);
        dest.writeString(serviceName);
        dest.writeString(barberName);

        // Write Timestamp as long. Use 0L if it's null.
        dest.writeLong(reservationTime != null ? reservationTime.getTime() : 0L);

        dest.writeString(status);

        // --- 🔑 Parcelable Update for byte[] ---
        // Write the size first, then the array. 0 size indicates null.
        if (paymentReceiptBytes != null) {
            dest.writeInt(paymentReceiptBytes.length);
            dest.writeByteArray(paymentReceiptBytes);
        } else {
            dest.writeInt(0);
        }
    }

    // --- 4. CREATOR (Required static field for Parcelable) ---
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

    // --- 🔑 New Getter for byte[] ---
    public byte[] getPaymentReceiptBytes() { return paymentReceiptBytes; }

    // --- Utility Methods (for completeness) ---
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