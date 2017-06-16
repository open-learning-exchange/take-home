package pbell.offline.ole.org.pbell;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class User_Dashboard extends FragmentActivity {
    private View mControlsView;
    String TAG = "UserDashboard";
    public static final String PREFS_NAME = "MyPrefsFile";

    //// Declare Image Buttons
    ImageButton btnBadges,btnSurvay,btnEmails,btnPoints,btnFeedback,
            btnMyLibrary,btnMyCourses,btnMyTeams,btnMyMeetups,btnLogout,btnPlanetLogo;
    //// TextView
    TextView lblMyLibrary,lblMyCourses,lblMyTeams,lblMyMeetups,lblLogout,
            lblHome,lblLibrary,lblCourses,lblMeetups,lblMembers,lblReports,lblFeedback,
            lbl_Name,lbl_Role,lbl_NumMyLibrary, lbl_NumMyCourse,lbl_NumMyTeams,lbl_NumMyMeetups;
    /// String
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    String doc_lastVisit, sys_NewDate, profile_membersRoles="";
    /// Integer
    int sys_uservisits_Int,myLibraryItemCount,myCoursesItemCount;
    //// Boolean
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    //// Object
    Object[] sys_membersWithResource;
    Activity activity;

    ///Others
    SharedPreferences settings;
    CouchViews chViews = new CouchViews();
    Intent serviceIntent;
    AndroidContext androidContext;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__dashboard);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        androidContext = new AndroidContext(this);
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

    public void initiateLayoutMaterials(){
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

        lblMyLibrary = (TextView)findViewById(R.id.lbl_myLibrary);
        lblMyCourses = (TextView)findViewById(R.id.lbl_myCourses);
        lblMyTeams = (TextView)findViewById(R.id.lbl_myTeams);
        lblMyMeetups = (TextView)findViewById(R.id.lbl_myMeetups);
        lblLogout = (TextView)findViewById(R.id.lbl_Logout);
        lblHome = (TextView)findViewById(R.id.lbl_home);
        lblLibrary = (TextView)findViewById(R.id.lbl_library);
        lblCourses = (TextView)findViewById(R.id.lbl_courses);
        lblMeetups = (TextView)findViewById(R.id.lbl_meetups);
        lblMembers = (TextView)findViewById(R.id.lbl_members);
        lblReports = (TextView)findViewById(R.id.lbl_reports);
        lblFeedback = (TextView)findViewById(R.id.lbl_feedback);

        lbl_Name = (TextView)findViewById(R.id.lbl_name);
        lbl_Role = (TextView)findViewById(R.id.lbl_role);
        lbl_NumMyLibrary = (TextView)findViewById(R.id.lbl_NumMyLibrary);
        lbl_NumMyCourse = (TextView)findViewById(R.id.lbl_NumMyCourses);
        lbl_NumMyTeams = (TextView)findViewById(R.id.lbl_NumMyTeams);
        lbl_NumMyMeetups = (TextView)findViewById(R.id.lbl_NumMyMeetups);
    }
    public void initiateOnClickActions(){
        btnBadges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Badges click action error "+except.getMessage());
                }
            }
        });
        btnSurvay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Survay click action error "+except.getMessage());
                }
            }
        });
        btnEmails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Emails click action error "+except.getMessage());
                }
            }
        });
        btnPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Points click action error "+except.getMessage());
                }
            }
        });
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Feedback click action error "+except.getMessage());
                }
            }
        });
        btnMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyLibrary click action error "+except.getMessage());
                }
            }
        });
        btnMyCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyCourses click action error "+except.getMessage());
                }
            }
        });
        btnMyTeams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyTeams click action error "+except.getMessage());
                }
            }
        });
        btnMyMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyMeetups click action error "+except.getMessage());
                }
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Logout click action error "+except.getMessage());
                }
            }
        });
        btnPlanetLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"PlanetLogo click action error "+except.getMessage());
                }
            }
        });


        //// Labels //
        lblMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyLibrary click action error "+except.getMessage());
                }
            }
        });
        lblMyCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyCourses click action error "+except.getMessage());
                }
            }
        });
        lblMyTeams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyTeams click action error "+except.getMessage());
                }
            }
        });
        lblMyMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"MyMeetups click action error "+except.getMessage());
                }
            }
        });
        lblLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Logout click action error "+except.getMessage());
                }
            }
        });
        lblHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Home click action error "+except.getMessage());
                }
            }
        });
        lblLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                    // Create fragment and give it an argument specifying the article it should show
                    ListView_Library newFragment = new ListView_Library();
                    Bundle args = new Bundle();
                    args.putInt("Arg1",1);
                    newFragment.setArguments(args);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fmlt_container, newFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } catch (Exception except) {
                    Log.d(TAG,"Library click action error "+except.getMessage());
                }
            }
        });
        lblCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Courses click action error "+except.getMessage());
                }
            }
        });
        lblMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Meetups click action error "+except.getMessage());
                }
            }
        });
        lblMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Members click action error "+except.getMessage());
                }
            }
        });
        lblReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Reports click action error "+except.getMessage());
                }
            }
        });
        lblFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                try {
                } catch (Exception except) {
                    Log.d(TAG,"Feedback click action error "+except.getMessage());
                }
            }
        });


    }
    public void restorePreferences(){
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","http://");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
        sys_password = settings.getString("pf_password","");
        sys_usercouchId = settings.getString("pf_usercouchId","");
        sys_userfirstname = settings.getString("pf_userfirstname","");
        sys_userlastname = settings.getString("pf_userlastname","");
        sys_usergender = settings.getString("pf_usergender","");
        sys_uservisits = settings.getString("pf_uservisits","");;
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int",0);
        sys_singlefilestreamdownload =settings.getBoolean("pf_singlefilestreamdownload",true);
        sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload",true);
        sys_servername = settings.getString("pf_server_name"," ");
        sys_serverversion = settings.getString("pf_server_version"," ");
        loadUIDynamicText();
        Set<String> mwr = settings.getStringSet("membersWithResource",null);
        try{
            sys_membersWithResource = mwr.toArray();
            Log.e(TAG, " membersWithResource  = "+sys_membersWithResource.length);
        }catch(Exception err){
            Log.e(TAG, " Error creating  sys_membersWithResource");
        }
        runBackgroundService();
    }
    public void loadUIDynamicText(){
        lbl_Name.setText(getUserName());
        lbl_Role.setText(String.valueOf(getUserRole()));
        lbl_NumMyLibrary.setText(String.valueOf(getUserMyLibraryNum()));
        lbl_NumMyCourse.setText(String.valueOf(getUserMyCourseNum()));
        lbl_NumMyTeams.setText(getUserMyTeamsNum());
        lbl_NumMyMeetups.setText(getUserMtMeetupsNum());
    }
    public String getUserName(){
        if(sys_username!=""){
            return sys_userfirstname + " " +sys_userlastname;
        }else{
            return "";
        }
    }
    public String getUserRole(){
        String memberId = sys_usercouchId;
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db_members = manager.getExistingDatabase("members");
            Document members_doc = db_members.getExistingDocument(memberId);
            Map<String, Object> members_doc_properties = members_doc.getProperties();
            ArrayList membersRoles = (ArrayList) members_doc_properties.get("roles");
            profile_membersRoles = TextUtils.join(" - ", membersRoles);
            return profile_membersRoles;
        }catch(Exception except){
            Log.d(TAG,"Counting MyLibrary resources error "+except.getMessage());
            return  "-";
        }
    }
    public Integer getUserMyLibraryNum(){
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
            return  myLibraryItemCount;
        }catch(Exception except){
            Log.d(TAG,"Counting MyLibrary resources error "+except.getMessage());
            return  0;
        }
    }
    public Integer getUserMyCourseNum(){
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
            return  myCoursesItemCount;
        }catch(Exception except){
            Log.d(TAG,"Counting MyLibrary resources error "+except.getMessage());
            return  0;
        }
    }
    public String getUserMyTeamsNum(){
        return "0";
    }
    public String getUserMtMeetupsNum(){
        return "0";
    }
    public void runBackgroundService(){
        try{
            serviceIntent = new Intent(context,ServerSearchService.class);
            context.stopService(serviceIntent);
        }catch(Exception error) {
            Log.e("MYAPP", " Creating Service error "+error.getMessage());
        }
    }
}

