package com.example.mikechung.android_messaging_app_implementation.contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.mikechung.android_messaging_app_implementation.BaseDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by MikeChung on 15/6/23.
 */
public class ContactDB extends BaseDB {

    /* Members */
    protected final String tableName = "Contact";
    protected final String tableId = "contact_id";
    protected final String name = "Name";
    protected final String displayName = "DisplayName";
    protected final String phone = "Phone";
    protected int newestGet; // newest message get from database

    public ContactDB(Context context){
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
    public Contacts getContact(){
        // get all contact already
        if(newestGet<=0)
            return null;
        // get newest message
        Cursor cursor = get(tableName, tableId, newestGet);
        if(cursor==null)
            return null;
        cursor.moveToFirst();
        newestGet--;
        return new Contacts(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3)
        );
    }

    // add message
    public int addContact(Contacts contact){
        ContentValues entry = new ContentValues();
        entry.put(name, contact.get_name());
        entry.put(displayName, contact.get_displayName());
        entry.put(phone, contact.get_phone());
        return add(tableName, entry);
    }

    // delete contact
    public int removeContact(Contacts contact){
        int delete_itemId = contact.get_id();
        int delete_result = delete(tableName,tableId,delete_itemId);
        Cursor cursor = getAll(tableName);
        if(cursor!=null)
            newestGet = cursor.getCount();
        return delete_result;
    }

    public List<Contacts> getRecentContacts(int number){
        List<Contacts> list = new ArrayList<Contacts>();
        if(newestGet<=0)
            return list;
        int from = (newestGet>number)? newestGet-number+1 : 1;
        int to = newestGet;
        Cursor cursor = getRange(tableName, tableId, from, to);
        if(cursor==null)
            return list;
        while(cursor.moveToNext()){
            list.add(
                    new Contacts(
                            Integer.parseInt(cursor.getString(0)),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3)
                    )
            );
        }
        newestGet -= number;
        return list;
    }

}
