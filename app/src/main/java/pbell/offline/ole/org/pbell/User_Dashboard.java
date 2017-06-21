package pbell.offline.ole.org.pbell;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClientAndroid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class User_Dashboard extends FragmentActivity {
    private View mControlsView;
    String TAG = "MYAPP";
    public static final String PREFS_NAME = "MyPrefsFile";

    //// Declare LinearLayouts
    LinearLayout lt_myLibrary, lt_myCourses, lt_myTeams, lt_myMembers;

    //// Declare Image Buttons
    ImageButton btnBadges, btnSurvay, btnEmails, btnPoints, btnFeedback,
            btnMyLibrary, btnMyCourses, btnMyTeams, btnMyMeetups, btnLogout, btnPlanetLogo;
    //// TextView
    TextView lblMyLibrary, lblMyCourses, lblMyTeams, lblMyMeetups, lblLogout,
            lblHome, lblLibrary, lblCourses, lblMeetups, lblMembers, lblReports, lblFeedback,
            lbl_Name, lbl_Role, lbl_NumMyLibrary, lbl_NumMyCourse, lbl_NumMyTeams, lbl_NumMyMeetups;
    /// String
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    String doc_lastVisit, sys_NewDate, profile_membersRoles = "";
    String resourceIdTobeOpened,OneByOneResID,OneByOneResTitle;
    /// Integer
    int sys_uservisits_Int, myLibraryItemCount, myCoursesItemCount;
    //// Boolean
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    //// Object
    Object[] sys_membersWithResource;
    Activity activity;

    ///Others
    SharedPreferences settings;
    CouchViews chViews = new CouchViews();
    LogHouse logHouse = new LogHouse();
    Intent serviceIntent;
    AndroidContext androidContext ;
    final Context context = this;
    private ProgressDialog mDialog;
    Dialog openResourceDialog;
    DownloadManager downloadManager;
    private long enqueue;
    boolean singleFileDownload=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__dashboard);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        androidContext= new AndroidContext(this);
        activity = this;
        initiateLayoutMaterials();
        initiateOnClickActions();
        restorePreferences();
        loadUIDynamicText();


        TabFragment0 tF0 = new TabFragment0();
        tF0.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fmlt_container, tF0).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }

    public void initiateLayoutMaterials() {
        btnBadges = (ImageButton) findViewById(R.id.ibtn_Badges);
        btnSurvay = (ImageButton) findViewById(R.id.ibtn_Survay);
        btnEmails = (ImageButton) findViewById(R.id.ibtn_Emails);
        btnPoints = (ImageButton) findViewById(R.id.ibtn_Points);
        btnFeedback = (ImageButton) findViewById(R.id.ibtn_Feedback);
        btnMyLibrary = (ImageButton) findViewById(R.id.ibtn_myLibrary);
        btnMyCourses = (ImageButton) findViewById(R.id.ibtn_myCourses);
        btnMyTeams = (ImageButton) findViewById(R.id.ibtn_myTeams);
        btnMyMeetups = (ImageButton) findViewById(R.id.ibtn_myMeetups);
        btnLogout = (ImageButton) findViewById(R.id.ibtn_Logout);
        btnPlanetLogo = (ImageButton) findViewById(R.id.ibtn_PlanetLogo);

        lblMyLibrary = (TextView) findViewById(R.id.lbl_myLibrary);
        lblMyCourses = (TextView) findViewById(R.id.lbl_myCourses);
        lblMyTeams = (TextView) findViewById(R.id.lbl_myTeams);
        lblMyMeetups = (TextView) findViewById(R.id.lbl_myMeetups);
        lblLogout = (TextView) findViewById(R.id.lbl_Logout);
        lblHome = (TextView) findViewById(R.id.lbl_home);
        lblLibrary = (TextView) findViewById(R.id.lbl_library);
        lblCourses = (TextView) findViewById(R.id.lbl_courses);
        lblMeetups = (TextView) findViewById(R.id.lbl_meetups);
        lblMembers = (TextView) findViewById(R.id.lbl_members);
        lblReports = (TextView) findViewById(R.id.lbl_reports);
        lblFeedback = (TextView) findViewById(R.id.lbl_feedback);

        lbl_Name = (TextView) findViewById(R.id.lbl_name);
        lbl_Role = (TextView) findViewById(R.id.lbl_role);
        lbl_NumMyLibrary = (TextView) findViewById(R.id.lbl_NumMyLibrary);
        lbl_NumMyCourse = (TextView) findViewById(R.id.lbl_NumMyCourses);
        lbl_NumMyTeams = (TextView) findViewById(R.id.lbl_NumMyTeams);
        lbl_NumMyMeetups = (TextView) findViewById(R.id.lbl_NumMyMeetups);

        lt_myLibrary = (LinearLayout) findViewById(R.id.lt_myLibrary);
        lt_myCourses = (LinearLayout) findViewById(R.id.lt_myCourses);
        lt_myTeams = (LinearLayout) findViewById(R.id.lt_myTeams);
        lt_myMembers = (LinearLayout) findViewById(R.id.lt_myMeetups);
    }

    public void initiateOnClickActions() {
        btnBadges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Badges click action error " + except.getMessage());
                }
            }
        });
        btnSurvay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Survay click action error " + except.getMessage());
                }
            }
        });
        btnEmails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Emails click action error " + except.getMessage());
                }
            }
        });
        btnPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Points click action error " + except.getMessage());
                }
            }
        });
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Feedback click action error " + except.getMessage());
                }
            }
        });
        btnMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openMyLibrary();
                } catch (Exception except) {
                    Log.d(TAG, "MyLibrary click action error " + except.getMessage());
                }
            }
        });
        btnMyCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openMyCourses();
                } catch (Exception except) {
                    Log.d(TAG, "MyCourses click action error " + except.getMessage());
                }
            }
        });
        btnMyTeams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "MyTeams click action error " + except.getMessage());
                }
            }
        });
        btnMyMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "MyMeetups click action error " + except.getMessage());
                }
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Logout click action error " + except.getMessage());
                }
            }
        });
        btnPlanetLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "PlanetLogo click action error " + except.getMessage());
                }
            }
        });


        //// Labels //
        lblMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                    openMyLibrary();
                } catch (Exception except) {
                    Log.d(TAG, "MyLibrary click action error " + except.getMessage());
                }
            }
        });
        lblMyCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                    openMyCourses();
                } catch (Exception except) {
                    Log.d(TAG, "MyCourses click action error " + except.getMessage());
                }
            }
        });
        lblMyTeams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "MyTeams click action error " + except.getMessage());
                }
            }
        });
        lblMyMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {

                } catch (Exception except) {
                    Log.d(TAG, "MyMeetups click action error " + except.getMessage());
                }
            }
        });
        lblLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Logout click action error " + except.getMessage());
                }
            }
        });
        lblHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Home click action error " + except.getMessage());
                }
            }
        });
        lblLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                    openLibrary();
                } catch (Exception except) {
                    Log.d(TAG, "Library click action error " + except.getMessage());
                }
            }
        });
        lblCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Courses click action error " + except.getMessage());
                }
            }
        });
        lblMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Meetups click action error " + except.getMessage());
                }
            }
        });
        lblMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Members click action error " + except.getMessage());
                }
            }
        });
        lblReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Reports click action error " + except.getMessage());
                }
            }
        });
        lblFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG, "Feedback click action error " + except.getMessage());
                }
            }
        });


    }

    public void restorePreferences() {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
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
        loadUIDynamicText();
        Set<String> mwr = settings.getStringSet("membersWithResource", null);
        try {
            sys_membersWithResource = mwr.toArray();
            Log.e(TAG, " membersWithResource  = " + sys_membersWithResource.length);
        } catch (Exception err) {
            Log.e(TAG, " Error creating  sys_membersWithResource");
        }
        runBackgroundService();
    }

    public void loadUIDynamicText() {
        lbl_Name.setText(getUserName());
        lbl_Role.setText(String.valueOf(getUserRole()));
        lbl_NumMyLibrary.setText(String.valueOf(getUserMyLibraryNum()));
        lbl_NumMyCourse.setText(String.valueOf(getUserMyCourseNum()));
        lbl_NumMyTeams.setText(getUserMyTeamsNum());
        lbl_NumMyMeetups.setText(getUserMtMeetupsNum());
    }

    public String getUserName() {
        if (sys_username != "") {
            return sys_userfirstname + " " + sys_userlastname;
        } else {
            return "";
        }
    }

    public String getUserRole() {
        String memberId = sys_usercouchId;
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db_members = manager.getExistingDatabase("members");
            Document members_doc = db_members.getExistingDocument(memberId);
            Map<String, Object> members_doc_properties = members_doc.getProperties();
            ArrayList membersRoles = (ArrayList) members_doc_properties.get("roles");
            profile_membersRoles = TextUtils.join(" - ", membersRoles);
            return profile_membersRoles;
        } catch (Exception except) {
            Log.d(TAG, "Counting MyLibrary resources error " + except.getMessage());
            return "-";
        }
    }

    public Integer getUserMyLibraryNum() {
        String memberId = sys_usercouchId;
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db_shelf = manager.getExistingDatabase("shelf");
            Query orderedQuery = chViews.ReadShelfByIdView(db_shelf).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            myLibraryItemCount = 0;
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document shelf_doc = db_shelf.getExistingDocument(docId);
                Map<String, Object> shelf_doc_properties = shelf_doc.getProperties();
                if (memberId.equals((String) shelf_doc_properties.get("memberId"))) {
                    myLibraryItemCount++;
                }
            }
            return myLibraryItemCount;
        } catch (Exception except) {
            Log.d(TAG, "Counting MyLibrary resources error " + except.getMessage());
            return 0;
        }
    }

    public Integer getUserMyCourseNum() {
        String memberId = sys_usercouchId;
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db_courses = manager.getExistingDatabase("courses");
            Query orderedQuery = chViews.ReadCourses(db_courses).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            myCoursesItemCount = 0;
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document courses_doc = db_courses.getExistingDocument(docId);
                Map<String, Object> courses_doc_properties = courses_doc.getProperties();
                ArrayList courseMembers = (ArrayList) courses_doc_properties.get("members");
                for (int cnt = 0; cnt < courseMembers.size(); cnt++) {
                    if (memberId.equals(courseMembers.get(cnt).toString())) {
                        myCoursesItemCount++;
                    }
                }
            }
            return myCoursesItemCount;
        } catch (Exception except) {
            Log.d(TAG, "Counting MyLibrary resources error " + except.getMessage());
            return 0;
        }
    }

    public String getUserMyTeamsNum() {
        return "0";
    }

    public String getUserMtMeetupsNum() {
        return "0";
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

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteDirectory(files[i]);
                }
            }
            return path.delete();
        }

        return false;
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
        Log.e("MyCouch", " Destination is " + FileName);
        request.setDestinationInExternalPublicDir("ole_temp", FileName);
        // get download service and enqueue file
        mDialog.setMessage("Downloading  \" " + OneByOneResTitle + " \" . please wait...");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = downloadManager.enqueue(request);
    }

    public void createResourceDoc(String manualResId, String manualResTitle, String manualResopenWith) {
        Database database = null;
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase("resources");
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("title", manualResTitle);
            properties.put("openWith", manualResopenWith);
            properties.put("localfile", "yes");
            // properties.put("resourceType", manualResType);
            Document document = database.getDocument(manualResId);
            try {
                document.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                Log.e("MyCouch", "Cannot save document", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void runBackgroundService() {
        try {
            serviceIntent = new Intent(context, ServerSearchService.class);
            context.stopService(serviceIntent);
        } catch (Exception error) {
            Log.e("MYAPP", " Creating Service error " + error.getMessage());
        }
    }

    public void resetActiveButton() {
        lt_myLibrary.setBackgroundColor(Color.TRANSPARENT);
        lt_myLibrary.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
        lt_myCourses.setBackgroundColor(Color.TRANSPARENT);
        lt_myCourses.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
        lt_myTeams.setBackgroundColor(Color.TRANSPARENT);
        lt_myTeams.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
        lt_myMembers.setBackgroundColor(Color.TRANSPARENT);
        lt_myMembers.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
        lblHome.setTextColor(getResources().getColor(R.color.ole_white));
        lblLibrary.setTextColor(getResources().getColor(R.color.ole_white));
        lblCourses.setTextColor(getResources().getColor(R.color.ole_white));
        lblMeetups.setTextColor(getResources().getColor(R.color.ole_white));
        lblMembers.setTextColor(getResources().getColor(R.color.ole_white));
        lblReports.setTextColor(getResources().getColor(R.color.ole_white));
        lblFeedback.setTextColor(getResources().getColor(R.color.ole_white));
    }

    public void openLibrary() {
        ListView_Library newFragment = new ListView_Library();
        Bundle args = new Bundle();
        args.putInt("Arg1", 1);
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        ///Show as active
        resetActiveButton();
        lblLibrary.setTextColor(getResources().getColor(R.color.ole_yellow));
    }

    public void openMyLibrary() {
        ListView_myLibrary fg_myLibrary = new ListView_myLibrary();
        Bundle args = new Bundle();
        args.putInt("Arg1", 1);
        fg_myLibrary.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, fg_myLibrary);
        transaction.addToBackStack(null);
        transaction.commit();
        ///Show as active
        resetActiveButton();
        lt_myLibrary.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
    }

    public void openMyCourses() {
        ListView_myCourses fg_myCourses = new ListView_myCourses();
        Bundle args = new Bundle();
        args.putInt("Arg1", 1);
        fg_myCourses.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, fg_myCourses);
        transaction.addToBackStack(null);
        transaction.commit();
        ///Show as active
        resetActiveButton();
        lt_myCourses.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
    }

    public Boolean openResources(String id) {
        resourceIdTobeOpened = id;
        Log.d(TAG, "Trying to open resource " + id);
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(resourceIdTobeOpened);
            String openwith = (String) res_doc.getProperty("openWith");
            String openResName = (String) res_doc.getProperty("title");
            ///openFromDiskDirectly = true;
            logHouse.updateActivityOpenedResources(this, sys_usercouchId, resourceIdTobeOpened, openResName);
            Log.e("MYAPP", " member opening resource  = " + resourceIdTobeOpened + " and Open with " + openwith);
            List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
/////HTML
            if (openwith.equalsIgnoreCase("HTML")) {
               /* indexFilePath = null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(resourceIdTobeOpened, (String) attmentNames.get(cnt));
                    }
                    if (indexFilePath != null) {
                        openHTML(indexFilePath);
                    }
                } else {
                    openImage(resourceIdTobeOpened, (String) attmentNames.get(0), getExtension(attmentNames.get(0)));
                }*/
////PDF
            } else if (openwith.equalsIgnoreCase("Just download")) {
                //// Todo work to get just download
            } else if (openwith.equalsIgnoreCase("PDF.js")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            try {
                                mDialog.dismiss();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setPackage("com.adobe.reader");
                                intent.setDataAndType(Uri.fromFile(f), "application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } catch (Exception err) {
                                myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                                File dst = new File(myDir, "adobe_reader.apk");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }
                }

////MP3
            } else if (openwith.equalsIgnoreCase("MP3")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            this.startActivity(intent);
                        }
                    }
                }
/// BELL READER
            } else if (openwith.equalsIgnoreCase("Bell-Reader")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            try {
                                mDialog.dismiss();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setPackage("com.adobe.reader");
                                intent.setDataAndType(Uri.fromFile(f), "application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } catch (Exception err) {
                                myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                                File dst = new File(myDir, "adobe_reader.apk");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }
                }
/////VIDEO
            } else if (openwith.equalsIgnoreCase("Flow Video Player")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            this.startActivity(intent);
                        }
                    }
                }
            }
//// Video Book Player
            else if (openwith.equalsIgnoreCase("BeLL Video Book Player")) {
            }
            /// Native Video
            else if (openwith.equalsIgnoreCase("Native Video")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            this.startActivity(intent);
                        }
                    }
                }
            }
        } catch (Exception Er) {
            Log.d("MyCouch", "Opening resource error " + Er.getMessage());
        }
        return true;
    }

    public Boolean downloadResources(String resId, Activity act,Context perimeter_context) {
        /*clicked_rs_status = online;
        clicked_rs_title = title;
        clicked_rs_ID = resId;
        resButtonId = buttonPressedId;*/
        OneByOneResID = resId;
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(context);
        // custom dialog
        dialogB2.setView(R.layout.dialog_prompt_resource_location);
        dialogB2.setCancelable(true);
        Dialog openResourceDialog = dialogB2.create();
        TextView txtResourceId = (TextView) openResourceDialog.findViewById(R.id.txtResourceID);
        txtResourceId.setText(resId);
/*
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_prompt_resource_location);
        dialog.setTitle("Title...");
        TextView txtResourceId = (TextView) dialog.findViewById(R.id.txtResourceID);
        txtResourceId.setText(resId);
        */

        //// Open material online
      /*  Button dialogBtnOpenFileOnline = (Button) openResourceDialog.findViewById(R.id.btnOpenOnline);
        dialogBtnOpenFileOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo Open resource in a browser
                dialog2.dismiss();
                OneByOneResID = clicked_rs_ID;
                Fuel ful = new Fuel();
                onlinecouchresource = sys_oldSyncServerURL + "/resources/" + OneByOneResID;
                ful.get(sys_oldSyncServerURL + "/resources/" + OneByOneResID).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                    @Override
                    public void success(Request request, Response response, String s) {
                        try {
                            try {
                                /// alertDialogOkay(OneByOneResID+"");
                                openFromOnlineServer = true;
                                jsonData = new JSONObject(s);
                                String openWith = (String) jsonData.get("openWith");
                                Log.e("MyCouch", "Open With -- " + openWith);
                                JSONObject _attachments = (JSONObject) jsonData.get("_attachments");
                                if (!openWith.equalsIgnoreCase("HTML")) {
                                    Iterator<String> keys = _attachments.keys();
                                    if (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        String encodedkey = URLEncoder.encode(key, "utf-8");
                                        onlinecouchresource = onlinecouchresource + "/" + encodedkey;
                                        mDialog.dismiss();
                                        openHTML(onlinecouchresource);
                                    }
                                } else {
                                    if (_attachments.length() <= 1) {
                                        Iterator<String> keys = _attachments.keys();
                                        if (keys.hasNext()) {
                                            String key = (String) keys.next();
                                            String encodedkey = URLEncoder.encode(key, "utf-8");
                                            onlinecouchresource = onlinecouchresource + "/" + encodedkey;
                                            mDialog.dismiss();
                                            openHTML(onlinecouchresource);
                                        }
                                    } else {
                                        onlinecouchresource = onlinecouchresource + "/index.html";
                                        mDialog.dismiss();
                                        openHTML(onlinecouchresource);
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(Request request, Response response, FuelError fuelError) {
                        alertDialogOkay("Error downloading file");
                        Log.e("MyCouch", " " + fuelError);
                    }
                });
            }
        });
*/
        //// Download Only selected file
        Button dialogBtnDownoadFile = (Button) openResourceDialog.findViewById(R.id.btnDownloadFile);
        dialogBtnDownoadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openResourceDialog.dismiss();
                try {
                    mDialog = new ProgressDialog(context);
                    mDialog.setMessage("Please wait...");
                    mDialog.setCancelable(false);
                    mDialog.show();
                    singleFileDownload = true;
                    ///syncThreadHandler();
                    new downloadSpecificResourceToDisk().execute();
                    //final AsyncTask<String, Void, Boolean> execute = new SyncResource().execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    mDialog = new ProgressDialog(context);
                    mDialog.setMessage("Error Downloading Resource. Check connection to server and try again");
                    mDialog.setCancelable(true);
                    mDialog.show();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                }
            }
        });

        //// Download all resources on button click
        Button dialogBtnDownoadAll = (Button) openResourceDialog.findViewById(R.id.btnDownloadAll);
 /*dialogBtnDownoadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
                OneByOneResID = clicked_rs_ID;
                try {
                    //String root = Environment.getExternalStorageDirectory().toString();
                    //File myDir = new File(root + "/ole_temp");
                    //deleteDirectory(myDir);
                    //myDir.mkdirs();
                    mDialog = new ProgressDialog(context);
                    mDialog.setMessage("Downloading resource, please wait..." + resourceTitleList[allresDownload]);
                    mDialog.setCancelable(true);
                    mDialog.show();
                    htmlResourceList.clear();
                    allhtmlDownload = 0;
                    //// Todo Decide which option is best
                    singleFiledownload = false;
                    new FullscreenActivity.downloadAllResourceToDisk().execute();
                    //new SyncAllResource().execute();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                }
            }
        });*/
        openResourceDialog.show();
/*        mDialog = new ProgressDialog(act);
        mDialog.setMessage("This resource is not downloaded on this device. \n Please wait. Establishing connection with to server so you can download it...");
        mDialog.setCancelable(false);
        mDialog.show(); */
 /*       new FullscreenActivity.AsyncCheckConnection().execute();*/
        return true;
    }

    public boolean downloadNow(String resId, Activity act,Context perimeter_context){
        OneByOneResID = resId;
        new downloadSpecificResourceToDisk().execute();
        return true;
    }

    //// Opening Resource Types //
    class downloadSpecificResourceToDisk extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
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
                if (dbClient.contains(OneByOneResID)) {
                    /// Handle with Json
                    JsonObject jsonObject = dbClient.find(JsonObject.class, OneByOneResID);
                    JsonObject jsonAttachments = jsonObject.getAsJsonObject("_attachments");
                    final String openWith = (String) jsonObject.get("openWith").getAsString();
                    final String title = jsonObject.get("title").getAsString();
                    OneByOneResTitle = title;
                    Log.e("MyCouch", "Open With -- " + openWith);
                    if (!openWith.equalsIgnoreCase("HTML")) {
                        JSONObject _attachments = new JSONObject(jsonAttachments.toString());
                        Iterator<String> keys = _attachments.keys();
                        if (keys.hasNext()) {
                            String key = (String) keys.next();
                            Log.e("MyCouch", "-- " + key);
                            final String encodedkey = URLEncoder.encode(key, "utf-8");
                            File file = new File(encodedkey);
                            String extension = encodedkey.substring(encodedkey.lastIndexOf("."));
                            final String diskFileName = OneByOneResID + extension;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadWithDownloadManagerSingleFile(sys_oldSyncServerURL + "/resources/" + OneByOneResID + "/" + encodedkey, diskFileName);
                                    createResourceDoc(OneByOneResID, title, openWith);
                                }
                            });
                        }
                    } else {
                        /*Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS ");
                        htmlResourceList.add(OneByOneResID);
                        if (allhtmlDownload < htmlResourceList.size()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callSuncOneHTMLResource();
                                }
                            });
                        } else {
                            mDialog.dismiss();
                            alertDialogOkay("Download Completed");
                        }*/
                    }
                }
            } catch (Exception e) {
                Log.e("MyCouch", "Download this resource error " + e.getMessage());
               // mDialog.dismiss();
                alertDialogOkay("Error downloading file, check connection and try again");
                return null;
            }
            return null;
        }
    }

}

