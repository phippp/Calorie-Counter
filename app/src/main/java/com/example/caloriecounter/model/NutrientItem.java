package com.example.caloriecounter.model;

public class NutrientItem {

    private String name;
    private double qty;
    private String unit;

    public NutrientItem(String name, double qty, String unit){
        this.name = name;
        this.qty = qty;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQty() {
        return qty;
    }

    public String getUnit() {
        return unit;
    }
}
