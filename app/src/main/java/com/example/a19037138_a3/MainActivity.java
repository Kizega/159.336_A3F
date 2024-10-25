package com.example.a19037138_a3;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main activity that serves as the entry point of the app.
 * Handles navigation to other activities and performs initial setup.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Locks orientation, deletes old meals, and sets up buttons with click listeners.
     *
     * @param savedInstanceState If the activity is being reinitialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @SuppressWarnings("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isInMultiWindowMode() && !isInPictureInPictureMode()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_main);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.deleteOldMeals();

        Button addMealButton = findViewById(R.id.button_add_meal);
        Button weekButton = findViewById(R.id.button_week);
        Button shoppingListButton = findViewById(R.id.button_shopping_list);

        addMealButton.setOnClickListener(v -> openAddMeal());
        weekButton.setOnClickListener(v -> openWeekView());
        shoppingListButton.setOnClickListener(v -> openShoppingList());
    }

    /**
     * Opens the AddMealActivity to allow users to add a new meal.
     */
    private void openAddMeal() {
        startActivity(new Intent(this, AddMealActivity.class));
    }

    /**
     * Opens the WeekViewActivity to display the weekly meal plan.
     */
    private void openWeekView() {
        startActivity(new Intent(this, WeekViewActivity.class));
    }

    /**
     * Opens the ShoppingListActivity to display the shopping list.
     */
    private void openShoppingList() {
        startActivity(new Intent(this, ShoppingListActivity.class));
    }

    /**
     * Called when the activity is destroyed.
     * Closes the database to release resources.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseHelper.closeDatabase();
    }
}
