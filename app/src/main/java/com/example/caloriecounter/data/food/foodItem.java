package com.example.caloriecounter.data.food;

import org.json.JSONObject;

public class foodItem {

    private String name;
    private JSONObject data;
    private double calories;

    public foodItem(String name, JSONObject object, double cal){
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
