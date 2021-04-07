package com.example.caloriecounter.activities.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.adapters.DatabaseFoodListAdapter;
import com.example.caloriecounter.model.adapters.FoodItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private TextView calorieTitle;
    private TextView waterInfo;
    private TextView waterMessage;

    public ArrayList<FoodItem> listItems= new ArrayList<>();
    public DatabaseFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //create views
        calorieTitle = getView().findViewById(R.id.today_calories);
        waterInfo = getView().findViewById(R.id.water_today);
        waterMessage = getView().findViewById(R.id.water_message);

        //create adapter
        if(((MyApp)requireActivity()).isDark()) {
            adapter = new DatabaseFoodListAdapter(getActivity().getApplicationContext(), R.layout.database_food_item_dark, listItems);
        } else {
            adapter = new DatabaseFoodListAdapter(getActivity().getApplicationContext(), R.layout.database_food_item, listItems);
        }
        ListView lv = (ListView) getView().findViewById(R.id.recycler_list);
        lv.setAdapter(adapter);

        updateStats();
    }

    private void updateStats() {
        //get user id from parent activity
        int user_id = ((MyApp) getActivity()).getUser_id();
       //get all food items
        Cursor c = getActivity().getContentResolver().query(
                DataProvider.URI_CALORIES,
                null,
                DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?",
                new String[]{String.valueOf(user_id), new SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(new Date())},
                null
        );
        //add food items to adapter and calculate calories
        double calories = 0;
        while(c.moveToNext()){
            JSONObject obj = new JSONObject();
            String name = "";
            double val = c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
            try{
                obj = new JSONObject(c.getString(c.getColumnIndex(DataProvider.COLUMN_CALORIES_DATA)));
                name = obj.getJSONObject("food").getString("label");
            }catch(JSONException e){
                e.printStackTrace();
            }
            adapter.add(new FoodItem(name,obj,val));
            calories += val;
        }
        //free memory
        c.close();
        //get water
        c = getActivity().getContentResolver().query(
                DataProvider.URI_WATER,
                null,
                DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                new String[]{String.valueOf(user_id), new SimpleDateFormat("dd-MM-yyyy",Locale.UK).format(new Date())},
                null
        );
        //update water
        int water = 0;
        while(c.moveToNext()){
            water = c.getInt(c.getColumnIndex(DataProvider.COLUMN_WATER_VALUE));
        }
        //free memory
        c.close();
        //display motivational message for water
        if(water < 8){
            waterMessage.setVisibility(View.VISIBLE);
        } else {
            waterMessage.setVisibility(View.GONE);
        }
        //update values on screen
        waterInfo.setText(getResources().getString(R.string.num_water,water));
        calorieTitle.setText(getResources().getString(R.string.calories_consumed,(int)calories));
    }
}