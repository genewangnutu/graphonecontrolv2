package scrollmenu.materialtabs.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sample.ble.sensortag.two.R;
import scrollmenu.materialtabs.fragments.*;
import sample.ble.sensortag.BleService;
import com.devadvance.circularseekbar.CircularSeekBar;

public class SimpleTabsActivity extends AppCompatActivity {

    private final static String TAG =SimpleTabsActivity.class.getName();
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static BleService bleService;
    private String deviceAddress;

    //object
    public static TextView address,state,communicate,test,m1t,m2t,m3t,adc_voltage,click_ADC;
    public static Button m1button,m2button,m3button;
    public static int test_value=0;
    public static String s_address,s_state,s_communicate,s_testvalue;
    public static CircularSeekBar cbar;

    //onclicklistenser lock
    public static boolean mode_b01=false,mode_b02=false,mode_b03=false,
            forntmode_b01=false,forntmode_b02=false,forntmode_b03=false,adc_lock=false,adc_read=false;

    public static Date dt1 , dt2 ;
    public static String time_string;
    public static int min=0;
    public static int sec=0;
    public static byte[] send= new byte[5];
    public static String progress_str=" 99%";
    public static boolean mode2_lock=false;

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
                if(state!=null)
                    state.setText(R.string.connected);
                if(address!=null){
                    address.setText(deviceAddress);
                }

                s_state="Connected";
                s_address=deviceAddress;
                //invalidateOptionsMenu();
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if(state!=null)
                    state.setText(R.string.disconnected);
                s_state="Disconnected";
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if(communicate!=null)
                    communicate.setText("OK");
                s_communicate="OK";
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(bleService.getSupportedGattServices());
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                String get_data=intent.getStringExtra(BleService.DATA_ADC);
                Log.i(TAG,"get data"+get_data);
                if(bleService.Datalock_ADC){
                    adc_voltage.setText(get_data);
                }else if(!bleService.Datalock_ADC){
                    adc_voltage.setText("ADC");
                }
            }
        }


    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_tabs);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        s_address=" ";s_state="Disconnected";s_communicate=" ";s_testvalue=""+0;test_value=0;
        ble_init();
    }

    private void ble_init(){
        final Intent intent = getIntent();
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        final Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);

        mode_b01=false;
        mode_b02=false;
        mode_b03=false;
        adc_read=false;
        bleService = null;

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OneFragment(), "HOME");
        adapter.addFragment(new ADCFragment(), "ADC");
        adapter.addFragment(new TwoFragment(), "PWM");
        adapter.addFragment(new ThreeFragment(), "MODE1");
        adapter.addFragment(new FourFragment(), "MODE2");
        adapter.addFragment(new FiveFragment(), "MODE3");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public static void instruction_mode(byte mode ,byte data){
        byte [] send={mode,data};
        SimpleTabsActivity.bleService.write_board(send,1);
    }

    private void vocal_verify(String vocal){
        if(vocal.indexOf("開啟")!=-1 || vocal.indexOf("模式")!=-1 && vocal.indexOf("關閉")==-1 && vocal.indexOf("關掉")==-1){
            if(vocal.indexOf("模式1")!=-1){
                mode_b02=mode_b03=false;

                if(mode_b01){
                    Toast.makeText(getApplicationContext(),"模式已開啟",Toast.LENGTH_SHORT).show();
                }else{
                    mode_b01=true;
                    front_mode1 fm1=new front_mode1();
                    fm1.start();
                    Toast.makeText(getApplicationContext(),"開啟模式1",Toast.LENGTH_SHORT).show();
                }
            }else if(vocal.indexOf("模式2")!=-1){
                mode_b01=mode_b03=false;

                if(mode_b02){
                    Toast.makeText(getApplicationContext(),"模式已開啟",Toast.LENGTH_SHORT).show();
                }else{
                    mode_b02=true;
                    front_mode2 fm2=new front_mode2();
                    fm2.start();
                    Toast.makeText(getApplicationContext(),"開啟模式2",Toast.LENGTH_SHORT).show();
                }
            }else if(vocal.indexOf("模式3")!=-1){
                mode_b01=mode_b02=false;

                if(mode_b03){
                    Toast.makeText(getApplicationContext(),"模式已開啟",Toast.LENGTH_SHORT).show();
                }else{
                    mode_b03=true;
                    front_mode3 fm3=new front_mode3();
                    fm3.start();
                    Toast.makeText(getApplicationContext(),"開啟模式3",Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if(vocal.indexOf("關閉")!=-1 || vocal.indexOf("關掉")!=-1){
            if(vocal.indexOf("模式1")!=-1){
                mode_b01=false;
                Toast.makeText(getApplicationContext(),"關閉模式1",Toast.LENGTH_SHORT).show();
            }else if(vocal.indexOf("模式2")!=-1){
                mode_b02=false;
                Toast.makeText(getApplicationContext(),"關閉模式2",Toast.LENGTH_SHORT).show();
            }else if(vocal.indexOf("模式3")!=-1){
                mode_b03=false;
                Toast.makeText(getApplicationContext(),"關閉模式3",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //把所有辨識的可能結果印出來看一看，第一筆是最 match 的。
                ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String all = "";
                /*for (Object r : result) {
                    all = all + r.toString() + "\n";
                }*/
                all=all+result.get(0).toString();
                vocal_verify(all);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說話..."); //語音辨識 Dialog 上要顯示的提示文字

            startActivityForResult(intent, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class front_mode1 extends Thread {
        @Override
        public void run() {
            super.run();

            dt1 = new Date(); dt2 = new Date();
            send[0] = (byte) 0xa1;
            bleService.write_board(send, 1);

            if(ThreeFragment.view_lock)
                ThreeFragment.mHandler.sendEmptyMessage(1);

            while (mode_b01) {
                min=((int)(dt2.getTime()-dt1.getTime())/1000)/60;
                sec=((int)(dt2.getTime()-dt1.getTime())/1000)%60;
                dt2 = new Date();

                //15min atfer
                if (dt2.getTime() - dt1.getTime() >= 900000) {
                    send[0] = (byte) 0xb1;
                    bleService.write_board(send, 1);
                    time_string=min+":"+sec+" "+" 65%";
                    if(ThreeFragment.view_lock)
                        ThreeFragment.mHandler.sendEmptyMessage(0);
                } else {
                    time_string=min+":"+sec+" "+" 99%";
                    if(ThreeFragment.view_lock)
                        ThreeFragment.mHandler.sendEmptyMessage(0);
                }

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if(ThreeFragment.view_lock)
                ThreeFragment.mHandler.sendEmptyMessage(2);
            instruction_mode((byte) 0x21 ,(byte)0x00);
            Log.d(TAG, "--front_mode1 Thread stop--");
        }
    }

    public static class front_mode2 extends Thread{
        @Override
        public void run() {
            super.run();

            dt1=new Date();dt2=new Date();
            send[0]=(byte)0xa2;
            SimpleTabsActivity.bleService.write_board(send,1);

            if(FourFragment.view_lock)
                FourFragment.mHandler.sendEmptyMessage(1);

            while(mode_b02){
                min=((int)(dt2.getTime()-dt1.getTime())/1000)/60;
                sec=((int)(dt2.getTime()-dt1.getTime())/1000)%60;
                dt2=new Date();
                time_string=min+":"+sec+" "+progress_str;
                if(FourFragment.view_lock)
                    FourFragment.mHandler.sendEmptyMessage(0);

                //15min atfer
                if(dt2.getTime()-dt1.getTime()>=900000){
                    dt1=new Date();
                    dt2=new Date();

                    mode2_lock=!mode2_lock;
                    if(mode2_lock){
                        send[0]=(byte)0xb2;
                        bleService.write_board(send,1);

                        progress_str=" 5%";
                    }else{
                        send[0]=(byte)0xa2;
                        bleService.write_board(send,1);

                        progress_str=" 99%";
                    }
                }

                try {
                    Thread.sleep(7);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if(FourFragment.view_lock)
                FourFragment.mHandler.sendEmptyMessage(2);
            instruction_mode((byte) 0x21 ,(byte)0x00);
            Log.d(TAG, "--front_mode2 Thread stop--");
        }
    }

    public static class front_mode3 extends Thread{
        @Override
        public void run() {
            super.run();
            dt1=new Date();dt2=new Date();
            send[0]=(byte) 0xa3;
            bleService.write_board(send,1);

            if(FiveFragment.view_lock)
                FiveFragment.mHandler.sendEmptyMessage(1);

            while(mode_b03){
                min=((int)(dt2.getTime()-dt1.getTime())/1000)/60;
                sec=((int)(dt2.getTime()-dt1.getTime())/1000)%60;
                dt2=new Date();

                //5min atfer
                if(dt2.getTime()-dt1.getTime()>=300000){
                    send[0]=(byte)0xb3;
                    bleService.write_board(send,1);

                    time_string=min+":"+sec+" "+" 65%";
                }else{
                    time_string=min+":"+sec+" "+" 99%";
                }
                if(FiveFragment.view_lock)
                    FiveFragment.mHandler.sendEmptyMessage(0);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if(FiveFragment.view_lock)
                FiveFragment.mHandler.sendEmptyMessage(2);
            instruction_mode((byte) 0x21 ,(byte)0x00);
            Log.d(TAG, "--front_mode3 Thread stop--");
        }
    }
}
