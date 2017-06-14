package pbell.offline.ole.org.pbell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.couchbase.lite.android.AndroidContext;

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
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion, doc_lastVisit, sys_NewDate = "";
    /// Integer
    int sys_uservisits_Int;
    //// Boolean
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    //// Object
    Object[] sys_membersWithResource;

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
        initiateLayoutMaterials();
        initiateOnClickActions();
        restorePreferences();
        loadUIDynamicText();
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
       // lbl_Role.setText(getUserRole());
        lbl_NumMyLibrary.setText(getUserMyLibraryNum());
        lbl_NumMyCourse.setText(getUserMyCourseNum());
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
        return "0";
    }
    public String getUserMyLibraryNum(){
        return "0";
    }
    public String getUserMyCourseNum(){
        return "0";
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

