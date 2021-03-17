package com.example.caloriecounter.activities.fragments;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.sql.DatabaseHelper;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class FoodFragment extends Fragment {

    private int maxHeight = 100;
    private int minHeight = 10;

    private int userId;

    private TextView[] days = new TextView[7];
    private ImageView[] bars = new ImageView[7];
    private TextView[] dates = new TextView[7];

    private DatabaseHelper db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = new DatabaseHelper(requireActivity());
        userId = ((MyApp) getActivity()).getUser_id();

        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        DateFormat formatter = new SimpleDateFormat("dd-MM");

        double[] localValues = new double[7];
        double max = 0.0;
        double min = 999999.0;

        for(int i = 0; i < 7; i++){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            double calories = db.getTotalFood(userId,df.format(cal.getTime()));
            localValues[i] = calories;
            if(calories > max){
                max = calories;
            }else if(calories < min){
                min = calories;
            }
            int dayId = getResources().getIdentifier("day_"+i,"id",getActivity().getPackageName());
            int barId = getResources().getIdentifier("bar_"+i,"id",getActivity().getPackageName());
            int dateId = getResources().getIdentifier("date_"+i,"id",getActivity().getPackageName());
            days[i] = getActivity().findViewById(dayId);
            bars[i] = getActivity().findViewById(barId);
            dates[i] = getActivity().findViewById(dateId);
            dates[i].setText(formatter.format(cal.getTime()));
            days[i].setText(String.valueOf((int)calories));
        }

        //map all the values to be in the range 10 to 200
        int heights[] = new int[7];
        for(int i = 0; i < 7; i++){
            heights[i] = (int) (((maxHeight-minHeight) * ((localValues[i]-min)/(max-min))) + minHeight);
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heights[i], getResources().getDisplayMetrics());
            bars[i].getLayoutParams().height = dimensionInDp;
        }

    }

}