package com.example.caloriecounter.activities.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.model.adapters.NutrientItem;
import com.example.caloriecounter.model.adapters.NutrientListAdapter;
import com.example.caloriecounter.model.database.Calories;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddFoodFragment extends Fragment implements View.OnClickListener{

    private JSONObject data;
    private DatabaseHelper databaseHelper;
    public ArrayList<NutrientItem> listItems=new ArrayList<NutrientItem>();
    public NutrientListAdapter adapter;

    private TextView title;
    private RadioGroup radioGroup;
    private RadioButton gramsOption;
    private RadioButton servingOption;
    private Button addBtn;
    private Button mapsBtn;
    private Button addQty;
    private Button minusQty;
    private EditText input;
    private String brand;
    private double calories;
    private double serving; //weight

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_food, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String str = getArguments().getString("data");
        databaseHelper = new DatabaseHelper(getActivity());

        adapter = new NutrientListAdapter(getActivity().getApplicationContext(),R.layout.nutrient_list,listItems);
        ListView lv = (ListView) requireActivity().findViewById(R.id.nutrient_list);
        lv.setAdapter(adapter);

        try {
            data = new JSONObject(str);
            JSONArray measures = data.getJSONArray("measures");
            for(int i = 0; i < measures.length(); i++){
                JSONObject temp = measures.getJSONObject(i);
                if(temp.has("label") ){
                    if(temp.getString("label").equals("Serving")){
                        serving = temp.getDouble("weight");
                        Log.d("LOL","There are " + serving + "g in a serving");
                    }
                }
            }
            initViews();
            title.setText(data.getJSONObject("food").getString("label"));
            JSONObject nutrients = data.getJSONObject("food").getJSONObject("nutrients");
            if(nutrients.has("ENERC_KCAL")){
                calories = nutrients.getDouble("ENERC_KCAL");
                adapter.add(new NutrientItem("Calories",nutrients.getDouble("ENERC_KCAL"),"kcal"));
            }
            if(nutrients.has("PROCNT")){
                adapter.add(new NutrientItem("Protein",nutrients.getDouble("PROCNT"),"g"));
            }
            if(nutrients.has("FAT")){
                adapter.add(new NutrientItem("Fat",nutrients.getDouble("FAT"),"g"));
            }
            if(nutrients.has("CHOCDF")){
                adapter.add(new NutrientItem("Carbs",nutrients.getDouble("CHOCDF"),"g"));
            }
            if(nutrients.has("FIBTG")){
                adapter.add(new NutrientItem("Fiber",nutrients.getDouble("FIBTG"),"g"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initViews() throws JSONException {
        title = requireActivity().findViewById(R.id.food_title);
        input = requireActivity().findViewById(R.id.enter_number);
        radioGroup = requireActivity().findViewById(R.id.quantity_toggler);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int curr;
                switch(checkedId){
                    case R.id.option_grams:
                        try {
                            curr = Integer.parseInt(input.getText().toString());
                            input.setText(String.valueOf((int) Math.round(curr * serving)));
                        } catch (NumberFormatException e){
                            e.printStackTrace();
                            input.setText(String.valueOf((int)serving));
                        }
                        break;
                    case R.id.option_portion:
                        try {
                            curr = Integer.parseInt(input.getText().toString());
                            input.setText(String.valueOf((int) Math.round(curr / serving)));
                        }catch (NumberFormatException e){
                            e.printStackTrace();
                            input.setText("1");
                        }
                        break;
                }
            }
        });
        gramsOption = requireActivity().findViewById(R.id.option_grams);
        servingOption = requireActivity().findViewById(R.id.option_portion);
        mapsBtn = requireActivity().findViewById(R.id.maps_button);
        addBtn = requireActivity().findViewById(R.id.add_food_button);
        minusQty = requireActivity().findViewById(R.id.minus_qty);
        addQty = requireActivity().findViewById(R.id.add_qty);
        addQty.setOnClickListener(this);
        minusQty.setOnClickListener(this);
        mapsBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        if(data.getJSONObject("food").has("brand")){
            mapsBtn.setVisibility(View.VISIBLE);
            brand = data.getJSONObject("food").getString("brand");
            brand = brand.replaceAll(" ", "%20");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_qty:
                try {
                    input.setText(String.valueOf(Integer.parseInt(input.getText().toString()) + 1));
                } catch (NumberFormatException e){
                    e.printStackTrace();
                    input.setText("1");
                }
                break;
            case R.id.minus_qty:
                try {
                    if (Integer.parseInt(input.getText().toString()) > 1) {
                        input.setText(String.valueOf(Integer.parseInt(input.getText().toString()) - 1));
                    }
                } catch(NumberFormatException e){
                    e.printStackTrace();
                    input.setText("1");
                }
                break;
            case R.id.maps_button:
                Uri geo = Uri.parse("http://maps.google.com/maps?q="+brand);
                Intent maps = new Intent(Intent.ACTION_VIEW,geo);
                maps.setPackage("com.google.android.apps.maps");
                startActivity(maps);
                break;
            case R.id.add_food_button:
                Calories item = new Calories();
                //int user_id =
                int user_id = ((MyApp)getActivity()).getUser_id();
                double num;
                try {
                    if (radioGroup.getCheckedRadioButtonId() == R.id.option_grams) {
                        num = (Double.parseDouble(input.getText().toString()) / serving) * calories;
                    } else {
                        num = Double.parseDouble(input.getText().toString()) * calories;
                    }
                    if(user_id != -1) {
                        item.setUser_id(user_id);
                        item.setData(data);
                        item.setValue(num);
                        item.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                        databaseHelper.addCalories(item);
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    Snackbar snack = Snackbar.make(requireActivity().findViewById(R.id.container),"Invalid number try again!",Snackbar.LENGTH_LONG);
                    snack.show();
                }
                break;
        }
    }
}