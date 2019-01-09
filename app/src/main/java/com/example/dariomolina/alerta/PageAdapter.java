package com.example.dariomolina.alerta;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.dariomolina.alerta.Fragments.Home;
import com.example.dariomolina.alerta.Fragments.Rights;
import com.example.dariomolina.alerta.Fragments.Settings;

public class PageAdapter extends FragmentStatePagerAdapter {

    private Fragment[] childFragments;
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
        String title = getItem(position).getClass().getName();
        return title.subSequence(title.lastIndexOf(".") + 1, title.length());
    }
}
