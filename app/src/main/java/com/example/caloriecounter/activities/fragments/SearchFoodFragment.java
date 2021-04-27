package com.example.caloriecounter.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.AddFood;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.model.adapters.FoodItem;
import com.example.caloriecounter.model.adapters.FoodListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SearchFoodFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public EditText searchBox;
    public ArrayList<FoodItem> listItems= new ArrayList<>();
    public FoodListAdapter adapter;
    private RequestQueue queue;

    private String type;
    private String date;

    private boolean tablet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_food, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String[] items = new String[adapter.getCount()];
        for(int i = 0; i < adapter.getCount(); i++){
            items[i] = adapter.getItem(i).getData().toString();
        }
        outState.putStringArray("data",items);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        date = getArguments().getString("date");
        type = getArguments().getString("type");
        if(type == null){
            //this just makes sure that a type is always assigned
            type = "Other";
        }

        tablet = getView().findViewById(R.id.extra_fragment) != null;

        //get searchBox (EditText) and button
        searchBox = getView().findViewById(R.id.searchBox);
        Button btn = (Button) getView().findViewById(R.id.button);

        //Create custom list adapter
        if(((MyApp)requireActivity()).isDark()) {
            adapter = new FoodListAdapter(getActivity().getApplicationContext(), R.layout.list_item_dark, listItems);
        } else {
            adapter = new FoodListAdapter(getActivity().getApplicationContext(), R.layout.list_item, listItems);
        }
        ListView lv = (ListView) getView().findViewById(R.id.list_view);
        lv.setAdapter(adapter);

        //create firebase reference
        SharedPreferences pref = getActivity().getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        String username = pref.getString("username",null);
        DatabaseReference ref = database.getReference("/usr/"+((MyApp)getActivity()).getUser_id()+"/"+username+"/prefs");

        //load favourites into adapter before search or load saved state
        if(savedInstanceState != null){
            String[] items = savedInstanceState.getStringArray("data");
            if(items != null){
                adapter.clear();
                for(String data: items){
                    try {
                        JSONObject food = new JSONObject(data);
                        adapter.add(new FoodItem(food.getJSONObject("food").getString("label"), food,food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        } else {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot snap: snapshot.getChildren()){
                        try {
                            JSONObject food = new JSONObject(snap.getValue().toString());
                            adapter.add(new FoodItem(food.getJSONObject("food").getString("label"), food,food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        //create API call queue
        queue = Volley.newRequestQueue(getActivity());
        //set item on click create activity
        lv.setOnItemClickListener((parent, view, position, id) -> {
            FoodItem f = adapter.getItem(position);
            if(!tablet) {
                //create new activity
                Intent intent = new Intent(getActivity(), AddFood.class);
                intent.putExtra("type",type);
                intent.putExtra("date",date);
                intent.putExtra("data", f.getData().toString());
                startActivity(intent);
            } else {
                //create new fragment and display it in the extra fragment
                Fragment fragment = new AddFoodFragment();
                Bundle b = new Bundle();
                b.putString("data",f.getData().toString());
                b.putString("date",date);
                b.putString("type",type);
                fragment.setArguments(b);
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction().replace(R.id.extra_fragment,fragment).commit();
            }
        });

        //call API using data from EditText
        btn.setOnClickListener(v -> {
            //clear adapter
            adapter.clear();
            //construct url
            String url = "https://api.edamam.com/api/food-database/v2/parser?ingr=" + searchBox.getText() +"&app_id=" + getString(R.string.app_id) + "&app_key=" + getString(R.string.api_key);
            url = url.replaceAll(" ","%20");
            //check connection status
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                //connected can go ahead
                ConstraintLayout layout = getView().findViewById(R.id.internet_error);
                layout.setVisibility(View.GONE);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                    try {
                        //Access parsed items
                        JSONArray arr = response.getJSONArray("hints");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject food = arr.getJSONObject(i);
                            adapter.add(new FoodItem(food.getJSONObject("food").getString("label"), food, food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
                //add request to queue
                queue.add(jsonObjectRequest);
            } else {
                //not connected return error
                ConstraintLayout layout = getView().findViewById(R.id.internet_error);
                layout.setVisibility(View.VISIBLE);
            }
        });
    }
}