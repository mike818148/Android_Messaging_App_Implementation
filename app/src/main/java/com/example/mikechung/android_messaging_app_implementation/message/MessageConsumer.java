package com.example.mikechung.android_messaging_app_implementation.message;

/**
 * Created by MikeChung on 15/4/9.
 */

import java.io.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import android.os.Handler;

public class MessageConsumer{

    private Channel mModel;
    private String mExchange;
    private String mExchangeType;
    private boolean Running;

    public MessageConsumer(String exchange, String exchangeType, String queueName, Channel model) {
        mExchange = exchange;
        mExchangeType = exchangeType;
        mQueue = queueName;
        mModel = model;
        System.out.println("mModel:"+mModel);
    }

    //The Queue name for this consumer
    private String mQueue;
    private QueueingConsumer MySubscription;
    private Handler mMessageHandler = new Handler();
    private Handler mConsumeHandler = new Handler();

    //last message to post back
    private byte[] mLastMessage;

    //A reference to the listener, we can only have one at a time(for now)
    private OnReceiveMessageHandler mOnReceiveMessageHandler;

    // An interface to be implemented by an object that is interested in messages(listener)
    public interface OnReceiveMessageHandler{
        public void onReceiveMessage(byte[] message);
    }

    /**
     * Set the callback for received messages
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(OnReceiveMessageHandler handler) {
        mOnReceiveMessageHandler = handler;
    }

    // Create runnable for posting back to main thread
    final Runnable mReturnMessage = new Runnable() {
        public void run() {
            mOnReceiveMessageHandler.onReceiveMessage(mLastMessage);
        }
    };

    final Runnable mConsumeRunner = new Runnable() {
        public void run() {
            Consume();
        }
    };

    /**
     * Create Exchange and then start consuming. A binding needs to be added before any messages will be delivered
     */
    public boolean connectToRabbitMQ()
    {
        try {
            System.out.println("\n [Info] queueDeclarePassive:"+mModel.queueDeclarePassive(mQueue).getQueue());
            if(mQueue.equals(mModel.queueDeclarePassive(mQueue).getQueue())){
                System.out.println("[o] queue alread exists!");
            }else{
                mModel.queueDeclare(mQueue, false, false, false, null);
            }

            //addBinding("");
            if(MySubscription == null) {
                MySubscription = new QueueingConsumer(mModel);
            }
            mModel.basicConsume(mQueue, false, MySubscription);
        }catch (Exception e) {
            System.out.println("[x] MessageConsumer - connectToRabbitMQ Error:"+e);
            return false;
        }

        Running = true;
        mConsumeHandler.post(mConsumeRunner);
        System.out.println("[o] MessageConsumer - ConnectToRabbitMQ Success");
        return true;
    }

    /**
     * Add a binding between this consumers Queue and the Exchange with routingKey
     * @param routingKey the binding key eg GOOG
     */
    public void addBinding(String routingKey)
    {
        try {
            mModel.queueBind(mQueue, mExchange, routingKey);
        } catch (IOException e) {
            System.out.println("[x] MessageConusmer - addBinding BindFailed:"+e);
        }
    }

    /**
     * Remove binding between this consumers Queue and the Exchange with routingKey
     * @param routingKey the binding key eg GOOG
     */
    public void removeBinding(String routingKey)
    {
        try {
            mModel.queueUnbind(mQueue, mExchange, routingKey);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("[x] MessageConsumer - removeBinding Error:"+e);
        }
    }

    private void Consume()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                while(Running){
                    QueueingConsumer.Delivery delivery;
                    try {
                        delivery = MySubscription.nextDelivery();
                        mLastMessage = delivery.getBody();
                        mMessageHandler.post(mReturnMessage);
                        try {
                            mModel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    public void dispose(){
        Running = false;
    }

}
