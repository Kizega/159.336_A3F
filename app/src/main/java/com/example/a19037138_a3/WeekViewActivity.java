package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class WeekViewActivity extends AppCompatActivity {

    private LinearLayout weekViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

        weekViewContainer = findViewById(R.id.weekMealContainer);
        loadWeeklyMeals();

        // Back button functionality
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: if you want to remove WeekViewActivity from the back stack
        });
    }

    private void loadWeeklyMeals() {
        DatabaseHelper db = new DatabaseHelper(this);
        List<Meal> meals = db.getWeeklyMeals();

        weekViewContainer.removeAllViews(); // Clear existing views

        if (meals.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No meals for the next 7 days.");
            weekViewContainer.addView(emptyView);
        } else {
            for (Meal meal : meals) {
                TextView mealView = new TextView(this);
                mealView.setText(meal.getType() + " - " + meal.getName() + " (" + meal.getDate() + ")");
                mealView.setOnClickListener(v -> showDeleteDialog(meal));
                weekViewContainer.addView(mealView);
            }
        }
    }

    private void showDeleteDialog(Meal meal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Meal")
                .setMessage("Are you sure you want to delete this meal?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseHelper db = new DatabaseHelper(WeekViewActivity.this);
                    db.deleteMeal(meal.getId());
                    loadWeeklyMeals(); // Reload meals after deletion
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
