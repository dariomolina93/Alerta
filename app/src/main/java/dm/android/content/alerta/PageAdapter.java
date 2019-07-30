package dm.android.content.alerta;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.util.Log;

import dm.android.content.alerta.Fragments.Home;
import dm.android.content.alerta.Fragments.Rights;
import dm.android.content.alerta.Fragments.Settings;

public class PageAdapter extends FragmentStatePagerAdapter {

    private Fragment[] childFragments;
    private String tabTitles[] = new String[]{"Derechos", "Inicio", "Ajustes"};
    public PageAdapter(FragmentManager fm) {
        super(fm);
        childFragments = new Fragment[] {
                new Rights(), //0
                new Home(), //1
                new Settings() //2
        };
    }

    @Override
    public Fragment getItem(int position) {

        Log.d("Checking position", "position: " + position);
        return childFragments[position];
    }

    @Override
    public int getCount() {
        return childFragments.length; //3 items
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
