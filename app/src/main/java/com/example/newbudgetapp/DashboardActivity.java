package com.example.newbudgetapp;

import com.example.newbudgetapp.AchievementsActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private boolean isIncomeMode = true;
    private LineChart lineChart;
    private List<Entry> incomeEntries = new ArrayList<>();
    private List<String> dayLabels = new ArrayList<>();
    private String[] categories = {"Rent", "Groceries", "Utilities", "Going Out", "Transportation", "Entertainment", "Other"};
    private String userID;
    private com.google.firebase.Timestamp lastTimestamp = null;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Variables
        TextView usernameText = findViewById(R.id.usernameText);
        lineChart = findViewById(R.id.lineChart);
        EditText textHintInput = findViewById(R.id.monetaryInput);
        Button addDataBtn = findViewById(R.id.addDataBtn);
        Spinner expenseCategorySpinner = findViewById(R.id.expenseCategorySpinner);
        TextView monthLabel = findViewById(R.id.monthLabel);
        CardView incomeCardBtn = findViewById(R.id.incomeCardBtn);
        CardView expenseCardBtn = findViewById(R.id.expenseCardBtn);
        CardView savingsBtn = findViewById(R.id.savingsCardBtn);
        CardView visualsBtn = findViewById(R.id.visualsCardBtn);
        CardView achievementsCardBtn = findViewById(R.id.achievementsCardBtn);
        CardView settingsCardBtn = findViewById(R.id.settingsCardBtn);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            userID = user.getUid();
            FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
            DocumentReference userDoc = budgetData.collection("Users").document(userID);
            userDoc.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    if (username != null && !username.isEmpty()) {
                        usernameText.setText("Welcome " + username);
                    }
                }
            });

            // Initial load of chart data
            prepareChartDataForCurrentMonth();  // This sets up the chart and lastTimestamp
        } else {
            finish(); // No user logged in
        }

        // Set current month label
        String currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
        monthLabel.setText(currentMonth + " Summary");

        // Handle input
        addDataBtn.setOnClickListener(v -> {
            String inputAmount = textHintInput.getText().toString().trim();

            if (inputAmount.isEmpty()) {
                Toast.makeText(DashboardActivity.this, "Please enter a value", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float amount = Float.parseFloat(inputAmount);
                if (Float.isNaN(amount) || Float.isInfinite(amount)) throw new NumberFormatException();

                if (isIncomeMode) {
                    storeIncomeData(userID, amount);  // This now calls prepareChartAppend()
                } else {
                    String selectedCategory = expenseCategorySpinner.getSelectedItem().toString();
                    storeExpenseData(userID, selectedCategory, amount);  // Also calls prepareChartAppend()
                }

                textHintInput.setText("");  // Clear input

            } catch (NumberFormatException e) {
                Toast.makeText(DashboardActivity.this, "Invalid number. Please enter a valid amount.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up category spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseCategorySpinner.setAdapter(adapter);

        expenseCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Mode toggle buttons
        incomeCardBtn.setOnClickListener(v -> {
            isIncomeMode = true;
            textHintInput.setHint("Enter income");
            addDataBtn.setText("Add Income");
            expenseCategorySpinner.setVisibility(View.GONE);
        });

        expenseCardBtn.setOnClickListener(v -> {
            isIncomeMode = false;
            textHintInput.setHint("Enter expense");
            addDataBtn.setText("Add Expense");
            expenseCategorySpinner.setVisibility(View.VISIBLE);
        });

        // Navigation buttons
        savingsBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, SavingsActivity.class)));
        visualsBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, visualAnalytics.class)));
        achievementsCardBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AchievementsActivity.class)));
        settingsCardBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, settingsHome.class)));
    }

    // Methods
    private void prepareChartDataForCurrentMonth() {
        incomeEntries.clear();
        dayLabels.clear();

        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                List<Map<String, Object>> incomeList = (List<Map<String, Object>>) task.getResult().get("incomeEntries");
                List<Map<String, Object>> expenseList = (List<Map<String, Object>>) task.getResult().get("expenseEntries");

                // Use a list of timestamped changes (positive for income, negative for expense)
                List<Map<String, Object>> allEntries = new ArrayList<>();

                if (incomeList != null) allEntries.addAll(incomeList);
                if (expenseList != null) allEntries.addAll(expenseList);

                // Sort all entries by timestamp
                allEntries.sort((a, b) -> {
                    Date dateA = ((com.google.firebase.Timestamp) a.get("timestamp")).toDate();
                    Date dateB = ((com.google.firebase.Timestamp) b.get("timestamp")).toDate();
                    return dateA.compareTo(dateB);
                });

                float runningBalance = 0f;

                for (Map<String, Object> entry : allEntries) {
                    float amount = ((Number) entry.get("amount")).floatValue();

                    // Check if it's an expense
                    if (entry.containsKey("category")) {
                        amount = -amount;
                    }

                    runningBalance += amount;

                    incomeEntries.add(new Entry(incomeEntries.size(), runningBalance));

                    // Label: use full date (e.g., "May 5")
                    Date date = ((com.google.firebase.Timestamp) entry.get("timestamp")).toDate();
                    String label = new SimpleDateFormat("MMM d", Locale.getDefault()).format(date);
                    dayLabels.add(label);
                }

                if (!allEntries.isEmpty()) {
                    Map<String, Object> lastEntry = allEntries.get(allEntries.size() - 1);
                    lastTimestamp = (com.google.firebase.Timestamp) lastEntry.get("timestamp");
                }


                updateChart();
            } else {
                Toast.makeText(DashboardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChart() {
        LineDataSet dataSet = new LineDataSet(incomeEntries, "Monthly Balance");
        dataSet.setColor(Color.GREEN);
        dataSet.setCircleColor(Color.GREEN);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smoother curve

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Update the visible balance based on the last data point
        if (!incomeEntries.isEmpty()) {
            float latestBalance = incomeEntries.get(incomeEntries.size() - 1).getY();
            TextView incomeBalanceText = findViewById(R.id.incomeBalanceText);
            incomeBalanceText.setText("Balance: $" + String.format(Locale.getDefault(), "%.2f", latestBalance));
        }

        // Chart appearance settings
        lineChart.getAxisLeft().removeAllLimitLines();
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.getAxisRight().setEnabled(false);

        // X Axis formatting
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DayValueFormatter(dayLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.DKGRAY);

        // Y Axis formatting
        lineChart.getAxisLeft().setTextColor(Color.DKGRAY);

        lineChart.invalidate(); // Refresh the chart
    }

    // Custom formatter to show day numbers (12, 13, etc.)
    public class DayValueFormatter extends ValueFormatter {
        private final List<String> dayLabels;

        public DayValueFormatter(List<String> dayLabels) {
            this.dayLabels = dayLabels;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < dayLabels.size()) {
                return dayLabels.get(index);
            } else {
                return "";
            }
        }
    }

    // Grab income data
    private void storeIncomeData(String userID, float income) {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                List<Map<String, Object>> incomeList = (List<Map<String, Object>>) task.getResult().get("incomeEntries");
                if (incomeList == null) incomeList = new ArrayList<>();

                Map<String, Object> incomeData = new HashMap<>();
                incomeData.put("amount", income);
                incomeData.put("timestamp", com.google.firebase.Timestamp.now());
                incomeList.add(incomeData);

                userDoc.update("incomeEntries", incomeList)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Income updated successfully", Toast.LENGTH_SHORT).show();
                                prepareChartDataForCurrentMonth(); // ONLY run this when data is saved
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to update income", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // First-time setup
                List<Map<String, Object>> initialIncomeList = new ArrayList<>();
                Map<String, Object> incomeData = new HashMap<>();
                incomeData.put("amount", income);
                incomeData.put("timestamp", com.google.firebase.Timestamp.now());
                initialIncomeList.add(incomeData);

                userDoc.set(Collections.singletonMap("incomeEntries", initialIncomeList))
                        .addOnCompleteListener(createTask -> {
                            if (createTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Income saved successfully", Toast.LENGTH_SHORT).show();
                                prepareChartDataForCurrentMonth(); //  Now safe to refresh
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to save income", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void storeExpenseData(String userID, String category, float expense) {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                List<Map<String, Object>> expenseList = (List<Map<String, Object>>) task.getResult().get("expenseEntries");
                if (expenseList == null) expenseList = new ArrayList<>();

                Map<String, Object> expenseData = new HashMap<>();
                expenseData.put("amount", expense);
                expenseData.put("category", category);
                expenseData.put("timestamp", com.google.firebase.Timestamp.now());
                expenseList.add(expenseData);

                userDoc.update("expenseEntries", expenseList)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                                prepareChartAppend();//  Only after it's saved
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to update expense", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // First-time setup
                List<Map<String, Object>> initialExpenseList = new ArrayList<>();
                Map<String, Object> expenseData = new HashMap<>();
                expenseData.put("amount", expense);
                expenseData.put("category", category);
                expenseData.put("timestamp", com.google.firebase.Timestamp.now());
                initialExpenseList.add(expenseData);

                userDoc.set(Collections.singletonMap("expenseEntries", initialExpenseList))
                        .addOnCompleteListener(createTask -> {
                            if (createTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                                prepareChartAppend();//  Safe to refresh
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void prepareChartAppend() {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                List<Map<String, Object>> newEntries = new ArrayList<>();

                List<Map<String, Object>> incomeList = (List<Map<String, Object>>) task.getResult().get("incomeEntries");
                List<Map<String, Object>> expenseList = (List<Map<String, Object>>) task.getResult().get("expenseEntries");

                if (incomeList != null) {
                    for (Map<String, Object> entry : incomeList) {
                        com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) entry.get("timestamp");
                        if (lastTimestamp == null || timestamp.compareTo(lastTimestamp) > 0) {
                            newEntries.add(entry);
                        }
                    }
                }

                if (expenseList != null) {
                    for (Map<String, Object> entry : expenseList) {
                        com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) entry.get("timestamp");
                        if (lastTimestamp == null || timestamp.compareTo(lastTimestamp) > 0) {
                            entry.put("amount", -((Number) entry.get("amount")).floatValue()); // make it negative
                            newEntries.add(entry);
                        }
                    }
                }

                // Sort by timestamp
                newEntries.sort((a, b) -> {
                    Date da = ((com.google.firebase.Timestamp) a.get("timestamp")).toDate();
                    Date dbt = ((com.google.firebase.Timestamp) b.get("timestamp")).toDate();
                    return da.compareTo(dbt);
                });

                float lastY = incomeEntries.isEmpty() ? 0f : incomeEntries.get(incomeEntries.size() - 1).getY();

                for (Map<String, Object> entry : newEntries) {
                    float amount = ((Number) entry.get("amount")).floatValue();
                    lastY += amount;

                    incomeEntries.add(new Entry(incomeEntries.size(), lastY));

                    Date date = ((com.google.firebase.Timestamp) entry.get("timestamp")).toDate();
                    String label = new SimpleDateFormat("MMM d", Locale.getDefault()).format(date);
                    dayLabels.add(label);

                    lastTimestamp = (com.google.firebase.Timestamp) entry.get("timestamp"); // Update last seen
                }
                updateChart();
            }
        });
    }
}
