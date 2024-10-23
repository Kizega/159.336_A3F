package com.example.a19037138_a3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database constants
    private static final String DATABASE_NAME = "mealPlanner.db";
    private static final int DATABASE_VERSION = 2;

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create tables when the database is first created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY, name TEXT, type TEXT, date TEXT)");
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY, mealId INTEGER, name TEXT, quantity INTEGER, category TEXT, " +
                "FOREIGN KEY (mealId) REFERENCES meals(id) ON DELETE CASCADE)");
    }

    // Upgrade database by dropping old tables and creating new ones
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS meals");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        onCreate(db);
    }

    // Add a new meal along with its ingredients to the database
    public long addMeal(String name, String type, String date, List<Ingredient> ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("type", type);
        values.put("date", date);

        // Insert the meal and get the generated ID
        long mealId = db.insert("meals", null, values);
        Log.d("DatabaseHelper", "Inserted meal: " + name + ", Type: " + type + ", Date: " + date);

        // Add all ingredients related to this meal
        if (ingredients != null) {
            for (Ingredient ingredient : ingredients) {
                addIngredient((int) mealId, ingredient.getName(), ingredient.getQuantity(), ingredient.getCategory());
            }
        }
        return mealId;
    }

    // Add an ingredient to the database
    public void addIngredient(int mealId, String name, int quantity, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Capitalize the name before saving it
        String capitalizedName = capitalizeWord(name);

        values.put("mealId", mealId);
        values.put("name", capitalizedName);
        values.put("quantity", quantity);
        values.put("category", category);

        db.insert("ingredients", null, values);
        db.close();
    }

    // Update the quantity of an existing ingredient
    public void updateIngredient(Ingredient ingredient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", ingredient.getQuantity());

        db.update("ingredients", values, "name=? AND category=?",
                new String[]{ingredient.getName(), ingredient.getCategory()});
        db.close();
    }

    // Retrieve a list of meals by date and type
    public List<Meal> getMealsByDateAndType(String date, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Meal> meals = new ArrayList<>();

        String query = "SELECT * FROM meals WHERE date = ? AND type = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date, type});
        Log.d("DatabaseHelper", "Querying meals for Date: " + date + ", Type: " + type);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String mealType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String mealDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                meals.add(new Meal(id, name, mealType, mealDate));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return meals;
    }

    // Delete a meal and its associated ingredients from the database
    public void deleteMeal(long mealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ingredients", "mealId = ?", new String[]{String.valueOf(mealId)});
        db.delete("meals", "id = ?", new String[]{String.valueOf(mealId)});
        Log.d("DatabaseHelper", "Meal and its ingredients deleted with ID: " + mealId);
    }

    // Delete all old meals (before today's date)
    public void deleteOldMeals() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        db.execSQL("DELETE FROM ingredients WHERE mealId IN (SELECT id FROM meals WHERE date < ?)",
                new String[]{today});
        db.delete("meals", "date < ?", new String[]{today});
    }

    @Deprecated  // This indicates it's for development/debugging purposes only.
    public void clearDatabase() {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.execSQL("DELETE FROM ingredients");
            db.execSQL("DELETE FROM meals");
            Log.d("DatabaseHelper", "All data cleared from the database.");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error clearing the database: ", e);
        }
    }


    // Get ingredients by category from the database
    public List<Ingredient> getIngredientsByCategory(String category) {
        List<Ingredient> ingredients = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM ingredients WHERE category = ?", new String[]{category})) {
            if (cursor.moveToFirst()) {
                do {
                    ingredients.add(new Ingredient(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching ingredients by category", e);
        }
        return ingredients;
    }

    // Get ingredients matching a name query
    public List<Ingredient> getIngredientsByName(String query) {
        List<Ingredient> ingredients = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM ingredients WHERE name LIKE ?",
                     new String[]{"%" + query + "%"})) {
            if (cursor.moveToFirst()) {
                do {
                    ingredients.add(new Ingredient(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving ingredients by name", e);
        }
        return ingredients;
    }

    // Get all ingredients from the database
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM ingredients", null)) {
            if (cursor.moveToFirst()) {
                do {
                    ingredients.add(new Ingredient(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving all ingredients", e);
        }
        return ingredients;
    }

    // Delete an ingredient by its ID
    public void deleteIngredient(int ingredientId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete("ingredients", "id = ?", new String[]{String.valueOf(ingredientId)});
            Log.d("DatabaseHelper", "Ingredient deleted with ID: " + ingredientId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting ingredient", e);
        }
    }

    // Capitalize the first letter of a word
    public String capitalizeWord(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
