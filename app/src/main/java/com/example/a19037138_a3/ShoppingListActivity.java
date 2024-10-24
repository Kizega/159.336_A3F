package com.example.a19037138_a3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

import android.widget.ArrayAdapter;

public class ShoppingListActivity extends AppCompatActivity
        implements AddIngredientDialog.AddIngredientListener, ShoppingListAdapter.OnIngredientDeleteListener {

    private ShoppingListViewModel viewModel; // ViewModel to manage data
    private ShoppingListAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        viewModel = new ViewModelProvider(this).get(ShoppingListViewModel.class);

        setupRecyclerView();
        setupCategorySpinner();
        setupSwipeToDelete();
        setupSearchFunctionality();

        FloatingActionButton fab = findViewById(R.id.fab_add_item);
        fab.setOnClickListener(v -> showAddIngredientDialog());

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> goBackToMain());

        observeData();
    }

    // Observes changes in the ingredient list from the ViewModel
    private void observeData() {
        viewModel.getIngredients().observe(this, ingredients -> adapter.updateList(new ArrayList<>(ingredients)));

        viewModel.loadShoppingList(getSelectedCategory());
    }

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
                    viewModel.loadShoppingList(selectedCategory);
                    saveSelectedCategory(selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(@Nullable AdapterView<?> parent) {
                viewModel.loadShoppingList("All");
            }
        });

        String savedCategory = getSelectedCategory();
        int position = adapter.getPosition(savedCategory);
        if (position >= 0) {
            categorySpinner.setSelection(position);
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new ShoppingListAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Use getBindingAdapterPosition() to handle the position correctly
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Ingredient ingredient = adapter.getIngredientAt(position);

                    viewModel.deleteIngredient(ingredient);
                    Snackbar.make(recyclerView, "Item removed", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> viewModel.undoDelete(ingredient))
                            .show();
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }


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

    private void showAddIngredientDialog() {
        AddIngredientDialog dialog = new AddIngredientDialog();
        dialog.show(getSupportFragmentManager(), "AddIngredientDialog");
    }

    private void goBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveSelectedCategory(String category) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString("selected_category", category).apply();
    }

    private String getSelectedCategory() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("selected_category", "All");
    }

    @Override
    public void onIngredientAdded(Ingredient newIngredient) {
        viewModel.addOrUpdateIngredient(newIngredient);
    }

    @Override
    public void onIngredientDelete(Ingredient ingredient) {
        viewModel.deleteIngredient(ingredient);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.setAdapter(null);  // Release adapter to prevent memory leaks
        }
    }
}
