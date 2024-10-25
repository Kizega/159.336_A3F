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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity to display the weekly view of meals.
 * Loads meals for each day and displays them in their respective slots.
 */
public class WeekViewActivity extends AppCompatActivity {

    private static final String TAG = "WeekViewActivity"; // Log tag for debugging

    private LinearLayout weekViewContainer; // Layout container for the weekly view
    private DatabaseHelper db; // Singleton instance of the database helper

    // Cache to store meals by date and type for quick access
    private final Map<String, Map<String, List<Meal>>> mealsCache = new HashMap<>();

    /**
     * Initializes the activity, sets up the UI, and loads weekly meals.
     *
     * @param savedInstanceState The saved instance state (if any).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

        db = DatabaseHelper.getInstance(this); // Initialize the database helper

        initializeUI(); // Set up the UI components
        loadWeeklyMeals(); // Load the weekly meals on activity start
    }

    /**
     * Reloads the meals when the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadWeeklyMeals(); // Reload meals when returning to the activity
    }

    /**
     * Releases resources when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResources(); // Clear views and cache
    }

    /**
     * Initializes UI components and sets up listeners.
     */
    private void initializeUI() {
        weekViewContainer = findViewById(R.id.weekMealContainer);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> goBackToMain()); // Set back button listener
    }

    /**
     * Loads meals for the next 7 days and displays them in the week view.
     */
    private void loadWeeklyMeals() {
        weekViewContainer.removeAllViews(); // Clear previous views

        Calendar calendar = Calendar.getInstance(); // Start with today's date
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());

        // Loop through the next 7 days to display meals
        for (int i = 0; i < 7; i++) {
            String dbFormattedDate = dbDateFormat.format(calendar.getTime());
            String displayFormattedDate = displayDateFormat.format(calendar.getTime());

            Log.d(TAG, "Loading meals for date: " + dbFormattedDate);

            View dateContainer = LayoutInflater.from(this)
                    .inflate(R.layout.date_container, weekViewContainer, false);

            TextView mealDateView = dateContainer.findViewById(R.id.mealDate);
            LinearLayout mealsForTheDayContainer = dateContainer.findViewById(R.id.mealsForTheDayContainer);

            mealDateView.setText(getUnderlinedText(displayFormattedDate));

            // Display meals for each meal type slot
            displayMealsForSlot(dbFormattedDate, "Breakfast", mealsForTheDayContainer);
            displayMealsForSlot(dbFormattedDate, "Lunch", mealsForTheDayContainer);
            displayMealsForSlot(dbFormattedDate, "Dinner", mealsForTheDayContainer);

            weekViewContainer.addView(dateContainer); // Add the day's view to the container
            calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to the next day
        }
    }

    /**
     * Returns underlined text using HTML formatting.
     *
     * @param text The text to underline.
     * @return A Spanned object with underlined text.
     */
    private Spanned getUnderlinedText(String text) {
        return Html.fromHtml("<u>" + text + "</u>", Html.FROM_HTML_MODE_LEGACY);
    }

    /**
     * Displays meals for a specific meal slot (e.g., Breakfast) in the parent layout.
     *
     * @param date   The date of the meals.
     * @param mealType The type of the meal (Breakfast, Lunch, Dinner).
     * @param parent  The parent layout to add the meal views to.
     */
    private void displayMealsForSlot(String date, String mealType, LinearLayout parent) {
        List<Meal> meals = getCachedMeals(date, mealType);

        View mealItemView = LayoutInflater.from(this)
                .inflate(R.layout.meal_item_layout, parent, false);

        TextView mealNameView = mealItemView.findViewById(R.id.mealName);
        ImageView mealIcon = mealItemView.findViewById(R.id.mealIcon);

        setMealIcon(mealType, mealIcon); // Set the appropriate icon for the meal type

        if (!meals.isEmpty()) {
            StringBuilder mealNamesBuilder = new StringBuilder();
            for (Meal meal : meals) {
                mealNamesBuilder.append(meal.getName()).append("\n");
            }
            mealNameView.setText(mealNamesBuilder.toString().trim());

            // Set a click listener to show the delete dialog for the first meal
            mealItemView.setOnClickListener(v -> showDeleteDialog(meals.get(0)));
        } else {
            // Display a message if no meals are available for the slot
            mealNameView.setText(getString(R.string.no_meal, mealType.toLowerCase()));
        }

        parent.addView(mealItemView); // Add the meal view to the parent layout
    }

    /**
     * Retrieves cached meals for a specific date and meal type.
     * If not cached, it fetches from the database.
     *
     * @param date     The date of the meals.
     * @param mealType The type of the meal (Breakfast, Lunch, Dinner).
     * @return A list of meals for the specified date and type.
     */
    private List<Meal> getCachedMeals(String date, String mealType) {
        Map<String, List<Meal>> mealsForDate = mealsCache.get(date);

        if (mealsForDate != null && mealsForDate.containsKey(mealType)) {
            return mealsForDate.get(mealType);
        }

        List<Meal> meals = db.getMealsByDateAndType(date, mealType);

        if (mealsForDate == null) {
            mealsForDate = new HashMap<>();
            mealsCache.put(date, mealsForDate);
        }
        mealsForDate.put(mealType, meals);
        return meals;
    }

    /**
     * Sets the appropriate icon for a meal type.
     *
     * @param mealType The type of the meal (Breakfast, Lunch, Dinner).
     * @param mealIcon The ImageView to set the icon on.
     */
    private void setMealIcon(String mealType, ImageView mealIcon) {
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
                mealIcon.setImageResource(android.R.drawable.ic_menu_help);
                break;
        }
    }

    /**
     * Displays a dialog to confirm the deletion of a meal.
     *
     * @param meal The meal to delete.
     */
    private void showDeleteDialog(Meal meal) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_meal_title)
                .setMessage(getString(R.string.delete_meal_message, meal.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteMeal(meal.getId());
                    mealsCache.clear(); // Clear the cache after deletion
                    loadWeeklyMeals(); // Reload the meals
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Navigates back to the MainActivity.
     */
    private void goBackToMain() {
        Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Prevent back navigation
    }

    /**
     * Releases resources by clearing the cache and views.
     */
    private void releaseResources() {
        mealsCache.clear();
        if (weekViewContainer != null) {
            weekViewContainer.removeAllViews(); // Clear the layout
        }
    }
}
