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
import com.example.caloriecounter.model.database.Water;
import com.example.caloriecounter.data.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private DatabaseHelper dbHelper;
    private TextView cc;
    private TextView wc;
    private TextView foodOutOf;

    private ConstraintLayout waterLayout;
    private ConstraintLayout foodLayout;

    private int userId;
    private int water;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        //get views
        this.cc = v.findViewById(R.id.calorie_counter_home);
        this.wc = v.findViewById(R.id.water_counter_home);
        this.foodOutOf = v.findViewById(R.id.calorie_out_home);
        this.foodLayout = v.findViewById(R.id.calorie_layout_home);
        this.waterLayout = v.findViewById(R.id.water_layout_home);
        Button addFood = v.findViewById(R.id.calorie_button_home);
        Button decrease = v.findViewById(R.id.water_decrease_home);
        Button increase = v.findViewById(R.id.water_increase_home);
        //set onclicklisteners
        this.foodLayout.setOnClickListener(this);
        this.waterLayout.setOnClickListener(this);
        addFood.setOnClickListener(this);
        decrease.setOnClickListener(this);
        increase.setOnClickListener(this);
        //return view
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.userId = ((MyApp) requireActivity()).getUser_id();
        updateCalories();
        updateWater();
    }

    private void updateCalories() {
        this.dbHelper = new DatabaseHelper(getActivity());
        if(this.userId != -1) {

            double calories = 0;//db.getTotalFood(userId, df.format(cal.getTime()));

            Cursor c = getActivity().getContentResolver().query(
                    DataProvider.URI_CALORIES,
                    null,
                    DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?",
                    new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy").format(new Date())},
                    null
            );
            while(c.moveToNext()){
                calories += c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
            }

            this.cc.setText(String.valueOf((int)calories));
        }
        this.dbHelper.close();
    }

    private void updateWater(){
        this.dbHelper = new DatabaseHelper(getActivity());
        this.water = 0;

        Cursor c = getActivity().getContentResolver().query(
                DataProvider.URI_WATER,
                null,
                DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy").format(new Date())},
                null
        );
        while(c.moveToNext()){
            this.water += c.getInt(c.getColumnIndex(DataProvider.COLUMN_WATER_VALUE));
        }

        this.wc.setText(String.valueOf(water));
        this.dbHelper.close();
    }

    @Override
    public void onClick(View v) {
        this.dbHelper = new DatabaseHelper(getActivity());
        switch (v.getId()){
            case R.id.water_decrease_home:
                if(this.water > 0) {
                    //this.dbHelper.updateWater(this.userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),this.water-1);

                    ContentValues values = new ContentValues();
                    values.put(DataProvider.COLUMN_WATER_VALUE, this.water -1);

                    getActivity().getContentResolver().update(
                            DataProvider.URI_WATER,
                            values,
                            DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                            new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy").format(new Date())}
                    );

                    updateWater();
                } else {
                    Snackbar.make(getView().findViewById(R.id.container),"Error, can't process",Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.water_increase_home:

                boolean exists = false;

                Cursor c = getActivity().getContentResolver().query(
                        DataProvider.URI_WATER,
                        null,
                        DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                        new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy").format(new Date())},
                        null
                );
                if(c.getCount() > 0){
                    exists = true;
                }

                if(!exists){


                    ContentValues values = new ContentValues();
                    values.put(DataProvider.COLUMN_WATER_VALUE,1);
                    values.put(DataProvider.COLUMN_WATER_USER_ID,userId);
                    values.put(DataProvider.COLUMN_WATER_DATE, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

                    getActivity().getContentResolver().insert(DataProvider.URI_WATER,values);

                } else {
//                    this.dbHelper.updateWater(this.userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),this.water+1);

                    ContentValues values = new ContentValues();
                    values.put(DataProvider.COLUMN_WATER_VALUE, this.water + 1);

                    getActivity().getContentResolver().update(
                            DataProvider.URI_WATER,
                            values,
                            DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                            new String[]{String.valueOf(userId), new SimpleDateFormat("dd-MM-yyyy").format(new Date())}
                    );
                }
                updateWater();
                break;
            case R.id.calorie_button_home:
            case R.id.calorie_layout_home:
                Navigation.findNavController(v).navigate(R.id.food_fragment);
                break;
            case R.id.water_layout_home:
                Navigation.findNavController(v).navigate(R.id.water_fragment);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        this.foodOutOf.setText(getString(R.string.calories, Integer.parseInt(pref.getString("calories_goal", "2000"))));
    }
}