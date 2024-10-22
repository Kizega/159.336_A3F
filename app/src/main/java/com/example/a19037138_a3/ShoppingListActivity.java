package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    private static final String TAG = "ShoppingListActivity";
    private LinearLayout shoppingListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Initialize the UI elements
        shoppingListContainer = findViewById(R.id.shoppingListContainer);
        ImageButton backButton = findViewById(R.id.back_button);

        // Log that the activity has been created
        Log.d(TAG, "onCreate: ShoppingListActivity created");

        // Set up the back button with logging to debug click events
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "onClick: Back button clicked");
                goBackToMain();
            });
        } else {
            Log.e(TAG, "onCreate: Back button not found");
        }

        // Load the shopping list
        loadShoppingList();
    }

    private void loadShoppingList() {
        // Ensure the container is properly initialized before use
        if (shoppingListContainer == null) {
            Log.e(TAG, "loadShoppingList: shoppingListContainer is null");
            return;
        }

        shoppingListContainer.removeAllViews(); // Clear any existing views

        DatabaseHelper db = new DatabaseHelper(this);
        Map<String, List<Ingredient>> categorizedIngredients = db.getCategorizedIngredients();

        for (Map.Entry<String, List<Ingredient>> entry : categorizedIngredients.entrySet()) {
            String category = entry.getKey();
            List<Ingredient> ingredients = entry.getValue();

            // Inflate category header
            View categoryHeader = getLayoutInflater().inflate(R.layout.category_header, shoppingListContainer, false);
            TextView categoryTitle = categoryHeader.findViewById(R.id.categoryTitle);
            ImageView arrowIcon = categoryHeader.findViewById(R.id.arrowIcon);
            LinearLayout ingredientsContainer = new LinearLayout(this);
            ingredientsContainer.setOrientation(LinearLayout.VERTICAL);
            ingredientsContainer.setVisibility(View.GONE);

            if (categoryTitle != null) {
                categoryTitle.setText(category);
            } else {
                Log.e(TAG, "loadShoppingList: categoryTitle not found");
            }

            if (arrowIcon != null) {
                arrowIcon.setImageResource(android.R.drawable.arrow_down_float);
            }

            // Toggle visibility on click
            categoryHeader.setOnClickListener(v -> {
                if (ingredientsContainer.getVisibility() == View.VISIBLE) {
                    ingredientsContainer.setVisibility(View.GONE);
                    if (arrowIcon != null) {
                        arrowIcon.setRotation(0);
                    }
                } else {
                    ingredientsContainer.setVisibility(View.VISIBLE);
                    if (arrowIcon != null) {
                        arrowIcon.setRotation(180);
                    }
                }
            });

            // Add ingredients to container
            for (Ingredient ingredient : ingredients) {
                View ingredientRow = getLayoutInflater().inflate(R.layout.ingredient_row, ingredientsContainer, false);
                CheckBox ingredientCheckbox = ingredientRow.findViewById(R.id.ingredientCheckbox);
                TextView ingredientName = ingredientRow.findViewById(R.id.ingredientName);
                TextView ingredientQuantity = ingredientRow.findViewById(R.id.ingredientQuantity);

                if (ingredientName != null) {
                    ingredientName.setText(ingredient.getName());
                }
                if (ingredientQuantity != null) {
                    ingredientQuantity.setText("Qty: " + ingredient.getQuantity());
                }

                ingredientsContainer.addView(ingredientRow);
            }

            // Add header and ingredients container to the main container
            shoppingListContainer.addView(categoryHeader);
            shoppingListContainer.addView(ingredientsContainer);
        }
    }

    private void goBackToMain() {
        Log.d(TAG, "goBackToMain: Navigating back to MainActivity");
        Intent intent = new Intent(ShoppingListActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Ensure this activity finishes
    }
}
