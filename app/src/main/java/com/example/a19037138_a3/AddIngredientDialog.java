package com.example.a19037138_a3;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.widget.ArrayAdapter;

public class AddIngredientDialog extends DialogFragment {

    private EditText ingredientName;
    private EditText ingredientQuantity;
    private Spinner ingredientCategory;
    private AddIngredientListener listener;
    private View dialogView;

    public interface AddIngredientListener {
        void onIngredientAdded(Ingredient ingredient);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Inflate the layout with a non-null parent to resolve layout params
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_ingredient, (ViewGroup) getView(), false);

        // Initialize UI elements
        ingredientName = dialogView.findViewById(R.id.ingredient_name);
        ingredientQuantity = dialogView.findViewById(R.id.ingredient_quantity);
        ingredientCategory = dialogView.findViewById(R.id.ingredient_category);

        // Set up the spinner with categories
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.category_no_all, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientCategory.setAdapter(spinnerAdapter);

        // Configure dialog buttons
        builder.setView(dialogView)
                .setTitle("Add Ingredient")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Add", (dialog, which) -> addIngredient());

        return builder.create();
    }

    private void addIngredient() {
        // Collect input and pass the new ingredient
        String name = capitalizeWord(ingredientName.getText().toString());
        int quantity = Integer.parseInt(ingredientQuantity.getText().toString());
        String category = ingredientCategory.getSelectedItem().toString();

        Ingredient newIngredient = new Ingredient(0, name, category, quantity);
        listener.onIngredientAdded(newIngredient);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddIngredientListener) {
            listener = (AddIngredientListener) context;
        } else {
            throw new RuntimeException(context + " must implement AddIngredientListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release references to avoid memory leaks
        ingredientName = null;
        ingredientQuantity = null;
        ingredientCategory = null;
        dialogView = null;
        listener = null;
    }

    public String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
