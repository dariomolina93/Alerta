package dm.android.content.alerta.Fragments;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dm.android.content.alerta.R;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubView;

import android.widget.RelativeLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;


public class Rights extends Fragment {

    private MoPubView moPubView;
    private String[] rights;
    private String[] description;
    private String tabName = "Derechos";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rights, container, false);

        this.rights = getActivity().getResources().getStringArray(R.array.your_rights);
        this.description = getActivity().getResources().getStringArray(R.array.description);
        setupListView(view);

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("b195f8dd8ded45fe847ad89ed1d016da")
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .withLegitimateInterestAllowed(false)
                .build();

        MoPub.initializeSdk(getContext(), sdkConfiguration, initSdkListener());

        moPubView = (MoPubView) view.findViewById(R.id.adView);
        moPubView.setAdUnitId("b195f8dd8ded45fe847ad89ed1d016da"); // Enter your Ad Unit ID from www.mopub.com

        return view;
    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
           /* MoPub SDK initialized.
           Check if you should show the consent dialog here, and make your ad requests. */

                moPubView.loadAd();
            }
        };
    }

    public void addData(List<List<String>> list) {
        if(rights.length != description.length){
            Log.i("RightsData","title and description missed match.");
            return;
        }
        for(int i = 0; i < rights.length; i++) {
            list.add(new ArrayList<String>());
            list.get(i).add(rights[i]);
            list.get(i).add(description[i]);
        }
    }

    public void setupListView(View view) {
        if(rights.length != description.length) {
            Log.i("RightsData","title and description missed match.");
            return;
        }
        List<List<String>> rightsData = new ArrayList<>();
        this.addData(rightsData);

        ListView listView = view.findViewById(R.id.rights_list);
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_2, android.R.id.text1, rightsData) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = view.findViewById(android.R.id.text1);
                text1.setTextSize(20);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(rights[position]);
                text2.setText(description[position]);
                text2.setVisibility(View.GONE);

                final LayoutParams layoutparams = (RelativeLayout.LayoutParams)text2.getLayoutParams();
                layoutparams.setMargins(0,10,0,0);
                text2.setLayoutParams(layoutparams);
                return view;
            }
        };

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView,
                                    View itemView,
                                    int position,
                                    long id) {
                TextView textView = itemView.findViewById(android.R.id.text2);
                if(textView.getVisibility() == View.GONE) {
                    textView.setVisibility(View.VISIBLE);
                } else {
                    textView.setVisibility(View.GONE);
                }
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);
    }

    public String getTabName() {
        return tabName;
    }
}
