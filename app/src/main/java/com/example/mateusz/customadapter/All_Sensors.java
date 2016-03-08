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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringBufferInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



public class All_Sensors  extends Fragment implements View.OnClickListener {

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

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }
    }

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

    static int ileRazyXG = 0;
    static int ileRazyYG = 0;
    static int ileRazyZG = 0;

    static int ileRazyXM = 0;
    static int ileRazyYM = 0;
    static int ileRazyZM = 0;

    static int ileRazyT = 0;
    static int ileRazyH = 0;

    //AlertDialog.Builder builder;

    int huj = 0;

    //private static Handler mHandler;

    static DataPoint[] values = new DataPoint[100];
    static DataPoint[] valuesX = new DataPoint[1001];
    static DataPoint[] valuesY = new DataPoint[1001];
    static DataPoint[] valuesZ = new DataPoint[1001];


    static DataPoint[] valuesXG = new DataPoint[1001];
    static DataPoint[] valuesYG = new DataPoint[1001];
    static DataPoint[] valuesZG = new DataPoint[1001];

    static DataPoint[] valuesXM = new DataPoint[1001];
    static DataPoint[] valuesYM = new DataPoint[1001];
    static DataPoint[] valuesZM = new DataPoint[1001];

    static DataPoint[] valuesT = new DataPoint[1001];
    static DataPoint[] valuesH = new DataPoint[1001];

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

    static LineGraphSeries SeriesXAccel;
    static LineGraphSeries SeriesYAccel;
    static LineGraphSeries SeriesZAccel;

    static LineGraphSeries SeriesXGyro;
    static LineGraphSeries SeriesYGyro;
    static LineGraphSeries SeriesZGyro;

    static LineGraphSeries SeriesXMagn;
    static LineGraphSeries SeriesYMagn;
    static LineGraphSeries SeriesZMagn;

    static LineGraphSeries SeriesT;
    static LineGraphSeries SeriesH;
    //graph value
    private static double graph2LastXValue = 0;
    private static double graph2LastXValueX = 0;
    private static double graph2LastXValueY = 0;
    private static double graph2LastXValueZ = 0;

    private static double graph2LastXValueXG = 0;
    private static double graph2LastXValueYG = 0;
    private static double graph2LastXValueZG = 0;

    private static double graph2LastXValueXM = 0;
    private static double graph2LastXValueYM = 0;
    private static double graph2LastXValueZM = 0;

    private static double graph2LastXValueT = 0;
    private static double graph2LastXValueH = 0;

    private static int Xview=10;
    Button bConnect, bDisconnect;

    //ListView lView;

    static Activity/*AppCompatActivity*/ thisActivity = null;
    public BluetoothConnection connection = null;
    BluetoothAdapter receivedBluetoothAdapter;
    static BluetoothDevice receivedBluetoothDevice;

    private final MyHandler mHandler = new MyHandler(this);


    static final int DELAY = 0;
    static final double STEP = 0.1;
    static final int MAX_X = 10;
    static boolean isX = false;
    static boolean isMinusX = false;

    static boolean isY = false;
    static boolean isMinusY = false;

    static boolean isZ = false;
    static boolean isMinusZ = false;

    static boolean isA = false;
    static boolean isG = false;
    static boolean isM = false;

    static boolean canI = true;


    public void resetVariables()
    {
        //reset x coordinates of plots
        graph2LastXValueX = 0;
        graph2LastXValueY = 0;
        graph2LastXValueZ = 0;

        graph2LastXValueXG = 0;
        graph2LastXValueYG = 0;
        graph2LastXValueZG = 0;

        graph2LastXValueXM = 0;
        graph2LastXValueYM = 0;
        graph2LastXValueZM = 0;

        graph2LastXValueT = 0;
        graph2LastXValueH = 0;
        //reset x coordinates of plots

        //reset array indices
        ileRazyX = 0;
        ileRazyY = 0;
        ileRazyZ = 0;

        ileRazyXG = 0;
        ileRazyYG = 0;
        ileRazyZG = 0;

        ileRazyXM = 0;
        ileRazyYM = 0;
        ileRazyZM = 0;

        ileRazyT = 0;
        ileRazyH = 0;
        //reset array indices
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

        graphView = /*new GraphView(this);*/(GraphView)rootView.findViewById(R.id.graph);

        graphView.setDrawingCacheEnabled(true);
        graphView.setBackgroundColor(Color.BLACK);


        graphView.getGridLabelRenderer().setGridColor(Color.WHITE);
        graphView.getGridLabelRenderer().setVerticalAxisTitle("All sensors");
        graphView.getGridLabelRenderer().setVerticalAxisTitleColor(Color.WHITE);

        graphView.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);

        graphView.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setVerticalLabelsVisible(true);

        graphView.getGridLabelRenderer().reloadStyles();
        //graphView.getGridLabelRenderer().draw();

        graphView.setTitle("All sensors");
        graphView.setTitleColor(Color.WHITE);

        SeriesXAccel = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesXAccel.setTitle("X accel");
        SeriesXAccel.setColor(Color.RED);
        SeriesXAccel.setThickness(2);

        SeriesYAccel = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesYAccel.setTitle("Y accel");
        SeriesYAccel.setColor(Color.BLUE);
        SeriesYAccel.setThickness(2);

        SeriesZAccel = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesZAccel.setTitle("Z accel");
        SeriesZAccel.setColor(Color.GREEN);
        SeriesZAccel.setThickness(2);

        SeriesXGyro = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesXGyro.setTitle("Gyro X");
        SeriesXGyro.setColor(Color.CYAN);
        SeriesXGyro.setThickness(2);

        SeriesYGyro = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesYGyro.setTitle("Gyro Y");
        SeriesYGyro.setColor(Color.YELLOW);
        SeriesYGyro.setThickness(2);

        SeriesZGyro = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesZGyro.setTitle("Gyro Z");
        SeriesZGyro.setColor(Color.GRAY);
        SeriesZGyro.setThickness(2);

        SeriesXMagn = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesXMagn.setTitle("Mag X");
        SeriesXMagn.setColor(Color.argb(255, 255, 177, 20));
        SeriesXMagn.setThickness(2);

        SeriesYMagn = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesYMagn.setTitle("Mag Y");
        SeriesYMagn.setColor(Color.argb(255, 214, 55, 236));
        SeriesYMagn.setThickness(2);

        SeriesZMagn = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesZMagn.setTitle("Mag Z");
        SeriesZMagn.setColor(Color.argb(255, 60, 89, 232));
        SeriesZMagn.setThickness(2);

        SeriesT = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesT.setTitle("Temperature");
        SeriesT.setColor(Color.argb(255, 102, 153, 102));
        SeriesT.setThickness(2);

        SeriesH = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0, 0)});
        SeriesH.setTitle("Humidity");
        SeriesH.setColor(Color.argb(255, 153, 26, 0));
        SeriesH.setThickness(2);


        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(Xview);

        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScalable(true);

        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-100);
        graphView.getViewport().setMaxY(100);

        //graphView.getViewport().setScalable(true);

        // graphView.addSeries(Series);
        graphView.addSeries(SeriesXAccel);
        graphView.addSeries(SeriesYAccel);
        graphView.addSeries(SeriesZAccel);


        graphView.addSeries(SeriesXGyro);
        graphView.addSeries(SeriesYGyro);
        graphView.addSeries(SeriesZGyro);

        graphView.addSeries(SeriesXMagn);
        graphView.addSeries(SeriesYMagn);
        graphView.addSeries(SeriesZMagn);

        graphView.addSeries(SeriesT);
        graphView.addSeries(SeriesH);
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
    /*private Handler mHandler = new Handler(Looper.getMainLooper())*/
    private static class MyHandler extends Handler
    {
        private final WeakReference<All_Sensors> fragment;

        public MyHandler(All_Sensors f) {
            fragment = new WeakReference<All_Sensors>(f);
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

                            (new Thread(new Runnable()
                            {
                                @Override
                                public void run() {

                                try
                                {
                                    JSONObject jsonRootObject = new JSONObject(m);

                                    //Get the instance of JSONArray that contains JSONObjects
                                    JSONArray jsonArray = jsonRootObject.optJSONArray("m");

                                    if(!mrs.isEmpty())
                                    {
                                        mrs.clear();
                                    }

                                    if(jsonArray != null)
                                    {
                                        for(int i=0; i < jsonArray.length(); i++){
                                            JSONObject jsonObject = jsonArray.getJSONObject(i);


                                            String id = jsonObject.optString("id").toString();
                                            if(!(id.equals("t") || id.equals("h")))
                                            {
                                                float x = Float.parseFloat(jsonObject.optString("x").toString());
                                                float y = Float.parseFloat(jsonObject.optString("y").toString());
                                                float z = Float.parseFloat(jsonObject.optString("z").toString());

                                                mrs.add(new Measurements(id,x,y,z));
                                            }else
                                            {
                                                //id = "";
                                                float x = Float.parseFloat(jsonObject.optString("x").toString());
                                                float y = 0;
                                                float z = 0;
                                                mrs.add(new Measurements(id,x,y,z));
                                            }
                                            //String name = jsonObject.optString("name").toString();
                                            //float salary = Float.parseFloat(jsonObject.optString("salary").toString());

                                          //  data += "Node"+i+" : \n id= "+ id +" \n Name= "+ name +" \n Salary= "+ salary +" \n ";
                                        }
                                        //output.setText(data);
                                    }
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }

                                    fragment.get().getActivity().runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run() {
                                            if (readMessage != null && !mrs.isEmpty()) {
                                                for (int i = 0; i < mrs.size(); i++) {
                                                    if (mrs.get(i).getId_mrs().equals("a")) {
                                                        if (ileRazyX < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesX[ileRazyX] = new DataPoint(graph2LastXValueX, mrs.get(i).getX() * 9.81);
                                                            SeriesXAccel.appendData(valuesX[ileRazyX], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueX >= Xview && Lock == true) {
                                                                SeriesXAccel.resetData(new DataPoint[]{});
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

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyX = 0;

                                                        /*GraphView.removeView(graphView);
                                                        GraphView.addView(graphView);*/
                                                        }

                                                        if (ileRazyY < ((int) (MAX_X / STEP) + 1)) {
                                                            valuesY[ileRazyY] = new DataPoint(graph2LastXValueY, mrs.get(i).getY() * 9.81);
                                                            SeriesYAccel.appendData(valuesY[ileRazyY], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueY >= Xview && Lock == true) {
                                                                SeriesYAccel.resetData(new DataPoint[]{});
                                                                graph2LastXValueY = 0;
                                                            } else graph2LastXValueY += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueY - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }

                                                            ileRazyY++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyY = 0;
                                                        }

                                                        if (ileRazyZ < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesZ[ileRazyZ] = new DataPoint(graph2LastXValueZ, mrs.get(i).getZ() * 9.81);
                                                            SeriesZAccel.appendData(valuesZ[ileRazyZ], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueZ >= Xview && Lock == true) {
                                                                SeriesZAccel.resetData(new DataPoint[]{});
                                                                graph2LastXValueZ = 0;
                                                            } else graph2LastXValueZ += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueZ - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }


                                                            ileRazyZ++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyZ = 0;

                                                            //GraphView.removeView(graphView);
                                                            //GraphView.addView(graphView);
                                                        }
                                                    } else if (mrs.get(i).getId_mrs().equals("g")) {
                                                        if (ileRazyXG < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesXG[ileRazyXG] = new DataPoint(graph2LastXValueXG, mrs.get(i).getX());
                                                            SeriesXGyro.appendData(valuesXG[ileRazyXG], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueXG >= Xview && Lock == true) {
                                                                SeriesXGyro.resetData(new DataPoint[]{});
                                                                graph2LastXValueXG = 0;
                                                            } else graph2LastXValueXG += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueXG - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }

                                                            ileRazyXG++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyXG = 0;

                                                        /*GraphView.removeView(graphView);
                                                        GraphView.addView(graphView);*/
                                                        }

                                                        if (ileRazyYG < ((int) (MAX_X / STEP) + 1)) {
                                                            valuesYG[ileRazyYG] = new DataPoint(graph2LastXValueYG, mrs.get(i).getY());
                                                            SeriesYGyro.appendData(valuesYG[ileRazyYG], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueYG >= Xview && Lock == true) {
                                                                SeriesYGyro.resetData(new DataPoint[]{});
                                                                graph2LastXValueYG = 0;
                                                            } else graph2LastXValueYG += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueYG - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }

                                                            ileRazyYG++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyYG = 0;
                                                        }

                                                        if (ileRazyZG < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesZG[ileRazyZG] = new DataPoint(graph2LastXValueZG, mrs.get(i).getZ());
                                                            SeriesZGyro.appendData(valuesZG[ileRazyZG], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueZG >= Xview && Lock == true) {
                                                                SeriesZGyro.resetData(new DataPoint[]{});
                                                                graph2LastXValueZG = 0;
                                                            } else graph2LastXValueZG += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueZG - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }


                                                            ileRazyZG++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyZG = 0;

                                                            //GraphView.removeView(graphView);
                                                            //GraphView.addView(graphView);
                                                        }
                                                    } else if (mrs.get(i).getId_mrs().equals("m")) {
                                                        if (ileRazyXM < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesXM[ileRazyXM] = new DataPoint(graph2LastXValueXM, mrs.get(i).getX());
                                                            SeriesXMagn.appendData(valuesXM[ileRazyXM], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueXM >= Xview && Lock == true) {
                                                                SeriesXMagn.resetData(new DataPoint[]{});
                                                                graph2LastXValueXM = 0;
                                                            } else graph2LastXValueXM += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueXM - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }

                                                            ileRazyXM++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyXM = 0;

                                                        /*GraphView.removeView(graphView);
                                                        GraphView.addView(graphView);*/
                                                        }

                                                        if (ileRazyYM < ((int) (MAX_X / STEP) + 1)) {
                                                            valuesYM[ileRazyYM] = new DataPoint(graph2LastXValueYM, mrs.get(i).getY());
                                                            SeriesYMagn.appendData(valuesYM[ileRazyYM], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueYM >= Xview && Lock == true) {
                                                                SeriesYMagn.resetData(new DataPoint[]{});
                                                                graph2LastXValueYM = 0;
                                                            } else graph2LastXValueYM += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueYM - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }

                                                            ileRazyYM++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyYM = 0;
                                                        }

                                                        if (ileRazyZM < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesZM[ileRazyZM] = new DataPoint(graph2LastXValueZM, mrs.get(i).getZ());
                                                            SeriesZMagn.appendData(valuesZM[ileRazyZM], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueZM >= Xview && Lock == true) {
                                                                SeriesZMagn.resetData(new DataPoint[]{});
                                                                graph2LastXValueZM = 0;
                                                            } else graph2LastXValueZM += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueZM - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }


                                                            ileRazyZM++;

                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyZM = 0;
                                                        }
                                                    } else if (mrs.get(i).getId_mrs().equals("t")) {
                                                        if (ileRazyT < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesT[ileRazyT] = new DataPoint(graph2LastXValueT, mrs.get(i).getX());
                                                            SeriesT.appendData(valuesT[ileRazyT], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueT >= Xview && Lock == true) {
                                                                SeriesT.resetData(new DataPoint[]{});
                                                                graph2LastXValueT = 0;
                                                            } else graph2LastXValueT += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueT - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }

                                                            ileRazyT++;


                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyT = 0;

                                                        /*GraphView.removeView(graphView);
                                                        GraphView.addView(graphView);*/
                                                        }
                                                    } else if (mrs.get(i).getId_mrs().equals("h")) {
                                                        if (ileRazyH < ((int) (MAX_X / STEP) + 1)) {

                                                            valuesH[ileRazyH] = new DataPoint(graph2LastXValueH, mrs.get(i).getX());
                                                            SeriesH.appendData(valuesH[ileRazyH], AutoScrollX, ((int) (MAX_X / STEP) + 1));

                                                            if (graph2LastXValueH >= Xview && Lock == true) {
                                                                SeriesH.resetData(new DataPoint[]{});
                                                                graph2LastXValueH = 0;
                                                            } else graph2LastXValueH += STEP;

                                                            if (Lock == true) {
                                                                graphView.getViewport().setMinX(0);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            } else {
                                                                graphView.getViewport().setMinX(graph2LastXValueH - Xview);
                                                                graphView.getViewport().setMaxX(Xview);
                                                            }
                                                            ileRazyH++;


                                                            try {
                                                                Thread.sleep(DELAY);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            /*GraphView.removeView(graphView);
                                                            GraphView.addView(graphView);*/
                                                        } else {
                                                            ileRazyH = 0;

                                                        /*GraphView.removeView(graphView);
                                                        GraphView.addView(graphView);*/
                                                        }
                                                    }
                                                }//

                                                try {
                                                    Thread.sleep(DELAY);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                GraphView.removeView(graphView);
                                                GraphView.addView(graphView);

                                                canI = true;
                                            } else {
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

                    SeriesXAccel.resetData(new DataPoint[]{});
                    SeriesYAccel.resetData(new DataPoint[]{});
                    SeriesZAccel.resetData(new DataPoint[]{});

                    SeriesXGyro.resetData(new DataPoint[]{});
                    SeriesYGyro.resetData(new DataPoint[]{});
                    SeriesZGyro.resetData(new DataPoint[]{});

                    SeriesXMagn.resetData(new DataPoint[]{});
                    SeriesYMagn.resetData(new DataPoint[]{});
                    SeriesZMagn.resetData(new DataPoint[]{});

                    SeriesT.resetData(new DataPoint[]{});
                    SeriesH.resetData(new DataPoint[]{});

                    resetVariables();

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
                    connection.write("All_sens".getBytes());
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
