package dm.release.dariomolina.alerta.Fragments;

import android.os.Build;
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

import dm.release.dariomolina.alerta.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import android.widget.RelativeLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;


public class Rights extends Fragment {

    private AdView mAdView;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MobileAds.initialize(getContext(), "ca-app-pub-4491011983892764~9524664327");
        }

        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return view;
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
