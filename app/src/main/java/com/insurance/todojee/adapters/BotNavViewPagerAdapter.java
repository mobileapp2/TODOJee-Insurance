package com.insurance.todojee.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.insurance.todojee.fragments.Calendar_Fragment;
import com.insurance.todojee.fragments.Clients_Fragment;
import com.insurance.todojee.fragments.Policy_Fragment;
import com.insurance.todojee.fragments.Reminders_Fragment;
import com.insurance.todojee.fragments.TodoList_Fragment;

import java.util.ArrayList;

public class BotNavViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private Fragment currentFragment;

    public BotNavViewPagerAdapter(FragmentManager fm) {
        super(fm);

        fragments.clear();
        fragments.add(new Clients_Fragment());
        fragments.add(new Policy_Fragment());
        fragments.add(new Calendar_Fragment());
        fragments.add(new Reminders_Fragment());
        fragments.add(new TodoList_Fragment());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }
}