package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.model.database.Water;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private DatabaseHelper dbHelper;
    private TextView cc;
    private TextView wc;
    private TextView foodOutOf;

    private Button increase;
    private Button decrease;
    private Button addFood;

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
        this.addFood = v.findViewById(R.id.calorie_button_home);
        this.decrease = v.findViewById(R.id.water_decrease_home);
        this.increase = v.findViewById(R.id.water_increase_home);
        //set onclicklisteners
        this.foodLayout.setOnClickListener(this);
        this.waterLayout.setOnClickListener(this);
        this.addFood.setOnClickListener(this);
        this.decrease.setOnClickListener(this);
        this.increase.setOnClickListener(this);
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
            double calories = this.dbHelper.getTotalFood(this.userId, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            this.cc.setText(String.valueOf((int)calories));
        }
        this.dbHelper.close();
    }

    private void updateWater(){
        this.dbHelper = new DatabaseHelper(getActivity());
        this.water = dbHelper.getWater(this.userId, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        this.wc.setText(String.valueOf(water));
        this.dbHelper.close();
    }

    @Override
    public void onClick(View v) {
        this.dbHelper = new DatabaseHelper(getActivity());
        switch (v.getId()){
            case R.id.water_decrease_home:
                if(water > 0) {
                    this.dbHelper.updateWater(this.userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),this.water-1);
                    updateWater();
                } else {
                    Snackbar.make(getView().findViewById(R.id.container),"Error, can't process",Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.water_increase_home:
                if(this.water == 0){
                    Water w = new Water();
                    w.setValue(1);
                    w.setUser_id(this.userId);
                    w.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                    this.dbHelper.addWater(w);
                } else {
                    this.dbHelper.updateWater(this.userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),this.water+1);
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