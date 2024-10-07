package com.example.basakhuji;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChartActivity extends AppCompatActivity {

    private BarChart barChart;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

    private Spinner monthSpinner, yearSpinner;
    ImageView backButton;
    private Button filterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize chart
        barChart = findViewById(R.id.lineChart);

        // Initialize spinners and button
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        filterButton = findViewById(R.id.filterButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set up the spinners with data
        setupMonthSpinner();
        setupYearSpinner();


        filterButton.setOnClickListener(v -> {
            String selectedMonth = monthSpinner.getSelectedItem().toString();
            int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
            fetchAndDisplayDataForMonthYear(selectedMonth, selectedYear);
        });

        Calendar calendar = Calendar.getInstance();
        String currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        int currentYear = calendar.get(Calendar.YEAR);
        fetchAndDisplayDataForMonthYear(currentMonth, currentYear);
    }

    private void setupMonthSpinner() {
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        monthSpinner.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH); // 0-based index
        monthSpinner.setSelection(currentMonthIndex);
    }

    private void setupYearSpinner() {
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < 10; i++) {
            years.add(String.valueOf(currentYear - i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);
        yearSpinner.setAdapter(adapter);

        yearSpinner.setSelection(0); // 0 means the current year
    }

    private void fetchAndDisplayDataForMonthYear(String month, int year) {
        db.collection("properties")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Store the count of listings for each week
                        Map<Integer, Integer> weeklyListingsCount = new HashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dateOfListing = document.getString("addedDate");
                            if (dateOfListing != null) {
                                try {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(dateFormat.parse(dateOfListing));

                                    int listingMonth = calendar.get(Calendar.MONTH);  // 0-based month
                                    int listingYear = calendar.get(Calendar.YEAR);
                                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); // Get day of month

                                    if (listingMonth == getMonthFromName(month) && listingYear == year) {
                                        // Manually calculate the week of the month
                                        int weekOfMonth = getWeekFromDay(dayOfMonth);

                                        // Increment the count for the appropriate week
                                        weeklyListingsCount.put(weekOfMonth, weeklyListingsCount.getOrDefault(weekOfMonth, 0) + 1);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    Log.e("ChartActivity", "Error parsing date: " + dateOfListing);
                                }
                            }
                        }
                        // Now populate the chart with the weekly data
                        populateChart(weeklyListingsCount);
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChartActivity.this, "Failed to fetch data.", Toast.LENGTH_LONG).show());
                    }
                });
    }

    private void populateChart(Map<Integer, Integer> weeklyListingsCount) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        for (int week = 1; week <= 4; week++) {
            int count = weeklyListingsCount.getOrDefault(week, 0);  // Default to 0 if no listings for this week
            barEntries.add(new BarEntry(week, count));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Weekly Listings");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData barData = new BarData(barDataSet);

        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getDescription().setText("Property Listings by Week");

        barChart.invalidate();
    }

    // Helper method to manually calculate the week based on the day of the month
    private int getWeekFromDay(int dayOfMonth) {
        if (dayOfMonth <= 7) {
            return 1;
        } else if (dayOfMonth <= 14) {
            return 2;
        } else if (dayOfMonth <= 21) {
            return 3;
        } else {
            return 4;
        }
    }

    // Helper method to convert month name to its corresponding index (0-based)
    private int getMonthFromName(String monthName) {
        try {
            SimpleDateFormat monthParse = new SimpleDateFormat("MMMM", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(monthParse.parse(monthName));
            return cal.get(Calendar.MONTH);  // 0-based index
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;  // Error
        }
    }
}
