package com.example.a19037138_a3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import android.widget.ArrayAdapter;
import androidx.appcompat.widget.SearchView;

public class ShoppingListActivity extends AppCompatActivity
        implements AddIngredientDialog.AddIngredientListener, ShoppingListAdapter.OnIngredientDeleteListener {

    private static final String TAG = "ShoppingListActivity";
    private final List<Ingredient> ingredientList = new ArrayList<>();
    private ShoppingListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        setupRecyclerView();
        setupCategorySpinner();
        setupSwipeToDelete(findViewById(R.id.recyclerView));
        setupSearchFunctionality();

        FloatingActionButton fab = findViewById(R.id.fab_add_item);
        fab.setOnClickListener(v -> showAddIngredientDialog());

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> goBackToMain());

        loadShoppingList();
        filterByCategory(getSelectedCategory());
    }


    // Configures the category filter spinner
    private void setupCategorySpinner() {
        Spinner categorySpinner = findViewById(R.id.category_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@Nullable AdapterView<?> parent, @Nullable View view, int position, long id) {
                if (parent != null) {
                    String selectedCategory = parent.getItemAtPosition(position).toString();
                    filterByCategory(selectedCategory);
                    saveSelectedCategory(selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(@Nullable AdapterView<?> parent) {
                filterByCategory("All");
            }
        });

        String savedCategory = getSelectedCategory();
        int position = adapter.getPosition(savedCategory);
        if (position >= 0) {
            categorySpinner.setSelection(position);
        }
    }

    // Filters ingredients by category
    private void filterByCategory(String category) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            List<Ingredient> filteredList = category.equalsIgnoreCase("All")
                    ? db.getAllIngredients()
                    : db.getIngredientsByCategory(category);
            adapter.updateList(filteredList);
        } catch (Exception e) {
            Log.e(TAG, "Error filtering ingredients", e);
        }
    }

    // Sets up swipe-to-delete functionality
    private void setupSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Ingredient ingredient = ingredientList.get(position);

                try (DatabaseHelper db = new DatabaseHelper(ShoppingListActivity.this)) {
                    db.deleteIngredient(ingredient.getId());
                    ingredientList.remove(position);
                    adapter.notifyItemRemoved(position);

                    Snackbar.make(recyclerView, "Item removed", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> {
                                ingredientList.add(position, ingredient);
                                adapter.notifyItemInserted(position);
                            }).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting ingredient", e);
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    // Sets up the RecyclerView
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new ShoppingListAdapter(ingredientList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadShoppingList();
    }

    // Loads all ingredients from the database
    private void loadShoppingList() {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            ingredientList.clear();
            ingredientList.addAll(db.getAllIngredients());
            adapter.updateList(new ArrayList<>(ingredientList));
        } catch (Exception e) {
            Log.e(TAG, "Error loading shopping list", e);
        }
    }

    // Displays the dialog to add a new ingredient
    private void showAddIngredientDialog() {
        AddIngredientDialog dialog = new AddIngredientDialog();
        dialog.show(getSupportFragmentManager(), "AddIngredientDialog");
    }

    // Returns to the main activity
    private void goBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Saves the selected category to SharedPreferences
    private void saveSelectedCategory(String category) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString("selected_category", category).apply();
    }

    // Retrieves the selected category from SharedPreferences
    private String getSelectedCategory() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("selected_category", "");
    }

    // Adds a new ingredient to the list or updates an existing one
    @Override
    public void onIngredientAdded(Ingredient newIngredient) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            String capitalizedName = db.capitalizeWord(newIngredient.getName());
            boolean ingredientExists = false;

            for (Ingredient ingredient : ingredientList) {
                if (ingredient.getName().equalsIgnoreCase(capitalizedName) &&
                        ingredient.getCategory().equalsIgnoreCase(newIngredient.getCategory())) {
                    int updatedQuantity = ingredient.getQuantity() + newIngredient.getQuantity();
                    ingredient.setQuantity(updatedQuantity);
                    db.updateIngredient(ingredient);
                    ingredientExists = true;
                    break;
                }
            }

            if (!ingredientExists) {
                db.addIngredient(0, capitalizedName, newIngredient.getQuantity(), newIngredient.getCategory());
            }

            loadShoppingList();
        } catch (Exception e) {
            Log.e(TAG, "Error adding ingredient", e);
        }
    }

    // Deletes an ingredient from the list and database
    @Override
    public void onIngredientDelete(Ingredient ingredient) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            db.deleteIngredient(ingredient.getId());
            ingredientList.remove(ingredient);
            adapter.updateList(new ArrayList<>(ingredientList));
        } catch (Exception e) {
            Log.e(TAG, "Error deleting ingredient", e);
        }
    }

    // Filters the ingredients by search query
    private void filterBySearch(String query) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            List<Ingredient> filteredList = db.getIngredientsByName(query);
            adapter.updateList(filteredList);
        } catch (Exception e) {
            Log.e(TAG, "Error searching ingredients", e);
        }
    }

    // Sets up search functionality for the shopping list
    private void setupSearchFunctionality() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBySearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBySearch(newText);
                return false;
            }
        });
    }
}
