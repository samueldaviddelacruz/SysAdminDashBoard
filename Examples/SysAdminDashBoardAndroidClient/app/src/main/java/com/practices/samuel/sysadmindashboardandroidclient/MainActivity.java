package com.practices.samuel.sysadmindashboardandroidclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public Button btnConnect;
    public EditText ipTxt;
    WebSocketManager myManager;
    String Address;
    public final static String SERVER_ADDRESS = "SERVER_ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnConnect= (Button)findViewById(R.id.btnConnect);
        ipTxt = (EditText)findViewById(R.id.IPeditTxt);
       // ipTxt.setText("0.0.0.0");
        btnConnect.setOnClickListener(this);

    }

private boolean IsAddressIsEmpty(){
    return ipTxt.getText().toString().length()==0;


}
    public void tryConnectionToWS(){
        String url = IsAddressIsEmpty()?"0.0.0.0":ipTxt.getText().toString();
        Address = "ws://"+ url;

        //Log.d("socketMessage","valid URL");
        Log.d("IP",Address);
        myManager = new WebSocketManager(Address);
        myManager.SetListener(new WebSocketAdapter(){
            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                Log.d("socketMessage","Connection Succesfull");
                myManager.disconnectWs();
                moveToMainMenu();
            }
        });
        myManager.execute("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("app destroyed","activity destroyed");
        myManager.disconnectWs();
        myManager.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("app destroyed","activity resumed");

    }


    @Override
    protected void onPause(){
        super.onPause();
        Log.d("app destroyed","activity Paused");

      //  myManager.disconnectWs();
       // myManager.cancel(true);
    }

public void moveToMainMenu(){
    Intent myIntent = new Intent(MainActivity.this,DashBoardMenu.class);
    myIntent.putExtra(SERVER_ADDRESS,Address);
    Log.d("button pressed","button clicked!");
    startActivity(myIntent);
}
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnConnect:
                tryConnectionToWS();
                break;
        }
    }
}
