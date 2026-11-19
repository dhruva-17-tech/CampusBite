package com.campusbite.orders;

public class OrderLine {
    private String menuItemId;
    private String name;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public OrderLine() {
    }

    public OrderLine(String menuItemId, String name, int quantity, double unitPrice) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }
}
