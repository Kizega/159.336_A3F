package com.example.a19037138_a3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

/**
 * Activity to manage the shopping list.
 * Provides features to view, search, add, delete, and update ingredients.
 */
public class ShoppingListActivity extends AppCompatActivity
        implements AddIngredientDialog.AddIngredientListener, ShoppingListAdapter.OnIngredientDeleteListener {

    private ShoppingListViewModel viewModel;  // ViewModel to handle logic
    private ShoppingListAdapter adapter;      // Adapter for RecyclerView
    private RecyclerView recyclerView;        // RecyclerView to display ingredients

    /**
     * Initializes the activity, sets up UI elements, and observes data changes.
     *
     * @param savedInstanceState The saved state of the activity (if any).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Initialize ViewModel and assign the Snackbar listener
        viewModel = new ViewModelProvider(this).get(ShoppingListViewModel.class);
        viewModel.setSnackbarListener(this::showUndoSnackbar);

        // Set up RecyclerView with adapter and layout manager
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new ShoppingListAdapter(new ArrayList<>(), this::showQuantityDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up other components and listeners
        setupCategorySpinner();
        setupSwipeToDelete();
        setupSearchFunctionality();

        // FloatingActionButton to add new ingredients
        FloatingActionButton fab = findViewById(R.id.fab_add_item);
        fab.setOnClickListener(v -> showAddIngredientDialog());

        // Back button to navigate to MainActivity
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> goBackToMain());

        // Observe data changes in ViewModel
        observeData();
    }

    /**
     * Observes changes in the ingredient data and updates the adapter accordingly.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void observeData() {
        viewModel.getIngredients().observe(this, ingredients -> {
            adapter.updateList(new ArrayList<>(ingredients));
            adapter.notifyDataSetChanged();  // Refresh UI
        });
    }

    /**
     * Configures the category spinner and restores the last selected category.
     */
    private void setupCategorySpinner() {
        Spinner categorySpinner = findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.category_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@Nullable AdapterView<?> parent, @Nullable View view, int position, long id) {
                if (parent != null) {
                    String selectedCategory = parent.getItemAtPosition(position).toString();
                    viewModel.loadShoppingList(selectedCategory);
                    saveSelectedCategory(selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(@Nullable AdapterView<?> parent) {
                viewModel.loadShoppingList("All");
            }
        });

        // Restore last selected category
        String savedCategory = getSelectedCategory();
        int position = spinnerAdapter.getPosition(savedCategory);
        if (position >= 0) {
            categorySpinner.setSelection(position);
        }
    }

    /**
     * Configures swipe gestures to delete ingredients from the RecyclerView.
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;  // No move operation required
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Ingredient ingredient = adapter.getIngredientAt(position);
                    viewModel.deleteIngredient(ingredient);
                    showUndoSnackbar(ingredient, ingredient.getQuantity());
                }
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    /**
     * Configures the search functionality for filtering ingredients.
     */
    private void setupSearchFunctionality() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchIngredients(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.searchIngredients(newText);
                return false;
            }
        });
    }

    /**
     * Displays the AddIngredientDialog to add a new ingredient.
     */
    private void showAddIngredientDialog() {
        AddIngredientDialog dialog = new AddIngredientDialog();
        dialog.show(getSupportFragmentManager(), "AddIngredientDialog");
    }

    /**
     * Navigates back to the MainActivity.
     */
    private void goBackToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Saves the selected category in shared preferences.
     */
    private void saveSelectedCategory(String category) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString("selected_category", category).apply();
    }

    /**
     * Retrieves the saved category from shared preferences.
     */
    private String getSelectedCategory() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("selected_category", "All");
    }

    /**
     * Displays an undo Snack bar when an ingredient is modified.
     */
    private void showUndoSnackbar(Ingredient ingredient, int originalQuantity) {
        View parentView = findViewById(android.R.id.content); // Use a valid root view

        if (parentView != null) {
            Snackbar.make(parentView, "Ingredient updated", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> viewModel.restoreOriginalQuantity(ingredient, originalQuantity))
                    .show();
        } else {
            Log.e("ShoppingListActivity", "Failed to show Snackbar: Parent view is null.");
        }
    }


    /**
     * Handles the addition of a new ingredient from the dialog.
     */
    @Override
    public void onIngredientAdded(Ingredient newIngredient) {
        viewModel.addOrUpdateIngredient(newIngredient);
    }

    /**
     * Handles ingredient deletion by showing the quantity dialog.
     */
    @Override
    public void onIngredientDelete(Ingredient ingredient, int position) {
        showQuantityDialog(ingredient, position);
    }

    /**
     * Displays a dialog to adjust or delete an ingredient's quantity.
     */
    private void showQuantityDialog(Ingredient ingredient, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Adjust Quantity")
                .setView(dialogView)
                .setNegativeButton("Cancel", (dialogInterface, which) -> adapter.notifyItemChanged(position))
                .create();

        EditText quantityInput = dialogView.findViewById(R.id.quantity_input);
        Button removeButton = dialogView.findViewById(R.id.remove_button);
        Button deleteAllButton = dialogView.findViewById(R.id.delete_all_button);

        removeButton.setOnClickListener(v -> {
            String inputText = quantityInput.getText().toString().trim();
            if (!inputText.isEmpty()) {
                int quantityToRemove = Integer.parseInt(inputText);
                if (quantityToRemove > 0 && quantityToRemove <= ingredient.getQuantity()) {
                    viewModel.reduceIngredientQuantity(ingredient, quantityToRemove);
                    adapter.notifyItemChanged(position);
                    dialog.dismiss();
                    showUndoSnackbar(ingredient, ingredient.getQuantity());
                } else {
                    quantityInput.setError("Invalid quantity");
                }
            } else {
                quantityInput.setError("Please enter a quantity");
            }
        });

        deleteAllButton.setOnClickListener(v -> {
            viewModel.deleteIngredient(ingredient);
            adapter.removeIngredientAt(position);
            dialog.dismiss();
            showUndoSnackbar(ingredient, ingredient.getQuantity());
        });

        dialog.show();
    }

    /**
     * Releases resources when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
    }
}
