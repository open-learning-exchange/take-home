package org.ole.learning.planet.planetlearning;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.ramotion.circlemenu.CircleMenuView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * org.ole.learning.planet
 *
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class User_Dashboard extends FragmentActivity implements Fragm_TakeCourse.OnFragmentInteractionListener,
        ListViewAdapter_myCourses.OnmyCourseListListener, Fragm_myCourses.OnFragmentInteractionListener,
        ListViewAdapter_myLibrary.OnResouceListListener,Fragm_Loading.OnFragmentInteractionListener,
        ListViewAdapter_Courses.OnCourseListListener, Fragm_Courses.OnFragmentInteractionListener,
        Fragm_TakeCourseTabbed.OnFragmentInteractionListener ,BlankFragment.OnFragmentInteractionListener {
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
            lbl_Name, lbl_Role, lbl_NumMyLibrary, lbl_NumMyCourse, lbl_NumMyTeams, lbl_NumMyMeetups,lbl_Visits;
    /// String
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    String doc_lastVisit, sys_NewDate, profile_membersRoles = "";
    String resourceIdTobeOpened, OneByOneResID, OneByOneResTitle;
    /// Integer
    int sys_uservisits_Int, myLibraryItemCount, myCoursesItemCount;
    //// Boolean
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    //// Object
    Object[] sys_membersWithResource;
    Activity activity;


    String openedResourceId, openedResourceTitle = "";
    boolean openedResource = false;
    ////Buttons
    Button dialogBtnDownoadAll, dialogBtnDownoadFile, dialogBtnOpenFileOnline;

    ///Others
    SharedPreferences settings;
    CouchViews chViews = new CouchViews();
    LogHouse logHouse = new LogHouse();
    Intent serviceIntent;
    AndroidContext androidContext;
    final Context context = this;
    private ProgressDialog mDialog;
    Dialog openResourceDialog;
    DownloadManager downloadManager;
    private long enqueue;
    boolean singleFileDownload = true;
    Dialog dialog2;
    ProgressDialog loading_dialog;
    LinearLayout loadingImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__dashboard);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        androidContext = new AndroidContext(this);
        activity = this;
        loadingImage = (LinearLayout) findViewById(R.id.lbl_loading_image);
        loadingImage.setVisibility(View.INVISIBLE);
        initiateLayoutMaterials();
        initiateOnClickActions();
        restorePreferences();
        ///totalVisits(sys_usercouchId);
        loadUIDynamicText();
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "myLibrary");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lt_myLibrary.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));


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
        lbl_Visits = (TextView) findViewById(R.id.lbl_NoOfVisits);
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
                    Intent intent = new Intent(context, FullscreenLogin.class);
                    startActivity(intent);
                } catch (Exception except) {
                    Log.d(TAG, "Logout click action error " + except.getMessage());
                }
            }
        });
        btnPlanetLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //showRoundMenu();
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
                    Intent intent = new Intent(context, FullscreenLogin.class);
                    startActivity(intent);
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
                    openCourses();
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
        if (sys_uservisits == "") {
            lbl_Visits.setText( sys_uservisits_Int );
        } else {
            lbl_Visits.setText(sys_uservisits);
        }
    }

    public String todaysDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println(dateFormat.format(cal.getTime()));
        return dateFormat.format(cal.getTime());
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
        lt_myCourses.setBackgroundColor(Color.TRANSPARENT);
        lt_myTeams.setBackgroundColor(Color.TRANSPARENT);
        lt_myMembers.setBackgroundColor(Color.TRANSPARENT);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            lt_myLibrary.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
            lt_myCourses.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
            lt_myTeams.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
            lt_myMembers.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
        } else {
            lt_myLibrary.setBackground(getResources().getDrawable(R.drawable.border));
            lt_myCourses.setBackground(getResources().getDrawable(R.drawable.border));
            lt_myTeams.setBackground(getResources().getDrawable(R.drawable.border));
            lt_myMembers.setBackground(getResources().getDrawable(R.drawable.border));
        }
        lblHome.setTextColor(ContextCompat.getColor(context,R.color.ole_white));
        lblLibrary.setTextColor(ContextCompat.getColor(context,R.color.ole_white));
        lblCourses.setTextColor(ContextCompat.getColor(context,R.color.ole_white));
        lblMeetups.setTextColor(ContextCompat.getColor(context,R.color.ole_white));
        lblMembers.setTextColor(ContextCompat.getColor(context,R.color.ole_white));
        lblReports.setTextColor(ContextCompat.getColor(context,R.color.ole_white));
        lblFeedback.setTextColor(ContextCompat.getColor(context,R.color.ole_white));
    }

    public void openLibrary() {
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "Library");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lblLibrary.setTextColor(ContextCompat.getColor(context,R.color.ole_yellow));
    }

    public void openCourses() {
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "Courses");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lblCourses.setTextColor(ContextCompat.getColor(context,R.color.ole_yellow));
    }

    public void openMyLibrary() {
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "myLibrary");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lt_myLibrary.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));

    }

    public void openMyCourses() {
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "myCourses");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lt_myCourses.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFinishPageLoad(Fragment fragToCall, String actionTarget) {
        if (actionTarget.equalsIgnoreCase("myLibrary") || actionTarget.equalsIgnoreCase("myCourses") || actionTarget.equalsIgnoreCase("Library")  || actionTarget.equalsIgnoreCase("Courses")) {
            Bundle args = new Bundle();
            args.putString("sys_usercouchId", sys_usercouchId);
            fragToCall.setArguments(args);
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fmlt_container, fragToCall);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    ///// Course

    @Override
    public void onTakeCourseOpen(String courseId) {
        Fragm_TakeCourse fg_TakeCourse = new Fragm_TakeCourse();
        Bundle args = new Bundle();
        args.putString("sys_usercouchId", sys_usercouchId);
        args.putString("courseId", courseId);
        fg_TakeCourse.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, fg_TakeCourse);
        transaction.addToBackStack(null);
        transaction.commit();
        ///Show as active
        resetActiveButton();
        lt_myCourses.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
       /* Fragm_TakeCourseTabbed fg_TakeCourse = new Fragm_TakeCourseTabbed();
        Bundle args = new Bundle();
        args.putString("sys_usercouchId", sys_usercouchId);
        args.putString("courseId", courseId);
        //fg_TakeCourse.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, fg_TakeCourse);
        transaction.addToBackStack(null);
        transaction.commit();
        ///Show as active
        resetActiveButton();
        lt_myCourses.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
        */
    }

    @Override
    public void onCourseDownloadCompleted(String CourseId, Object data) {
        ///alertDialogOkay("Download Completed");
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "myCourses");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lt_myCourses.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
    }

    @Override
    public void onCourseDownloadingProgress(String itemTitle, String status, String message) {

    }

    @Override
    public void onResourceDownloadCompleted(String CourseId, Object data) {
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "myLibrary");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lt_myLibrary.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
    }

    @Override
    public void onResourceOpened(String resourceId, String resourceTitle) {
        openedResource=true;
        openedResourceId =resourceId;
        openedResourceTitle = resourceTitle;
        checkResourceOpened();
    }


    public void checkResourceOpened() {
        if (openedResource) {
            rateResourceDialog(openedResourceId, openedResourceTitle);
            openedResource = false;
        }
    }
    public void rateResourceDialog(String resourceId, String title) {
        // custom dialog
        final String resourceID = resourceId;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rate_resource_dialog);
        dialog.setTitle("Add Feedback For \n");

        final TextView txtResTitle = (TextView) dialog.findViewById(R.id.txtResTitle);
        txtResTitle.setText(title);
        final EditText txtComment = (EditText) dialog.findViewById(R.id.editTextComment);
        final RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });
        Button dialogButton = (Button) dialog.findViewById(R.id.btnRateResource);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logHouse.saveRating(getApplicationContext(),(int) ratingBar.getRating(),String.valueOf(txtComment.getText()), resourceID );
                //saveRating((int) ratingBar.getRating(), String.valueOf(txtComment.getText()), resourceID);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onCourseAdmission(String CourseId) {
        ///alertDialogOkay("Download Completed");
        Fragm_Loading loading = new Fragm_Loading();
        Bundle args = new Bundle();
        args.putString("targetAction", "myCourses");
        args.putString("sys_usercouchId", sys_usercouchId);
        loading.setArguments(args);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fmlt_container, loading);
        transaction.addToBackStack(null);
        transaction.commit();
        resetActiveButton();
        lt_myCourses.setBackgroundColor(getResources().getColor(R.color.ole_blueLine));
    }
    public void showRoundMenu(){
        Dialog dialogMenu;
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(context,R.style.TransparentDialog);
        dialogB2.setView(R.layout.dialog_menu);
        dialogB2.setCancelable(false);
        Log.d(TAG, "Whats here");
        try {
            dialogMenu = dialogB2.create();
            dialogMenu.show();

            final CircleMenuView menu = (CircleMenuView) dialogMenu.findViewById(R.id.circle_menu);
            menu.setEventListener(new CircleMenuView.EventListener() {
                @Override
                public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
                    Log.d("D", "onMenuOpenAnimationStart");
                }

                @Override
                public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                    Log.d("D", "onMenuOpenAnimationEnd");
                }

                @Override
                public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                    Log.d("D", "onMenuCloseAnimationStart");
                }

                @Override
                public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                    Log.d("D", "onMenuCloseAnimationEnd");
                }

                @Override
                public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int index) {
                    Log.d("D", "onButtonClickAnimationStart| index: " + index);
                }

                @Override
                public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                    Log.d("D", "onButtonClickAnimationEnd| index: " + index);
                }
            });
            //downloadPB.setScaleY(3f);

        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    /*
    public void saveRating(int rate, String comment, String resourceId) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        Database resourceRating;
        int doc_rating;
        int doc_timesRated;
        ArrayList<String> commentList = new ArrayList<>();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            resourceRating = manager.getDatabase("resourcerating");
            Document retrievedDocument = resourceRating.getExistingDocument(resourceId);
            if (retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                if (properties.containsKey("sum")) {
                    doc_rating = (int) properties.get("sum");
                    doc_timesRated = (int) properties.get("timesRated");
                    commentList = (ArrayList<String>) properties.get("comments");
                    commentList.add(comment);
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("sum", (doc_rating + rate));
                    newProperties.put("timesRated", doc_timesRated + 1);
                    newProperties.put("comments", commentList);
                    retrievedDocument.putProperties(newProperties);
                    updateActivityRatingResources(rate, resourceId);
                    Toast.makeText(context, String.valueOf(rate), Toast.LENGTH_SHORT).show();
                }
            } else {
                Document newdocument = resourceRating.getDocument(resourceId);
                Map<String, Object> newProperties = new HashMap<>();
                newProperties.put("sum", rate);
                newProperties.put("timesRated", 1);
                commentList.add(comment);
                newProperties.put("comments", commentList);
                newdocument.putProperties(newProperties);
                /// todo check updating resource to see it works
                updateActivityRatingResources(rate, resourceId);
                Toast.makeText(context, String.valueOf(rate), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception err) {
            Log.e("MyCouch", "ERR : " + err.getMessage());
        }
    }

    public boolean updateActivityRatingResources(float rate, String resourceid) {
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        Database activityLog;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activityLog = manager.getDatabase("activitylog");
            @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String m_WLANMAC = wm.getConnectionInfo().getMacAddress();
            Document retrievedDocument = activityLog.getDocument(m_WLANMAC);
            if (retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                try {
                    ArrayList female_rating = (ArrayList<String>) properties.get("female_rating");
                    ArrayList female_timesRated = (ArrayList<String>) properties.get("female_timesRated");
                    ArrayList male_rating = (ArrayList<String>) properties.get("male_rating");
                    ArrayList male_timesRated = (ArrayList<String>) properties.get("male_timesRated");
                    ArrayList resourcesIds = (ArrayList<String>) properties.get("resourcesIds");
                    Log.e("MyCouch", "Option Rating 1");
                    if (sys_usergender.toLowerCase().equalsIgnoreCase("female")) {
                        female_rating.add(rate);
                        female_timesRated.add(1);
                        male_rating.add(0);
                        male_timesRated.add(0);
                    } else {
                        female_rating.add(0);
                        female_timesRated.add(0);
                        male_rating.add(rate);
                        male_timesRated.add(1);
                    }
                    resourcesIds.add(resourceid);
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("female_rating", female_rating);
                    newProperties.put("female_timesRated", female_timesRated);
                    newProperties.put("male_rating", male_rating);
                    newProperties.put("male_timesRated", male_timesRated);
                    newProperties.put("resourcesIds", resourcesIds);
                    retrievedDocument.putProperties(newProperties);
                    Log.e("MyCouch", "Saved resource rating in local Activity Log ");
                    return true;
                } catch (Exception err) {
                    Log.e("MyCouch", "Option Rating 1 Failed " + err.getMessage());
                    try {
                        Log.e("MyCouch", "Option 2");
                        Map<String, Object> newProperties = new HashMap<>();
                        newProperties.putAll(retrievedDocument.getProperties());
                        ArrayList female_rating = new ArrayList<>();
                        ArrayList female_timesRated = new ArrayList<>();
                        ArrayList male_rating = new ArrayList<>();
                        ArrayList male_timesRated = new ArrayList<>();
                        ArrayList resourcesIds = new ArrayList<>();
                        if (sys_usergender.toLowerCase().equalsIgnoreCase("female")) {
                            female_rating.add(rate);
                            female_timesRated.add(1);
                            male_rating.add(0);
                            male_timesRated.add(0);
                        } else {
                            female_rating.add(0);
                            female_timesRated.add(0);
                            male_rating.add(rate);
                            male_timesRated.add(1);
                        }
                        resourcesIds.add(resourceid);
                        newProperties.putAll(retrievedDocument.getProperties());
                        newProperties.put("female_rating", female_rating);
                        newProperties.put("female_timesRated", female_timesRated);
                        newProperties.put("male_rating", male_rating);
                        newProperties.put("male_timesRated", male_timesRated);
                        newProperties.put("resourcesIds", resourcesIds);
                        retrievedDocument.putProperties(newProperties);
                        Log.e("MyCouch", "Saved resource rating in local Activity Log ");
                        return true;
                    } catch (Exception er) {
                        Log.e("MyCouch", "Option Rating 2 Failed" + er.getMessage());
                        return false;
                    }
                }
            } else {
                try {
                    Log.e("MyCouch", "Option Rating 1b");
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    ArrayList female_rating = new ArrayList<>();
                    ArrayList female_timesRated = new ArrayList<>();
                    ArrayList male_rating = new ArrayList<>();
                    ArrayList male_timesRated = new ArrayList<>();
                    ArrayList resourcesIds = new ArrayList<>();
                    if (sys_usergender.toLowerCase().equalsIgnoreCase("female")) {
                        female_rating.add(rate);
                        female_timesRated.add(1);
                        male_rating.add(0);
                        male_timesRated.add(0);
                    } else {
                        female_rating.add(0);
                        female_timesRated.add(0);
                        male_rating.add(rate);
                        male_timesRated.add(1);
                    }
                    resourcesIds.add(resourceid);
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("female_rating", female_rating);
                    newProperties.put("female_timesRated", female_timesRated);
                    newProperties.put("male_rating", male_rating);
                    newProperties.put("male_timesRated", male_timesRated);
                    newProperties.put("resourcesIds", resourcesIds);
                    retrievedDocument.putProperties(newProperties);
                    Log.e("MyCouch", "Saved resource rating in local Activity Log ");
                    return true;
                } catch (Exception err) {
                    Log.e("MyCouch", "Option Rating 1b Failed : " + err.getMessage());
                    return false;
                }
            }
        } catch (Exception err) {
            Log.e("MyCouch", "Updating Activity Rating Log : " + err.getMessage());
            return false;
        }
    }
*/
}

