package com.example.mikechung.android_messaging_app_implementation.message;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.mikechung.android_messaging_app_implementation.R;
import com.example.mikechung.android_messaging_app_implementation.contact.Conversation;
import com.example.mikechung.android_messaging_app_implementation.message.MessageConsumer.OnReceiveMessageHandler;
import com.rabbitmq.client.Channel;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends ActionBarActivity{


    private Publisher msgPublisher;
    public Channel mModel;
    private LinearLayout layout;
    private String QUEUE_NAME;
    private String myQueueName;
    private String myFriendName;
    private String EXCHANGE_NAME;
    private String EXCHANGE_TYPE;
    private String myName;
    private String getMyQueueName;
    private String language = Locale.getDefault().getLanguage();
    private PopupWindow mIconPopupWindow;
    private PopupWindow mAttachmentPopWindow;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mPopupWindowWidth;
    private int mPopupWindowHeight;
    private String mode;

    Location location;
    double latitude;
    double longititude;
    protected LocationManager locationManager;
    Location newLocation = null;//add thiss

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute


    // Secret tag to identify it's image
    private String imgTag = "$*@img@*$";
    private String joinTag = "$*@join@*$";
    private String replyTag = "$*@reply@*$";
    private String gpsTap = "$*@gps@*$";

    private MessageDB messageDB;


    SimpleDateFormat sdf = new SimpleDateFormat("EEE h:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Get Target Data
        Bundle msgData = getIntent().getExtras();
        QUEUE_NAME = msgData.getString("queueName"); //Frind's Queue Name
        EXCHANGE_TYPE = msgData.getString("exchangeType");
        EXCHANGE_NAME = msgData.getString("exchangeName");
        myName = msgData.getString("myName");
        myFriendName = msgData.getString("friendName");
        myQueueName = msgData.getString("myQueueName");
        mode = msgData.getString("mode");
        mModel = IConnectToRabbitMQ.mModel;

        msgPublisher = new Publisher(QUEUE_NAME,EXCHANGE_NAME,mModel);
        System.out.println("QUEUENAME:"+QUEUE_NAME);
        messageDB = new MessageDB(this,QUEUE_NAME);



        // Get Share Locatoin Button
        //final Button shareGPSLoc = (Button) this.findViewById(R.id.shareGPS);

        // Get EditTxt Input message Field
        final EditText inputMsg = (EditText) this.findViewById(R.id.textField);

        // Get add image icon object
        final ImageButton addObjectBtn = (ImageButton) this.findViewById(R.id.addObject);

        // Environment Setup
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        mScreenWidth = displaymetrics.widthPixels;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(myFriendName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set Background image, to be continued...

        //LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //LocationListener mlocListener = new MyLocationListener();

        //mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,mlocListener);
        //newLocation = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //
        //  Set Text Field OnClickListener
        //
        inputMsg.setOnKeyListener(new OnKeyListener(){
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                // If the event is a key-down event on the "enter" button
                if ((arg2.getAction() == KeyEvent.ACTION_DOWN) && (arg1 == KeyEvent.KEYCODE_ENTER) && (!inputMsg.getText().toString().equals(""))) {
                    // Create new message object
                    Date now = new Date();
                    Messages newMessage = new Messages(
                            0,
                            myName,
                            inputMsg.getText().toString(),
                            now.getTime()/1000
                    );
                    // send to server for dispatching
                    new send().execute(newMessage.toString());
                    // add to database

                    messageDB.addMessage(newMessage);
                    // put on screen
                    addTextView(newMessage, Gravity.RIGHT, Color.YELLOW);
                    System.out.println("Message Send Time: "+ sdf.format(now));
                    inputMsg.setText("");
                    return true;
                }
                return false;
            }
        });

        //
        // Set addObject OnClickListener
        //
        addObjectBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                getIconPopupWindowInstance();
                                                mIconPopupWindow.showAsDropDown(v);
                                            }
                                        }
        );

        //The Scroll View of the output
        layout = (LinearLayout) this.findViewById(R.id.linearLayout1);



        // take recent message from
        showOldMessage(messageDB);

    }

    /**
     * addTextView is used to add the TextView to the the LinearLayout in the Main ScrollView.
     * @param message indicates the content of messages
     * @param gravity indicates that the publish message shows on the right and receive message on the left.
     */

    public void addTextView(Messages message, int gravity, int background){
        //
        // Check if it is image icon
        //
        if(isImageIcon(message.getContent())){
            addImageView(message, gravity, background);
        }else{
            final TextView mOutput = new TextView(this);
            //
            //	Here we just use the html form to build the message fields.
            //
            String temp = Long.toString(message.getTime());
            int timeStamp = Integer.parseInt(temp);
            Date time = new Date(timeStamp * 1000L);
            Locale timeZone = new Locale(language);
            DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, timeZone);
            String outputMessage;
            if(Gravity.RIGHT == gravity){
                outputMessage = "<font size=\"5\" color=#000000 bgcolor=\"#FFFFEB\">"+message.getContent()+"</font><br><font size=\"3\" color=#B6B6B4>"+df.format(time)+"</font>";
            }else{
                outputMessage = "<font size=\"3\" color=#000080 face=\"Times New Roman\">"+message.getWhoSaid()+"</font><br><font size=\"5\" color=#000000 bgcolor=\"#AFFFB0\">"+message.getContent()+"</font><br><font size=\"3\" color=#B6B6B4>"+df.format(time)+"</font>";
            }
            mOutput.setText(Html.fromHtml(outputMessage));
            // Set the textview height and width to wrap_content
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            // Set the textview will align to right or left according to the input gravity
            params.gravity = gravity;
            mOutput.setLayoutParams(params);
            // Text inside the textview is algin to right
            mOutput.setGravity(Gravity.RIGHT);
            mOutput.setPadding(1, 1, 3, 3);
            //mOutput.setBackgroundColor(background);
            layout.addView(mOutput);
            final ScrollView contentView = (ScrollView) this.findViewById(R.id.message_content);
            contentView.post(new Runnable() {
                @Override
                public void run() {
                    contentView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    /*
     * Check if this is a image icon
     */
    private boolean isImageIcon(String checkImageTag){
        if(checkImageTag.length() < 9) return false;
        String isImgTag = checkImageTag.substring(0, 9);
        System.out.println("\n isImgTag:"+isImgTag);
        if(imgTag.equals(isImgTag)) {
            return true;
        }
        return false;
    }

    /*
	 *  Add Image views in the screen
	 */
    private void addImageView(Messages message, int gravity, int background){
        String temp = Long.toString(message.getTime());
        int timeStamp = Integer.parseInt(temp);
        Date time = new Date(timeStamp * 1000L);
        Locale timeZone = new Locale(language);
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, timeZone);
        TextView test = new TextView(this);

        String selectedImage = message.getContent().substring(9, message.getContent().length());
        System.out.println("[!] Content:"+selectedImage);
        String outputMessage;
        if(Gravity.RIGHT == gravity){
            outputMessage = "<img src='"+selectedImage+"'><br><font size=\"3\" color=#B6B6B4>"+df.format(time)+"</font>";
        }else{
            outputMessage = "<font size=\"3\" color=#000080 face=\"Times New Roman\">"+message.getWhoSaid()+"</font><br><img src='"+selectedImage+"'><br><font size=\"3\" color=#B6B6B4>"+df.format(time)+"</font>";
        }
        test.setText(Html.fromHtml(outputMessage, new ImageGetter(), null));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // Set the textview will align to right or left according to the input gravity
        params.gravity = gravity;
        test.setLayoutParams(params);
        // Text inside the textview is algin to right
        test.setGravity(Gravity.RIGHT);
        test.setPadding(1, 1, 3, 3);
        //test.setBackgroundColor(background);
        layout.addView(test);
        final ScrollView contentView = (ScrollView) this.findViewById(R.id.message_content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                contentView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private class ImageGetter implements Html.ImageGetter{

        public Drawable getDrawable(String source){
            int id;
            System.out.println("Source:"+source);
            if (source.equals("message_icon1.png")) {
                id = R.drawable.message_icon1;
            }else if(source.equals("message_icon2.png")){
                id = R.drawable.message_icon2;
            }else if(source.equals("message_icon3.png")){
                id = R.drawable.message_icon3;
            }else if(source.equals("message_icon4.png")){
                id = R.drawable.message_icon4;
            }else if(source.equals("message_icon5.png")){
                id = R.drawable.message_icon5;
            }else if(source.equals("message_icon6.png")){
                id = R.drawable.message_icon6;
            }else if(source.equals("message_icon7.png")){
                id = R.drawable.message_icon7;
            }else {
                return null;
            }

            // Since the image may be too big it may need tot compressed in case the memeory overflow.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            int newImageHeight = mScreenHeight/4;
            int newImageWidth = mScreenHeight/4;
            Bitmap bMap = BitmapFactory.decodeResource(getResources(),id);
            Bitmap resized = getResizedBitmap(bMap,newImageWidth,newImageHeight);
            Drawable d = new BitmapDrawable(getResources(),resized);
            d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
            return d;
        }

        private Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
            return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
        };
    }

    /*
	 *  Send messages through RabbitMQ Publisher
	 */
    private class send extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {
                String sendMessage="";
                for(int i = 0; i < Message.length;i++)
                    sendMessage+=Message[i];
                sendMessage = sendMessage.replaceAll(" ","_SAPCE_");
                msgPublisher.updateSendMsg(sendMessage);
                //boolean isConnected = msgPublisher.
                if(mModel != null || mModel.isOpen()){
                    if("single".equalsIgnoreCase(mode)) {
                        msgPublisher.singlePublish();
                        System.out.println("[o] MessageActivtiy - single send Success");
                    }else if("group".equalsIgnoreCase(mode)) {
                        msgPublisher.groupPublish();
                        System.out.println("[o] MessageActivtiy - group send Success");
                    }
                }else{
                    System.out.println("[x] MessageActivtiy - send Error: Unable to connect to Server");
                }
            } catch (Exception e) {
                System.out.println("[x] MessageActivtiy - send Error:"+e);
            }
            return null;
        }
    }


    /*
     * Get PopupWindow instance
     */
    private void getAttachmentPopupWindowInstance() {
        if (null != mAttachmentPopWindow) {
            mAttachmentPopWindow.dismiss();
            return;
        } else {
            initAttachmentsPopuptWindow();
        }
    }


    /*
     * Get PopupWindow instance
     */
    private void getIconPopupWindowInstance() {
        if (null != mIconPopupWindow) {
            mIconPopupWindow.dismiss();
            return;
        } else {
            initIconPopuptWindow();
        }
    }





    /*
     * Create PopupWindow
     */
    @SuppressLint("InflateParams") private void initAttachmentsPopuptWindow() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View popupWindow = layoutInflater.inflate(R.layout.message_attachments, null);
        ImageButton shareLocation = (ImageButton) popupWindow.findViewById(R.id.imageButton1);
        shareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch GPS Activity
            }
        });

        // 创建一个PopupWindow
        // 参数1：contentView 指定PopupWindow的内容
        // 参数2：width 指定PopupWindow的width
        // 参数3：height 指定PopupWindow的height
        mAttachmentPopWindow = new PopupWindow(popupWindow, mScreenWidth/2, mScreenHeight/2);

        // Get PopupWindow的width和height
        mPopupWindowWidth = mAttachmentPopWindow.getWidth();
        System.out.println("popUpWindow Width:"+mPopupWindowWidth);
        mPopupWindowHeight = mAttachmentPopWindow.getHeight();
        System.out.println("popUpWindow Height:"+mPopupWindowHeight);
        mAttachmentPopWindow.setOutsideTouchable(true);
        mAttachmentPopWindow.setTouchable(true);
    }

    /*
     * Create PopupWindow
     */
    @SuppressLint("InflateParams") private void initIconPopuptWindow() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View popupWindow = layoutInflater.inflate(R.layout.message_images, null);
        ArrayList<ImageButton> imgBtns = new ArrayList<ImageButton>();
        ImageButton img1 = (ImageButton) popupWindow.findViewById(R.id.imageButton1);
        ImageButton img2 = (ImageButton) popupWindow.findViewById(R.id.imageButton2);
        ImageButton img3 = (ImageButton) popupWindow.findViewById(R.id.imageButton3);
        ImageButton img4 = (ImageButton) popupWindow.findViewById(R.id.imageButton4);
        ImageButton img5 = (ImageButton) popupWindow.findViewById(R.id.imageButton5);
        ImageButton img6 = (ImageButton) popupWindow.findViewById(R.id.imageButton6);
        ImageButton img7 = (ImageButton) popupWindow.findViewById(R.id.imageButton7);
        imgBtns.add(img1);
        imgBtns.add(img2);
        imgBtns.add(img3);
        imgBtns.add(img4);
        imgBtns.add(img5);
        imgBtns.add(img6);
        imgBtns.add(img7);
        for(int i=0; i<imgBtns.size(); i++){
            final int p = i;
            final ImageButton btn = imgBtns.get(i);
            btn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           String temp = "message_icon"+Integer.toString(p+1)+".png";
                                           System.out.println("Temp:"+temp);
                                           //String sendImage = imgTag+btn.getResources().getIdentifier(temp, "drawable", getActivity().getPackageName());
                                           String sendImage = imgTag+temp;
                                           Date now = new Date();
                                           Messages newMessage = new Messages(
                                                   myName,
                                                   sendImage,
                                                   now.getTime()/1000
                                           );
                                           // send to server for dispatching
                                           new send().execute(newMessage.toString());
                                           // add to database
                                           messageDB.addMessage(newMessage);
                                           // add imageView on screen
                                           addTextView(newMessage,Gravity.RIGHT,android.graphics.Color.YELLOW);
                                           //addImageView(newMessage,Gravity.RIGHT,android.graphics.Color.YELLOW);
                                           mIconPopupWindow.dismiss();
                                       }
                                   }
            );
        }

        // 创建一个PopupWindow
        // 参数1：contentView 指定PopupWindow的内容
        // 参数2：width 指定PopupWindow的width
        // 参数3：height 指定PopupWindow的height
        mIconPopupWindow = new PopupWindow(popupWindow, mScreenWidth, mScreenHeight/2);

        // Get PopupWindow的width和height
        mPopupWindowWidth = mIconPopupWindow.getWidth();
        System.out.println("popUpWindow Width:"+mPopupWindowWidth);
        mPopupWindowHeight = mIconPopupWindow.getHeight();
        System.out.println("popUpWindow Height:"+mPopupWindowHeight);
        mIconPopupWindow.setOutsideTouchable(true);
        mIconPopupWindow.setTouchable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onRestart() {
        super.onResume();
    }

    @Override
    public void onResume() {
        super.onPause();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected int showOldMessage(MessageDB db){
        // Take old messages
        List<Messages> messages = db.getRecentMessage(5);
        // add to screen
        for(int i=0;i<messages.size();i++){
            if(myName.equals(messages.get(i).getWhoSaid())){
                addTextView(messages.get(i),Gravity.RIGHT,android.graphics.Color.YELLOW);
            }
            else{
                addTextView(messages.get(i),Gravity.LEFT,android.graphics.Color.GREEN);
            }
        }
        return 0;
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location loc) {

            //save the new location
            newLocation = loc;

        }

        public Location getLocation()
        {
            return newLocation;
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
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
