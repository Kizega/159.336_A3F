package com.example.a19037138_a3;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main activity that serves as the entry point of the app.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock orientation to portrait if not in multi-window or picture-in-picture mode
        if (!isInMultiWindowMode() && !isInPictureInPictureMode()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_main);

        // Clean up old meals and ingredients on launch
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.deleteOldMeals();

        // Initialize buttons
        Button addMealButton = findViewById(R.id.button_add_meal);
        Button weekButton = findViewById(R.id.button_week);
        Button shoppingListButton = findViewById(R.id.button_shopping_list);

        // Set click listeners for buttons
        addMealButton.setOnClickListener(v -> openAddMeal());
        weekButton.setOnClickListener(v -> openWeekView());
        shoppingListButton.setOnClickListener(v -> openShoppingList());
    }

    // Opens the AddMealActivity
    private void openAddMeal() {
        startActivity(new Intent(this, AddMealActivity.class));
    }

    // Opens the WeekViewActivity
    private void openWeekView() {
        startActivity(new Intent(this, WeekViewActivity.class));
    }

    // Opens the ShoppingListActivity
    private void openShoppingList() {
        startActivity(new Intent(this, ShoppingListActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database to free resources
        DatabaseHelper.closeDatabase();
    }
}
