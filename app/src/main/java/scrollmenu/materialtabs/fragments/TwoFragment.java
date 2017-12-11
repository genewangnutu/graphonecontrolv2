package scrollmenu.materialtabs.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.devadvance.circularseekbar.CircularSeekBar;

import java.util.Date;

import sample.ble.sensortag.two.R;
import scrollmenu.materialtabs.activity.SimpleTabsActivity;


public class TwoFragment extends Fragment{
    private final static String TAG=TwoFragment.class.getName();

    public TwoFragment() {
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
        return inflater.inflate(R.layout.fragment_two, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
       // Log.d(TAG,"fuck you02");
    }
    private void init(){
        SimpleTabsActivity.cbar=(CircularSeekBar)getView().findViewById(R.id.circularSeekBar1);
        SimpleTabsActivity.cbar.setProgress(SimpleTabsActivity.test_value);
        SimpleTabsActivity.cbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(SimpleTabsActivity.test_value!=SimpleTabsActivity.cbar.getProgress()){
                    SimpleTabsActivity.mode_b01=false;
                    SimpleTabsActivity.mode_b02=false;
                    SimpleTabsActivity.mode_b03=false;

                    SimpleTabsActivity.test_value=SimpleTabsActivity.cbar.getProgress();
                    byte [] s={0x21,(byte) ((int) SimpleTabsActivity.test_value/16)};
                    SimpleTabsActivity.bleService.write_board(s,1);
                }

                mHandler.sendEmptyMessage(0);


                return false;
            }
        });
        SimpleTabsActivity.s_testvalue=""+SimpleTabsActivity.test_value;

        SimpleTabsActivity.test=(TextView) getView().findViewById(R.id.ftestview1);
        SimpleTabsActivity.test.setText(SimpleTabsActivity.s_testvalue);
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 0:

                    SimpleTabsActivity.test.setText(""+SimpleTabsActivity.test_value);
                    break;

                default :
                    break;
            }
        }
    };



    @Override
    public void onStart() {
        super.onStart();

    }
}
