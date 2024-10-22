package com.example.a19037138_a3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {

    private EditText mealNameEditText;
    private Spinner mealTypeSpinner;
    private TextView selectedDateTextView;
    private Button addMealButton;
    private Button addIngredientButton;
    private LinearLayout ingredientContainer;

    private Calendar selectedDateCalendar = Calendar.getInstance();
    private ArrayList<Ingredient> ingredientsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Initialize UI elements
        mealNameEditText = findViewById(R.id.meal_name);
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        selectedDateTextView = findViewById(R.id.selected_date);
        addMealButton = findViewById(R.id.add_meal_button);
        addIngredientButton = findViewById(R.id.add_ingredient_button);
        ingredientContainer = findViewById(R.id.ingredient_container);
        ImageButton backButton = findViewById(R.id.back_button);

        // Set up back button to return to MainActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddMealActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Show date picker dialog on clicking the date field
        selectedDateTextView.setOnClickListener(v -> showDatePickerDialog());

        // Add new ingredient field dynamically when the button is clicked
        addIngredientButton.setOnClickListener(v -> {
            if (validateLastIngredientField()) {
                addIngredientField();
            } else {
                Toast.makeText(this, "Please fill in the last ingredient first", Toast.LENGTH_SHORT).show();
            }
        });

        // Add meal to the database on button click
        addMealButton.setOnClickListener(v -> {
            String mealName = mealNameEditText.getText().toString();
            String mealType = mealTypeSpinner.getSelectedItem().toString();

            // Format the selected date to 'yyyy-MM-dd' for database storage
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dbDateFormat.format(selectedDateCalendar.getTime());

            if (mealName.isEmpty() || formattedDate.isEmpty() || mealType.equals("Select Meal Time")) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(this);
            long mealId = db.addMeal(mealName, mealType, formattedDate, ingredientsList);

            if (mealId != -1) {
                Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error adding meal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Dynamically add an ingredient field
    private void addIngredientField() {
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.VERTICAL);

        // First row with name and quantity
        LinearLayout firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);

        EditText ingredientNameEditText = new EditText(this);
        ingredientNameEditText.setHint("Ingredient Name");
        ingredientNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        ingredientNameEditText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ingredientNameEditText.setPadding(16, 16, 16, 16);

        EditText ingredientQuantityEditText = new EditText(this);
        ingredientQuantityEditText.setHint("Quantity");
        ingredientQuantityEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        ingredientQuantityEditText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ingredientQuantityEditText.setPadding(16, 16, 16, 16);

        firstRow.addView(ingredientNameEditText);
        firstRow.addView(ingredientQuantityEditText);

        // Second row with category dropdown and delete button
        LinearLayout secondRow = new LinearLayout(this);
        secondRow.setOrientation(LinearLayout.HORIZONTAL);

        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        categorySpinner.setPadding(16, 16, 16, 16);

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(android.R.drawable.ic_delete);
        deleteButton.setBackground(null); // Remove default background
        deleteButton.setPadding(16, 16, 16, 16);

        // Set up delete action
        deleteButton.setOnClickListener(v -> {
            int index = ingredientContainer.indexOfChild(ingredientLayout);
            ingredientContainer.removeView(ingredientLayout);
            if (index != -1) {
                ingredientsList.remove(index);
            }
            Toast.makeText(this, "Ingredient removed", Toast.LENGTH_SHORT).show();
        });

        secondRow.addView(categorySpinner);
        secondRow.addView(deleteButton);

        // Add rows to the ingredient layout
        ingredientLayout.addView(firstRow);
        ingredientLayout.addView(secondRow);

        // Add the complete ingredient layout to the container
        ingredientContainer.addView(ingredientLayout);

        // Add a new ingredient with default values to the list
        ingredientsList.add(new Ingredient(0, "", "Other", 1));
    }

    // Validate the last ingredient field to ensure it is filled
    private boolean validateLastIngredientField() {
        int childCount = ingredientContainer.getChildCount();
        if (childCount == 0) return true;

        LinearLayout lastIngredientLayout = (LinearLayout) ingredientContainer.getChildAt(childCount - 1);
        LinearLayout firstRow = (LinearLayout) lastIngredientLayout.getChildAt(0);
        EditText nameField = (EditText) firstRow.getChildAt(0);
        EditText quantityField = (EditText) firstRow.getChildAt(1);

        return !nameField.getText().toString().isEmpty() && !quantityField.getText().toString().isEmpty();
    }

    // Show date picker dialog for selecting the date
    private void showDatePickerDialog() {
        int year = selectedDateCalendar.get(Calendar.YEAR);
        int month = selectedDateCalendar.get(Calendar.MONTH);
        int day = selectedDateCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year1);
            selectedDateCalendar.set(Calendar.MONTH, month1);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateSelectedDateText();
        }, year, month, day);

        datePickerDialog.show();
    }

    // Update the selected date text view with the selected date
    private void updateSelectedDateText() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String formattedDate = displayFormat.format(selectedDateCalendar.getTime());
        selectedDateTextView.setText(formattedDate);
    }
}
