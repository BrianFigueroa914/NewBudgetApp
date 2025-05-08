package com.example.newbudgetapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class visualAnalytics extends AppCompatActivity {
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visual_analytics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.visualsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get references to all views
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        PieChart pieChart = findViewById(R.id.pieChart);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            fetchExpenseData(); // Fetch and display existing data
        }
        else
            finish(); // Redirect to login
    }

    // Methods
    private void fetchExpenseData() {
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        DocumentReference userDoc = budgetData.collection("Users").document(userID);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                List<Map<String, Object>> expenseList = (List<Map<String, Object>>) task.getResult().get("expenseEntries");

                if (expenseList != null) {
                    Map<String, Float> categoryTotals = new HashMap<>();

                    for (Map<String, Object> expense : expenseList) {
                        String category = (String) expense.get("category");
                        float amount = ((Number) expense.get("amount")).floatValue();

                        // Exclude "Saved to Goal" entries
                        if (!category.contains("Saved to Goal:")) {
                            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
                        }
                    }
                    // Pass categorized totals to progress bars and pie Chart method
                    generatePieChart(categoryTotals);
                    updateProgressBars(categoryTotals);
                }
            }
        });
    }

    private void updateProgressBars(Map<String, Float> categoryTotals) {
        // Retrieve progress bars by ID
        ProgressBar rentProgress = findViewById(R.id.rentProgressbar);
        ProgressBar groceryProgress = findViewById(R.id.groceryProgressbar);

        TextView rentText = findViewById(R.id.rentText);
        TextView foodText = findViewById(R.id.groceryText);

        // Set dynamic progress based on Firestore data
        float rentTotal = categoryTotals.getOrDefault("Rent", 0f);
        float groceryTotal = categoryTotals.getOrDefault("Groceries", 0f);

        rentProgress.setProgress((int) rentTotal);
        groceryProgress.setProgress((int) groceryTotal);

        rentText.setText("Rent: $" + rentTotal);
        foodText.setText("Grcoeries: $" + groceryTotal);
    }

    private void generatePieChart(Map<String, Float> categoryTotals) {

        // Variables
        PieChart pieChart = findViewById(R.id.pieChart);
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.bright_red_orange));
        colors.add(ContextCompat.getColor(this, R.color.neon_green));
        colors.add(ContextCompat.getColor(this, R.color.deep_blue));
        colors.add(ContextCompat.getColor(this, R.color.hot_pink));
        colors.add(ContextCompat.getColor(this, R.color.electric_yellow));
        colors.add(ContextCompat.getColor(this, R.color.vibrant_purple));
        colors.add(ContextCompat.getColor(this, R.color.aqua_cyan));

        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey())); // Expense amount and category name
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.charcoal_gray));
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);
        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh chart
    }
    public static class PercentFormatter extends ValueFormatter {

        public PercentFormatter(PieChart pieChart) {
        }

        @Override
        public String getFormattedValue(float value) {
            return String.format("%.1f%%", value); // Formats value as "XX.X%"
        }
    }

}