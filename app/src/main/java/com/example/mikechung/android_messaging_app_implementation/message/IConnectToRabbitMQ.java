package com.example.mikechung.android_messaging_app_implementation.message;

/**
 * Created by MikeChung on 15/4/9.
 */

import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class IConnectToRabbitMQ {
    /**
     * Class Variables
     */

    public static Channel mModel = null;
    public static Connection mConnection;

    private String mServer;
    protected String myUsername;
    protected String myPassword;

    /**
     * @param server The server address
     * @param username The rabbitmq account
     * @param password The rabbitmq password
     */

    public IConnectToRabbitMQ(String server, String username, String password)
    {
        mServer = server;
        myUsername = username;
        myPassword = password;
    }

    public static void dispose()
    {
        try{
            if(mConnection!=null)
                mConnection.close();
            if(mModel != null)
                mModel.abort();
                //mModel.close();
        }catch (IOException e){
            System.out.println("[x] IConnectToRabbitMQ-Dispose:"+e);
        }
    }

    /**
     * Connect to the broker and create the exchange
     * @return success
     */
    public boolean connectToRabbitMQ()
    {
        if(mModel != null && mModel.isOpen()){ // If channel already declared
            return true;
        }else{
            try{
                System.out.println("Going to Connect to RabbitMQ Server...");
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setHost(mServer);
                connectionFactory.setUsername(myUsername);
                connectionFactory.setPassword(myPassword);
                connectionFactory.setPort(5672);
                mConnection = connectionFactory.newConnection();
                mModel = mConnection.createChannel();
                System.out.println("Connect Success");
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                System.out.println("Connect Failed");
                return false;
            }
        }
    }


}
