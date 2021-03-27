package com.example.caloriecounter.activities.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.activities.Views.Chart;
import com.example.caloriecounter.model.database.Water;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class WaterFragment extends Fragment implements View.OnClickListener {

    private final int maxHeight = 100;
    private final int minHeight = 10;

    private int userId;

    private Button increaseBtn;
    private Button decreaseBtn;
    private TextView current;
    private TextView helper;

    private int day = 0;
    private int currentWater;

    private ConstraintLayout[] layouts = new ConstraintLayout[7];
    private TextView[] days = new TextView[7];
    private ImageView[] bars = new ImageView[7];
    private TextView[] dates = new TextView[7];

    private Uri file = null;

    private List<String> datesList= new ArrayList<>();
    private List<Integer> values= new ArrayList<>();
    private Button shareBtn;

    private DatabaseHelper db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_water, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        shareBtn = getActivity().findViewById(R.id.share_button);
        shareBtn.setOnClickListener(this::share);

        //create buttons and textview with current water count
        helper = requireActivity().findViewById(R.id.water_helper);
        increaseBtn = requireActivity().findViewById(R.id.water_increase_water);
        increaseBtn.setOnClickListener(this);
        decreaseBtn = requireActivity().findViewById(R.id.water_decrease_water);
        decreaseBtn.setOnClickListener(this);
        current = requireActivity().findViewById(R.id.water_counter_water);

        //get user is from MyApp
        userId = ((MyApp) getActivity()).getUser_id();

        //create date formatter
        DateFormat df = new SimpleDateFormat("dd-MM");

        //load all graph elements
        for(int i = 0; i < 7; i++){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            int dayId = getResources().getIdentifier("day_"+i,"id",getActivity().getPackageName());
            int barId = getResources().getIdentifier("bar_"+i,"id",getActivity().getPackageName());
            int dateId = getResources().getIdentifier("date_"+i,"id",getActivity().getPackageName());
            int layoutId = getResources().getIdentifier("hover_"+i,"id",getActivity().getPackageName());
            days[i] = getActivity().findViewById(dayId);
            bars[i] = getActivity().findViewById(barId);
            dates[i] = getActivity().findViewById(dateId);
            layouts[i] = getActivity().findViewById(layoutId);
            layouts[i].setOnClickListener(this);
            dates[i].setText(df.format(cal.getTime()));
            datesList.add(df.format(cal.getTime()));
        }
        Collections.reverse(datesList);

        updatePage();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(file != null){
            getContext().getContentResolver().delete(file, null, null);
            file = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LOL","Destroyed");
        if(file != null){
            getContext().getContentResolver().delete(file, null, null);
            file = null;
        }
    }

    private void share(View view) {
        TypedValue value = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimary,value,true);
        Chart chart = new Chart(getContext(),null);
        chart.setData(values,datesList,value.data);
        Bitmap bit = convertView(chart);
        file = getUri(bit);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, file);
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private Uri getUri(Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "image", null);
        return Uri.parse(path);
    }

    private Bitmap convertView(View view){
        Bitmap returnedBitmap = Bitmap.createBitmap(1500, 1000,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.extended_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.share_action);
        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        TypedValue value = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimary,value,true);
        Chart chart = new Chart(getContext(),null);
        chart.setData(values,datesList,value.data);
        Bitmap bit = convertView(chart);
        file = getUri(bit);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, file);

        actionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hover_0:
                day = 0;
                break;
            case R.id.hover_1:
                day = 1;
                break;
            case R.id.hover_2:
                day = 2;
                break;
            case R.id.hover_3:
                day = 3;
                break;
            case R.id.hover_4:
                day = 4;
                break;
            case R.id.hover_5:
                day = 5;
                break;
            case R.id.hover_6:
                day = 6;
                break;
            case R.id.water_increase_water:
                increaseWater();
                break;
            case R.id.water_decrease_water:
                decreaseWater();
                break;
        }
        updatePage();
    }

    private void increaseWater() {
        db = new DatabaseHelper(requireActivity());
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);
        if(currentWater == 0){
            Water w = new Water();
            w.setUser_id(userId);
            w.setValue(1);
            w.setDate(df.format(cal.getTime()));
            db.addWater(w);
        } else {
            db.updateWater(userId,df.format(cal.getTime()),currentWater + 1);
        }
        db.close();
    }

    private void decreaseWater() {
        db = new DatabaseHelper(requireActivity());
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);
        if(currentWater > 0){
            db.updateWater(userId,df.format(cal.getTime()),currentWater - 1);
        } else {
            Snackbar.make(getView().findViewById(R.id.container),"Error, can't process",Snackbar.LENGTH_SHORT).show();
        }
        db.close();
    }

    private void updatePage() {
        db = new DatabaseHelper(requireActivity());
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);
        currentWater = db.getWater(userId,df.format(cal.getTime()));
        current.setText(String.valueOf(currentWater));
        StringBuilder str = new StringBuilder();
        str.append("(").append(currentWater*250).append("ml)");
        helper.setText(str.toString());

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(R.attr.tint,typedValue, true);
        @ColorInt int color = typedValue.data;

        for(ConstraintLayout c: layouts){
            c.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        layouts[day].setBackgroundColor(color);
        db.close();
        updateGraph();
    }

    private void updateGraph() {
        //create formatter and database connection
        db = new DatabaseHelper(requireActivity());
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");

        //create values to manage height of bar
        double[] localValues = new double[7];
        int max = 0;
        int min = 999999;
        values.clear();
        //loop through days and set value on graph as well as calculating max/min
        for(int i = 0; i < 7; i++) {
            //take away days bias
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            //get water
            int waterCount = db.getWater(userId,df.format(cal.getTime()));
            localValues[i] = waterCount;
            //max and min calculations
            if(waterCount > max){
                max = waterCount;
            }
            if(waterCount < min){
                min = waterCount;
            }
            values.add(waterCount);
            //set text
            days[i].setText(String.valueOf(waterCount));
        }
        Collections.reverse(values);

        //update the bars height
        int[] heights = new int[7];
        for(int i = 0; i < 7; i++){
            heights[i] = (int) (((maxHeight-minHeight) * ((localValues[i]-min)/(max-min))) + minHeight);
            bars[i].getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heights[i], getResources().getDisplayMetrics());
        }
    }
}