package com.example.agri_tech;

public class Animal {
    private String label;
    private String timestamp;
    private String image_base64;

    public Animal() {
        // Default constructor required for Firebase
    }

    public String getLabel() {
        return label;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getImage_base64() {
        return image_base64;
    }
}
