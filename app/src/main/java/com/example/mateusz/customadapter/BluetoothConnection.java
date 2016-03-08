package com.example.mateusz.customadapter;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by Mateusz on 2015-12-16.
 */
public class BluetoothConnection{

    private static final String TAG = "BluetoothConnection";
    private static final int DELAY = 0;
    public ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;
    public int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int MESSAGE_READ = 2;
    //public Handler mHandler = null;
    static Handler mHandler = new Handler();
    public BluetoothAdapter bluetoothAdapter = null;
    public BluetoothDevice connectedDevice = null;
    private String chosen_sensor;

    static List<String> readChars = new ArrayList<>();



    public BluetoothConnection(Handler tmpHandler, BluetoothAdapter tmpBluetoothAdapter, BluetoothDevice tmpDevice)
    {
        mHandler = tmpHandler;
        bluetoothAdapter = tmpBluetoothAdapter;

        connectedDevice = tmpDevice;
    }

    public BluetoothConnection(Handler tmpHandler, BluetoothAdapter tmpBluetoothAdapter, BluetoothDevice tmpDevice, String chosen_sensor)
    {
        mHandler = tmpHandler;
        bluetoothAdapter = tmpBluetoothAdapter;

        connectedDevice = tmpDevice;

        this.chosen_sensor = chosen_sensor;
    }

    public void disconnect(){
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothConnection.this.start();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothConnection.this.start();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
    //* @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        //Check if you have chosen a device
        /*if(chosen_sensor == null)
        {
            mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NO_CHOICE).sendToTarget();
        }*/


        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        if (mConnectThread.isSocketEmpty())
        {
            mHandler.obtainMessage(Constants.MESSAGE_SOCKET_ERROR).sendToTarget();
        }
        else
        {
            mConnectThread.start();
            setState(STATE_CONNECTING);
        }

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        //Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        // Start the thread to manage the connection and perform transmissions
        setState(STATE_CONNECTED);
        mConnectedThread = new ConnectedThread(socket);

        if(mConnectedThread != null)
        {
            mConnectedThread.start();
            mConnectedThread.setPriority(Thread.MIN_PRIORITY);
        }

    }



    private class ConnectThread extends Thread
    {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        private final long WAITING_TIME = 10000;
        private final long WAITING_INTERVAL = 1000;

        boolean isOk = true;


        ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket temporarySocket = null;

            mmDevice = device;
            try {
                temporarySocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = temporarySocket;
        }


        public boolean isSocketEmpty()
        {
            if(mmSocket == null)
            {
                return true;
            }
            return false;
        }

        public void run()
        {
            if(bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.cancelDiscovery();
            }

            try
            {
                //manageConnectedSocket(mmSocket);
                if (mmSocket != null)
                {
                    //CONNECTING TO THE DEVICE
                    mmSocket.connect();
                }

            }
            catch (IOException e) {
               // e.printStackTrace();
                try
                {
                    mmSocket.close();

                    mHandler.obtainMessage(Constants.MESSAGE_BLUETOOTH_DEVICE_UNAVAILABLE).sendToTarget();

                }catch (IOException e2)
                {
                    Log.e(TAG, "unable to close() " + mmSocket +
                            " socket during connection failure", e2);


                    mHandler.obtainMessage(Constants.MESSAGE_CLOSE_SOCKET_ERROR).sendToTarget();

                }
                isOk = false;
            }


            synchronized (BluetoothConnection.this)
            {
                mConnectThread = null;
            }

            if(isOk)
            {
                connected(mmSocket, mmDevice);
            }

        }

        /*private void manageConnectedSocket(BluetoothSocket socket) throws IOException {
            socket.connect();
        }*/

        public void cancel()
        {
            try
            {
                if (mmSocket != null)
                {
                    mmSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }





    }

    public class ConnectedThread extends Thread
    {
        BluetoothSocket connectedSocket;
        InputStream inStream;
        OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            connectedSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;



            try
            {

                if (connectedSocket != null)
                {
                    tmpIn = connectedSocket.getInputStream();
                    tmpOut = connectedSocket.getOutputStream();
                    mHandler.obtainMessage(Constants.MESSAGE_DEVICE_CONNECTED_SUCCESSFULLY).sendToTarget();
                   // Toast.makeText(activityContext, "connectedSocket != null", Toast.LENGTH_LONG);
                }
            }
            catch (IOException e) {
                mHandler.obtainMessage(Constants.MESSAGE_INPUT_OUTPUT_STREAM_UNAVAILABLE).sendToTarget();
                e.printStackTrace();
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run()
        {

            if(bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.cancelDiscovery();
            }

            byte[] buffer = new byte[4096];
            int bytes;

            String dupa;

            while (true)
            {
                try {
                    if (inStream != null)
                    {
                        bytes = inStream.available();

                        if(bytes > 0/* && bytes <= 200*/)
                        {
                            byte[] pocketBytes = new byte[bytes];
                            inStream.read(pocketBytes);
                            //Log.d(TAG, "setState() " + mState + " -> " + state);
                            System.out.println(pocketBytes.toString());
                            dupa = pocketBytes.toString();
                            System.out.println(dupa);

                            /*if(!dupa.contains("]"))
                            {
                                readChars.add(dupa);
                            }
                            else
                            {
                                readChars.add(dupa);

                                String m = "";
                                Iterator<String> iter = readChars.iterator();
                                StringBuilder sb = new StringBuilder();

                                if (iter.hasNext()) {
                                    sb.append(iter.next());
                                    while (iter.hasNext()) {
                                        sb.append("").append(iter.next());
                                    }
                                }
                                m = sb.toString();
                            }*/

                            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, pocketBytes).sendToTarget();

                            try
                            {
                                Thread.sleep(DELAY);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    mHandler.obtainMessage(Constants.MESSAGE_REMOTE_DEV_DISCONNECTED).sendToTarget();
                    break;
                }

                /*try
                {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                connectedSocket.close();
            } catch (IOException e) { }
        }
    }
}


