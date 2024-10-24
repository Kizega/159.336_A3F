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

/**
 * DatabaseHelper class for managing the meal planner database.
 * Implements a singleton pattern to ensure a single instance of the database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Singleton instance
    private static DatabaseHelper instance;

    // Database constants
    private static final String DATABASE_NAME = "mealPlanner.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = "DatabaseHelper";

    // Private constructor to prevent multiple instances
    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "DatabaseHelper instance created.");
    }

    /**
     * Returns the singleton instance of DatabaseHelper.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
            SQLiteDatabase db = instance.getWritableDatabase();
            if (!db.isWriteAheadLoggingEnabled()) {
                db.enableWriteAheadLogging();
                Log.i(TAG, "Write-Ahead Logging enabled.");
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating database tables.");
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY, name TEXT, type TEXT, date TEXT)");
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY, mealId INTEGER, name TEXT, quantity INTEGER, category TEXT, " +
                "FOREIGN KEY (mealId) REFERENCES meals(id) ON DELETE CASCADE)");
        Log.i(TAG, "Database tables created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS meals");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        onCreate(db);
    }

    /**
     * Adds a meal with its ingredients using a transaction.
     */
    public long addMeal(String name, String type, String date, List<Ingredient> ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("type", type);
            values.put("date", date);

            long mealId = db.insert("meals", null, values);
            Log.i(TAG, "Meal added with ID: " + mealId);

            if (ingredients != null) {
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.getName() != null && !ingredient.getName().trim().isEmpty()) {
                        addIngredient(db, (int) mealId, ingredient.getName(), ingredient.getQuantity(), ingredient.getCategory());
                    }
                }
            }

            db.setTransactionSuccessful();
            return mealId;
        } catch (Exception e) {
            Log.e(TAG, "Error adding meal", e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }
    public void addOrUpdateIngredient(String name, String category, int quantity) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            // Check if the ingredient already exists in the table.
            String query = "SELECT id, quantity FROM ingredients WHERE name = ? AND category = ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{name, category})) {
                if (cursor.moveToFirst()) {
                    // Ingredient exists, update its quantity.
                    int existingId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));

                    ContentValues values = new ContentValues();
                    values.put("quantity", existingQuantity + quantity);  // Add to existing quantity

                    db.update("ingredients", values, "id = ?", new String[]{String.valueOf(existingId)});
                    Log.i(TAG, "Updated ingredient with ID: " + existingId + ", new quantity: " + (existingQuantity + quantity));
                } else {
                    // Ingredient doesn't exist, insert a new one.
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("category", category);
                    values.put("quantity", quantity);

                    long newId = db.insert("ingredients", null, values);
                    Log.i(TAG, "Inserted new ingredient with ID: " + newId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding or updating ingredient", e);
        }
    }


    /**
     * Adds an ingredient to a specific meal.
     */
    public void addIngredient(SQLiteDatabase db, int mealId, String name, int quantity, String category) {
        ContentValues values = new ContentValues();
        values.put("mealId", mealId);
        values.put("name", capitalizeWord(name));
        values.put("quantity", quantity);
        values.put("category", category);

        long result = db.insert("ingredients", null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to insert ingredient: " + name);
        } else {
            Log.i(TAG, "Ingredient added: " + name);
        }
    }

    /**
     * Retrieves consolidated ingredients by summing quantities with the same name and category.
     */
    public List<Ingredient> getConsolidatedIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, null)) {

            while (cursor.moveToNext()) {
                // Ensure the ID is properly assigned here
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));

                // Create the Ingredient object with the correct ID
                ingredients.add(new Ingredient(id, name, category, quantity));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving ingredients", e);
        }
        return ingredients;
    }

    /**
     * Retrieves meals based on date and type.
     */
    public List<Meal> getMealsByDateAndType(String date, String type) {
        List<Meal> meals = new ArrayList<>();
        String query = "SELECT id, name, type, date FROM meals WHERE date = ? AND type = ?";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{date, type})) {

            while (cursor.moveToNext()) {
                meals.add(new Meal(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving meals", e);
        }
        return meals;
    }

    /**
     * Deletes a meal and its associated ingredients.
     */
    public void deleteMeal(long mealId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete("ingredients", "mealId = ?", new String[]{String.valueOf(mealId)});
            db.delete("meals", "id = ?", new String[]{String.valueOf(mealId)});
            Log.i(TAG, "Meal deleted: ID " + mealId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting meal", e);
        }
    }

    /**
     * Deletes old meals based on the current date.
     */
    public void deleteOldMeals() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.execSQL("DELETE FROM ingredients WHERE mealId IN (SELECT id FROM meals WHERE date < ?)", new String[]{today});
            db.delete("meals", "date < ?", new String[]{today});
            Log.i(TAG, "Old meals deleted.");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting old meals", e);
        }
    }

    /**
     * Retrieves all ingredients.
     */
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT id, name, category, quantity FROM ingredients";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, null)) {

            while (cursor.moveToNext()) {
                ingredients.add(new Ingredient(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving ingredients", e);
        }
        return ingredients;
    }

    /**
     * Capitalizes the first letter of a word.
     */
    public String capitalizeWord(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    /**
     * Closes the database and resets the instance.
     */
    public static synchronized void closeDatabase() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
    /**
     * Deletes an ingredient by its ID.
     */

    public void deleteIngredientByNameAndCategory(String name, String category) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            int rows = db.delete("ingredients", "name = ? AND category = ?", new String[]{name, category});
            Log.i(TAG, "Deleted " + rows + " row(s) from ingredients with name: " + name + " and category: " + category);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting ingredient", e);
        }
    }



    /**
     * Retrieves ingredients by category.
     */
    public List<Ingredient> getIngredientsByCategory(String category) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE category = ?";
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{category})) {

            while (cursor.moveToNext()) {
                ingredients.add(new Ingredient(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving ingredients by category", e);
        }
        return ingredients;
    }

    /**
     * Searches for ingredients by name.
     */
    public List<Ingredient> getIngredientsByName(String query) {
        List<Ingredient> ingredients = new ArrayList<>();
        String sqlQuery = "SELECT * FROM ingredients WHERE name LIKE ?";
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(sqlQuery, new String[]{"%" + query + "%"})) {

            while (cursor.moveToNext()) {
                ingredients.add(new Ingredient(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving ingredients by name", e);
        }
        return ingredients;
    }
    public void updateIngredientQuantity(int ingredientId, int newQuantity) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("quantity", newQuantity);

            int rows = db.update("ingredients", values, "id = ?", new String[]{String.valueOf(ingredientId)});
            Log.i("DatabaseHelper", "Updated " + rows + " row(s) with new quantity: " + newQuantity);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating ingredient quantity", e);
        }
    }
    public Ingredient getIngredientByNameAndCategory(String name, String category) {
        Ingredient ingredient = null;
        String query = "SELECT * FROM ingredients WHERE name = ? AND category = ?";
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{name, category})) {

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String ingredientName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String ingredientCategory = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));

                ingredient = new Ingredient(id, ingredientName, ingredientCategory, quantity);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving ingredient", e);
        }
        return ingredient;
    }





}
