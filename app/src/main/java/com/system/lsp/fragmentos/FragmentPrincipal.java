package com.system.lsp.fragmentos;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.system.lsp.R;

public class FragmentPrincipal extends Fragment {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    public TabLayout mTabber;

    public FragmentPrincipal(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.main_layout, container, false);
        prepareView(mView);


        return mView;
    }

    private void prepareView(View view){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
        mTabber =(TabLayout)view.findViewById(R.id.tabber);
        mTabber.addTab(mTabber.newTab().setText("Hoy"));
        mTabber.addTab(mTabber.newTab().setText("Atras"));

        mViewPager = (ViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabber));
        mTabber.setupWithViewPager(mViewPager);
        mTabber.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);


//            switch (position){
//                case 0:
//                    FragmentListaCoutas hoy = new FragmentListaCoutas();
//                    return hoy;
//                case 1:
//                    FragmentListaCoutas hoy2 = new FragmentListaCoutas();
//                    return hoy2;
//
//            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 0;
        }


    }
}
