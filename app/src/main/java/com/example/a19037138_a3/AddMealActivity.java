package com.example.a19037138_a3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

        // Back button action
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddMealActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Show DatePicker when clicking on date field
        selectedDateTextView.setOnClickListener(v -> showDatePickerDialog());

        // Add new ingredient field dynamically when clicking the button
        addIngredientButton.setOnClickListener(v -> {
            if (validateLastIngredientField()) {
                addIngredientField();
            } else {
                Toast.makeText(this, "Please fill in the last ingredient first", Toast.LENGTH_SHORT).show();
            }
        });

        // Add meal to the database
        addMealButton.setOnClickListener(v -> {
            String mealName = mealNameEditText.getText().toString();
            String mealType = mealTypeSpinner.getSelectedItem().toString();
            String selectedDate = selectedDateTextView.getText().toString();

            if (mealName.isEmpty() || selectedDate.isEmpty() || mealType.equals("Select Meal Time")) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(this);
            long mealId = db.addMeal(mealName, mealType, selectedDate, ingredientsList);

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
        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);

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

        ingredientLayout.addView(ingredientNameEditText);
        ingredientLayout.addView(ingredientQuantityEditText);
        ingredientContainer.addView(ingredientLayout);

        ingredientsList.add(new Ingredient("", "", "Other"));
    }

    // Validate the last ingredient field
    private boolean validateLastIngredientField() {
        int childCount = ingredientContainer.getChildCount();
        if (childCount == 0) return true;

        LinearLayout lastIngredientLayout = (LinearLayout) ingredientContainer.getChildAt(childCount - 1);
        EditText nameField = (EditText) lastIngredientLayout.getChildAt(0);
        EditText quantityField = (EditText) lastIngredientLayout.getChildAt(1);

        return !nameField.getText().toString().isEmpty() && !quantityField.getText().toString().isEmpty();
    }

    // Show date picker dialog
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

    // Update the selected date text
    private void updateSelectedDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(selectedDateCalendar.getTime());
        selectedDateTextView.setText(formattedDate);
    }
}
