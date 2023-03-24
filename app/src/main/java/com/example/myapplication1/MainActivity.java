package com.example.myapplication1;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity {
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel channel;
    WifiP2pManager mManager;
    WiFiDirectBroadcastReceiver receiver;
    ListView listView;
    MyAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView text1 = findViewById(R.id.text1);
        Button btn = findViewById(R.id.btn1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartDiscoverPeer();
            }
        });

        Button btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pInfo info = receiver.getWifiInfo();
                if(info != null){
                    String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
                    if (info.groupFormed && !info.isGroupOwner) {
                        ClientAsyncTask sendTask = new ClientAsyncTask(MainActivity.this, info);
                        sendTask.execute();
                        System.out.println("send msg");
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "소켓 연결 없음",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        listView = findViewById(R.id.wifi_list_view);
        adapter = new MyAdapter(getApplicationContext());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> a_parent, View a_view, int a_position, long a_id) {
                ConnectPeer(a_position);
            }
        });

        //인텐트 필터 설정
        setWifiIntentFilter();

        // 무선랜 디바이스 초기화
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = mManager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(mManager, channel, listView, adapter, text1, this);


    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private  void setWifiIntentFilter(){
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION); //wifi p2p  활성화 여부
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION); //피어 목록 변경 여부
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION); // wifi p2p 연결 상태 변경 여부
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION); // 디바이스 세부 정보 변경 여부
    }

    private void StartDiscoverPeer(){
        mManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "기기 탐색 시작",
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(getApplicationContext(), "기기 탐색 실패",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ConnectPeer(int a_position){
        final MyItem item = (MyItem) adapter.getItem(a_position);
        WifiP2pDevice device = item.getDevice();
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        mManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "success connect "+item.getDeviceName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });



    }

}