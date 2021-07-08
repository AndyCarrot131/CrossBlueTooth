package com.example.crossbluetooth;

public class DeviceInfo {
    public String name;
    public String macAddr;
    public String connecting;
    public DeviceInfo(){

    }
    public DeviceInfo( String name, String macAddr,String connecting) {
        this.name = name;
        this.macAddr=macAddr;
        this.connecting=connecting;
    }

    public DeviceInfo( String name, String macAddr) {
        this.name = name;
        this.macAddr=macAddr;
        this.connecting="None";
    }
    public String printInfo(){
        return name+","+macAddr+"\n";
    }
}
