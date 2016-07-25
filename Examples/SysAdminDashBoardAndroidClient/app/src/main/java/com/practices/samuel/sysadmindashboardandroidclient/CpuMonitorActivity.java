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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CpuMonitorActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_cpu_monitor);
       // labels.add("RAM");
        dataset = new BarDataSet(entries, "CPU % USED PER CORE");
        chart = new BarChart(CpuMonitorActivity.this);
        chart.getAxisLeft().setAxisMaxValue(100);
        chart.getAxisRight().setAxisMaxValue(100);
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
                        JSONObject obj;
                        JSONArray CPUSArray;
                        entries = new ArrayList<>();
                        labels = new ArrayList<>();
                        SocketMessageParser CpuParser = new CpuPercentMessageParser();
                        String jsonData = CpuParser.ParseString(message).get("CPUDATA");
                        try {
                            if(jsonData.length()>0){
                                obj = new JSONObject(jsonData);
                                CPUSArray =  obj.getJSONArray("CPUDATA");

                                for(int i = 0 ; i < CPUSArray.length() ; i++){
                                    String Label ="cpu: "+CPUSArray.getJSONObject(i).getString("cpu");
                                    Float value = Float.parseFloat(CPUSArray.getJSONObject(i).getString("usedPercent"));
                                    labels.add(Label);
                                    entries.add(new BarEntry(value, i));

                                }

                                dataset = new BarDataSet(entries, "CPU % Used");
                                data = new BarData(labels, dataset);
                                chart.setData(data);
                                chart.invalidate();
                                Log.d("socketMessage",message);
                                Log.d("socketMessage","Refresh chart please inside CPU thread");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        });
        WSManager.execute("");
    }



}
