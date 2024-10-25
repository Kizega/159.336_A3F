package com.example.a19037138_a3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * DatabaseHelper class for managing the meal planner database.
 * Implements a singleton pattern to ensure a single instance is used.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // --- Constants and Singleton Setup ---
    private static final String DATABASE_NAME = "mealPlanner.db";
    private static final int DATABASE_VERSION = 2;

    private static DatabaseHelper instance;

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
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
            }
        }
        return instance;
    }

    // --- Lifecycle Methods ---

    /**
     * Creates the database tables when the database is first created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY, name TEXT, type TEXT, date TEXT)");
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY, mealId INTEGER, name TEXT, " +
                "quantity INTEGER, category TEXT, FOREIGN KEY (mealId) REFERENCES meals(id) ON DELETE CASCADE)");
    }

    /**
     * Upgrades the database by dropping existing tables and creating new ones.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS meals");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        onCreate(db);
    }

    /**
     * Closes the database and resets the instance to null.
     */
    public static synchronized void closeDatabase() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    // --- CRUD Operations ---

    /**
     * Adds a new meal and its ingredients to the database.
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

            if (ingredients != null) {
                for (Ingredient ingredient : ingredients) {
                    if (!ingredient.getName().trim().isEmpty()) {
                        addIngredient(db, (int) mealId, ingredient.getName(),
                                ingredient.getQuantity(), ingredient.getCategory());
                    }
                }
            }
            db.setTransactionSuccessful();
            return mealId;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Adds an ingredient to the database linked to a specific meal.
     */
    public void addIngredient(SQLiteDatabase db, int mealId, String name, int quantity, String category) {
        ContentValues values = new ContentValues();
        values.put("mealId", mealId);
        values.put("name", capitalizeWord(name));
        values.put("quantity", quantity);
        values.put("category", category);
        db.insert("ingredients", null, values);
    }

    /**
     * Adds or updates an ingredient based on its name and category.
     */
    public void addOrUpdateIngredient(String name, String category, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT id, quantity FROM ingredients WHERE name = ? AND category = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{name, category})) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));

                ContentValues values = new ContentValues();
                values.put("quantity", existingQuantity + quantity);
                db.update("ingredients", values, "id = ?", new String[]{String.valueOf(id)});
            } else {
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("category", category);
                values.put("quantity", quantity);
                db.insert("ingredients", null, values);
            }
        }
    }

    /**
     * Deletes a meal and its associated ingredients.
     */
    public void deleteMeal(long mealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ingredients", "mealId = ?", new String[]{String.valueOf(mealId)});
        db.delete("meals", "id = ?", new String[]{String.valueOf(mealId)});
    }

    /**
     * Deletes an ingredient by name and category.
     */
    public void deleteIngredientByNameAndCategory(String name, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ingredients", "name = ? AND category = ?", new String[]{name, category});
    }

    /**
     * Deletes meals that are older than today.
     */
    public void deleteOldMeals() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM ingredients WHERE mealId IN (SELECT id FROM meals WHERE date < ?)", new String[]{today});
        db.delete("meals", "date < ?", new String[]{today});
    }

    // --- Retrieval Methods ---

    /**
     * Retrieves meals by date and type.
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
        }
        return meals;
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
        }
        return ingredients;
    }

    /**
     * Retrieves a specific ingredient by name and category.
     */
    public Ingredient getIngredientByNameAndCategory(String name, String category) {
        String query = "SELECT * FROM ingredients WHERE name = ? AND category = ?";
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{name, category})) {
            if (cursor.moveToFirst()) {
                return new Ingredient(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                );
            }
        }
        return null;
    }

    /**
     * Retrieves consolidated ingredients by summing quantities with the same name and category.
     */
    public List<Ingredient> getConsolidatedIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, category, quantity FROM ingredients", null);
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
        cursor.close();
        return ingredients;
    }

    // --- Utility Methods ---

    /**
     * Updates the quantity of an ingredient by its ID.
     */
    public boolean updateIngredientQuantity(int id, int newQuantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", newQuantity);
        int rowsAffected = db.update("ingredients", values, "id = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    /**
     * Capitalizes the first letter of a word.
     */
    public String capitalizeWord(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
