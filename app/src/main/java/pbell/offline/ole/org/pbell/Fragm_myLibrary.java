package pbell.offline.ole.org.pbell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Fragm_myLibrary extends Fragment {
    // static final String URL = "http://api.androidhive.info/music/music.xml";
    static final String KEY_MATERIALS = "materials"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_DETAILS = "details";
    static final String KEY_FEEDBACK = "feedback";
    static final String KEY_DELETE = "delete";
    static final String KEY_RESOURCE_STATUS = "downloaded";
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
    String resourceIdList[], resourceTitleList[];
    int rsLstCnt, csLstCnt = 0;
    static Intent intent;
    Database database;
    AssetManager assetManager;

    private List<String> resIDArrayList = new ArrayList<>();
    ListView list;
    ListViewAdapter_myLibrary adapter;
    ArrayList<HashMap<String, String>> materialList;
    Activity mActivity;
    LogHouse logHouse = new LogHouse();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        context = this.getActivity().getWindow().getContext();
        assetManager = getActivity().getAssets();
        View rootView = inflater.inflate(R.layout.listview_universal, container, false);

        materialList = new ArrayList<>();

        androidContext = new AndroidContext(container.getContext());
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        restorePref();
        LoadMyLibraryList();

        list = (ListView) rootView.findViewById(R.id.material_list);
        adapter = new ListViewAdapter_myLibrary(resIDArrayList, getActivity(), context, materialList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        return rootView;
    }

    public void LoadMyLibraryList() {
/*
        try {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase("resources");
            database.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database shelf_db = manager.getExistingDatabase("shelf");
            Database shadowresources_db = manager.getExistingDatabase("shadowresources");
            Database local_downloaded_resources = manager.getDatabase("resources");
            Query orderedQuery = chViews.ReadShelfByIdView(shelf_db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            resourceIdList = new String[results.getCount()];
            resourceTitleList = new String[results.getCount()];
            rsLstCnt = 0;
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document shelf_doc = shelf_db.getExistingDocument(docId);
                Map<String, Object> shelf_properties = shelf_doc.getProperties();
                ///Map<String, Object> shadowresources_properties = null;
                if (sys_usercouchId.equals((String) shelf_properties.get("memberId"))) {
                    String myresTitile = ((String) shelf_properties.get("resourceTitle"));
                    String myresId = ((String) shelf_properties.get("resourceId"));
                    resourceTitleList[rsLstCnt] = myresTitile;
                    resourceIdList[rsLstCnt] = myresId;
                    resIDArrayList.add(myresId);
                    Log.e(TAG, "Resource Title " + (String) shelf_properties.get("resourceTitle"));
                    String buildDecript = "Description not available.";
                    String buildRating = "0";
                    String avgRating ="0";
                    Boolean resourceDownloaded = false;
                    HashMap<String, String> map = new HashMap<>();
                    map.put(KEY_ID, myresId);
                    map.put(KEY_TITLE, myresTitile);
                    try {
                        Log.e(TAG, "Resource In try " + myresTitile);
                        Document shadowresources_doc = shadowresources_db.getExistingDocument(myresId);
                        Document local_downloaded_doc = local_downloaded_resources.getExistingDocument(myresId);

                        Log.e(TAG, "Completed going to if " + myresTitile);
                        if(shadowresources_doc!=null){
                            Map<String, Object> shadowresources_properties = shadowresources_doc.getProperties();
                            buildDecript = "Author : " + (String) shadowresources_properties.get("author") + "  Language : " + (String) shadowresources_properties.get("language") + " \n" +
                                    "  Resource Type : " + (String) shadowresources_properties.get("Medium") + " \n" +
                                    "Date Uploaded : " + (String) shadowresources_properties.get("uploadDate") + "  ";
                            buildRating = (((String) shadowresources_properties.get("averageRating")) == "") ? "2.2" : (String) shadowresources_properties.get("averageRating");
                            avgRating =shadowresources_properties.get("averageRating").toString();
                            Log.e(TAG, "OBJECT FOUND - Item found in shadow resources " + myresTitile);
                        }
                        if (local_downloaded_doc != null) {
                            Log.e(TAG, "OBJECT ERROR - Item not in shadow resources checking local resources " + myresTitile);
                            Map<String, Object> local_downloaded_properties = local_downloaded_doc.getProperties();
                            Log.e(TAG, "OBJECT FOUND - Item found in local resources " + myresTitile);
                            buildDecript = "Author : " + (String) local_downloaded_properties.get("author") + "  Language : " + (String) local_downloaded_properties.get("language") + " \n" +
                                    "Date Uploaded : " + (String) local_downloaded_properties.get("uploadDate") + "  ";
                            resourceDownloaded = true;
                            Log.e(TAG, "Resource Downloaded True " + myresTitile);
                        }

                    } catch (Exception err) {
                        Log.e(TAG, "ERROR NOT Found in shadow resources" + err.getMessage());
                        err.printStackTrace();
                       // rsLstCnt++;
                    }

                    if(resourceDownloaded){
                        map.put(KEY_RESOURCE_STATUS,"downloaded");
                    }else{
                        map.put(KEY_RESOURCE_STATUS,"not downloaded");
                    }
                    map.put(KEY_DESCRIPTION, buildDecript);
                    map.put(KEY_DETAILS, myresId);
                    map.put(KEY_FEEDBACK,myresId);
                    map.put(KEY_DELETE, myresId);
                    map.put(KEY_RATING, buildRating);
                    map.put(KEY_TOTALNUM_RATING, "Rating  (" + avgRating + ")");
                    map.put(KEY_FEMALE_RATING, "");
                    map.put(KEY_MALE_RATING, "");
                    ///map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));
                    materialList.add(map);
                    rsLstCnt++;
                }
            }
            shelf_db.close();

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

    public void restorePref() {
        // Restore preferences
        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username", "");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl", "http://");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate", "");
        sys_password = settings.getString("pf_password", "");
        sys_usercouchId = settings.getString("pf_usercouchId", "");
        sys_userfirstname = settings.getString("pf_userfirstname", "");
        sys_userlastname = settings.getString("pf_userlastname", "");
        sys_usergender = settings.getString("pf_usergender", "");
        sys_uservisits = settings.getString("pf_uservisits", "");
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int", 0);
        sys_singlefilestreamdownload = settings.getBoolean("pf_singlefilestreamdownload", true);
        sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload", true);
        sys_servername = settings.getString("pf_server_name", " ");
        sys_serverversion = settings.getString("pf_server_version", " ");
    }



}