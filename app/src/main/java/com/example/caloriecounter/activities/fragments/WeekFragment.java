package com.example.caloriecounter.activities.fragments;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.activities.Views.Chart;
import com.example.caloriecounter.data.DataProvider;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WeekFragment extends Fragment implements View.OnLongClickListener{

    private Uri file = null;

    private Chart food, water;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //create views
        food = getView().findViewById(R.id.chart);
        water = getView().findViewById(R.id.chart_water);

        food.setOnLongClickListener(this);
        water.setOnLongClickListener(this);

        //create lists for items
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        List<Integer> waterList = new ArrayList<>();

        //get user id from parent activity
        int user = ((MyApp) getActivity()).getUser_id();

        DateFormat df = new SimpleDateFormat("dd-MM-yyy", Locale.UK);
        DateFormat formatter = new SimpleDateFormat("dd-MM",Locale.UK);

        for(int i = 0; i < 7; i++){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            dates.add(formatter.format(cal.getTime()));

            double calories = 0.0;
            int w = 0;

            Cursor c = getActivity().getContentResolver().query(
                    DataProvider.URI_WATER,
                    null,
                    DataProvider.COLUMN_WATER_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_WATER_DATE + " =?",
                    new String[]{String.valueOf(user), df.format(cal.getTime())},
                    null
            );
            while(c.moveToNext()){
                w += c.getInt(c.getColumnIndex(DataProvider.COLUMN_WATER_VALUE));
            }
            c.close();
            c = getActivity().getContentResolver().query(
                    DataProvider.URI_CALORIES,
                    null,
                    DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?",
                    new String[]{String.valueOf(user), df.format(cal.getTime())},
                    null
            );
            while(c.moveToNext()){
                calories += c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
            }
            c.close();
            values.add((int)calories);
            waterList.add(w);
        }

        Collections.reverse(dates);
        Collections.reverse(values);
        Collections.reverse(waterList);

        TypedValue value = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimary,value,true);
        food.setData(values,dates,value.data);
        water.setData(waterList,dates,value.data);


    }

    private Uri getUri(Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "image", null);
        return Uri.parse(path);
    }

    private Bitmap convertView(View view){
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(file != null) {
            getContext().getContentResolver().delete(file, null, null);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Bitmap image = convertView(v);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        file = getUri(image);
        share.putExtra(Intent.EXTRA_STREAM, file);
        startActivity(Intent.createChooser(share, "Share Image"));
        return false;
    }
}