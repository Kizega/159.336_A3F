package com.example.a19037138_a3;

public class Ingredient {
    private String name;
    private String quantity;
    private String category;

    public Ingredient(String name, String quantity, String category) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getCategory() {
        return category;
    }
}
