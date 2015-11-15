package com.example.mikechung.android_messaging_app_implementation.contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.mikechung.android_messaging_app_implementation.BaseDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by MikeChung on 15/6/25.
 */
public class ConversationDB extends BaseDB {

    /* Members */
    protected final String tableName = "Conversation";
    protected final String tableId = "conversation_id";
    protected final String name = "Name";
    protected final String displayName = "DisplayName";
    protected final String phone = "Phone";
    protected int newestGet; // newest message get from database

    public ConversationDB(Context context){
        super(context);
        // create message table
        Vector<String> attrName = new Vector<String>(3);
        Vector<String> attrType = new Vector<String>(3);
        attrName.add(name); 	attrType.add("TEXT");
        attrName.add(displayName); attrType.add("TEXT");
        attrName.add(phone); 	attrType.add("TEXT");
        createTable(tableName, tableId, attrName, attrType);
        // get newest contact id
        Cursor cursor = getAll(tableName);
        if(cursor!=null)
            newestGet = cursor.getCount(); // id from 1
    }

    // get message
    public Conversation getConversation(){
        // get all contact already
        if(newestGet<=0)
            return null;
        // get newest message
        Cursor cursor = get(tableName, tableId, newestGet);
        if(cursor==null)
            return null;
        cursor.moveToFirst();
        newestGet--;
        return new Conversation(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),
                cursor.getString(2)
        );
    }

    // check if table account exist
    public boolean chceckConversationExist(String account){
        return checkTableExist(account);
    }

    // add conversation
    public int addConversation(Conversation conversation){
        ContentValues entry = new ContentValues();
        System.out.println("AddConversation:"+conversation.get_displayName()+"~~~"+conversation.get_name());
        entry.put(displayName, conversation.get_displayName());
        entry.put(name, conversation.get_name());
        //entry.put(phone, conversation.get_phone());
        return add(tableName, entry);
    }

    // delete conversation
    public int removeContact(Conversation conversation){
        int delete_itemId = conversation.get_id();
        int delete_result = delete(tableName,tableId,delete_itemId);
        Cursor cursor = getAll(tableName);
        if(cursor!=null)
            newestGet = cursor.getCount();
        return delete_result;
    }

    public List<Conversation> getRecentConversations(int number){
        List<Conversation> list = new ArrayList<Conversation>();
        if(newestGet<=0)
            return list;
        int from = (newestGet>number)? newestGet-number+1 : 1;
        int to = newestGet;
        Cursor cursor = getRange(tableName, tableId, from, to);
        if(cursor==null)
            return list;
        while(cursor.moveToNext()){
            list.add(
                    new Conversation(
                            Integer.parseInt(cursor.getString(0)),
                            cursor.getString(1),
                            cursor.getString(2)
                    )
            );
        }
        newestGet -= number;
        return list;
    }
}
