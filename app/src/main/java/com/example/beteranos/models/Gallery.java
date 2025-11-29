package com.example.beteranos.models;

import java.util.Arrays;
import java.util.Objects;

public class Gallery {
    private final int id;
    private final byte[] imageData;

    public Gallery(int id, byte[] imageData) {
        this.id = id;
        this.imageData = imageData;
    }

    public int getId() { return id; }
    public byte[] getImageData() { return imageData; }

    // Required for DiffUtil in Adapter
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gallery that = (Gallery) o;
        return id == that.id && Arrays.equals(imageData, that.imageData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id);
        result = 31 * result + Arrays.hashCode(imageData);
        return result;
    }
}