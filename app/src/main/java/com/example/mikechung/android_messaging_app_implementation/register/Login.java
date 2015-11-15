package com.example.mikechung.android_messaging_app_implementation.register;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mikechung.android_messaging_app_implementation.AccountData;
import com.example.mikechung.android_messaging_app_implementation.R;
import com.example.mikechung.android_messaging_app_implementation.mysql.mySQL;

public class Login extends Activity {

    private static final int Login_Success  = 0;
    private static final int Wrong_Password  = 1;
    private static final int None_Account  = 2;
    private static final int Login_Error  = -1;
    private int loginResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //System.out.println("-------------Login----------");

        setContentView(R.layout.activity_login);

        final EditText typeAccount = (EditText) this.findViewById(R.id.login_account);
        final EditText typePassword = (EditText) this.findViewById(R.id.login_password);
        final Button login = (Button) this.findViewById(R.id.login_btn);
        final Button signUp = (Button) this.findViewById(R.id.goto_signup);

        login.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     String account = typeAccount.getText().toString();
                     String password = typePassword.getText().toString();
                     new dbSelect().execute(account,password);
                 }
             }
        );

        signUp.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent =  new Intent(v.getContext(),SignUp.class);
                  v.getContext().startActivity(intent);
                  finish();
              }
          }
        );
    }


    private boolean isMySQLAccount(String account,String password){
        //-----------//
        //   MySQL   //
        //-----------//
        new dbSelect().execute(account,password);
        return false;
    }

    private class dbSelect extends AsyncTask<String, Void, Void> {

        private int loginResult;

        protected Void doInBackground(String... urls){
            mySQL mySQLDB = new mySQL();
            loginResult = mySQLDB.loginCheck(urls[0], urls[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            handleLoginResult(loginResult);
        }
    }

    private int handleLoginResult (int arg_loginResult) {
        //handle value
        loginResult = arg_loginResult;

        final EditText typeAccount = (EditText) this.findViewById(R.id.login_account);
        final EditText typePassword = (EditText) this.findViewById(R.id.login_password);

        String account = typeAccount.getText().toString();
        String password = typePassword.getText().toString();

        switch (loginResult){
            case Login_Success:
                // Save SharedPreferences
                AccountData.setAccount(account,password);
                // Leave Intent Activity
                finish();
            case Wrong_Password:
                typePassword.setError("Wrong Password");
                break;
            case None_Account:
                typeAccount.setError("No Such Account");
                break;
            case Login_Error:
                Toast.makeText(getApplicationContext(),"Login Error",Toast.LENGTH_LONG);
                break;
            default:
                break;
        }
        return 0;
    }


}
