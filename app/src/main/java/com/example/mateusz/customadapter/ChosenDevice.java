package com.example.mateusz.customadapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
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
import android.app.Activity;

public class ChosenDevice extends Activity {

    EditText messageRx;
    static List<String> msgs;
    static Activity/*AppCompatActivity*/ thisActivity = null;
    BluetoothDevice receivedBluetoothDevice;


    List<menuItem> sensorsList = new ArrayList<menuItem>();
    ListView menuDevices = null;
    TextView textConnection = null;
    ArrayList<String>availableDevice = null;
    ArrayList<TurnedOnDevice>turnedOnDevices = null;
    int chosenDevice = -1;



    public class TurnedOnDevice
    {
        private String model;
        private boolean isTurnedOn;

        public TurnedOnDevice()
        {
            model = null;
            isTurnedOn = false;
        }

        public TurnedOnDevice(String model, boolean isTurnedOn)
        {
            this.model = model;
            this.isTurnedOn = isTurnedOn;
        }

        boolean getIsTurnedOn()
        {
            return isTurnedOn;
        }

        String getModel()
        {
            return model;
        }
    }

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


        //Toast.makeText(ChosenDevice.this, "Chosen Device - onCreate()",Toast.LENGTH_SHORT).show();
        msgs = new ArrayList<>();

        turnedOnDevices = new ArrayList<>();
        availableDevice = new ArrayList<>();
        menuItem singleItem = new menuItem();


        String model = "LSM9DS1";
        String name = "Accelerometer, Magnetometer, Gyroscope";
        TurnedOnDevice singleDevice = new TurnedOnDevice(model, false);
        turnedOnDevices.add(singleDevice);
        availableDevice.add(model);
        singleItem.addSensor(model, name);
        sensorsList.add(singleItem);


        //textConnection = (TextView)findViewById(R.id.textConnection);

        thisActivity = this;


        //receivedBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /*BluetoothConnection connection = new BluetoothConnection(mHandler, );*/
        receivedBluetoothDevice = getIntent().getExtras().getParcelable("device");
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(this, "onResume() ChosenDevice", Toast.LENGTH_LONG).show();

        final adapterMenu menuItems = new adapterMenu();
        menuDevices = (ListView) findViewById(R.id.choosingSensor);
        menuDevices.setAdapter(menuItems);

        menuDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChosenDevice.this, LSM9DS1_sensor.class);
                intent.putExtra("device", receivedBluetoothDevice);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }

    @Override
    public void onPause()
    {
        super.onPause();

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


    public class TimerThread extends Thread
    {
        private class MyTimer extends CountDownTimer
        {
            private long countDownInterval;
            private long millisInFuture;
            /**
             * @param millisInFuture    The number of millis in the future from the call
             *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
             *                          is called.
             * @param countDownInterval The interval along the way to receive
             *                          {@link #onTick(long)} callbacks.
             */
            public MyTimer(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);

                this.millisInFuture = millisInFuture;
                this.countDownInterval = countDownInterval;
            }

            @Override
            public void onTick(long millisUntilFinished)
            {

            }

            @Override
            public void onFinish()
            {

            }
        }
    }
}
