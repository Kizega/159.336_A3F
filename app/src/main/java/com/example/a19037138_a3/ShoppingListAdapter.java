package com.example.a19037138_a3;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of ingredients in the shopping list.
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private final List<Ingredient> ingredients;
    private final OnIngredientDeleteListener deleteListener;

    /**
     * Interface to handle ingredient deletion.
     */
    public interface OnIngredientDeleteListener {
        void onIngredientDelete(Ingredient ingredient);
    }

    /**
     * Constructor to initialize the adapter with ingredients and a delete listener.
     */
    public ShoppingListAdapter(List<Ingredient> ingredients, OnIngredientDeleteListener deleteListener) {
        this.ingredients = new ArrayList<>(ingredients);
        this.deleteListener = deleteListener;
    }

    /**
     * Returns the ingredient at the specified position.
     */
    public Ingredient getIngredientAt(int position) {
        return ingredients.get(position);
    }

    /**
     * Updates the ingredient list and refreshes the RecyclerView.
     */
    public void updateList(List<Ingredient> newList) {
        ingredients.clear();
        ingredients.addAll(newList);
        notifyDataSetChanged();  // Ensure the adapter refreshes the UI
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        // Set ingredient details to the UI
        holder.name.setText(ingredient.getName());
        holder.quantity.setText(String.valueOf(ingredient.getQuantity()));

        // When the delete button is clicked, show the quantity dialog
        holder.deleteButton.setOnClickListener(v -> {
            if (ingredient.getQuantity() > 1) {
                // Show dialog to ask how many units to delete
                ((ShoppingListActivity) v.getContext()).showQuantityDialog(ingredient, position);
            } else {
                // Directly delete if quantity is 1
                ((ShoppingListActivity) v.getContext()).viewModel.deleteIngredient(ingredient);
            }
        });
    }


    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Shows a confirmation dialog before deleting an ingredient.
     */
    private void showDeleteConfirmationDialog(View view, Ingredient ingredient, int position) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete " + ingredient.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                        deleteListener.onIngredientDelete(ingredient);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * ViewHolder class to hold the views for each ingredient item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView quantity;
        CheckBox ingredientCheckbox;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ingredientName);
            quantity = itemView.findViewById(R.id.ingredientQuantity);
            ingredientCheckbox = itemView.findViewById(R.id.ingredientCheckbox);
            deleteButton = itemView.findViewById(R.id.deleteIngredientButton);
        }
    }
    public void removeIngredientAt(int position) {
        ingredients.remove(position);  // Remove ingredient from the list
        notifyItemRemoved(position);   // Notify RecyclerView of item removal
    }

}
