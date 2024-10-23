package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeekViewActivity extends AppCompatActivity {

    private LinearLayout weekViewContainer;
    private static final String TAG = "WeekViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

        // Initialize UI components
        weekViewContainer = findViewById(R.id.weekMealContainer);
        ImageButton backButton = findViewById(R.id.back_button);

        // Set back button click listener to return to the main screen
        backButton.setOnClickListener(v -> goBackToMain());

        // Load the meals for the week on activity start
        loadWeeklyMeals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeeklyMeals(); // Reload meals when the activity resumes
    }

    /**
     * Loads meals for the upcoming week and displays them.
     */
    private void loadWeeklyMeals() {
        weekViewContainer.removeAllViews(); // Clear previous views

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());

        // Loop through the next 7 days and load meals for each
        for (int i = 0; i < 7; i++) {
            String dbFormattedDate = dbDateFormat.format(calendar.getTime());
            String displayFormattedDate = displayDateFormat.format(calendar.getTime());

            Log.d(TAG, "Loading meals for date: " + dbFormattedDate);

            // Inflate the layout for each date
            View dateContainer = LayoutInflater.from(this)
                    .inflate(R.layout.date_container, weekViewContainer, false);

            TextView mealDateView = dateContainer.findViewById(R.id.mealDate);
            LinearLayout mealsForTheDayContainer = dateContainer.findViewById(R.id.mealsForTheDayContainer);

            // Set the date with underlined text
            mealDateView.setText(getUnderlinedText(displayFormattedDate));

            // Display meals for breakfast, lunch, and dinner
            displayMealForSlot(dbFormattedDate, "Breakfast", mealsForTheDayContainer);
            displayMealForSlot(dbFormattedDate, "Lunch", mealsForTheDayContainer);
            displayMealForSlot(dbFormattedDate, "Dinner", mealsForTheDayContainer);

            weekViewContainer.addView(dateContainer); // Add to the main container
            calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to the next day
        }
    }

    /**
     * Returns underlined text for display.
     */
    private Spanned getUnderlinedText(String text) {
        return Html.fromHtml("<u>" + text + "</u>", Html.FROM_HTML_MODE_LEGACY);
    }

    /**
     * Displays meals for a given date and slot (e.g., breakfast, lunch, dinner).
     */
    private void displayMealForSlot(String date, String mealType, LinearLayout parent) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            List<Meal> meals = db.getMealsByDateAndType(date, mealType);

            // Inflate meal item layout
            View mealItemView = LayoutInflater.from(this)
                    .inflate(R.layout.meal_item_layout, parent, false);

            TextView mealNameView = mealItemView.findViewById(R.id.mealName);
            ImageView mealIcon = mealItemView.findViewById(R.id.mealIcon);

            // Set the appropriate icon based on the meal type
            switch (mealType) {
                case "Breakfast":
                    mealIcon.setImageResource(R.drawable.ic_breakfast_icon);
                    break;
                case "Lunch":
                    mealIcon.setImageResource(R.drawable.ic_lunch_icon);
                    break;
                case "Dinner":
                    mealIcon.setImageResource(R.drawable.ic_dinner_icon);
                    break;
                default:
                    mealIcon.setImageResource(android.R.drawable.ic_menu_help); // Default icon
                    break;
            }

            // Display meal names or a no-meal message
            if (!meals.isEmpty()) {
                StringBuilder mealNamesBuilder = new StringBuilder();
                for (Meal meal : meals) {
                    mealNamesBuilder.append(meal.getName()).append("\n");
                }
                mealNameView.setText(mealNamesBuilder.toString().trim());

                // Set up click listener to show the delete dialog
                mealItemView.setOnClickListener(v -> showDeleteDialog(meals.get(0)));
            } else {
                mealNameView.setText(getString(R.string.no_meal, mealType.toLowerCase()));
            }

            parent.addView(mealItemView); // Add meal item to the parent container
        }
    }

    /**
     * Shows a confirmation dialog for deleting a meal.
     */
    private void showDeleteDialog(Meal meal) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_meal_title)
                .setMessage(getString(R.string.delete_meal_message, meal.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    try (DatabaseHelper db = new DatabaseHelper(this)) {
                        db.deleteMeal(meal.getId()); // Delete the meal from the database
                        loadWeeklyMeals(); // Reload the meals to reflect the changes
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Navigates back to the main activity.
     */
    private void goBackToMain() {
        Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity to prevent back navigation
    }
}
