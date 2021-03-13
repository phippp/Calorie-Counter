package com.example.caloriecounter.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.example.caloriecounter.activities.AddFood;
import com.example.caloriecounter.R;
import com.example.caloriecounter.model.FoodListAdapter;
import com.example.caloriecounter.model.FoodItem;
import com.example.caloriecounter.activities.MyApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchFoodFragment extends Fragment {

    public EditText searchBox;
    public ArrayList<FoodItem> listItems=new ArrayList<FoodItem>();
    public FoodListAdapter adapter;
    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_search_food, container, false);
        //Set fragment title
        ((MyApp) getActivity()).setActionBarTitle("Search Food");
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
                Intent intent = new Intent(getActivity(), AddFood.class);
                FoodItem f = adapter.getItem(position);
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
                String url = "https://api.edamam.com/api/food-database/v2/parser?ingr=" + searchBox.getText() +"&app_id=" + getString(R.string.app_id) + "&app_key=" + getString(R.string.api_key);
                url = url.replaceAll(" ","%20");
                //notify the user of API call
                Toast toast = Toast.makeText(getActivity(), "API Call", Toast.LENGTH_SHORT);
                toast.show();
                Log.d("URL",url);
//                construct JSON request
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    ConstraintLayout layout = getActivity().findViewById(R.id.internet_error);
                    layout.setVisibility(View.GONE);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("RESPONSE", response.toString());
                                //Access parsed items
                                JSONArray arr = response.getJSONArray("parsed");
//                                for (int i = 0; i < arr.length(); i++) {
//                                    JSONObject food = arr.getJSONObject(i);
//                                    adapter.add(new FoodItem(food.getJSONObject("food").getString("label"), food, food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
//                                }
                                //Access hints items
                                arr = response.getJSONArray("hints");
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject food = arr.getJSONObject(i);
                                    adapter.add(new FoodItem(food.getJSONObject("food").getString("label"), food, food.getJSONObject("food").getJSONObject("nutrients").getDouble("ENERC_KCAL")));
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
                } else {
                    //not connected
                    ConstraintLayout layout = getActivity().findViewById(R.id.internet_error);
                    layout.setVisibility(View.VISIBLE);
                    Log.d("ERROR","NO INTERNET");

                }
            }
        });
        return view;
    }
}