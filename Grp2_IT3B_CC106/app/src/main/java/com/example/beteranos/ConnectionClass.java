package com.example.beteranos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.util.Log; // for Log.e on Android

public class ConnectionClass {
    String db = "beteranos_db";
    String ip = "10.0.2.2"; // use 10.0.2.2 for host machine from Android Emulator
    String username = "root";
    String password = "password";
    String port = "3306";

    @SuppressLint("NewApi")
    public Connection CONN() {
        Connection conn = null;

        // Declare once
        String ConnURL = "jdbc:mysql://" + ip + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true";

        try {
            Class.forName("com.mysql.jdbc.Driver"); // or "com.mysql.cj.jdbc.Driver" for newer drivers
            conn = DriverManager.getConnection(ConnURL, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

}
