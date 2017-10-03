package scrollmenu.materialtabs.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import sample.ble.sensortag.two.R;
import scrollmenu.materialtabs.activity.SimpleTabsActivity;


public class OneFragment extends Fragment{
    private final static String TAG= OneFragment.class.getName();

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"OneFragment onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"OneFragment onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }
    private void init(){
        SimpleTabsActivity.address=(TextView)getView().findViewById(R.id.textView2);
        SimpleTabsActivity.state=(TextView)getView().findViewById(R.id.textView4);
        SimpleTabsActivity.communicate=(TextView)getView().findViewById(R.id.textView6);

        SimpleTabsActivity.address.setText(SimpleTabsActivity.s_address);
        SimpleTabsActivity.state.setText(SimpleTabsActivity.s_state);
        SimpleTabsActivity.communicate.setText(SimpleTabsActivity.s_communicate);

    }
}
