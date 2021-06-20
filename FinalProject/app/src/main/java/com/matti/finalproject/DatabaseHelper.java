package com.matti.finalproject;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * DATABASE_NAME is basically a "document name".
 * TABLE_NAME is the name of the tables we are referring to.
 * COl# is for storing all the data.
 * refer:  https://github.com/mitchtabian/SQLiteSaveUserData
 * https://www.youtube.com/watch?v=sK15YvRIdqY
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "database.db";
    public static final String TABLE_NAME = "markers";
    public static final String COL1 = "TITLE";
    public static final String COL2 = "SNIPPET";
    public static final String COL3 = "LATITUDE";
    public static final String COL4 = "LONGITUDE";
    public static final String COL5 = "MODE";
    public int DataNumber;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = " CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " TITLE TEXT, SNIPPET TEXT, LATITUDE TEXT , LONGITUDE TEXT, MODE TEXT)";
        db.execSQL(createTable);
        DataNumber = 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public int getDataNumber(){
        return DataNumber;
    }

    public boolean addData(String title, String snippet, String latitude, String longitude, String mode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1,title);
        contentValues.put(COL2,snippet);
        contentValues.put(COL3, latitude);
        contentValues.put(COL4,longitude);
        contentValues.put(COL5,mode);

        long result  = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            DataNumber += 1 ;
            return true;
        }
    }

    public Cursor showAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

    public Cursor getData (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + "WHERE ID=" + id+"", null);
        return data;
    }

    public String getLat (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT LATITUDE FROM " + TABLE_NAME + "WHERE ID=" + id+"", null);
        String returning = data.getString(data.getColumnIndex(COL3));
        return returning;
    }

    public String getLongi (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT LONGITUDE FROM " + TABLE_NAME + "WHERE ID=" + id+"", null);
        String returning = data.getString(data.getColumnIndex(COL4));
        return returning;
    }

    public ArrayList<String> getAllMarkers() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COL1)));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean updateData(String id, String title, String snippet, String latitude, String longitude, String mode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1,title);
        contentValues.put(COL2,snippet);
        contentValues.put(COL3, latitude);
        contentValues.put(COL4,longitude);
        contentValues.put(COL5,mode);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[] {id});
        return true;
    }

    public Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        DataNumber -= 1;
        return db.delete(TABLE_NAME, "ID = ?", new String[] {id});
    }

}

