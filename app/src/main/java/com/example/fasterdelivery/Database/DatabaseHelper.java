package com.example.fasterdelivery.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Addresslist.db";
    public static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ADDRESS_TABLE = "CREATE TABLE " +
                ItemContract.AddressEntry.TABLE_NAME + " (" +
                ItemContract.AddressEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ItemContract.AddressEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ItemContract.AddressEntry.COLUMN_NUMBER + " TEXT NOT NULL, " +
                ItemContract.AddressEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        db.execSQL(SQL_CREATE_ADDRESS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ItemContract.AddressEntry.TABLE_NAME);
        onCreate(db);
    }

    public Cursor selectName(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from AddressList",null);
        return  cursor;
    }

    public Cursor CountName(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(name) FROM AddressList",null);
        return  cursor;
    }
}
