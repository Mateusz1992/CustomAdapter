package com.example.mateusz.customadapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

//import android.support.v4.app.FragmentActivity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";


    BluetoothAdapter bluetoothAdapter;
    //List<codeLearnChapter> bluetoothDevicesList = new ArrayList<codeLearnChapter>();
    ListView codeLearnLessons;


    Button searchButton;
    Set<BluetoothDevice> pairedDevices;

    /*private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;*/
    private int mState;

    public final int REQUEST_ENABLE_BT = 1;
    private boolean turnedOn = false;
    public static final int MESSAGE_READ = 2;

    List<String> listOfSentMessages = new ArrayList<String>();




    @Override
    protected void onDestroy() {
        super.onDestroy();


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    @Override
    protected void onResume() {
        super.onResume();

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        Intent intent = new Intent(MainActivity.this, ChosenDevice.class);

        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}


