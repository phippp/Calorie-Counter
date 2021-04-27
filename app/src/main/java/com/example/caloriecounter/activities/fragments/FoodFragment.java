package com.example.caloriecounter.activities.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
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
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FoodFragment extends Fragment {

    private int userId;

    private String dateInFragment, mealInFragment;

    private boolean tablet;

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

    private int day = 0;
    private final String[] mealNames = {"Breakfast","Lunch","Dinner","Other"};

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
        if(file != null){
            requireContext().getContentResolver().delete(file, null, null);
            file = null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tablet = getView().findViewById(R.id.extra_fragment) != null;

        if(tablet && savedInstanceState!=null){
            Bundle b = new Bundle();
            b.putString("type",savedInstanceState.getString("meal"));
            b.putString("date",savedInstanceState.getString("date"));
            Fragment fragment = new MealFragment();
            fragment.setArguments(b);
            FragmentManager fm = getParentFragmentManager();
            fm.beginTransaction().replace(R.id.extra_fragment,fragment).commit();
            mealInFragment = savedInstanceState.getString("meal");
            dateInFragment = savedInstanceState.getString("date");
        }

        //share button to share (the better way)
        Button shareBtn = getView().findViewById(R.id.share_button);
        shareBtn.setOnClickListener(this::share);

        //get user id from activity
        userId = ((MyApp) requireActivity()).getUser_id();

        //create date formatter for graph
        DateFormat formatter = new SimpleDateFormat("dd-MM", Locale.UK);

        //load the views for creating popup windows
        for(int i = 0; i < 4; i++){
            int id = getResources().getIdentifier("selector_"+i,"id",requireActivity().getPackageName());
            selectors[i] = getView().findViewById(id);
            selectors[i].setOnClickListener(this::onClickShowPopup);
        }

        //create views for meal specific calorie counting
        for(int i = 0; i< 4; i++){
            int mealId = getResources().getIdentifier(mealNames[i].toLowerCase()+"_counter","id",requireActivity().getPackageName());
            int layoutId = getResources().getIdentifier(mealNames[i].toLowerCase()+"_container","id",requireActivity().getPackageName());
            meals[i] = getView().findViewById(mealId);
            mealsLayouts[i] = getView().findViewById(layoutId);
            mealsLayouts[i].setOnClickListener(this::openMeal);
        }
        //view for the selected daily total
        current = getView().findViewById(R.id.food_counter_food);

        //create views for graph
        for(int i = 0; i < 7; i++){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-i);
            int dayId = getResources().getIdentifier("day_"+i,"id",requireActivity().getPackageName());
            int barId = getResources().getIdentifier("bar_"+i,"id",requireActivity().getPackageName());
            int dateId = getResources().getIdentifier("date_"+i,"id",requireActivity().getPackageName());
            int layoutId = getResources().getIdentifier("layout_"+i,"id",requireActivity().getPackageName());
            days[i] = getView().findViewById(dayId);
            bars[i] = getView().findViewById(barId);
            layouts[i] = getView().findViewById(layoutId);
            layouts[i].setOnClickListener(this::onClickLayout);
            dates[i] = getView().findViewById(dateId);
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
            requireContext().getContentResolver().delete(file, null, null);
            file = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(tablet){
            outState.putString("date",dateInFragment);
            outState.putString("meal",mealInFragment);
        }
    }

    private void share(View view) {
        //share using a chooser
        TypedValue value = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimary,value,true);
        //create custom view to share
        Chart chart = new Chart(getContext(),null);
        chart.setData(values,datesList,value.data);
        Bitmap bit = convertView(chart);
        //convert to file and store
        file = getUri(bit);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, file);
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private Uri getUri(Bitmap bitmap){
        //takes a bitmap and converts it to a JPEG that is saved locally
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), bitmap, "image", null);
        return Uri.parse(path);
    }

    private Bitmap convertView(View view){
        //takes a view and turns it into a bitmap
        Bitmap returnedBitmap = Bitmap.createBitmap(1500, 1000,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //create shareActionProvider menu
        inflater.inflate(R.menu.extended_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.share_action);
        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        //create view to share
        TypedValue value = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimary,value,true);
        Chart chart = new Chart(getContext(),null);
        chart.setData(values,datesList,value.data);
        Bitmap bit = convertView(chart);
        file = getUri(bit);
        //create intent
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, file);
        //set share action
        actionProvider.setShareIntent(shareIntent);
    }

    public void onClickLayout(View view){
        //updates current day
        if(view.getId() == R.id.layout_0){
            day = 0;
        } else if(view.getId() == R.id.layout_1){
            day = 1;
        }else if(view.getId() == R.id.layout_2){
            day = 2;
        }else if(view.getId() == R.id.layout_3){
            day = 3;
        }else if(view.getId() == R.id.layout_4){
            day = 4;
        }else if(view.getId() == R.id.layout_5){
            day = 5;
        }else if(view.getId() == R.id.layout_6){
            day = 6;
        }
        updatePage();
    }

    private void updatePage() {
        //create database and calendar elements
        DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
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
        double food = 0;
        double[] mealVals = new double[]{0.0,0.0,0.0,0.0};
        //gets all items for that day
        Cursor c = requireActivity().getContentResolver().query(
                DataProvider.URI_CALORIES,
                null,
                DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?",
                new String[]{String.valueOf(userId), df.format(cal.getTime())},
                null
        );
        //loops through all items
        while(c.moveToNext()){
            double val = c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
            food += val;
            String meal = c.getString(c.getColumnIndex(DataProvider.COLUMN_CALORIES_MEAL));
            if(meal.equals(mealNames[0])){
                mealVals[0] += val;
            }else if(meal.equals(mealNames[1])){
                mealVals[1] += val;
            }else if(meal.equals(mealNames[2])){
                mealVals[2] += val;
            }else if(meal.equals(mealNames[3])){
                mealVals[3] += val;
            }
        }
        //close cursor to free memory
        c.close();
        //update text views
        current.setText(getString(R.string.calories,(int)food));
        for(int i = 0; i < 4; i++){
            double val = mealVals[i];
            meals[i].setText(getString(R.string.calories,(int)val));
        }
        updateGraph();
    }

    private void updateGraph() {
        //create date formatter for db
        DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
        //local values to calculate bar heights
        double[] localValues = new double[7];
        double max = 0.0;
        double min = 999999.0;
        //set values and calculate max/min
        values.clear();
        for(int i = 0; i < 7; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -i);
            double calories = 0;
            //gets all items for that day
            Cursor c = requireActivity().getContentResolver().query(
                    DataProvider.URI_CALORIES,
                    null,
                    DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?",
                    new String[]{String.valueOf(userId), df.format(cal.getTime())},
                    null
            );
            while(c.moveToNext()){
                calories += c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
            }
            //frees cursor
            c.close();
            //calculate max & min
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
            int maxHeight = 100;
            int minHeight = 10;
            heights[i] = (int) (((maxHeight - minHeight) * ((localValues[i]-min)/(max-min))) + minHeight);
            bars[i].getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heights[i], getResources().getDisplayMetrics());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickShowPopup(View view){
        String type = "";
        if(view.getId() == R.id.selector_0){
            type = "Breakfast";
        } else if(view.getId() == R.id.selector_1){
            type = "Lunch";
        }else if(view.getId() == R.id.selector_2){
            type = "Dinner";
        }else if(view.getId() == R.id.selector_3){
            type = "Other";
        }

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_add_delete, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        //move up 400px so that it doesn't go off the screen
        popupWindow.showAsDropDown(view,0,-400);

        String finalType = type;
        popupView.findViewById(R.id.delete_container).setOnClickListener(v -> {
            DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-day);

            requireActivity().getContentResolver().delete(
                    DataProvider.URI_CALORIES,
                    DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " =?" + " AND " + DataProvider.COLUMN_CALORIES_MEAL + " = ?",
                    new String[]{String.valueOf(userId), df.format(cal.getTime()), finalType}
            );

            updatePage();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.add_container).setOnClickListener(v -> {
            DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-day);
            Bundle b = new Bundle();
            b.putString("type", finalType);
            b.putString("date",df.format(cal.getTime()));
            Navigation.findNavController(view).navigate(R.id.search_food,b);
            popupWindow.dismiss();
        });

    }

    public void openMeal(View view){
        String type = "";
        if(view.getId() == R.id.breakfast_container){
            type = "Breakfast";
        } else if(view.getId() == R.id.lunch_container){
            type = "Lunch";
        }else if(view.getId() == R.id.dinner_container){
            type = "Dinner";
        }else if(view.getId() == R.id.other_container){
            type = "Other";
        }
        DateFormat df = new SimpleDateFormat("dd-MM-yyy",Locale.UK);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-day);

        Bundle b = new Bundle();
        b.putString("type",type);
        b.putString("date",df.format(cal.getTime()));

        if(!tablet) {
            Navigation.findNavController(view).navigate(R.id.meal_fragment, b);
        } else {
            Fragment fragment = new MealFragment();
            fragment.setArguments(b);
            FragmentManager fm = getParentFragmentManager();
            fm.beginTransaction().replace(R.id.extra_fragment,fragment).commit();
            mealInFragment = type;
            dateInFragment = df.format(cal.getTime());
        }
    }

}