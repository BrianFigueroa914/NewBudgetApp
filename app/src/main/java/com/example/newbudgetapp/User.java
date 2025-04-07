package com.example.newbudgetapp;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

public class User {
    public String username;
    public String email;

    public List<Entry> incomeEntries;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email,List<Entry> incomeEntries) {
        this.username = username;
        this.email = email;
        this.incomeEntries = incomeEntries;
    }

    public User(List<Entry> incomeEntries) {
        this.incomeEntries = incomeEntries;
    }
}