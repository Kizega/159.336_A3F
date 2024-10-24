package com.example.a19037138_a3;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class ShoppingListViewModel extends AndroidViewModel {

    private final DatabaseHelper db;
    private final MutableLiveData<List<Ingredient>> ingredients = new MutableLiveData<>();
    private static final String TAG = "ShoppingListViewModel";
    private SnackbarListener snackbarListener;

    public ShoppingListViewModel(@NonNull Application application) {
        super(application);
        db = DatabaseHelper.getInstance(application);
    }

    public void setSnackbarListener(SnackbarListener listener) {
        this.snackbarListener = listener;
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return ingredients;
    }

    public void loadShoppingList(String category) {
        List<Ingredient> ingredientList;

        if (category.equalsIgnoreCase("All")) {
            ingredientList = db.getConsolidatedIngredients();  // Use consolidated ingredients
        } else {
            ingredientList = db.getIngredientsByCategory(category);  // Use existing category filter
        }

        ingredients.postValue(ingredientList);
    }




    public void addOrUpdateIngredient(Ingredient newIngredient) {
        if (isIngredientValid(newIngredient)) {
            db.addOrUpdateIngredient(
                    newIngredient.getName(),
                    newIngredient.getCategory(),
                    newIngredient.getQuantity()
            );
            loadShoppingList("All");  // Refresh the list after adding or updating.
        } else {
            Log.w("ShoppingListViewModel", "Invalid ingredient: " + newIngredient);
        }
    }
    public void reduceIngredientQuantity(Ingredient ingredient, int quantityToRemove) {
        int oldQuantity = ingredient.getQuantity();
        int newQuantity = oldQuantity - quantityToRemove;

        if (newQuantity > 0) {
            db.updateIngredientQuantity(ingredient.getId(), newQuantity);
            Log.i("ShoppingListViewModel", "Reduced quantity to: " + newQuantity);
        } else {
            deleteIngredient(ingredient);
            Log.i("ShoppingListViewModel", "Deleted ingredient as quantity reached 0");
        }

        // Trigger the Snackbar using the listener
        if (snackbarListener != null) {
            snackbarListener.showUndoSnackbar(ingredient, oldQuantity);
        }

        // Refresh the shopping list
        loadShoppingList("All");
    }

    public void restoreIngredientQuantity(Ingredient ingredient, int oldQuantity) {
        db.updateIngredientQuantity(ingredient.getId(), oldQuantity);  // Restore old quantity
        Log.i(TAG, "Restored ingredient: " + ingredient.getName() + " with quantity: " + oldQuantity);

        // Refresh the shopping list
        loadShoppingList("All");
    }




    public void deleteIngredient(Ingredient ingredient) {
        db.deleteIngredientByNameAndCategory(ingredient.getName(), ingredient.getCategory());
        loadShoppingList("All");  // Refresh the list after deletion
    }

    public void searchIngredients(String query) {
        List<Ingredient> searchResults = db.getIngredientsByName(query);
        ingredients.postValue(searchResults);
    }

    // Helper method to validate an ingredient.
    private boolean isIngredientValid(Ingredient ingredient) {
        return ingredient != null &&
                ingredient.getName() != null &&
                !ingredient.getName().trim().isEmpty() &&
                ingredient.getQuantity() > 0;
    }

    public interface SnackbarListener {
        void showUndoSnackbar(Ingredient ingredient, int oldQuantity);
    }

    public void restoreOriginalQuantity(Ingredient ingredient, int originalQuantity) {
        // Get the existing ingredient from the database
        Ingredient existingIngredient = db.getIngredientByNameAndCategory(
                ingredient.getName(), ingredient.getCategory());

        if (existingIngredient != null) {
            // Set the quantity back to the original value
            db.updateIngredientQuantity(existingIngredient.getId(), originalQuantity);
            Log.i(TAG, "Restored original quantity. New quantity: " + originalQuantity);
        } else {
            // If the ingredient no longer exists, re-add it with the original quantity
            db.addIngredient(db.getWritableDatabase(), 0, ingredient.getName(),
                    originalQuantity, ingredient.getCategory());
            Log.i(TAG, "Re-added ingredient with original quantity: " + originalQuantity);
        }

        // Refresh the shopping list
        loadShoppingList("All");
    }





}
