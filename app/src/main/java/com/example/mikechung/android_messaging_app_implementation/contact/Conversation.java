package com.example.mikechung.android_messaging_app_implementation.contact;

import android.media.Image;

/**
 * Created by MikeChung on 15/6/25.
 */
public class Conversation {
    private int _id;
    private String _name;
    private String _displayName;
    //private String _phone;
    private Image _photo;

    public Conversation(int id, String displayName, String name){
        this._id = id;
        this._name = name;
        this._displayName = displayName;
        //this._phone = phone;
    }

    public Conversation(String displayName, String name) {
        this._name = name;
        this._displayName = displayName;
        //this._phone = phone;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_displayName() {
        return _displayName;
    }

    public void set_displayName(String _displayName) {
        this._displayName = _displayName;
    }

    public Image get_photo() {
        return _photo;
    }

    public void set_photo(Image _photo) {
        this._photo = _photo;
    }
}
