package com.seniordesign.newsmartvoltmeter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.View;


































































































import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.Viewport;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {

    Button save;
    TextView txtString, txtStringLength, current, average, max, min;
    Handler bluetoothIn;

    private int graph2LastXValue = 0;

    private LineGraphSeries<DataPoint> voltage;

    File myExternalFile;
    private static final String FILENAME = "Voltages.txt";

    final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    final String v = " V";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Link the buttons and textViews to respective views
        save = (Button) findViewById(R.id.Save);
        txtString = (TextView) findViewById(R.id.seperator);
        txtStringLength = (TextView) findViewById(R.id.textView);
        current = (TextView) findViewById(R.id.current);
        average = (TextView) findViewById(R.id.average);
        max = (TextView) findViewById(R.id.max);
        min = (TextView) findViewById(R.id.min);

        GraphView volts = (GraphView) findViewById(R.id.voltage_graph);

        voltage = new LineGraphSeries<DataPoint>();

        volts.addSeries(voltage);

        Viewport viewport = volts.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(-10);
        viewport.setMaxY(10);
        viewport.setScrollable(true);
        viewport.setScalable(true);

        volts.getViewport().setScalable(true);


        volts.setBackgroundColor(getResources().getColor(android.R.color.background_light));

        volts.setTitle("Waveform");
        volts.setTitleTextSize(50);
      //  volts.setTitleColor(Color.parseColor("#e3b018"));

        volts.getGridLabelRenderer().setVerticalAxisTitle("Voltage");
        //volts.getGridLabelRenderer().setVerticalAxisTitleColor(Color.parseColor("#e3b018"));
        volts.getGridLabelRenderer().setNumVerticalLabels(10);

        volts.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        //volts.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.parseColor("#e3b018"));



        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        //txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();                          //get length of data received
                        //txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
                        {
                            if (dataInPrint.length() == 13) {
                                String mCurrent = recDataString.substring(1, 5);             //get voltage value from string between indices 1-5
                                String mAvg = recDataString.substring(6, 10);            //same again...
                                String test = recDataString.substring(11, 13);

                                current.setTextColor(Color.parseColor("#e3b018"));
                                current.setTypeface(null, Typeface.NORMAL);
                                current.setText("Peak " + test + " Voltage: " + mCurrent + v);    //update the textviews with voltage values
                                average.setText("Root Mean Square "+ test + " Voltage: " + mAvg + v);
                                min.setText("");
                                max.setText("");

                                voltage.appendData(new DataPoint(graph2LastXValue++, Double.parseDouble(mCurrent)),true, 10);

                            }
                            if (dataInPrint.length() == 23) {
                                String mCurrent = recDataString.substring(1, 5);            //get voltage value from string between indices 1-5
                                String mAvg = recDataString.substring(6, 10);            //same again...
                                String mMax = recDataString.substring(11, 15);
                                String mMin = recDataString.substring(16, 20);
                                String test = recDataString.substring(21, 23);


                                current.setTextColor(Color.parseColor("#e3b018"));
                                current.setTypeface(null, Typeface.NORMAL);
                                current.setText("Current " + test + " Voltage: " + mCurrent + v);    //update the textviews with voltage values
                                average.setText("Average " + test + " Voltage: " + mAvg + v);
                                max.setText("Maximum " + test + " Voltage: " + mMax + v);
                                min.setText("Minimum " + test + " Voltage: " + mMin + v);

                                voltage.appendData(new DataPoint(graph2LastXValue++, Double.parseDouble(mCurrent)),true, 10);

                            }

                            if (dataInPrint.length() == 27) {
                                String mCurrent = recDataString.substring(1, 6);             //get voltage value from string between indices 1-6
                                String mAvg = recDataString.substring(7, 12);            //same again...
                                String mMax = recDataString.substring(13, 18);
                                String mMin = recDataString.substring(19, 24);
                                String test = recDataString.substring(25, 27);


                                current.setText("Current " + test + " Voltage: " + mCurrent + v);   //update the textviews with voltage values
                                average.setText("Average " + test + " Voltage: " + mAvg + v);
                                max.setText("Maximum " + test + " Voltage: " + mMax + v);
                                min.setText("Minimum " + test + " Voltage: " + mMin + v);

                                voltage.appendData(new DataPoint(graph2LastXValue++, Double.parseDouble(mCurrent)),true, 10);
                            }

                            if (dataInPrint.length() == 31) {
                                String mCurrent = recDataString.substring(1, 7);             //get voltage value from string between indices 1-7
                                String mAvg = recDataString.substring(8, 14);            //same again...
                                String mMax = recDataString.substring(15, 21);
                                String mMin = recDataString.substring(22, 28);
                                String test = recDataString.substring(29, 31);


                                current.setTextColor(Color.parseColor("#e3b018"));
                                current.setTypeface(null, Typeface.NORMAL);
                                current.setText("Current " + test + " Voltage: " + mCurrent + v);    //update the textviews with voltage values
                                average.setText("Average " + test + " Voltage: " + mAvg + v);
                                max.setText("Maximum " + test + " Voltage: " + mMax + v);
                                min.setText("Minimum " + test + " Voltage: " + mMin + v);

                                voltage.appendData(new DataPoint(graph2LastXValue++, Double.parseDouble(mCurrent)),true, 10);
                            }


                            /*
                            else if (recDataString.charAt(0) == 'M')                             //if it starts with # we know it is what we are looking for
                            {
                                current.setTextColor(Color.RED);
                                current.setText("Warning! Check Voltmeter Switches and Status");
                                average.setText("");
                                max.setText("");
                                min.setText("");
                            }
                            else if (recDataString.charAt(0) == 'O')                             //if it starts with # we know it is what we are looking for
                            {
                                current.setTextColor(Color.YELLOW);
                                current.setText("Open Loop");
                                average.setText("");
                                max.setText("");
                                min.setText("");
                            }
                            else if (recDataString.charAt(0) == 'P')                             //if it starts with # we know it is what we are looking for
                            {
                                current.setTextColor(Color.GREEN);
                                current.setText("Please Select Range on Voltmeter");
                                average.setText("");
                                max.setText("");
                                min.setText("");
                            }
                            */

                            recDataString.delete(0, recDataString.length());                    //clear all string data
                            // strIncom =" ";
                            dataInPrint = " ";
                        }


                    }
                }
            }

        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        verifyStoragePermissions(this);     //get external storage permissions

        if(!isExternalStorageAvailable()|| isExternalStorageReadOnly()){
            save.setEnabled(false);
        }
/*
        screenshot.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

                try {
                    // image naming and path  to include sd card  appending name you choose for file
                    String mPath = Environment.getExternalStorageDirectory().toString() + "/PICTURES/Screenshots/" + now + ".jpg";

                    // create bitmap screen capture
                    View v1 = getWindow().getDecorView().getRootView();
                    v1.setDrawingCacheEnabled(true);
                    Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                    v1.setDrawingCacheEnabled(false);

                    File imageFile = new File(mPath);

                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    outputStream.flush();
                    outputStream.close();

                } catch (Throwable e) {
                    // Several error may come out with file handling or OOM
                    e.printStackTrace();
                }
            }
        });
*/

    //Create text file on save button press, output whatever is filling textboxes
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    myExternalFile = new File(Environment.getExternalStorageDirectory(), FILENAME);

                    FileOutputStream fos = new FileOutputStream(myExternalFile);

                    fos.write(current.getText().toString().getBytes());

                    fos.write("\n".getBytes());

                    fos.write(average.getText().toString().getBytes());

                    fos.write("\n".getBytes());

                    fos.write(max.getText().toString().getBytes());

                    fos.write("\n".getBytes());

                    fos.write(min.getText().toString().getBytes());

                    fos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });



    }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {

            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }



}