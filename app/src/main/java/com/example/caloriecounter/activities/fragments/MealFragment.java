package com.example.caloriecounter.activities.fragments;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.model.adapters.FoodItem;
import com.example.caloriecounter.model.adapters.RecyclerAdapter;
import com.example.caloriecounter.sql.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MealFragment extends Fragment {

    private String date;
    private String type;

    private TextView mealTitle;
    private TextView dateTitle;

    private int user_id;

    private DatabaseHelper dbHelper;

    RecyclerView recyclerView;
    List<FoodItem> list;
    RecyclerAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_meal,container,false);

        recyclerView = view.findViewById(R.id.recycler_list);
        list = new ArrayList<>();

        user_id = ((MyApp) requireActivity()).getUser_id();
        if(getArguments() != null) {
            date = getArguments().getString("date");
            type = getArguments().getString("type");
        }

        adapter = new RecyclerAdapter(getContext(), list, date, type, user_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            private ColorDrawable background;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.showMenu(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                TypedValue value = new TypedValue();
                requireContext().getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
                background = new ColorDrawable(value.data);

                View itemView = viewHolder.itemView;

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

        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                adapter.closeMenu();
            }
        });

        return view;
    }

    public void slideComplete(View view){
        Log.d("LOL","LOL");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mealTitle = requireActivity().findViewById(R.id.meal_title);
        dateTitle = requireActivity().findViewById(R.id.date_title);

        mealTitle.setText(type);
        dateTitle.setText(date);

        populateAdapter();

    }

    public void populateAdapter(){
        dbHelper = new DatabaseHelper(getActivity());
        FoodItem[] food = dbHelper.getMealItems(user_id, date, type);
        list.addAll(Arrays.asList(food));
        dbHelper.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        list.clear();
        populateAdapter();
    }
}
