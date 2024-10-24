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

public class WeekViewActivity extends AppCompatActivity {

    private LinearLayout weekViewContainer;
    private static final String TAG = "WeekViewActivity";
    private DatabaseHelper db; // Singleton DatabaseHelper instance
    private final Map<String, Map<String, List<Meal>>> mealsCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

        // Initialize DatabaseHelper instance
        db = DatabaseHelper.getInstance(this);

        // Initialize UI components
        weekViewContainer = findViewById(R.id.weekMealContainer);
        ImageButton backButton = findViewById(R.id.back_button);

        // Set back button click listener
        backButton.setOnClickListener(v -> goBackToMain());

        // Load meals on activity start
        loadWeeklyMeals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeeklyMeals(); // Reload meals when the activity resumes
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResources(); // Clear views and cache
    }

    private void loadWeeklyMeals() {
        weekViewContainer.removeAllViews(); // Clear previous views

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());

        // Loop through the next 7 days
        for (int i = 0; i < 7; i++) {
            String dbFormattedDate = dbDateFormat.format(calendar.getTime());
            String displayFormattedDate = displayDateFormat.format(calendar.getTime());

            Log.d(TAG, "Loading meals for date: " + dbFormattedDate);

            View dateContainer = LayoutInflater.from(this)
                    .inflate(R.layout.date_container, weekViewContainer, false);

            TextView mealDateView = dateContainer.findViewById(R.id.mealDate);
            LinearLayout mealsForTheDayContainer = dateContainer.findViewById(R.id.mealsForTheDayContainer);

            mealDateView.setText(getUnderlinedText(displayFormattedDate));

            // Display meals for each slot
            displayMealsForSlot(dbFormattedDate, "Breakfast", mealsForTheDayContainer);
            displayMealsForSlot(dbFormattedDate, "Lunch", mealsForTheDayContainer);
            displayMealsForSlot(dbFormattedDate, "Dinner", mealsForTheDayContainer);

            weekViewContainer.addView(dateContainer);
            calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to the next day
        }
    }

    private Spanned getUnderlinedText(String text) {
        return Html.fromHtml("<u>" + text + "</u>", Html.FROM_HTML_MODE_LEGACY);
    }

    private void displayMealsForSlot(String date, String mealType, LinearLayout parent) {
        List<Meal> meals = getCachedMeals(date, mealType);

        View mealItemView = LayoutInflater.from(this)
                .inflate(R.layout.meal_item_layout, parent, false);

        TextView mealNameView = mealItemView.findViewById(R.id.mealName);
        ImageView mealIcon = mealItemView.findViewById(R.id.mealIcon);

        setMealIcon(mealType, mealIcon);

        if (!meals.isEmpty()) {
            StringBuilder mealNamesBuilder = new StringBuilder();
            for (Meal meal : meals) {
                mealNamesBuilder.append(meal.getName()).append("\n");
            }
            mealNameView.setText(mealNamesBuilder.toString().trim());

            mealItemView.setOnClickListener(v -> showDeleteDialog(meals.get(0)));
        } else {
            mealNameView.setText(getString(R.string.no_meal, mealType.toLowerCase()));
        }

        parent.addView(mealItemView);
    }

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

    private void showDeleteDialog(Meal meal) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_meal_title)
                .setMessage(getString(R.string.delete_meal_message, meal.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteMeal(meal.getId());
                    mealsCache.clear(); // Invalidate cache
                    loadWeeklyMeals(); // Reload meals
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void goBackToMain() {
        Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Prevent back navigation
    }

    private void releaseResources() {
        mealsCache.clear();
        if (weekViewContainer != null) {
            weekViewContainer.removeAllViews();
        }
    }
}
