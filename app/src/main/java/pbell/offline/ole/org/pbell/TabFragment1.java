package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 17/05/16.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TabFragment1 extends Fragment {
    /*
    String[] data = new String[]{
            "Max","Nan","Another"
    };*/
    ListView list;
    CustomAdapter adapter;
    public  TabFragment1 CustomListView = null;
    public ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<ListModel>();
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    CouchViews chViews = new CouchViews();
    Context context = null;

    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
            sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
            sys_usergender, sys_uservisits= "";
    int sys_uservisits_Int=0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();

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
        //Discussion Questions*/

        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
        sys_password = settings.getString("pf_password","");
        sys_usercouchId = settings.getString("pf_usercouchId","");
        sys_userfirstname = settings.getString("pf_userfirstname","");
        sys_userlastname = settings.getString("pf_userlastname","");
        sys_usergender = settings.getString("pf_usergender","");
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int",0);
        sys_uservisits= settings.getString("pf_uservisits","");



        loadUserDetails();
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
            sched.setTitle("Resource Title : "+i);
            ///sched.setImage("image"+i);
            sched.setImage("image"+(i+1)+"");
            sched.setDescription("Resource Type : "+i+"");

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
    public void loadUserDetails() {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            if (sys_usercouchId != "") {
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database db = manager.getExistingDatabase("shelf");
                //Database db_resources = manager.getExistingDatabase("resources");
                Query orderedQuery = chViews.ReadShelfByIdView(db).createQuery();
                orderedQuery.setDescending(true);
                ///orderedQuery.setStartKeyDocId(sys_usercouchId);
                orderedQuery.setStartKey(sys_usercouchId);
                //orderedQuery.setEndKey("2014");
                //orderedQuery.setLimit(0);
                QueryEnumerator results = orderedQuery.run();
                for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                    QueryRow row = it.next();
                    ///String docId = (String) row.getValue();
                   // HashMap<String,String> map =(HashMap<String,String>)row.getValue();
                    //String value = map.get("_id");



                    ///Document doc = db.getExistingDocument(docId);
                    //Map<String, Object> properties = doc.getProperties();
                    Log.e("MYAPP", " Data In Shelf Id: " + row.getValue());

                    /*Set<String> stgSet = settings.getStringSet("pf_userroles", new HashSet<String>());
                    ArrayList roleList = (ArrayList<String>) properties.get("roles");
                    for(int cnt=0;cnt< roleList.size();cnt++){
                        stgSet.add(String.valueOf(roleList.get(cnt)));
                    }
                    Log.e("MYAPP", " Data Login Id: " + doc_loginId +" Password: "+ doc_password);
                    Intent intent = new Intent(this,Dashboard.class);
                    startActivity(intent);*/


                }
                //db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
