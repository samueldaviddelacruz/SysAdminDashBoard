package com.practices.samuel.sysadmindashboardandroidclient;

import android.os.AsyncTask;
import android.util.Log;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by samuel on 7/24/16.
 */
public class WebSocketManager extends AsyncTask<String,Void,Void> {

    private  WebSocket ws;
    private  WebSocketFactory factory = new WebSocketFactory();
    private  boolean isConnected = false;
    public WebSocketManager(String Address){

            try {
                ws = factory.createSocket(Address);
                ws.addProtocol("echo-protocol");
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void SetListener(WebSocketAdapter Listener){
        ws.addListener(Listener);


    }

    public boolean isNotConnected(){

        return !isConnected;
    }

    public boolean isConnectionValid() {
        try {
            ws.connect();
        } catch (WebSocketException e) {
            e.printStackTrace();
            return false;
        }
        ws.disconnect();
        return true;
    }

    public void disconnectWs(){

        ws.disconnect();
    }



    @Override
    protected Void doInBackground(String... strings) {

        try {

            ws.connect();
            isConnected = true;
        } catch (WebSocketException e) {
            isConnected = false;
            e.printStackTrace();
        }
        return null;
    }
}


