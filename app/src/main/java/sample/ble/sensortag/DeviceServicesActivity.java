/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.ble.sensortag;

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import sample.ble.sensortag.adapters.TiServicesAdapter;
//import sample.ble.sensortag.sensor.TiSensor;
import sample.ble.sensortag.two.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BleService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceServicesActivity extends Activity {
    private final static String TAG = DeviceServicesActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView connectionState;
    private TextView dataField;
    private TextView UUIDText;
    private ImageView viewview;
    
    
    private Button btnon;
    private Button btnoff;
    private Button btnread;
    private Button pppppp;
    
    public byte Readdata[];
    
    
    private ExpandableListView gattServicesList;
    //private TiServicesAdapter gattServiceAdapter; //sensor project

    private String deviceName;
    private String deviceAddress;
    private BleService bleService;
    private boolean isConnected = false;

    //private TiSensor<?> activeSensor; //sensor project
    
    //testing time from ble.read() to recieving data
    Date curDate1,curDate2 ;

    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            boolean aaa= bleService.connect(deviceAddress);
            while(aaa=false){
            	aaa= bleService.connect(deviceAddress);
            }
            
            
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action = intent.getAction();
            action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            	 UUIDText.setText("OK");
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(bleService.getSupportedGattServices());
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
               displayData(intent.getStringExtra(BleService.DATA_ADC));
               /*curDate2= new Date(System.currentTimeMillis());
               long diff=curDate2.getTime()-curDate1.getTime();
               displayData(diff+"");*/
            }
        }

		
    };
    
    private void displayData(String data) {
		// TODO Auto-generated method stub
    	if(data != null)
    		Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
		
	}

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
  

  

      

   

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
       
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        //gattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        //gattServicesList.setOnChildClickListener(servicesListClickListner);
        btnon = (Button) findViewById(R.id.button1);
        btnoff = (Button) findViewById(R.id.button2);
        btnread = (Button) findViewById(R.id.button5);
        pppppp=(Button) findViewById(R.id.button3);
        
        connectionState = (TextView) findViewById(R.id.connection_state);
        dataField = (TextView) findViewById(R.id.data_value);
        UUIDText = (TextView) findViewById(R.id.textView1);
        //viewview = (ImageView) findViewById(R.id.imageView1);
        //viewview.setVisibility(View.INVISIBLE);

        getActionBar().setTitle(deviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        
        btnon.setOnClickListener(new Button.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				byte [] aa ={0x21,0x21,0x21,0x21,0x21,0x21,0x21,0x21,0x21,0x21,
						0x21,0x21,0x21,0x21,0x21,0x21,0x21,0x21,0x21,0x21};
				if(bleService.write_board(aa,1)){
					Log.d(TAG, "-------write_board sucess-------");
				}
			}
        	
        });
        
        btnoff.setOnClickListener(new Button.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				byte [] aa ={0x18};
				if(bleService.write_board(aa,1)){
					Log.d(TAG, "-------write_board sucess-------");
				}
			}
        	
        });
        btnread.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				bleService.read();
				curDate1 = new Date(System.currentTimeMillis());
			}
        	
        });
        pppppp.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte [] ll = {0x22};
				bleService.write_board(ll,1);
			}
        	
        });
        
    }
    private void print(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        //boolean result=false;
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
        	/*while(result!=true){
        		result = bleService.connect(deviceAddress);
        	}*/
            
            //Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bleService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (isConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
            	bleService.connect(deviceAddress);
                
                return true;
            case R.id.menu_disconnect:
                bleService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText(resourceId);
            }
        });
    }

   
 

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
