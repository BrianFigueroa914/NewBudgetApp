package com.example.newbudgetapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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

        // Variables
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore budgetData = FirebaseFirestore.getInstance();
        PieChart pieChart = findViewById(R.id.pieChart);


        if (user != null) {
            userID = user.getUid();
            fetchFilteredExpenses(); // Fetch and display existing data
        }
        else
            finish(); // Redirect to login

    }

    private void fetchFilteredExpenses() {
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

                    // Use the categoryTotals map as needed (e.g., update UI or generate Pie Chart)
                    generatePieChart(categoryTotals);
                }
            }
        });
    }

    private void generatePieChart(Map<String, Float> categoryTotals) {
        PieChart pieChart = findViewById(R.id.pieChart);
        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey())); // Expense amount and category name
        }

        PieDataSet dataSet = new PieDataSet(entries, "- Expense Breakdown");

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.animateY(1000);

        pieChart.invalidate(); // Refresh chart
    }


}