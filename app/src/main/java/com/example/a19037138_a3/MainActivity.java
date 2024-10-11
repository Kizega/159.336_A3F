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

        // Setup buttons with click listeners
        Button addMealButton = findViewById(R.id.button_add_meal);
        Button weekButton = findViewById(R.id.button_week);
        Button shoppingListButton = findViewById(R.id.button_shopping_list);

        // Set up click listeners for each button
        addMealButton.setOnClickListener(v -> openAddMeal());
        weekButton.setOnClickListener(v -> openWeekView());
        shoppingListButton.setOnClickListener(v -> openShoppingList());
    }

    // Opens the AddMealActivity
    private void openAddMeal() {
        Intent intent = new Intent(this, AddMealActivity.class);
        startActivity(intent);
    }

    // Opens the WeekViewActivity
    private void openWeekView() {
        Intent intent = new Intent(this, WeekViewActivity.class);
        startActivity(intent);
    }

    // Opens the ShoppingListActivity
    private void openShoppingList() {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }
}
