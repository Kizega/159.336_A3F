package com.example.a19037138_a3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    // Marked ingredients as final since it is only assigned once in the constructor.
    private final List<Ingredient> ingredients;
    private final OnIngredientDeleteListener deleteListener;

    // Interface for handling ingredient deletion.
    public interface OnIngredientDeleteListener {
        void onIngredientDelete(Ingredient ingredient);
    }

    // Constructor to initialize the list and listener.
    public ShoppingListAdapter(List<Ingredient> ingredients, OnIngredientDeleteListener deleteListener) {
        this.ingredients = new ArrayList<>(ingredients);
        this.deleteListener = deleteListener;
    }

    // Get an ingredient at a specific position.
    public Ingredient getIngredientAt(int position) {
        return ingredients.get(position);
    }

    // Update the ingredient list and notify changes.
    public void updateList(List<Ingredient> newList) {
        int oldSize = ingredients.size();
        int newSize = newList.size();

        ingredients.clear();
        ingredients.addAll(newList);

        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else if (newSize < oldSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize);
        } else {
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

        // Set data for the current ingredient.
        holder.name.setText(ingredient.getName());
        holder.quantity.setText(String.valueOf(ingredient.getQuantity()));
        holder.ingredientCheckbox.setChecked(false);

        // Handle delete button click.
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onIngredientDelete(ingredient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }


    // ViewHolder to manage individual ingredient rows.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        AutoCompleteTextView name;
        EditText quantity;
        CheckBox ingredientCheckbox;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize views.
            name = itemView.findViewById(R.id.ingredientName);
            quantity = itemView.findViewById(R.id.ingredientQuantity);
            ingredientCheckbox = itemView.findViewById(R.id.ingredientCheckbox);
            deleteButton = itemView.findViewById(R.id.deleteIngredientButton);
        }
    }
}
