package com.example.mikechung.android_messaging_app_implementation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by MikeChung on 15/5/5.
 */
public class AccountData {
    /* Constants */
    protected static final String user_info = "USER_INFO";
    protected static final String account = "ACCOUNT";
    protected static final String password = "PASSWORD";
    protected static final String name = "NAME";
    protected static final String phone = "PHONE";
    private static final String iv = "1234567890abcdef"; // For AES
    private static final String key = "12345678901234567890123456789012"; // For AES
    /* Members */
    protected static SharedPreferences sp;

    /* Functions */
    // Constructor
    public AccountData(Context context){
        sp = context.getSharedPreferences(user_info, Context.MODE_PRIVATE);
    }
    // Get account
    public static String getAccount(){
        return sp.getString(account, null);
    }
    // Get password
    public static String getPassword(){
        //  return sp.getString(password, "");
        try{ // AES decryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key.getBytes("UTF-8"), "AES"),
                    new IvParameterSpec(iv.getBytes("UTF-8"))
            );
            return new String(
                    cipher.doFinal(Base64.decode(sp.getString(password, "").getBytes("UTF-8"), Base64.DEFAULT)),
                    "UTF-8"
            );
        }catch(Exception e){
            return null;
        }
    }
    // Set account information
    public static String setAccount(String aAccount, String aPassword){
        sp.edit().putString(account, aAccount).commit();
        // sp.edit().putString(password, aPassword).commit();
        try{ // AES encryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key.getBytes("UTF-8"), "AES"),
                    new IvParameterSpec(iv.getBytes("UTF-8"))
            );
            sp.edit().putString(
                    password,
                    Base64.encodeToString(cipher.doFinal(aPassword.getBytes("UTF-8")), Base64.DEFAULT)
            ).commit();
        }catch(Exception e){

        }
        return aAccount;
    }
    public static int clear(){
        sp.edit().clear().commit();
        return 0;
    }

}
