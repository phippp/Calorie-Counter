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
import com.example.caloriecounter.data.DatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class WeekFragment extends Fragment implements View.OnClickListener{

    private DatabaseHelper dbHelper;

    private Uri file = null;

    private Button shareBtn;

    private Chart chart;
    private Chart chart_water;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_week, container, false);

        chart_water = v.findViewById(R.id.chart_water);
        chart = v.findViewById(R.id.chart);
        shareBtn = v.findViewById(R.id.share_btn);

        return v;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbHelper = new DatabaseHelper(requireActivity());

        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        List<Integer> water = new ArrayList<>();

        int user = ((MyApp) getActivity()).getUser_id();

        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        DateFormat formatter = new SimpleDateFormat("dd-MM");

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

            values.add((int)calories);
            water.add(w);
        }

        Collections.reverse(dates);
        Collections.reverse(values);
        Collections.reverse(water);

        TypedValue value = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimary,value,true);
        chart.setData(values,dates,value.data);
        chart_water.setData(water,dates,value.data);

        shareBtn.setOnClickListener(this);

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
    public void onClick(View v) {
        Bitmap image = convertView(chart);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        file = getUri(image);
        share.putExtra(Intent.EXTRA_STREAM, file);
        startActivity(Intent.createChooser(share, "Share Image"));
    }
}