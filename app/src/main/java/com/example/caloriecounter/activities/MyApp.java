package com.example.caloriecounter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.caloriecounter.sql.DatabaseHelper;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.caloriecounter.R;

public class MyApp extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private String username;
    private String password;
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_app);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_today, R.id.nav_week, R.id.nav_help, R.id.search_food, R.id.add_food)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //gets user and updates navslider
        SharedPreferences loggedInUser = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        username = loggedInUser.getString("username",null);
        password = loggedInUser.getString("password",null);
        if(username != null) {
            View header = navigationView.getHeaderView(0);
            TextView navUsername = (TextView) header.findViewById(R.id.username_nav);
            navUsername.setText(username);
            DatabaseHelper db = new DatabaseHelper(this);
            user_id = db.getUserId(username,password);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_app, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    public void logout(MenuItem item){
        SharedPreferences loggedInUser = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        loggedInUser.edit().clear().commit();
        Intent in = new Intent(MyApp.this,LoginActivity.class);
        startActivity(in);
        finish();
    }

    public void settings(MenuItem item){
        Intent in = new Intent(MyApp.this,SettingsActivity.class);
        startActivity(in);
    }

    public int getUser_id(){
        return this.user_id;
    }

}