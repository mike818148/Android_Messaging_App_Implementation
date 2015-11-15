package com.example.mikechung.android_messaging_app_implementation.message;

import com.example.mikechung.android_messaging_app_implementation.BaseDB;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
/**
 * Created by MikeChung on 15/4/9.
 */

/**
 Message
 +----+------+---------+------+
 | id | Name | Content | Time |
 +----+------+---------+------+
*/
public class MessageDB extends BaseDB{
    /* Members */
    protected String tableName;
    protected String tableId = "TableId";
    protected final String name = "Name";
    protected final String content = "Content";
    protected final String time = "Time";
    protected int newestGet; // newest message get from database

    public static OnMessageDBChangeListener mListener;

    /* Functions */
    // constructor
    public MessageDB(Context context,String _tableName){
        super(context);
        mListener = null;
        // create message table
        Vector<String> attrName = new Vector<String>(3);
        Vector<String> attrType = new Vector<String>(3);
        attrName.add(name); 	attrType.add("TEXT");
        attrName.add(content); 	attrType.add("TEXT");
        attrName.add(time); 	attrType.add("DATETIME");
        tableName = _tableName;
        tableId = tableId;
        if(!checkTableExist(tableName)) {
            createTable(tableName, tableId, attrName, attrType);
        }
        // get oldest message id
        Cursor cursor = getAll(tableName);
        if(cursor!=null)
            newestGet = cursor.getCount(); // id from 1
    }
    // get message
    public Messages getMessage(){
        // get all message already
        if(newestGet<=0)
            return null;
        // get newest message
        Cursor cursor = get(tableName, tableId, newestGet);
        if(cursor==null)
            return null;
        cursor.moveToFirst();
        newestGet--;
        return new Messages(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),
                cursor.getString(2),
                Long.parseLong(cursor.getString(3))
        );

    }

    // get newest n message
    public List<Messages> getRecentMessage(int number){
        List<Messages> list = new ArrayList<Messages>();
        if(newestGet<=0)
            return list;
        int from = (newestGet>number)? newestGet-number+1 : 1;
        int to = newestGet;
        Cursor cursor = getRange(tableName, tableId, from, to);
        if(cursor==null)
            return list;
        while(cursor.moveToNext()){
            list.add(
                    new Messages(
                            Integer.parseInt(cursor.getString(0)),
                            cursor.getString(1),
                            cursor.getString(2),
                            Long.parseLong(cursor.getString(3))
                    )
            );
        }
        newestGet -= number;
        return list;
    }
    // add message
    public int addMessage(Messages message){
        ContentValues entry = new ContentValues();
        entry.put(name, message.getWhoSaid());
        entry.put(content, message.getContent());
        entry.put(time, Long.toString(message.getTime()));
        return add(tableName, entry);
    }

    public static void setmListener(OnMessageDBChangeListener _mListener) {
        mListener = _mListener;
    }

    public static interface OnMessageDBChangeListener{
        public void onMessageAdd(Messages message);
    }

}
