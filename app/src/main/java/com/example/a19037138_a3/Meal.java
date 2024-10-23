package com.example.a19037138_a3;

public class Meal {
    // Fields that won't change once set are marked as final.
    private final long id;
    private final String name;
    private final String type;
    private final String date;

    // Constructor to initialize all meal details.
    public Meal(long id, String name, String type, String date) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
    }

    // Getters to access private fields.
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Currently unused but kept for future use.
    public String getType() {
        return type;
    }

    // Currently unused but kept for future use.
    public String getDate() {
        return date;
    }
}
