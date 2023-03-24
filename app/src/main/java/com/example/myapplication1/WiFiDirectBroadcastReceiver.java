package com.example.myapplication1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    public static WifiP2pManager mManager;
    public static WifiP2pManager.Channel mChannel;
    private ListView listview;
    private MyAdapter adapter;
    private TextView text1;
    public WifiP2pInfo wifiInfo = null;
    private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private Context mainActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       ListView listView, MyAdapter adapter, TextView text1, Context mainActivity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.listview = listView;
        this.adapter = adapter;
        this.text1 = text1;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // WIFI P2P가 켜져 있고 지원되는지 확인
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //activity.setIsWifiP2pEnabled(true);
                System.out.println("state is true");
            }
            else {
                //activity.setIsWifiP2pEnabled(false);
                System.out.println("state is false");
            }
        }
        // peer가 탐지된 경우
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener); // peer 리스트 요청
            } else {
                System.out.println("manager is null");
            }
            System.out.println("The peer list has changed!");

        }
        // 연결이 되면 정보 전달
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, connectionInfoListener);
            }


        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            System.out.println("DeviceList changed!");
        }
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            ArrayList<WifiP2pDevice> refreshedPeers = new ArrayList(peerList.getDeviceList());

            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);

                for(int loop=0; loop < peers.size(); loop++){
                    adapter.addItem(new MyItem(peers.get(loop).deviceName,peers.get(loop)));
                    System.out.println("device: "+peers.get(loop).deviceName);
                }
                listview.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                System.out.println("No devices found");
                return;
            }


        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        WifiP2pInfo info;
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            wifiInfo = info;
            // InetAddress from WifiP2pInfo struct.
            String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

            // After the group negotiation, we can determine the group owner
            // (server).
            if (info.groupFormed && info.isGroupOwner) {
                System.out.println("i'm server and my IP Address is "+groupOwnerAddress);
                //ServerAsyncTask recvTask = new ServerAsyncTask(text1, 8988);
                //recvTask.execute();
                ClientAsyncTask sendTask = new ClientAsyncTask(mainActivity, info);
                sendTask.execute();
                System.out.println("server Thread is start");

            }
            // client
            else if(info.groupFormed && info.isGroupOwner){

            }

        }
    };


    public WifiP2pInfo getWifiInfo(){
        return wifiInfo;
    }


}
