package com.example.mateusz.customadapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ChosenDevice extends AppCompatActivity {

    EditText messageRx;
    static List<String> msgs;
    static AppCompatActivity thisActivity = null;
    public BluetoothConnection connection = null;
    BluetoothAdapter receivedBluetoothAdapter;
    BluetoothDevice receivedBluetoothDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_device);

        messageRx = (EditText)findViewById(R.id.msgReceived);

        msgs = new ArrayList<>();


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
        Toast.makeText(this, "onResume()", Toast.LENGTH_LONG).show();

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
            Toast.makeText(this, "onPause()", Toast.LENGTH_LONG).show();
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
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    //Toast.makeText(MainActivity.this, "MESSAGE_WRITE", Toast.LENGTH_LONG).show();
                    break;
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
            }
        }
    };
}
