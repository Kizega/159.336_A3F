package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeekViewActivity extends AppCompatActivity {

    private LinearLayout weekViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

        weekViewContainer = findViewById(R.id.weekMealContainer);
        loadWeeklyMeals();

        // Set up the back button functionality
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Close current activity
        });
    }


    private void loadWeeklyMeals() {
        DatabaseHelper db = new DatabaseHelper(this);

        weekViewContainer.removeAllViews(); // Clear existing views

        // Get the next 7 days
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // This matches the database format
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE d'th' MMMM", Locale.getDefault()); // Display format for UI

        for (int i = 0; i < 7; i++) {
            String dbFormattedDate = dbDateFormat.format(calendar.getTime());
            String displayFormattedDate = displayDateFormat.format(calendar.getTime());

            // Inflate a date header
            TextView dateView = new TextView(this);
            dateView.setText(displayFormattedDate);
            dateView.setTextSize(18);
            dateView.setTypeface(null, android.graphics.Typeface.BOLD);
            dateView.setPadding(0, 40, 0, 10);
            weekViewContainer.addView(dateView);

            // Check and display meals for breakfast, lunch, and dinner
            displayMealForSlot(db, dbFormattedDate, "Breakfast");
            displayMealForSlot(db, dbFormattedDate, "Lunch");
            displayMealForSlot(db, dbFormattedDate, "Dinner");

            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void displayMealForSlot(DatabaseHelper db, String date, String mealType) {
        Meal meal = db.getMealByDateAndType(date, mealType);

        View mealItemView = LayoutInflater.from(this).inflate(R.layout.meal_item_layout, weekViewContainer, false);
        TextView mealNameView = mealItemView.findViewById(R.id.mealName);
        View mealIcon = mealItemView.findViewById(R.id.mealIcon);

        if (meal != null) {
            mealNameView.setText(meal.getName());

            switch (mealType) {
                case "Breakfast":
                    mealIcon.setBackgroundResource(R.drawable.ic_breakfast_icon);
                    break;
                case "Lunch":
                    mealIcon.setBackgroundResource(R.drawable.ic_lunch_icon);
                    break;
                case "Dinner":
                    mealIcon.setBackgroundResource(R.drawable.ic_dinner_icon);
                    break;
            }

            // Add click listener for deletion
            mealItemView.setOnClickListener(v -> showDeleteDialog(meal, db));

        } else {
            mealNameView.setText("No " + mealType.toLowerCase() + " allocated");
            switch (mealType) {
                case "Breakfast":
                    mealIcon.setBackgroundResource(R.drawable.ic_breakfast_icon);
                    break;
                case "Lunch":
                    mealIcon.setBackgroundResource(R.drawable.ic_lunch_icon);
                    break;
                case "Dinner":
                    mealIcon.setBackgroundResource(R.drawable.ic_dinner_icon);
                    break;
            }
        }

        weekViewContainer.addView(mealItemView);
    }

    private void showDeleteDialog(Meal meal, DatabaseHelper db) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Meal")
                .setMessage("Are you sure you want to delete " + meal.getName() + "? This will also remove associated ingredients.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteMeal(meal.getId());
                    loadWeeklyMeals(); // Refresh the week view after deletion
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // Method to handle back button click
    public void goBackToMain(View view) {
        // This will finish the current activity and go back to the previous activity (MainActivity)
        Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish this activity to prevent stacking activities
    }
}
