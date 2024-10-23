package com.example.a19037138_a3;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.widget.ArrayAdapter;

/**
 * A dialog fragment used to add a new ingredient.
 * Allows users to enter the name, quantity, and category of an ingredient.
 * Implements the AddIngredientListener interface to pass data back to the parent activity.
 */
public class AddIngredientDialog extends DialogFragment {

    // UI elements for input fields
    private EditText ingredientName;
    private EditText ingredientQuantity;
    private Spinner ingredientCategory;
    private AddIngredientListener listener;

    /**
     * Listener interface to handle the event when an ingredient is added.
     * The activity hosting the dialog must implement this interface.
     */
    public interface AddIngredientListener {
        void onIngredientAdded(Ingredient ingredient);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use AlertDialog.Builder to create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_ingredient, null);

        // Initialize input fields
        ingredientName = view.findViewById(R.id.ingredient_name);
        ingredientQuantity = view.findViewById(R.id.ingredient_quantity);
        ingredientCategory = view.findViewById(R.id.ingredient_category);

        // Set up the spinner adapter, excluding the "All" option
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.category_no_all, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientCategory.setAdapter(spinnerAdapter);

        // Configure the dialog's buttons and their actions
        builder.setView(view)
                .setTitle("Add Ingredient") // Title at the top of the dialog
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Dismiss on cancel
                .setPositiveButton("Add", (dialog, which) -> {
                    // Get user input values
                    String name = capitalizeWord(ingredientName.getText().toString());
                    int quantity = Integer.parseInt(ingredientQuantity.getText().toString());
                    String category = ingredientCategory.getSelectedItem().toString();

                    // Create a new Ingredient object with user input
                    Ingredient newIngredient = new Ingredient(0, name, category, quantity);

                    // Pass the ingredient back to the activity using the listener
                    listener.onIngredientAdded(newIngredient);
                });

        return builder.create(); // Return the created dialog
    }

    /**
     * Ensures that the hosting activity implements the AddIngredientListener interface.
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
     * Capitalizes the first letter of a word to make it look nicer.
     *
     * @param word The input string to be capitalized.
     * @return The capitalized string.
     */
    private String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word; // Return as-is if null or empty
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
