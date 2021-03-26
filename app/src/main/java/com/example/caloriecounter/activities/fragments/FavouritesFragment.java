package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FavouritesFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseHelper dbHelper;
    private String username;
    private int user_id;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());

        SharedPreferences pref = getActivity().getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        username = pref.getString("username",null);
        String password = pref.getString("password",null);
        user_id = dbHelper.getUserId(username,password);

        DatabaseReference ref = database.getReference("/usr/"+user_id+"/"+username+"/prefs");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<String> list = new ArrayList<>();
                TextView tv = getActivity().findViewById(R.id.test);
                for(DataSnapshot snap: snapshot.getChildren()){
                    //list.add(snap.getValue().toString());
                    tv.setText(tv.getText() + "\n" + snap.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
