package com.example.caloriecounter.activities.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.activities.Views.Chart;
import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

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
            values.add((int) dbHelper.getTotalFood(user,df.format( cal.getTime() ) ));
            water.add(dbHelper.getWater(user,df.format( cal.getTime() )));
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