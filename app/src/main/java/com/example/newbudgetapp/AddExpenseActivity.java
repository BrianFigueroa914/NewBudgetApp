package com.example.newbudgetapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText amountInput;
    private Spinner categorySpinner;
    private Button addExpenseBtn;

    private final String[] categories = {"Rent", "Groceries", "Utilities", "Going Out", "Transportation", "Entertainment", "Other"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        amountInput = findViewById(R.id.expenseAmount);
        categorySpinner = findViewById(R.id.expenseCategorySpinner);
        addExpenseBtn = findViewById(R.id.addExpenseBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(adapter);

        addExpenseBtn.setOnClickListener(v -> {
            String amountText = amountInput.getText().toString().trim();

            if (amountText.isEmpty()) {
                Toast.makeText(AddExpenseActivity.this, "Enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            float amount;
            try {
                amount = Float.parseFloat(amountText);
            } catch (NumberFormatException e) {
                Toast.makeText(AddExpenseActivity.this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = categorySpinner.getSelectedItem().toString();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDoc = db.collection("Users").document(userId);

            userDoc.get().addOnSuccessListener(documentSnapshot -> {
                List<Map<String, Object>> expenseList = (List<Map<String, Object>>) documentSnapshot.get("expenseEntries");
                if (expenseList == null) expenseList = new ArrayList<>();

                Map<String, Object> expenseData = new HashMap<>();
                expenseData.put("amount", amount);
                expenseData.put("category", category);
                expenseData.put("timestamp", Timestamp.now());

                expenseList.add(expenseData);

                userDoc.update("expenseEntries", expenseList)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Expense saved under: " + category, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show());
            });
        });
    }
}