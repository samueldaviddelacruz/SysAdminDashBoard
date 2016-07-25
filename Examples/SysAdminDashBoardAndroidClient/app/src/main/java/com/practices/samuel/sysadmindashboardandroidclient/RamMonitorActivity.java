package com.practices.samuel.sysadmindashboardandroidclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RamMonitorActivity extends AppCompatActivity {
    ArrayList<BarEntry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<String>();
    BarChart chart;
    BarDataSet dataset;
    BarData data;
    WebSocketManager WSManager;
    String Address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ram_monitor);

        labels.add("RAM");
        dataset = new BarDataSet(entries, "Ram Used");
        chart = new BarChart(RamMonitorActivity.this);
        data = new BarData(labels, dataset);
        chart.setData(data);

        setContentView(chart);

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("app destroyed","activity resumed");
        ConnectToWS();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("app destroyed","activity destroyed");
        WSManager.disconnectWs();
        WSManager.cancel(true);
    }

    @Override
    protected void onPause(){
        super.onPause();
         Log.d("app destroyed","activity Paused");
         WSManager.disconnectWs();
         WSManager.cancel(true);
    }


    public void ConnectToWS(){
        Address = getIntent().getStringExtra("SERVER_ADDRESS");
        WSManager = new WebSocketManager(Address);
        WSManager.SetListener(new WebSocketAdapter(){
            @Override
            public void onTextMessage(WebSocket websocket, final String message) {
                // System.out.println(message);

                runOnUiThread(new Runnable(){
                    public void run() {

                        entries = new ArrayList<>();
                        SocketMessageParser RamParser = new RamPercentMessageParser();
                        String barValue = RamParser.ParseString(message).get("RAMusagePercent");
                        entries.add(new BarEntry(Float.parseFloat(barValue), 0));
                        dataset = new BarDataSet(entries, "Ram Used");
                        data = new BarData(labels, dataset);
                        chart.setData(data);
                        chart.invalidate();
                        Log.d("socketMessage",message);
                        Log.d("socketMessage","Refresh chart plz inside ui tread! 2");
                    }
                });
            }
        });
        WSManager.execute("");
    }




}
