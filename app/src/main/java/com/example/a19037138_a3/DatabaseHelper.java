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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mealPlanner.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY, name TEXT, type TEXT, date TEXT)");
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY, mealId INTEGER, name TEXT, quantity INTEGER, category TEXT, " +
                "FOREIGN KEY (mealId) REFERENCES meals(id) ON DELETE CASCADE)");
    }

    public Map<String, List<Ingredient>> getCategorizedIngredients() {
        Map<String, List<Ingredient>> categorizedIngredients = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ingredients", null);

        if (cursor.moveToFirst()) {
            do {
                Ingredient ingredient = new Ingredient(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")) // Corrected: quantity as int
                );

                String category = ingredient.getCategory();
                categorizedIngredients
                        .computeIfAbsent(category, k -> new ArrayList<>())
                        .add(ingredient);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categorizedIngredients;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS meals");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        onCreate(db);
    }

    public long addMeal(String name, String type, String date, List<Ingredient> ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("type", type);
        values.put("date", date);

        long mealId = db.insert("meals", null, values);
        Log.d("DatabaseHelper", "Inserted meal: " + name + ", Type: " + type + ", Date: " + date);

        if (ingredients != null) {
            for (Ingredient ingredient : ingredients) {
                addIngredient(mealId, ingredient.getName(), ingredient.getQuantity(), ingredient.getCategory());
            }
        }
        return mealId;
    }


    public void addIngredient(long mealId, String name, int quantity, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mealId", mealId);
        values.put("name", capitalizeWords(name));
        values.put("quantity", quantity); // Store quantity as an integer
        values.put("category", category);

        db.insert("ingredients", null, values);
    }

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

                Meal meal = new Meal(id, name, mealType, mealDate);
                meals.add(meal);

                Log.d("DatabaseHelper", "Retrieved Meal: " + name + ", Type: " + mealType + ", Date: " + mealDate);
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No meals found for Date: " + date + ", Type: " + type);
        }

        if (cursor != null) {
            cursor.close();
        }

        return meals;
    }



    public void deleteMeal(long mealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ingredients", "mealId = ?", new String[]{String.valueOf(mealId)});
        db.delete("meals", "id = ?", new String[]{String.valueOf(mealId)});
    }

    public String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.toLowerCase().split(" ");
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedWords.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return capitalizedWords.toString().trim();
    }

    public void deleteOldMeals() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get today's date in 'yyyy-MM-dd' format
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        // Delete all ingredients associated with old meals
        db.execSQL("DELETE FROM ingredients WHERE mealId IN (SELECT id FROM meals WHERE date < ?)", new String[]{today});

        // Delete old meals
        db.delete("meals", "date < ?", new String[]{today});
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM ingredients");
            db.execSQL("DELETE FROM meals");
            Log.d("DatabaseHelper", "All meals and ingredients cleared from the database.");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error clearing the database: ", e);
        } finally {
            db.close();
        }
    }

}
