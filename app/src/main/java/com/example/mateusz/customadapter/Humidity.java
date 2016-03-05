package com.example.mateusz.customadapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Humidity  extends Fragment implements View.OnClickListener {

    public final int REQUEST_ENABLE_BT = 1;
    private boolean turnedOn = false;
    Set<BluetoothDevice> pairedDevices;

    //toggle Button
    static boolean Lock;//whether lock the x-axis to 0-5
    static boolean AutoScrollX;//auto scroll to the last x value
    static boolean Stream;//Start or stop streaming
    static int ileRazy = 0;

    static int ileRazyX = 0;
    static int ileRazyY = 0;
    static int ileRazyZ = 0;


    //AlertDialog.Builder builder;

    int huj = 0;

    static DataPoint[] values = new DataPoint[100];
    static DataPoint[] valuesX = new DataPoint[100];
    static DataPoint[] valuesY = new DataPoint[100];
    static DataPoint[] valuesZ = new DataPoint[100];

    List<BtDevice> bluetoothDevicesList = new ArrayList<>();

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
    static LineGraphSeries Series;
    static LineGraphSeries SeriesX;
    static LineGraphSeries SeriesY;
    static LineGraphSeries SeriesZ;
    //graph value
    private static double graph2LastXValue = 0;
    private static double graph2LastXValueX = 0;
    private static double graph2LastXValueY = 0;
    private static double graph2LastXValueZ = 0;

    private static int Xview=10;
    Button bConnect, bDisconnect;

    //ListView lView;

    static Activity/*AppCompatActivity*/ thisActivity = null;
    public BluetoothConnection connection = null;
    BluetoothAdapter receivedBluetoothAdapter;
    static BluetoothDevice receivedBluetoothDevice;

    static boolean isX = false;
    static boolean isMinusX = false;

    static boolean isY = false;
    static boolean isMinusY = false;

    static boolean isZ = false;
    static boolean isMinusZ = false;


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

                    //registerReceiver(mReceiver, filter);
                    //Toast.makeText(GraphView.getContext(), "Searching for devices", Toast.LENGTH_LONG).show();
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
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Relative humidity [%]");
        graphView.getGridLabelRenderer().setVerticalAxisTitleColor(Color.WHITE);

        graphView.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);

        graphView.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setVerticalLabelsVisible(true);

        graphView.getGridLabelRenderer().reloadStyles();
        //graphView.getGridLabelRenderer().draw();

        graphView.setTitle("Relative humidity");
        graphView.setTitleColor(Color.WHITE);

        /*Series = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0,0)});
        Series.setTitle("Signal");
        Series.setColor(Color.RED);
        Series.setThickness(2);*/

        SeriesX = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesX.setTitle("X - magnetic flux");
        SeriesX.setColor(Color.RED);
        SeriesX.setThickness(2);
        /*SeriesX.setDrawDataPoints(true);
        SeriesX.setDataPointsRadius(10);*/

        SeriesY = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesY.setTitle("Y - magnetic flux");
        SeriesY.setColor(Color.BLUE);
        SeriesY.setThickness(2);

        SeriesZ = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesZ.setTitle("Z - magnetic flux");
        SeriesZ.setColor(Color.GREEN);
        SeriesZ.setThickness(2);

       /* graphView = new GraphView(this);
        graphView.setTitle("Graph");*/

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(Xview);

        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScalable(true);

        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-5);
        graphView.getViewport().setMaxY(5);

        // graphView.addSeries(Series);
        graphView.addSeries(SeriesX);
        graphView.addSeries(SeriesY);
        graphView.addSeries(SeriesZ);


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
    private static final Handler mHandler = new Handler(Looper.getMainLooper()) {
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

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //char[] readMessage1 = readMessage.toCharArray();

                    if(readMessage != null)
                    {

                        if(ileRazyX < 100)
                        {
                            if(readMessage.equals("x"))
                            {
                                isX = true;
                            }

                            if(isX)
                            {
                                /*if(readMessage.equals("-"))
                                {
                                    isMinusX = true;
                                }*/

                                if(isFloatNumber(readMessage))
                                {
                                    //double d = (double)Integer.parseInt(readMessage);
                                    double d = parseFloat(readMessage);
                                    if(-0.00 == d) d = 0.00;

                                    /*if(isMinusX)
                                    {
                                        d = d * (-1);
                                        valuesX[ileRazyX] = new DataPoint(graph2LastXValueX, d);
                                    }
                                    else
                                    {
                                        valuesX[ileRazyX] = new DataPoint(graph2LastXValueX, d);
                                    }*/
                                    //graph2LastXValueX += 0.5;

                                    valuesX[ileRazyX] = new DataPoint(graph2LastXValueX, d);
                                    SeriesX.appendData(valuesX[ileRazyX],AutoScrollX,101);

                                    if (graph2LastXValueX >= Xview && Lock == true){
                                        SeriesX.resetData(new DataPoint[] {});
                                        graph2LastXValueX = 0;
                                    }
                                    else graph2LastXValueX += 0.04;

                                    if(Lock == true){
                                        graphView.getViewport().setMinX(0);
                                        graphView.getViewport().setMaxX(Xview);
                                    }
                                    else
                                    {
                                        graphView.getViewport().setMinX(graph2LastXValueX-Xview);
                                        graphView.getViewport().setMaxX(Xview);
                                    }

                                    /*GraphView.removeView(graphView);
                                    GraphView.addView(graphView);

                                    try {
                                        Thread.sleep(2);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }*/


                                    ileRazyX++;

                                    isX = false;
                                    isMinusX = false;

                                }
                            }
                        }else
                        {
                            ileRazyX = 0;
                            /*graph2LastXValueX = 0;

                            SeriesX.resetData(valuesX);*/

                            GraphView.removeView(graphView);
                            GraphView.addView(graphView);
                        }

                        if(ileRazyY < 100)
                        {
                            if(readMessage.equals("y"))
                            {
                                isY = true;
                            }

                            if(isY)
                            {
                                /*if(readMessage.equals("-"))
                                {
                                    isMinusY = true;
                                }*/

                                if(isFloatNumber(readMessage))
                                {
                                    //double d = (double)Integer.parseInt(readMessage);
                                    double d = parseFloat(readMessage);
                                    if(-0.00 == d) d = 0.00;
                                    /*if(isMinusY)
                                    {
                                        d = d * (-1);
                                        valuesY[ileRazyY] = new DataPoint(graph2LastXValueY, d);
                                    }
                                    else
                                    {
                                        valuesY[ileRazyY] = new DataPoint(graph2LastXValueY, d);
                                    }*/

                                    valuesY[ileRazyY] = new DataPoint(graph2LastXValueY, d);
                                    SeriesY.appendData(valuesY[ileRazyY],AutoScrollX,101);

                                    if (graph2LastXValueY >= Xview && Lock == true){
                                        SeriesY.resetData(new DataPoint[] {});
                                        graph2LastXValueY = 0;
                                    }
                                    else graph2LastXValueY += 0.04;

                                    if(Lock == true){
                                        graphView.getViewport().setMinX(0);
                                        graphView.getViewport().setMaxX(Xview);
                                    }
                                    else
                                    {
                                        graphView.getViewport().setMinX(graph2LastXValueY-Xview);
                                        graphView.getViewport().setMaxX(Xview);
                                    }

                                    /*GraphView.removeView(graphView);
                                    GraphView.addView(graphView);*/


                                    ileRazyY++;

                                    isY = false;
                                    isMinusY = false;

                                    /*try {
                                        Thread.sleep(4);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }*/


                                }
                            }
                        }
                        else
                        {
                            ileRazyY = 0;

                            /*GraphView.removeView(graphView);
                            GraphView.addView(graphView);*/
                        }

                        if(ileRazyZ < 100)
                        {
                            if(readMessage.equals("z"))
                            {
                                isZ = true;
                            }

                            if(isZ)
                            {
                                /*if(readMessage.equals("-"))
                                {
                                    isMinusZ = true;
                                }*/

                                if(isFloatNumber(readMessage))
                                {
                                    //double d = (double)Integer.parseInt(readMessage);
                                    double d = parseFloat(readMessage);
                                    if(-0.00 == d) d = 0.00;


                                    /*if(isMinusZ)
                                    {
                                        d = d * (-1);
                                        valuesZ[ileRazyZ] = new DataPoint(graph2LastXValueZ, d);
                                    }
                                    else
                                    {
                                        valuesZ[ileRazyZ] = new DataPoint(graph2LastXValueZ, d);
                                    }*/

                                    valuesZ[ileRazyZ] = new DataPoint(graph2LastXValueZ, d);
                                    SeriesZ.appendData(valuesZ[ileRazyZ],AutoScrollX,101);

                                    if (graph2LastXValueZ >= Xview && Lock == true){
                                        SeriesZ.resetData(new DataPoint[] {});
                                        graph2LastXValueZ = 0;
                                    }
                                    else graph2LastXValueZ += 0.04;

                                    if(Lock == true){
                                        graphView.getViewport().setMinX(0);
                                        graphView.getViewport().setMaxX(Xview);
                                    }
                                    else
                                    {
                                        graphView.getViewport().setMinX(graph2LastXValueZ-Xview);
                                        graphView.getViewport().setMaxX(Xview);
                                    }


                                    ileRazyZ++;

                                    isZ = false;
                                    isMinusZ = false;



                                    /*GraphView.removeView(graphView);
                                    GraphView.addView(graphView);

                                    try {
                                        Thread.sleep(4);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }*/

                                }
                            }
                        }
                        else
                        {
                            ileRazyZ = 0;

                            //GraphView.removeView(graphView);
                            //GraphView.addView(graphView);
                        }

                        try
                        {
                            Thread.sleep(4);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        GraphView.removeView(graphView);
                        GraphView.addView(graphView);
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

                    SeriesX.resetData(new DataPoint[]{});
                    SeriesY.resetData(new DataPoint[]{});
                    SeriesZ.resetData(new DataPoint[]{});

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
                    connection.write("Magnetometer".getBytes());
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
            builder.setTitle("Choose device");

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