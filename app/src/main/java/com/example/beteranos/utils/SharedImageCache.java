package com.example.beteranos.utils; // Put in the same package as FullImageActivity

import java.util.HashMap;
import java.util.Map;

/**
 * Simple, temporary static cache for passing large byte arrays (BLOBs)
 * between Fragments/Dialogs and a dedicated Activity without hitting the
 * Android IPC Intent limit.
 */
public class SharedImageCache {

    // Map to store byte arrays, keyed by the Reservation ID
    private static final Map<Integer, byte[]> imageMap = new HashMap<>();

    /**
     * Stores the receipt data under a specific Reservation ID.
     */
    public static void putReceiptBytes(int reservationId, byte[] bytes) {
        imageMap.put(reservationId, bytes);
    }

    /**
     * Retrieves the receipt data for a specific Reservation ID.
     */
    public static byte[] getReceiptBytes(int reservationId) {
        // We retrieve and remove to ensure cleanup and prevent memory leaks
        return imageMap.remove(reservationId);
    }
}