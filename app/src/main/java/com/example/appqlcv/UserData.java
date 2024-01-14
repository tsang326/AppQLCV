package com.example.appqlcv;

public class UserData {
    private String fullName;
    private String email;
    private String imageAvt;
    private String key;

    public UserData() {
        // Default constructor required for calls to DataSnapshot.getValue(UserData.class)
    }

    public UserData(String fullName, String email, String imageAvt, String key) {
        this.fullName = fullName;
        this.email = email;
        this.imageAvt = imageAvt;
        this.key = key;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageAvt() {
        return imageAvt;
    }

    public void setImageAvt(String imageAvt) {
        this.imageAvt = imageAvt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
