package com.example.caloriecounter.activities.fragments;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.data.DataProvider;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private TextView cc;
    private TextView wc;
    private TextView foodOutOf;

    private int userId;
    private int water;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //get adjustable text values
        cc = getView().findViewById(R.id.calorie_counter_home);
        wc = getView().findViewById(R.id.water_counter_home);
        foodOutOf = getView().findViewById(R.id.calorie_out_home);
        //get containers and set onClick
        ConstraintLayout foodLayout = getView().findViewById(R.id.calorie_layout_home);
        ConstraintLayout waterLayout = getView().findViewById(R.id.water_layout_home);
        foodLayout.setOnClickListener(this);
        waterLayout.setOnClickListener(this);
        //get buttons and set onClick
        Button addFood = getView().findViewById(R.id.calorie_button_home);
        Button decrease = getView().findViewById(R.id.water_decrease_home);
        Button increase = getView().findViewById(R.id.water_increase_home);
        addFood.setOnClickListener(this);
        decrease.setOnClickListener(this);
        increase.setOnClickListener(this);
        //get userId from parent activity
        userId = ((MyApp) requireActivity()).getUser_id();
        //update values
        updateCalories();
        updateWater();
    }

    @Override
    public void onStart() {
        super.onStart();
        //set value for the calorie target
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        foodOutOf.setText(getString(R.string.calories, Integer.parseInt(pref.getString("calories_goal", "2000"))));
    }

    @Override
    public void onResume() {
        super.onResume();
        //set value for the calorie target
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        foodOutOf.setText(getString(R.string.calories, Integer.parseInt(pref.getString("calories_goal", "2000"))));
    }

    private void updateCalories() {
        double calories = 0;
        //get all food items
        Cursor c = requireActivity().getContentResolver().query(
                DataProvider.URI_CALORIES,
                null,
                DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?",
                new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(new Date())},
                null
        );
        //sum of all items values
        while(c.moveToNext()){
            calories += c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
        }
        //free memory
        c.close();
        //set text
        cc.setText(String.valueOf((int)calories));
    }

    private void updateWater(){
        water = 0;
        //gets all water items for that day (should be one)
        Cursor c = requireActivity().getContentResolver().query(
                DataProvider.URI_WATER,
                null,
                DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy",Locale.UK).format(new Date())},
                null
        );
        //set water count
        while(c.moveToNext()){
            water += c.getInt(c.getColumnIndex(DataProvider.COLUMN_WATER_VALUE));
        }
        //free memory
        c.close();
        //set text
        wc.setText(String.valueOf(water));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.water_decrease_home){
            //if decrease button clicked
            if(water > 0) {
                //if can have water subtracted
                ContentValues values = new ContentValues();
                values.put(DataProvider.COLUMN_WATER_VALUE, this.water -1);
                //update database via content provider
                requireActivity().getContentResolver().update(
                        DataProvider.URI_WATER,
                        values,
                        DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                        new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy",Locale.UK).format(new Date())}
                );
                //update screen
                updateWater();
            } else {
                //if the value is 0 send a error message
                Snackbar.make(getView().findViewById(R.id.container),"Error, can't process",Snackbar.LENGTH_SHORT).show();
            }
        }else if(v.getId() == R.id.water_increase_home){
            //if increase button clicked
            boolean exists = false;
            //check if there is an entry in the table (can be an entry with value 0)
            Cursor c = requireActivity().getContentResolver().query(
                    DataProvider.URI_WATER,
                    null,
                    DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                    new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy",Locale.UK).format(new Date())},
                    null
            );
            if(c.getCount() > 0){
                exists = true;
            }
            //free memory
            c.close();

            ContentValues values = new ContentValues();
            if(!exists){
                //no value exists
                values.put(DataProvider.COLUMN_WATER_VALUE,1);
                values.put(DataProvider.COLUMN_WATER_USER_ID,userId);
                values.put(DataProvider.COLUMN_WATER_DATE, new SimpleDateFormat("dd-MM-yyyy",Locale.UK).format(new Date()));
                //insert new data into database
                getActivity().getContentResolver().insert(DataProvider.URI_WATER,values);
            } else {
                //value exists
                values.put(DataProvider.COLUMN_WATER_VALUE, water + 1);
                //update database with content provider
                getActivity().getContentResolver().update(
                        DataProvider.URI_WATER,
                        values,
                        DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                        new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy",Locale.UK).format(new Date())}
                );
            }
            //update screen
            updateWater();
        }else if(v.getId() == R.id.calorie_button_home || v.getId() == R.id.calorie_layout_home){
            //move to the food fragment
            Navigation.findNavController(v).navigate(R.id.food_fragment);
        }else if(v.getId() == R.id.water_layout_home){
            //move to the water fragment
            Navigation.findNavController(v).navigate(R.id.water_fragment);
        }
    }


}