package com.example.newbudgetapp;

import com.example.newbudgetapp.AchievementsActivity;
import com.google.firebase.firestore.Source;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.LimitLine;
import java.util.Random;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newbudgetapp.AddExpenseActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private LineChart lineChart;
    private List<Entry> incomeEntries = new ArrayList<>();
    private List<String> dayLabels = new ArrayList<>();
    private String userID;
    private FirebaseAuth mAuth;
    private com.google.firebase.Timestamp lastTimestamp = null;
    private void addGoalLineToChart(float goalAmount) {
        LimitLine goalLine = new LimitLine(goalAmount, "Goal: $" + (int) goalAmount);
        goalLine.setLineColor(Color.MAGENTA);
        goalLine.setLineWidth(2f);
        goalLine.setTextColor(Color.MAGENTA);
        goalLine.setTextSize(12f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.removeAllLimitLines();  // Clear previous lines
        leftAxis.addLimitLine(goalLine);
    }

    private int generatePastelColor(String key) {
        int hash = Math.abs(key.hashCode());
        int red = (hash % 128) + 127;
        int green = ((hash / 128) % 128) + 127;
        int blue = ((hash / 16384) % 128) + 127;
        return Color.rgb(red, green, blue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Variables
        TextView usernameText = findViewById(R.id.usernameText);
        lineChart = findViewById(R.id.lineChart);
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
            prepareChartDataForCurrentMonth();
        } else {
            finish(); // No user logged in
        }

        // Set current month label
        String currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
        monthLabel.setText(currentMonth + " Summary");

        incomeCardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, IncomeActivity.class);
            startActivity(intent);
        });

        expenseCardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, 1001);
        });

        // Navigation buttons
        savingsBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, SavingsActivity.class)));
        visualsBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, visualAnalytics.class)));
        achievementsCardBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AchievementsActivity.class)));
        settingsCardBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, settingsHome.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DEBUG", "onResume: refreshing chart from return");
        prepareChartDataForCurrentMonth();  // Ensure the graph is refreshed every time activity resumes
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

                    Date date = ((com.google.firebase.Timestamp) entry.get("timestamp")).toDate();
                    String label = new SimpleDateFormat("MMM d", Locale.getDefault()).format(date);
                    dayLabels.add(label);
                }

                updateChart();  // Redraws balance and goal lines
            }
        });
    }

    private void updateChart() {
        TextView incomeBalanceText = findViewById(R.id.incomeBalanceText);

        if (!incomeEntries.isEmpty()) {
            float latestBalance = incomeEntries.get(incomeEntries.size() - 1).getY();
            incomeBalanceText.setText("Balance: $" + String.format(Locale.getDefault(), "%.2f", latestBalance));
        } else {
            incomeBalanceText.setText("Balance: $0.00");
        }

        LineDataSet balanceLine = new LineDataSet(incomeEntries, "Balance");
        balanceLine.setColor(Color.GREEN);
        balanceLine.setDrawCircles(false);
        balanceLine.setLineWidth(2f);
        balanceLine.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // ✅ Only show value label on the last data point
        balanceLine.setDrawValues(true);
        balanceLine.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                if (entry.equals(incomeEntries.get(incomeEntries.size() - 1))) {
                    return String.valueOf((int) entry.getY());
                } else {
                    return "";
                }
            }
        });

        lineChart.setData(new LineData(balanceLine));

        // === Fetch savings goals and apply limit lines ===
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("Users").document(userID);

        userDoc.get(Source.SERVER).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<Map<String, Object>> goalsList = (List<Map<String, Object>>) doc.get("savingsGoals");

                YAxis leftAxis = lineChart.getAxisLeft();
                leftAxis.removeAllLimitLines();  // Clear old goals

                float minY = Float.MAX_VALUE;
                float maxY = Float.MIN_VALUE;
                for (Entry entry : incomeEntries) {
                    float y = entry.getY();
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }

                float highestGoal = maxY;

                if (goalsList != null) {
                    for (Map<String, Object> goal : goalsList) {
                        if (goal.containsKey("goalAmount") && goal.containsKey("goalName")) {
                            float amount = ((Number) goal.get("goalAmount")).floatValue();
                            String name = (String) goal.get("goalName");

                            LimitLine goalLine = new LimitLine(amount, name + ": $" + (int) amount);
                            int pastelColor = generatePastelColor(name); // `name` is the goalName
                            goalLine.setLineColor(pastelColor);
                            goalLine.setTextColor(pastelColor);
                            goalLine.setLineWidth(2f);
                            goalLine.setTextSize(10f);
                            leftAxis.addLimitLine(goalLine);

                            if (amount > highestGoal) highestGoal = amount;
                        }
                    }
                }

                leftAxis.setAxisMinimum(minY - 50);
                leftAxis.setAxisMaximum(highestGoal + 50);
                leftAxis.setTextColor(Color.DKGRAY);
                lineChart.getAxisRight().setEnabled(false);
                lineChart.getDescription().setEnabled(false);
                lineChart.getLegend().setEnabled(true);

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(new DayValueFormatter(dayLabels));
                xAxis.setGranularity(1f);
                xAxis.setLabelRotationAngle(-45);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextColor(Color.DKGRAY);

                lineChart.invalidate();  // Refresh chart
            }
        });
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
