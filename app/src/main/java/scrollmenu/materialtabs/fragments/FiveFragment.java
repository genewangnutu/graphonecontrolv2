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


public class FiveFragment extends Fragment{
    private final static String TAG=FiveFragment.class.getName();
    private static int min=0;
    private static int sec=0;
    private static String time_string;
    public FiveFragment() {
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
        return inflater.inflate(R.layout.fragment_five, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){
        SimpleTabsActivity.m3t=(TextView) getView().findViewById(R.id.mode_text03);
        SimpleTabsActivity.m3button=(Button)getView().findViewById(R.id.mbutton03);
        SimpleTabsActivity.m3button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(SimpleTabsActivity.mode_b03){
                    SimpleTabsActivity.mode_b03=!SimpleTabsActivity.mode_b03;
                    mHandler.sendEmptyMessage(2);

                    byte [] send={(byte)0x21,0x00};
                    SimpleTabsActivity.bleService.write_board(send,1);
                }else{
                    SimpleTabsActivity.mode_b03=!SimpleTabsActivity.mode_b03;
                    SimpleTabsActivity.mode_b02=false;
                    SimpleTabsActivity.mode_b01=false;

                    mHandler.sendEmptyMessage(1);
                    mode3 m3=new mode3();
                    m3.start();
                }
            }
        });
        if(SimpleTabsActivity.mode_b03){
            //true
            mHandler.sendEmptyMessage(1);
        }else{
            //false
            mHandler.sendEmptyMessage(2);
        }
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 0:
                    SimpleTabsActivity.m3t.setText(time_string);
                    break;
                case 1:
                    SimpleTabsActivity.m3button.setText("STOP");
                    break;
                case 2:
                    SimpleTabsActivity.m3t.setText("MODE 3");
                    SimpleTabsActivity.m3button.setText("START");
                    break;
                default :
                    break;
            }
        }
    };
    class mode3 extends Thread{
        @Override
        public void run() {
            super.run();
            Date dt1=new Date(),dt2=new Date();
            byte [] send={(byte) 0xa3};
            SimpleTabsActivity.bleService.write_board(send,1);

            while(SimpleTabsActivity.mode_b03){
                min=((int)(dt2.getTime()-dt1.getTime())/1000)/60;
                sec=((int)(dt2.getTime()-dt1.getTime())/1000)%60;
                dt2=new Date();

                //5min atfer
                if(dt2.getTime()-dt1.getTime()>=300000){
                    send[0]=(byte)0xb3;
                    SimpleTabsActivity.bleService.write_board(send,1);

                    time_string=min+":"+sec+" "+" 65%";
                }else{
                    time_string=min+":"+sec+" "+" 99%";
                }
                mHandler.sendEmptyMessage(0);

                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
