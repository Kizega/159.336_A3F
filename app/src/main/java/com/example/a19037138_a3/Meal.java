package com.example.a19037138_a3;

public class Meal {
    private long id;
    private String name;
    private String type;
    private String date;

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
