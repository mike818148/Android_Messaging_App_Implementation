package com.example.mikechung.android_messaging_app_implementation.contact.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MikeChung on 15/6/25.
 */
public class DummyConversation {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyConversationItem> ITEMS = new ArrayList<DummyConversationItem>();

    public static int getCount(){
        return ITEMS.size();
    }

    public static void clear(){
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyConversationItem> ITEM_MAP = new HashMap<String, DummyConversationItem>();


    public static void addConversation(DummyConversationItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static DummyConversationItem getItem(String id){
        System.out.println("ITEMS:"+ITEM_MAP);

        return ITEM_MAP.get(id);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyConversationItem {
        public String id;
        public String UserDispalyName;
        public String Username;

        public DummyConversationItem(String id, String _userDisplayName, String _username) {
            this.id = id;
            this.UserDispalyName = _userDisplayName;
            this.Username = _username;
        }

        @Override
        public String toString() {
            return UserDispalyName;
        }

        public String getUserDispalyName() {
            return UserDispalyName;
        }

        public String getUsername() {
            return Username;
        }
    }


}
