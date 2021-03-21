package com.example.caloriecounter.model.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
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

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<FoodItem> list;
    Context context;

    private String date;
    private String meal;
    private int user_id;

    private final int SHOW_MENU = 1;
    private final int HIDE_MENU = 2;

    public RecyclerAdapter(Context context, List<FoodItem> articlesList, String date, String meal, int user_id) {
        this.list = articlesList;
        this.context = context;
        this.meal = meal;
        this.date = date;
        this.user_id = user_id;
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
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.database_food_menu, parent, false);
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
            ((MenuViewHolder)holder).add.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddFood.class);
                    intent.putExtra("date",date);
                    intent.putExtra("type",meal);
                    intent.putExtra("data",list.get(position).getData().toString());
                    context.startActivity(intent);
                    closeMenu();
                }
            });
            ((MenuViewHolder)holder).delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.dropFood(list.get(position).getId());
                    closeMenu();
                    list.remove(position);
                    db.close();
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
        ImageView add;
        ImageView delete;

        public MenuViewHolder(View view){
            super(view);
            add = view.findViewById(R.id.add_image);
            delete = view.findViewById(R.id.remove_image);
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