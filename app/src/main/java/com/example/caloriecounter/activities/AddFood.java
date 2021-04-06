package com.example.caloriecounter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.ToggleButton;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.adapters.NutrientItem;
import com.example.caloriecounter.model.adapters.NutrientListAdapter;
import com.example.caloriecounter.model.database.Calories;
import com.example.caloriecounter.data.DatabaseHelper;
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

public class AddFood extends AppCompatActivity implements View.OnClickListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private JSONObject data;
    private DatabaseHelper databaseHelper;
    public ArrayList<NutrientItem> listItems=new ArrayList<NutrientItem>();
    public NutrientListAdapter adapter;

    private int user_id;
    private String username;

    private TextView title;
    private ToggleButton favouriteButton;
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
    private boolean dark = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_theme = pref.getBoolean("dark_theme",true);
        if(!dark_theme) {
            dark = false;
            setTheme(R.style.CustomLight);
        }

        databaseHelper = new DatabaseHelper(this);

        pref = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        username = pref.getString("username",null);
        String password = pref.getString("password",null);
        Cursor c = getContentResolver().query(
                DataProvider.URI_USER,
                null,
                DataProvider.COLUMN_USER_USERNAME + " = ?" +" AND " + DataProvider.COLUMN_USER_PASSWORD + " =?",
                new String[]{ username, password },
                null);
        if(c.getCount() > 0){
            c.moveToNext();
            user_id = c.getInt(c.getColumnIndex(DataProvider.COLUMN_USER_ID));
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_food);


        if(this.dark) {
            adapter = new NutrientListAdapter(getApplicationContext(), R.layout.nutrient_list_dark, listItems);
        }else{
            adapter = new NutrientListAdapter(getApplicationContext(), R.layout.nutrient_list, listItems);
        }
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
        favouriteButton = findViewById(R.id.favourite_toggle);
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
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton)v).isChecked();
                try {
                    DatabaseReference ref = database.getReference("/usr/" + user_id + "/" + username + "/prefs/" + data.getJSONObject("food").getString("foodId"));
                    if (isChecked) { //add to database
                        ref.setValue(data.toString());
                    } else { //remove from database
                        ref.removeValue();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
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
                double num;
                try {
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
                        values.put(DataProvider.COLUMN_CALORIES_MEAL,type);
                        if(date != null) {
                            values.put(DataProvider.COLUMN_CALORIES_DATE, date);
                        } else {
                            values.put(DataProvider.COLUMN_CALORIES_DATE, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                        }
                        getContentResolver().insert(DataProvider.URI_CALORIES,values);
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