package com.example.mikechung.android_messaging_app_implementation.message;

/**
 * Created by MikeChung on 15/4/9.
 */

import java.util.HashMap;
import java.util.Map;

public class Messages {
    //private variables
    protected int _id;
    protected String _whoSaid;
    protected String _content;
    protected long _time;

    //Empty constructor
    public Messages() {

    }

    // constructor
    public Messages(int id, String whoSaid, String content, long time) {
        this._id = id;
        this._whoSaid = whoSaid;
        this._content = content;
        this._time = time;
    }

    // constructor
    public Messages(String whoSaid, String content, long time) {
        this._whoSaid = whoSaid;
        this._content = content;
        this._time = time;
    }

    // getting ID
    public int getID() {
        return this._id;
    }

    // setting id
    public void setID(int id) {
        this._id = id;
    }

    // getting name
    public String getWhoSaid() {
        return this._whoSaid;
    }

    // setting name
    public void setWhoSaid(String name) {
        this._whoSaid = name;
    }

    // getting phone number
    public String getContent() {
        return this._content;
    }

    // setting phone number
    public void setContent(String msg) {
        this._content = msg;
    }

    // getting msg time
    public long getTime() {
        return this._time;
    }

    // setting msg time
    public void setTime(long time) {
        this._time = time;
    }

    /* convert function with RabbitMQ */
    // create string for sending
    @Override
    public String toString() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", _whoSaid);
        data.put("message", _content);
        data.put("time", Integer.toString((int) _time));
        return data.toString();
    }

    public Messages(String mqString) {
        //
        // Here since we HashMap change to String we need to change back to HashMap
        //
        mqString = mqString.substring(1);
        mqString = mqString.substring(0, mqString.length() - 1);
        Map<String, String> stringToMap = new HashMap<String, String>();
        String[] pairs = mqString.split(", ");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            stringToMap.put(keyValue[0], keyValue[1]);
        }
        _whoSaid = stringToMap.get("name");
        _content = stringToMap.get("message");
        _time = Long.parseLong(stringToMap.get("time"));
    }
}
