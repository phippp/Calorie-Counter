package com.example.caloriecounter.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.caloriecounter.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class FoodListAdapter extends ArrayAdapter<FoodItem> {

    private Context mContext;
    int mResource;

    public FoodListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<FoodItem> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String name = getItem(position).getName();
        JSONObject data = getItem(position).getData();
        double calories = getItem(position).getCalories();

        FoodItem food = new FoodItem(name,data,calories);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = convertView.findViewById(R.id.food_name);
        tvName.setText(name);
        TextView tvCal = convertView.findViewById(R.id.food_calories);
        tvCal.setText(calories +" calories per serving");

        return convertView;
    }
}
