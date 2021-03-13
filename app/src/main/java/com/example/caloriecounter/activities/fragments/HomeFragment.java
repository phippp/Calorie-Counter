package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.sql.DatabaseHelper;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbhelper;
    private TextView cc;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        cc = view.findViewById(R.id.calorie_counter);
        updateCalories();
        return view;
    }

    private void updateCalories() {
        dbhelper = new DatabaseHelper(getActivity());
        SharedPreferences pref = getActivity().getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        String username = pref.getString("username",null);
        String password = pref.getString("password",null);
        int user_id = dbhelper.getUserId(username,password);
        if(user_id != -1) {
            double calories = dbhelper.getTotalFood(user_id);
            cc.setText("You have consumed " + calories + " calories today");
        }
        dbhelper.close();
    }
}