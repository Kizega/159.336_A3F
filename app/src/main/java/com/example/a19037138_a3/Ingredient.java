package com.example.a19037138_a3;

/**
 * Represents an ingredient with an ID, name, category, and quantity.
 * Provides methods to retrieve and modify the ingredient's attributes.
 */
public class Ingredient {

    private final int id;
    private final String name;
    private final String category;
    private int quantity;

    /**
     * Creates a new ingredient with the specified attributes.
     *
     * @param id       The unique ID of the ingredient.
     * @param name     The name of the ingredient.
     * @param category The category of the ingredient.
     * @param quantity The quantity of the ingredient.
     */
    public Ingredient(int id, String name, String category, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    /**
     * Retrieves the name of the ingredient.
     *
     * @return The name of the ingredient.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the category of the ingredient.
     *
     * @return The category of the ingredient.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Retrieves the quantity of the ingredient.
     *
     * @return The current quantity of the ingredient.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Retrieves the unique ID of the ingredient.
     *
     * @return The ID of the ingredient.
     */
    public int getId() {
        return id;
    }

    /**
     * Updates the quantity of the ingredient.
     *
     * @param quantity The new quantity to set.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
