package com.campusbite.menu;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "menu_items")
public class MenuItem {
    @Id
    private String id;
    private String name;
    private String description;
    private String category;
    private double price;
    private int stock;
    @Indexed
    private String outlet;
    private String imageUrl;
    private String emoji;
    private boolean available;
    private Instant createdAt;
    private Instant updatedAt;

    public MenuItem() {
    }

    public MenuItem(String name, String description, String category, double price,
            int stock, String outlet, String imageUrl, boolean available) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.outlet = outlet;
        this.imageUrl = imageUrl;
        this.available = available;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getOutlet() {
        return outlet;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public boolean isAvailable() {
        return available;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String name, String description, String category, double price,
            String outlet, String imageUrl, boolean available) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.outlet = outlet;
        this.imageUrl = imageUrl;
        this.available = available;
        this.updatedAt = Instant.now();
    }

    public void setStock(int stock) {
        this.stock = stock;
        this.updatedAt = Instant.now();
    }

    public void setAvailable(boolean available) {
        this.available = available;
        this.updatedAt = Instant.now();
    }
}
