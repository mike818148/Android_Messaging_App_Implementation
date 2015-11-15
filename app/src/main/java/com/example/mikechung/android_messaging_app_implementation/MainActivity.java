package com.example.mikechung.android_messaging_app_implementation;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mikechung.android_messaging_app_implementation.contact.ContactActivity;
import com.example.mikechung.android_messaging_app_implementation.message.IConnectToRabbitMQ;
import com.example.mikechung.android_messaging_app_implementation.register.Login;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity {

    //private String myServerIP = "52.68.99.148"; // Reference to the server ip
    //private IConnectToRabbitMQ rabbitMQConnection;
    //private String rabbitMQAccount;
    //private String rabbitMQPassword;

    GifMovieView gifMovieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //To be continued
        // Need to check Account Info first


        Intent contactIntent = new Intent(this, ContactActivity.class);
        // Setup parameters passed to messaging activity
        Bundle userData = new Bundle();
        new connectThread().execute();

        while(IConnectToRabbitMQ.mModel == null) {
            try {
                Thread.sleep(2000);                 //1000 milliseconds is one second.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Main Activity mModel:"+IConnectToRabbitMQ.mModel);

        AccountData accountData = new AccountData(getApplicationContext());
        // If there is no account data preference launch login activity.
        String myName = accountData.getAccount();

        if(myName != null){
            System.out.println("Account:"+myName);
            userData.putString("account", myName);
            contactIntent.putExtras(userData);
            startActivity(contactIntent);
        }else {
            Intent loginIntent = new Intent(this, Login.class);
            startActivity(loginIntent);
        }
    }

    private class connectThread extends AsyncTask<String, Void, Void> {
        private String myServerIP = "52.68.99.148";
        private IConnectToRabbitMQ rabbitMQConnection;
        @Override
        protected Void doInBackground(String... Message) {
            try {
                //connect to broker
                rabbitMQConnection = new IConnectToRabbitMQ(myServerIP, "admin", "admin");
                rabbitMQConnection.connectToRabbitMQ();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent contactIntent = new Intent(this, ContactActivity.class);
        // Setup parameters passed to messaging activity
        Bundle userData = new Bundle();
        AccountData accountData = new AccountData(getApplicationContext());
        // If there is no account data preference launch login activity.
        String myName = accountData.getAccount();

        if(myName != null){
            System.out.println("Account:"+myName);
            userData.putString("account", myName);
            contactIntent.putExtras(userData);
            startActivity(contactIntent);
        }else {
            Intent loginIntent = new Intent(this, Login.class);
            startActivity(loginIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //IConnectToRabbitMQ.dispose();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //IConnectToRabbitMQ.dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IConnectToRabbitMQ.dispose();
    }
}
