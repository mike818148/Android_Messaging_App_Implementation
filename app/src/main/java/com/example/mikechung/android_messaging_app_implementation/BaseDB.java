package com.example.mikechung.android_messaging_app_implementation;

/**
 * Created by MikeChung on 15/4/9.
 */

import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDB extends SQLiteOpenHelper{
    protected final static String DBName = "DBPrototype.db";
    protected final static int DBVersion = 1;
    // constructor
    public BaseDB(Context context){
        super(context, DBName, null, DBVersion);
    }
    // create
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO
    }
    // upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
    }
    // get
    // Get cursor with value1, value2 ...
    // tableId: name of id in table
    protected Cursor get(String tableName, String tableId, int id){
        String sql = "SELECT * FROM " + tableName + " WHERE " + tableId + "=" + Integer.toString(id);
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, null);
    }
    // add
    // Put (key1, value1), (key2, value2) ... in entry
    protected int add(String tableName, ContentValues entry){
        SQLiteDatabase db = getWritableDatabase();
        return (int) db.insert(tableName, null, entry);
    }
    // update
    // Update entry by id
    protected int update(String tableName, String tableId, int id, ContentValues entry){
        String where = tableId + "=" + Integer.toString(id);
        SQLiteDatabase db = getWritableDatabase();
        return db.update(tableName, entry, where, null);
    }
    // delete
    // Delete entry by id
    protected int delete(String tableName, String tableId, int id){
        String where = tableId + "=" + Integer.toString(id);
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(tableName, where, null);
    }
    // get all
    protected Cursor getAll(String tableName){
        String sql = "SELECT * FROM " + tableName;
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, null);
    }
    // get range
    protected Cursor getRange(String tableName, String tableId, int from, int to){
        String sql = "SELECT * FROM " + tableName +
                " WHERE " + tableId + " BETWEEN " + Integer.toString(from) + " AND " + Integer.toString(to);
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, null);
    }
    // create table
    protected int createTable(String tableName, String tableId, Vector<String> attrName, Vector<String> attrType){
        SQLiteDatabase db = getWritableDatabase();
        if(attrName.size()!=attrType.size())
            return -1;
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "( " +
                tableId + " INTEGER PRIMARY KEY AUTOINCREMENT";
        for(int i=0;i<attrName.size();i++)
            sql += ", " + attrName.get(i) + " " + attrType.get(i);
        sql += ")";
        db.execSQL(sql);
        return 0;
    }
    // delete table
    // @ This function should not be used unnecessarily
    protected int deleteTable(String tableName){
        String sql = "DROP TABLE IF EXISTS " + tableName;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        return 0;
    }

    // check table exist
    protected boolean checkTableExist(String tableName){
        String sql = "SELECT * FROM sqlite_master WHERE type='table' AND name='"+tableName+"'";
        SQLiteDatabase db = getReadableDatabase();
        int result = db.rawQuery(sql, null).getCount();
        System.out.println("Check Table Exist:"+result);
        if(result != 0) return true;
        return false;
    }

}
