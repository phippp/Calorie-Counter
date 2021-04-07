package com.example.caloriecounter.activities.fragments;

import android.content.ContentValues;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.adapters.NutrientItem;
import com.example.caloriecounter.model.adapters.NutrientListAdapter;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddFoodFragment extends Fragment implements View.OnClickListener{

    private JSONObject data;
    public ArrayList<NutrientItem> listItems = new ArrayList<>();
    public NutrientListAdapter adapter;

    private TextView title;
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

        //create adapter for nutrients
        adapter = new NutrientListAdapter(getView().getContext(),R.layout.nutrient_list,listItems);
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
                    values.put(DataProvider.COLUMN_CALORIES_MEAL,"Breakfast");
                    values.put(DataProvider.COLUMN_CALORIES_DATE, new SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(new Date()));

                    requireActivity().getContentResolver().insert(
                            DataProvider.URI_CALORIES,
                            values
                    );
                }
            }catch (NumberFormatException e){
                e.printStackTrace();
                Snackbar snack = Snackbar.make(requireActivity().findViewById(R.id.container),"Invalid number try again!",Snackbar.LENGTH_LONG);
                snack.show();
            }
        }
    }
}
