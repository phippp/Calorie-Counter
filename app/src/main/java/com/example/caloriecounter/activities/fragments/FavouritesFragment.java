package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.model.adapters.FavouriteFoodAdapter;
import com.example.caloriecounter.model.adapters.FoodItem;
import com.example.caloriecounter.model.adapters.RecyclerAdapter;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FavouritesFragment extends Fragment implements ValueEventListener{

    private ConstraintLayout container;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    DatabaseHelper dbHelper;

    private String username;
    private int user_id;

    RecyclerView recyclerView;
    List<FoodItem> list;
    FavouriteFoodAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        container = view.findViewById(R.id.favourite_container);

        dbHelper = new DatabaseHelper(getContext());

        SharedPreferences pref = getActivity().getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        username = pref.getString("username",null);
        String password = pref.getString("password",null);
        user_id = dbHelper.getUserId(username,password);

        recyclerView = view.findViewById(R.id.recycler_list);
        list = new ArrayList<>();
        adapter = new FavouriteFoodAdapter(getContext(), list, username, user_id, container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ref = database.getReference("/usr/"+user_id+"/"+username+"/prefs");
        ref.addValueEventListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        ref.removeEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        list.clear();
        for(DataSnapshot snap: snapshot.getChildren()){
            try {
                JSONObject food = new JSONObject(snap.getValue().toString());
                list.add(new FoodItem(food.getJSONObject("food").getString("label"), food,food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
        TextView tv = getActivity().findViewById(R.id.favourite_count);
        tv.setText(getString(R.string.favourite,list.size()));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}
