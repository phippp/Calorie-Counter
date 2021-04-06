package com.example.caloriecounter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public DatabaseHelper(Context context) {
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

}