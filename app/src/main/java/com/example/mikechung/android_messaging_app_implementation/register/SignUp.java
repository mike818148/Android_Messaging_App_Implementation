package com.example.mikechung.android_messaging_app_implementation.register;

import android.app.Activity;
import android.content.Context;
import android.location.GpsStatus;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.mikechung.android_messaging_app_implementation.AccountData;
import com.example.mikechung.android_messaging_app_implementation.R;
import com.example.mikechung.android_messaging_app_implementation.mysql.mySQL;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends Activity {

    //
    //	mySQL variables
    //
    private String rabbitMQUrl = "http://52.68.99.148:15672";
    private int signUpResult = -1;
    private static final int Create_Success = 0;
    private static final int Create_Failed = 1;
    private static final int Account_Existed = 2;
    private static final int Phone_Exitsted = 3;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final EditText typeAccount = (EditText) this.findViewById(R.id.create_account);
        final EditText typePassword = (EditText) this.findViewById(R.id.create_password);
        final EditText typeConfirmPassword = (EditText) this.findViewById(R.id.confirm_password);
        final EditText typeName = (EditText) this.findViewById(R.id.create_name);
        final EditText typePhone = (EditText) this.findViewById(R.id.create_phone_number);
        final Button signUp = (Button) this.findViewById(R.id.signup_btn);

        //typeAccount.setText("mike818148");
        //typePassword.setText("12345678");
        //typeConfirmPassword.setText("12345678");
        //typeName.setText("有志");
        //typePhone.setText("0975057838");
        signUp.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  String account = typeAccount.getText().toString();
                  String password = typePassword.getText().toString();
                  String displayName = typeName.getText().toString();
                  String phoneNumber = typePhone.getText().toString();
                  String confirmPassword = typeConfirmPassword.getText().toString();
                  if(password.length() == 0){
                      typePassword.requestFocus();
                      typePassword.setError("Password can't be empty !");
                  }else if(confirmPassword.length() == 0){
                      typeConfirmPassword.requestFocus();
                      typeConfirmPassword.setError("Confirm Password can't be empty !");
                  }else if(!isConfirmPassword(password,confirmPassword)){
                      typeConfirmPassword.requestFocus();
                      typeConfirmPassword.setError("Confirm Password doesn't match !");
                  }else if(!isValidPassword(password)){
                      typePassword.requestFocus();
                      typePassword.setError(validPasswordLog(password));
                  }else{
                      //Map<String,Object> accArgs = new HashMap<String,Object>();
                      //accArgs.put("tags", "user");
                      //accArgs.put("password",password);
                      //accArgs.put("name",account);
                      //new putRequest().execute(accUrl,accArgs.toString());

                      // Add vHost with account
                      //String vhostUrl = "http://52.68.99.148:15672/api/permissions/%2F/"+account;
                      //Map<String,Object> vhostArgs = new HashMap<String,Object>();
                      //vhostArgs.put("scope", "client");
                      //vhostArgs.put("configure",".*");
                      //vhostArgs.put("write",".*");
                      //vhostArgs.put("read",".*");
                      //new putRequest().execute(vhostUrl,vhostArgs.toString());
                      //
                      //
                      //
                      // Create the queue
                      String queueUrl = rabbitMQUrl+"/api/queues/%2F/"+account;
                      Map<String,Object> queueArgs = new HashMap<String,Object>();
                      queueArgs.put("auto_delete", false);
                      queueArgs.put("durable", true);
                      new putRequest().execute(queueUrl,queueArgs.toString());

                      //-----------//
                      //   MySQL   //
                      //-----------//
                      new dbInsert().execute(account,password,displayName,phoneNumber);

                  }
              }
          }
        );

    }


    private boolean isValidPassword(String password){
        if(password.length() >= 6) return true;
        return false;
    }

    private String validPasswordLog(String password){
        if(password.length() < 6) return "Password Length must greater than 5 digits";
        return null;
    }

    private boolean isConfirmPassword(String password, String confirm){
        if(password.equals(confirm)) return true;
        return false;
    }

    private Map<String,Object> strToMap(String mapStr){
        Map<String,Object> myMap = new HashMap<String,Object>();
        mapStr = mapStr.substring(1, mapStr.length()-1);
        String[] pairs = mapStr.split(", ");
        for (int i=0;i<pairs.length;i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split("=");
            myMap.put(keyValue[0], keyValue[1]);
        }
        return myMap;
    }

    private class putRequest extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... args){

            String url = args[0];	// For http put
            String mapStr = args[1]; // Get input value
            Map<String,Object> map = strToMap(mapStr); // Change string back to Map

            HttpURLConnection httpConn = null;
            try{
                URL urlConn = new URL(url);
                httpConn = (HttpURLConnection) urlConn.openConnection();
                httpConn.setRequestMethod("PUT");
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
                httpConn.setRequestProperty("Content-Type", "application/json");
                httpConn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(("admin:admin").getBytes(), Base64.DEFAULT));
                httpConn.connect();
                OutputStreamWriter osw = new OutputStreamWriter(httpConn.getOutputStream());
                JSONObject jsonParam = new JSONObject();
                for(Object key : map.keySet()){
                    jsonParam.put((String) key,map.get(key));
                }
                System.out.println("jsonParam:"+jsonParam);
                osw.write(jsonParam.toString());
                osw.flush();
                osw.close();
                System.err.println("RabbitMQ SignUp:"+httpConn.getResponseCode());
                httpConn.disconnect();

            }catch(Exception e){
                System.out.println("[x] SignUp - putRequest Error:"+e);
            }
            return null;
        }
    }

    private class dbInsert extends AsyncTask<String, Void, Void> {

        private int signUpResult;

        protected Void doInBackground(String... datas){
            System.out.println("datas[0]:"+datas[0]);
            mySQL mySQLDB = new mySQL();
            Map data = new HashMap();
            data.put("id",datas[0]);
            data.put("password",datas[1]);
            data.put("phone",datas[2]);
            data.put("name",datas[3]);

            signUpResult = mySQLDB.insert(data);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            handleSignUpResult(signUpResult);
        }

    }

    private boolean handleSignUpResult (int arg_signUpResult) {
        //handle value
        signUpResult = arg_signUpResult;
        final EditText typeAccount = (EditText) this.findViewById(R.id.create_account);
        final EditText typePassword = (EditText) this.findViewById(R.id.create_password);
        //final EditText typeName = (EditText) this.findViewById(R.id.create_name);
        final EditText typePhone = (EditText) this.findViewById(R.id.create_phone_number);

        String account = typeAccount.getText().toString();
        String password = typePassword.getText().toString();
        //String name = typeName.getText().toString();
        //String phone = typePhone.getText().toString();
        if(signUpResult == Create_Success){
            // Save SharedPreferences;
            AccountData.setAccount(account,password);
            // Leave Intent Activity
            finish();
        }else if(signUpResult == Account_Existed){
            typeAccount.setError("Acconut Has Been Used");
        }else if(signUpResult == Phone_Exitsted) {
            typePhone.setError("Phone Has Been Used");
        }else{
            Toast.makeText(getApplicationContext(),"Create Failed, Account or Phone maybe already been used...",Toast.LENGTH_LONG);
        }

        return true;
    }

}
