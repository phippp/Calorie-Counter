package com.example.caloriecounter.model;

import org.json.JSONObject;

public class FoodItem {

    private String name;
    private JSONObject data;
    private double calories;

    public FoodItem(String name, JSONObject object, double cal){
        this.name = name;
        this.data = object;
        this.calories = cal;
    }

    public double getCalories() {
        return calories;
    }

    public String getName() {
        return name;
    }

    public JSONObject getData() {
        return data;
    }

}
