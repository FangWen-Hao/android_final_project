package com.matti.finalproject;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "database.db";
    public static final String TABLE_NAME = "markers";
    public static final String COL1 = "TITLE";
    public static final String COL2 = "SNIPPET";
    public static final String COL3 = "LATITUDE";
    public static final String COL4 = "LONGITUDE";
    public static final String COL5 = "MODE";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = " CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " TITLE TEXT, SNIPPET TEXT, LATITUDE BLOB , LONGITUDE BLOB, MODE TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public boolean addData(String title, String snippet, Double latitude, Double longitude, String mode){
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
            return true;
        }
    }

    public Cursor showAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public int getData (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE ID=" + id+"", null);
        data.moveToFirst();
        int returning = data.getInt(0);
        return returning;
    }

    public boolean checkID (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor data = db.rawQuery("SELECT ID FROM " + TABLE_NAME + " WHERE ID=" + id + "", null);
        }
        catch (Exception e){ //Is this even the right exception?
            return false;
        }
        return true;
    }


    public String getTitle (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE ID=" + id+"", null);
        data.moveToFirst();
        String returning = data.getString(1);
        return returning;
    }

    public String getSnippet (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE ID=" + id+"", null);
        data.moveToFirst();
        String returning = data.getString(2);
        return returning;
    }

    public double getLat (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE ID=" + id+"", null);
        data.moveToFirst();
        double returning = data.getDouble(3);
        return returning;
    }

    public double getLong(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE ID=" + id+"", null);
        data.moveToFirst();
        double returning = data.getDouble(4);
        return returning;
    }

//    public ArrayList<String> getAllMarkers() {
//        ArrayList<String> array_list = new ArrayList<String>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res =  db.rawQuery( "SELECT * FROM " + TABLE_NAME, null );
//        res.moveToFirst();
//
//        while(res.isAfterLast() == false){
//            array_list.add(res.getString(res.getColumnIndex(COL1)));
//            res.moveToNext();
//        }
//        return array_list;
//    }

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
        return db.delete(TABLE_NAME, "ID = ?", new String[] {id});
    }

}

