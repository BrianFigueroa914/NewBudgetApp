package com.example.newbudgetapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private LineChart lineChart;
    private List<Entry> incomeEntries = new ArrayList<>();
    private List<String> dayLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Step 1: Get references to all views
        lineChart = findViewById(R.id.lineChart);
        EditText incomeInput = findViewById(R.id.incomeInput);
        Button addIncomeBtn = findViewById(R.id.addIncomeBtn);
        TextView monthLabel = findViewById(R.id.monthLabel);

        // Step 2: Set the dynamic month label
        String currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
        monthLabel.setText(currentMonth + " Income");

        // Step 3: Initial chart setup
        updateChart();

        // Step 4: Handle income input on button click
        addIncomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String incomeText = incomeInput.getText().toString().trim();
                if (!incomeText.isEmpty()) {
                    float income = Float.parseFloat(incomeText);

                    // Get current day of month and store it as x-axis label
                    String currentDay = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());
                    incomeEntries.add(new Entry(incomeEntries.size(), income));
                    dayLabels.add(currentDay);

                    updateChart();
                    incomeInput.setText(""); // Clear input after adding
                }
            }
        });
    }

    private void updateChart() {
        LineDataSet dataSet = new LineDataSet(incomeEntries, "Income");
        dataSet.setColor(Color.GREEN);
        dataSet.setCircleColor(Color.GREEN);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Add/redraw budget line
        float budget = 1100f;
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
        lineChart.invalidate(); // refresh chart
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
}
