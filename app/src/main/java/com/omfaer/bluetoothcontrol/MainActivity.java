package com.omfaer.bluetoothcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    Button btnConnection;
    Button btnPairDevices;
    ListView lwPairedList;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnConnection = findViewById(R.id.btnConnection);
        btnPairDevices = findViewById(R.id.btnPairDevices);
        lwPairedList = findViewById(R.id.lwPairedList);

        btnConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toogleBluetooth();
            }
        });

        btnPairDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listDevices();
            }
        });
    }

    private void listDevices() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if(pairedDevices.size() > 0){
            for(BluetoothDevice bt: pairedDevices){
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "Eşleşmiş Cihaz Yok", Toast.LENGTH_SHORT).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        lwPairedList.setAdapter(adapter);
        lwPairedList.setOnItemClickListener(selectDevice);

    }

    public AdapterView.OnItemClickListener selectDevice = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length()-17);

            Intent comIntent = new Intent(MainActivity.this,CommunicationActivity.class);
            comIntent.putExtra(EXTRA_ADDRESS, address);
            startActivity(comIntent);
        }
    };

    private void toogleBluetooth() {
        if(mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "Bluetooth Cihazı Bulunamadı", Toast.LENGTH_SHORT).show();
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            btnConnection.setText("Bluetoothu Kapat");
        }

        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
            btnConnection.setText("Bluetoothu Aç");
        }



    }
}