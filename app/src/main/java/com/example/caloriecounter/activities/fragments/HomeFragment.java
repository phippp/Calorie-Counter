package com.example.caloriecounter.activities.fragments;

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

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.model.database.Water;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private DatabaseHelper dbHelper;
    private TextView cc;
    private TextView wc;

    private Button increase;
    private Button decrease;
    private Button addFood;

    private ConstraintLayout waterLayout;
    private ConstraintLayout foodLayout;

    private int userId;
    private int water;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        cc = getActivity().findViewById(R.id.calorie_counter_home);
        wc = getActivity().findViewById(R.id.water_counter_home);
        foodLayout = getActivity().findViewById(R.id.calorie_layout_home);
        foodLayout.setOnClickListener(this);
        waterLayout = getActivity().findViewById(R.id.water_layout_home);
        waterLayout.setOnClickListener(this);
        addFood = getActivity().findViewById(R.id.calorie_button_home);
        addFood.setOnClickListener(this);
        decrease = getActivity().findViewById(R.id.water_decrease_home);
        decrease.setOnClickListener(this);
        increase = getActivity().findViewById(R.id.water_increase_home);
        increase.setOnClickListener(this);

        updateCalories();
        updateWater();
    }

    private void updateCalories() {
        dbHelper = new DatabaseHelper(getActivity());
        userId = ((MyApp) getActivity()).getUser_id();
        if(userId != -1) {
            double calories = dbHelper.getTotalFood(userId, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            cc.setText(String.valueOf((int)calories));
        }
        dbHelper.close();
    }

    private void updateWater(){
        dbHelper = new DatabaseHelper(getActivity());
        water = dbHelper.getWater(userId, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        wc.setText(String.valueOf(water));
        dbHelper.close();
    }

    @Override
    public void onClick(View v) {
        dbHelper = new DatabaseHelper(getActivity());
        switch (v.getId()){
            case R.id.water_decrease_home:
                if(water > 0) {
                    dbHelper.updateWater(userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),water-1);
                    updateWater();
                } else {
                    Snackbar.make(getView().findViewById(R.id.container),"Error, can't process",Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.water_increase_home:
                if(water == 0){
                    Water w = new Water();
                    w.setValue(1);
                    w.setUser_id(userId);
                    w.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                    dbHelper.addWater(w);
                } else {
                    dbHelper.updateWater(userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),water+1);
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

}