package com.example.a19037138_a3;

/**
 * Represents a meal with an ID, name, type, and date.
 * Provides methods to access the meal's attributes.
 */
public class Meal {

    private final long id;
    private final String name;
    private final String type;
    private final String date;

    /**
     * Initializes a new Meal object with the given details.
     *
     * @param id    The unique ID of the meal.
     * @param name  The name of the meal.
     * @param type  The type of the meal (e.g., Breakfast, Lunch).
     * @param date  The date associated with the meal.
     */
    public Meal(long id, String name, String type, String date) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
    }

    /**
     * Retrieves the ID of the meal.
     *
     * @return The unique ID of the meal.
     */
    public long getId() {
        return id;
    }

    /**
     * Retrieves the name of the meal.
     *
     * @return The name of the meal.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the type of the meal (e.g., Breakfast, Lunch).
     *
     * @return The type of the meal.
     * @noinspection unused
     */
    public String getType() {
        return type;
    }

    /**
     * Retrieves the date associated with the meal.
     *
     * @return The date of the meal.
     * @noinspection unused
     */
    public String getDate() {
        return date;
    }
}
