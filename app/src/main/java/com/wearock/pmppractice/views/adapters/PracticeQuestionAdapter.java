package com.wearock.pmppractice.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class PracticeQuestionAdapter extends FragmentPagerAdapter {

    private List<Fragment> lstFragments;

    public PracticeQuestionAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.lstFragments = list;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int index) {
        return lstFragments.get(index);
    }

    @Override
    public int getCount() {
        return lstFragments.size();
    }

}
