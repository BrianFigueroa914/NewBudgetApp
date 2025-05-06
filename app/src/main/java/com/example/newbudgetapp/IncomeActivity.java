package com.example.newbudgetapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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

public class IncomeActivity extends AppCompatActivity {

    private EditText incomeInput;
    private EditText incomeDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        incomeInput = findViewById(R.id.incomeInput);
        incomeDescription = findViewById(R.id.incomeDescription);
        Button addIncomeBtn = findViewById(R.id.addIncomeBtn);

        addIncomeBtn.setOnClickListener(v -> {
            String amountText = incomeInput.getText().toString().trim();
            String description = incomeDescription.getText().toString().trim();

            if (amountText.isEmpty() || description.isEmpty()) {
                Toast.makeText(IncomeActivity.this, "Please enter both amount and description", Toast.LENGTH_SHORT).show();
                return;
            }

            float amount;
            try {
                amount = Float.parseFloat(amountText);
            } catch (NumberFormatException e) {
                Toast.makeText(IncomeActivity.this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(IncomeActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userID = user.getUid();
            Map<String, Object> incomeData = new HashMap<>();
            incomeData.put("amount", amount);
            incomeData.put("timestamp", Timestamp.now());
            incomeData.put("description", description);

            DocumentReference userDoc = db.collection("Users").document(userID);
            userDoc.get().addOnSuccessListener(documentSnapshot -> {
                List<Map<String, Object>> incomeList = (List<Map<String, Object>>) documentSnapshot.get("incomeEntries");
                if (incomeList == null) incomeList = new ArrayList<>();
                incomeList.add(incomeData);
                userDoc.update("incomeEntries", incomeList)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(IncomeActivity.this, "Income saved!", Toast.LENGTH_SHORT).show();
                            incomeInput.setText("");
                            incomeDescription.setText("");

                            Intent intent = new Intent(IncomeActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> Toast.makeText(IncomeActivity.this, "Failed to save income", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(IncomeActivity.this, "Error accessing user data", Toast.LENGTH_SHORT).show());
        });
    }
}
