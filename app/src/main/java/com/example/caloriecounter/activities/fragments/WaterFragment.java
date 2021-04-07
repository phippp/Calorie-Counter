package com.example.caloriecounter.activities.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.caloriecounter.data.DataProvider;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WaterFragment extends Fragment implements View.OnClickListener {

    private int userId;

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

        Button shareBtn = getView().findViewById(R.id.share_button);
        shareBtn.setOnClickListener(this::share);

        //create buttons and textview with current water count
        helper = getView().findViewById(R.id.water_helper);
        Button increaseBtn = getView().findViewById(R.id.water_increase_water);
        increaseBtn.setOnClickListener(this);
        Button decreaseBtn = getView().findViewById(R.id.water_decrease_water);
        decreaseBtn.setOnClickListener(this);
        current = getView().findViewById(R.id.water_counter_water);

        //get user is from MyApp
        userId = ((MyApp) getActivity()).getUser_id();

        //create date formatter
        DateFormat df = new SimpleDateFormat("dd-MM", Locale.UK);

        //load all graph elements
        for(int i = 0; i < 7; i++){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            int dayId = getResources().getIdentifier("day_"+i,"id",getActivity().getPackageName());
            int barId = getResources().getIdentifier("bar_"+i,"id",getActivity().getPackageName());
            int dateId = getResources().getIdentifier("date_"+i,"id",getActivity().getPackageName());
            int layoutId = getResources().getIdentifier("hover_"+i,"id",getActivity().getPackageName());
            days[i] = getView().findViewById(dayId);
            bars[i] = getView().findViewById(barId);
            dates[i] = getView().findViewById(dateId);
            layouts[i] = getView().findViewById(layoutId);
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
        //remove file from storage to stop memory problems and renaming problems
        if(file != null){
            getContext().getContentResolver().delete(file, null, null);
            file = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //remove file from storage to stop memory problems and renaming problems
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
    public void onClick(View view) {
        if(view.getId() == R.id.hover_0){
            day = 0;
        } else if(view.getId() == R.id.hover_1){
            day = 1;
        }else if(view.getId() == R.id.hover_2){
            day = 2;
        }else if(view.getId() == R.id.hover_3){
            day = 3;
        }else if(view.getId() == R.id.hover_4){
            day = 4;
        }else if(view.getId() == R.id.hover_5){
            day = 5;
        }else if(view.getId() == R.id.hover_6){
            day = 6;
        }else if(view.getId() == R.id.water_increase_water){
            increaseWater();
        }else if(view.getId() == R.id.water_decrease_water){
            decreaseWater();
        }
        updatePage();
    }

    private void increaseWater() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);

        boolean exists = false;

        Cursor c = getActivity().getContentResolver().query(
             DataProvider.URI_WATER,
             null,
             DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
             new String[]{String.valueOf(userId), df.format(cal.getTime())},
             null
        );
        if(c.getCount() > 0){
            exists = true;
        }
        c.close();

        ContentValues values = new ContentValues();
        if(!exists){

            values.put(DataProvider.COLUMN_WATER_VALUE,1);
            values.put(DataProvider.COLUMN_WATER_USER_ID,userId);
            values.put(DataProvider.COLUMN_WATER_DATE, df.format(cal.getTime()));

            getActivity().getContentResolver().insert(DataProvider.URI_WATER,values);

        } else {

            values.put(DataProvider.COLUMN_WATER_VALUE, currentWater + 1);

            getActivity().getContentResolver().update(
                    DataProvider.URI_WATER,
                    values,
                    DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                    new String[]{String.valueOf(userId), df.format(cal.getTime())}
            );
        }
    }

    private void decreaseWater() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);
        if(currentWater > 0){
            //db.updateWater(userId,df.format(cal.getTime()),currentWater - 1);

            ContentValues values = new ContentValues();
            values.put(DataProvider.COLUMN_WATER_VALUE, currentWater -1);

            getActivity().getContentResolver().update(
                    DataProvider.URI_WATER,
                    values,
                    DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                    new String[]{String.valueOf(userId), df.format(cal.getTime())}
            );


        } else {
            Snackbar.make(getView().findViewById(R.id.container),"Error, can't process",Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updatePage() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);
        currentWater = 0;//db.getWater(userId,df.format(cal.getTime()));

        Cursor c = getActivity().getContentResolver().query(
                DataProvider.URI_WATER,
                null,
                DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                new String[]{String.valueOf(userId), df.format(cal.getTime())},
                null
        );
        while(c.moveToNext()){
            currentWater += c.getInt(c.getColumnIndex(DataProvider.COLUMN_WATER_VALUE));
        }

        c.close();

        current.setText(String.valueOf(currentWater));
        helper.setText(getResources().getString(R.string.current_water,currentWater*250));

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(R.attr.tint,typedValue, true);
        @ColorInt int color = typedValue.data;

        for(ConstraintLayout con: layouts){
            con.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        layouts[day].setBackgroundColor(color);
        updateGraph();
    }

    private void updateGraph() {
        //create formatter and database connection
        DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);

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
            int waterCount = 0;//db.getWater(userId,df.format(cal.getTime()));

            Cursor c = getActivity().getContentResolver().query(
                    DataProvider.URI_WATER,
                    null,
                    DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                    new String[]{String.valueOf(userId), df.format(cal.getTime())},
                    null
            );
            while(c.moveToNext()){
                waterCount += c.getInt(c.getColumnIndex(DataProvider.COLUMN_WATER_VALUE));
            }
            c.close();
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
            int maxHeight = 100;
            int minHeight = 10;
            heights[i] = (int) (((maxHeight - minHeight) * ((localValues[i]-min)/(max-min))) + minHeight);
            bars[i].getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heights[i], getResources().getDisplayMetrics());
        }
    }
}