package com.example.mikechung.android_messaging_app_implementation.message;

import com.rabbitmq.client.Channel;

/**
 * Created by MikeChung on 15/4/9.
 */
public class Publisher {

    private String mQueueName;
    private String mExchange;
    private String routingKey = ""; // routingKey equals null means send to all exchanges.
    private String sendMsg;
    private Channel mModel;

    public Publisher(String queueName,String exchange,Channel model) {
        mQueueName = queueName;
        mExchange = exchange;
        mModel = model;
    }

    public void dispose()
    {
        try{
            mModel.abort();
        }catch(Exception e){
            System.out.println("[x] Publisher-dispose:"+e);
        }
    }


    public void updateSendMsg(String message){
        sendMsg = message;
    }

    public void singlePublish()
    {
        try{
            Thread thread = new Thread()
            {
                @Override
                public void run() {
                    try{
                        mModel.basicPublish(mExchange,mQueueName, null, sendMsg.getBytes());
                    }catch(Exception e){
                        System.out.println("[x] Publisher-basicPublish:"+e);
                    }
                }
            };
            thread.start();
        }catch(Exception e){
            System.out.println("[x] Publisher-publish:"+e);
        }
    }


    public void groupPublish()
    {
        try{
            Thread thread = new Thread()
            {
                @Override
                public void run() {
                    try{
                        mModel.basicPublish(mExchange,routingKey, null, sendMsg.getBytes());
                    }catch(Exception e){
                        System.out.println("[x] Publisher-basicPublish:"+e);
                    }
                }
            };
            thread.start();
        }catch(Exception e){
            System.out.println("[x] Publisher-publish:"+e);
        }
    }


}
