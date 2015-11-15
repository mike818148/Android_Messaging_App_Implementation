package com.example.mikechung.android_messaging_app_implementation.contact;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.mikechung.android_messaging_app_implementation.R;
import com.example.mikechung.android_messaging_app_implementation.contact.dummy.DummyContent;
import com.example.mikechung.android_messaging_app_implementation.contact.dummy.DummyConversation;
import com.example.mikechung.android_messaging_app_implementation.message.IConnectToRabbitMQ;
import com.example.mikechung.android_messaging_app_implementation.message.MessageActivity;
import com.example.mikechung.android_messaging_app_implementation.message.MessageConsumer;
import com.example.mikechung.android_messaging_app_implementation.message.MessageDB;
import com.example.mikechung.android_messaging_app_implementation.message.Messages;
import com.rabbitmq.client.Channel;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MikeChung on 15/4/21.
 */
public class ContactActivity extends FragmentActivity implements ListContactFragment.OnFragmentInteractionListener, ListConversationFragment.OnFragmentInteractionListener{

    private TabHost mTabHost;
    private TabManager mTabManager;

    public Channel mModel;
    private MessageConsumer mConsumer;

    private ContactDB contactDB;
    private ConversationDB conversationDB;
    private String userAcc = "admin";
    private String userPwd = "admin";
    private final int MAX_CONTACTS = 30;
    private final int MAX_CONVERSATION = 30;
    private String account;

    // Secret tag to identify it's image
    private String imgTag = "$*@img@*$";
    private String joinTag = "$*@join@*$";
    private String replyTag = "$*@reply@*$";
    private String gpsTap = "$*@gps@*$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        Bundle accountData = getIntent().getExtras();
        account = accountData.getString("account");
        mModel = IConnectToRabbitMQ.mModel;
        contactDB = new ContactDB(this);
        conversationDB = new ConversationDB(this);




        //
        //  Initial Contacts Info in Dummy Content
        //
        initialDummyContent();
        //
        //  Initial Contacts Info in Dummy Content
        //
        initialDummyConversation();
        System.out.println("ITEMS:"+DummyConversation.getCount());
        System.out.println("ITEMS:"+DummyConversation.ITEMS);
        System.out.println("ITEMS MAP:"+DummyConversation.ITEM_MAP);
        //
        // Here we add tab fragments
        //
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
        mTabHost.setCurrentTab(0);//設定一開始就跳到第一個分頁
        mTabManager.addTab(mTabHost.newTabSpec("聯絡人").setIndicator(null,this.getResources().getDrawable(R.drawable.contact_icon)),ListContactFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("對話").setIndicator(null,this.getResources().getDrawable(R.drawable.conversation_icon)),ListConversationFragment.class, null);

        //
        // Set each tab size
        //
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm); // get resolution of the screen
        int screenWidth = dm.widthPixels; // the width of the screen
        TabWidget tabWidget = mTabHost.getTabWidget();
        int count = tabWidget.getChildCount();   // fetch the number of tabs
        for (int i = 0; i < count; i++) {
            if(i == 0){
                tabWidget.getChildTabViewAt(i).setBackgroundColor(Color.parseColor("#ffe550"));
            }else{
                tabWidget.getChildTabViewAt(i).setBackgroundColor(Color.GRAY);
            }
            tabWidget.getChildTabViewAt(i).getLayoutParams().width = screenWidth/2;
        }

        //
        //  Set up message Consumer
        //
        System.out.println("account:"+account);
        // Create the consumer
        mConsumer = new MessageConsumer("","",account,mModel);
        new consumerCreateQueue().execute();

        //register for messages
        mConsumer.setOnReceiveMessageHandler(new MessageConsumer.OnReceiveMessageHandler(){

            public void onReceiveMessage(byte[] message) {
                String text = "";
                String whoSaid;
                String content;
                long timestamp;
                try {
                    text = new String(message, "UTF8");
                    System.out.println("SetOnReceiveMessageHandler Receive----->"+text);
                    Map receiveMessage = StringToHashMap(text);
                    whoSaid = (String) receiveMessage.get("name");
                    content = (String) receiveMessage.get("message");
                    System.out.println("content:"+content);
                    String timestampStr = (String) receiveMessage.get("time");
                    System.out.println("timeStampStr:"+timestampStr);
                    timestamp = Long.parseLong(timestampStr);

                    //System.out.println("******:"+getCallingActivity());
                    //System.out.println("&&&&&&:"+getLocalClassName());
                    //System.out.println("!!!!!!!:"+getClass());
                    if(!whoSaid.equals(account)){
                        Messages rcvMessage = new Messages(whoSaid,content,timestamp);
                        MessageDB db = new MessageDB(getApplicationContext(),whoSaid);
                        db.addMessage(rcvMessage);
                    }
                    if(!conversationDB.chceckConversationExist(whoSaid)){
                        Conversation new_conversation = new Conversation(whoSaid,whoSaid);
                        conversationDB.addConversation(new_conversation);
                        updateDummyConversation(new_conversation);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            private boolean isReplyTag(String content) {
                if(replyTag.equals(content)) return true;
                return false;
            }

            private boolean isJoinTag(String content) {
                if(joinTag.equals(content)) return true;
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("Data:"+data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                String findResult_name = data.getStringExtra("name");
                String findResult_displayName = data.getStringExtra("displayName");
                String findResult_phone = data.getStringExtra("phone");
                Contacts new_Contact = new Contacts(findResult_name,findResult_displayName,findResult_phone);
                contactDB.addContact(new_Contact);
                updateDummyContent(new_Contact);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_addFriend){
            Intent findFriendIntent = new Intent(this, FindFriendActivity.class);
            startActivityForResult(findFriendIntent, 1);
        }

        return true;
    }

    @Override
    public void setActionBar(Toolbar toolbar) {
        super.setActionBar(toolbar);

    }

    @Override
    public void onContactFragmentInteraction(String id) {
        //
        //  If id already existed then straight launch the Message Activity
        //
        if(DummyConversation.ITEM_MAP.containsKey(id)){
            DummyContent.DummyItem item = DummyContent.getItem(id);
            launchMessageActivity(account, item.getHidden_info(),"","single",item.toString());
        }else{
            DummyContent.DummyItem item = DummyContent.getItem(id);
            String userDisplayName = item.toString();
            String username = item.getHidden_info();
            DummyConversation.DummyConversationItem conversationItem = new DummyConversation.DummyConversationItem(id,userDisplayName,username);
            DummyConversation.addConversation(conversationItem);
            Conversation new_conversation = new Conversation(Integer.parseInt(id),userDisplayName,username);
            conversationDB.addConversation(new_conversation);
            launchMessageActivity(account, item.getHidden_info(),"","single",item.toString());
        }
    }

    @Override
    public void onConversationFragmentInteraction(String id) {

        DummyConversation.DummyConversationItem item = DummyConversation.getItem(id);

        System.out.println("ITEM:"+item.getUsername()+","+item.getUserDispalyName());
        launchMessageActivity(account, item.getUsername(),"","single",item.getUserDispalyName());
    }

    private void initialDummyContent(){
        DummyContent.clear();
        List contacts = contactDB.getRecentContacts(MAX_CONTACTS);
        for(int i=0; i<contacts.size() ;i++){
            Contacts contact = (Contacts) contacts.get(i);
            DummyContent.DummyItem new_dummyItem = new DummyContent.DummyItem(String.valueOf(i),contact.get_displayName(),contact.get_name());
            DummyContent.addItem(new_dummyItem);
        }
    }

    private void updateDummyContent(Contacts new_contact){
        DummyContent.DummyItem new_dummyItem = new DummyContent.DummyItem(String.valueOf(DummyContent.getCount()),new_contact.get_displayName(),new_contact.get_name());
        DummyContent.addItem(new_dummyItem);
    }

    private void initialDummyConversation(){
        DummyConversation.clear();
        List conversations = conversationDB.getRecentConversations(MAX_CONVERSATION);
        for(int i=0; i<conversations.size() ;i++){
            Conversation conversation = (Conversation) conversations.get(i);
            System.out.println("InitialDumyConversatoin:"+conversation.get_displayName()+"----"+conversation.get_name());
            //DummyConversation.DummyConversationItem new_dummyConversationItem = new DummyConversation.DummyConversationItem(String.valueOf(i),conversation.get_displayName(),conversation.get_name());
            DummyConversation.DummyConversationItem new_dummyConversationItem = new DummyConversation.DummyConversationItem(String.valueOf(i),conversation.get_name(),conversation.get_displayName());
            DummyConversation.addConversation(new_dummyConversationItem);
        }
    }

    private void updateDummyConversation(Conversation new_conversation){
        DummyConversation.DummyConversationItem new_dummyItem = new DummyConversation.DummyConversationItem(String.valueOf(DummyConversation.getCount()),new_conversation.get_displayName(),new_conversation.get_name());
        DummyConversation.addConversation(new_dummyItem);
    }

    private void launchMessageActivity(String UserDisplayName, String firendQueueName, String groupName,String mode, String friendDisplayName){

        Intent messageIntent = new Intent(this, MessageActivity.class);

        // Setup parameters passed to messaging activity
        Bundle msgData = new Bundle();
        msgData.putString("account",userAcc);
        msgData.putString("password",userPwd);
        msgData.putString("queueName",firendQueueName); // Friend's Queue Name
        msgData.putString("myName",UserDisplayName); // My Display Name
        msgData.putString("exchangeName",groupName);
        msgData.putString("mode",mode);
        msgData.putString("friendName",friendDisplayName);
        messageIntent.putExtras(msgData);
        startActivity(messageIntent);
    }

    private class consumerCreateQueue extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... Message) {
            try {
                //connect to broker
                mConsumer.connectToRabbitMQ();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private Map StringToHashMap(String inputStr){
        Map myMap = new HashMap();
        inputStr = inputStr.replaceAll("_SPACE_"," ");
        inputStr = inputStr.replaceAll("\\{","");
        inputStr = inputStr.replaceAll("\\}","");
        inputStr = inputStr.replaceAll(" ","");
        String[] pairs = inputStr.split(",");
        for (int i=0;i<pairs.length;i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split("=");
            myMap.put(keyValue[0], keyValue[1]);
        }
        System.out.println("myMap:"+myMap);
        return myMap;
    }
}
