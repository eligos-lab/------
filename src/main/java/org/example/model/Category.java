package org.example.model;

public class Category {
    private Long id;
    private String name;
    private CategoryType type;
    private String color;

    public Category() {}

    public Category(Long id, String name, CategoryType type, String color) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
    }

    public Category(String name, CategoryType type, String color) {
        this(null, name, type, color);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return name;
    }
}