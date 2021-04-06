package com.example.caloriecounter.background;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.data.DatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotificationCreator extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("Channel", "Meal Update Checker", NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            mChannel.setVibrationPattern(new long[]{100,200,300,400,500,400,300,200,400});
            manager.createNotificationChannel(mChannel);
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        SharedPreferences loggedInUser = context.getSharedPreferences("LoggedInUser", Context.MODE_PRIVATE);
        String username = loggedInUser.getString("username",null);
        String password = loggedInUser.getString("password",null);
        int user_id = -1;
        DatabaseHelper db = new DatabaseHelper(context);
        DateFormat df = new SimpleDateFormat("dd-MM-yyy");
        if(username != null) {

            Cursor c = context.getContentResolver().query(
                    DataProvider.URI_USER,
                    null,
                    DataProvider.COLUMN_USER_USERNAME + " = ?" +" AND " + DataProvider.COLUMN_USER_PASSWORD + " =?",
                    new String[]{ username, password },
                    null);
            if(c.getCount() > 0) {
                c.moveToNext();
                user_id = c.getInt(c.getColumnIndex(DataProvider.COLUMN_USER_ID));
            }
            //user_id = db.getUserId(username,password);
        }

        String message = "";

        if(user_id != -1) {
            if (hour < 6) {
                message = "It's a new day, don't forget to add your meals!";
            } else if (hour < 12) {
                int cals = 0;//(int) db.getMealVal(user_id, df.format(Calendar.getInstance().getTime()), "Breakfast");

                Cursor c = context.getContentResolver().query(
                        DataProvider.URI_CALORIES,
                        null,
                        DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_MEAL + " = ?",
                        new String[]{String.valueOf(user_id),df.format(Calendar.getInstance().getTime()), "Breakfast"},
                        null
                );
                while(c.moveToNext()){
                    cals += c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
                }

                if (cals == 0) {
                    message = "Make sure to enter your breakfast into the app";
                } else {
                    message = "Make sure you enter any snacks you have before lunch!";
                }
            } else if (hour < 16) {
                int cals = 0;//(int) db.getMealVal(user_id, df.format(Calendar.getInstance().getTime()), "Breakfast");

                Cursor c = context.getContentResolver().query(
                        DataProvider.URI_CALORIES,
                        null,
                        DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_MEAL + " = ?",
                        new String[]{String.valueOf(user_id),df.format(Calendar.getInstance().getTime()), "Lunch"},
                        null
                );
                while(c.moveToNext()){
                    cals += c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
                }
                if (cals == 0) {
                    message = "Make sure to enter your lunch into the app";
                } else {
                    message = "Make sure you enter any snacks you have before dinner!";
                }
            } else {
                int cals = 0;//(int) db.getMealVal(user_id, df.format(Calendar.getInstance().getTime()), "Breakfast");

                Cursor c = context.getContentResolver().query(
                        DataProvider.URI_CALORIES,
                        null,
                        DataProvider.COLUMN_CALORIES_USER_ID + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_DATE + " = ?" + " AND " + DataProvider.COLUMN_CALORIES_MEAL + " = ?",
                        new String[]{String.valueOf(user_id),df.format(Calendar.getInstance().getTime()), "Dinner"},
                        null
                );
                while(c.moveToNext()){
                    cals += c.getDouble(c.getColumnIndex(DataProvider.COLUMN_CALORIES_VALUE));
                }
                if (cals == 0) {
                    message = "Make sure to enter your dinner into the app";
                } else {
                    message = "Make sure you enter any snacks you have after dinner!";
                }
            }
        } else {
            message = "Log in to keep track of your calories!";
        }

        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, "Channel")
                    .setSmallIcon(R.drawable.baseline_restaurant_black_24)
                    .setContentTitle("Update your meal")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
        } else {
            builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.baseline_restaurant_black_24)
                    .setContentTitle("Update your meal")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
        }

        manager.notify(1, builder.build());
    }

    public void createStickyNotification(Context context){
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationCreator.class);
        PendingIntent pending = PendingIntent.getBroadcast(context,0,intent,0);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (1000 * 60 * 60), 1000 * 60 * 60 * 4 ,pending); //4 hours -> 1000 * 60 * 60 * 4
    }

    //to delete the notification if needed
    public void cancelNotification(Context context){
        Intent intent = new Intent(context, NotificationCreator.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}