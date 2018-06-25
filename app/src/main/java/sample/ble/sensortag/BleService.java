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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

//import sample.ble.sensortag.sensor.TiSensor;
//import sample.ble.sensortag.sensor.TiSensors;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BleService extends Service {
    private final static String TAG = BleService.class.getSimpleName();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter adapter;
    private String deviceAddress;
    private BluetoothGatt gatt;
    //public byte a[]={0x21,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x21};
    public byte a[]={0x01};
    public byte b[]={0x18};
    public byte notify_open_lock[]={0x01,0x00};
    public boolean Datalock_ADC = false;
    
    Context m;
    
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic,notify_characteristic;
    private BluetoothGattDescriptor Descriptor ;
    
    
    private int connectionState = STATE_DISCONNECTED;
    
    

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private final static String INTENT_PREFIX = BleService.class.getPackage().getName();
    public final static String ACTION_GATT_CONNECTED = INTENT_PREFIX+".ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = INTENT_PREFIX+".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = INTENT_PREFIX+".ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = INTENT_PREFIX+".ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_NOTIFY = INTENT_PREFIX+".ACTION_DATA_NOTIFY";
    public final static String EXTRA_SERVICE_UUID = INTENT_PREFIX+".EXTRA_SERVICE_UUID";
    public final static String EXTRA_CHARACTERISTIC_UUID = INTENT_PREFIX+".EXTRA_CHARACTERISTIC_UUI";
    public final static String EXTRA_DATA = INTENT_PREFIX+".EXTRA_DATA";
    public final static String DATA_ADC = INTENT_PREFIX+".EXTRA_TEXT";
    
    private static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE = "0000fff3-0000-1000-8000-00805f9b34fb";
    private static final String UUID_READ = "0000fff2-0000-1000-8000-00805f9b34fb";
    private static final String UUIDw_notify = "0000fff4-0000-1000-8000-00805f9b34fb";
    private static final String notify_enable="00002902-0000-1000-8000-00805f9b34fb";
    
    private static final String UUID_stm = "0000fff6-0000-1000-8000-00805f9b34fb";

    // Implements callback methods for GATT events that the app cares about.
    // For example, connection change and services discovered.
    private final BluetoothGattExecutor executor = new BluetoothGattExecutor() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                //gatt.discoverServices();
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        BleService.this.gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                /*final TiSensor<?> sensor = TiSensors.getSensor(characteristic.getService().getUuid().toString());
                if (sensor != null) {
                    if (sensor.onCharacteristicRead(characteristic)) {
                        return;
                    }
                }*/

                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_SERVICE_UUID, characteristic.getService().getUuid().toString());
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
        Log.i(TAG, "broadcast Update");
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            if(Datalock_ADC){
                stringBuilder.append(Integer.toString(data[0] & 0xff)+"\n");
                stringBuilder.append(Integer.toString(data[1] & 0xff)+"V");
                intent.putExtra(DATA_ADC, stringBuilder.toString());
            }
            /*for (int loop=0;loop<data.length;loop++)
                if(loop==0)
                    stringBuilder.append(Integer.toString(data[loop] & 0xff));

            intent.putExtra(EXTRA_TEXT, stringBuilder.toString());*/
        }

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        adapter = bluetoothManager.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (adapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (deviceAddress != null && address.equals(deviceAddress)
                && gatt != null) {
            Log.d(TAG, "Trying to use an existing BluetoothGatt for connection.");
            Toast.makeText(m,
					"Trying to use an existing BluetoothGatt for connection.",
					Toast.LENGTH_LONG).show();
            boolean dd=gatt.connect();
            while (dd==false) {
            	dd=gatt.connect();
            } 
            connectionState = STATE_CONNECTING;
            return true;
        }

        final BluetoothDevice device = adapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            Toast.makeText(m,
					"Device not found.  Unable to connect.",
					Toast.LENGTH_LONG).show();
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        gatt = device.connectGatt(this, false, executor);
        Log.d(TAG, "Trying to create a new connection.");
        deviceAddress = address;
        connectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (adapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            Toast.makeText(m,
					"BluetoothAdapter not initialized",
					Toast.LENGTH_LONG).show();
            return;
        }
        gatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (gatt == null) {
            return;
        }
        gatt.close();
        gatt = null;
    }
    public boolean write_board(byte [] write_data,int channel) {
    	
    	service = gatt.getService(UUID.fromString(UUID_SERVICE));
    	if(service==null){
    		Log.d(TAG, "service = null");
    		return false ;
    	}
    	if(channel==1){
    		/*if(service.getCharacteristic(UUID.fromString(UUID_WRITE))==null){
    			Log.d(TAG, "service = null");
        		return false ;
    		}*/
        	characteristic = service.getCharacteristic(UUID.fromString(UUID_WRITE));
        	if(characteristic==null){
        		Log.d(TAG, "characteristic = null");
        		return false ;
        	}
    	}
    	else if(channel==2){
    		if(service.getCharacteristic(UUID.fromString(UUID_WRITE))==null){
        		return false ;
    		}
        	characteristic = service.getCharacteristic(UUID.fromString(UUID_WRITE));
        	if(characteristic==null){
        		return false ;
        	}
    	}
    	
    	characteristic.setValue(write_data);
    	//gatt.setCharacteristicNotification(characteristic, true);
	    return 	gatt.writeCharacteristic(characteristic);

    }
    public void write(BleService bleService) {
    	
    	
    	
    	service = gatt.getService(UUID.fromString(UUID_stm));
    	
    	characteristic = service.getCharacteristic(UUID.fromString(UUID_stm));
    	
    	characteristic.setValue(a);
        gatt.writeCharacteristic(characteristic);
        
    }
    
    public void write2(BleService bleService) {
    	service = gatt.getService(UUID.fromString(UUID_stm));
    	
    	characteristic = service.getCharacteristic(UUID.fromString(UUID_stm));
    	
    	characteristic.setValue(b);
        gatt.writeCharacteristic(characteristic);
    }

    public boolean open_notify(){

        service = gatt.getService(UUID.fromString(UUID_SERVICE));
    	if(service==null){
    		Log.w(TAG, "gatt getService = null");
    	}
    	if(service.getCharacteristic(UUID.fromString(UUIDw_notify))==null){
    		Log.w(TAG, "gatt Service get Characteristic = null");
		}

    	characteristic = service.getCharacteristic(UUID.fromString(UUIDw_notify));
    	if(characteristic==null){
    		Log.w(TAG, "gatt Service get Characteristic = null");
    	}

        gatt.setCharacteristicNotification(characteristic, true);

    	Descriptor=characteristic.getDescriptor(UUID.fromString(notify_enable));
        if(Descriptor==null){
            Log.w(TAG, "open_notify() descriptor = null");return false;
        }
    	Descriptor.setValue(notify_open_lock);

        if(gatt.writeDescriptor(Descriptor)){
            Log.i(TAG, "open_notify sucess");
        }

        return true;
    }
    
    public void read() {
    	//byte j[] = new byte[10];
    	//int i ;
    	 byte[] data = null ;
    	 String s;
    	service = gatt.getService(UUID.fromString(UUID_SERVICE));
    	/*if(service==null){
    		data[0]='s';
    		return data;
    	}*/
    	characteristic = service.getCharacteristic(UUID.fromString(UUID_READ));
    	/*if(characteristic==null){
    		data[0]='c';
    		return data;
    	}*/
    	
    	//characteristic.setValue(data);
    	
    	readCharacteristic(characteristic);
    	//if(gatt.readCharacteristic(characteristic)){
    		
    	//}
    	
    	/*if(data != null)
    		return data;
    	else{
    		data[0]=0x00;
    		return data;
    	}*/
    		
    	
    	
    	
    	
    }
    
    


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (adapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.readCharacteristic(characteristic);
    }

    /*public void updateSensor(TiSensor<?> sensor) {
        if (sensor == null)
            return;

        if (adapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        executor.update(sensor);
        executor.execute(gatt);
    }*/

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param sensor
     * @param enabled If true, enable notification.  False otherwise.
     */
    /*public void enableSensor(TiSensor<?> sensor, boolean enabled) {
        if (sensor == null)
            return;

        if (adapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        executor.enable(sensor, enabled);
        executor.execute(gatt);
    }*/

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (gatt == null) return null;

        return gatt.getServices();
    }
}
