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

    public void showDialogProgress(){
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(activity,R.style.TransparentDialog);
        dialogB2.setView(R.layout.dialog_simulate_download_small);
        dialogB2.setCancelable(false);
        try {
            dialogDownloadProgress = dialogB2.create();
            // dialogDownloadProgress.ActivityIndicator(this, R.style.TransparentDialog);
            dialogDownloadProgress.show();
            downloadPB = (ProgressBar)dialogDownloadProgress.findViewById(R.id.progressBarDownloading);
            txtProgressStatus = (TextView) dialogDownloadProgress.findViewById(R.id.txtProgressStatus);
            txtProgressStatus.setText("Downloading, please wait");
            //downloadPB.setScaleY(3f);

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void alertDialogOkay(String Message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
        builder1.setMessage(Message);
        builder1.setCancelable(true);
        builder1.setNegativeButton("Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void alertCompletedDownload(String Message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
        builder1.setMessage(Message);
        builder1.setCancelable(true);
        builder1.setNegativeButton("Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCourseDownloadCompleted(OneByOneResTitle, resIDArrayList);
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
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

    public void createCourseResourceDoc(String manualResId, String manualResTitle, String manualResopenWith) {
        Database database = null;
        try {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase("offline_course_resources");

            ///Document doc = database.getExistingDocument(manualResId);
            //Map<String, Object> existing_properties = doc.getProperties();
            //if(existing_properties == null){
            Log.e(TAG, "File does not exist");
            Map<String, Object> properties = new HashMap<>();
            properties.put("title", manualResTitle);
            properties.put("openWith", manualResopenWith);
            properties.put("localfile", "yes");
            // properties.put("resourceType", manualResType);
            Document document = database.getDocument(manualResId);
            try {
                document.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Cannot save document", e);
            }
            // }else{
            //     Log.e("MyCouch", "File already exist");
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
            } catch (Exception e) {
                Log.e(TAG, "Cannot course details in offline courses" + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Create Course error " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void buttonAction(String courseId, String action) {
        switch (action) {
            case "Open":
                mListener.onTakeCourseOpen(courseId);
                Log.i(TAG, "Open Clicked ********** " + courseId);
                break;
            case "admission":
                restorePreferences(activity);
                admission(courseId);
               // downloadCourseResources(courseId);
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
        } catch (Exception err) {
            Log.e("MyCouch", "local_courses_admission on device " + err.getMessage());
            err.printStackTrace();
        }

    }

    public void downloadCourseResources(String courseId) {
        try {
            resIDArrayList.clear();
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database coursestep_Db = manager.getExistingDatabase("coursestep");
            Query orderedQuery = chViews.ReadCourseSteps(coursestep_Db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            courseStepsCounter = 0;
            for (Iterator<QueryRow> item = results; item.hasNext(); ) {
                QueryRow row = item.next();
                String docId = (String) row.getValue();
                Document doc = coursestep_Db.getExistingDocument(docId);
                Map<String, Object> coursestep_properties = doc.getProperties();
                if (courseId.equals((String) coursestep_properties.get("courseId"))) {
                    ArrayList resourceList = (ArrayList<String>) coursestep_properties.get("resourceId");
                    for (int cnt = 0; cnt < resourceList.size(); cnt++) {
                        if (!resIDArrayList.contains(String.valueOf(resourceList.get(cnt)))) {
                            resIDArrayList.add(String.valueOf(resourceList.get(cnt)));
                        }
                    }
                    downloadingCourseTitle = ((String) coursestep_properties.get("title"));
                    Log.e(TAG, "Course Step title " + downloadingCourseTitle);
                    courseStepsCounter++;
                }
            }
            fetch.removeRequests();
            if (resIDArrayList.size() > 0) {
                OneByOneCourseId = courseId;
                showDialogProgress();
                downloadCompleted=false;
                new FetchCompileDownload().execute();
            } else {

            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    class FetchCompileDownload extends AsyncTask<String, Void, Boolean> {

        private Exception exception;

        protected Boolean doInBackground(String... urls) {
            try {
                for (int x = 0; x < resIDArrayList.size(); x++) {
                    OneByOneResID = resIDArrayList.get(x);
                    try {
                        URI uri = URI.create(sys_oldSyncServerURL);
                        String url_Scheme = uri.getScheme();
                        String url_Host = uri.getHost();
                        int url_Port = uri.getPort();
                        String url_user = null, url_pwd = null;
                        if (sys_oldSyncServerURL.contains("@")) {
                            String[] userinfo = uri.getUserInfo().split(":");
                            url_user = userinfo[0];
                            url_pwd = userinfo[1];
                        }
                        CouchDbClientAndroid dbClient = new CouchDbClientAndroid("resources", true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                        Log.e(TAG, "Has resources -- " + OneByOneResID);
                        if (dbClient.contains(OneByOneResID)) {
                            /// Handle with Json
                            JsonObject jsonObject = dbClient.find(JsonObject.class, OneByOneResID);
                            JsonObject jsonAttachments = jsonObject.getAsJsonObject("_attachments");
                            final String openWith = (String) jsonObject.get("openWith").getAsString();
                            final String title = jsonObject.get("title").getAsString();
                            OneByOneResTitle = title;
                            Log.e(TAG, "Open With -- " + openWith);
                            if (!openWith.equalsIgnoreCase("HTML")) {
                                JSONObject _attachments = new JSONObject(jsonAttachments.toString());
                                Iterator<String> keys = _attachments.keys();
                                if (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    Log.e(TAG, "-- " + key);
                                    final String encodedkey = URLEncoder.encode(key, "utf-8");
                                    String extension = encodedkey.substring(encodedkey.lastIndexOf('.'));
                                    final String diskFileName = OneByOneResID + extension;
                                    String root = Environment.getExternalStorageDirectory().toString();
                                    File dirPath = new File(root + "/ole_temp");
                                    String downloadURL = sys_oldSyncServerURL + "/resources/" + OneByOneResID + "/" + encodedkey;
                                    Request request = new Request(downloadURL, dirPath.getAbsolutePath(), diskFileName);
                                    requests.add(request);
                                    ///Save Resource Fully Downloaded Document
                                    createCourseResourceDoc(OneByOneResID, title, openWith);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Fetch -- Download this resource error " + e.getMessage());
                        ///////////// mListener.onCourseDownloadingProgress(OneByOneResTitle,"Please Wait","Downloading item");
                        /// mDialog.dismiss();
                        alertDialogOkay(" Error downloading file, check connection and try again");
                        e.printStackTrace();
                    }
                }

                try{
                    fetch.enqueue(requests);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            downloadPB.setProgress(0);
                        }
                    });
                }catch (Exception err){
                    downloadCompleted=false;
                    dialogDownloadProgress.dismiss();
                }
                fetch.addFetchListener(new FetchListener() {
                    @Override
                    public void onUpdate(long id, int status, int progress, long downloadedBytes, long fileSize, int error) {
                        if (status == Fetch.STATUS_DOWNLOADING) {
                            downloadPB.setProgress(progress);
                            txtProgressStatus.setText(progress +"/ 100");
                        } else if (status == Fetch.STATUS_DONE) {
                            downloadPB.setProgress(100);
                            try {
                                dialogDownloadProgress.dismiss();
                                if(!downloadCompleted){
                                    downloadCompleted=true;
                                    alertCompletedDownload("Download completed successfully");
                                    createCourseDoc(OneByOneCourseId, courseStepsCounter);
                                }
                            }catch (Exception err){
                            }
                            fetch.release();
                            fetch = Fetch.newInstance(context);
                        } else if (error != Fetch.NO_ERROR) {
                            //An error occurred
                            dialogDownloadProgress.dismiss();
                            Log.e(TAG, " Down Error No " + error);
                            fetch.release();
                            fetch = Fetch.newInstance(context);
                            if (error == Fetch.ERROR_HTTP_NOT_FOUND) {
                                //handle error
                            }
                        }
                    }
                });
                return true;
            } catch (Exception e) {
                this.exception = e;
                return false;
            }
        }

        protected void onPostExecute() {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

    public void downloadWithDownloadManagerSingleFile(String fileURL, String FileName) {
        String url = fileURL;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(OneByOneResID + "-" + OneByOneResTitle);
        request.setTitle(OneByOneResTitle);
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        Log.e(TAG, " Destination is " + FileName);
        request.setDestinationInExternalPublicDir("ole_temp", FileName);
        // get download service and enqueue file
        mDialog.setMessage("Downloading  \" " + OneByOneResTitle + " \" . please wait...");
        mListener.onCourseDownloadingProgress(OneByOneResTitle, "Please Wait", "Downloading item");
        downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = downloadManager.enqueue(request);
    }

    public interface OnCourseListListener {
        void onTakeCourseOpen(String CourseId);
        void onCourseDownloadCompleted(String CourseId, Object data);
        void onCourseDownloadingProgress(String itemTitle, String status, String message);
    }

}