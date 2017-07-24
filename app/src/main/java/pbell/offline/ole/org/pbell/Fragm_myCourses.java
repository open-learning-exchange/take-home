package pbell.offline.ole.org.pbell;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragm_myCourses.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragm_myCourses#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragm_myCourses extends Fragment {
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

    CouchViews chViews = new CouchViews();
    String resourceIdList[], resourceTitleList[],courseIdList[],courseTitleList[];
    int rsLstCnt, csLstCnt = 0;
    static Intent intent;
    Database database;
    AssetManager assetManager;
    View rootView;

    private List<String> resIDArrayList = new ArrayList<>();
    private List<String> courseIDArrayList = new ArrayList<>();
    ListView list;
    ListViewAdapter_myCourses adapter;
    ArrayList<HashMap<String, String>> materialList;

    ////
    private int mShortAnimationDuration;

    Activity mActivity;
    /////////////////////////////////
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    LogHouse logHouse = new LogHouse();

    private OnFragmentInteractionListener mListener;

    public Fragm_myCourses() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragm_myCourses.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragm_myCourses newInstance(String param1, String param2) {
        Fragm_myCourses fragment = new Fragm_myCourses();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        context = this.getActivity().getWindow().getContext();
        assetManager = getActivity().getAssets();
        rootView = inflater.inflate(R.layout.listview_universal, container, false);

        materialList = new ArrayList<>();

        androidContext = new AndroidContext(container.getContext());
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        restorePref();
        LoadMyCourses();

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
    public void LoadMyCourses() {
        try {
            /// Todo remove bellow code

           /* try {
                AndroidContext androidContext = new AndroidContext(context);
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbOffline_courses = manager.getDatabase("offline_courses");
                dbOffline_courses.delete();
            }catch(Exception err){
                err.printStackTrace();
            }

            try {
                AndroidContext androidContext = new AndroidContext(context);
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dboffline_course_resources = manager.getDatabase("offline_course_resources");
                dboffline_course_resources.delete();
            }catch(Exception err){
                err.printStackTrace();
            }*/



            //maximus
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database course_db = manager.getExistingDatabase("courses");
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

                        //// Get Steps
                        manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                        Database coursestep_Db = manager.getExistingDatabase("coursestep");
                        orderedQuery = chViews.ReadCourseSteps(coursestep_Db).createQuery();
                        orderedQuery.setDescending(true);
                        results = orderedQuery.run();
                        int courseStepsCounter = 0;
                        ArrayList course_step_resourceId = null;
                        for (Iterator<QueryRow> item = results; item.hasNext(); ) {
                            row = item.next();
                            docId = (String) row.getValue();
                            doc = coursestep_Db.getExistingDocument(docId);
                            Map<String, Object> coursestep_properties = doc.getProperties();
                            if (mycourseId.equals((String) coursestep_properties.get("courseId"))) {
                               course_step_resourceId = (ArrayList) coursestep_properties.get("resourceId");
                                Log.e(TAG, "Course Step title " + ((String) coursestep_properties.get("title")) + " Step ID "+ docId);
                                Log.e(TAG, "Course Step Resources - " + course_step_resourceId.size() + " ");
                                courseStepsCounter++;
                            }
                        }
                        if (courseStepsCounter > 0) {
                            courseIdList[csLstCnt] = mycourseId;
                            courseTitleList[csLstCnt] = mycourseTitile;
                            courseIDArrayList.add(mycourseId);

                            HashMap<String, String> map = new HashMap<>();
                            map.put(KEY_ID, ((String) properties.get("_id")));
                            map.put(KEY_TITLE, ((String) properties.get("CourseTitle")));
                            Log.e(TAG, "Course item title " + ((String) properties.get("CourseTitle")) + " ");
                            String desc = (String) properties.get("description");
                            if (desc.length() > 100) {
                                desc = desc.substring(0, 100);
                            }
                            String dateFrom, dateTo, timeFrom, timeTo, buildDescription;
                            try {
                                dateFrom = ((String) properties.get("startDate") == "") ? "" : ("Date : " + ((String) properties.get("startDate")));
                            } catch (Exception err) {
                                dateFrom = "";
                            }
                            try {
                                dateTo = ((String) properties.get("endDate") == "") ? "" : (" - " + ((String) properties.get("endDate")));
                            } catch (Exception err) {
                                dateTo = "";
                            }
                            try {
                                timeFrom = ((String) properties.get("startTime") == "") ? "" : ("  Time : " + ((String) properties.get("startTime")));
                            } catch (Exception err) {
                                timeFrom = "";
                            }
                            try {
                                timeTo = ((String) properties.get("endTime") == "") ? "" : (" - " + ((String) properties.get("endTime")));
                            } catch (Exception err) {
                                timeTo = "";
                            }
                            buildDescription = "Description : " + desc + " \n" +
                                    "Number of steps : " + courseStepsCounter + "  " + dateFrom + dateTo + timeFrom + timeTo + "\n";

                            map.put(KEY_DESCRIPTION, buildDescription);
                            map.put(KEY_DETAILS, ((String) properties.get("_id")));
                            map.put(KEY_FEEDBACK, ((String) properties.get("_id")));
                            map.put(KEY_DELETE, ((String) properties.get("_id")));
                            map.put(KEY_RATING, (((String) properties.get("averageRating")) == "") ? "2.2" : (String) properties.get("averageRating"));
                            map.put(KEY_TOTALNUM_RATING, "Rating  (" + properties.get("averageRating") + ")");
                            map.put(KEY_FEMALE_RATING, "");
                            map.put(KEY_MALE_RATING, "");
                            ///map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));
                            Database local_downloaded_courses = manager.getDatabase("offline_courses");
                            Document local_downloaded_doc = local_downloaded_courses.getExistingDocument((String) properties.get("_id"));
                            if (local_downloaded_doc != null) {
                                map.put(KEY_RESOURCE_STATUS, "downloaded");
                            } else {
                                if(course_step_resourceId.size()<1) {
                                    map.put(KEY_RESOURCE_STATUS, "downloaded");
                                    createCourseDoc((String) properties.get("_id"), course_step_resourceId.size());
                                }else{
                                    map.put(KEY_RESOURCE_STATUS, "not downloaded");
                                }
                            }
                            materialList.add(map);
                            rsLstCnt++;
                            csLstCnt++;
                        }
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
    public void createCourseDoc(String manualCourseId, int numberOfSteps) {
        Database database = null;
        try {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase("offline_courses");
            Map<String, Object> properties = new HashMap<>();
            properties.put("Steps", numberOfSteps);
            properties.put("localfile", "yes");
            Document document = database.getDocument(manualCourseId);
            try {
                document.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Cannot course details in offline courses"+ e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private void crossfadeShowLoading(final View fromView,View toView) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        fromView.setAlpha(0f);
        fromView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        fromView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        fromView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fromView.setVisibility(View.GONE);
                    }
                });
    }


}
