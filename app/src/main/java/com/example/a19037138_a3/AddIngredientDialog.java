package com.example.a19037138_a3;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * DialogFragment for adding a new ingredient.
 * Provides user input fields for name, quantity, and category selection.
 */
public class AddIngredientDialog extends DialogFragment {

    private EditText ingredientName;
    private EditText ingredientQuantity;
    private Spinner ingredientCategory;
    private AddIngredientListener listener;
    private View dialogView;

    /**
     * Interface to pass the new ingredient back to the parent activity.
     */
    public interface AddIngredientListener {
        void onIngredientAdded(Ingredient ingredient);
    }

    /**
     * Attaches the dialog to the parent activity, ensuring it implements the required listener.
     *
     * @param context The context of the parent activity.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddIngredientListener) {
            listener = (AddIngredientListener) context;
        } else {
            throw new RuntimeException(context + " must implement AddIngredientListener");
        }
    }

    /**
     * Creates and returns the dialog instance with user input fields.
     *
     * @param savedInstanceState Contains the previous state if the activity is re-initialized.
     * @return A configured AlertDialog instance.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_ingredient, (ViewGroup) getView(), false);

        initializeUIComponents(); // Initialize the dialog's UI elements
        configureSpinner();        // Set up the spinner with categories

        builder.setView(dialogView)
                .setTitle("Add Ingredient")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Add", (dialog, which) -> addIngredient());

        return builder.create();
    }

    /**
     * Initializes the dialog's input fields and category spinner.
     */
    private void initializeUIComponents() {
        ingredientName = dialogView.findViewById(R.id.ingredient_name);
        ingredientQuantity = dialogView.findViewById(R.id.ingredient_quantity);
        ingredientCategory = dialogView.findViewById(R.id.ingredient_category);
    }

    /**
     * Configures the category spinner with predefined options.
     */
    private void configureSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.category_no_all, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientCategory.setAdapter(spinnerAdapter);
    }

    /**
     * Gathers user input to create a new Ingredient object and passes it to the listener.
     */
    private void addIngredient() {
        String name = capitalizeWord(ingredientName.getText().toString());
        int quantity = Integer.parseInt(ingredientQuantity.getText().toString());
        String category = ingredientCategory.getSelectedItem().toString();

        Ingredient newIngredient = new Ingredient(0, name, category, quantity);
        listener.onIngredientAdded(newIngredient); // Pass the new ingredient to the listener
    }

    /**
     * Cleans up references to avoid memory leaks when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ingredientName = null;
        ingredientQuantity = null;
        ingredientCategory = null;
        dialogView = null;
        listener = null;
    }

    /**
     * Capitalizes the first letter of the given word.
     *
     * @param word The word to capitalize.
     * @return The word with its first letter capitalized.
     */
    public String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
