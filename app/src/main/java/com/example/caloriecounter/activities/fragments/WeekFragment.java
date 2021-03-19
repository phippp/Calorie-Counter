package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.sql.DatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class WeekFragment extends Fragment {

    private TextView temp;
    private DatabaseHelper dbHelper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_week, container, false);

        temp = v.findViewById(R.id.temp);

        return v;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbHelper = new DatabaseHelper(requireActivity());

        int user = ((MyApp) getActivity()).getUser_id();

        DateFormat df = new SimpleDateFormat("dd-MM-yyy");

        for(int i = 0; i < 7; i++){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            temp.setText(temp.getText().toString() + df.format(cal.getTime()) + " = " + dbHelper.getTotalFood(user,df.format( cal.getTime() ) ) + " & " + dbHelper.getWater(user,df.format( cal.getTime() )) + "\n");
        }
    }
}