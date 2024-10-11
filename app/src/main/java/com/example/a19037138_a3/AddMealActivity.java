package com.example.a19037138_a3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class AddMealActivity extends AppCompatActivity {

    private LinearLayout ingredientContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        ingredientContainer = findViewById(R.id.ingredientContainer);
        setupMealTypeSpinner();
    }

    // Back button handler to navigate to MainActivity
    public void goBackToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }

    public void addIngredient(View view) {
        View ingredientRow = getLayoutInflater().inflate(R.layout.ingredient_row, ingredientContainer, false); // Inflate within container
        ingredientContainer.addView(ingredientRow);
    }

    public void saveMeal(View view) {
        EditText mealName = findViewById(R.id.mealName);
        Spinner mealType = findViewById(R.id.mealTypeSpinner);
        DatePicker datePicker = findViewById(R.id.datePicker);

        if (mealName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Meal name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format the date to yyyy-MM-dd
        String mealDate = String.format(Locale.getDefault(), "%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());

        // Collect all ingredients in a list
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < ingredientContainer.getChildCount(); i++) {
            View ingredientRow = ingredientContainer.getChildAt(i);
            EditText ingredientName = ingredientRow.findViewById(R.id.ingredientName);
            EditText ingredientQuantity = ingredientRow.findViewById(R.id.ingredientQuantity);
            Spinner ingredientCategory = ingredientRow.findViewById(R.id.ingredientCategory);

            if (!ingredientName.getText().toString().isEmpty()) {
                ingredients.add(new Ingredient(ingredientName.getText().toString(), ingredientQuantity.getText().toString(), ingredientCategory.getSelectedItem().toString()));
            }
        }

        // Save the meal and ingredients to the database
        DatabaseHelper db = new DatabaseHelper(this);
        db.addMeal(mealName.getText().toString(), mealType.getSelectedItem().toString(), mealDate, ingredients);

        Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupMealTypeSpinner() {
        Spinner spinner = findViewById(R.id.mealTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.meal_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
