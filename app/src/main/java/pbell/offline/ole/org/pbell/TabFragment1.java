package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 17/05/16.
 */
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class TabFragment1 extends Fragment {
    /*
    String[] data = new String[]{
            "Max","Nan","Another"
    };*/
    ListView list;
    CustomAdapter adapter;
    public  TabFragment1 CustomListView = null;
    public ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<ListModel>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Just Download
        //HTML
        //PDF.js
        //Bell-Reader
        //MP3
        //Flow Video Player
        //BeLL Video Book Player
        //Native Video


        //Type
        //Textbook
        //Lesson Plan
        //Activities
        //Exercises
        //Discussion Questions
/*
        View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);
        CustomListView = this;
        setListData();
        Resources res =getResources();
        ListView lv = (ListView)rootView.findViewById(R.id.frg1_listView);

        adapter=new CustomAdapter( getActivity(), CustomListViewValuesArr,res );
        list.setAdapter( adapter );*/



        setListData();

        View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);
        ListView lv = (ListView)rootView.findViewById(R.id.frg1_listView);
        MyLibraryArrayAdapter adapter = new MyLibraryArrayAdapter(getActivity(),  R.id.frg1_listView,CustomListViewValuesArr);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Toast.makeText(getContext(), position+" Clicked Item ", Toast.LENGTH_LONG).show();
            }
        });



        //return inflater.inflate(R.layout.tab_fragment_1, container, false);
        return rootView;

    }

    /****** Function to set data in ArrayList *************/
    public void setListData()
    {

        for (int i = 0; i < 8; i++) {

            final ListModel sched = new ListModel();

            /******* Firstly take data in model object ******/
            sched.setCompanyName("Resource Title : "+i);
            ///sched.setImage("image"+i);
            sched.setImage("image"+(i+1)+"");
            sched.setUrl("Resource Type : "+i+"");

            /******** Take Model Object in ArrayList **********/
            CustomListViewValuesArr.add( sched );
        }

    }


    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        ListModel tempValues = ( ListModel ) CustomListViewValuesArr.get(mPosition);


        // SHOW ALERT

       // Toast.makeText(CustomListView,""+tempValues.getCompanyName()+" Image:"+tempValues.getImage() +" Url:"+tempValues.getUrl(), Toast.LENGTH_LONG).show();
    }
}
