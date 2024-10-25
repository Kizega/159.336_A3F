package com.example.a19037138_a3;

import android.util.Log;
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
 * Manages the binding of data to UI components within RecyclerView items.
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private final List<Ingredient> ingredients; // List of ingredients to display
    private final OnIngredientDeleteListener deleteListener; // Listener for delete actions

    /**
     * Constructor to initialize the adapter with ingredients and a delete listener.
     *
     * @param ingredients   List of ingredients to display.
     * @param deleteListener Listener to handle ingredient deletion.
     */
    public ShoppingListAdapter(List<Ingredient> ingredients, OnIngredientDeleteListener deleteListener) {
        this.ingredients = new ArrayList<>(ingredients);
        this.deleteListener = deleteListener;
    }

    /**
     * Interface to handle ingredient deletion events.
     */
    public interface OnIngredientDeleteListener {
        void onIngredientDelete(Ingredient ingredient, int position);
    }

    /**
     * Retrieves the ingredient at a specified position.
     *
     * @param position The position of the ingredient in the list.
     * @return The ingredient at the given position.
     */
    public Ingredient getIngredientAt(int position) {
        return ingredients.get(position);
    }

    /**
     * Updates the ingredient list and refreshes the RecyclerView.
     *
     * @param newList The new list of ingredients to display.
     */
    @SuppressWarnings("NotifyDataSetChanged")
    public void updateList(List<Ingredient> newList) {
        ingredients.clear();
        ingredients.addAll(newList);
        notifyDataSetChanged();  // Refresh the entire UI with new data
    }

    /**
     * Inflates the item layout and creates a ViewHolder.
     *
     * @param parent The parent view group.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder for the given position.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        // Bind ingredient data to the UI components
        holder.name.setText(ingredient.getName());
        holder.quantity.setText(String.valueOf(ingredient.getQuantity()));

        // Set the delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                Log.i("Adapter", "Ingredient ID passed to dialog: " + ingredient.getId());
                deleteListener.onIngredientDelete(ingredient, holder.getBindingAdapterPosition());
            }
        });
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The size of the ingredient list.
     */
    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * ViewHolder class to hold the views for each ingredient item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;             // Displays the ingredient's name
        TextView quantity;         // Displays the ingredient's quantity
        CheckBox ingredientCheckbox; // Optional checkbox (can be used for selection)
        ImageButton deleteButton;  // Button to delete the ingredient

        /**
         * Constructor to initialize the ViewHolder with its views.
         *
         * @param itemView The item view associated with this ViewHolder.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ingredientName);
            quantity = itemView.findViewById(R.id.ingredientQuantity);
            ingredientCheckbox = itemView.findViewById(R.id.ingredientCheckbox);
            deleteButton = itemView.findViewById(R.id.deleteIngredientButton);
        }
    }

    /**
     * Removes the ingredient at the specified position from the list.
     *
     * @param position The position of the ingredient to remove.
     */
    public void removeIngredientAt(int position) {
        ingredients.remove(position);  // Remove the ingredient from the list
        notifyItemRemoved(position);   // Notify RecyclerView of the removal
        notifyItemRangeChanged(position, ingredients.size());  // Smooth UI transition
    }
}
