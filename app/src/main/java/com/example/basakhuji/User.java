package com.example.basakhuji;

public class User {
    private String fullname;
    private String email;
    private String pnumber;

    private String username;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String fullname, String email, String pnumber, String username) {
        this.fullname = fullname;
        this.email = email;
        this.pnumber = pnumber;
        this.username = username;
    }

    public String getFullName() {
        return fullname;
    }

    public void setFullName(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return pnumber;
    }

    public void setPhoneNumber(String pnumber) {
        this.pnumber = pnumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

