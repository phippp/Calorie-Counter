package com.example.caloriecounter.model.adapters;

import org.json.JSONObject;

public class FoodItem {

    private int id;
    private String name;
    private JSONObject data;
    private double calories;
    private boolean SHOW_MENU = false;

    public FoodItem(String name, JSONObject object, double cal){
        this.name = name;
        this.data = object;
        this.calories = cal;
    }

    public FoodItem(int id, String name, JSONObject object, double cal){
        this.id = id;
        this.name = name;
        this.data = object;
        this.calories = cal;
    }

    public int getId() {
        return id;
    }

    public boolean isShowMenu(){
        return this.SHOW_MENU;
    }

    public void updateMenu(boolean m){
        this.SHOW_MENU = m;
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
