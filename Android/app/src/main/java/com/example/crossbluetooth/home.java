package com.example.crossbluetooth;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.UUID;
import java.lang.reflect.Method;
import java.io.IOException;



public class home extends AppCompatActivity {
    private TextView home_status;
    private Button sync_button;

    private String user_id;
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference myRef = database.getInstance().getReference();
    public static DatabaseReference userRef;
    public static DatabaseReference userDeviceRef;
    public static BluetoothAdapter bluetoothAdapter;
    //public static ScrollView connectdeviceButtonList;
    public static LinearLayout connectdeviceButtonList;
    //public static ScrollView notConnectdeviceButtonList;
    public static LinearLayout notConnectdeviceButtonList;

    private final static int REQUEST_ENABLE_BT = 1;
    LinkedList<DeviceInfo> localDevices = new LinkedList<DeviceInfo>();
    LinkedList<String> localDevicesName=new LinkedList<String>();
    LinkedList<DeviceInfo> cloudDevices = new LinkedList<DeviceInfo>();
    LinkedList<String> cloudDevicesName=new LinkedList<String>();
    ArrayList<Button> buttons = new ArrayList<Button>();

    UUID MY_UUID=UUID.randomUUID();









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        home_status=(TextView)findViewById(R.id.home_status);
        user_id=intent.getStringExtra("User ID");
        userRef=myRef.child("User").child(user_id);
        userDeviceRef=userRef.child("Devices");
        sync_button=(Button)findViewById(R.id.Sync);
        //connectdeviceButtonList=(LinearLayout) findViewById(R.id.ConnectedScrollView);
        //notConnectdeviceButtonList=(LinearLayout) findViewById(R.id.NotConnectedDeviceButtonList);
        connectdeviceButtonList=(LinearLayout) findViewById(R.id.ConnectedDeviceButtonList);
        notConnectdeviceButtonList=(LinearLayout) findViewById(R.id.NotConnectedDeviceButtonList);
        home_status.setText(user_id);


        sync_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncDeviceinfo();
            }
        });


        turnOnBluetooth();
        listDevice();
    }

    protected void turnOnBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            home_status.setText("Please turn on Bluetooth\n");
        }else {
            home_status.setText("Your device support Bluetooth\n");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //home_status.setText("Please turn on Bluetooth\n");
        }

        Intent discoverableIntent=new Intent(bluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(bluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
    }

    protected void listDevice(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                localDevicesName.add(deviceName);
                String deviceHardwareAddress = device.getAddress(); // MAC address
                DeviceInfo newDevice=new DeviceInfo(deviceName,deviceHardwareAddress);
                localDevices.add(newDevice);
                home_status.append(deviceName+"\n");
            }
            loadCloudDevice();


        }
    }

    protected void loadCloudDevice(){
        userDeviceRef.addValueEventListener(new ValueEventListener() {
            //@Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cloudDevicesName.clear();
                cloudDevices.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    String deviceName = ds.child("name").getValue(String.class);
                    cloudDevicesName.add(deviceName);
                    String deviceMac = ds.child("macAddr").getValue(String.class);
                    String deviceConnect = ds.child("connecting").getValue(String.class);

                    DeviceInfo cDevice=new DeviceInfo(deviceName,deviceMac,deviceConnect);
                    cloudDevices.add(cDevice);
                }
                //Log.d(TAG, "Value is: " + value);
            }
            //@Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }


    protected void syncDeviceinfo(){

        for(int num=0; num<localDevicesName.size(); num++)
        {
            if (!cloudDevicesName.contains(localDevicesName.get(num))){
                userDeviceRef.child(localDevices.get(num).name).setValue(localDevices.get(num));
                loadCloudDevice();
            }
        }
        listConnectButtons();
    }

    protected void listConnectButtons(){
        buttons.clear();
        connectdeviceButtonList.removeAllViews();
        notConnectdeviceButtonList.removeAllViews();
        for(int num=0; num<cloudDevices.size(); num++)
        {
            DeviceInfo device=cloudDevices.get(num);
            String deviceName=device.name;
            String deviceConnect=device.connecting;
            Button button = new Button(this);
            button.setWidth(80);

            if (deviceConnect.equals(Build.MODEL)){
                button.setText("Disconnect "+deviceName);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        disConnectDevice(device);
                    }
                });
                connectdeviceButtonList.addView(button);
            }else {
                button.setText("Connect "+deviceName);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connectDevice(device);
                    }
                });
                notConnectdeviceButtonList.addView(button);
            }
            buttons.add(button);

        }
    }


    protected void connectDevice(DeviceInfo device){
        device.connecting=Build.MODEL;
        userDeviceRef.child(device.name).child("connecting").setValue(Build.MODEL);

        String address=device.macAddr;

        BluetoothDevice bluetoothdevice = bluetoothAdapter.getRemoteDevice(address);
        BluetoothSocket tmp = null;
        BluetoothSocket mmSocket = null;


        try {
            tmp = bluetoothdevice.createRfcommSocketToServiceRecord(MY_UUID);
            Method m = bluetoothdevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(bluetoothdevice, 1);


        } catch (IOException | NoSuchMethodException e) {
            Log.e("ConnectDevice", "create() failed", e);
            Toast.makeText(this, "create() failed", Toast.LENGTH_SHORT).show();

        } catch (IllegalAccessException e) {
            Log.e("ConnectDevice", "create() failed", e);
            Toast.makeText(this, "create() failed", Toast.LENGTH_SHORT).show();

        } catch (InvocationTargetException e) {
            Log.e("ConnectDevice", "create() failed", e);
            Toast.makeText(this, "create() failed", Toast.LENGTH_SHORT).show();

        }
        mmSocket = tmp;




        listConnectButtons();
    }

    protected void disConnectDevice(DeviceInfo device){
        device.connecting="None";
        userDeviceRef.child(device.name).child("connecting").setValue("None");
        listConnectButtons();
    }
}