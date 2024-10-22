package com.example.a19037138_a3;

public class Ingredient {
    private int id;
    private String name;
    private String category;
    private int quantity;

    public Ingredient(int id, String name, String category, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getId() {
        return id;
    }
}
