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

    // Singleton instance
    private static DatabaseHelper instance;

    // Database constants
    private static final String DATABASE_NAME = "mealPlanner.db";
    private static final int DATABASE_VERSION = 2;

    // Private constructor to prevent direct instantiation
    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Get the singleton instance of DatabaseHelper
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY, name TEXT, type TEXT, date TEXT)");
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY, mealId INTEGER, name TEXT, quantity INTEGER, category TEXT, " +
                "FOREIGN KEY (mealId) REFERENCES meals(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS meals");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        onCreate(db);
    }

    // Add a new meal with its ingredients using a transaction
    public long addMeal(String name, String type, String date, List<Ingredient> ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("type", type);
            values.put("date", date);

            long mealId = db.insert("meals", null, values);
            if (ingredients != null) {
                for (Ingredient ingredient : ingredients) {
                    addIngredient((int) mealId, ingredient.getName(), ingredient.getQuantity(), ingredient.getCategory());
                }
            }
            db.setTransactionSuccessful();
            return mealId;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding meal", e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    // Add an ingredient to the database
    public void addIngredient(int mealId, String name, int quantity, String category) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("mealId", mealId);
            values.put("name", capitalizeWord(name));
            values.put("quantity", quantity);
            values.put("category", category);

            db.insert("ingredients", null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding ingredient", e);
        }
    }

    // Retrieve meals by date and type
    public List<Meal> getMealsByDateAndType(String date, String type) {
        List<Meal> meals = new ArrayList<>();
        String query = "SELECT id, name, type, date FROM meals WHERE date = ? AND type = ?";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{date, type})) {

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String mealType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String mealDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                    meals.add(new Meal(id, name, mealType, mealDate));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving meals", e);
        }
        return meals;
    }

    // Delete a meal and its associated ingredients
    public void deleteMeal(long mealId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete("ingredients", "mealId = ?", new String[]{String.valueOf(mealId)});
            db.delete("meals", "id = ?", new String[]{String.valueOf(mealId)});
            Log.d("DatabaseHelper", "Meal deleted: ID " + mealId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting meal", e);
        }
    }

    // Delete old meals
    public void deleteOldMeals() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.execSQL("DELETE FROM ingredients WHERE mealId IN (SELECT id FROM meals WHERE date < ?)", new String[]{today});
            db.delete("meals", "date < ?", new String[]{today});
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting old meals", e);
        }
    }

    // Clear all data (for debugging)
    @Deprecated
    public void clearDatabase() {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM ingredients");
                db.execSQL("DELETE FROM meals");
                db.setTransactionSuccessful();
                Log.d("DatabaseHelper", "Database cleared");
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error clearing database", e);
        }
    }

    // Get all ingredients
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT id, name, category, quantity FROM ingredients";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, null)) {

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

    // Get ingredients by name
    public List<Ingredient> getIngredientsByName(String query) {
        List<Ingredient> ingredients = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM ingredients WHERE name LIKE ?", new String[]{"%" + query + "%"})) {

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

    // Get ingredients by category
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
            Log.e("DatabaseHelper", "Error retrieving ingredients by category", e);
        }
        return ingredients;
    }

    // Delete an ingredient by ID
    public void deleteIngredient(int ingredientId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete("ingredients", "id = ?", new String[]{String.valueOf(ingredientId)});
            Log.d("DatabaseHelper", "Ingredient deleted: ID " + ingredientId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting ingredient", e);
        }
    }

    // Capitalize the first letter of a word
    public String capitalizeWord(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    // Close the database instance
    public static synchronized void closeDatabase() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}
