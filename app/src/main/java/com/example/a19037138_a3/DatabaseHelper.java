package com.example.a19037138_a3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mealPlanner.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create meals table
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY, name TEXT, type TEXT, date TEXT)");

        // Create ingredients table with category column
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY, mealId INTEGER, name TEXT, quantity TEXT, category TEXT, " +
                "FOREIGN KEY (mealId) REFERENCES meals(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables
        db.execSQL("DROP TABLE IF EXISTS meals");
        db.execSQL("DROP TABLE IF EXISTS ingredients");

        // Recreate tables with new schema
        onCreate(db);
    }

    // Add a meal and return its id (Primary Key)
    public long addMeal(String name, String type, String date, List<Ingredient> ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("type", type);
        values.put("date", date);

        // Insert the meal and get the mealId
        long mealId = db.insert("meals", null, values);

        // Insert each ingredient linked to this meal
        if (ingredients != null) {
            for (Ingredient ingredient : ingredients) {
                addIngredient(mealId, ingredient.getName(), ingredient.getQuantity(), ingredient.getCategory());
            }
        }

        return mealId;
    }

    public void addIngredient(long mealId, String name, String quantity, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mealId", mealId);
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("category", category);

        db.insert("ingredients", null, values);
    }

    // Get a categorized shopping list
    public Map<String, List<String>> getShoppingListByCategory() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, List<String>> shoppingList = new HashMap<>();

        // Initialize categories
        shoppingList.put("Vegetables", new ArrayList<>());
        shoppingList.put("Meat", new ArrayList<>());
        shoppingList.put("Other", new ArrayList<>());

        // Query to get ingredients grouped by name and category, summing quantities
        String query = "SELECT name, category, SUM(CAST(quantity AS INTEGER)) as totalQuantity FROM ingredients GROUP BY name, category";
        Cursor cursor = db.rawQuery(query, null);
        String[] columnNames = cursor.getColumnNames();
        for (String columnName : columnNames) {
            Log.d("DatabaseHelper", "Column: " + columnName);
        }

        if (cursor.moveToFirst()) {
            do {
                String ingredientName = cursor.getString(cursor.getColumnIndex("name"));
                String category = cursor.getString(cursor.getColumnIndex("category"));
                int totalQuantity = cursor.getInt(cursor.getColumnIndex("totalQuantity"));

                String displayText = ingredientName + " Qty: " + totalQuantity;
                shoppingList.get(category).add(displayText);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return shoppingList;
    }

    // Get weekly meals
    public List<Meal> getWeeklyMeals() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Meal> meals = new ArrayList<>();

        String query = "SELECT * FROM meals WHERE date BETWEEN date('now') AND date('now', '+7 days') ORDER BY date ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");
            int typeIndex = cursor.getColumnIndex("type");
            int dateIndex = cursor.getColumnIndex("date");

            if (idIndex != -1 && nameIndex != -1 && typeIndex != -1 && dateIndex != -1) {
                do {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    String type = cursor.getString(typeIndex);
                    String date = cursor.getString(dateIndex);

                    Meal meal = new Meal(id, name, type, date);
                    meals.add(meal);
                } while (cursor.moveToNext());
            }
        }

        cursor.close();
        return meals;
    }

    // Delete a meal and its associated ingredients
    public void deleteMeal(long mealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ingredients", "mealId = ?", new String[] { String.valueOf(mealId) });
        db.delete("meals", "id = ?", new String[] { String.valueOf(mealId) });
    }
}