package com.example.a19037138_a3;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class ShoppingListViewModel extends AndroidViewModel {

    private final DatabaseHelper db;
    private final MutableLiveData<List<Ingredient>> ingredients = new MutableLiveData<>();

    public ShoppingListViewModel(@NonNull Application application) {
        super(application);
        db = DatabaseHelper.getInstance(application);
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return ingredients;
    }

    public void loadShoppingList(String category) {
        List<Ingredient> ingredientList = category.equalsIgnoreCase("All")
                ? db.getAllIngredients()
                : db.getIngredientsByCategory(category);
        ingredients.postValue(ingredientList);
    }

    public void addOrUpdateIngredient(Ingredient newIngredient) {
        db.addIngredient(0, newIngredient.getName(), newIngredient.getQuantity(), newIngredient.getCategory());
        loadShoppingList("All"); // Refresh list
    }

    public void deleteIngredient(Ingredient ingredient) {
        db.deleteIngredient(ingredient.getId());
        loadShoppingList("All"); // Refresh list
    }

    public void undoDelete(Ingredient ingredient) {
        db.addIngredient(0, ingredient.getName(), ingredient.getQuantity(), ingredient.getCategory());
        loadShoppingList("All"); // Refresh list
    }

    public void searchIngredients(String query) {
        List<Ingredient> searchResults = db.getIngredientsByName(query);
        ingredients.postValue(searchResults);
    }
}
