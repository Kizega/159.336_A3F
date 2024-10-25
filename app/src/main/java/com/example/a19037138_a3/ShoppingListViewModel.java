package com.example.a19037138_a3;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

/**
 * ViewModel to manage the shopping list data and business logic.
 * Provides data to the UI and handles operations on ingredients.
 */
public class ShoppingListViewModel extends AndroidViewModel {

    private static final String TAG = "ShoppingListViewModel";  // Log tag for debugging

    private final DatabaseHelper db;  // Database helper instance
    private final MutableLiveData<List<Ingredient>> ingredients = new MutableLiveData<>();  // LiveData for ingredients

    /**
     * Interface for Snackbar actions to restore ingredient states.
     */
    public interface SnackbarListener {
        void showUndoSnackbar(Ingredient ingredient, int originalQuantity);
    }

    /**
     * Constructor to initialize the ViewModel with the application context.
     *
     * @param application The application context.
     */
    public ShoppingListViewModel(@NonNull Application application) {
        super(application);
        db = DatabaseHelper.getInstance(application);  // Initialize the database
    }

    /**
     * Sets the Snackbar listener for undo actions.
     *
     * @param listener The SnackbarListener to set.
     */
    public void setSnackbarListener(SnackbarListener listener) {
        if (listener != null) {
            listener.showUndoSnackbar(new Ingredient(0, "", "", 1), 1);  // Example call to validate listener
        }
    }

    /**
     * Returns the LiveData of ingredients to observe in the UI.
     *
     * @return A LiveData object containing a list of ingredients.
     */
    public LiveData<List<Ingredient>> getIngredients() {
        return ingredients;
    }

    /**
     * Loads the shopping list based on the selected category.
     *
     * @param category The category to filter ingredients by, or "All" to fetch all.
     */
    public void loadShoppingList(String category) {
        List<Ingredient> ingredientList = category.equalsIgnoreCase("All")
                ? db.getConsolidatedIngredients()  // Fetch all ingredients
                : db.getIngredientsByCategory(category);  // Fetch by category

        ingredients.postValue(ingredientList);  // Notify observers with the new list
    }

    /**
     * Adds or updates an ingredient in the database and refreshes the shopping list.
     *
     * @param newIngredient The ingredient to be added or updated.
     */
    public void addOrUpdateIngredient(Ingredient newIngredient) {
        if (isIngredientValid(newIngredient)) {
            db.addOrUpdateIngredient(
                    newIngredient.getName(),
                    newIngredient.getCategory(),
                    newIngredient.getQuantity()
            );
            loadShoppingList("All");  // Refresh the list
        } else {
            Log.w(TAG, "Invalid ingredient: " + newIngredient);
        }
    }

    /**
     * Deletes an ingredient from the database and refreshes the shopping list.
     *
     * @param ingredient The ingredient to delete.
     */
    public void deleteIngredient(Ingredient ingredient) {
        db.deleteIngredientByNameAndCategory(ingredient.getName(), ingredient.getCategory());
        loadShoppingList("All");  // Refresh the list after deletion
    }

    /**
     * Searches for ingredients by name and updates the shopping list with the results.
     *
     * @param query The search query to filter ingredients by name.
     */
    public void searchIngredients(String query) {
        List<Ingredient> searchResults = db.getIngredientsByName(query);
        ingredients.postValue(searchResults);  // Notify observers with search results
    }

    /**
     * Restores the original quantity of an ingredient, re-adding it if necessary.
     *
     * @param ingredient       The ingredient to restore.
     * @param originalQuantity The original quantity to restore.
     */
    public void restoreOriginalQuantity(Ingredient ingredient, int originalQuantity) {
        Ingredient existingIngredient = db.getIngredientByNameAndCategory(
                ingredient.getName(), ingredient.getCategory());

        if (existingIngredient != null) {
            db.updateIngredientQuantity(existingIngredient.getId(), originalQuantity);
            Log.i(TAG, "Restored original quantity. New quantity: " + originalQuantity);
        } else {
            db.addIngredient(db.getWritableDatabase(), 0, ingredient.getName(),
                    originalQuantity, ingredient.getCategory());
            Log.i(TAG, "Re-added ingredient with original quantity: " + originalQuantity);
        }

        loadShoppingList("All");  // Refresh the list after restoration
    }

    /**
     * Reduces the quantity of an ingredient and refreshes the list.
     * If the quantity reaches 0, the ingredient is deleted.
     *
     * @param ingredient       The ingredient to reduce quantity for.
     * @param quantityToRemove The quantity to remove.
     */
    public void reduceIngredientQuantity(Ingredient ingredient, int quantityToRemove) {
        int oldQuantity = ingredient.getQuantity();
        int newQuantity = oldQuantity - quantityToRemove;

        Log.i(TAG, "Reducing quantity for Ingredient ID: " + ingredient.getId()
                + " from: " + oldQuantity + " to: " + newQuantity);

        if (newQuantity > 0) {
            boolean updated = db.updateIngredientQuantity(ingredient.getId(), newQuantity);

            if (!updated) {
                Log.e(TAG, "Failed to update quantity for ID: " + ingredient.getId());
            }
        } else {
            deleteIngredient(ingredient);  // Delete ingredient if quantity is zero
        }

        loadShoppingList("All");  // Refresh the list after reduction
    }

    /**
     * Validates the provided ingredient object.
     *
     * @param ingredient The ingredient to validate.
     * @return True if the ingredient is valid, false otherwise.
     */
    private boolean isIngredientValid(Ingredient ingredient) {
        return ingredient != null &&
                ingredient.getName() != null &&
                !ingredient.getName().trim().isEmpty() &&
                ingredient.getQuantity() > 0;
    }
}
