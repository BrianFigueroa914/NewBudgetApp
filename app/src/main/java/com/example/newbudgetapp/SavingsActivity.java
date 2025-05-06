package com.example.newbudgetapp;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SavingsActivity extends AppCompatActivity {
    private EditText goalTargetName, savingsTargetInput;
    private TextView deadlineDate, goalName;
    private ProgressBar savingsProgress;
    private Button selectDeadlineBtn, updateGoalBtn;
    private String userID;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);

        // Variables
        goalTargetName = findViewById(R.id.goalTargetName);
        savingsTargetInput = findViewById(R.id.savingsTargetInput);
        deadlineDate = findViewById(R.id.deadlineDate);
        goalName = findViewById(R.id.goalName);
        savingsProgress = findViewById(R.id.savingsProgress);
        selectDeadlineBtn = findViewById(R.id.selectDeadlineBtn);
        updateGoalBtn = findViewById(R.id.updateGoalBtn);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();

        // Get logged-in user's UID
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
            userID = user.getUid();
        else
            finish(); // Redirect to login

        // Load existing savings goal
        loadSavingsGoal();

        // Select deadline button logic
        selectDeadlineBtn.setOnClickListener(v -> showDatePicker());

        // Update savings goal button logic
        updateGoalBtn.setOnClickListener(v -> updateSavingsGoal());
    }

    private void showDatePicker() {
        selectedDate = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            deadlineDate.setText("Deadline: " + dateFormat.format(selectedDate.getTime()));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateSavingsGoal() {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance(); // Ensure correct Firestore reference
        DocumentReference userDoc = budgetData.collection("Users").document(userID); // Reference user doc
        String targetAmountText = savingsTargetInput.getText().toString().trim();
        String targetNameText = goalTargetName.getText().toString().trim();

        if (targetAmountText.isEmpty() || selectedDate == null || targetNameText.isEmpty()) {
            Toast.makeText(this, "Please enter a goal name, valid savings amount and select a deadline.", Toast.LENGTH_SHORT).show();
            return;
        }

        float targetAmount = Float.parseFloat(targetAmountText);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String deadline = dateFormat.format(selectedDate.getTime());

        //Prepare savings goal data
        Map<String, Object> savingsGoal = new HashMap<>();
        savingsGoal.put("goalName", targetNameText);
        savingsGoal.put("goalAmount", targetAmount);
        savingsGoal.put("deadline", deadline);

        //Store savings goal in Firestore under user document
        userDoc.update("savingsGoal", savingsGoal)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Savings goal updated successfully!", Toast.LENGTH_SHORT).show();
                        savingsProgress.setProgress(0); // Reset progress
                    } else {
                        Toast.makeText(this, "Failed to update savings goal.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSavingsGoal() {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance(); // Initialize Firestore
        DocumentReference userDoc = budgetData.collection("Users").document(userID); // Reference user document

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Retrieve savings goal data
                Map<String, Object> savingsGoal = (Map<String, Object>) task.getResult().get("savingsGoal");

                if (savingsGoal != null) {
                    // Safely extract values with validation
                    float goalAmount = savingsGoal.containsKey("goalAmount") ?
                            ((Number) savingsGoal.get("goalAmount")).floatValue() : 0;

                    String deadline = savingsGoal.containsKey("deadline") ?
                            (String) savingsGoal.get("deadline") : "Not Set";

                    String goal = savingsGoal.containsKey("goalName") ?
                            (String) savingsGoal.get("goalName") : "Not Set";

                    // Update UI components
                    savingsTargetInput.setText(String.valueOf(goalAmount));
                    deadlineDate.setText("Deadline: " + deadline);
                    goalName.setText(goal);
                } else {
                    Toast.makeText(SavingsActivity.this, "No savings goal set yet.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SavingsActivity.this, "Failed to load savings goal.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
