package com.example.caloriecounter.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.model.adapters.FavouriteFoodAdapter;
import com.example.caloriecounter.model.adapters.FoodItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class FavouritesFragment extends Fragment implements ValueEventListener{

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;

    RecyclerView recyclerView;
    List<FoodItem> list;
    FavouriteFoodAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //get container to hold the Snackbar in the recyclerView
        ConstraintLayout container = getView().findViewById(R.id.favourite_container);

        //get user information
        SharedPreferences pref = requireActivity().getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        String username = pref.getString("username", null);
        int user_id = ((MyApp) requireActivity()).getUser_id();

        //create recyclerView and adapter to hold items
        recyclerView = getView().findViewById(R.id.recycler_list);
        list = new ArrayList<>();
        adapter = new FavouriteFoodAdapter(getContext(), list, username, user_id, container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        //setup firebase reference and listener
        ref = database.getReference("/usr/"+ user_id +"/"+ username +"/prefs");
        ref.addValueEventListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ref.removeEventListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ref.addValueEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        list.clear();
        //empty list so all items can be re-added
        for(DataSnapshot snap: snapshot.getChildren()){
            try {
                JSONObject food = new JSONObject(Objects.requireNonNull(snap.getValue()).toString());
                list.add(new FoodItem(food.getJSONObject("food").getString("label"), food,food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
        //update number of favourites
        TextView tv = getView().findViewById(R.id.favourite_count);
        tv.setText(getString(R.string.favourite,list.size()));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}
