package com.example.caloriecounter.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.example.caloriecounter.data.DataProvider";
    public static final Uri URI_USER = Uri.parse("content://" + PROVIDER_NAME + "/user");
    public static final Uri URI_CALORIES = Uri.parse("content://" + PROVIDER_NAME + "/calories");
    public static final Uri URI_WATER = Uri.parse("content://" + PROVIDER_NAME + "/water");

    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_USERNAME = "user_name";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_USER_PASSWORD = "user_password";

    public static final String COLUMN_CALORIES_ID = "calories_id";
    public static final String COLUMN_CALORIES_USER_ID = "user_id";
    public static final String COLUMN_CALORIES_VALUE = "calories_value";
    public static final String COLUMN_CALORIES_DATE = "calories_date";
    public static final String COLUMN_CALORIES_DATA = "food_data";
    public static final String COLUMN_CALORIES_MEAL = "calories_meal";

    public static final String COLUMN_WATER_ID = "water_id";
    public static final String COLUMN_WATER_USER_ID = "user_id";
    public static final String COLUMN_WATER_VALUE = "water_value";
    public static final String COLUMN_WATER_DATE = "water_date";

    static final int USERS = 1;
    static final int USER_ID = 2;
    static final int CALORIES = 3;
    static final int CALORIE_ID = 4;
    static final int WATER = 5;
    static final int WATER_ID = 6;

    private SQLiteDatabase db;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,"user",USERS);
        uriMatcher.addURI(PROVIDER_NAME,"user/#",USER_ID);
        uriMatcher.addURI(PROVIDER_NAME,"calories",CALORIES);
        uriMatcher.addURI(PROVIDER_NAME,"calories/#",CALORIE_ID);
        uriMatcher.addURI(PROVIDER_NAME,"water",WATER);
        uriMatcher.addURI(PROVIDER_NAME,"water/#",WATER_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
        return db != null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri _uri = null;
        long id;
        switch (uriMatcher.match(uri)){
            case USERS:
                id = db.insert("user", "", values);
                break;
            case CALORIES:
                id = db.insert("calories", "", values);
                break;
            case WATER:
                id = db.insert("water", "", values);
                break;
            default: throw new SQLException("Failed to insert row into " + uri);
        }
        if(id != -1){
            _uri = ContentUris.withAppendedId(URI_USER, id);
            getContext().getContentResolver().notifyChange(_uri, null);
        }
        return _uri;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        long _id;
        switch (uriMatcher.match(uri)){
            case USERS:
                cursor = db.query(
                        "user",
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case USER_ID:
                _id = ContentUris.parseId(uri);
                cursor = db.query(
                        "user",
                        projection,
                        COLUMN_USER_ID + " = ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case CALORIES:
                cursor = db.query(
                        "calories",
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CALORIE_ID:
                _id = ContentUris.parseId(uri);
                cursor = db.query(
                        "calories",
                        projection,
                        COLUMN_CALORIES_ID + " = ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case WATER:
                cursor = db.query(
                        "water",
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case WATER_ID:
                _id = ContentUris.parseId(uri);
                cursor = db.query(
                        "water",
                        projection,
                        COLUMN_WATER_ID + " = ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows;
        switch (uriMatcher.match(uri)) {
            case USERS:
                rows = db.delete("user",selection,selectionArgs);
                break;
            case CALORIES:
                rows = db.delete("calories",selection,selectionArgs);
                break;
            case WATER:
                rows = db.delete("water",selection,selectionArgs);
                break;
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(selection == null || rows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows;
        switch (uriMatcher.match(uri)) {
            case USERS:
                rows = db.update("user",values,selection,selectionArgs);
                break;
            case CALORIES:
                rows = db.update("calories",values,selection,selectionArgs);
                break;
            case WATER:
                rows = db.update("water",values,selection,selectionArgs);
                break;
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rows!=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }
}
