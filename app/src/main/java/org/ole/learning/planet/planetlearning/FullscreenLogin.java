package org.ole.learning.planet.planetlearning;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.lightcouch.CouchDbClientAndroid;

import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressWarnings("ALL")
public class FullscreenLogin extends Activity {
    private View mContentView;
    private static final int REQUEST_READ_CONTACTS = 0;
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    CouchViews chViews = new CouchViews();
    String doc_lastVisit;
    private EditText mUsername;
    private EditText mPasswordView;
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion, sys_NewDate = "";
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    Boolean sys_appInDemoMode = false;
    Object[] sys_membersWithResource;
    int sys_uservisits_Int;
    String Serverdate = null;
    private Dialog dialog, promptDialog;
   /// private ProgressDialog mDialog;
    JSONObject jsonServerData;
    final Context context = this;
    String[] databaseList = {"members", "meetups", "usermeetups", "assignments",
            "assignmentpaper", "courseanswer", "coursequestion", "courses","coursestep", "courseschedule", "membercourseprogress",
            "calendar", "groups", "invitations", "configurations", "requests", "shelf", "languages"};
    Replication[] push = new Replication[databaseList.length];
    Replication[] pull = new Replication[databaseList.length];
    Database[] db = new Database[databaseList.length];
    Manager[] manager = new Manager[databaseList.length];
    boolean syncmembers, openMemberList = false;
    int syncCnt = 0;
    AndroidContext androidContext;
    Database database;
    Replication pullReplication;
    Button dialogSyncButton, btnFeedback;
    Boolean wipeClean = false;
    Intent serviceIntent;
    private static final String TAG = "MYAPP";
    Button SignInButton;
    private static final int REQUEST_WRITE_STORAGE = 112;
    Dialog dialogDownloadProgress;
    Dialog dialogFeedbackProgress;
    ProgressBar downloadPB;
    ProgressBar downloadFeedbackPB;
    TextView txtFeedbackProgressStatus;
    TextView txtProgressStatus;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_login);
        mContentView = findViewById(R.id.fullscreen_content2);
        //final ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();
        androidContext = new AndroidContext(this);
        // Todo - : Decide on either to clear resource database and file storage anytime user syncs or rather keep old resources only if user doesn't change server url
        mUsername = (EditText) mContentView.findViewById(R.id.txtUsername);
        mPasswordView = (EditText) findViewById(R.id.txtPassword);

        SignInButton = (Button) findViewById(R.id.btnSignIn);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSystemInfo()) {
                    if (authenticateUser()) {
                        if (updateActivityLog()) {
                            Intent intent = new Intent(context, Main_Dashboard.class);
                           // Intent intent = new Intent(context, User_Dashboard.class);
                            //Intent intent = new Intent(context, FullscreenActivity.class);
                            Log.e(TAG, "Opening Dashboard");
                            startActivity(intent);
                        } else {
                            alertDialogOkay("System Error. Please contact system manager");
                        }
                    } else {
                        alertDialogOkay("Login incorrect / Not found. Check and try again.");
                    }
                } else {
                    alertDialogOkay("Device not linked to a cummunity/nation. Use the setting button to sync with community/nation.");
                }
            }
        });
        // New UI
        Button DemoModeButton = (Button) findViewById(R.id.btnBecomeMember);
        DemoModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /// alertDialogOkay("This feature has not been activated on this version.");
                /// Demo Mode Code
                sys_appInDemoMode = true;
                mUsername.setText("learner");
                mPasswordView.setText("learner");
                ///  DemoDataLoader demoDataLoader = new DemoDataLoader(context, SignInButton);
                //alertDialogOkay("Device configured for Demo. Click Sign-in");

                ///Intent intent = new Intent(context, Home.class);
                //startActivity(intent);
            }
        });
        Button Language = (Button) findViewById(R.id.btnLanguage);
        Language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //alertDialogOkay("This feature has not been activated on this version.");
                setLocale("fr");
                //Intent intent = new Intent(context, Fragm_Library.class);
                //Intent intent = new Intent(context, User_Dashboard.class);
                //startActivity(intent);
            }
        });
        //

        TextView Version = (TextView) findViewById(R.id.txtVersion);
        Version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //alertDialogOkay("This feature has not been activated on this version.");
                //setLocale("fr");
                Intent intent = new Intent(context, Main_Dashboard.class);
                startActivity(intent);
            }
        });
        //


        // New UI
        ImageButton SetupButton = (ImageButton) findViewById(R.id.btnSetup);
        SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSyncURLDialog();
            }
        });
        //

        restorePref();

        ////startServiceCommand();
        btnFeedback = (Button) findViewById(R.id.btnFeedback);
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Fuel ful = new Fuel();
                showFeedbackProgress();
                ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                    @Override
                    public void success(Request request, Response response, String s) {
                        try {
                            List<String> myList = new ArrayList<String>();
                            myList.clear();
                            myList = Arrays.asList(s.split(","));
                            Log.e(TAG, "-- " + myList.size());
                            if (myList.size() < 8) {
                                alertDialogOkay("Check WiFi connection and try again");
                                dialogFeedbackProgress.dismiss();
                            } else {
                                sendfeedbackToServer();
                            }

                        } catch (Exception e) {
                            dialogFeedbackProgress.dismiss();
                            alertDialogOkay("Device couldn't reach server. Check and try again");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(Request request, Response response, FuelError fuelError) {
                        dialogFeedbackProgress.dismiss();
                        alertDialogOkay("Device couldn't reach server. Check and try again");
                        Log.e(TAG, " " + fuelError);
                    }
                });
            }
        });

        boolean hasPermission = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        boolean isAppInstalled = appInstalledOrNot("com.adobe.reader");
        if(!isAppInstalled) {
            alertInstallAdobeReader("This application requires adobe reader. Click continue to install it");
        }else{

        }
    }
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features what required the permission
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void sendfeedbackToServer() {
        Database resourceRating, activitylog, visits;
        try {
            // Get server date for activitylog update
            GetServerDate gtSvDt = new GetServerDate();
            gtSvDt.setdbName("activitylog");
            gtSvDt.setView("date_now");
            gtSvDt.execute("");
        } catch (Exception err) {
            dialogFeedbackProgress.dismiss();
            alertDialogOkay("Device can not send feedback data. Check connection to server and try again");
            Log.e(TAG, "Getting date from server" + err);
        }

        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            resourceRating = manager.getDatabase("resourcerating");
            Query orderedQuery = chViews.ReadResourceRatingByIdView(resourceRating).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = resourceRating.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                int sum = ((int) properties.get("sum"));
                int timesRated = ((Integer) properties.get("timesRated"));
                // Update server resources with new ratings
                UpdateResourceDocument nwUpdateResDoc = new UpdateResourceDocument();
                nwUpdateResDoc.setResourceId(docId);
                nwUpdateResDoc.setSum(sum);
                nwUpdateResDoc.setTimesRated(timesRated);
                nwUpdateResDoc.execute("");
            }
            Database dbResources = manager.getDatabase("resourcerating");
            dbResources.delete();
        } catch (Exception err) {
            dialogFeedbackProgress.dismiss();
            alertDialogOkay("Device can not send feedback data. Check connection to server and try again");
            Log.e(TAG, "reading resource rating error " + err);
        }
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            visits = manager.getDatabase("visits");
            Query orderedQuery = chViews.ReadMemberVisitsId(visits).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = visits.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                int numOfVisits = ((Integer) properties.get("noOfVisits"));
                Log.e(TAG, "Number Of visits for " + docId + " is " + numOfVisits);
                // Update server members with visits
                UpdateMemberVisitsDocument nwUpdateMemberVisits = new UpdateMemberVisitsDocument();
                nwUpdateMemberVisits.setMemberId(docId);
                nwUpdateMemberVisits.setSumVisits(numOfVisits);
                nwUpdateMemberVisits.execute("");
            }
            Database visitsdb = manager.getDatabase("visits");
            visitsdb.delete();
        } catch (Exception err) {
            dialogFeedbackProgress.dismiss();
            alertDialogOkay("Device can not send feedback data. Check connection to server and try again");
            Log.e(TAG, "reading visits error " + err);
        }
        //// Read activitylog database details
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activitylog = manager.getDatabase("activitylog");
            @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String m_WLANMAC = wm.getConnectionInfo().getMacAddress();
            Document doc = activitylog.getExistingDocument(m_WLANMAC);
            Map<String, Object> properties = doc.getProperties();
            // Update server resources with new ratings
            UpdateActivityLogDatabase nwActivityLog = new UpdateActivityLogDatabase();
            if (properties.get("female_visits") != null) {
                nwActivityLog.set_female_visits(((Integer) properties.get("female_visits")));
            } else {
                nwActivityLog.set_female_visits(0);
            }
            if (properties.get("male_visits") != null) {
                nwActivityLog.set_male_visits(((Integer) properties.get("male_visits")));
            } else {
                nwActivityLog.set_male_visits(0);
            }
        } catch (Exception err) {
            dialogFeedbackProgress.dismiss();
            alertDialogOkay("Device can not send feedback data. Check connection to server and try again");
            Log.e(TAG, "reading activity log " + err);
        }

        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            resourceRating = manager.getDatabase("resourcerating");
            Query orderedQuery = chViews.ReadResourceRatingByIdView(resourceRating).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = resourceRating.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                int sum = ((int) properties.get("sum"));
                int timesRated = ((Integer) properties.get("timesRated"));
                // Update server resources with new ratings
                UpdateResourceDocument nwUpdateResDoc = new UpdateResourceDocument();
                nwUpdateResDoc.setResourceId(docId);
                nwUpdateResDoc.setSum(sum);
                nwUpdateResDoc.setTimesRated(timesRated);
                nwUpdateResDoc.execute("");
            }
            Database dbResources = manager.getDatabase("resourcerating");
            dbResources.delete();
        } catch (Exception err) {
            dialogFeedbackProgress.dismiss();
            alertDialogOkay("Device can not send feedback data. Check connection to server and try again");
            Log.e(TAG, "reading resource rating error " + err);
        }
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            visits = manager.getDatabase("visits");
            Query orderedQuery = chViews.ReadMemberVisitsId(visits).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = visits.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                int numOfVisits = ((Integer) properties.get("noOfVisits"));
                Log.e(TAG, "Number Of visits for " + docId + " is " + numOfVisits);
                // Update server members with visits
                UpdateMemberVisitsDocument nwUpdateMemberVisits = new UpdateMemberVisitsDocument();
                nwUpdateMemberVisits.setMemberId(docId);
                nwUpdateMemberVisits.setSumVisits(numOfVisits);
                nwUpdateMemberVisits.execute("");
            }
            Database dbResources = manager.getDatabase("visits");
            dbResources.delete();
        } catch (Exception err) {
            dialogFeedbackProgress.dismiss();
            alertDialogOkay("Device can not send feedback data. Check connection to server and try again");
            Log.e(TAG, "reading visits error " + err);
        }
        //// Read activitylog database details
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activitylog = manager.getDatabase("activitylog");
            @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String m_WLANMAC = wm.getConnectionInfo().getMacAddress();
            Document doc = activitylog.getExistingDocument(m_WLANMAC);
            Map<String, Object> properties = doc.getProperties();
            // Update server resources with new ratings
            UpdateActivityLogDatabase nwActivityLog = new UpdateActivityLogDatabase();
            if (properties.get("female_visits") != null) {
                nwActivityLog.set_female_visits(((Integer) properties.get("female_visits")));
            } else {
                nwActivityLog.set_female_visits(0);
            }
            if (properties.get("male_visits") != null) {
                nwActivityLog.set_male_visits(((Integer) properties.get("male_visits")));
            } else {
                nwActivityLog.set_male_visits(0);
            }
            nwActivityLog.set_female_opened((ArrayList) properties.get("female_opened"));
            nwActivityLog.set_female_rating(((ArrayList) properties.get("female_rating")));
            nwActivityLog.set_female_timesRated(((ArrayList) properties.get("female_timesRated")));
            nwActivityLog.set_male_opened(((ArrayList) properties.get("male_opened")));
            nwActivityLog.set_male_rating(((ArrayList) properties.get("male_rating")));
            nwActivityLog.set_male_timesRated(((ArrayList) properties.get("male_timesRated")));
            nwActivityLog.set_resources_names(((ArrayList) properties.get("resources_names")));
            nwActivityLog.set_resources_opened(((ArrayList) properties.get("resources_opened")));
            nwActivityLog.set_resourcesIds(((ArrayList) properties.get("resourcesIds")));
            nwActivityLog.execute("");
           // dialogFeedbackProgress.dismiss();

            sendCourseToServer();
            //updateUI();
        } catch (Exception err) {
            updateUI();
            dialogFeedbackProgress.dismiss();
            alertDialogOkay("Device can not send feedback data. Check connection to server and try again");
            Log.e(TAG, "reading activity log error " + err);
        }


    }
    public void sendCourseToServer() {
        try{
            UpdateCoursesDatabase updateCoursesDatabase = new UpdateCoursesDatabase();
            updateCoursesDatabase.execute();
        }catch(Exception err){
            Log.e(TAG,"Course database update error "+err.getMessage());
            alertDialogOkay("Updating member course progress error. Check connection and try again");
            err.printStackTrace();
        }

    }
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, FullscreenLogin.class);
        startActivity(refresh);
        finish();
    }
    ////////
    private void TestConnectionToServer(String textURL) {
        //mDialog = new ProgressDialog(context);
        //mDialog.setMessage("Please wait. Connecting to server...");
        //mDialog.setCancelable(false);
        //mDialog.show();
        showDialogProgress();
        final Fuel ful = new Fuel();
        ful.get(textURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
            @Override
            public void success(Request request, Response response, String s) {
                try {
                    List<String> myList = new ArrayList<String>();
                    myList.clear();
                    myList = Arrays.asList(s.split(","));
                    if (myList.size() < 8) {
                        dialogDownloadProgress.dismiss();
                        alertDialogOkay("Check the server address again. What i connected to wasn't the BeLL Server");
                        dialogSyncButton.setVisibility(View.INVISIBLE);
                    } else {
                        dialogDownloadProgress.dismiss();
                        alertDialogOkay("Test successful. You can now click on \"Save and Proceed\" ");
                        dialogSyncButton.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(Request request, Response response, FuelError fuelError) {
                dialogDownloadProgress.dismiss();
                alertDialogOkay("Device couldn't reach server. Check and try again");
                dialogSyncButton.setVisibility(View.INVISIBLE);
                Log.e(TAG, " " + fuelError);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public boolean authenticateUser() {
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        /// getSystemInfo();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("members");
            Query orderedQuery = chViews.CreateLoginByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                String doc_loginId = (String) properties.get("login");
                String doc_password = (String) properties.get("password");
                if (doc_password == "" && !mPasswordView.getText().toString().equals("")) {
                    if (mUsername.getText().toString().equals(doc_loginId)) {
                        try {
                            Map<String, Object> doc_credentials = (Map<String, Object>) properties.get("credentials");
                            AndroidDecrypter adc = new AndroidDecrypter();
                            SharedPreferences.Editor editor = settings.edit();
                            String ServerName = settings.getString("pf_server_code", "");

                            Log.e(TAG, " User Login  " + (String) properties.get("login"));
                            Log.e(TAG, " User id  " + (String) properties.get("_id"));
                            Log.e(TAG, " User ServerName  " + ServerName);
                            Log.e(TAG, " User Value  " + doc_credentials.get("value").toString());
                            Log.e(TAG, " User Community  " + (String) properties.get("community"));

                            if (adc.AndroidDecrypter(doc_loginId, mPasswordView.getText().toString(), doc_credentials.get("value").toString())) {
                                Log.e(TAG, " Got Here 1");
                                if (ServerName.equalsIgnoreCase((String) properties.get("community"))) {
                                    Log.e(TAG, " Got Here 2");
                                    editor.putString("pf_username", (String) properties.get("login"));
                                    editor.putString("pf_password", (String) properties.get("password"));
                                    editor.putString("pf_usercouchId", (String) properties.get("_id"));
                                    editor.putString("pf_userfirstname", (String) properties.get("firstName"));
                                    editor.putString("pf_userlastname", (String) properties.get("lastName"));
                                    editor.putString("pf_usergender", (String) properties.get("Gender"));
                                    editor.putBoolean("pf_appindemomode", sys_appInDemoMode);
                                    sys_usergender = (String) properties.get("Gender");
                                    try {
                                        String noOfVisits = properties.get("visits").toString();
                                        int currentTotalVisits = Integer.parseInt(noOfVisits) + totalVisits((String) properties.get("_id"));
                                        editor.putInt("pf_uservisits_Int", currentTotalVisits);
                                        editor.putString("pf_uservisits", currentTotalVisits + "");
                                        editor.putString("pf_lastVisitDate", doc_lastVisit);
                                    } catch (Exception err) {
                                        alertDialogOkay(err.getMessage());
                                        err.printStackTrace();
                                    }
                                    Set<String> stgSet = settings.getStringSet("pf_userroles", new HashSet<String>());
                                    ArrayList roleList = (ArrayList<String>) properties.get("roles");
                                    for (int cnt = 0; cnt < roleList.size(); cnt++) {
                                        stgSet.add(String.valueOf(roleList.get(cnt)));
                                    }
                                    editor.putStringSet("pf_userroles", stgSet);
                                    editor.commit();
                                    Log.e("MYAPP", " RowChipsView Login Id: " + doc_loginId + " Password: " + doc_password);
                                    restorePref();
                                    return true;
                                }
                            }

                        } catch (Exception err) {
                            Log.e("MYAPP", " Encryption Err  " + err.getMessage());
                            err.printStackTrace();
                            return false;
                        }
                    }
                } else if (mUsername.getText().toString().equals(doc_loginId)) {
                    Log.e(TAG, " Got Here 3");
                    if (mPasswordView.getText().toString().equals(doc_password) && !properties.containsKey("credentials")) {
                        Log.e(TAG, " Got Here 4");
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("pf_username", (String) properties.get("login"));
                        editor.putString("pf_password", (String) properties.get("password"));
                        editor.putString("pf_usercouchId", (String) properties.get("_id"));
                        editor.putString("pf_userfirstname", (String) properties.get("firstName"));
                        editor.putString("pf_userlastname", (String) properties.get("lastName"));
                        editor.putString("pf_usergender", (String) properties.get("Gender"));
                        editor.putBoolean("pf_appindemomode", sys_appInDemoMode);
                        sys_usergender = (String) properties.get("Gender");
                        try {
                            String noOfVisits = properties.get("visits").toString();
                            int currentTotalVisits = Integer.parseInt(noOfVisits) + totalVisits((String) properties.get("_id"));
                            editor.putInt("pf_uservisits_Int", currentTotalVisits);
                            editor.putString("pf_uservisits", currentTotalVisits + "");
                            editor.putString("pf_lastVisitDate", doc_lastVisit);

                        } catch (Exception err) {
                            Log.e("MYAPP", "Pref Error " + err.getMessage());
                        }
                        Set<String> stgSet = settings.getStringSet("pf_userroles", new HashSet<String>());
                        ArrayList roleList = (ArrayList<String>) properties.get("roles");
                        for (int cnt = 0; cnt < roleList.size(); cnt++) {
                            stgSet.add(String.valueOf(roleList.get(cnt)));
                        }
                        editor.putStringSet("pf_userroles", stgSet);
                        editor.commit();
                        Log.e("MYAPP", " RowChipsView Login OLD encryption: " + doc_loginId + " Password: " + doc_password);
                        return true;
                    } else {
                        return false;
                    }
                }

            }
            db.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getSystemInfo() {
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("configurations");
            Query orderedQuery = chViews.LocalServerInfo(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                String Server_name = (String) properties.get("name");
                String Server_nationName = (String) properties.get("nationName");
                String Server_version = (String) properties.get("version");
                String Server_code = (String) properties.get("code");

                //////alertDialogOkay(Server_name+" Names");
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pf_server_name", Server_name);
                editor.putString("pf_server_nation", Server_nationName);
                editor.putString("pf_server_version", Server_version);
                editor.putString("pf_server_code", Server_code);
                editor.commit();
            }
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public int totalVisits(String memberId) {
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        Database visitHolder;
        int doc_noOfVisits;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            visitHolder = manager.getDatabase("visits");
            Document retrievedDocument = visitHolder.getExistingDocument(memberId);
            if (retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                if (properties.containsKey("noOfVisits")) {
                    doc_noOfVisits = (int) properties.get("noOfVisits");
                    doc_lastVisit = (String) properties.get("lastVisits");
                    /// Increase No Of visits by 1
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    doc_noOfVisits += 1;
                    newProperties.put("noOfVisits", doc_noOfVisits);
                    newProperties.put("lastVisits", todaysDate());
                    retrievedDocument.putProperties(newProperties);
                    return doc_noOfVisits;
                }
            } else {
                Document newdocument = visitHolder.getDocument(memberId);
                Map<String, Object> newProperties = new HashMap<String, Object>();
                newProperties.put("noOfVisits", 1);
                doc_lastVisit = todaysDate();
                newProperties.put("lastVisits", doc_lastVisit);
                newdocument.putProperties(newProperties);
                return 1;
            }
        } catch (Exception err) {
            Log.e("MyCouch", "Error - Updating Visits Database  : " + err.getMessage());
        }
        return -1;
    }

    public boolean updateActivityLog() {
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        Database activityLog;
        int genderVisits;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activityLog = manager.getDatabase("activitylog");
            @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            //This is for setting the MAC address if it is being run in a android emulator.
            String m_WLANMAC;
            m_WLANMAC = wm.getConnectionInfo().getMacAddress();
            if (m_WLANMAC == null) {
                m_WLANMAC = "mymac";
            }

            Document retrievedDocument = activityLog.getDocument(m_WLANMAC);
            if (retrievedDocument != null) {
                if (retrievedDocument.getProperties() != null) {
                    Map<String, Object> properties = retrievedDocument.getProperties();
                    if (properties.containsKey(sys_usergender.toLowerCase() + "_visits")) {
                        genderVisits = (int) properties.get(sys_usergender.toLowerCase() + "_visits");
                        Map<String, Object> newProperties = new HashMap<String, Object>();
                        newProperties.putAll(retrievedDocument.getProperties());
                        newProperties.put(sys_usergender.toLowerCase() + "_visits", (genderVisits + 1));
                        retrievedDocument.putProperties(newProperties);
                        return true;
                    }
                    return true;
                } else {
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.put(sys_usergender.toLowerCase() + "_visits", 1);
                    retrievedDocument.putProperties(newProperties);
                    return true;
                }
            } else {
                Document newvistsdocument = activityLog.getDocument(m_WLANMAC);
                Map<String, Object> newvisitsProperties = new HashMap<String, Object>();
                newvisitsProperties.put(sys_usergender.toLowerCase() + "_visits", 1);
                newvistsdocument.putProperties(newvisitsProperties);
                return true;
            }
        } catch (Exception err) {
            Log.e("MyCouch", "Updating Activity Log : " + err.toString());
            err.printStackTrace();
            return false;
        }
    }

    public String todaysDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        Log.e("MyCouch", "System Date : " + dateFormat.format(cal.getTime()));
        return dateFormat.format(cal.getTime());

    }

    public void getSyncURLDialog() {
        @SuppressLint("WifiManagerLeak") final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        AlertDialog.Builder dialogB = new AlertDialog.Builder(this);
        dialogB.setView(R.layout.dialog_setup);
        dialogB.setCancelable(true);
        dialog = dialogB.create();
        dialog.show();
        final EditText txtSuncURL = (EditText) dialog.findViewById(R.id.txtNewSyncURL);
        txtSuncURL.setText(sys_oldSyncServerURL);
        Button testConnButton = (Button) dialog.findViewById(R.id.btnTestCnnection);
        testConnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestConnectionToServer(txtSuncURL.getText().toString());
            }
        });

        CheckBox chBox = (CheckBox) dialog.findViewById(R.id.chWipeClean);
        chBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wipeClean = true;
                } else {
                    wipeClean = false;
                }
                Log.e(TAG, " Wipe clean is now " + isChecked);
            }
        });

        ////
        sys_singlefilestreamdownload = settings.getBoolean("pf_singlefilestreamdownload", true);
        sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload", true);

        dialogSyncButton = (Button) dialog.findViewById(R.id.btnNewSaveSyncURL);
        dialogSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sys_oldSyncServerURL = txtSuncURL.getText().toString();
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pf_sysncUrl", sys_oldSyncServerURL);
                editor.commit();
                dialog.dismiss();
               //mDialog = new ProgressDialog(context);
                //mDialog.setMessage("Please wait...");
                //mDialog.setCancelable(false);

                try {
                    showDialogProgress();
                    //mDialog.show();
                    syncNotifier();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialogSyncButton.setVisibility(View.INVISIBLE);
        if (wifiManager.isWifiEnabled()) {
            dialog.show();
            ////
        } else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface int_dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            wifiManager.setWifiEnabled(true);
                            dialog.show();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            finish();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Wifi is off. Are you sure you want to turn it on?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    public void restorePref() {
        // Restore preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
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

        if (sys_username != "") {
            mUsername.setText(sys_username);
        } else {
            mUsername.setText("");
        }
        Set<String> mwr = settings.getStringSet("membersWithResource", null);
        try {
            if(mwr!=null){
                sys_membersWithResource = mwr.toArray();
            }else {
                sys_membersWithResource = null;
            }

        } catch (Exception err) {
            Log.e(TAG, " Error creating  sys_membersWithResource ");
            err.printStackTrace();
        }
        try {
            //           serviceIntent = new Intent(context, ServerSearchService.class);
//            context.stopService(serviceIntent);
        } catch (Exception error) {
            Log.e(TAG, " Creating Service error " + error.getMessage());
        }
    }

    public void showDialogProgress(){
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(context,R.style.TransparentDialog);
        dialogB2.setView(R.layout.dialog_simulate_download_small);
        dialogB2.setCancelable(false);
        try {
            dialogDownloadProgress = dialogB2.create();
           // dialogDownloadProgress.ActivityIndicator(this, R.style.TransparentDialog);
            dialogDownloadProgress.show();
            downloadPB = (ProgressBar)dialogDownloadProgress.findViewById(R.id.progressBarDownloading);
            txtProgressStatus = (TextView) dialogDownloadProgress.findViewById(R.id.txtProgressStatus);
            txtProgressStatus.setText("Please wait");
            //downloadPB.setScaleY(3f);

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void showFeedbackProgress(){
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(context,R.style.TransparentDialog);
        dialogB2.setView(R.layout.dialog_simulate_download_small);
        dialogB2.setCancelable(false);
        try {
            dialogFeedbackProgress = dialogB2.create();
            dialogFeedbackProgress.show();
            downloadFeedbackPB= (ProgressBar)dialogFeedbackProgress.findViewById(R.id.progressBarDownloading);
            txtFeedbackProgressStatus = (TextView) dialogFeedbackProgress.findViewById(R.id.txtProgressStatus);
            txtFeedbackProgressStatus.setText("Please wait");
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    /*
    public void startServiceCommand() {
        try {
            serviceIntent = new Intent(context, ServerSearchService.class);
            serviceIntent.putExtra("serverUrl", sys_oldSyncServerURL);
            context.startService(serviceIntent);
        } catch (Exception err) {
            Log.e("MYAPP", " Creating Service error " + err.getMessage());
        }
    }
    */

    public void syncNotifier() {
        emptyAllDbs();
        try {
/// Todo Decide either use design document in apps or not
            JsonObject filterContent = new JsonObject();
            filterContent.addProperty("by_resource", "function(doc, req){return doc._id === req.query._id;}");
            RunCreateDocTask newRunCreateFilterTask = new RunCreateDocTask();
            newRunCreateFilterTask.setDbName("resources");
            newRunCreateFilterTask.setDocNameId("_design/apps");
            newRunCreateFilterTask.setSyncServerURL(sys_oldSyncServerURL);
            newRunCreateFilterTask.setCategory("filters");
            newRunCreateFilterTask.setViewContent(filterContent);
            newRunCreateFilterTask.execute("");

        } catch (Exception err) {
            Log.e(TAG, err.getMessage());
        }
        try {

            JsonObject viewContent = new JsonObject();
            viewContent.addProperty("map", "function() { var now = new Date().toLocaleDateString(); " +
                    "var output = JSON.parse(JSON.stringify(now)); emit(output, output); }");
            JsonObject dateViewContent = new JsonObject();
            dateViewContent.add("date_now", viewContent);
            RunCreateDocTask newRunCreateViewTask = new RunCreateDocTask();
            newRunCreateViewTask.setDbName("activitylog");
            newRunCreateViewTask.setDocNameId("_design/apps");
            newRunCreateViewTask.setSyncServerURL(sys_oldSyncServerURL);
            newRunCreateViewTask.setCategory("views");
            newRunCreateViewTask.setViewContent(dateViewContent);
            newRunCreateViewTask.execute("");

        } catch (Exception err) {
            Log.e(TAG, err.getMessage());
        }
        ///ResourcesJson rsj = new ResourcesJson();
       ///// if (rsj.ResourcesJson(sys_oldSyncServerURL, "resources", androidContext)) {
            startSyncProcess();
        ///}
        ///// End creating design Document
    }

    public void emptyAllDbs() {
        if (wipeClean) {
            for (int cnt = 0; cnt < databaseList.length; cnt++) {
                try {
                    Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                    Database db = manager.getDatabase(databaseList[cnt]);
                    Log.e(TAG, "Deleting " + databaseList[cnt]);
                    db.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //// Delete Device Created Databases
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbResources = manager.getDatabase("resources");
                dbResources.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbResources = manager.getDatabase("shadowresources");
                dbResources.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbOffline_courses = manager.getDatabase("offline_courses");
                dbOffline_courses.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbcourses_admission = manager.getDatabase("local_courses_admission");
                dbcourses_admission.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbLocalMemCourseProg = manager.getDatabase("local_member_course_progress");
                dbLocalMemCourseProg.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbLocalMemCourseAns = manager.getDatabase("local_member_course_answer");
                dbLocalMemCourseAns.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbOffline_course_resources = manager.getDatabase("offline_course_resources");
                dbOffline_course_resources.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbResource_rating = manager.getDatabase("resourcerating");
                dbResource_rating.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbActivitylog = manager.getDatabase("activitylog");
                dbActivitylog.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }

            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbVisits = manager.getDatabase("visits");
                dbVisits.delete();
            } catch (Exception err) {
                err.printStackTrace();
            }

            try {
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                String[] flist = myDir.list();
                for (int i = 0; i < flist.length; i++) {
                    System.out.println(" " + myDir.getAbsolutePath());
                    File temp = new File(myDir.getAbsolutePath() + "/" + flist[i]);
                    if (temp.isDirectory()) {
                        Log.d(TAG, " Deleting " + temp.getName());
                        temp.delete();
                    } else {
                        temp.delete();
                    }
                }
            } catch (Exception err) {
                Log.e(TAG, " Deleting materials from ole_temp directory ");
                err.printStackTrace();
            }
        }
    }

    public void startSyncProcess() {
        /// Start Syncing databases from server
        syncmembers = true;
        new FullscreenLogin.TestAsyncPull().execute();
        Log.e("MyCouch", "syncNotifier Running");
        final Thread th = new Thread(new Runnable() {
            private long startTime = System.currentTimeMillis();

            public void run() {
                while (syncmembers) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (openMemberList) {
                                dialogDownloadProgress.dismiss();;
                                //mDialog.dismiss();
                                openMemberList = false;
                                alertDialogOkay("Completed. Thank you for waiting, you can now \" Sign In \" .");
                                syncmembers = false;
                                return;
                            }
                            Log.d("runOnUiThread", "running pull members");
                            txtProgressStatus.setText("Synchronizing, please wait ... " + databaseList[syncCnt] + " [" + (syncCnt + 1) + " / " + databaseList.length + "]");
                            //downloadPB.setProgress(syncCnt+1);
                            ///mDialog.setMessage("Downloading, please wait ... " + databaseList[syncCnt] + " [" + (syncCnt + 1) + " / " + databaseList.length + "]");
                        }
                    });
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }

    public void alertInstallAdobeReader(String Message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(Message);
        builder1.setCancelable(true);
        builder1.setNegativeButton("Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                        File dst = new File(myDir, "adobe_reader.apk");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    public void alertDialogOkay(String Message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
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

    private void copyAPK(int resource, String apkUrl) {
        InputStream in = getResources().openRawResource(resource);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/ole_temp2");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File dst = new File(myDir, apkUrl);
        try {
            FileOutputStream out = new FileOutputStream(dst);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
            Log.e("tag", "APK Copied " + dst.toString());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    class RunCreateDocTask extends AsyncTask<String, Void, Boolean> {
        private Exception exception;
        private String cls_SyncServerURL;
        private String cls_DbName;
        private String cls_DocNameId;
        private String cls_Category;
        private JsonObject cls_ViewContent;

        public String getSyncServerURL() {
            return cls_SyncServerURL;
        }

        public void setSyncServerURL(String oldSyncServerURL) {
            cls_SyncServerURL = oldSyncServerURL;
        }

        public String getDbName() {
            return cls_DbName;
        }

        public void setDbName(String dbName) {
            cls_DbName = dbName;
        }

        public String getDocNameId() {
            return cls_DocNameId;
        }

        public void setCategory(String category) {
            cls_Category = category;
        }

        public String getCategory() {
            return cls_Category;
        }

        public void setDocNameId(String docNameId) {
            cls_DocNameId = docNameId;
        }

        public JsonObject getViewContent() {
            return cls_ViewContent;
        }

        public void setViewContent(JsonObject viewContent) {
            cls_ViewContent = viewContent;
        }

        protected Boolean doInBackground(String... urls) {
            try {
                Log.e("MyCouch", "URL = " + getSyncServerURL());
                URI uri = URI.create(getSyncServerURL());
                String url_Scheme = uri.getScheme();
                String url_Host = uri.getHost();
                int url_Port = uri.getPort();
                String url_user = null, url_pwd = null;
                if (sys_oldSyncServerURL.contains("@")) {
                    String[] userinfo = uri.getUserInfo().split(":");
                    url_user = userinfo[0];
                    url_pwd = userinfo[1];
                }
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid(getDbName(), true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                if (!dbClient.contains(URLEncoder.encode(getDocNameId(), "UTF-8"))) {
                    JsonObject json = new JsonObject();
                    json.addProperty("_id", getDocNameId());
                    json.add(getCategory(), getViewContent());
                    dbClient.save(json);
                }
                return true;
            } catch (Exception e) {
                this.exception = e;
                Log.e("MyCouch", e.toString());
                return false;
            }
        }

        protected void onPostExecute(Boolean docResult) {
            startSyncProcess();
        }
    }

    class TestAsyncPull extends AsyncTask<Void, Integer, String> {
        protected void onPreExecute() {
            Log.d("PreExceute", "On pre Exceute......");
        }

        protected String doInBackground(Void... arg0) {
            Log.d("DoINBackGround", "On doInBackground...");
            syncmembers = true;
            pull = new Replication[databaseList.length];
            db = new Database[databaseList.length];
            manager = new Manager[databaseList.length];
            try {
                manager[syncCnt] = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                db[syncCnt] = manager[syncCnt].getDatabase(databaseList[syncCnt]);
                URL url = new URL(sys_oldSyncServerURL + "/" + databaseList[syncCnt]);
                pull[syncCnt] = db[syncCnt].createPullReplication(url);
                pull[syncCnt].setContinuous(false);
                pull[syncCnt].addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        if (pull[syncCnt].isRunning()) {
                            Log.e("MyCouch", databaseList[syncCnt] + " " + event.getChangeCount());
                        } else {
                            Log.e("Finished", databaseList[syncCnt] + " " + db[syncCnt].getDocumentCount());
                            if (syncCnt < (databaseList.length - 2)) {
                                syncCnt++;
                                new FullscreenLogin.TestAsyncPull().execute();
                            } else {
                                Log.e("MyCouch", "Sync Completed");
                                dialogDownloadProgress.dismiss();
                                if (!openMemberList) {
                                    openMemberList = true;
                                }
                            }
                        }
                    }
                });
                pull[syncCnt].start();
            } catch (Exception e) {
                Log.e("MyCouch", databaseList[syncCnt] + " " + " Cannot create database", e);
            }
            publishProgress(syncCnt);
            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Integer... a) {
            Log.d("onProgress", "You are in progress update ... " + a[0]);
            if (syncCnt != (databaseList.length - 2)) {
            } else {
            }
        }

        protected void onPostExecute(String result) {
            Log.d("OnPostExec", "" + result);
        }
    }

    class UpdateMemberVisitsDocument extends AsyncTask<String, Void, String> {
        private Exception exception;
        private String cls_memberId;
        private int cls_sumVisits;
        private int cls_serverNewTotalVisits;

        public String getMemberId() {
            return cls_memberId;
        }

        public void setMemberId(String memberId) {
            cls_memberId = memberId;
        }

        public int getSumVisits() {
            return cls_sumVisits;
        }

        public void setSumVisits(int sumVisits) {
            cls_sumVisits = sumVisits;
        }

        protected String doInBackground(String... urls) {
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
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid("members", true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                if (dbClient.contains(cls_memberId)) {
                    /// Handle with Json
                    JsonObject json = dbClient.find(JsonObject.class, getMemberId());
                    cls_serverNewTotalVisits = (getSumVisits() + Integer.parseInt(json.get("visits").getAsString()));
                    json.addProperty("visits", cls_serverNewTotalVisits);
                    dbClient.update(json);
                }
                return "";
            } catch (Exception e) {
                this.exception = e;
                Log.e("MyCouch", "Updating member visits on server " + e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(String message) {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = null;
            try {
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database members = manager.getDatabase("members");
                Document retrievedDocument = members.getExistingDocument(getMemberId());
                if (retrievedDocument != null) {
                    Map<String, Object> properties = retrievedDocument.getProperties();
                    if (properties.containsKey("visits")) {
                        Map<String, Object> newProperties = new HashMap<String, Object>();
                        newProperties.putAll(retrievedDocument.getProperties());
                        newProperties.put("visits", cls_serverNewTotalVisits);
                        retrievedDocument.putProperties(newProperties);
                    }
                }
            } catch (Exception err) {
                Log.e("MyCouch", "Updating local member visits on device " + err.getMessage());
            }

        }
    }

    class UpdateResourceDocument extends AsyncTask<String, Void, String> {
        private Exception exception;
        private String cls_resouceId;
        private int cls_sum;
        private int cls_timesRated;
        private String cls_revision;

        public String getResourceId() {
            return cls_resouceId;
        }

        public void setResourceId(String resouceId) {
            cls_resouceId = resouceId;
        }

        public void getRevision(String revision) {
            cls_revision = revision;
        }

        public String setRevision() {
            return cls_revision;
        }

        public int getSum() {
            return cls_sum;
        }

        public void setSum(int sum) {
            cls_sum = sum;
        }

        public int getTimesRated() {
            return cls_timesRated;
        }

        public void setTimesRated(int timesRated) {
            cls_timesRated = timesRated;
        }

        protected String doInBackground(String... urls) {
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
                //if (uri.getUserInfo() != null) {
                //
                //}
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid("resources", true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                if (dbClient.contains(cls_resouceId)) {
                    /// Handle with Json
                    JsonObject json = dbClient.find(JsonObject.class, getResourceId());
                    int total_sum = (int) (getSum() + Integer.parseInt(json.get("sum").getAsString()));
                    int total_timesRated = getTimesRated() + Integer.parseInt(json.get("timesRated").toString());
                    json.addProperty("sum", total_sum);
                    json.addProperty("timesRated", total_timesRated);
                    dbClient.update(json);
                }
                return "";
            } catch (Exception e) {
                this.exception = e;
                Log.e("MyCouch", e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(String message) {

        }
    }

    class UpdateActivityLogDatabase extends AsyncTask<String, Void, String> {
        private int cls_male_visits, cls_female_visits;
        private ArrayList cls_male_rating, cls_male_timesRated, cls_female_timesRated, cls_female_rating, cls_resourcesIds;
        private ArrayList cls_male_opened, cls_female_opened, cls_resources_names, cls_resources_opened;

        public void set_resourcesIds(ArrayList resourcesIds) {
            cls_resourcesIds = resourcesIds;
        }

        ;

        public void set_resources_names(ArrayList resources_names) {
            cls_resources_names = resources_names;
        }

        ;

        public void set_resources_opened(ArrayList resources_opened) {
            cls_resources_opened = resources_opened;
        }

        ;

        public void set_male_visits(int male_visits) {
            cls_male_visits = male_visits;
        }

        ;

        public void set_female_visits(int female_visits) {
            cls_female_visits = female_visits;
        }

        ;

        public void set_male_rating(ArrayList male_rating) {
            cls_male_rating = male_rating;
        }

        ;

        public void set_female_rating(ArrayList female_rating) {
            cls_female_rating = female_rating;
        }

        ;

        public void set_male_timesRated(ArrayList male_timesRated) {
            cls_male_timesRated = male_timesRated;
        }

        ;

        public void set_female_timesRated(ArrayList female_timesRated) {
            cls_female_timesRated = female_timesRated;
        }

        ;

        public void set_male_opened(ArrayList male_opened) {
            cls_male_opened = male_opened;
        }

        ;

        public void set_female_opened(ArrayList female_opened) {
            cls_female_opened = female_opened;
        }

        ;

        protected String doInBackground(String... urls) {
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
                //if (uri.getUserInfo() != null) {
                //
                //}
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid("activitylog", true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                org.lightcouch.View view = dbClient.view("bell/getdocBylogdate").includeDocs(false);
                List<Map> results = view.reduce(false).includeDocs(false).query(Map.class);
                String todaysActivityDocId = null;
                String docDateStr = null;
                int i = 0;
                if (results.size() != 0) {
                    while (i < results.size()) {
                        LinkedTreeMap treemap = (LinkedTreeMap) results.get(i).get("value");
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.toJsonTree(treemap).getAsJsonObject();
                        docDateStr = jsonObject.get("logDate").getAsString();
                        Log.e("MyCouch", i + " " + docDateStr + " - " + jsonObject.get("_id").toString());
                        // Todo convert to date Compare date, month, year
                        if (docDateStr.equalsIgnoreCase(Serverdate)) {
                            todaysActivityDocId = jsonObject.get("_id").getAsString();
                            Log.e("MyCouch", "Found " + docDateStr + " with Id " + todaysActivityDocId);
                            break;
                        }
                        i++;
                    }
                    //// Check if activitylog document for today exist
                    if (docDateStr != null && todaysActivityDocId != null) {
                        try {
                            Gson gson = new Gson();
                            JsonParser parser = new JsonParser();
                            JsonObject jsonObject = dbClient.find(JsonObject.class, todaysActivityDocId);
                            //female_opened
                            JsonArray female_opened_Array = jsonObject.get("female_opened").getAsJsonArray();
                            JsonElement female_opened_Element = parser.parse(gson.toJson(cls_female_opened));
                            Log.e("MyCouch", "Begun -- ");
                            try {
                                female_opened_Array.addAll(female_opened_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to female_opened " + err.getLocalizedMessage());
                            }
                            //male_rating
                            JsonArray male_rating_Array = jsonObject.get("male_rating").getAsJsonArray();
                            JsonElement male_rating_Element = parser.parse(gson.toJson(cls_male_rating));
                            try {
                                male_rating_Array.addAll(male_rating_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to male_rating " + err.getLocalizedMessage());
                            }

                            //male_timesRated
                            JsonArray male_timesRated_Array = jsonObject.get("male_timesRated").getAsJsonArray();
                            JsonElement male_timesRated_Element = parser.parse(gson.toJson(cls_male_timesRated));
                            try {
                                male_timesRated_Array.addAll(male_timesRated_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to male_timesRated " + err.getLocalizedMessage());
                            }
                            //female_timesRated
                            JsonArray female_timesRated_Array = jsonObject.get("female_timesRated").getAsJsonArray();
                            JsonElement female_timesRated_Element = parser.parse(gson.toJson(cls_female_timesRated));
                            try {
                                female_timesRated_Array.addAll(female_timesRated_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to female_timesRated " + err.getLocalizedMessage());
                            }
                            //female_rating
                            JsonArray female_rating_Array = jsonObject.get("female_rating").getAsJsonArray();
                            JsonElement female_rating_Element = parser.parse(gson.toJson(cls_female_rating));
                            try {
                                female_rating_Array.addAll(female_rating_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to female_rating " + err.getLocalizedMessage());
                            }
                            //resourcesIds
                            JsonArray resourcesIds_Array = jsonObject.get("resourcesIds").getAsJsonArray();
                            JsonElement resourcesIds_Element = parser.parse(gson.toJson(cls_resourcesIds));
                            try {
                                resourcesIds_Array.addAll(resourcesIds_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to resourcesIds " + err.getLocalizedMessage());
                            }
                            //male_opened
                            JsonArray male_opened_Array = jsonObject.get("male_opened").getAsJsonArray();
                            JsonElement male_opened_Element = parser.parse(gson.toJson(cls_male_opened));
                            try {
                                male_opened_Array.addAll(male_opened_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to male_opened " + err.getLocalizedMessage());
                            }
                            //resources_names
                            JsonArray resources_names_Array = jsonObject.get("resources_names").getAsJsonArray();
                            JsonElement resources_names_Element = parser.parse(gson.toJson(cls_resources_names));
                            try {
                                resources_names_Array.addAll(resources_names_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to resources_names " + err.getLocalizedMessage());
                            }
                            //resources_opened
                            JsonArray resources_opened_Array = jsonObject.get("resources_opened").getAsJsonArray();
                            JsonElement resources_opened_Element = parser.parse(gson.toJson(cls_resources_opened));
                            try {
                                resources_opened_Array.addAll(resources_opened_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to resources_opened " + err.getLocalizedMessage());
                            }
                            Log.e("MyCouch", "Ended -- ");
                            int totalMaleVisits = (cls_male_visits + jsonObject.get("male_visits").getAsInt());
                            jsonObject.addProperty("male_visits", totalMaleVisits);
                            int totalFemaleVisits = (cls_female_visits + jsonObject.get("female_visits").getAsInt());
                            jsonObject.addProperty("female_visits", totalFemaleVisits);
                            jsonObject.add("female_opened", female_opened_Array);
                            jsonObject.add("male_rating", male_rating_Array);
                            jsonObject.add("male_timesRated", male_timesRated_Array);
                            jsonObject.add("female_timesRated", female_timesRated_Array);
                            jsonObject.add("female_rating", female_rating_Array);
                            jsonObject.add("resourcesIds", resourcesIds_Array);
                            jsonObject.add("male_opened", male_opened_Array);
                            jsonObject.add("resources_names", resources_names_Array);
                            jsonObject.add("resources_opened", resources_opened_Array);
                            dbClient.update(jsonObject);
                        } catch (Exception err) {
                            Log.e("MyCouch", "Error updating existing activity log " + err.getMessage());
                        }
                    } else {
                        try {
                            Gson gson = new Gson();
                            JsonParser parser = new JsonParser();
                            JsonObject jsonObject = new JsonObject();
                            Log.e("MyCouch", "Begun New Activity Resource -- ");
                            //female_opened
                            JsonArray female_opened_Array = new JsonArray();
                            JsonElement female_opened_Element = parser.parse(gson.toJson(cls_female_opened));
                            try {
                                female_opened_Array.addAll(female_opened_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to female_opened " + err.getLocalizedMessage());
                            }
                            //male_rating
                            JsonArray male_rating_Array = new JsonArray();
                            JsonElement male_rating_Element = parser.parse(gson.toJson(cls_male_rating));
                            try {
                                male_rating_Array.addAll(male_rating_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to male_rating " + err.getLocalizedMessage());
                            }
                            //male_timesRated
                            JsonArray male_timesRated_Array = new JsonArray();
                            JsonElement male_timesRated_Element = parser.parse(gson.toJson(cls_male_timesRated));
                            try {
                                male_timesRated_Array.addAll(male_timesRated_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to male_timesRated " + err.getLocalizedMessage());
                            }
                            //female_timesRated
                            JsonArray female_timesRated_Array = new JsonArray();
                            JsonElement female_timesRated_Element = parser.parse(gson.toJson(cls_female_timesRated));
                            try {
                                female_timesRated_Array.addAll(female_timesRated_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to female_timesRated " + err.getLocalizedMessage());
                            }
                            //female_rating
                            JsonArray female_rating_Array = new JsonArray();
                            JsonElement female_rating_Element = parser.parse(gson.toJson(cls_female_rating));
                            try {
                                female_rating_Array.addAll(female_rating_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to female_rating " + err.getLocalizedMessage());
                            }
                            //resourcesIds
                            JsonArray resourcesIds_Array = new JsonArray();
                            JsonElement resourcesIds_Element = parser.parse(gson.toJson(cls_resourcesIds));
                            try {
                                resourcesIds_Array.addAll(resourcesIds_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to resourcesIds " + err.getLocalizedMessage());
                            }
                            //male_opened
                            JsonArray male_opened_Array = new JsonArray();
                            JsonElement male_opened_Element = parser.parse(gson.toJson(cls_male_opened));
                            try {
                                male_opened_Array.addAll(male_opened_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to male_opened " + err.getLocalizedMessage());
                            }
                            //resources_names
                            JsonArray resources_names_Array = new JsonArray();
                            JsonElement resources_names_Element = parser.parse(gson.toJson(cls_resources_names));
                            try {
                                resources_names_Array.addAll(resources_names_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Got to resources_names " + err.getLocalizedMessage());
                            }
                            //resources_opened
                            Log.e("MyCouch", "Ended New Activity Resource -- ");
                            JsonArray resources_opened_Array = new JsonArray();
                            JsonElement resources_opened_Element = parser.parse(gson.toJson(cls_resources_opened));
                            try {
                                resources_opened_Array.addAll(resources_opened_Element.getAsJsonArray());
                            } catch (Exception err) {
                                Log.e("MyCouch", "Opened resources null " + err.getMessage());
                                err.printStackTrace();
                            }
                            Log.e("MyCouch", "Rating " + female_opened_Array);
                            jsonObject.addProperty("logDate", Serverdate);
                            jsonObject.addProperty("male_visits", cls_male_visits);
                            jsonObject.addProperty("female_visits", cls_female_visits);
                            jsonObject.add("female_opened", female_opened_Array);
                            jsonObject.add("male_rating", male_rating_Array);
                            jsonObject.add("male_timesRated", male_timesRated_Array);
                            jsonObject.add("female_timesRated", female_timesRated_Array);
                            jsonObject.add("female_rating", female_rating_Array);
                            jsonObject.add("resourcesIds", resourcesIds_Array);
                            jsonObject.add("male_opened", male_opened_Array);
                            jsonObject.add("resources_names", resources_names_Array);
                            jsonObject.add("resources_opened", resources_opened_Array);
                            dbClient.save(jsonObject);
                        } catch (Exception err) {
                            Log.e("MyCouch", "Error writing new activity log data " + err.getMessage());
                            err.printStackTrace();
                        }
                    }
                }
                return "";
            } catch (Exception e) {
                Log.e("MyCouch", e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(String message) {
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database activitylog = manager.getDatabase("activitylog");
                activitylog.delete();
                //WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                //String m_WLANMAC = wm.getConnectionInfo().getMacAddress();
                //Document doc = activitylog.getExistingDocument(m_WLANMAC);
                //doc.delete();
            } catch (Exception err) {
                Log.e("MyCouch", "Error deleting document from activitylog " + err.getMessage());
            }
        }
    }

    private void updateUI() {
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database activitylog = manager.getDatabase("activitylog");
            @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String m_WLANMAC = wm.getConnectionInfo().getMacAddress();
            Document doc = activitylog.getDocument(m_WLANMAC);
            ///Map<String, Object> properties = doc.getProperties();
            if (doc.getProperties() != null) {
                btnFeedback.setTextColor(getResources().getColor(R.color.ole_yellow));
                btnFeedback.setEnabled(true);
            } else {
                btnFeedback.setTextColor(getResources().getColor(R.color.ole_white));
                btnFeedback.setEnabled(false);
            }
        } catch (Exception err) {

        }
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (error == null) {
                   /// resultText.setText(resultText.getText() + result);
                    Log.e("MyCouch", "error: " + result);
                } else {
                    Log.e("MyCouch", "error: " + error.getException().getMessage());
                    ///resultText.setText(resultText.getText() + error.getException().getMessage());
                }
            }
        });*/
    }

    public JsonArray ConvertStringToJsonArray(List<String> list ) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray _Array = new JsonArray();
        JsonElement _Element = parser.parse(gson.toJson(list));
        try {
            _Array.addAll(_Element.getAsJsonArray());
            return _Array;
        }catch(Exception err){
            err.printStackTrace();
            return null;
        }
    }

    public JsonArray ConvertToJsonArray(List<Integer> list ) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray _Array = new JsonArray();
        JsonElement _Element = parser.parse(gson.toJson(list));
        try {
            _Array.addAll(_Element.getAsJsonArray());
            return _Array;
        }catch(Exception err){
            err.printStackTrace();
            return null;
        }
    }

    class GetServerDate extends AsyncTask<String, Void, String> {
        private Exception exception;
        private String cls_dbName;
        private String cls_View;

        public String getdbName() {
            return cls_dbName;
        }

        public void setdbName(String dbName) {
            cls_dbName = dbName;
        }

        public String getView() {
            return cls_View;
        }

        public void setView(String view) {
            cls_View = view;
        }

        protected String doInBackground(String... urls) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Serverdate = df.format(c.getTime());
            Log.e("MyCouch", "Current Date: " + Serverdate.toString());

            /*try {
                URI uri = URI.create(sys_oldSyncServerURL);
                String url_Scheme = uri.getScheme();
                String url_Host = uri.getHost();
                int url_Port = uri.getPort();
                String url_user = null, url_pwd = null;
                if(sys_oldSyncServerURL.contains("@")){
                    String[] userinfo = uri.getUserInfo().split(":");
                    url_user = userinfo[0];
                    url_pwd = userinfo[1];
                    Log.e("MyCouch", "Contain @");
                }
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid(getdbName(), true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
//// Todo Decide either use design document in apps or not
//                org.lightcouch.View view= dbClient.view("bell/date_now").includeDocs(true);
                org.lightcouch.View view= dbClient.view("apps/date_now").includeDocs(true);
                List<Map> results = view.reduce(false).includeDocs(false).query(Map.class);
                if (results.size() != 0) {
                    Serverdate = (String) results.get(0).get("value");
                    Log.e("MyCouch", Serverdate.toString());
                }
                return "";
            } catch (Exception e) {
                this.exception = e;
                Log.e("MyCouch", "error getting date "+e.getMessage());
                return null;
            }
            */
            return "";
        }

        protected void onPostExecute(String message) {
            // TODO: check this.exception
            // TODO: do something with the message
        }
    }

    //// Courses

    class UpdateCoursesDatabase extends AsyncTask<String, Void, String> {
        private ArrayList local_coursemembers;
        protected String doInBackground(String... urls) {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = null;
            ArrayList local_Admitted_Courses = new ArrayList();
            try {
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database admission_course_db = manager.getExistingDatabase("local_courses_admission");
                Query orderedQuery = chViews.ReadAdmissionCourseList(admission_course_db).createQuery();
                orderedQuery.setDescending(true);
                QueryEnumerator results = orderedQuery.run();
                for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                    QueryRow row = it.next();
                    String docId = (String) row.getValue();
                    Document doc = admission_course_db.getExistingDocument(docId);
                    Map<String, Object> properties = doc.getProperties();
                    ArrayList courseMembers = (ArrayList) properties.get("members");
                    if(courseMembers.contains(sys_usercouchId)){
                        local_Admitted_Courses.add(docId);
                    }
                }
            } catch (Exception err) {
                Log.e("MyCouch", "Compiling user specific locally admitted courses " + err.getMessage());
                err.getMessage();
            }
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
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid("courses", true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                org.lightcouch.View view = dbClient.view("bell/GetCourseByID").includeDocs(false);
                List<Map> results = view.reduce(false).includeDocs(false).query(Map.class);
                String docIdStr = null;
                int i = 0;
                if (results.size() != 0) {
                    while (i < results.size()) {
                        LinkedTreeMap treemap = (LinkedTreeMap) results.get(i).get("value");
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.toJsonTree(treemap).getAsJsonObject();
                        docIdStr = jsonObject.get("_id").getAsString();
                        if(local_Admitted_Courses.contains(docIdStr)){
                            Log.e(TAG,"New admission on course id"+ docIdStr);
                            JsonParser parser = new JsonParser();
                            JsonObject jsonDocObject = dbClient.find(JsonObject.class, docIdStr );
                            //female_opened
                            JsonArray members_Array = jsonDocObject.get("members").getAsJsonArray();
                            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                            Database admission_course_db = manager.getExistingDatabase("local_courses_admission");
                            Document doc = admission_course_db.getExistingDocument(docIdStr);
                            Map<String, Object> properties = doc.getProperties();
                            local_coursemembers = (ArrayList) properties.get("members");
                            try {
                                for(int x=0;x<members_Array.size();x++){
                                    if(local_coursemembers.contains(members_Array.get(x).getAsString())){
                                        local_coursemembers.remove(local_coursemembers.indexOf(members_Array.get(x).getAsString()));
                                    }
                                }
                                JsonElement members_Element = parser.parse(gson.toJson(local_coursemembers));
                                members_Array.addAll(members_Element.getAsJsonArray());
                                Log.e(TAG, docIdStr + " Included all is " + members_Array);
                                jsonObject.add("members", members_Array);
                                dbClient.update(jsonObject);
                            } catch (Exception err) {
                                Log.e(TAG, "Got to Saving course members into db " + err.getLocalizedMessage());
                                err.printStackTrace();
                            }
                            dialogFeedbackProgress.dismiss();
                        }
                        Log.e(TAG, i + " " + docIdStr + " - " + jsonObject.get("_id").toString());
                        i++;
                    }
                }
                return "";
            } catch (Exception e) {
                Log.e("MyCouch", e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(String message) {
            try {
                UpdateMemberCourseProgressDatabase updateMemberCourseProgressDatabase = new UpdateMemberCourseProgressDatabase();
                updateMemberCourseProgressDatabase.execute();

            } catch (Exception err) {
                Log.e("MyCouch", "Error deleting document from activitylog " + err.getMessage());
            }
        }
    }

    class UpdateMemberCourseProgressDatabase extends AsyncTask<String, Void, String> {
        private ArrayList local_coursemembers;
        protected String doInBackground(String... urls) {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = null;
            try {
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database local_member_course_progress = manager.getDatabase("local_member_course_progress");
                Query orderedQuery = chViews.ReadMemberCourseProg(local_member_course_progress).createQuery();
                orderedQuery.setDescending(true);
                QueryEnumerator results = orderedQuery.run();
                if(results.getCount()>0) {
                    for (Iterator<QueryRow> c_prg = results; c_prg.hasNext(); ) {
                        QueryRow row = c_prg.next();
                        String docId = (String) row.getValue();
                        Document mem_course_prog_doc = local_member_course_progress.getExistingDocument(docId);
                        Map<String, Object> mem_course_prog_properties = new HashMap<>();
                        mem_course_prog_properties.putAll(mem_course_prog_doc.getProperties());
                        List<Integer> ary_pqAttempts = (List<Integer>) mem_course_prog_properties.get("pqAttempts");
                        List<List> ary_stepsResult = (List<List>) mem_course_prog_properties.get("stepsResult");
                        List<String> ary_stepsStringResult = (List<String>) mem_course_prog_properties.get("stepsResult");
                        List<List> ary_stepsStatus = (List<List>) mem_course_prog_properties.get("stepsStatus");
                        List<String> ary_stepsStringStatus = (List<String>) mem_course_prog_properties.get("stepsStatus");
                        List<String> ary_stepsIDs = (List<String>) mem_course_prog_properties.get("stepsIds");
                        String memberId = (String) mem_course_prog_properties.get("memberId");
                        String courseId = (String)  mem_course_prog_properties.get("courseId");
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
                            CouchDbClientAndroid dbClient = new CouchDbClientAndroid("membercourseprogress", true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                            org.lightcouch.View view = dbClient.view("bell/GetMemberCourseResult").includeDocs(true);
                            ArrayList<String> keys = new ArrayList<String>();
                            keys.add(memberId);
                            keys.add(courseId);
                            List<Map> online_results = view.key(keys).reduce(false).query(Map.class);
                            String docIdStr = null;
                            int i = 0;
                            Log.e(TAG, "Starting result " + online_results );
                            if (online_results.size() != 0) {
                                while (i < online_results.size()) {
                                    LinkedTreeMap treemap = (LinkedTreeMap) online_results.get(i);
                                    Log.e(TAG, "Reading Tree " + treemap);
                                    Gson gson = new Gson();
                                    JsonObject jsonObject = gson.toJsonTree(treemap).getAsJsonObject();
                                    docIdStr = jsonObject.get("_id").getAsString();
                                    Log.e(TAG, "Reading online membercourseprogress " + docIdStr);
                                    JsonParser parser = new JsonParser();
                                    JsonObject jsonDocObject = dbClient.find(JsonObject.class, docIdStr);
                                    JsonArray jsonpqAttempts = jsonDocObject.get("pqAttempts").getAsJsonArray();
                                    JsonElement jsonStepsResult = jsonDocObject.get("stepsResult");
                                    JsonElement jsonStepsStatus = jsonDocObject.get("stepsStatus");
                                    // Attempt
                                    for (int x=0; x<ary_stepsIDs.size();x++){
                                        int serverAttempt = jsonpqAttempts.get(x).getAsInt();
                                        int deviceAttempt = ary_pqAttempts.get(x).intValue();
                                        jsonpqAttempts.set(x,parser.parse(gson.toJson(deviceAttempt+serverAttempt)));
                                        //Log.e(TAG,   " New device total  " + jsonpqAttempts);
                                    }
                                    Log.e(TAG,   " Output Attempts  " + jsonpqAttempts);
                                    // Results
                                    JsonArray arrayResults_output= new JsonArray();
                                    JsonArray arrayResults = jsonStepsResult.getAsJsonArray();
                                    for (int x=0; x<ary_stepsIDs.size();x++) {
                                        JsonArray arrayResultsHolder = arrayResults.get(x).getAsJsonArray();
                                        List<String> listResult =  ary_stepsResult.get(x);
                                        int maxAttepmts = ((arrayResultsHolder.size() > listResult.size())? arrayResultsHolder.size():listResult.size());
                                        Log.e(TAG,"Maximum attemps " + maxAttepmts + " Local " + listResult.size() + " Remote "+arrayResultsHolder.size());
                                        for(int cnt=1; cnt<maxAttepmts; cnt++){
                                            //Log.e(TAG,   " Inner local device array" + listResult.get(cnt));
                                            int serverResult=0;
                                            int deviceResult  = 0;
                                            try {
                                                if (listResult.get(cnt) != null) {
                                                    serverResult = Integer.parseInt(listResult.get(cnt));
                                                }
                                            }catch (IndexOutOfBoundsException err2){
                                                Log.e(TAG,"Server result array short / with null");
                                            }
                                            try {
                                                if (arrayResultsHolder.get(cnt) != null) {
                                                    deviceResult = arrayResultsHolder.get(cnt).getAsInt();
                                                }
                                            }catch (IndexOutOfBoundsException err2){
                                                Log.e(TAG,"Device result array short / with null");
                                            }
                                            if(cnt >= arrayResultsHolder.size()){
                                                arrayResultsHolder.add(parser.parse(gson.toJson(serverResult+deviceResult+"")));
                                            }else {
                                                arrayResultsHolder.set(cnt,parser.parse(gson.toJson(serverResult+deviceResult+"")));
                                            }

                                            //Log.e(TAG,   " New device total  " + arrayResultsHolder);
                                        }
                                        arrayResults_output.add(arrayResultsHolder);
                                        ///Log.e(TAG,   " Results  " + arrayResults.get(x).getAsJsonArray());
                                    }
                                    Log.e(TAG,   " Output Result " + arrayResults_output);

                                    // Status
                                    JsonArray arrayStatus_output= new JsonArray();
                                    JsonArray arrayStatus = jsonStepsStatus.getAsJsonArray();
                                    for (int n=0; n < ary_stepsIDs.size(); n++) {
                                        JsonArray arrayStatusHolder = arrayStatus.get(n).getAsJsonArray();
                                        List<String> listStatus =  ary_stepsStatus.get(n);
                                        int maxStatusAttepmts = ((arrayStatusHolder.size() > listStatus.size())? arrayStatusHolder.size():listStatus.size());
                                        for(int cntn=1; cntn<maxStatusAttepmts; cntn++){
                                            if(cntn>= arrayStatusHolder.size()){
                                                arrayStatusHolder.add(parser.parse(gson.toJson(1+"")));
                                            }else {
                                                arrayStatusHolder.set(cntn, parser.parse(gson.toJson(1 + "")));
                                            }
                                        }
                                        arrayStatus_output.add(arrayStatusHolder);
                                        ///Log.e(TAG,   " Results  " + arrayResults.get(x).getAsJsonArray());
                                    }
                                    Log.e(TAG,   " Output Status  " + arrayStatus_output);
                                    ///
                                    jsonObject.add("stepsResult",arrayResults_output);
                                    jsonObject.add("stepsStatus", arrayStatus_output);
                                    jsonObject.add("pqAttempts", jsonpqAttempts);
                                    dbClient.update(jsonObject);
                                    i++;
                                }
                            }else{
                                // create a new document
                                Gson gson = new Gson();
                                JsonParser parser = new JsonParser();
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("kind", "course-member-result");
                                jsonObject.addProperty("memberId", memberId);
                                jsonObject.addProperty("courseId", courseId);
                                jsonObject.add("stepsIds", ConvertStringToJsonArray(ary_stepsIDs));
                                jsonObject.add("stepsResult",ConvertStringToJsonArray(ary_stepsStringResult));
                                jsonObject.add("stepsStatus", ConvertStringToJsonArray(ary_stepsStringStatus));
                                jsonObject.add("pqAttempts", ConvertToJsonArray(ary_pqAttempts));
                                Log.e(TAG, "New record online membercourseprogress - " + jsonObject);
                                dbClient.save(jsonObject);
                                //mem_course_prog_doc.delete();
                            }
                            dialogFeedbackProgress.dismiss();
                            return "";
                        } catch (Exception e) {
                            Log.e("MyCouch", e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    }
                }
            } catch (Exception err) {
                Log.e(TAG, "local_courses_admission on device " + err.getMessage());
                err.printStackTrace();
            }





           /*
           AndroidContext androidContext = new AndroidContext(context);
            Manager manager = null;
            ArrayList local_Admitted_Courses = new ArrayList();
            try {
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database admission_course_db = manager.getExistingDatabase("local_courses_admission");
                Query orderedQuery = chViews.ReadAdmissionCourseList(admission_course_db).createQuery();
                orderedQuery.setDescending(true);
                QueryEnumerator results = orderedQuery.run();
                for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                    QueryRow row = it.next();
                    String docId = (String) row.getValue();
                    Document doc = admission_course_db.getExistingDocument(docId);
                    Map<String, Object> properties = doc.getProperties();
                    ArrayList courseMembers = (ArrayList) properties.get("members");
                    if(courseMembers.contains(sys_usercouchId)){
                        local_Admitted_Courses.add(docId);
                    }
                }
            } catch (Exception err) {
                Log.e("MyCouch", "Compiling user specific locally admitted courses " + err.getMessage());
                err.getMessage();
            }
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
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid("courses", true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                org.lightcouch.View view = dbClient.view("bell/GetCourseByID").includeDocs(false);
                List<Map> results = view.reduce(false).includeDocs(false).query(Map.class);
                String docIdStr = null;
                int i = 0;
                if (results.size() != 0) {
                    while (i < results.size()) {
                        LinkedTreeMap treemap = (LinkedTreeMap) results.get(i).get("value");
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.toJsonTree(treemap).getAsJsonObject();
                        docIdStr = jsonObject.get("_id").getAsString();
                        if(local_Admitted_Courses.contains(docIdStr)){
                            Log.e(TAG,"New admission on course id"+ docIdStr);
                            JsonParser parser = new JsonParser();
                            JsonObject jsonDocObject = dbClient.find(JsonObject.class, docIdStr );
                            //female_opened
                            JsonArray members_Array = jsonDocObject.get("members").getAsJsonArray();
                            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                            Database admission_course_db = manager.getExistingDatabase("local_courses_admission");
                            Document doc = admission_course_db.getExistingDocument(docIdStr);
                            Map<String, Object> properties = doc.getProperties();
                            local_coursemembers = (ArrayList) properties.get("members");
                            try {
                                for(int x=0;x<members_Array.size();x++){
                                    if(local_coursemembers.contains(members_Array.get(x).getAsString())){
                                        local_coursemembers.remove(local_coursemembers.indexOf(members_Array.get(x).getAsString()));
                                    }
                                }
                                JsonElement members_Element = parser.parse(gson.toJson(local_coursemembers));
                                members_Array.addAll(members_Element.getAsJsonArray());
                                Log.e(TAG, docIdStr + " Included all is " + members_Array);
                                jsonObject.add("members", members_Array);
                                dbClient.update(jsonObject);
                            } catch (Exception err) {
                                Log.e(TAG, "Got to Saving course members into db " + err.getLocalizedMessage());
                                err.printStackTrace();
                            }
                            dialogFeedbackProgress.dismiss();
                        }
                        Log.e(TAG, i + " " + docIdStr + " - " + jsonObject.get("_id").toString());
                        i++;
                    }
                }
                return "";
            } catch (Exception e) {
                Log.e("MyCouch", e.getMessage());
                return null;
            }*/
           return null;
        }

        protected void onPostExecute(String message) {
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database activitylog = manager.getDatabase("local_courses_admission");
                activitylog.delete();
                updateUI();
                btnFeedback.setTextColor(getResources().getColor(R.color.ole_white));
                btnFeedback.setEnabled(false);
            } catch (Exception err) {
                Log.e("MyCouch", "Error deleting document from activitylog " + err.getMessage());
            }
        }
    }
}
