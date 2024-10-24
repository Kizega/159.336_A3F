package com.example.a19037138_a3;

/**
 * Represents an ingredient with an ID, name, category, and quantity.
 */
public class Ingredient {

    private final int id;
    private final String name;
    private final String category;
    private int quantity;

    /**
     * Creates a new ingredient.
     */
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
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
