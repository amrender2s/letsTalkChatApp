package com.example.sample;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

//import com.example.firebasetest.CallFragment.Calls;
//import com.example.firebasetest.ChatFragment.Chat;
//import com.example.firebasetest.StatusFragment.Status;

public class TabFragmentAdapter extends FragmentPagerAdapter {
    Context context;
    int totalTabs;
    public TabFragmentAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        this.context=context;
        this.totalTabs=totalTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RequestFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new FriendsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
