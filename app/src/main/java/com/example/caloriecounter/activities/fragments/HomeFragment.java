package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.model.database.Water;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private DatabaseHelper dbhelper;
    private TextView cc;
    private TextView wc;

    private Button increase;
    private Button decrease;

    private String username;
    private String password;
    private int userId;
    private int water;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        cc = view.findViewById(R.id.calorie_counter);
        wc = view.findViewById(R.id.water_display);
        decrease = view.findViewById(R.id.water_decrease);
        decrease.setOnClickListener(this);
        increase = view.findViewById(R.id.water_increase);
        increase.setOnClickListener(this);

        getPrefs();
        updateCalories();
        updateWater();
        return view;
    }

    private void getPrefs() {
        SharedPreferences pref = getActivity().getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        username = pref.getString("username",null);
        password = pref.getString("password",null);
    }

    private void updateCalories() {
        dbhelper = new DatabaseHelper(getActivity());
        userId = dbhelper.getUserId(username,password);
        if(userId != -1) {
            double calories = dbhelper.getTotalFood(userId, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            cc.setText("You have consumed " + String.format("%.2f",calories) + " calories today");
        }
        dbhelper.close();
    }

    private void updateWater(){
        dbhelper = new DatabaseHelper(getActivity());
        water = dbhelper.getWater(userId, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        wc.setText(String.valueOf(water));
    }

    @Override
    public void onClick(View v) {
        dbhelper = new DatabaseHelper(getActivity());
        switch (v.getId()){
            case R.id.water_decrease:
                if(water > 0) {
                    dbhelper.updateWater(userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),water-1);
                    updateWater();
                } else {
                    Snackbar.make(getView().findViewById(R.id.container),"Error, can't process",Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.water_increase:
                if(water == 0){
                    Water w = new Water();
                    w.setValue(1);
                    w.setUser_id(userId);
                    w.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                    dbhelper.addWater(w);
                } else {
                    dbhelper.updateWater(userId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),water+1);
                }
                updateWater();
                break;
        }
    }
}