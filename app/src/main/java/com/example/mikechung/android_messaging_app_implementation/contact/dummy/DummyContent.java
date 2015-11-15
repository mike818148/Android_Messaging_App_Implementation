package com.example.mikechung.android_messaging_app_implementation.contact.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

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
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    public static DummyItem getItem(String id){
        return ITEM_MAP.get(id);
    }

    public static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;
        public String hidden_info;

        public DummyItem(String id, String content, String hidden_info) {
            this.id = id;
            this.content = content;
            this.hidden_info = hidden_info;
        }

        @Override
        public String toString() {
            return content;
        }

        public String getHidden_info() {
            return hidden_info;
        }
    }
}
