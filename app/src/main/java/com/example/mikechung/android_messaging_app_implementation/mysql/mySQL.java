package com.example.mikechung.android_messaging_app_implementation.mysql;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.util.Log;
/**
 * Created by MikeChung on 15/4/22.
 */
public class mySQL {

    private String create_account_url = "http://52.68.99.148/create_user.php";
    private String user_login_url = "http://52.68.99.148/login_user.php";
    private String find_account_url = "http://52.68.99.148/find_user.php";
    private static final int Create_Success = 0;
    private static final int Create_Failed = 1;
    private static final int Account_Existed = 2;
    private static final int Phone_Exitsted = 3;
    private static final int Login_Success = 0;
    private static final int Wrong_Password = 1;
    private static final int None_Account = 2;
    private static final int Login_Error = -1;

    public int insert(Map data){
        /**
         data structure
         +----+----------+------+------+--------+
         | id | password | name | phone | photo |
         +----+----------+------+------+--------+
         */
        HttpURLConnection httpConn = null;
        String httpResponse = null;
        try{
            URL urlConn = new URL(create_account_url);
            httpConn = (HttpURLConnection) urlConn.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("charset", "UTF-8");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", (String) data.get("id")));
            params.add(new BasicNameValuePair("password", (String) data.get("password")));
            params.add(new BasicNameValuePair("phone", (String) data.get("name")));
            params.add(new BasicNameValuePair("name", (String) data.get("phone")));
            //params.add(new BasicNameValuePair("photo", (String) data.get("password")));
            httpConn.setRequestProperty("Content-Length", "" + Integer.toString(getQuery(params).getBytes().length));
            httpConn.setUseCaches (false);
            DataOutputStream wr = new DataOutputStream(httpConn.getOutputStream ());
            wr.writeBytes(getQuery(params));
            wr.flush();
            wr.close();
            httpConn.connect();
            System.out.println("MySql SignUp:"+httpConn.getResponseCode());
            httpResponse = getResponse(httpConn);
            System.out.println("MySQL Response:"+httpResponse);

        }catch(Exception e){
            Log.e("Connection Fail", e.toString());
        }finally {
            if(httpConn != null) {
                httpConn.disconnect();
            }
        }
        System.out.println("httpResponse:"+httpResponse+";");
        httpResponse = httpResponse.replace("\n","").replace("\r","");
        System.out.println("httpResponse:"+httpResponse+";");

        if("Create Success".equals(httpResponse)){
            return Create_Success;
        }else if("Account Existed".equals(httpResponse)){
            System.out.println("ACCOUNT EXISTED....");
            return Account_Existed;
        }else if("Phone Existed".equals(httpResponse)){
            return Phone_Exitsted;
        }else if("Create Failed".equals(httpResponse)){
            return Create_Failed;
        }else{
            System.err.println("mySQL inset Error....");
        }
        return Create_Failed;
    }

    public HashMap findUser(String query){
        // Note: query is either user id or phone number
        HttpURLConnection httpConn = null;

        try{
            URL urlConn = new URL(find_account_url);
            httpConn = (HttpURLConnection) urlConn.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("charset", "UTF-8");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("query", query));
            httpConn.setRequestProperty("Content-Length", "" + Integer.toString(getQuery(params).getBytes().length));
            httpConn.setUseCaches (false);
            DataOutputStream wr = new DataOutputStream(httpConn.getOutputStream ());
            wr.writeBytes(getQuery(params));
            wr.flush();
            wr.close();
            httpConn.connect();
            System.err.println("MySql FindUser:"+httpConn.getResponseCode());
            JSONObject serverResponse = new JSONObject(getResponse(httpConn));
            System.out.println("serverResponse:"+serverResponse.getString("name"));
            HashMap returnMap = new HashMap();
            returnMap.put("name",serverResponse.get("name"));
            returnMap.put("displayName",serverResponse.get("displayName"));
            returnMap.put("phone",serverResponse.get("phone"));
            return returnMap;
        }catch(Exception e){
            Log.e("Connection Fail", e.toString());
        }finally {

            if(httpConn != null) {
                httpConn.disconnect();
            }
        }
        return null;
    }

    public int loginCheck(String id, String password){

        HttpURLConnection httpConn = null;
        String loginResponse = null;

        try{
            URL urlConn = new URL(user_login_url);
            httpConn = (HttpURLConnection) urlConn.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("charset", "UTF-8");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", id));
            params.add(new BasicNameValuePair("password", password));
            httpConn.setRequestProperty("Content-Length", "" + Integer.toString(getQuery(params).getBytes().length));
            httpConn.setUseCaches (false);
            DataOutputStream wr = new DataOutputStream(httpConn.getOutputStream ());
            wr.writeBytes(getQuery(params));
            wr.flush();
            wr.close();
            httpConn.connect();

            loginResponse = getResponse(httpConn);
            System.out.println("MySql Login:"+httpConn.getResponseCode());
            System.out.println("MySQL Response:"+loginResponse);

        }catch(Exception e){
            Log.e("Connection Fail", e.toString());
        }finally {

            if(httpConn != null) {
                httpConn.disconnect();
            }
        }

        loginResponse = loginResponse.replace("\n","").replace("\r","");

        if("Correct Password".equals(loginResponse)){
            return Login_Success;
        }else if("Wrong Password".equals(loginResponse)){
            return Wrong_Password;
        }else if("None Account".equals(loginResponse)){
            return None_Account;
        }else{
            System.err.println("mySQL login check Error....");
        }
        return Login_Error;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        System.out.println("\n result:"+result.toString());
        return result.toString();
    }

    private String getResponse(HttpURLConnection connection){
        //Get Response
        InputStream is;
        try {
            is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (IOException e) {
            System.out.println("MySQL getResponse Error:"+e);
        }
        return null;
    }
}
