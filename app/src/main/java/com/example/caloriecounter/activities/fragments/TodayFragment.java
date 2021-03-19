package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
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
import com.example.caloriecounter.model.adapters.DatabaseFoodListAdapter;
import com.example.caloriecounter.model.adapters.FoodItem;
import com.example.caloriecounter.sql.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class TodayFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private TextView calorieTitle;
    private TextView waterInfo;
    private TextView waterMessage;

    private int user_id;

    public ArrayList<FoodItem> listItems= new ArrayList<>();
    public DatabaseFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_today, container, false);
        //get views
        calorieTitle = v.findViewById(R.id.today_calories);
        waterInfo = v.findViewById(R.id.water_today);
        waterMessage = v.findViewById(R.id.water_message);
        //create adapter
        adapter = new DatabaseFoodListAdapter(getActivity().getApplicationContext(),R.layout.database_food_item,listItems);
        ListView lv = (ListView) v.findViewById(R.id.list_for_database);
        lv.setAdapter(adapter);
        //return view
        return v;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateStats();
    }

    private void updateStats() {
        dbHelper = new DatabaseHelper(getActivity());

        user_id = ((MyApp) getActivity()).getUser_id();
        if(user_id != -1) {
            double calories = dbHelper.getTotalFood(user_id, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            int water = dbHelper.getWater(user_id,new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            if(water < 8){
                waterMessage.setVisibility(View.VISIBLE);
            } else {
                waterMessage.setVisibility(View.GONE);
            }
            waterInfo.setText(water+ " glasses of water");
            calorieTitle.setText("You have consumed:\n"+String.format("%.2f",calories)+"Kcal");
        }
        FoodItem[] food = dbHelper.getFoodItems(user_id, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        for(int i = 0; i < food.length; i++){
            adapter.add(food[i]);
        }
    }
}