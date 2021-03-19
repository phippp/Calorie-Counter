package com.example.caloriecounter.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.caloriecounter.model.adapters.FoodItem;
import com.example.caloriecounter.model.database.Calories;
import com.example.caloriecounter.model.database.User;
import com.example.caloriecounter.model.database.Water;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "UserManager.db";
    private static final String TABLE_USER = "user";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_USERNAME = "user_name";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PASSWORD = "user_password";

    private static final String TABLE_CALORIES = "calories";
    private static final String COLUMN_CALORIES_ID = "calories_id";
    private static final String COLUMN_CALORIES_USER_ID = "user_id";
    private static final String COLUMN_CALORIES_VALUE = "calories_value";
    private static final String COLUMN_CALORIES_DATE = "calories_date";
    private static final String COLUMN_CALORIES_DATA = "food_data";
    private static final String COLUMN_CALORIES_MEAL = "calories_meal";

    private static final String TABLE_WATER = "water";
    private static final String COLUMN_WATER_ID = "water_id";
    private static final String COLUMN_WATER_USER_ID = "user_id";
    private static final String COLUMN_WATER_VALUE = "water_value";
    private static final String COLUMN_WATER_DATE = "water_date";

    private String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_USERNAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT," + COLUMN_USER_PASSWORD + " TEXT" + ");";

    private String CREATE_TABLE_CALORIES = "CREATE TABLE " + TABLE_CALORIES + "("
            + COLUMN_CALORIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CALORIES_USER_ID + " INTEGER,"
            + COLUMN_CALORIES_VALUE + " REAL," + COLUMN_CALORIES_DATE + " TEXT,"
            + COLUMN_CALORIES_DATA + " TEXT," + COLUMN_CALORIES_MEAL + " TEXT, "
            + " FOREIGN KEY (" + COLUMN_CALORIES_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + "));";

    private String CREATE_TABLE_WATER = "CREATE TABLE " + TABLE_WATER + "("
            + COLUMN_WATER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_WATER_USER_ID + " INTEGER,"
            + COLUMN_WATER_VALUE + " INTEGER," + COLUMN_WATER_DATE + " TEXT,"
            + " FOREIGN KEY (" + COLUMN_WATER_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + "));";

    private String DROP_TABLE_USER = "DROP TABLE IF EXISTS " + TABLE_USER + ";";

    private String DROP_TABLE_CALORIES = "DROP TABLE IF EXISTS " + TABLE_CALORIES + ";";

    private String DROP_TABLE_WATER = "DROP TABLE IF EXISTS " + TABLE_WATER + ";";

    private String INSERT_USER = "INSERT INTO " + TABLE_USER + "(" + COLUMN_USER_PASSWORD + ", " +
            COLUMN_USER_EMAIL + ", " + COLUMN_USER_USERNAME + ") " + "VALUES('eee','eee','eee');";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_CALORIES);
        db.execSQL(CREATE_TABLE_WATER);
        db.execSQL(INSERT_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_CALORIES);
        db.execSQL(DROP_TABLE_WATER);
        db.execSQL(DROP_TABLE_USER);
        onCreate(db);
    }

    public void addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, user.getUsername());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());

        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public void addCalories(Calories calories){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CALORIES_USER_ID, calories.getUser_id());
        values.put(COLUMN_CALORIES_DATA, calories.getData().toString());
        values.put(COLUMN_CALORIES_DATE, calories.getDate());
        values.put(COLUMN_CALORIES_VALUE, calories.getValue());
        values.put(COLUMN_CALORIES_MEAL, calories.getType());

        db.insert(TABLE_CALORIES, null, values);
        db.close();
    }

    public void addWater(Water water){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WATER_USER_ID, water.getUser_id());
        values.put(COLUMN_WATER_DATE, water.getDate());
        values.put(COLUMN_WATER_VALUE, water.getValue());

        db.insert(TABLE_WATER, null, values);
        db.close();
    }

    public boolean checkUser(String username, String password){
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_USERNAME + " = ?" +" AND " + COLUMN_USER_PASSWORD + " =?";
        String[] selectionArgs = { username, password };

        Cursor cursor = db.query(TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }

    public boolean checkUser(String email){
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }

    public int getUserId(String username, String password){
        int id = -1;
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_USERNAME + " = ?" +" AND " + COLUMN_USER_PASSWORD + " =?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        if(cursor.getCount() > 0){
            cursor.moveToNext();
            id = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return id;
    }

    public double getTotalFood(int user_id, String date){
        double total = 0.0;
        String[] columns = {
                COLUMN_CALORIES_VALUE
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_CALORIES_USER_ID + " = ?" + " AND " + COLUMN_CALORIES_DATE + " =?";
        String[] selectionArgs = {String.valueOf(user_id), date};
        Cursor cursor = db.query(TABLE_CALORIES,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        while(cursor.moveToNext()){
            total += cursor.getDouble(cursor.getColumnIndex(COLUMN_CALORIES_VALUE));
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getMealVal(int user_id, String date, String meal){
        double total = 0.0;
        String[] columns = {
                COLUMN_CALORIES_VALUE
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_CALORIES_USER_ID + " = ?" + " AND " + COLUMN_CALORIES_DATE + " =?" + " AND " + COLUMN_CALORIES_MEAL + " =?";
        String[] selectionArgs = {String.valueOf(user_id), date, meal};
        Cursor cursor = db.query(TABLE_CALORIES,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        while(cursor.moveToNext()){
            total += cursor.getDouble(cursor.getColumnIndex(COLUMN_CALORIES_VALUE));
        }
        cursor.close();
        db.close();
        return total;
    }

    public void dropMeals(int user_id, String date, String meal){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_CALORIES_USER_ID + " = ?" + " AND " + COLUMN_CALORIES_DATE + " =?" + " AND " + COLUMN_CALORIES_MEAL + " =?";
        String[] selectionArgs = {String.valueOf(user_id), date, meal};
        db.delete(TABLE_CALORIES,selection,selectionArgs);
    }

    public FoodItem[] getFoodItems(int user_id, String date){
        ArrayList<FoodItem> list = new ArrayList<>();
        String[] columns = {
                COLUMN_CALORIES_VALUE,
                COLUMN_CALORIES_DATA
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_CALORIES_USER_ID + " = ?" + " AND " + COLUMN_CALORIES_DATE + " =?";
        String[] selectionArgs = {String.valueOf(user_id), date};
        Cursor cursor = db.query(TABLE_CALORIES,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        while(cursor.moveToNext()){
            JSONObject obj = new JSONObject();
            String name = "";
            double val = cursor.getDouble(cursor.getColumnIndex(COLUMN_CALORIES_VALUE));
            try{
                obj = new JSONObject(cursor.getString(cursor.getColumnIndex(COLUMN_CALORIES_DATA)));
                name = obj.getJSONObject("food").getString("label");
            } catch (JSONException e){
                e.printStackTrace();
            }
            list.add(new FoodItem(name,obj,val));
        }
        cursor.close();
        db.close();
        return list.toArray(new FoodItem[0]);
    }

    public int getWater(int user_id, String date){
        int num = 0;
        String[] columns = {
                COLUMN_WATER_VALUE
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_WATER_USER_ID + " = ?" + " AND " + COLUMN_WATER_DATE + " =?";
        String[] selectionArgs = {String.valueOf(user_id), date};
        Cursor cursor = db.query(TABLE_WATER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        while(cursor.moveToNext()){
            num = cursor.getInt(cursor.getColumnIndex(COLUMN_WATER_VALUE));
        }
        cursor.close();
        db.close();
        return num;
    }

    public void updateWater(int user_id, String date,int val){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_WATER_VALUE, val);
            String selection = COLUMN_WATER_USER_ID + " =?" + " AND " + COLUMN_WATER_DATE + " =?";
            String[] selectionArgs = {String.valueOf(user_id), date};
            db.update(TABLE_WATER, cv, selection, selectionArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
