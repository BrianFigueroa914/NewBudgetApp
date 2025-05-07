package com.example.newbudgetapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

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
            finish();
        }

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

        savingsBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, SavingsActivity.class)));
        visualsBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, visualAnalytics.class)));
        achievementsCardBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AchievementsActivity.class)));
        settingsCardBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, settingsHome.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareChartDataForCurrentMonth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(userID)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        prepareChartDataForCurrentMonth();
                    });
        }
    }

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

                allEntries.sort((a, b) -> {
                    Date dateA = ((com.google.firebase.Timestamp) a.get("timestamp")).toDate();
                    Date dateB = ((com.google.firebase.Timestamp) b.get("timestamp")).toDate();
                    return dateA.compareTo(dateB);
                });

                float runningBalance = 0f;

                for (Map<String, Object> entry : allEntries) {
                    float amount = ((Number) entry.get("amount")).floatValue();

                    if (entry.containsKey("category")) {
                        amount = -amount;
                    }

                    runningBalance += amount;
                    incomeEntries.add(new Entry(incomeEntries.size(), runningBalance));

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
        balanceLine.setDrawValues(true);

        LineData lineData = new LineData(balanceLine);
        lineChart.setData(lineData);

        float minY = 0f, maxY = 0f;
        for (Entry entry : incomeEntries) {
            float y = entry.getY();
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(minY - 50);
        leftAxis.setAxisMaximum(maxY + 50);
        leftAxis.setTextColor(Color.DKGRAY);
        lineChart.getAxisRight().setEnabled(false);

        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("Users").document(userID);
        userDocRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Map<String, Object> savingsGoal = (Map<String, Object>) doc.get("savingsGoal");
                if (savingsGoal != null && savingsGoal.containsKey("goalAmount")) {
                    float goalAmount = ((Number) savingsGoal.get("goalAmount")).floatValue();
                    LimitLine goalLine = new LimitLine(goalAmount, "Goal: $" + (int) goalAmount);
                    goalLine.setLineColor(Color.MAGENTA);
                    goalLine.setLineWidth(2f);
                    goalLine.setTextColor(Color.MAGENTA);
                    goalLine.setTextSize(12f);
                    leftAxis.removeAllLimitLines();
                    leftAxis.addLimitLine(goalLine);
                }
            }
            lineChart.invalidate();
        });

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DayValueFormatter(dayLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.DKGRAY);
    }

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
}
