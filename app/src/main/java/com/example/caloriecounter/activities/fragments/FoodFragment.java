package com.example.caloriecounter.activities.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.caloriecounter.R;
import com.example.caloriecounter.activities.MyApp;
import com.example.caloriecounter.activities.Views.Chart;
import com.example.caloriecounter.sql.DatabaseHelper;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FoodFragment extends Fragment {

    private int maxHeight = 100;
    private int minHeight = 10;

    private int userId;

    private ConstraintLayout[] layouts = new ConstraintLayout[7];

    private TextView[] days = new TextView[7];
    private ImageView[] bars = new ImageView[7];
    private TextView[] dates = new TextView[7];
    private ImageView[] selectors = new ImageView[4];

    private TextView[] meals = new TextView[4];
    private ConstraintLayout[] mealsLayouts = new ConstraintLayout[4];
    private TextView current;

    private Uri file = null;

    private List<String> datesList= new ArrayList<>();
    private List<Integer> values= new ArrayList<>();
    private Button shareBtn;

    private int day = 0;
    private final String[] mealNames = {"Breakfast","Lunch","Dinner","Other"};

    private DatabaseHelper db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food, container, false);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        shareBtn = getActivity().findViewById(R.id.share_button);
        shareBtn.setOnClickListener(this::share);

        //get user id from activity
        userId = ((MyApp) getActivity()).getUser_id();
        //create date formatter for graph
        DateFormat formatter = new SimpleDateFormat("dd-MM");
        //load the views for creating popupwindows
        for(int i = 0; i < 4; i++){
            int id = getResources().getIdentifier("selector_"+i,"id",getActivity().getPackageName());
            selectors[i] = getActivity().findViewById(id);
            selectors[i].setOnClickListener(this::onClickShowPopup);
        }
        //create views for meal specific calorie counting
        for(int i = 0; i< 4; i++){
            int mealId = getResources().getIdentifier(mealNames[i].toLowerCase()+"_counter","id",getActivity().getPackageName());
            int layoutId = getResources().getIdentifier(mealNames[i].toLowerCase()+"_container","id",getActivity().getPackageName());
            meals[i] = requireActivity().findViewById(mealId);
            mealsLayouts[i] = requireActivity().findViewById(layoutId);
            mealsLayouts[i].setOnClickListener(this::openMeal);
        }
        current = requireActivity().findViewById(R.id.food_counter_food);
        //create views for graph
        for(int i = 0; i < 7; i++){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            int dayId = getResources().getIdentifier("day_"+i,"id",getActivity().getPackageName());
            int barId = getResources().getIdentifier("bar_"+i,"id",getActivity().getPackageName());
            int dateId = getResources().getIdentifier("date_"+i,"id",getActivity().getPackageName());
            int layoutId = getResources().getIdentifier("layout_"+i,"id",getActivity().getPackageName());
            days[i] = getActivity().findViewById(dayId);
            bars[i] = getActivity().findViewById(barId);
            layouts[i] = getActivity().findViewById(layoutId);
            layouts[i].setOnClickListener(this::onClickLayout);
            dates[i] = getActivity().findViewById(dateId);
            dates[i].setText(formatter.format(cal.getTime()));
            datesList.add(formatter.format(cal.getTime()));
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

    public void onClickLayout(View view){
        //updates current day
        switch (view.getId()){
            case R.id.layout_0:
                day = 0;
                break;
            case R.id.layout_1:
                day = 1;
                break;
            case R.id.layout_2:
                day = 2;
                break;
            case R.id.layout_3:
                day = 3;
                break;
            case R.id.layout_4:
                day = 4;
                break;
            case R.id.layout_5:
                day = 5;
                break;
            case R.id.layout_6:
                day = 6;
                break;
        }
        updatePage();
    }

    private void updatePage() {
        //create database and calendar elements
        db = new DatabaseHelper(requireActivity());
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);
        //set graph backgrounds
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(R.attr.tint,typedValue, true);
        @ColorInt int color = typedValue.data;
        for(ConstraintLayout c: layouts){
            c.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        layouts[day].setBackgroundColor(color);
        //update calorie counters
        double food = db.getTotalFood(userId,df.format(cal.getTime()));
        current.setText(getString(R.string.calories,(int)food));
        for(int i = 0; i < 4; i++){
            double val = db.getMealVal(userId,df.format(cal.getTime()),mealNames[i]);
            meals[i].setText(getString(R.string.calories,(int)val));
        }
        updateGraph();
        db.close();
    }

    private void updateGraph() {
        //create date formatter for db
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        //local values to calculate bar heights
        double[] localValues = new double[7];
        double max = 0.0;
        double min = 999999.0;
        //set values and calculate max/min
        values.clear();
        for(int i = 0; i < 7; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -i);
            double calories = db.getTotalFood(userId, df.format(cal.getTime()));
            localValues[i] = calories;
            if (calories > max) {
                max = calories;
            }
            if(calories < min) {
                min = calories;
            }
            values.add((int)calories);
            days[i].setText(String.valueOf((int)calories));
        }
        Collections.reverse(values);
        //update heights for bars
        int[] heights = new int[7];
        for(int i = 0; i < 7; i++){
            heights[i] = (int) (((maxHeight-minHeight) * ((localValues[i]-min)/(max-min))) + minHeight);
            bars[i].getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heights[i], getResources().getDisplayMetrics());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickShowPopup(View view){
        String type = "";
        switch (view.getId()){
            case R.id.selector_0:
                type = "Breakfast";
                break;
            case R.id.selector_1:
                type = "Lunch";
                break;
            case R.id.selector_2:
                type = "Dinner";
                break;
            case R.id.selector_3:
                type = "Other";
                break;
        }

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_add_delete, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //popupWindow.showAtLocation(view, Gravity.BOTTOM, x, y);
        popupWindow.showAsDropDown(view,0,-400);

        String finalType = type;
        popupView.findViewById(R.id.delete_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = new DatabaseHelper(getActivity());
                DateFormat df = new SimpleDateFormat("dd-MM-yyy");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE,-day);
                db.dropMeals(userId,df.format(cal.getTime()),finalType);
                db.close();
                updatePage();
                popupWindow.dismiss();
            }
        });


        popupView.findViewById(R.id.add_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat("dd-MM-yyy");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE,-day);
                Bundle b = new Bundle();
                b.putString("type", finalType);
                b.putString("date",df.format(cal.getTime()));
                Navigation.findNavController(view).navigate(R.id.search_food,b);
                popupWindow.dismiss();
            }
        });

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    public void openMeal(View view){
        String type = "";
        switch (view.getId()){
            case R.id.breakfast_container:
                type = "Breakfast";
                break;
            case R.id.lunch_container:
                type = "Lunch";
                break;
            case R.id.dinner_container:
                type = "Dinner";
                break;
            case R.id.other_container:
                type = "Other";
                break;
        }
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);

        Bundle b = new Bundle();
        b.putString("type",type);
        b.putString("date",df.format(cal.getTime()));

        Navigation.findNavController(view).navigate(R.id.meal_fragment,b);
    }

}