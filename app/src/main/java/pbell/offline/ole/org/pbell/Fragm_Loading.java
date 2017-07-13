package pbell.offline.ole.org.pbell;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragm_Loading.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragm_Loading#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragm_Loading extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TargetAction = "targetAction";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    String requestedAction;
    String mParam2;

    private OnFragmentInteractionListener mListener;

    public Fragm_Loading() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Fragm_Loading newInstance(String param1, String param2) {
        Fragm_Loading fragment = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString(ARG_TargetAction, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            requestedAction = getArguments().getString(ARG_TargetAction);
            ///mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragm_loading, container, false);
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

    @Override
    public void onResume() {
        super.onResume();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(requestedAction.equalsIgnoreCase("myLibrary")){
                    Fragm_myLibrary fg_myLibrary = new Fragm_myLibrary();
                    mListener.onFinishPageLoad(fg_myLibrary,"myLibrary");

                }else if(requestedAction.equalsIgnoreCase("myCourses")) {
                    Fragm_myCourses fg_myCourses = new Fragm_myCourses();
                    mListener.onFinishPageLoad(fg_myCourses, "myCourses");

                }else if(requestedAction.equalsIgnoreCase("Library")) {
                    ListView_Library fg_Library = new ListView_Library();
                    mListener.onFinishPageLoad(fg_Library, "Library");
                }
            }
        }, 1500);

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void onFinishPageLoad(Fragment fragToCall,String actionTarget);
    }
}
