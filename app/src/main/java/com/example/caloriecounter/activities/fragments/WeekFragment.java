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
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class WeekFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

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

        DatabaseReference myRef = database.getReference("/usr/"+user+"/prefs");
        myRef.setValue("Hello, World!");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                temp.setText(value);
                Log.d("LOL", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("LOL", "Failed to read value.", error.toException());
            }
        });

    }
}