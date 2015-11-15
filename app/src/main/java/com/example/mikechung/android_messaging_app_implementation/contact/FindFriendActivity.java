package com.example.mikechung.android_messaging_app_implementation.contact;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mikechung.android_messaging_app_implementation.R;
import com.example.mikechung.android_messaging_app_implementation.mysql.mySQL;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class FindFriendActivity extends Activity {

    private HashMap findResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        final EditText searchQuery = (EditText) this.findViewById(R.id.findFriend_query);
        final TextView searchResult = (TextView) this.findViewById(R.id.findFriend_result);
        final Button confirmBtn = (Button) this.findViewById(R.id.findFriend_confirm);


        // Setup confirm Button
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnData = new Intent();
                System.out.println("findResult"+findResult);
                returnData.putExtra("name",(String) findResult.get("name"));
                returnData.putExtra("displayName",(String) findResult.get("displayName"));
                returnData.putExtra("phone",(String) findResult.get("phone"));
                setResult(Activity.RESULT_OK,returnData);
                // Fiish FindFriend Activity and return the friend value
                finish();
            }
        });

        // Setup search find Button
        Button searchBtn = (Button) this.findViewById(R.id.findButton);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchAccount = searchQuery.getText().toString();

                /* Send Http Request for the Query */
                try {
                    MySQLQuery mySQLQuery = new MySQLQuery();
                    try {
                        findResult = mySQLQuery.execute(searchAccount).get();
                        if(findResult != null){
                            String user_info = findResult.get("name")+"\n"+findResult.get("displayName")+"\n"+findResult.get("phone");
                            searchResult.setText(user_info);
                            confirmBtn.setVisibility(View.VISIBLE);
                        }else{
                            searchResult.setText("No Result");
                        }
                        /* To be continued*/
                    }catch(ExecutionException ee){
                        Log.e("Error","findFriendActivity-Query Execution Error");
                    }
                }catch(InterruptedException ie){
                    Log.e("Error","findFriendActivity-Async Task Error");
                }
            }
        });



    }

    private class MySQLQuery extends AsyncTask<String, Void, HashMap>{

        protected HashMap doInBackground(String... params){
            mySQL mySQLDB = new mySQL();
            HashMap queryResult = mySQLDB.findUser(params[0]);
            return  queryResult;
        }
    }
}
