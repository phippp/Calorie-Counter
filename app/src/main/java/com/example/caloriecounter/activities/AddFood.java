package com.example.caloriecounter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.caloriecounter.R;

import org.json.JSONException;
import org.json.JSONObject;

public class AddFood extends AppCompatActivity {

    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        Intent in = getIntent();
        if(in.hasExtra("data")){
            try {
                data = new JSONObject(in.getStringExtra("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        TextView tvTitle = (TextView)findViewById(R.id.food_title);
        try {
            tvTitle.setText(data.getJSONObject("food").getString("label"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tv = (TextView)findViewById(R.id.food_data);
        tv.setText(data.toString());
    }
}