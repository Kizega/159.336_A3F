package com.example.a19037138_a3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private final List<Ingredient> ingredients; // Holds the list of ingredients
    private final OnIngredientDeleteListener deleteListener; // Listener for handling deletion events

    /**
     * Interface to communicate ingredient deletion actions.
     */
    public interface OnIngredientDeleteListener {
        void onIngredientDelete(Ingredient ingredient);
    }

    /**
     * Constructor for ShoppingListAdapter.
     *
     * @param ingredients    List of ingredients to display.
     * @param deleteListener Listener for handling ingredient deletions.
     */
    public ShoppingListAdapter(List<Ingredient> ingredients, OnIngredientDeleteListener deleteListener) {
        this.ingredients = ingredients;
        this.deleteListener = deleteListener;
    }

    /**
     * Updates the ingredient list efficiently by notifying changes.
     *
     * @param newList The new list of ingredients.
     */
    public void updateList(List<Ingredient> newList) {
        int oldSize = ingredients.size();
        int newSize = newList.size();

        ingredients.clear();
        ingredients.addAll(newList);

        if (newSize > oldSize) {
            // Notify only the new items inserted
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else if (newSize < oldSize) {
            // Notify the removal of items
            notifyItemRangeRemoved(newSize, oldSize - newSize);
        } else {
            // Notify that the list contents have changed
            notifyItemRangeChanged(0, newSize);
        }
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

        // Bind the ingredient data to the UI elements
        holder.name.setText(ingredient.getName());
        holder.quantity.setText(String.valueOf(ingredient.getQuantity()));

        // Set up the delete button click listener
        holder.deleteButton.setOnClickListener(v -> deleteListener.onIngredientDelete(ingredient));
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * ViewHolder class for holding the views for each ingredient row.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        AutoCompleteTextView name;
        EditText quantity;
        CheckBox ingredientCheckbox;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views
            name = itemView.findViewById(R.id.ingredientName);
            quantity = itemView.findViewById(R.id.ingredientQuantity);
            ingredientCheckbox = itemView.findViewById(R.id.ingredientCheckbox);
            deleteButton = itemView.findViewById(R.id.deleteIngredientButton);
        }
    }
}
