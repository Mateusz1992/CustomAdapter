package com.example.mateusz.customadapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.*;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;


public class LSM9DS1_sensor extends Activity implements View.OnClickListener {

    //toggle Button
    static boolean Lock;//whether lock the x-axis to 0-5
    static boolean AutoScrollX;//auto scroll to the last x value
    static boolean Stream;//Start or stop streaming
    static int ileRazy = 0;

    static int ileRazyX = 0;
    static int ileRazyY = 0;
    static int ileRazyZ = 0;


    static DataPoint[] values = new DataPoint[100];
    static DataPoint[] valuesX = new DataPoint[100];
    static DataPoint[] valuesY = new DataPoint[100];
    static DataPoint[] valuesZ = new DataPoint[100];

    //Button init
    Button bXminus;
    Button bXplus;
    ToggleButton tbLock;
    ToggleButton tbScroll;
    ToggleButton tbStream;

    //GraphView init
    static LinearLayout GraphView;
    static LinearLayout RightLayout;
    static GraphView graphView;
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

    static Activity/*AppCompatActivity*/ thisActivity = null;
    public BluetoothConnection connection = null;
    BluetoothAdapter receivedBluetoothAdapter;
    BluetoothDevice receivedBluetoothDevice;
    String moja;

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
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_lsm9_ds1_sensor);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//Hide title
        this.getWindow().setFlags(WindowManager.LayoutParams.
                FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//Hide Status bar
        setContentView(R.layout.activity_lsm9_ds1_sensor);

        RightLayout = (LinearLayout)findViewById(R.id.LL2);
        RightLayout.setBackgroundColor(Color.BLACK);
        init();
        ButtonInit();

        thisActivity = this;

        receivedBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        receivedBluetoothDevice = getIntent().getExtras().getParcelable("device");

    }


    public void init()
    {
        //init graphview
        GraphView = (LinearLayout) findViewById(R.id.Graph);
        //GraphView.setBackgroundColor(Color.BLACK);

        GraphView.setBackgroundColor(Color.BLACK);

        graphView = new GraphView(this);//(GraphView)findViewById(R.id.graph);

        graphView.setDrawingCacheEnabled(true);
        graphView.setBackgroundColor(Color.BLACK);


        graphView.getGridLabelRenderer().setGridColor(Color.WHITE);
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Acceleration");
        graphView.getGridLabelRenderer().setVerticalAxisTitleColor(Color.WHITE);

        graphView.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);

        graphView.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graphView.getGridLabelRenderer().setVerticalLabelsVisible(true);

        graphView.getGridLabelRenderer().reloadStyles();
        //graphView.getGridLabelRenderer().draw();

        graphView.setTitle("Graph - acceleration in 3D: x, y, z");
        graphView.setTitleColor(Color.WHITE);

        /*Series = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0,0)});
        Series.setTitle("Signal");
        Series.setColor(Color.RED);
        Series.setThickness(2);*/

        SeriesX = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0,0)});
        SeriesX.setTitle("X accel");
        SeriesX.setColor(Color.RED);
        SeriesX.setThickness(2);

        SeriesY = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0,0)});
        SeriesY.setTitle("Y accel");
        SeriesY.setColor(Color.BLUE);
        SeriesY.setThickness(2);

        SeriesZ = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0,0)});
        SeriesZ.setTitle("Z accel");
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


        GraphView.addView(graphView);
        //graphView.getViewport()

    }


    void ButtonInit(){
        bConnect = (Button)findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);
        bDisconnect = (Button)findViewById(R.id.bDisconnect);
        bDisconnect.setOnClickListener(this);
        //X-axis control button
        bXminus = (Button)findViewById(R.id.bXminus);
        bXminus.setOnClickListener(this);
        bXplus = (Button)findViewById(R.id.bXplus);
        bXplus.setOnClickListener(this);
        //
        tbLock = (ToggleButton)findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);
        tbScroll = (ToggleButton)findViewById(R.id.tbScroll);
        tbScroll.setOnClickListener(this);
        tbStream = (ToggleButton)findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);
        //init toggleButton
        Lock=true;
        AutoScrollX=true;
        Stream=true;
    }

    public void connectBt()
    {
        if (connection != null) {
            connection.stop();
            connection = null;
        }


        if(connection == null)
        {
            if ((receivedBluetoothDevice == null) || (receivedBluetoothAdapter == null))
            {
                finish();
            }

            connection = new BluetoothConnection(mHandler, receivedBluetoothAdapter, receivedBluetoothDevice);

            if(connection == null)
            {
                finish();
            }
            connection.connect(receivedBluetoothDevice);
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
        if(connection != null)
        {
            //Toast.makeText(this, "LSM9DS1_sensor", Toast.LENGTH_LONG).show();
            connection.stop();
        }
    }

    @Override
    protected void onPause() {
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
    private static final Handler h = new Handler(Looper.getMainLooper());
    private static final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //ChosenDevice activity = this.;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnection.STATE_CONNECTED:
                            Toast.makeText(thisActivity, "STATE_CONNECTED - handle", Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            Toast.makeText(thisActivity, "STATE_CONNECTING - handle", Toast.LENGTH_LONG).show();
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
                                if(readMessage.equals("-"))
                                {
                                    isMinusX = true;
                                }

                                if(isIntegerNumber(readMessage))
                                {
                                    double d = (double)Integer.parseInt(readMessage);
                                    if(isMinusX)
                                    {
                                        d = d * (-1);
                                        valuesX[ileRazyX] = new DataPoint(graph2LastXValueX, d);
                                    }
                                    else
                                    {
                                        valuesX[ileRazyX] = new DataPoint(graph2LastXValueX, d);
                                    }
                                    //graph2LastXValueX += 0.5;


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
                                if(readMessage.equals("-"))
                                {
                                    isMinusY = true;
                                }

                                if(isIntegerNumber(readMessage))
                                {
                                    double d = (double)Integer.parseInt(readMessage);
                                    if(isMinusY)
                                    {
                                        d = d * (-1);
                                        valuesY[ileRazyY] = new DataPoint(graph2LastXValueY, d);
                                    }
                                    else
                                    {
                                        valuesY[ileRazyY] = new DataPoint(graph2LastXValueY, d);
                                    }

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
                                if(readMessage.equals("-"))
                                {
                                    isMinusZ = true;
                                }

                                if(isIntegerNumber(readMessage))
                                {
                                    double d = (double)Integer.parseInt(readMessage);

                                    if(d == 0)
                                    {
                                        System.out.print("Jest zero");
                                    }

                                    if(isMinusZ)
                                    {
                                        d = d * (-1);
                                        valuesZ[ileRazyZ] = new DataPoint(graph2LastXValueZ, d);
                                    }
                                    else
                                    {
                                        valuesZ[ileRazyZ] = new DataPoint(graph2LastXValueZ, d);
                                    }

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

                        try {
                            Thread.sleep(4);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        GraphView.removeView(graphView);
                        GraphView.addView(graphView);
                    }

                    break;
                case Constants.MESSAGE_SOCKET_ERROR:
                    Toast.makeText(thisActivity, "MESSAGE_SOCKET_ERROR", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                case Constants.MESSAGE_CLOSE_SOCKET_ERROR:
                    Toast.makeText(thisActivity, "MESSAGE_CLOSE_SOCKET_ERROR", Toast.LENGTH_LONG).show();
                    thisActivity.finish();
                    break;
                case Constants.MESSAGE_BLUETOOTH_DEVICE_UNAVAILABLE:
                    Toast.makeText(thisActivity, "Bluetooth device unavailable", Toast.LENGTH_LONG).show();
                    //chosenDevice = -1;
                    //textConnection.setText("Bluetooth device unavailable");
                    //thisActivity.finish();
                    break;
                case Constants.MESSAGE_DEVICE_CONNECTED_SUCCESSFULLY:
                    Toast.makeText(thisActivity, "Device connected successfully", Toast.LENGTH_LONG).show();

                   // connection.write(availableDevice.get(chosenDevice).getBytes());
                    //thisActivity.finish();
                    break;
                case Constants.MESSAGE_DEVICE_NO_CHOICE:
                    Toast.makeText(thisActivity, "You did not chosen a device", Toast.LENGTH_LONG).show();
                    break;
                case Constants.MESSAGE_INPUT_OUTPUT_STREAM_UNAVAILABLE:
                    Toast.makeText(thisActivity, "I/O stream unavailable", Toast.LENGTH_LONG).show();
                    break;
                case Constants.MESSAGE_REMOTE_DEV_DISCONNECTED:
                    Toast.makeText(thisActivity, "Remote device disconnected", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

public static boolean isIntegerNumber(String s)
{
    try{
        double x = (double)Integer.parseInt(s);
    }catch(NumberFormatException e)
    {
        return false;
    }
    return true;
}

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bConnect:
                connectBt();
                break;
            case R.id.bDisconnect:
                if(connection != null)
                {
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
                        connection.write("LSM9DS1".getBytes());
                //}
                /*else{
                    if (connection != null)
                        connection.write("Q".getBytes());*/
                //}
                break;
        }
    }
}
