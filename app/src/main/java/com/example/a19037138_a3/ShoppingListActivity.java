package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    private LinearLayout shoppingListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        shoppingListContainer = findViewById(R.id.shoppingListContainer);
        refreshShoppingList();  // Load the shopping list when the activity starts

        // Back button functionality
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShoppingListActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: if you want to remove ShoppingListActivity from the back stack
        });
    }

    private void refreshShoppingList() {
        DatabaseHelper db = new DatabaseHelper(this);
        Map<String, List<String>> categorizedIngredients = db.getShoppingListByCategory();

        shoppingListContainer.removeAllViews(); // Clear existing views

        for (String category : categorizedIngredients.keySet()) {
            TextView categoryView = new TextView(this);
            categoryView.setText(category);
            shoppingListContainer.addView(categoryView);

            for (String ingredient : categorizedIngredients.get(category)) {
                CheckBox ingredientCheckBox = new CheckBox(this);
                ingredientCheckBox.setText(ingredient);
                shoppingListContainer.addView(ingredientCheckBox);
            }
        }
    }
}
