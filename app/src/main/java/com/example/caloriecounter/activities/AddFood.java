package com.example.caloriecounter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.caloriecounter.R;
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

public class AddFood extends AppCompatActivity implements View.OnClickListener {

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

    private String type;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        databaseHelper = new DatabaseHelper(this);

        adapter = new NutrientListAdapter(getApplicationContext(),R.layout.nutrient_list,listItems);
        ListView lv = (ListView) findViewById(R.id.nutrient_list);
        lv.setAdapter(adapter);

        Intent in = getIntent();
        if(in.hasExtra("data")){
            try {
                data = new JSONObject(in.getStringExtra("data"));
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(in.hasExtra("type")){
            type = in.getStringExtra("type");
        } else {
            type = "Other";
        }
        if(in.hasExtra("date")){
            date = in.getStringExtra("date");
        }

        try {
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
        title = findViewById(R.id.food_title);
        input = findViewById(R.id.enter_number);
        radioGroup = findViewById(R.id.quantity_toggler);
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
        gramsOption = findViewById(R.id.option_grams);
        servingOption = findViewById(R.id.option_portion);
        mapsBtn = findViewById(R.id.maps_button);
        addBtn = findViewById(R.id.add_food_button);
        minusQty = findViewById(R.id.minus_qty);
        addQty = findViewById(R.id.add_qty);
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
                SharedPreferences pref = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
                String username = pref.getString("username",null);
                String password = pref.getString("password",null);
                int user_id = databaseHelper.getUserId(username,password);
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
                        item.setType(type);
                        if(date != null){
                            item.setDate(date);
                        }else {
                            item.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                        }
                        databaseHelper.addCalories(item);
                        finish();
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    Snackbar snack = Snackbar.make(findViewById(R.id.container),"Invalid number try again!",Snackbar.LENGTH_LONG);
                    snack.show();
                }
                break;
        }
    }
}