package scrollmenu.materialtabs.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sample.ble.sensortag.two.R;
import scrollmenu.materialtabs.activity.SimpleTabsActivity;


public class ADCFragment extends Fragment{
    private final static String TAG=ADCFragment.class.getName();
    public ADCFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_six, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG,"ThreeFragment onViewCreated");
        //view_lock=true;
        //FourFragment.view_lock=FiveFragment.view_lock=false;
        init();
    }

    private void init(){
        SimpleTabsActivity.adc_voltage = (TextView) getView().findViewById(R.id.adc_voltage);
        SimpleTabsActivity.click_ADC = (TextView) getView().findViewById(R.id.click_startADC);
        SimpleTabsActivity.click_ADC.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(0);

                if(SimpleTabsActivity.adc_lock){
                    mHandler.sendEmptyMessage(0);
                    SimpleTabsActivity.adc_lock=!SimpleTabsActivity.adc_lock;
                    enable_notification();
                }else{
                    mHandler.sendEmptyMessage(1);
                    SimpleTabsActivity.adc_lock=!SimpleTabsActivity.adc_lock;
                }
            }
        });
    }

    private void enable_notification(){
        SimpleTabsActivity.bleService.write(SimpleTabsActivity.bleService);
        SimpleTabsActivity.bleService.open_notify();
    }

    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 0:

                    SimpleTabsActivity.click_ADC.setText("Click to stop detect");
                    break;
                case 1:
                    SimpleTabsActivity.click_ADC.setText("Click to start detect");
                    break;
                default :
                    break;
            }

        }
    };

    private Thread adcfragment_td = new Thread(){
        @Override
        public void run() {
            super.run();

        }
    };
}
