package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Delete old meals and ingredients on app launch
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            db.deleteOldMeals();  // Clean up outdated data
        }

        // Set up buttons with click listeners
        Button addMealButton = findViewById(R.id.button_add_meal);
        Button weekButton = findViewById(R.id.button_week);
        Button shoppingListButton = findViewById(R.id.button_shopping_list);

        // Open activities when buttons are clicked
        addMealButton.setOnClickListener(v -> openAddMeal());
        weekButton.setOnClickListener(v -> openWeekView());
        shoppingListButton.setOnClickListener(v -> openShoppingList());
    }

    // Opens AddMealActivity
    private void openAddMeal() {
        Intent intent = new Intent(this, AddMealActivity.class);
        startActivity(intent);
    }

    // Opens WeekViewActivity
    private void openWeekView() {
        Intent intent = new Intent(this, WeekViewActivity.class);
        startActivity(intent);
    }

    // Opens ShoppingListActivity
    private void openShoppingList() {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }
}
