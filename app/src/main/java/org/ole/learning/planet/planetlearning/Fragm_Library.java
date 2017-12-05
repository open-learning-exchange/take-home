package org.ole.learning.planet.planetlearning;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
/*
public class Fragm_Library extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_universal);
    }
}
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

public class Fragm_Library extends OpenResource {
   // static final String URL = "http://api.androidhive.info/music/music.xml";
    static final String KEY_MATERIALS = "materials"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_DETAILS = "details";
    static final String KEY_FEEDBACK = "feedback";
    static final String KEY_DELETE = "delete";
    static final String KEY_RATING = "4";
    static final String KEY_TOTALNUM_RATING = "200";
    static final String KEY_FEMALE_RATING = "0";
    static final String KEY_MALE_RATING = "0";
    static final String KEY_THUMB_URL = "thumb_url";
    Context context;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USERNAME = "";
    SharedPreferences settings;
    private static final String TAG = "MYAPP";
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    int sys_uservisits_Int = 0;
    AndroidContext androidContext;
    Manager manager;
    private ProgressDialog mDialog;

    CouchViews chViews = new CouchViews();
    String resourceIdList[],resourceTitleList[];
    int rsLstCnt,csLstCnt = 0;
    static Intent intent;
    Database database;
    AssetManager assetManager;

    private List<String> resIDArrayList = new ArrayList<>();
    ListView list;
    ListViewAdapter_Library adapter;
    ArrayList<HashMap<String, String>> materialList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        assetManager = getActivity().getAssets();
        View rootView = inflater.inflate(R.layout.listview_universal, container, false);

        materialList = new ArrayList<>();

        androidContext = new AndroidContext(container.getContext());
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            restorePref();
            LoadShadowResourceList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        list= rootView.findViewById(R.id.material_list);
        adapter=new ListViewAdapter_Library(getActivity(), materialList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
            }
        });

        return rootView;
    }
    public void LoadShadowResourceList() {
            try {
                Database shadowres = manager.getDatabase("shadowresources");
                Query orderedQuery = chViews.ReadMemberVisitsId(shadowres).createQuery();
                orderedQuery.setDescending(true);
                QueryEnumerator results = orderedQuery.run();
                resourceTitleList =  new String[results.getCount()];
                resourceIdList = new String[results.getCount()];
                for (Iterator<QueryRow> shr = results; shr.hasNext(); ) {
                    QueryRow row = shr.next();
                    String docId = (String) row.getValue();
                    com.couchbase.lite.Document resource_doc = shadowres.getExistingDocument(docId);
                    Map<String, Object> resource_properties = resource_doc.getProperties();
                    try {
                        resource_properties = resource_doc.getProperties();
                    } catch (Exception errs) {
                        Log.e("tag", "OBJECT ERROR " + errs.toString());
                    }
                    HashMap<String, String> map = new HashMap<>();
                    Log.e("MyCouch", "Number shadow items " + ((String) resource_properties.get("title")) + " ");
                    String buildDecript ="";
                    try{
                        buildDecript = "Author : "+(String) resource_properties.get("author") + "  Language : "+(String) resource_properties.get("language") + " \n" +
                                "Subject : "+android.text.TextUtils.join(",", (ArrayList) resource_properties.get("subject")) + "  Resource Type : " +(String) resource_properties.get("resourceType") + " \n" +
                                "Date Uploaded : "+(String) resource_properties.get("uploadDate") + "  ";
                    }catch(Exception err){
                        buildDecript = " ";
                        return;
                    }
                    resourceTitleList[rsLstCnt] = (String) resource_properties.get("title");
                    resourceIdList[rsLstCnt] = (String) resource_properties.get("_id");
                    resIDArrayList.add((String) resource_properties.get("_id"));
                    map.put(KEY_ID, ((String) resource_properties.get("_id")));
                    map.put(KEY_TITLE, ((String) resource_properties.get("title")));
                    map.put(KEY_DESCRIPTION, buildDecript);
                    // Button Actions
                    map.put(KEY_DETAILS,((String) resource_properties.get("_id")));
                    map.put(KEY_FEEDBACK, ((String) resource_properties.get("_id")));
                    map.put(KEY_DELETE, ((String) resource_properties.get("_id")));

                    map.put(KEY_RATING, (((String) resource_properties.get("averageRating")) == "")? "2.2" : (String) resource_properties.get("averageRating"));
                    map.put(KEY_TOTALNUM_RATING, "Rating  ("+resource_properties.get("averageRating")+")");
                    map.put(KEY_FEMALE_RATING, "");
                    map.put(KEY_MALE_RATING, "");
                    ///map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));
                    materialList.add(map);
                    rsLstCnt++;
                }
                shadowres.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int getIconType(String myresExt) {

        int img = R.drawable.web;
        switch (myresExt) {
            case "Flow Video Player":
                img = R.drawable.video;
                break;
            case "MP3":
                img = R.drawable.mp3;
                break;
            case "PDF.js":
                img = R.drawable.pdf;
                break;
            case "HTML":
                img = R.drawable.htmlimage;
                break;
            case "-":
                img = R.drawable.web;
                break;
            default:
                img = R.drawable.unknownresource1;
                break;
        }
        return img;
    }
    public void restorePref(){
        // Restore preferences
        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","http://");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
        sys_password = settings.getString("pf_password","");
        sys_usercouchId = settings.getString("pf_usercouchId","");
        sys_userfirstname = settings.getString("pf_userfirstname","");
        sys_userlastname = settings.getString("pf_userlastname","");
        sys_usergender = settings.getString("pf_usergender","");
        sys_uservisits = settings.getString("pf_uservisits","");
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int",0);
        sys_singlefilestreamdownload =settings.getBoolean("pf_singlefilestreamdownload",true);
        sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload",true);
        sys_servername = settings.getString("pf_server_name"," ");
        sys_serverversion = settings.getString("pf_server_version"," ");
    }

}