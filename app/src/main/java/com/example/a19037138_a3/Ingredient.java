package com.example.a19037138_a3;

/**
 * Represents an ingredient with ID, name, category, and quantity.
 */
public class Ingredient {

    private final int id;         // Unique ID for the ingredient (unchangeable)
    private final String name;    // Name of the ingredient (unchangeable)
    private final String category; // Category like "Vegetable" or "Meat" (unchangeable)
    private final int quantity;         // Quantity of the ingredient (can change)

    /**
     * Constructor to create a new ingredient.
     *
     * @param id       Unique ID of the ingredient.
     * @param name     Name of the ingredient.
     * @param category Category the ingredient belongs to.
     * @param quantity Quantity of the ingredient.
     */
    public Ingredient(int id, String name, String category, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    // Get the name of the ingredient.
    public String getName() {
        return name;
    }

    // Get the category of the ingredient.
    public String getCategory() {
        return category;
    }

    // Get the quantity of the ingredient.
    public int getQuantity() {
        return quantity;
    }

    // Get the ID of the ingredient.
    public int getId() {
        return id;
    }

}
