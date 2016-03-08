package com.example.mateusz.customadapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class Temperature_sensor extends Fragment implements View.OnClickListener {

    static class Measurements
    {
        String id_mrs;
        float x;
        float y;
        float z;

        Measurements(String id, float x, float y, float z)
        {
            id_mrs = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float getX() {
            return x;
        }

        public String getId_mrs() {
            return id_mrs;
        }

    }

    public final int REQUEST_ENABLE_BT = 1;
    private boolean turnedOn = false;
    Set<BluetoothDevice> pairedDevices;

    //toggle Button
    static boolean Lock;//whether lock the x-axis to 0-5
    static boolean AutoScrollX;//auto scroll to the last x value
    static boolean Stream;//Start or stop streaming

    static int ileRazyX = 0;



    //AlertDialog.Builder builder;

    int huj = 0;

    static final double STEP = 0.1;
    static final int MAX_X = 10;


    static DataPoint[] valuesX = new DataPoint[(int)(MAX_X/STEP) + 1];


    List<BtDevice> bluetoothDevicesList = new ArrayList<>();
    static List<String> readChars = new ArrayList<>();
    static List<Measurements>mrs = new ArrayList<>();

    //Button init
    Button bXminus;
    Button bXplus;
    ToggleButton tbLock;
    ToggleButton tbScroll;
    ToggleButton tbStream;

    //GraphView init
    static LinearLayout GraphView;
    static LinearLayout RightLayout;
    static com.jjoe64.graphview.GraphView graphView;
    static LineGraphSeries SeriesT;


    private static double graph2LastXValueX = 0;


    private static int Xview=10;
    Button bConnect, bDisconnect;

    //ListView lView;

    static Activity/*AppCompatActivity*/ thisActivity = null;
    public BluetoothConnection connection = null;
    BluetoothAdapter receivedBluetoothAdapter;
    static BluetoothDevice receivedBluetoothDevice;

    private final MyHandler mHandler = new MyHandler(this);

    static boolean canI = true;


    public void resetVariables()
    {
        graph2LastXValueX = 0;

        ileRazyX = 0;
    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {

            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                boolean may = true;
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                BtDevice dev = new BtDevice();

                dev.devName = device.getName() + "\n" + device.getAddress();
                dev.devAddress = "\nFound ";

                if(!bluetoothDevicesList.isEmpty())
                {
                    for(int z = 0; z < bluetoothDevicesList.size(); z++)
                    {
                        if(bluetoothDevicesList.get(z).devName.equals(dev.devName))
                        {
                            may = false;
                        }
                    }
                }


                if(may)
                {
                    bluetoothDevicesList.add(dev);
                }
                //setListLayout();
                //pairedDevices.add(device);

            }
        }
    };


    void initBt()
    {
        receivedBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(receivedBluetoothAdapter == null)
        {
            ;
        }
        else
        {
            if(!receivedBluetoothAdapter.isEnabled())
            {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
            }
            else
            {
                turnedOn = true;
            }

            if(turnedOn)
            {
                pairedDevices = receivedBluetoothAdapter.getBondedDevices();

                if(pairedDevices.size() > 0)
                {
                    for (BluetoothDevice device:pairedDevices)
                    {
                        boolean may = true;
                        BtDevice device_paired = new BtDevice();
                        device_paired.devName = device.getName() + "\n" + device.getAddress();
                        device_paired.devAddress = "\nPaired ";

                        if(!bluetoothDevicesList.isEmpty())
                        {
                            for(int z = 0; z < bluetoothDevicesList.size(); z++)
                            {
                                if(bluetoothDevicesList.get(z).devName.equals(device_paired.devName) && bluetoothDevicesList.get(z).devAddress.equals(device_paired.devAddress))
                                {
                                    may = false;
                                }
                            }
                        }

                        if(may)
                        {
                            bluetoothDevicesList.add(device_paired);
                        }
                    }
                    //Toast.makeText(MainActivity.this, "PairedDevices", Toast.LENGTH_LONG).show();
                    //setListLayout();
                }

                if(receivedBluetoothAdapter.startDiscovery()) //szukaj urządzeń
                {
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    getContext().registerReceiver(mReceiver, filter);

                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_ENABLE_BT == resultCode)
        {
            Toast.makeText(GraphView.getContext(), "Bluetooth is turning on", Toast.LENGTH_LONG);
        }
        else if (Activity.RESULT_OK == resultCode)
        {
            Toast.makeText(GraphView.getContext(), "Bluetooth is turned on successfully...", Toast.LENGTH_LONG);
            turnedOn = true;
        }
        else if(Activity.RESULT_CANCELED == resultCode)
        {
            Toast.makeText(GraphView.getContext(), "User does not turned on bluetooth", Toast.LENGTH_LONG);
            turnedOn = false;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //dialogLayout = new LinearLayout(getContext());
        initBt();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ChosenDevice) activity).onSectionAttached(
                getArguments().getInt(ChosenDevice.ARG_SECTION_NUMBER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        RightLayout = (LinearLayout)rootView.findViewById(R.id.LL2);
        RightLayout.setBackgroundColor(Color.BLACK);

        init(rootView);
        ButtonInit(rootView);

        receivedBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // receivedBluetoothDevice = (BluetoothDevice)getArguments().getParcelable("device");

        return rootView;
    }

    public void init(View rootView)
    {
        //init graphview
        GraphView = (LinearLayout) rootView.findViewById(R.id.Graph);
        //GraphView.setBackgroundColor(Color.BLACK);

        GraphView.setBackgroundColor(Color.BLACK);

        graphView = /*new GraphView(this);*/(com.jjoe64.graphview.GraphView)rootView.findViewById(R.id.graph);

        graphView.setDrawingCacheEnabled(true);
        graphView.setBackgroundColor(Color.BLACK);


        graphView.getGridLabelRenderer().setGridColor(Color.WHITE);
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Temperature [*C]");
        graphView.getGridLabelRenderer().setVerticalAxisTitleColor(Color.WHITE);

        graphView.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);

        graphView.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setVerticalLabelsVisible(true);

        graphView.getGridLabelRenderer().reloadStyles();
        //graphView.getGridLabelRenderer().draw();

        graphView.setTitle("Temperature");
        graphView.setTitleColor(Color.WHITE);

        /*Series = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0,0)});
        Series.setTitle("Signal");
        Series.setColor(Color.RED);
        Series.setThickness(2);*/

        SeriesT = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesT.setTitle("Temperature");
        SeriesT.setColor(Color.RED);
        SeriesT.setThickness(2);



        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(Xview);

        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScalable(true);

        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-30);
        graphView.getViewport().setMaxY(120);

        // graphView.addSeries(Series);
        graphView.addSeries(SeriesT);
        /*graphView.addSeries(SeriesY);
        graphView.addSeries(SeriesZ);*/


//        GraphView.addView(graphView);
        //graphView.getViewport()

    }


    void ButtonInit(View rootView){
        bConnect = (Button)rootView.findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);
        bDisconnect = (Button)rootView.findViewById(R.id.bDisconnect);
        bDisconnect.setOnClickListener(this);
        //X-axis control button
        bXminus = (Button)rootView.findViewById(R.id.bXminus);
        bXminus.setOnClickListener(this);
        bXplus = (Button)rootView.findViewById(R.id.bXplus);
        bXplus.setOnClickListener(this);
        //
        tbLock = (ToggleButton)rootView.findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);
        tbScroll = (ToggleButton)rootView.findViewById(R.id.tbScroll);
        tbScroll.setOnClickListener(this);
        tbStream = (ToggleButton)rootView.findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);
        //init toggleButton
        Lock=true;
        AutoScrollX=true;
        Stream=true;
    }

    public void connectBt()
    {
        if(receivedBluetoothDevice != null)
        {
            if (connection != null) {
                connection.stop();
                connection = null;
            }


            if(connection == null)
            {
                if ((receivedBluetoothDevice == null) || (receivedBluetoothAdapter == null))
                {
                    //finish();
                }

                connection = new BluetoothConnection(mHandler, receivedBluetoothAdapter, receivedBluetoothDevice);

                if(connection == null)
                {
                    //finish();
                }
                connection.connect(receivedBluetoothDevice);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Toast.makeText(this, "LSM9DS1_sensor1", Toast.LENGTH_LONG).show();
        getActivity().unregisterReceiver(mReceiver);

        if(connection != null)
        {
            //Toast.makeText(this, "LSM9DS1_sensor", Toast.LENGTH_LONG).show();
            connection.stop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(this, "onPause() ChosenDevice1", Toast.LENGTH_LONG).show();
        if(connection != null)
        {
            //Toast.makeText(this, "onPause() ChosenDevice", Toast.LENGTH_LONG).show();
            connection.stop();
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    /*private final Handler mHandler = new Handler(Looper.getMainLooper())*/
    private static class MyHandler extends Handler
    {

        private final WeakReference<Temperature_sensor> fragment;

        public MyHandler(Temperature_sensor f)
        {
            fragment = new WeakReference<Temperature_sensor>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            //ChosenDevice activity = this.;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnection.STATE_CONNECTED:
                            Toast.makeText(GraphView.getContext(), "STATE_CONNECTED - handle", Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            Toast.makeText(GraphView.getContext(), "STATE_CONNECTING - handle", Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothConnection.STATE_LISTEN:
                            Toast.makeText(GraphView.getContext(), "STATE_LISTEN - handle", Toast.LENGTH_LONG).show();
                            //Log.e(TAG, "3");
                            break;
                        case BluetoothConnection.STATE_NONE:
                            Toast.makeText(GraphView.getContext(), "STATE_NONE - handle", Toast.LENGTH_LONG).show();
                            //Log.e(TAG, "4");
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    if(canI)
                    {
                        final String readMessage = new String(readBuf, 0, msg.arg1);
                        //char[] readMessage1 = readMessage.toCharArray();

                        if(!readMessage.contains("]"))
                        {
                            readChars.add(readMessage);
                        }
                        else
                        {
                            canI = false;
                            readChars.add(readMessage);

                            String[] k = new String[readChars.size()];
                            k = readChars.toArray(k);
                            //String m = "";

                            Iterator<String> iter = readChars.iterator();
                            StringBuilder sb = new StringBuilder();

                            if (iter.hasNext()) {
                                sb.append(iter.next());
                                while (iter.hasNext()) {
                                    sb.append("").append(iter.next());
                                }
                            }
                            final String m = sb.toString();

                            if(!readChars.isEmpty())
                            {
                                readChars.clear();
                            }

                            (new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        JSONObject jsonRootObject = new JSONObject(m);

                                        //Get the instance of JSONArray that contains JSONObjects
                                        JSONArray jsonArray = jsonRootObject.optJSONArray("m");

                                        if(!mrs.isEmpty())
                                        {
                                            mrs.clear();
                                        }

                                        if(jsonArray != null){
                                            for(int i = 0; i < jsonArray.length(); i++)
                                            {
                                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                                String id = jsonObject.optString("id").toString();

                                                float x = Float.parseFloat(jsonObject.optString("x").toString());
                                                float y = 0;
                                                float z = 0;

                                                mrs.add(new Measurements(id,x,y,z));
                                            }
                                        }
                                    }catch (org.json.JSONException e)
                                    {
                                        ;
                                    }

                                    fragment.get().getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (readMessage != null && !mrs.isEmpty()){
                                                for (int i = 0; i < mrs.size(); i++)
                                                {
                                                    if (mrs.get(i).getId_mrs().equals("t")) {
                                                        if (ileRazyX < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesX[ileRazyX] = new DataPoint(graph2LastXValueX, mrs.get(i).getX());
                                                            SeriesT.appendData(valuesX[ileRazyX], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueX >= Xview && Lock == true) {
                                                                SeriesT.resetData(new DataPoint[]{});
                                                                graph2LastXValueX = 0;
                                                            } else graph2LastXValueX += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueX - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }

                                                            ileRazyX++;

                                                        } else {
                                                            ileRazyX = 0;

                                                                    /*GraphView.removeView(graphView);
                                                                    GraphView.addView(graphView);*/
                                                        }
                                                    }
                                                }
                                                GraphView.removeView(graphView);
                                                GraphView.addView(graphView);

                                                canI = true;
                                            }
                                            else
                                            {
                                                canI = true;
                                            }
                                        }
                                    });
                                }
                            })).start();
                        }
                    }

                    break;
                case Constants.MESSAGE_SOCKET_ERROR:
                    Toast.makeText(GraphView.getContext(), "MESSAGE_SOCKET_ERROR", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                case Constants.MESSAGE_CLOSE_SOCKET_ERROR:
                    Toast.makeText(GraphView.getContext(), "MESSAGE_CLOSE_SOCKET_ERROR", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                    break;
                case Constants.MESSAGE_BLUETOOTH_DEVICE_UNAVAILABLE:
                    receivedBluetoothDevice = null;
                    Toast.makeText(GraphView.getContext(), "Bluetooth device unavailable", Toast.LENGTH_LONG).show();
                    //chosenDevice = -1;
                    //textConnection.setText("Bluetooth device unavailable");
                    //thisActivity.finish();
                    break;
                case Constants.MESSAGE_DEVICE_CONNECTED_SUCCESSFULLY:
                    Toast.makeText(GraphView.getContext(), "Device connected successfully", Toast.LENGTH_LONG).show();

                    // connection.write(availableDevice.get(chosenDevice).getBytes());
                    //thisActivity.finish();
                    break;
                case Constants.MESSAGE_DEVICE_NO_CHOICE:
                    Toast.makeText(GraphView.getContext(), "You did not chosen a device", Toast.LENGTH_LONG).show();
                    break;
                case Constants.MESSAGE_INPUT_OUTPUT_STREAM_UNAVAILABLE:
                    Toast.makeText(GraphView.getContext(), "I/O stream unavailable", Toast.LENGTH_LONG).show();
                    break;
                case Constants.MESSAGE_REMOTE_DEV_DISCONNECTED:
                    Toast.makeText(GraphView.getContext(), "Remote device disconnected", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public static boolean isIntegerNumber(String s)
    {
        try
        {
            double x = (double)Integer.parseInt(s);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    public static boolean isFloatNumber(String s)
    {
        try
        {
            double x = (double)Float.parseFloat(s);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    public static float parseFloat(String s)
    {
        float x;
        try
        {
            x = Float.parseFloat(s);
        }
        catch(NumberFormatException e)
        {
            return -1000;
        }
        return x;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bConnect:
                initBt();
                /*if(receivedBluetoothDevice == null)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/
                showDialogListView();
                connectBt();
                break;
            case R.id.bDisconnect:
                if(connection != null)
                {
                    receivedBluetoothDevice = null;
                    connection.disconnect();
                    connection = null;

                    resetVariables();

                    SeriesT.resetData(new DataPoint[]{});


                    GraphView.removeView(graphView);
                    GraphView.addView(graphView);
                }
                break;
            case R.id.bXminus:
                if (Xview>1) Xview--;
                break;
            case R.id.bXplus:
                if (Xview<30) Xview++;
                break;
            case R.id.tbLock:
                if (tbLock.isChecked()){
                    Lock = true;
                }else{
                    Lock = false;
                }
                break;
            case R.id.tbScroll:
                if (tbScroll.isChecked()){
                    AutoScrollX = true;
                }else{
                    AutoScrollX = false;
                }
                break;
            case R.id.tbStream:
                //if (tbStream.isChecked())
                //{
                if (connection != null)
                    connection.write("Temperature".getBytes());
                //}
                /*else{
                    if (connection != null)
                        connection.write("Q".getBytes());*/
                //}
                break;
        }
    }

    public class BtDeviceAdapter extends BaseAdapter
    {
        List<BtDevice> devDescription = bluetoothDevicesList;

        public BtDevice getBtDevice(int position)
        {
            return devDescription.get(position);
        }

        @Override
        public Object getItem(int position) {
            return devDescription.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return devDescription.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.listitem, parent,false);

            if(convertView==null)
            {
                inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listitem, parent,false);
            }
            TextView chapterName = (TextView)convertView.findViewById(R.id.textView1);
            TextView chapterDesc = (TextView)convertView.findViewById(R.id.textView2);

            BtDevice dev = devDescription.get(position);

            chapterName.setText(dev.devName);
            chapterDesc.setText(dev.devAddress);

            return convertView;
        }
    }


    public class BtDevice {
        String devName;
        String devAddress;
    }

    public void setListLayout(ListView lV)
    {
        final BtDeviceAdapter btAdapterList = new BtDeviceAdapter();

        //lView = (ListView)getActivity().findViewById(R.id.listView12);

        ListView lView;
        lView = lV;//new ListView(getActivity());
        lView.setAdapter(btAdapterList);



        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice tmpDevice = null;

                BtDevice dev = btAdapterList.getBtDevice(position);

                boolean found1 = false;

                for (BluetoothDevice device : pairedDevices) {
                    //Toast.makeText(MainActivity.this, "Znaleziono1", Toast.LENGTH_LONG).show();
                    if (dev.devName.equals(device.getName() + "\n" + device.getAddress())) {
                        tmpDevice = device;
                        found1 = true;

                        break;
                    }
                }

                if(found1)
                {
                    receivedBluetoothDevice = tmpDevice;
                }
            }
        });
    }

    public void showDialogListView()
    {
        //static int i = 0;
        if(receivedBluetoothDevice == null)
        {
            /*if((ViewGroup)lView != null)
            {
                if(((ViewGroup)lView.getParent()).getChildCount() > 0)
                {
                    ((ViewGroup)lView.getParent()).removeView(lView);
                }
            }*/

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog dialog;
            builder.setTitle("Chooose device");

            LinearLayout dialogLayout = new LinearLayout(getContext());
            ListView lView = new ListView(getContext());
            //LinearLayout dialogLayout;
            setListLayout(lView);

            if(dialogLayout.getChildCount() > 0)
            {
                //dialogLayout.removeAllViewsInLayout();
                dialogLayout.removeAllViews();
                //dialogLayout.
            }
            //dialogLayout.addView(lView);

            //if(huj == 0)
            //{
            dialogLayout.addView(lView);
            huj++;
            //}

            builder.setCancelable(true);

            builder.setPositiveButton("OK", null);

            builder.setView(dialogLayout);

            //builder.setView(lView);
            dialog = builder.create();

            //

            dialog.show();
            //builder.show();

        }
    }
}