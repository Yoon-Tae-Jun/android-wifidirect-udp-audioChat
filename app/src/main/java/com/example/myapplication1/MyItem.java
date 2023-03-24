package com.example.myapplication1;

import android.net.wifi.p2p.WifiP2pDevice;

public class MyItem {
    private String device_name;
    private WifiP2pDevice device;

    public MyItem(String device_name,  WifiP2pDevice device) {
        this.device_name = device_name;
        this.device = device;
    }

    public MyItem() {}

    public String getDeviceName() {
        return device_name;
    }

    public void setDeviceName(String id) {
        this.device_name = id;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) { this.device = device; }
}
