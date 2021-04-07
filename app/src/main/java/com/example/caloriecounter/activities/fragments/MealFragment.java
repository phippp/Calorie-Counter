package com.example.caloriecounter.activities.fragments;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.adapters.FoodItem;
import com.example.caloriecounter.model.adapters.RecyclerAdapter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealFragment extends Fragment {

    private String date;
    private String type;

    private int user_id;

    RecyclerView recyclerView;
    List<FoodItem> list;
    RecyclerAdapter adapter;

    private boolean saved = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            date = savedInstanceState.getString("date");
            type = savedInstanceState.getString("type");
            saved = true;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_meal,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //get screen orientation
        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        //create recycler view and list
        recyclerView = requireActivity().findViewById(R.id.recycler_list);
        list = new ArrayList<>();
        //get userId from parent activity
        user_id = ((MyApp) requireActivity()).getUser_id();

        //make sure savedInstanceState is not overwritten by null values
        if(!saved) {
            if (getArguments() != null) {
                if (getArguments().getString("date") != null) {
                    date = getArguments().getString("date");
                }
                if (getArguments().getString("type") != null) {
                    type = getArguments().getString("type");
                }
            }
        }
        //create adapter and set layout manager depending on orientation
        adapter = new RecyclerAdapter(getContext(), list, date, type);
        if(landscape) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        recyclerView.setAdapter(adapter);

        //set slide left functionality when in portrait
        if(!landscape) {
            ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    adapter.showMenu(viewHolder.getAdapterPosition());
                }

                @Override
                public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    //get primary colour
                    TypedValue value = new TypedValue();
                    requireContext().getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
                    ColorDrawable background = new ColorDrawable(value.data);
                    //get view for bounds
                    View itemView = viewHolder.itemView;
                    //draw background between the views original and current positions
                    if (dX > 0) {
                        background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
                    } else if (dX < 0) {
                        background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    } else {
                        background.setBounds(0, 0, 0, 0);
                    }
                    background.draw(c);
                }
            };

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
            //reset on scroll on versions M and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                recyclerView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> adapter.closeMenu());
            }
        }

        //update meal and date
        TextView mealTitle = requireActivity().findViewById(R.id.meal_title);
        TextView dateTitle = requireActivity().findViewById(R.id.date_title);
        mealTitle.setText(type);
        dateTitle.setText(date);

    }



    public void populateAdapter(){
        //get all food items for that day and meal
        Cursor c = getActivity().getContentResolver().query(
                DataProvider.URI_CALORIES,
                null,
                DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_MEAL + " = ?" ,
                new String[]{String.valueOf(user_id), date, type},
                null
        );
        //use items to add to list
        while(c.moveToNext()){
            JSONObject obj = new JSONObject();
            int id = c.getInt(c.getColumnIndex(DataProvider.COLUMN_CALORIES_ID));
            String name = "";
            double val = c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
            try{
                obj = new JSONObject(c.getString(c.getColumnIndex(DataProvider.COLUMN_CALORIES_DATA)));
                name = obj.getJSONObject("food").getString("label");
            }catch(JSONException e){
                e.printStackTrace();
            }
            list.add(new FoodItem(id,name,obj,val));
        }
        //free memory
        c.close();
        //show either error or recyclerView
        if(list.size() == 0){
            recyclerView.setVisibility(View.GONE);
            getActivity().findViewById(R.id.empty_message).setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.empty_message).setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("date",date);
        outState.putString("type",type);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        list.clear();
        populateAdapter();
    }

}
