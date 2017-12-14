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
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import sample.ble.sensortag.two.R;
import scrollmenu.materialtabs.activity.SimpleTabsActivity;

/**
 * mode 2
 * */
public class FourFragment extends Fragment{
    private final static String TAG=FourFragment.class.getName();
    public static boolean view_lock=false;
    public FourFragment() {
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
        return inflater.inflate(R.layout.fragment_four, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG,"FourFragment onViewCreated");
        view_lock=true;
        //ThreeFragment.view_lock=FiveFragment.view_lock=false;
        init();
    }
    private void init(){
        SimpleTabsActivity.m2t=(TextView) getView().findViewById(R.id.mode_text02);
        SimpleTabsActivity.m2button=(Button)getView().findViewById(R.id.mbutton02);
        SimpleTabsActivity.m2button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SimpleTabsActivity.mode_b02){
                    SimpleTabsActivity.mode_b02=!SimpleTabsActivity.mode_b02;
                    mHandler.sendEmptyMessage(2);

                    byte [] send={(byte)0x21,0x00};
                    SimpleTabsActivity.bleService.write_board(send,1);
                }else{
                    SimpleTabsActivity.mode_b02=!SimpleTabsActivity.mode_b02;
                    SimpleTabsActivity.mode_b01=false;
                    SimpleTabsActivity.mode_b03=false;

                    mHandler.sendEmptyMessage(1);
                    SimpleTabsActivity.front_mode2 fm2=new SimpleTabsActivity.front_mode2();
                    fm2.start();
                }
            }
        });
        if(SimpleTabsActivity.mode_b02){
            //true
            mHandler.sendEmptyMessage(1);
        }
        else{
            //false
            mHandler.sendEmptyMessage(2);
        }
    }
    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 0:
                    SimpleTabsActivity.m2t.setText(SimpleTabsActivity.time_string);
                    break;
                case 1:
                    SimpleTabsActivity.m2button.setText("STOP");
                    break;
                case 2:
                    SimpleTabsActivity.m2t.setText("MODE 2");
                    SimpleTabsActivity.m2button.setText("START");
                    break;
                default :
                    break;
            }
        }
    };
    class mode2 extends Thread{
        @Override
        public void run() {
            super.run();
            if(SimpleTabsActivity.forntmode_b02){
                //switch & close front thread
                SimpleTabsActivity.forntmode_b02=false;
            }else{
                SimpleTabsActivity.dt1=new Date();SimpleTabsActivity.dt2=new Date();
                SimpleTabsActivity.send[0]=(byte)0xa2;
                SimpleTabsActivity.bleService.write_board(SimpleTabsActivity.send,1);
            }


            while(SimpleTabsActivity.mode_b02){
                SimpleTabsActivity.min=((int)(SimpleTabsActivity.dt2.getTime()-SimpleTabsActivity.dt1.getTime())/1000)/60;
                SimpleTabsActivity.sec=((int)(SimpleTabsActivity.dt2.getTime()-SimpleTabsActivity.dt1.getTime())/1000)%60;
                SimpleTabsActivity.dt2=new Date();
                SimpleTabsActivity.time_string=SimpleTabsActivity.min+":"+SimpleTabsActivity.sec+" "+SimpleTabsActivity.progress_str;
                mHandler.sendEmptyMessage(0);

                //15min atfer
                if(SimpleTabsActivity.dt2.getTime()-SimpleTabsActivity.dt1.getTime()>=900000){
                    SimpleTabsActivity.dt1=new Date();
                    SimpleTabsActivity.dt2=new Date();

                    SimpleTabsActivity.mode2_lock=!SimpleTabsActivity.mode2_lock;
                    if(SimpleTabsActivity.mode2_lock){
                        SimpleTabsActivity.send[0]=(byte)0xb2;
                        SimpleTabsActivity.bleService.write_board(SimpleTabsActivity.send,1);

                        SimpleTabsActivity.progress_str=" 5%";
                    }else{
                        SimpleTabsActivity.send[0]=(byte)0xa2;
                        SimpleTabsActivity.bleService.write_board(SimpleTabsActivity.send,1);

                        SimpleTabsActivity.progress_str=" 99%";
                    }
                }

                try {
                    Thread.sleep(12);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            mHandler.sendEmptyMessage(2);
            Log.d(TAG, "--mode1 Thread stop--");
        }
    }
}
