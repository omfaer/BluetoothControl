package com.omfaer.bluetoothcontrol;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class CommunicationActivity extends AppCompatActivity {

    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BluetoothControl";
    private boolean isBluetoothConnected = false;

    Button btnGetData;
    Button btnClear;
    TextView tvReceivedData;
    String address = null;

    private ProgressDialog progressDialog;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket mBluetoothSocket = null;

    InputStream mInputStream;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        Intent getAddressIntent = getIntent();
        address = getAddressIntent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        tvReceivedData = findViewById(R.id.tvReceivedData);

        btnGetData = findViewById(R.id.btnGetData);
        btnClear = findViewById(R.id.btnClear);

        new BTConnectAsync().execute();


        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getData();
                } catch (IOException e) {
                    e.printStackTrace();
                    tvReceivedData.setText("Error");
                }
            }
            });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvReceivedData.setText(" ");
            }
        });

    }


    void getData() throws IOException {
        String msg = "1";
        msg += "\n";
        mBluetoothSocket.getOutputStream().write(msg.toString().getBytes());
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                           tvReceivedData.setText(data);
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    private void Disconnect(){
        if (mBluetoothSocket != null){
            try {
                stopWorker = true;
                mBluetoothSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Disconnect();
    }

    private class BTConnectAsync extends AsyncTask<Void,Void,Void> {
        private boolean ConnectSuccess = true;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CommunicationActivity.this, "Bağlanıyor..", "Lütfen bekleyin");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (mBluetoothSocket == null || !isBluetoothConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBluetoothSocket.connect();
                    mInputStream = mBluetoothSocket.getInputStream();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Bağlantı Hatası Tekrar Deneyin", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Bağlantı Başarılı", Toast.LENGTH_SHORT).show();
                isBluetoothConnected = true;
                beginListenForData();
            }
            progressDialog.dismiss();
        }

    }
}



