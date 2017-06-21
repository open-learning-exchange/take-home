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

public class ListView_myCourses extends Fragment {
    // static final String URL = "http://api.androidhive.info/music/music.xml";
    static final String KEY_MATERIALS = "materials"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_DETAILS = "details";
    static final String KEY_FEEDBACK = "feedback";
    static final String KEY_DELETE = "delete";
    static final String KEY_RESOURCE_STATUS = "delete";
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
    String resourceIdList[], resourceTitleList[],courseIdList[],courseTitleList[];
    int rsLstCnt, csLstCnt = 0;
    static Intent intent;
    Database database;
    AssetManager assetManager;

    private List<String> resIDArrayList = new ArrayList<String>();
    private List<String> courseIDArrayList = new ArrayList<String>();
    ListView list;
    ListViewAdapter_myCourses adapter;
    ArrayList<HashMap<String, String>> materialList;
    Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        context = this.getActivity().getWindow().getContext();
        assetManager = getActivity().getAssets();
        View rootView = inflater.inflate(R.layout.listview_universal, container, false);

        materialList = new ArrayList<HashMap<String, String>>();

        androidContext = new AndroidContext(container.getContext());
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        restorePref();
        LoadMyLibraryList();

        list = (ListView) rootView.findViewById(R.id.material_list);
        adapter = new ListViewAdapter_myCourses(resIDArrayList, getActivity(), context, materialList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        return rootView;
    }

    public void LoadMyLibraryList() {
        try {
            //maximus
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database course_db = manager.getExistingDatabase("courses");
            ///Database resource_Db = manager.getDatabase("resources");
            Query orderedQuery = chViews.ReadCourses(course_db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            courseIdList = new String[results.getCount()];
            courseTitleList = new String[results.getCount()];
            csLstCnt = 0;
            String mycourseId="";
            String mycourseTitile;
            String mycourseForgndColor="#000000";
            String mycourseBackgndColor="#FFFFFF";
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = course_db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                ArrayList courseMembers = (ArrayList) properties.get("members");
                for (int cnt = 0; cnt < courseMembers.size(); cnt++) {
                    if (sys_usercouchId.equals(courseMembers.get(cnt).toString())) {
                        mycourseTitile = ((String) properties.get("CourseTitle"));
                        mycourseId = ((String) properties.get("_id"));
                        mycourseForgndColor = ((String) properties.get("foregroundColor"));
                        mycourseBackgndColor = ((String) properties.get("backgroundColor"));
                        courseIdList[csLstCnt] = mycourseId;
                        courseTitleList[csLstCnt] = mycourseTitile;
                        courseIDArrayList.add(mycourseId);

                        //// Get Steps
                        manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                        Database coursestep_Db = manager.getExistingDatabase("coursestep");
                        orderedQuery = chViews.ReadCourseSteps(coursestep_Db).createQuery();
                        orderedQuery.setDescending(true);
                        results = orderedQuery.run();
                        int courseStepsCounter =0;
                        for (Iterator<QueryRow> item = results; item.hasNext(); ) {
                            row = item.next();
                            docId = (String) row.getValue();
                            doc = coursestep_Db.getExistingDocument(docId);
                            Map<String, Object> coursestep_properties = doc.getProperties();
                            if (mycourseId.equals((String) coursestep_properties.get("courseId"))) {
                              /*  ArrayList course_step_resourceId = (ArrayList) coursestep_properties.get("resourceId");
                                ArrayList course_step_resourceTitles = (ArrayList) coursestep_properties.get("resourceTitles");
                                String course_step_title = (String) coursestep_properties.get("title");
                                String course_step_id = (String) coursestep_properties.get("_id");
                                String course_step_descr = (String) coursestep_properties.get("description");
                                int course_step_No = (Integer) coursestep_properties.get("step");*/
                                Log.e(TAG, "Course Step title " + ((String) coursestep_properties.get("title")) + " ");
                                courseStepsCounter++;
                            }
                        }

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ID, ((String) properties.get("_id")));
                        map.put(KEY_TITLE, ((String) properties.get("CourseTitle")));
                        Log.e(TAG, "Course item title " + ((String) properties.get("CourseTitle")) + " ");
                        String desc = (String) properties.get("description");
                        if(desc.length() > 100){
                            desc = desc.substring(0,100);
                        }
                        String dateFrom,dateTo,timeFrom,timeTo,buildDescription;
                        try{
                            dateFrom = ((String) properties.get("startDate") == "") ? "" : ("Date : " +((String) properties.get("startDate")));
                        }catch(Exception err){
                            dateFrom="";
                        }
                        try{
                            dateTo=((String) properties.get("endDate") == "") ? "" : (" - " + ((String) properties.get("endDate")));
                        }catch(Exception err){
                            dateTo="";
                        }
                        try{
                            timeFrom=((String) properties.get("startTime") == "") ? "" :  ("       Time : " +((String) properties.get("startTime")));
                        }catch(Exception err){
                            timeFrom="";
                        }
                        try{
                            timeTo=((String) properties.get("endTime") == "") ? "" : (" - " + ((String) properties.get("endTime")));
                        }catch(Exception err){
                            timeTo="";
                        }
                        buildDescription = "Description : " + desc + " \n" +
                                "Number Of Steps : " + courseStepsCounter + "  " +  dateFrom  + dateTo + timeFrom +  timeTo + "\n";

                        map.put(KEY_DESCRIPTION, buildDescription);
                        map.put(KEY_DETAILS, ((String) properties.get("_id")));
                        map.put(KEY_FEEDBACK, ((String) properties.get("_id")));
                        map.put(KEY_DELETE, ((String) properties.get("_id")));
                        map.put(KEY_RATING, (((String) properties.get("averageRating")) == "") ? "2.2" : (String) properties.get("averageRating"));
                        map.put(KEY_TOTALNUM_RATING, "Rating  (" + properties.get("averageRating") + ")");
                        map.put(KEY_FEMALE_RATING, "");
                        map.put(KEY_MALE_RATING, "");
                        ///map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));
                        Database local_downloaded_resources = manager.getDatabase("offline_courses");
                        Document local_downloaded_doc = local_downloaded_resources.getExistingDocument((String) properties.get("_id"));
                        if(local_downloaded_doc!=null){
                            map.put(KEY_RESOURCE_STATUS,"downloaded");
                        }else{
                            map.put(KEY_RESOURCE_STATUS,"not downloaded");
                        }
                        materialList.add(map);
                        rsLstCnt++;
                        csLstCnt++;
                        coursestep_Db.close();
                    }
                }
            }
            course_db.close();

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
        ;
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int", 0);
        sys_singlefilestreamdownload = settings.getBoolean("pf_singlefilestreamdownload", true);
        sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload", true);
        sys_servername = settings.getString("pf_server_name", " ");
        sys_serverversion = settings.getString("pf_server_version", " ");
    }

}