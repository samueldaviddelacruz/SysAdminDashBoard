package com.practices.samuel.sysadmindashboardandroidclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;

public class DashBoardMenu extends AppCompatActivity implements View.OnClickListener{

    public Button btnRam;
    public Button btnCPU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board_menu);
        btnRam = (Button)findViewById(R.id.RAMbtn);
        btnCPU = (Button)findViewById(R.id.CPUbtn);
        btnRam.setOnClickListener(this);
        btnCPU.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent myIntent;
        switch(view.getId()){
            case R.id.RAMbtn:
                myIntent = new Intent(DashBoardMenu.this,RamMonitorActivity.class);
                myIntent.putExtra("SERVER_ADDRESS",getIntent().getStringExtra("SERVER_ADDRESS"));
                startActivity(myIntent);
                break;
            case R.id.CPUbtn:
                myIntent = new Intent(DashBoardMenu.this,CpuMonitorActivity.class);
                myIntent.putExtra("SERVER_ADDRESS",getIntent().getStringExtra("SERVER_ADDRESS"));
                startActivity(myIntent);
                break;
        }

    }
}
