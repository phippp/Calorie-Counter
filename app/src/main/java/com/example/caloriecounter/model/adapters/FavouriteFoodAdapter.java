package com.example.caloriecounter.model.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.AddFood;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.util.List;

public class FavouriteFoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<FoodItem> list;
    Context context;
    View container;

    private int user;
    private String username;

    private final int SHOW_MENU = 1;
    private final int HIDE_MENU = 2;

    public FavouriteFoodAdapter(Context context, List<FoodItem> articlesList, String username, int user, View container){
        this.list = articlesList;
        this.username = username;
        this.user = user;
        this.context = context;
        this.container = container;
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).isShowMenu()){
            return SHOW_MENU;
        }else{
            return HIDE_MENU;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == SHOW_MENU) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_food_menu, parent, false);
            return new MenuViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.database_food_item, parent, false);
            return new MyViewHolder(v);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        FoodItem entity = list.get(position);
        if(holder instanceof MyViewHolder){
            ((MyViewHolder)holder).food_name.setText(entity.getName());
            ((MyViewHolder)holder).calories_value.setText(String.format("%.2f",entity.getCalories()) + " kcal");
            ((MyViewHolder)holder).container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showMenu(position);
                    return true;
                }
            });
        }
        if(holder instanceof MenuViewHolder){
            ((MenuViewHolder)holder).remove.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    try {
                        DatabaseReference ref = database.getReference("/usr/"+user+"/"+username+"/prefs/"+entity.getData().getJSONObject("food").getString("foodId"));
                        ref.removeValue();
                        Snackbar.make(container,"Item removed from favourites",Snackbar.LENGTH_LONG).setAction("Undo", v1 -> ref.setValue(entity.getData().toString())).show();
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView food_name;
        TextView calories_value;
        ConstraintLayout container;

        public MyViewHolder(View itemView) {
            super(itemView);
            food_name = itemView.findViewById(R.id.food_name);
            calories_value = itemView.findViewById(R.id.calories_value);
            container = itemView.findViewById(R.id.container);
        }
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder{
        ImageView remove;

        public MenuViewHolder(View view){
            super(view);
            remove = view.findViewById(R.id.remove_image);
        }
    }

    public void showMenu(int position){
        for(int i = 0; i < list.size(); i++){
            list.get(i).updateMenu(false);
        }
        list.get(position).updateMenu(true);
        notifyDataSetChanged();
    }

    public boolean isMenuShown() {
        for(int i=0; i<list.size(); i++){
            if(list.get(i).isShowMenu()){
                return true;
            }
        }
        return false;
    }

    public void closeMenu() {
        for(int i=0; i<list.size(); i++){
            list.get(i).updateMenu(false);
        }
        notifyDataSetChanged();
    }

    public void deleteItem(View view){

    }

}