package com.example.mateusz.customadapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChosenDevice extends AppCompatActivity {

    EditText messageRx;
    static List<String> msgs;
    static AppCompatActivity thisActivity = null;
    public BluetoothConnection connection = null;
    BluetoothAdapter receivedBluetoothAdapter;
    BluetoothDevice receivedBluetoothDevice;


    //Set<BluetoothDevice> pairedDevices;
    List<menuItem> sensorsList = new ArrayList<menuItem>();
    ListView menuDevices = null;


    /*public void setListLayout() {

        final adapterMenu menuItems = new adapterMenu();
        menuDevices = (ListView) findViewById(R.id.choosingSensor);
        menuDevices.setAdapter(menuItems);

        menuDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                menuItem item = menuItems.getMenuItem(position);
            }
        });
    };*/



    public class menuItem {
        String sensorModel;
        String sensorName;

        void addSensor(String model, String name)
        {
            sensorModel = model;
            sensorName = name;
        }
    }

    public class adapterMenu extends BaseAdapter
    {
        List<menuItem> listMenu = sensorsList;
        public int getCount() {
            return listMenu.size();
        }


        public menuItem getMenuItem(int position)
        {
            return listMenu.get(position);
        }

        @Override
        public Object getItem(int position) {
            return listMenu.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) ChosenDevice.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.chosen_device, parent,false);

            if(convertView==null)
            {
                inflater = (LayoutInflater) ChosenDevice.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chosen_device, parent,false);
            }
            TextView model = (TextView)convertView.findViewById(R.id.model);
            TextView name = (TextView)convertView.findViewById(R.id.modelName);

            menuItem singleItem = listMenu.get(position);

            model.setText(singleItem.sensorModel);
            name.setText(singleItem.sensorName);

            return convertView;
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_device);

        //messageRx = (EditText)findViewById(R.id.msgReceived);
        Toast.makeText(ChosenDevice.this, "Chosen Device",Toast.LENGTH_SHORT).show();
        msgs = new ArrayList<>();


        menuItem singleItem = new menuItem();
        String model = "LSM9DS1";
        String name = "Accelerometer, Magnetometer, Gyroscope";
        singleItem.addSensor(model,name);
        sensorsList.add(singleItem);

       // Bundle bundle = getIntent().getExtras();
       // String msg = bundle.getString("key");

       // msgs.add(msg);

        thisActivity = this;


        receivedBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /*BluetoothConnection connection = new BluetoothConnection(mHandler, );*/
        receivedBluetoothDevice = getIntent().getExtras().getParcelable("device");


    }

    @Override
    public void onResume()
    {
        super.onResume();
        Toast.makeText(this, "onResume() ChosenDevice", Toast.LENGTH_LONG).show();

        if (connection != null)
        {
            connection.stop();
            connection = null;
        }

        if (connection == null)
        {
            Toast.makeText(this, "onResume() -- connection == null", Toast.LENGTH_LONG).show();
            if((receivedBluetoothDevice == null) || (receivedBluetoothAdapter == null))
            {
                finish();
            }

            connection = new BluetoothConnection(mHandler, receivedBluetoothAdapter, receivedBluetoothDevice, ChosenDevice.this);

            if(connection == null)
            {
                //Toast.makeText();
                finish();
            }
            connection.connect(receivedBluetoothDevice);

            final adapterMenu menuItems = new adapterMenu();
            menuDevices = (ListView) findViewById(R.id.choosingSensor);
            menuDevices.setAdapter(menuItems);

            menuDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    menuItem item = menuItems.getMenuItem(position);
                    Intent intent = new Intent(ChosenDevice.this, LSM9DS1_sensor.class);
                    startActivity(intent);
                    /*connection.write(item.sensorModel.getBytes());

                    connection.write(item.sensorName.getBytes());*/

                }
            });
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(connection != null)
        {
            Toast.makeText(this, "onDestroy()", Toast.LENGTH_LONG).show();
            connection.stop();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if(connection != null)
        {
            Toast.makeText(this, "onPause() ChosenDevice", Toast.LENGTH_LONG).show();
            connection.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chosen_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setTextChosenDevice(String msg)
    {
        String message = " ";
        msgs.add(msg);

        for(int i = 0; i < msgs.size(); i++)
        {
            message = message + msgs.get(i)/* + "\n"*/;
        }
        messageRx.setText(message);
    }


    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //ChosenDevice activity = this.;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnection.STATE_CONNECTED:
                            Toast.makeText(thisActivity, "STATE_CONNECTED - handle", Toast.LENGTH_LONG).show();
                            //Intent intent = new Intent(thisActivity, ChosenDevice.class);
                            //thisActivity.startActivity(intent);
                            //Log.e(TAG, "1");
                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            Toast.makeText(thisActivity, "STATE_CONNECTING - handle", Toast.LENGTH_LONG).show();
                            //Log.e(TAG, "2");
                            break;
                        case BluetoothConnection.STATE_LISTEN:
                            Toast.makeText(thisActivity, "STATE_LISTEN - handle", Toast.LENGTH_LONG).show();
                            //Log.e(TAG, "3");
                            break;
                        case BluetoothConnection.STATE_NONE:
                            Toast.makeText(thisActivity, "STATE_NONE - handle", Toast.LENGTH_LONG).show();
                            //Log.e(TAG, "4");
                            break;
                    }
                    break;
                /*case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    //Toast.makeText(MainActivity.this, "MESSAGE_WRITE", Toast.LENGTH_LONG).show();
                    break;*/
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    setTextChosenDevice(readMessage);


                    //Toast.makeText(MainActivity.this, "MESSAGE_READ", Toast.LENGTH_LONG).show();
                    break;
                case Constants.MESSAGE_SOCKET_ERROR:
                    Toast.makeText(thisActivity, "MESSAGE_SOCKET_ERROR", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                case Constants.MESSAGE_CLOSE_SOCKET_ERROR:
                    Toast.makeText(thisActivity, "MESSAGE_CLOSE_SOCKET_ERROR", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                    break;
                case Constants.MESSAGE_DEVICE_UNAVAILABLE:
                    Toast.makeText(thisActivity, "Device unavailable", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                    break;
                case Constants.MESSAGE_DEVICE_CONNECTED_SUCCESSFULLY:
                    Toast.makeText(thisActivity, "Device connected successfully", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                    break;

            }
        }
    };
}
