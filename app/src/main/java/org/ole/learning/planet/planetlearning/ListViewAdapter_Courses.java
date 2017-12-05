package org.ole.learning.planet.planetlearning;

/**
 * Created by leonardmensah on 06/06/2017.
 */

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.ArrayUtils;
import com.google.gson.JsonObject;
import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.listener.FetchListener;
import com.tonyodev.fetch.request.Request;

import org.json.JSONObject;
import org.lightcouch.CouchDbClientAndroid;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ListViewAdapter_Courses extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;
    private static final String TAG = "MYAPP";
    Context context;
    private ProgressDialog mDialog;
    private long enqueue;
    private DownloadManager downloadManager;
    boolean singleFileDownload = true;
    public static final String PREFS_NAME = "MyPrefsFile";
    CouchViews chViews = new CouchViews();
    long downloadId = 0;

    /// String
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    String OneByOneResID, OneByOneResTitle, OneByOneCourseId;
    int courseStepsCounter = 0;
    int action_button_id = 0;
    SharedPreferences settings;
    List<String> resIDArrayList = new ArrayList<>();
    View vi;
    TextView title, description, ratingAvgNum, totalNum;
    Button open;
    RatingBar ratingStars;
    LayerDrawable stars;
    ProgressBar femalerating, malerating;
    String activityName = "Courses";
    protected int _splashTime = 5000;
    private Thread splashTread;

    private long enqueues;
    private DownloadManager dm;
    Cursor c;
    LogHouse logHouse = new LogHouse();
    private OnCourseListListener mListener;
    Fetch fetch;
    List<Long> downloadListIDs = new ArrayList<>();
    List<Request> requests = new ArrayList<>();
    Dialog dialogDownloadProgress;
    ProgressBar downloadPB;
    TextView txtProgressStatus;
    String downloadingCourseTitle;
    boolean downloadCompleted = false;


    public ListViewAdapter_Courses(final List<String> resIDsList, Activity a, Context cont, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        context = cont.getApplicationContext();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());

        fetch = Fetch.newInstance(context);

        ///  initialActivityLoad = true;

        this.mListener = null;
        mListener = (OnCourseListListener) cont;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.listview_row_courses, null);
        }

        title = (TextView) vi.findViewById(R.id.list_title); // title
        description = (TextView) vi.findViewById(R.id.list_desc); // description
        open = (Button) vi.findViewById(R.id.btn_listOpen); // open
        ratingAvgNum = (TextView) vi.findViewById(R.id.lbl_listAvgRating); //
        totalNum = (TextView) vi.findViewById(R.id.lbl_listTotalrating); //
        ratingStars = (RatingBar) vi.findViewById(R.id.list_rating);
        stars = (LayerDrawable) ratingStars.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffa500"), PorterDuff.Mode.SRC_ATOP);

        femalerating = (ProgressBar) vi.findViewById(R.id.female_progressbar); //
        malerating = (ProgressBar) vi.findViewById(R.id.male_progressbar); //
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image); //  image

        HashMap<String, String> material = new HashMap<>();
        material = data.get(position);

        // Setting all values in Fragm_Courses
        title.setText(material.get(Fragm_Courses.KEY_TITLE));
        description.setText(material.get(Fragm_Courses.KEY_DESCRIPTION));

        open.setText("Admission");
        open.setTag(material.get(Fragm_Courses.KEY_ID));
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(v.getTag().toString(), "admission");
            }
        });

        ratingAvgNum.setText("" + material.get(Fragm_Courses.KEY_RATING));
        ratingStars.setRating(Float.parseFloat((material.get(Fragm_Courses.KEY_RATING) == null) ? "2.2" : material.get(Fragm_Courses.KEY_RATING)));
        totalNum.setText(material.get(Fragm_Courses.KEY_TOTALNUM_RATING));
        femalerating.setProgress(Integer.parseInt("1"));
        //femalerating.setProgress(Integer.parseInt(material.get(Fragm_Courses.KEY_FEMALE_RATING)));
        malerating.setProgress(Integer.parseInt("1"));
        //malerating.setProgress(Integer.parseInt(material.get(Fragm_Courses.KEY_MALE_RATING)));
        imageLoader.DisplayImage(material.get(Fragm_Courses.KEY_THUMB_URL), thumb_image);
        return vi;
    }

    public void restorePreferences(Activity activity) {
        settings = activity.getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username", "");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl", "http://");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate", "");
        sys_password = settings.getString("pf_password", "");
        sys_usercouchId = settings.getString("pf_usercouchId", "");
        sys_userfirstname = settings.getString("pf_userfirstname", "");
        sys_userlastname = settings.getString("pf_userlastname", "");
        sys_usergender = settings.getString("pf_usergender", "");
        sys_uservisits = settings.getString("pf_uservisits", "");
        sys_servername = settings.getString("pf_server_name", " ");
        sys_serverversion = settings.getString("pf_server_version", " ");
    }

    public void buttonAction(String courseId, String action) {
        switch (action) {
            case "Open":
                Log.i(TAG, "Open Clicked ********** " + courseId);
                break;
            case "admission":
                restorePreferences(activity);
                admission(courseId);
                break;
        }
    }

    public void admission(String courseId){
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database members = manager.getDatabase("local_courses_admission");
            Document retrievedDocument = members.getExistingDocument(courseId);
            if (retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                if (properties.containsKey("members")) {
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    List<String> ext_ary_datesAdmit = (List<String>) properties.get("dateAdmitted");
                    List<String> ext_ary_members = (List<String>) properties.get("members");
                    if(!ext_ary_members.contains(sys_usercouchId)) {
                        ext_ary_members.add(sys_usercouchId);
                        newProperties.put("members", ext_ary_members);
                        Long tsLong = System.currentTimeMillis() / 1000;
                        String ts = tsLong.toString();
                        ext_ary_datesAdmit.add(ts);
                        newProperties.put("dateAdmitted", ext_ary_datesAdmit);
                        retrievedDocument.putProperties(newProperties);
                        Log.e(TAG, "Existing Doc but new entry - " +sys_usercouchId);
                    }else{
                        Log.e(TAG, "Already contains - " +sys_usercouchId);
                    }
                }
            }else{
                Document newDocument = members.getDocument(courseId);
                Map<String, Object> newProperties = new HashMap<>();
                List<String> ary_members = new ArrayList<>();
                List<String> ary_datesAdmit = new ArrayList<>();
                ary_members.add(sys_usercouchId);
                newProperties.put("members", ary_members);
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                ary_datesAdmit.add(ts);
                newProperties.put("dateAdmitted", ary_datesAdmit);
                newDocument.putProperties(newProperties);
                Log.e(TAG, "New entry - " +sys_usercouchId);
            }
            mListener.onCourseAdmission(courseId);

        } catch (Exception err) {
            Log.e("MyCouch", "local_courses_admission on device " + err.getMessage());
            err.printStackTrace();
        }

    }

    public interface OnCourseListListener {
        void onCourseAdmission(String CourseId);
    }

}