package com.example.caloriecounter.activities.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.adapters.NutrientItem;
import com.example.caloriecounter.model.adapters.NutrientListAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddFoodFragment extends Fragment implements View.OnClickListener{

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private int user_id;
    private String username;

    private JSONObject data;
    private String date, type;

    public ArrayList<NutrientItem> listItems = new ArrayList<>();
    public NutrientListAdapter adapter;

    private TextView title;
    private ToggleButton favouriteButton;
    private RadioGroup radioGroup;
    private EditText input;
    private String brand;
    private double calories;
    private double serving;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_food, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //get data with null check
        String str = getArguments() != null ? getArguments().getString("data") : null;
        date = getArguments().getString("date");
        type = getArguments().getString("type");

        //get user info
        user_id = ((MyApp)requireActivity()).getUser_id();
        SharedPreferences pref = requireActivity().getSharedPreferences("LoggedInUser", Context.MODE_PRIVATE);
        username = pref.getString("username",null);

        //create adapter for nutrients
        if(((MyApp)requireActivity()).isDark()){
            adapter = new NutrientListAdapter(getView().getContext(),R.layout.nutrient_list_dark,listItems);
        } else {
            adapter = new NutrientListAdapter(getView().getContext(),R.layout.nutrient_list,listItems);
        }
        ListView listView = getView().findViewById(R.id.nutrient_list);
        listView.setAdapter(adapter);

        //use the data passed to the page
        if(str != null) {
            try {
                data = new JSONObject(str);
                JSONArray measures = data.getJSONArray("measures");
                for (int i = 0; i < measures.length(); i++) {
                    JSONObject temp = measures.getJSONObject(i);
                    if (temp.has("label")) {
                        if (temp.getString("label").equals("Serving")) {
                            serving = temp.getDouble("weight");
                        }
                    }
                }
                //create the views
                initViews();
                title.setText(data.getJSONObject("food").getString("label"));
                JSONObject nutrients = data.getJSONObject("food").getJSONObject("nutrients");
                if (nutrients.has("ENERC_KCAL")) {
                    calories = nutrients.getDouble("ENERC_KCAL");
                    adapter.add(new NutrientItem("Calories", nutrients.getDouble("ENERC_KCAL"), "kcal"));
                }
                if (nutrients.has("PROCNT")) {
                    adapter.add(new NutrientItem("Protein", nutrients.getDouble("PROCNT"), "g"));
                }
                if (nutrients.has("FAT")) {
                    adapter.add(new NutrientItem("Fat", nutrients.getDouble("FAT"), "g"));
                }
                if (nutrients.has("CHOCDF")) {
                    adapter.add(new NutrientItem("Carbs", nutrients.getDouble("CHOCDF"), "g"));
                }
                if (nutrients.has("FIBTG")) {
                    adapter.add(new NutrientItem("Fiber", nutrients.getDouble("FIBTG"), "g"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() throws JSONException {
        //title
        title = getView().findViewById(R.id.food_title);
        //input
        input = getView().findViewById(R.id.enter_number);
        //radio group buttons
        radioGroup = getView().findViewById(R.id.quantity_toggler);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int curr;
            if(checkedId == R.id.option_grams){
                try {
                    curr = Integer.parseInt(input.getText().toString());
                    input.setText(String.valueOf((int) Math.round(curr * serving)));
                } catch (NumberFormatException e){
                    e.printStackTrace();
                    input.setText(String.valueOf((int)serving));
                }
            } else if(checkedId == R.id.option_portion){
                try {
                    curr = Integer.parseInt(input.getText().toString());
                    input.setText(String.valueOf((int) Math.round(curr / serving)));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    input.setText("1");
                }
            }
        });
        //favourite button
        favouriteButton = getView().findViewById(R.id.favourite_toggle);
        DatabaseReference ref = database.getReference("/usr/"+user_id+"/"+username+"/prefs");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    String foodId = data.getJSONObject("food").getString("foodId");
                    for(DataSnapshot snap: snapshot.getChildren()){
                        if(snap.getKey().equals(foodId)){
                            favouriteButton.setChecked(true);
                        }
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        favouriteButton.setOnClickListener(v -> {
            boolean isChecked = ((ToggleButton)v).isChecked();
            try {
                DatabaseReference ref1 = database.getReference("/usr/" + user_id + "/" + username + "/prefs/" + data.getJSONObject("food").getString("foodId"));
                if (isChecked) { //add to database
                    ref1.setValue(data.toString());
                } else { //remove from database
                    ref1.removeValue();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        });
        //map btn if the food has a company attached
        Button mapsBtn = getView().findViewById(R.id.maps_button);
        mapsBtn.setOnClickListener(this);
        //button to increase value
        Button addQty = getView().findViewById(R.id.add_qty);
        addQty.setOnClickListener(this);
        //button to decrease value
        Button minusQty = getView().findViewById(R.id.minus_qty);
        minusQty.setOnClickListener(this);
        //button to submit the food
        Button addBtn = getView().findViewById(R.id.add_food_button);
        addBtn.setOnClickListener(this);
        //show the maps button if brand is attached
        if(data.getJSONObject("food").has("brand")){
            mapsBtn.setVisibility(View.VISIBLE);
            brand = data.getJSONObject("food").getString("brand");
            brand = brand.replaceAll(" ", "%20");
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.add_qty){
            //add qty
            try {
                input.setText(String.valueOf(Integer.parseInt(input.getText().toString()) + 1));
            } catch (NumberFormatException e){
                e.printStackTrace();
                input.setText("1");
            }
        } else if(v.getId() == R.id.minus_qty){
            //decrease qty
            try {
                if (Integer.parseInt(input.getText().toString()) > 1) {
                    input.setText(String.valueOf(Integer.parseInt(input.getText().toString()) - 1));
                }
            } catch(NumberFormatException e){
                e.printStackTrace();
                input.setText("1");
            }
        } else if(v.getId() == R.id.maps_button){
            //open google maps by searching for the nearest restaurant (brand)
            Uri geo = Uri.parse("http://maps.google.com/maps?q="+brand);
            Intent maps = new Intent(Intent.ACTION_VIEW,geo);
            maps.setPackage("com.google.android.apps.maps");
            startActivity(maps);
        } else if(v.getId() == R.id.add_food_button){
            //add the food to the database
            int user_id = ((MyApp) requireActivity()).getUser_id();
            double num;
            try {
                //calculate the calories
                if (radioGroup.getCheckedRadioButtonId() == R.id.option_grams) {
                    num = (Double.parseDouble(input.getText().toString()) / serving) * calories;
                } else {
                    num = Double.parseDouble(input.getText().toString()) * calories;
                }
                if(user_id != -1) {

                    ContentValues values = new ContentValues();
                    values.put(DataProvider.COLUMN_CALORIES_DATA,data.toString());
                    values.put(DataProvider.COLUMN_CALORIES_USER_ID,user_id);
                    values.put(DataProvider.COLUMN_CALORIES_VALUE,num);
                    values.put(DataProvider.COLUMN_CALORIES_MEAL, type);
                    values.put(DataProvider.COLUMN_CALORIES_DATE, date);

                    requireActivity().getContentResolver().insert(
                            DataProvider.URI_CALORIES,
                            values
                    );

                    Snackbar snack = Snackbar.make(requireActivity().findViewById(R.id.container),"Item added!",Snackbar.LENGTH_LONG);
                    snack.show();

                    FragmentManager fm = getParentFragmentManager();
                    fm.beginTransaction().remove(this).commit();

                }
            }catch (NumberFormatException e){
                e.printStackTrace();
                Snackbar snack = Snackbar.make(requireActivity().findViewById(R.id.container),"Invalid number try again!",Snackbar.LENGTH_LONG);
                snack.show();
            }
        }
    }
}
