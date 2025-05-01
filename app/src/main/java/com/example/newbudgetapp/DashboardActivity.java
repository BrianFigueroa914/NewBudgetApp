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
    private String[] categories = {"Rent", "Food", "Utilities", "Transportation", "Entertainment", "Other"};
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Step 1: Get references to all views
        TextView usernameText = findViewById(R.id.usernameText);
        lineChart = findViewById(R.id.lineChart);
        EditText textHintInput = findViewById(R.id.monetaryInput);
        Button addDataBtn = findViewById(R.id.addDataBtn);
        Spinner expenseCategorySpinner = findViewById(R.id.expenseCategorySpinner);
        TextView monthLabel = findViewById(R.id.monthLabel);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        CardView incomeCardBtn = findViewById(R.id.incomeCardBtn);
        CardView expenseCardBtn = findViewById(R.id.expenseCardBtn);
        CardView savingsBtn = findViewById(R.id.savingsCardBtn);
        CardView visualsBtn = findViewById(R.id.visualsCardBtn);
        CardView achievementsCardBtn = findViewById(R.id.achievementsCardBtn);
        CardView settingsCardBtn = findViewById(R.id.settingsCardBtn);

        //Get logged-in user's UID
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            fetchPreviousData(); // Fetch and display existing data
        } else
            finish(); // Redirect to login


        // Step 2: Set the dynamic month label
        String currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
        monthLabel.setText(currentMonth + " Summary");

        //Step 3: Prepare chart for current month summary
        prepareChartDataForCurrentMonth();

        // Step 4: Initial chart setup
        updateChart();

        // Step 5: Handle data input on button click
        addDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputAmount = textHintInput.getText().toString().trim();
                if (!inputAmount.isEmpty()) {
                    float amount = Float.parseFloat(inputAmount);

                    //Handle data Input whether in income or expense
                    if (isIncomeMode) {
                        // Handle income addition
                        storeIncomeData(userID, amount);

                        // Get current day of month and store it as x-axis label
                        String currentDay = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());
                        incomeEntries.add(new Entry(incomeEntries.size(), amount));
                        dayLabels.add(currentDay);
                    } else {
                        // Handle expense addition
                        String selectedCategory = expenseCategorySpinner.getSelectedItem().toString();
                        storeExpenseData(userID, selectedCategory, amount);
                    }

                    updateChart();
                    textHintInput.setText(""); // Clear input after adding
                }
            }
        });

        //Populates spinner with categories
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseCategorySpinner.setAdapter(adapter);

        //Detects which category user chose
        expenseCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Button functionality - Just need to create the activity and layout files
        incomeCardBtn.setOnClickListener(v -> {
            isIncomeMode = true;
            textHintInput.setHint("Enter income");
            addDataBtn.setText("Add Income");
            expenseCategorySpinner.setVisibility(View.GONE); // Hide spinner when entering income
        });
        expenseCardBtn.setOnClickListener(v -> {
            isIncomeMode = false;
            textHintInput.setHint("Enter expense");
            addDataBtn.setText("Add Expense");
            // Add a dropdown menu for expense categories
            expenseCategorySpinner.setVisibility(View.VISIBLE); // Make it visible
        });

/*      savingsCardBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, savings.class));
        });
        visualsCardBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, visualAnalytics.class));
        });*/
        achievementsCardBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AchievementsActivity.class));
        });

        settingsCardBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, settingsHome.class));
        });
    }

    //Methods
    private void prepareChartDataForCurrentMonth() {
        incomeEntries.clear(); // Clear previous data
        dayLabels.clear(); // Clear previous labels

        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Get income and expense data
                List<Map<String, Object>> incomeList = (List<Map<String, Object>>) task.getResult().get("incomeEntries");
                List<Map<String, Object>> expenseList = (List<Map<String, Object>>) task.getResult().get("expenses");

                if (incomeList != null || expenseList != null) {
                    Map<String, Float> dailyBalances = new HashMap<>();

                    String currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());

                    // Process Income
                    if (incomeList != null) {
                        for (Map<String, Object> incomeData : incomeList) {
                            Date incomeDate = ((com.google.firebase.Timestamp) incomeData.get("timestamp")).toDate();
                            String dayLabel = new SimpleDateFormat("d", Locale.getDefault()).format(incomeDate);

                            if (currentMonth.equals(new SimpleDateFormat("MMMM", Locale.getDefault()).format(incomeDate))) {
                                float amount = ((Number) incomeData.get("amount")).floatValue();
                                dailyBalances.put(dayLabel, dailyBalances.getOrDefault(dayLabel, 0f) + amount);
                            }
                        }
                    }

                    // Process Expenses
                    if (expenseList != null) {
                        for (Map<String, Object> expenseData : expenseList) {
                            Date expenseDate = ((com.google.firebase.Timestamp) expenseData.get("timestamp")).toDate();
                            String dayLabel = new SimpleDateFormat("d", Locale.getDefault()).format(expenseDate);

                            if (currentMonth.equals(new SimpleDateFormat("MMMM", Locale.getDefault()).format(expenseDate))) {
                                float amount = ((Number) expenseData.get("amount")).floatValue();
                                dailyBalances.put(dayLabel, dailyBalances.getOrDefault(dayLabel, 0f) - amount);
                            }
                        }
                    }

                    // Sort by day and add to chart data
                    List<String> sortedDays = new ArrayList<>(dailyBalances.keySet());
                    Collections.sort(sortedDays, (a, b) -> Integer.parseInt(a) - Integer.parseInt(b)); // Sort by day (numerical order)

                    for (String day : sortedDays) {
                        float balance = dailyBalances.get(day);
                        incomeEntries.add(new Entry(incomeEntries.size(), balance));
                        dayLabels.add(day);
                    }

                    updateChart(); // Update the chart after preparing data
                }
            } else
                Toast.makeText(DashboardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateChart() {
        LineDataSet dataSet = new LineDataSet(incomeEntries, "Monthly Balance"); // Change label to "Monthly Balance"
        dataSet.setColor(Color.GREEN); // Optional: Use a distinct color for balance
        dataSet.setCircleColor(Color.GREEN);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Add/redraw budget line
        float budget = 1100f; // Budget as a reference
        lineChart.getAxisLeft().removeAllLimitLines();
        LimitLine budgetLine = new LimitLine(budget, "Budget");
        budgetLine.setLineColor(Color.RED);
        budgetLine.setLineWidth(2f);
        budgetLine.setTextColor(Color.RED);
        budgetLine.setTextSize(12f);
        lineChart.getAxisLeft().addLimitLine(budgetLine);

        // Format X axis using day labels
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DayValueFormatter(dayLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate(); // Refresh chart
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

    //Grab income data
    private void storeIncomeData(String userID, float income) {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();

        // Reference the user's document
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        // Fetch existing data or create new if necessary
        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Document exists: update the incomeEntries
                List<Map<String, Object>> incomeList = (List<Map<String, Object>>) task.getResult().get("incomeEntries");
                if (incomeList == null) {
                    incomeList = new ArrayList<>();
                }

                // Add the new income to the list
                Map<String, Object> incomeData = new HashMap<>();
                incomeData.put("amount", income);
                incomeData.put("timestamp", com.google.firebase.Timestamp.now());
                incomeList.add(incomeData);

                // Update the incomeEntries in Firestore
                userDoc.update("incomeEntries", incomeList)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Income updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to update income, please retry", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Document does not exist: create new incomeEntries list
                List<Map<String, Object>> initialIncomeList = new ArrayList<>();
                Map<String, Object> incomeData = new HashMap<>();
                incomeData.put("amount", income);
                incomeData.put("timestamp", com.google.firebase.Timestamp.now()); //Firestore timestamp
                initialIncomeList.add(incomeData);

                userDoc.set(Collections.singletonMap("incomeEntries", initialIncomeList))
                        .addOnCompleteListener(createTask -> {
                            if (createTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Income saved successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to save income, please retry", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void storeExpenseData(String userID, String category, float expense) {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();

        // Reference the user's document
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        // Fetch existing data or create new if necessary
        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Document exists: update the expenseEntries
                List<Map<String, Object>> expenseList = (List<Map<String, Object>>) task.getResult().get("expenseEntries");
                if (expenseList == null) {
                    expenseList = new ArrayList<>();
                }

                // Add the new expense to the list
                Map<String, Object> expenseData = new HashMap<>();
                expenseData.put("amount", expense);
                expenseData.put("category", category);
                expenseData.put("timestamp", com.google.firebase.Timestamp.now());
                expenseList.add(expenseData);

                // Update the expenseEntries in Firestore
                userDoc.update("expenseEntries", expenseList)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to update expense, please retry", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Document does not exist: create new expenseEntries list
                List<Map<String, Object>> initialExpenseList = new ArrayList<>();
                Map<String, Object> expenseData = new HashMap<>();
                expenseData.put("amount", expense);
                expenseData.put("category", category);
                expenseData.put("timestamp", com.google.firebase.Timestamp.now()); //Firestore timestamp
                initialExpenseList.add(expenseData);

                userDoc.set(Collections.singletonMap("expenseEntries", initialExpenseList))
                        .addOnCompleteListener(createTask -> {
                            if (createTask.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to save expense, please retry", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    //Load previous session data for user
    private void fetchPreviousData() {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Retrieve existing income entries
                List<Map<String, Object>> incomeList = (List<Map<String, Object>>) task.getResult().get("incomeEntries");
                if (incomeList != null) {
                    incomeEntries.clear();
                    dayLabels.clear();

                    for (int i = 0; i < incomeList.size(); i++) {
                        Map<String, Object> incomeData = incomeList.get(i);
                        if (incomeData.containsKey("amount") && incomeData.containsKey("timestamp")) {
                            float amount = ((Number) incomeData.get("amount")).floatValue();
                            String dayLabel = new SimpleDateFormat("d", Locale.getDefault())
                                    .format(((com.google.firebase.Timestamp) incomeData.get("timestamp")).toDate());

                            incomeEntries.add(new Entry(i, amount));
                            dayLabels.add(dayLabel);
                        }
                    }

                    // Update the chart after fetching data
                    updateChart();
                }
            } else {
                Toast.makeText(DashboardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
