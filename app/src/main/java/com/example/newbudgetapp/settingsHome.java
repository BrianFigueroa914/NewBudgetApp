package com.example.newbudgetapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class settingsHome extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_settings_home);

        // Initialize buttons
        ImageButton toDashboard = findViewById(R.id.homeBtn);
        ImageButton toSettings = findViewById(R.id.billRemindersBtn);
        ImageButton toSettings2 = findViewById(R.id.scheduleUpdatesBtn);
        Button resetGraph = findViewById(R.id.resetGraphBtn);
        Button resetBudgetsBtn = findViewById(R.id.resetBudgetsBtn);
        Button resetSavingsBtn = findViewById(R.id.resetSavingsBtn);
        Button logOut = findViewById(R.id.signOutBtn);

        // Navigation Buttons
        toDashboard.setOnClickListener(v ->
                startActivity(new Intent(settingsHome.this, DashboardActivity.class))
        );

        toSettings.setOnClickListener(v ->
                startActivity(new Intent(settingsHome.this, settings.class))
        );

        toSettings2.setOnClickListener(v ->
                startActivity(new Intent(settingsHome.this, settings2.class))
        );

        // Reset Graph Data
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

        // Clear Budgeting Data
        resetBudgetsBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(settingsHome.this)
                    .setTitle("Clear Budgeting Data")
                    .setMessage("Are you sure you want to delete all budgeting limits?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        String userID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

                        if (userID != null) {
                            FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
                            DocumentReference userDoc = budgetData.collection("Users").document(userID);

                            userDoc.update("budgets", new ArrayList<>())
                                    .addOnSuccessListener(aVoid -> Toast.makeText(settingsHome.this, "Budgeting data cleared.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(settingsHome.this, "Failed to clear budgeting data.", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Clear Savings Goals
        resetSavingsBtn.setOnClickListener(savingsView -> {
            new AlertDialog.Builder(settingsHome.this)
                    .setTitle("Clear Savings Goals")
                    .setMessage("Are you sure you want to delete all savings goals?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        String userID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

                        if (userID != null) {
                            FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
                            DocumentReference userDoc = budgetData.collection("Users").document(userID);

                            userDoc.update("savingsGoals", new ArrayList<>())
                                    .addOnSuccessListener(aVoid -> Toast.makeText(settingsHome.this, "Savings goals cleared.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(settingsHome.this, "Failed to clear savings goals.", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Sign Out
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

        // Optional: force layout redraw
        new Handler().post(() -> {
            View rootView = findViewById(android.R.id.content);
            rootView.invalidate();
            rootView.requestLayout();
        });
    }
}