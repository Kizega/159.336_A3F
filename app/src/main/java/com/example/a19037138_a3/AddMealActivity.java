package com.example.a19037138_a3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for adding a new meal.
 * It allows users to input the meal's name, type, date, and ingredients.
 */
public class AddMealActivity extends AppCompatActivity {

    // UI components
    private EditText mealNameEditText;
    private Spinner mealTypeSpinner;
    private TextView selectedDateTextView;
    private Button addMealButton;
    private Button addIngredientButton;
    private LinearLayout ingredientContainer;

    // Stores the selected date
    private final Calendar selectedDateCalendar = Calendar.getInstance();

    // List to store ingredients added to the meal
    private final ArrayList<Ingredient> ingredientsList = new ArrayList<>();

    // Database helper instance
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Initialize the database and UI components
        db = DatabaseHelper.getInstance(this);
        initializeUI();

        // Set listeners for various UI elements
        setBackButtonListener();
        selectedDateTextView.setOnClickListener(v -> showDatePickerDialog());
        addIngredientButton.setOnClickListener(v -> {
            if (validateLastIngredientField()) {
                addIngredientField();
            } else {
                Toast.makeText(this, "Please fill in the last ingredient first", Toast.LENGTH_SHORT).show();
            }
        });
        addMealButton.setOnClickListener(v -> addMeal());
    }

    // Initialize all UI components
    private void initializeUI() {
        mealNameEditText = findViewById(R.id.meal_name);
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        selectedDateTextView = findViewById(R.id.selected_date);
        addMealButton = findViewById(R.id.add_meal_button);
        addIngredientButton = findViewById(R.id.add_ingredient_button);
        ingredientContainer = findViewById(R.id.ingredient_container);
    }

    // Set back button listener to return to MainActivity
    private void setBackButtonListener() {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddMealActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Add a new meal to the database
    private void addMeal() {
        String mealName = mealNameEditText.getText().toString().trim();
        String mealType = mealTypeSpinner.getSelectedItem().toString();

        if (selectedDateTextView.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedDateCalendar.getTime());

        if (mealName.isEmpty() || mealType.equals("Select Meal Time")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect ingredients from UI
        ingredientsList.clear();
        for (int i = 0; i < ingredientContainer.getChildCount(); i++) {
            LinearLayout ingredientLayout = (LinearLayout) ingredientContainer.getChildAt(i);
            LinearLayout firstRow = (LinearLayout) ingredientLayout.getChildAt(0);
            EditText nameField = (EditText) firstRow.getChildAt(0);
            EditText quantityField = (EditText) firstRow.getChildAt(1);

            String name = nameField.getText().toString().trim();
            int quantity = quantityField.getText().toString().isEmpty() ? 1 : Integer.parseInt(quantityField.getText().toString());

            LinearLayout secondRow = (LinearLayout) ingredientLayout.getChildAt(1);
            Spinner categorySpinner = (Spinner) secondRow.getChildAt(0);
            String category = categorySpinner.getSelectedItem().toString();

            if (!name.isEmpty()) {
                ingredientsList.add(new Ingredient(0, name, category, quantity));
            } else {
                Toast.makeText(this, "Skipping ingredient with empty name", Toast.LENGTH_SHORT).show();
            }
        }

        if (ingredientsList.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        long mealId = db.addMeal(mealName, mealType, formattedDate, ingredientsList);

        if (mealId != -1) {
            Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error adding meal", Toast.LENGTH_SHORT).show();
        }
    }

    // Add a new ingredient field dynamically
    private void addIngredientField() {
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout firstRow = createIngredientRow();
        LinearLayout secondRow = createCategoryRow(ingredientLayout);

        ingredientLayout.addView(firstRow);
        ingredientLayout.addView(secondRow);

        ingredientContainer.addView(ingredientLayout);
        ingredientsList.add(new Ingredient(0, "", "Other", 1));
    }

    // Create the first row for ingredient input
    private LinearLayout createIngredientRow() {
        LinearLayout firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);

        EditText ingredientNameEditText = new EditText(this);
        ingredientNameEditText.setHint("Ingredient Name");
        ingredientNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        ingredientNameEditText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        EditText ingredientQuantityEditText = new EditText(this);
        ingredientQuantityEditText.setHint("Quantity");
        ingredientQuantityEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        ingredientQuantityEditText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        firstRow.addView(ingredientNameEditText);
        firstRow.addView(ingredientQuantityEditText);

        return firstRow;
    }

    // Create the second row with a category spinner and delete button
    private LinearLayout createCategoryRow(LinearLayout ingredientLayout) {
        LinearLayout secondRow = new LinearLayout(this);
        secondRow.setOrientation(LinearLayout.HORIZONTAL);

        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_no_all, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(android.R.drawable.ic_delete);
        deleteButton.setOnClickListener(v -> removeIngredient(ingredientLayout));

        secondRow.addView(categorySpinner);
        secondRow.addView(deleteButton);

        return secondRow;
    }

    // Remove an ingredient field
    private void removeIngredient(LinearLayout ingredientLayout) {
        int index = ingredientContainer.indexOfChild(ingredientLayout);
        ingredientContainer.removeView(ingredientLayout);
        if (index != -1) ingredientsList.remove(index);
        Toast.makeText(this, "Ingredient removed", Toast.LENGTH_SHORT).show();
    }

    // Validate the last ingredient field
    private boolean validateLastIngredientField() {
        int childCount = ingredientContainer.getChildCount();
        if (childCount == 0) return true;

        LinearLayout lastIngredientLayout = (LinearLayout) ingredientContainer.getChildAt(childCount - 1);
        LinearLayout firstRow = (LinearLayout) lastIngredientLayout.getChildAt(0);
        EditText nameField = (EditText) firstRow.getChildAt(0);
        EditText quantityField = (EditText) firstRow.getChildAt(1);

        return !nameField.getText().toString().isEmpty() && !quantityField.getText().toString().isEmpty();
    }

    // Show the date picker dialog
    private void showDatePickerDialog() {
        int year = selectedDateCalendar.get(Calendar.YEAR);
        int month = selectedDateCalendar.get(Calendar.MONTH);
        int day = selectedDateCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDateCalendar.set(year1, month1, dayOfMonth);
            updateSelectedDateText();
        }, year, month, day);

        datePickerDialog.show();
    }

    // Update the date text view
    private void updateSelectedDateText() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        selectedDateTextView.setText(displayFormat.format(selectedDateCalendar.getTime()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseHelper.closeDatabase();
    }
}
