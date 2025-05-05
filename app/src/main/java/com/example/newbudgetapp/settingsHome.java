package com.example.newbudgetapp;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class settingsHome extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_settings_home);

        ImageButton toDashboard = findViewById(R.id.homeBtn);
        ImageButton toSettings = findViewById(R.id.billRemindersBtn);
        ImageButton toSettings2 = findViewById(R.id.scheduleUpdatesBtn);
        Button resetGraph = findViewById(R.id.resetGraphBtn);
        Button logOut = findViewById(R.id.signOutBtn);

        toDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settingsHome.this, DashboardActivity.class));
            }
        });

        toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settingsHome.this, settings.class));
            }
        });

        toSettings2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settingsHome.this, settings2.class));
            }
        });

        resetGraph.setOnClickListener(v -> {
            new AlertDialog.Builder(settingsHome.this)
                    .setTitle("Reset Graph Data")
                    .setMessage("Are you sure you want to clear all income and expense data?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        String userID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

                        if (userID != null) {
                            FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
                            DocumentReference userDoc = budgetData.collection("Users").document(userID);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("incomeEntries", new ArrayList<>());
                            updates.put("expenseEntries", new ArrayList<>());

                            userDoc.update(updates)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(settingsHome.this, "Graph data reset successfully.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(settingsHome.this, "Failed to reset graph data.", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });



        logOut.setOnClickListener(v -> {
            new AlertDialog.Builder(settingsHome.this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(settingsHome.this, "Signed out successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(settingsHome.this, login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


    }
}
