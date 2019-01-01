package com.insurance.todojee.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

import com.insurance.todojee.R;
import com.insurance.todojee.fragments.GeneralInsurePolicyType_Fragment;
import com.insurance.todojee.fragments.LifeInsurePolicyType_Fragment;

import java.util.ArrayList;
import java.util.List;

public class MastersPolicyType_Activity extends FragmentActivity {

    private Context context;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_type);

        init();
        setDefaults();
        setEventListner();
        setUpToolbar();
    }

    private void init() {
        context = MastersPolicyType_Activity.this;
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tl_tabnames);
        viewPager = findViewById(R.id.viewPager);
//        setupViewPager(viewPager);
//        tabLayout.setupWithViewPager(viewPager);
//        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
//        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setColor(Color.GRAY);
//        drawable.setSize(1, 1);
//        linearLayout.setDividerPadding(10);
//        linearLayout.setDividerDrawable(drawable);
        viewPager.setOffscreenPageLimit(3);
    }

    private void setDefaults() {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.icon_lifeinsurance);
        tabLayout.getTabAt(1).setIcon(R.drawable.icon_generalinsurance);

        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.Battleship_Gray), PorterDuff.Mode.SRC_IN);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new LifeInsurePolicyType_Fragment(), "Life Insurance");
        adapter.addFrag(new GeneralInsurePolicyType_Fragment(), "General Insurance");
        viewPager.setAdapter(adapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setEventListner() {

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.Battleship_Gray), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Policy Type");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
