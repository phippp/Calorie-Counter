package com.example.caloriecounter;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.caloriecounter.data.food.FoodListAdapter;
import com.example.caloriecounter.data.food.foodItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class search_food extends Fragment {

    public EditText searchBox;
    public ArrayList<foodItem> listItems=new ArrayList<foodItem>();
    public FoodListAdapter adapter;
    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_search_food, container, false);
        //get searchBox (EditText) and button
        searchBox = view.findViewById(R.id.searchBox);
        Button btn = (Button) view.findViewById(R.id.button);
        //Create custom list adapter
        adapter = new FoodListAdapter(getActivity().getApplicationContext(),R.layout.list_item,listItems);
        ListView lv = (ListView) view.findViewById(R.id.list_view);
        lv.setAdapter(adapter);
        //create API call queue
        queue = Volley.newRequestQueue(getActivity());
        //set item function -> create activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),AddFood.class);
                foodItem f = adapter.getItem(position);
                intent.putExtra("data",f.getData().toString());
                startActivity(intent);
            }
        });
        //call API using data from EditText
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear adapter
                adapter.clear();
                //construct url
                String url = "https://api.edamam.com/api/food-database/v2/parser?ingr="+ searchBox.getText() +"&app_id=d30be939&app_key=0cc3adf3f518970ab62700f205e99648";
                url = url.replaceAll(" ","%20");
                //notify the user of API call
                Toast toast = Toast.makeText(getActivity(), "API Call", Toast.LENGTH_SHORT);
                toast.show();
                Log.d("URL",url);
                //construct JSON request
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("RESPONSE",response.toString());
                            //Access parsed items
                            JSONArray arr = response.getJSONArray("parsed");
                            for(int i = 0; i < arr.length(); i++){
                                JSONObject food = arr.getJSONObject(i);
                                adapter.add(new foodItem(food.getJSONObject("food").getString("label"), food, food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
                            }
                            //Access hints items
                            arr = response.getJSONArray("hints");
                            for(int i = 0; i < arr.length(); i++){
                                JSONObject food = arr.getJSONObject(i);
                                adapter.add(new foodItem(food.getJSONObject("food").getString("label"), food, food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                //add request to queue
                queue.add(jsonObjectRequest);
            }
        });
        return view;
    }

}