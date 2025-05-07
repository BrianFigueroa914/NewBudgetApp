

package com.example.newbudgetapp;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class SavingsActivity extends AppCompatActivity {
    private EditText goalTargetName, savingsTargetInput, budgetLimitInput;
    private Spinner budgetCategorySpinner;
    private TextView deadlineDate;
    private Button selectDeadlineBtn, updateGoalBtn, addBudgetBtn;
    private RecyclerView goalsRecyclerView, budgetsRecyclerView;

    private Calendar selectedDate;
    private String userID;
    private FirebaseFirestore db;
    private List<SavingsGoal> savingsGoals;
    private List<BudgetCategory> budgetCategories;
    private SavingsGoalAdapter goalAdapter;
    private BudgetAdapter budgetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);


        goalTargetName = findViewById(R.id.goalTargetName);
        savingsTargetInput = findViewById(R.id.savingsTargetInput);
        budgetLimitInput = findViewById(R.id.budgetLimitInput);
        budgetCategorySpinner = findViewById(R.id.budgetCategorySpinner);
        deadlineDate = findViewById(R.id.deadlineDate);
        selectDeadlineBtn = findViewById(R.id.selectDeadlineBtn);
        updateGoalBtn = findViewById(R.id.updateGoalBtn);
        addBudgetBtn = findViewById(R.id.addBudgetBtn);
        goalsRecyclerView = findViewById(R.id.goalsRecyclerView);
        budgetsRecyclerView = findViewById(R.id.budgetsRecyclerView);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            userID = user.getUid();
        } else {
            finish(); return;
        }

        savingsGoals = new ArrayList<>();
        goalAdapter = new SavingsGoalAdapter(savingsGoals, userID);
        goalsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        goalsRecyclerView.setAdapter(goalAdapter);

        budgetCategories = new ArrayList<>();
        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        String[] categories = {"Rent", "Groceries", "Utilities", "Transportation", "Going Out", "Entertainment", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        budgetCategorySpinner.setAdapter(categoryAdapter);

        loadSavingsGoals();
        loadBudgets();

        selectDeadlineBtn.setOnClickListener(v -> showDatePicker());
        updateGoalBtn.setOnClickListener(v -> addNewGoal());
        addBudgetBtn.setOnClickListener(v -> addNewBudget());
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

    private void addNewGoal() {
        String targetNameText = goalTargetName.getText().toString().trim();
        String targetAmountText = savingsTargetInput.getText().toString().trim();

        if (TextUtils.isEmpty(targetAmountText) || selectedDate == null || TextUtils.isEmpty(targetNameText)) {
            Toast.makeText(this, "Please enter a goal name, valid savings amount and select a deadline.", Toast.LENGTH_SHORT).show();
            return;
        }

        float targetAmount = Float.parseFloat(targetAmountText);
        String deadline = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.getTime());

        Map<String, Object> goalMap = new HashMap<>();
        goalMap.put("goalName", targetNameText);
        goalMap.put("goalAmount", targetAmount);
        goalMap.put("deadline", deadline);
        goalMap.put("currentAmount", 0f);

        DocumentReference userDoc = db.collection("Users").document(userID);

        userDoc.update("savingsGoals", FieldValue.arrayUnion(goalMap))
                .addOnSuccessListener(aVoid -> {
                    // Update local UI
                    SavingsGoal newGoal = new SavingsGoal(targetNameText, targetAmount, deadline, 0);
                    savingsGoals.add(newGoal);
                    goalAdapter.notifyDataSetChanged();
                    goalTargetName.setText("");
                    savingsTargetInput.setText("");
                    deadlineDate.setText("Deadline: Not Set");

                    // Log a $0 expense entry for tracking
                    Map<String, Object> expenseEntry = new HashMap<>();
                    expenseEntry.put("amount", 0f);
                    expenseEntry.put("category", "Saved to Goal: " + targetNameText);
                    expenseEntry.put("timestamp", com.google.firebase.Timestamp.now());

                    userDoc.get().addOnSuccessListener(doc -> {
                        List<Map<String, Object>> expenseList = (List<Map<String, Object>>) doc.get("expenseEntries");
                        if (expenseList == null) expenseList = new ArrayList<>();
                        expenseList.add(expenseEntry);
                        userDoc.update("expenseEntries", expenseList);

                        // ✅ Navigate back to Dashboard to refresh chart
                        Intent intent = new Intent(SavingsActivity.this, DashboardActivity.class);
                        startActivity(intent);
                    });
                });
    }



    private void addNewBudget() {
        String category = budgetCategorySpinner.getSelectedItem().toString();
        String limitStr = budgetLimitInput.getText().toString().trim();

        if (TextUtils.isEmpty(category) || TextUtils.isEmpty(limitStr)) {
            Toast.makeText(this, "Enter category and limit", Toast.LENGTH_SHORT).show();
            return;
        }

        float limit = Float.parseFloat(limitStr);
        BudgetCategory budget = new BudgetCategory(category, limit);

        db.collection("Users").document(userID)
                .update("budgets", FieldValue.arrayUnion(budget))
                .addOnSuccessListener(aVoid -> {
                    budgetCategories.add(budget);
                    loadBudgets();
                    budgetLimitInput.setText("");
                });
    }

    private void loadSavingsGoals() {
        db.collection("Users").document(userID).get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) doc.get("savingsGoals");
                    if (list != null) {
                        for (Map<String, Object> m : list) {
                            String n = (String) m.get("goalName");
                            float a = ((Number) m.get("goalAmount")).floatValue();
                            String d = (String) m.get("deadline");
                            float c = m.containsKey("currentAmount") ? ((Number) m.get("currentAmount")).floatValue() : 0;
                            savingsGoals.add(new SavingsGoal(n, a, d, c));
                        }
                        goalAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadBudgets() {
        db.collection("Users").document(userID).get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> budgetList = (List<Map<String, Object>>) doc.get("budgets");
                    List<Map<String, Object>> expenseList = (List<Map<String, Object>>) doc.get("expenseEntries");

                    Map<String, Float> spendingMap = new HashMap<>();
                    if (expenseList != null) {
                        for (Map<String, Object> expense : expenseList) {
                            if (expense.containsKey("category")) {
                                String category = (String) expense.get("category");
                                float amount = ((Number) expense.get("amount")).floatValue();
                                spendingMap.put(category, spendingMap.getOrDefault(category, 0f) + amount);
                            }
                        }
                    }

                    budgetCategories.clear();
                    if (budgetList != null) {
                        for (Map<String, Object> m : budgetList) {
                            String c = (String) m.get("category");
                            float l = ((Number) m.get("limit")).floatValue();
                            budgetCategories.add(new BudgetCategory(c, l));
                        }
                    }

                    budgetAdapter = new BudgetAdapter(budgetCategories, spendingMap, userID);
                    budgetsRecyclerView.setAdapter(budgetAdapter);
                    budgetAdapter.notifyDataSetChanged();
                });
    }
}
