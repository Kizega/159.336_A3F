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
 * Users can input the meal's name, type, date, and ingredients.
 */
public class AddMealActivity extends AppCompatActivity {

    private EditText mealNameEditText;
    private Spinner mealTypeSpinner;
    private TextView selectedDateTextView;
    private Button addMealButton, addIngredientButton;
    private LinearLayout ingredientContainer;

    private final Calendar selectedDateCalendar = Calendar.getInstance();
    private final ArrayList<Ingredient> ingredientsList = new ArrayList<>();
    private DatabaseHelper db;

    /**
     * Initializes the activity and sets up the UI components and listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        db = DatabaseHelper.getInstance(this);
        initializeUI();
        setListeners();
    }

    /**
     * Initializes all UI components.
     */
    private void initializeUI() {
        mealNameEditText = findViewById(R.id.meal_name);
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        selectedDateTextView = findViewById(R.id.selected_date);
        addMealButton = findViewById(R.id.add_meal_button);
        addIngredientButton = findViewById(R.id.add_ingredient_button);
        ingredientContainer = findViewById(R.id.ingredient_container);
    }

    /**
     * Configures listeners for UI interactions.
     */
    private void setListeners() {
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

    /**
     * Sets the back button to return to the MainActivity.
     */
    private void setBackButtonListener() {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    /**
     * Validates and adds a new meal to the database.
     */
    private void addMeal() {
        String mealName = mealNameEditText.getText().toString().trim();
        String mealType = mealTypeSpinner.getSelectedItem().toString();

        if (selectedDateTextView.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateValid()) {
            Toast.makeText(this, "Invalid date: Please select a valid date", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedDateCalendar.getTime());

        if (mealName.isEmpty() || mealType.equals("Select Meal Time")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        collectIngredients();

        if (ingredientsList.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        long mealId = db.addMeal(mealName, mealType, formattedDate, ingredientsList);
        if (mealId != -1) {
            Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Error adding meal", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates that the selected date is not in the past.
     *
     * @return True if the date is valid, false otherwise.
     */
    private boolean isDateValid() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return !selectedDateCalendar.before(today);
    }

    /**
     * Collects ingredients from the UI.
     */
    private void collectIngredients() {
        ingredientsList.clear();
        for (int i = 0; i < ingredientContainer.getChildCount(); i++) {
            LinearLayout ingredientLayout = (LinearLayout) ingredientContainer.getChildAt(i);
            EditText nameField = ingredientLayout.findViewById(R.id.ingredient_name);
            EditText quantityField = ingredientLayout.findViewById(R.id.ingredient_quantity);
            Spinner categorySpinner = ingredientLayout.findViewById(R.id.ingredient_category);

            String name = nameField.getText().toString().trim();
            int quantity = quantityField.getText().toString().isEmpty() ?
                    1 : Integer.parseInt(quantityField.getText().toString());
            String category = categorySpinner.getSelectedItem().toString();

            if (!name.isEmpty()) {
                ingredientsList.add(new Ingredient(0, name, category, quantity));
            } else {
                Toast.makeText(this, "Skipping ingredient with empty name", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Adds a new ingredient input field dynamically.
     */
    private void addIngredientField() {
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.VERTICAL);
        ingredientLayout.addView(createIngredientRow());
        ingredientLayout.addView(createCategoryRow(ingredientLayout));
        ingredientContainer.addView(ingredientLayout);
    }

    /**
     * Creates the first row for ingredient input.
     */
    private LinearLayout createIngredientRow() {
        LinearLayout firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);

        EditText nameField = new EditText(this);
        nameField.setHint("Ingredient Name");
        nameField.setInputType(InputType.TYPE_CLASS_TEXT);
        nameField.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        EditText quantityField = new EditText(this);
        quantityField.setHint("Quantity");
        quantityField.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityField.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        firstRow.addView(nameField);
        firstRow.addView(quantityField);

        return firstRow;
    }

    /**
     * Creates the second row with a category spinner and delete button.
     */
    private LinearLayout createCategoryRow(LinearLayout ingredientLayout) {
        LinearLayout secondRow = new LinearLayout(this);
        secondRow.setOrientation(LinearLayout.HORIZONTAL);

        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.category_no_all, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(android.R.drawable.ic_delete);
        deleteButton.setBackground(null);
        deleteButton.setOnClickListener(v -> removeIngredient(ingredientLayout));

        secondRow.addView(categorySpinner);
        secondRow.addView(deleteButton);

        return secondRow;
    }

    /**
     * Removes an ingredient field from the layout.
     */
    private void removeIngredient(LinearLayout ingredientLayout) {
        int index = ingredientContainer.indexOfChild(ingredientLayout);
        ingredientContainer.removeView(ingredientLayout);
        if (index != -1) ingredientsList.remove(index);
        Toast.makeText(this, "Ingredient removed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Validates if the last ingredient field is complete.
     */
    private boolean validateLastIngredientField() {
        if (ingredientContainer.getChildCount() == 0) return true;

        LinearLayout lastLayout = (LinearLayout) ingredientContainer.getChildAt(
                ingredientContainer.getChildCount() - 1);
        EditText nameField = lastLayout.findViewById(R.id.ingredient_name);
        EditText quantityField = lastLayout.findViewById(R.id.ingredient_quantity);

        return !nameField.getText().toString().isEmpty() &&
                !quantityField.getText().toString().isEmpty();
    }

    /**
     * Displays the date picker dialog.
     */
    private void showDatePickerDialog() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(year, month, dayOfMonth);
            updateSelectedDateText();
        }, selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Updates the selected date text view.
     */
    private void updateSelectedDateText() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        selectedDateTextView.setText(displayFormat.format(selectedDateCalendar.getTime()));
    }

    /**
     * Closes the database when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseHelper.closeDatabase();
    }
}
