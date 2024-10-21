package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

        weekViewContainer = findViewById(R.id.weekMealContainer);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> goBackToMain());

        loadWeeklyMeals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeeklyMeals();
    }

    private void loadWeeklyMeals() {
        weekViewContainer.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String dbFormattedDate = dbDateFormat.format(calendar.getTime());
            String displayFormattedDate = displayDateFormat.format(calendar.getTime());

            View dateContainer = LayoutInflater.from(this).inflate(R.layout.date_container, weekViewContainer, false);
            TextView mealDateView = dateContainer.findViewById(R.id.mealDate);
            LinearLayout mealsForTheDayContainer = dateContainer.findViewById(R.id.mealsForTheDayContainer);

            mealDateView.setText(getUnderlinedText(displayFormattedDate));

            displayMealForSlot(dbFormattedDate, "Breakfast", mealsForTheDayContainer);
            displayMealForSlot(dbFormattedDate, "Lunch", mealsForTheDayContainer);
            displayMealForSlot(dbFormattedDate, "Dinner", mealsForTheDayContainer);

            weekViewContainer.addView(dateContainer);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private Spanned getUnderlinedText(String text) {
        return Html.fromHtml("<u>" + text + "</u>", Html.FROM_HTML_MODE_LEGACY);
    }

    private void displayMealForSlot(String date, String mealType, LinearLayout parent) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            List<Meal> meals = db.getMealsByDateAndType(date, mealType);

            View mealItemView = LayoutInflater.from(this).inflate(R.layout.meal_item_layout, parent, false);
            TextView mealNameView = mealItemView.findViewById(R.id.mealName);
            View mealIcon = mealItemView.findViewById(R.id.mealIcon);

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
    }

    private void showDeleteDialog(Meal meal) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_meal_title)
                .setMessage(getString(R.string.delete_meal_message, meal.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    try (DatabaseHelper db = new DatabaseHelper(this)) {
                        db.deleteMeal(meal.getId());
                        loadWeeklyMeals();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void goBackToMain() {
        Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
