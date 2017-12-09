package org.ole.learning.planet.planetlearning;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragm_TakeCourseTabbed.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragm_TakeCourseTabbed#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragm_TakeCourseTabbed extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Context context;
    public static final String PREFS_NAME = "MyPrefsFile";
    AssetManager assetManager;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Fragm_TakeCourseTabbed() {
        // Required empty public constructor
    }

    TabLayout tabLayout;
    ViewPager viewPager;
    View rootView;
    int no_of_categories=-1;

    // TODO: Rename and change types and number of parameters
    public static Fragm_TakeCourseTabbed newInstance(String param1, String param2) {
        Fragm_TakeCourseTabbed fragment = new Fragm_TakeCourseTabbed();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = container.getContext();
        context = this.getActivity().getWindow().getContext();

        assetManager = getActivity().getAssets();
        rootView = inflater.inflate(R.layout.frgm_take_course_tabbed, container, false);


        tabLayout=(TabLayout)rootView.findViewById(R.id.tabs);
        viewPager =(ViewPager)rootView.findViewById(R.id.viewpager);

        ///no_of_categories=YOUR_NO_CATEGORIES;

        for (int i = 0; i < 5; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("TAB " + String.valueOf(i + 1)));
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        return rootView;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


