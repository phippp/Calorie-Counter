package com.example.caloriecounter.activities.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.adapters.DatabaseFoodListAdapter;
import com.example.caloriecounter.model.adapters.FoodItem;
import com.example.caloriecounter.data.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TodayFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private TextView calorieTitle;
    private TextView waterInfo;
    private TextView waterMessage;

    private int user_id;

    private Button add;

    public ArrayList<FoodItem> listItems= new ArrayList<>();
    public DatabaseFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_today, container, false);
        //get views
        calorieTitle = v.findViewById(R.id.today_calories);
        waterInfo = v.findViewById(R.id.water_today);
        waterMessage = v.findViewById(R.id.water_message);
        add = v.findViewById(R.id.add);
        add.setOnClickListener(this::addFunction);
        //create adapter
        if(((MyApp)requireActivity()).isDark()) {
            adapter = new DatabaseFoodListAdapter(getActivity().getApplicationContext(), R.layout.database_food_item_dark, listItems);
        } else {
            adapter = new DatabaseFoodListAdapter(getActivity().getApplicationContext(), R.layout.database_food_item, listItems);
        }
        ListView lv = (ListView) v.findViewById(R.id.recycler_list);
        lv.setAdapter(adapter);
        //return view
        return v;
    }

    private void addFunction(View view) {
        ContentValues values = new ContentValues();
        values.put(DataProvider.COLUMN_USER_USERNAME,"phippp");
        values.put(DataProvider.COLUMN_USER_PASSWORD,"yes");
        values.put(DataProvider.COLUMN_USER_EMAIL,"pvn1@btinternet.com");

        getActivity().getContentResolver().insert(DataProvider.URI_USER,values);

        Toast.makeText(getActivity().getBaseContext(), "New Record Inserted", Toast.LENGTH_LONG).show();
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateStats();
    }

    private void updateStats() {
        dbHelper = new DatabaseHelper(getActivity());
        FoodItem[] food;

        user_id = ((MyApp) getActivity()).getUser_id();
        if(user_id != -1) {
            Cursor c = getActivity().getContentResolver().query(
                    DataProvider.URI_CALORIES,
                    null,
                    DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?",
                    new String[]{String.valueOf(user_id), new SimpleDateFormat("dd-MM-yyyy").format(new Date())},
                    null
            );
            food = new FoodItem[c.getCount()];
            double calories = 0;
            int counter = 0;
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
                food[counter] = new FoodItem(name,obj,val);
                calories += val;
                counter++;
            }

            c.close();

            c = getActivity().getContentResolver().query(
                    DataProvider.URI_WATER,
                    null,
                    DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                    new String[]{String.valueOf(user_id), new SimpleDateFormat("dd-MM-yyyy").format(new Date())},
                    null
            );

            int water = 0;
            while(c.moveToNext()){
                water = c.getInt(c.getColumnIndex(DataProvider.COLUMN_WATER_VALUE));
            }

            if(water < 8){
                waterMessage.setVisibility(View.VISIBLE);
            } else {
                waterMessage.setVisibility(View.GONE);
            }
            waterInfo.setText(water+ " glasses of water");
            calorieTitle.setText("You have consumed:\n"+String.format("%.2f",calories)+"Kcal");

            for(int i = 0; i < food.length; i++){
                adapter.add(food[i]);
            }
        }
        //FoodItem[] food = dbHelper.getFoodItems(user_id, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

    }
}