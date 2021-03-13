package com.example.caloriecounter.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.caloriecounter.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class NutrientListAdapter extends ArrayAdapter<NutrientItem> {

    private Context mContext;
    int mResource;

    public NutrientListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<NutrientItem> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String name = getItem(position).getName();
        double qty = getItem(position).getQty();
        String unit = getItem(position).getUnit();

        NutrientItem nutrientItem = new NutrientItem(name,qty,unit);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = convertView.findViewById(R.id.nutrient_name);
        tvName.setText(name);
        TextView tvVal = convertView.findViewById(R.id.nutrient_value);
        tvVal.setText(qty + unit);

        return convertView;
    }
}