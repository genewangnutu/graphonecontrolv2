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

import java.nio.charset.StandardCharsets;
import java.util.Vector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import sample.ble.sensortag.adapters.BleDevicesAdapter;
import sample.ble.sensortag.two.R;
import scrollmenu.materialtabs.activity.SimpleTabsActivity;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
@SuppressLint("NewApi")
public class DeviceScanActivity extends ListActivity {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 500;

    private BleDevicesAdapter leDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private Scanner scanner;

    private void requestContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "start");
        Log.d("DeviceScanActivity","123");
        getActionBar().setTitle(R.string.title_devices);

        requestContactPermission();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        }
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_scan, menu);
        if (scanner == null || !scanner.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                leDeviceListAdapter.clear();
                if (scanner == null) {
                    scanner = new Scanner(bluetoothAdapter.getBluetoothLeScanner(), scanCallback);
                    scanner.startScanning();

                    invalidateOptionsMenu();
                }
                break;
            case R.id.menu_stop:
                if (scanner != null) {
                    scanner.stopScanning();
                    scanner = null;

                    invalidateOptionsMenu();
                }
                break;
        }
        return true;
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "im back");
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }


        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //finish();
            } else {
                init();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scanner != null) {
            scanner.stopScanning();
            scanner = null;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = leDeviceListAdapter.getDevice(position);
        if (device == null)
            return;
        int xx=position;
        Toast.makeText(this, "position="+xx, Toast.LENGTH_SHORT).show();
        final Intent intent = new Intent(this, SimpleTabsActivity.class);
        intent.putExtra(DeviceServicesActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceServicesActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        startActivity(intent);
    }

    private void init() {
        if (leDeviceListAdapter == null) {
            leDeviceListAdapter = new BleDevicesAdapter(getBaseContext());
            setListAdapter(leDeviceListAdapter);
        }

        if (scanner == null) {
            scanner = new Scanner(bluetoothAdapter.getBluetoothLeScanner(), scanCallback);
            scanner.startScanning();
        }

        invalidateOptionsMenu();
    }

    // Device scan callback. API 21 before
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @SuppressLint("NewApi")
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leDeviceListAdapter.addDevice(device, rssi);
                            leDeviceListAdapter.notifyDataSetChanged();
                            Log.d(TAG, device.getAddress()+"");
                        }
                    });
                }
            };
    // Device scan callback. API 21 after
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult scanResult) {
            //BluetoothDevice device = scanResult.getDevice();
            //int rssi=scanResult.getRssi();
            leDeviceListAdapter.addDevice(scanResult.getDevice(), scanResult.getRssi());
            leDeviceListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int i) {
            Log.e(TAG, "Scan attempt failed");
        }
    };

    private static class Scanner extends Thread {
        private BluetoothAdapter bluetoothAdapter=null;
        private BluetoothLeScanner bluetoothLeScanner=null;
        private final ScanCallback scanCallback;

        private volatile boolean isScanning = false;

        private ScanFilter.Builder builder;
        private Vector<ScanFilter> filter;
        private ScanSettings.Builder builderScanSettings;


        Scanner(BluetoothAdapter adapter, ScanCallback callback) {
            bluetoothAdapter = adapter;
            scanCallback = callback;
        }
        Scanner(BluetoothLeScanner LeScanner, ScanCallback callback) {
            bluetoothLeScanner=LeScanner;
            scanCallback = callback;

            builder = new ScanFilter.Builder();
            filter = new Vector<ScanFilter>();
            filter.add(builder.build());
            builderScanSettings= new ScanSettings.Builder();
            builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            builderScanSettings.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            builderScanSettings.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
            builderScanSettings.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            builderScanSettings.setReportDelay(0);
        }

        public boolean isScanning() {
            return isScanning;
        }

        public void startScanning() {
            synchronized (this) {
                isScanning = true;

                //bluetoothLeScanner.startScan(scanCallback);
                bluetoothLeScanner.startScan(filter,builderScanSettings.build(),scanCallback);
                start();
            }
        }

        public void stopScanning() {
            synchronized (this) {
                isScanning = false;
                //bluetoothAdapter.stopLeScan(mLeScanCallback); //After API21 is deprecated
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (this) {
                        if (!isScanning)
                            break;
                        //bluetoothLeScanner.startScan(scanCallback);
                    }

                    sleep(SCAN_PERIOD);

                    synchronized (this) {
                        //bluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }
            } catch (InterruptedException ignore) {
            } finally {
                // bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }
}