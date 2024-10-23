package com.example.a19037138_a3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {

    // UI Components
    private EditText mealNameEditText;
    private Spinner mealTypeSpinner;
    private TextView selectedDateTextView;
    private Button addMealButton;
    private Button addIngredientButton;
    private LinearLayout ingredientContainer;

    // Calendar for storing the selected date
    private final Calendar selectedDateCalendar = Calendar.getInstance();

    // List to store ingredients added to the meal
    private final ArrayList<Ingredient> ingredientsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Initialize UI components
        initializeUI();

        // Set the back button's click listener to navigate back to MainActivity
        setBackButtonListener();

        // Set listener to show date picker when date field is clicked
        selectedDateTextView.setOnClickListener(v -> showDatePickerDialog());

        // Set listener to dynamically add new ingredient fields
        addIngredientButton.setOnClickListener(v -> {
            if (validateLastIngredientField()) {
                addIngredientField();
            } else {
                Toast.makeText(this, "Please fill in the last ingredient first", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the meal to the database when the "Add Meal" button is clicked
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

    // Set the listener for the back button to navigate to MainActivity
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
        String mealName = mealNameEditText.getText().toString();
        String mealType = mealTypeSpinner.getSelectedItem().toString();

        // Format the selected date to 'yyyy-MM-dd'
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedDateCalendar.getTime());

        if (mealName.isEmpty() || formattedDate.isEmpty() || mealType.equals("Select Meal Time")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the meal and its ingredients to the database
        try (DatabaseHelper db = new DatabaseHelper(this)) {
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
    }

    // Dynamically add a new ingredient field
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

    // Create the first row for ingredient name and quantity input
    private LinearLayout createIngredientRow() {
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

        return firstRow;
    }

    // Create the second row with a category spinner and delete button
    private LinearLayout createCategoryRow(LinearLayout ingredientLayout) {
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
        deleteButton.setBackground(null);
        deleteButton.setPadding(16, 16, 16, 16);
        deleteButton.setOnClickListener(v -> removeIngredient(ingredientLayout));

        secondRow.addView(categorySpinner);
        secondRow.addView(deleteButton);

        return secondRow;
    }

    // Remove the selected ingredient field from the list and the UI
    private void removeIngredient(LinearLayout ingredientLayout) {
        int index = ingredientContainer.indexOfChild(ingredientLayout);
        ingredientContainer.removeView(ingredientLayout);

        if (index != -1) {
            ingredientsList.remove(index);
        }

        Toast.makeText(this, "Ingredient removed", Toast.LENGTH_SHORT).show();
    }

    // Validate that the last added ingredient field is filled
    private boolean validateLastIngredientField() {
        int childCount = ingredientContainer.getChildCount();
        if (childCount == 0) return true;

        LinearLayout lastIngredientLayout = (LinearLayout) ingredientContainer.getChildAt(childCount - 1);
        LinearLayout firstRow = (LinearLayout) lastIngredientLayout.getChildAt(0);
        EditText nameField = (EditText) firstRow.getChildAt(0);
        EditText quantityField = (EditText) firstRow.getChildAt(1);

        return !nameField.getText().toString().isEmpty() && !quantityField.getText().toString().isEmpty();
    }

    // Show a date picker dialog to select a date
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

    // Update the displayed date with the selected date
    private void updateSelectedDateText() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String formattedDate = displayFormat.format(selectedDateCalendar.getTime());
        selectedDateTextView.setText(formattedDate);
    }
}
