package com.example.a19037138_a3;

/**
 * Represents a meal with an ID, name, type, and date.
 */
public class Meal {

    private final long id;
    private final String name;
    private final String type;
    private final String date;

    /**
     * Initializes a new Meal object with the given details.
     */
    public Meal(long id, String name, String type, String date) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }
}
