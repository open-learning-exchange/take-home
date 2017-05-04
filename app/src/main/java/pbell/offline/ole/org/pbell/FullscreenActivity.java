package pbell.offline.ole.org.pbell;

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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Revision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.replicator.Replication;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static pbell.offline.ole.org.pbell.R.id.ratingBar;

@SuppressWarnings("ALL")
public class FullscreenActivity extends AppCompatActivity {

    private View mContentView;
    final Context context = this;

    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    private static final String TAG = "MYAPP";
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    int sys_uservisits_Int = 0;
    Object[] sys_membersWithResource;
    boolean userShelfSynced = true;
    boolean synchronizing = true;
    JSONObject jsonData;
    Database dbResources;
    int syncCnt, resourceNo, allresDownload, allhtmlDownload = 0;
    AndroidContext androidContext;
    Replication pull;
    Manager manager;
    boolean status_SyncOneByOneResource = false;
    private ProgressDialog mDialog;
    String indexFilePath;
    String openedResourceId, openedResourceTitle = "";
    boolean openedResource = false;
    boolean openFromDiskDirectly = false;
    boolean singleFiledownload = false;
    boolean openFromOnlineServer = false;
    boolean clicked_rs_status;
    String clicked_rs_title, clicked_rs_ID;
    String onlinecouchresource;

    CouchViews chViews = new CouchViews();
    String resourceIdList[];
    String resourceTitleList[];
    int rsLstCnt = 0;
    Button[] libraryButtons;
    Dialog dialog2;
    String OneByOneResID = "";
    int resButtonId;
    boolean userInfoDisplayed = false;

    ArrayList<String> lst;

    ImageView[] imageView;
    static Uri videoURl;
    static Intent intent;
    MediaPlayer sd_Slidin;
    Database database;
    Replication repl;
    DownloadManager downloadManager;
    Boolean initialActivityLoad = false;


    private List<Resource> resourceList = new ArrayList<Resource>();
    private List<String> resIDArrayList = new ArrayList<String>();
    private ListView listView;
    private CustomListAdapter adapter;
    Boolean calbackStatus, syncALLInOneStarted = false;
    Button dialogBtnDownoadAll, dialogBtnDownoadFile, dialogBtnOpenFileOnline;
    private long enqueue;
    int resourceCntr, attachmentLength;
    List<String> htmlResourceList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.fullscreen_content);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        androidContext = new AndroidContext(this);
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        initialActivityLoad = true;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            openFromDiskDirectly = true;
                            if (!singleFiledownload) {
                                libraryButtons[allresDownload].setTextColor(getResources().getColor(R.color.ole_white));
                                if (allresDownload < libraryButtons.length) {
                                    allresDownload++;
                                    if (resourceTitleList[allresDownload] != null) {
                                        new downloadAllResourceToDisk().execute();
                                    } else {
                                        if (allhtmlDownload < htmlResourceList.size()) {
                                            new SyncAllHTMLResource().execute();
                                        } else {
                                            mDialog.dismiss();
                                            alertDialogOkay("Download Completed");
                                        }
                                    }
                                } else {
                                    if (allhtmlDownload < htmlResourceList.size()) {
                                        new SyncSingleHTMLResource().execute();
                                        openFromDiskDirectly = true;
                                    } else {
                                        mDialog.dismiss();
                                        alertDialogOkay("Download Completed");
                                    }

                                }
                            } else {
                                libraryButtons[resButtonId].setTextColor(getResources().getColor(R.color.ole_white));
                                mDialog.dismiss();
                                alertDialogOkay("Download Completed");
                            }
                        } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                            alertDialogOkay("Download Failed" + resourceTitleList[allresDownload]);
                            if (!singleFiledownload) {
                                if (allresDownload < libraryButtons.length) {
                                    allresDownload++;
                                    if (resourceTitleList[allresDownload] != null) {
                                        new downloadAllResourceToDisk().execute();
                                        openFromDiskDirectly = true;
                                    } else {
                                        if (allhtmlDownload < htmlResourceList.size()) {
                                            new SyncAllHTMLResource().execute();
                                        } else {
                                            mDialog.dismiss();
                                            alertDialogOkay("Download Completed");
                                        }
                                    }
                                }
                            } else {
                                mDialog.dismiss();
                                alertDialogOkay("Download Completed");
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        ///////////////////////////////////
        TextView txtcurDate = (TextView) findViewById(R.id.lblDate);
        txtcurDate.setText(curdate());
        /////////////////////////////////
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username", "");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl", "");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate", "");
        sys_password = settings.getString("pf_password", "");
        sys_usercouchId = settings.getString("pf_usercouchId", "");
        sys_userfirstname = settings.getString("pf_userfirstname", "");
        sys_userlastname = settings.getString("pf_userlastname", "");
        sys_usergender = settings.getString("pf_usergender", "");
        sys_uservisits = settings.getString("pf_uservisits", "");
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int", 0);
        sys_servername = settings.getString("pf_server_name", " ------------- ");
        sys_serverversion = settings.getString("pf_server_version", " ------------");
        /////////////////////////
        resourceList.clear();
        resIDArrayList.clear();
        LoadShelfResourceList();

        ///Update user info
        updateUI();

        /// Todo :- Sync user usage data back to server

        if (!userShelfSynced) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Materials on your shelf are NOT yet synchronized unto this device. " +
                    "You can only view title of items on your shelf BUT you can not launch or read them." +
                    "To access your materials on this device, please sync device with server.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog closeDialogue = builder.create();
            closeDialogue.show();
        }

        sd_Slidin = MediaPlayer.create(this, R.raw.wave);

        Button btnCourses = (Button) findViewById(R.id.btnCourses);
        btnCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fuel ful = new Fuel();
                final WifiManager wifiManager = (WifiManager) FullscreenActivity.this.getSystemService(Context.WIFI_SERVICE);
                mDialog = new ProgressDialog(context);
                mDialog.setMessage("Opening Courses please wait...");
                mDialog.setCancelable(true);
                mDialog.show();
                if (wifiManager.isWifiEnabled()) {
                    ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                        @Override
                        public void success(Request request, Response response, String s) {
                            try {
                                List<String> myList = new ArrayList<String>();
                                myList.clear();
                                myList = Arrays.asList(s.split(","));
                                Log.e("MyCouch", "-- " + myList.size());
                                if (myList.size() < 8) {
                                    mDialog.dismiss();
                                    alertDialogOkay("You need to turn on Wi-Fi and connect to server to use this function");
                                } else {
                                    openServerPage("/apps/_design/bell/MyApp/index.html#courses");
                                }
                            } catch (Exception e) {
                                mDialog.dismiss();
                                alertDialogOkay("You need to turn on your Wi-Fi and connect to the server to use this function");
                                Log.e(TAG, "Device couldn't reach server. Error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(Request request, Response response, FuelError fuelError) {
                            mDialog.dismiss();
                            alertDialogOkay("You need to turn on your Wi-Fi and connect to the server to use this function");
                            Log.e(TAG, "Device couldn't reach server. Check and try again");
                            Log.e(TAG, " " + fuelError);
                        }
                    });
                } else {
                    mDialog.dismiss();
                    alertDialogOkay("You need to turn on Wi-Fi and connect to server to use this function");
                }
            }
        });

        Button btnMeetups = (Button) findViewById(R.id.btnMeetups);
        btnMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fuel ful = new Fuel();
                final WifiManager wifiManager = (WifiManager) FullscreenActivity.this.getSystemService(Context.WIFI_SERVICE);
                mDialog = new ProgressDialog(context);
                mDialog.setMessage("Opening Meetups please wait...");
                mDialog.setCancelable(true);
                mDialog.show();
                if (wifiManager.isWifiEnabled()) {
                    ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                        @Override
                        public void success(Request request, Response response, String s) {
                            try {
                                List<String> myList = new ArrayList<String>();
                                myList.clear();
                                myList = Arrays.asList(s.split(","));
                                Log.e("MyCouch", "-- " + myList.size());
                                if (myList.size() < 8) {
                                    mDialog.dismiss();
                                    alertDialogOkay("You need to turn on Wi-Fi and connect to server to use this function");
                                } else {
                                    openServerPage("/apps/_design/bell/MyApp/index.html#meetups");
                                }
                            } catch (Exception e) {
                                mDialog.dismiss();
                                alertDialogOkay("You need to turn on your Wi-Fi and connect to the server to use this function");
                                Log.e(TAG, "Device couldn't reach server. Error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(Request request, Response response, FuelError fuelError) {
                            mDialog.dismiss();
                            alertDialogOkay("You need to turn on your Wi-Fi and connect to the server to use this function");
                            Log.e(TAG, "Device couldn't reach server. Check and try again");
                            Log.e(TAG, " " + fuelError);
                        }
                    });
                } else {
                    mDialog.dismiss();
                    alertDialogOkay("You need to turn on Wi-Fi and connect to server to use this function");
                }
            }
        });

        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnLibrary = (Button) findViewById(R.id.btnLibrary);
        btnLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fuel ful = new Fuel();
                final WifiManager wifiManager = (WifiManager) FullscreenActivity.this.getSystemService(Context.WIFI_SERVICE);
                mDialog = new ProgressDialog(context);
                mDialog.setMessage("Opening library please wait...");
                mDialog.setCancelable(true);
                mDialog.show();
                if (wifiManager.isWifiEnabled()) {
                    ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                        @Override
                        public void success(Request request, Response response, String s) {
                            try {
                                List<String> myList = new ArrayList<String>();
                                myList.clear();
                                myList = Arrays.asList(s.split(","));
                                Log.e("MyCouch", "-- " + myList.size());
                                if (myList.size() < 8) {
                                    mDialog.dismiss();
                                    populateLibraryDialogList();
                                } else {
                                    openServerPage("/apps/_design/bell/MyApp/index.html#resources");
                                }
                            } catch (Exception e) {
                                mDialog.dismiss();
                                populateLibraryDialogList();
                                Log.e(TAG, "Device couldn't reach server. Error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(Request request, Response response, FuelError fuelError) {
                            mDialog.dismiss();
                            populateLibraryDialogList();
                            Log.e(TAG, "Device couldn't reach server. Check and try again");
                            Log.e(TAG, " " + fuelError);
                        }
                    });
                } else {
                    mDialog.dismiss();
                    populateLibraryDialogList();
                }
            }
        });

        Button btnUpdate = (Button) findViewById(R.id.btnSetting);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///openSettingDialogList();
                new checkServerConnection().execute("");
            }
        });

        TextView lblMyLibrary = (TextView) findViewById(R.id.lblMyLibrary);
        lblMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateLibraryDialogList();
            }
        });

        TextView lblmycoursesProgress = (TextView) findViewById(R.id.lblmycourses);
        lblmycoursesProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fuel ful = new Fuel();
                final WifiManager wifiManager = (WifiManager) FullscreenActivity.this.getSystemService(Context.WIFI_SERVICE);
                mDialog = new ProgressDialog(context);
                mDialog.setMessage("Opening Course Progress please wait...");
                mDialog.setCancelable(true);
                mDialog.show();
                if (wifiManager.isWifiEnabled()) {
                    ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                        @Override
                        public void success(Request request, Response response, String s) {
                            try {
                                List<String> myList = new ArrayList<String>();
                                myList.clear();
                                myList = Arrays.asList(s.split(","));
                                Log.e("MyCouch", "-- " + myList.size());
                                if (myList.size() < 8) {
                                    mDialog.dismiss();
                                    alertDialogOkay("You need to turn on Wi-Fi and connect to server to use this function");
                                } else {
                                    openServerPage("/apps/_design/bell/MyApp/index.html#courses/barchart");
                                }
                            } catch (Exception e) {
                                mDialog.dismiss();
                                alertDialogOkay("You need to turn on Wi-Fi and connect to server to use this function");
                                Log.e(TAG, "Device couldn't reach server. Error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(Request request, Response response, FuelError fuelError) {
                            mDialog.dismiss();
                            populateLibraryDialogList();
                            Log.e(TAG, "Device couldn't reach server. Check and try again");
                            Log.e(TAG, " " + fuelError);
                        }
                    });
                } else {
                    mDialog.dismiss();
                    alertDialogOkay("You need to turn on Wi-Fi and connect to server to use this function");
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        animateLayoutBars();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkResourceOpened();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    public void populateLibraryDialogList() {
        AlertDialog.Builder dialogBMyLibrary = new AlertDialog.Builder(this);
        // custom dialog
        dialogBMyLibrary.setView(R.layout.dialog_my_library);
        dialogBMyLibrary.setCancelable(true);
        final Dialog dialogMyLibrary = dialogBMyLibrary.create();
        dialogMyLibrary.show();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Double width = metrics.widthPixels * .8;
        Double height = metrics.heightPixels * .8;
        Window win = dialogMyLibrary.getWindow();
        win.setLayout(width.intValue(), height.intValue());
        dialogMyLibrary.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        listView = (ListView) dialogMyLibrary.findViewById(R.id.list);
        adapter = new CustomListAdapter(this, resourceList);
        try {
            adapter = new CustomListAdapter(this, resourceList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    if (libraryButtons[position].getCurrentTextColor() == getResources().getColor(R.color.ole_yellow)) {
                        MaterialClickDialog(false, resourceTitleList[position], resourceIdList[position], position);
                        dialogMyLibrary.dismiss();
                    } else {
                        mDialog = new ProgressDialog(context);
                        mDialog.setMessage("Opening please " + resourceTitleList[position] + "wait...");
                        mDialog.setCancelable(true);
                        mDialog.show();
                        openedResourceId = resourceIdList[position];
                        openedResourceTitle = resourceTitleList[position];
                        openedResource = true;
                        openDoc(resourceIdList[position]);
                        Log.e("MyCouch", "Clicked to open resource Id " + resourceIdList[position]);
                    }
                }
            });
        } catch (Exception err) {
            Log.e("adapter", " " + err);
        }
    }

    public void openSettingDialogList() {
        restorePref();
        AlertDialog.Builder dialogBSettings = new AlertDialog.Builder(this);
        // custom dialog
        dialogBSettings.setView(R.layout.dialog_my_settings);
        dialogBSettings.setCancelable(true);
        final Dialog dialogSetting = dialogBSettings.create();
        dialogSetting.show();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Double width = metrics.widthPixels * .8;
        Double height = metrics.heightPixels * .8;
        Window win = dialogSetting.getWindow();
        win.setLayout(width.intValue(), height.intValue());

        Button btnCheck4Updates = (Button) dialogSetting.findViewById(R.id.btnCheck4Updates);
        btnCheck4Updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new checkServerConnection().execute("");
            }
        });
    }

    public String curdate() {
        Calendar cal = Calendar.getInstance();
        Date d = new Date();

        SimpleDateFormat s_df = new SimpleDateFormat("EEEE");
        String dayOfTheWeek = s_df.format(d);
        SimpleDateFormat date_df = new SimpleDateFormat("d");
        String dayNumber = date_df.format(d);
        SimpleDateFormat month_df = new SimpleDateFormat("MMMM");
        String month_name = month_df.format(cal.getTime());
        SimpleDateFormat year_df = new SimpleDateFormat("yyyy");
        String year = year_df.format(d);
        String displayedDate = dayOfTheWeek + "  |  " + dayNumber + " " + month_name + " " + year;
        return displayedDate;
    }

    public void restorePref() {
        // Restore preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username", "");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl", "");
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
        Set<String> mwr = settings.getStringSet("membersWithResource", null);
        try {
            sys_membersWithResource = mwr.toArray();
            Log.e("MYAPP", " membersWithResource  = " + sys_membersWithResource.length);

        } catch (Exception err) {
            Log.e("MYAPP", " Error creating  sys_membersWithResource");
        }
    }

    public void LoadShelfResourceList() {
        String memberId = sys_usercouchId;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("shelf");
            Database resource_Db = manager.getDatabase("resources");
            Query orderedQuery = chViews.ReadShelfByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            resourceIdList = new String[results.getCount()];
            resourceTitleList = new String[results.getCount()];
            rsLstCnt = 0;
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                Map<String, Object> resource_properties = null;
                if (memberId.equals((String) properties.get("memberId"))) {
                    String myresTitile = ((String) properties.get("resourceTitle"));
                    String myresId = ((String) properties.get("resourceId"));
                    String myresType, myresDec, myresExt = "";
                    resourceTitleList[rsLstCnt] = myresTitile;
                    resourceIdList[rsLstCnt] = myresId;
                    resIDArrayList.add(myresId);
                    Log.e("tag", "MEMBER ID " + (String) properties.get("resourceTitle"));
                    try {
                        Document resource_doc = resource_Db.getExistingDocument((String) properties.get("resourceId"));
                        Log.e("tag", "RES ID " + (String) properties.get("resourceId"));
                        try {
                            resource_properties = resource_doc.getProperties();
                        } catch (Exception errs) {
                            Log.e("tag", "OBJECT ERROR " + errs.toString());
                        }
                        //myresTitile = (String) resource_properties.get("title")+"";
                        //myresId = (String) properties.get("resourceId")+"";
                        myresDec = (String) resource_properties.get("author") + "";
                        myresType = (String) resource_properties.get("averageRating") + "";
                        myresExt = (String) resource_properties.get("openWith") + "";
                        rsLstCnt++;
                    } catch (Exception err) {
                        Log.e("tag", "ERROR " + err.getMessage());
                        //myresTitile = "Unknown resource .. ";
                        //myresId = "";
                        myresDec = "";
                        myresType = "";
                        rsLstCnt++;
                    }
                    Resource resource = new Resource();
                    resource.setTitle(myresTitile);
                    resource.setThumbnailUrl(getIconType(myresExt));
                    resource.setDescription(myresDec);
                    resource.setRating(myresType);

                    resource.setGenre(null);
                    // adding resource to resources array
                    resourceList.add(resource);
                    resourceNo++;
                }
            }

            LinearLayout row2 = (LinearLayout) findViewById(R.id.layholder_library);
            libraryButtons = new Button[rsLstCnt];
            for (int ButtonCnt = 0; ButtonCnt < rsLstCnt; ButtonCnt++) {
                libraryButtons[ButtonCnt] = new Button(this);
                libraryButtons[ButtonCnt].setText(resourceTitleList[ButtonCnt]);
                libraryButtons[ButtonCnt].setId(ButtonCnt);
                libraryButtons[ButtonCnt].setBackgroundResource(R.drawable.rounded_corners_black_blue);
                libraryButtons[ButtonCnt].setTextColor(getResources().getColor(R.color.white));
                libraryButtons[ButtonCnt].setAllCaps(false);
                libraryButtons[ButtonCnt].setPadding(10, 5, 10, 5);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    libraryButtons[ButtonCnt].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(170, MATCH_PARENT);
                layoutParams.setMargins(1, 0, 1, 0); // left, top, right, bottom
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setStroke(2, Color.WHITE);
                drawable.setCornerRadius(2);
                drawable.setColor(getResources().getColor(R.color.ole_black_blue));
                libraryButtons[ButtonCnt].setBackgroundDrawable(drawable);
                libraryButtons[ButtonCnt].setLayoutParams(layoutParams);
                row2.addView(libraryButtons[ButtonCnt]);
                try {
                    manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                    Document resource_doc = resource_Db.getExistingDocument((String) resourceIdList[ButtonCnt]);
                    Map<String, Object> resource_properties = resource_doc.getProperties();
                    Log.e("tag", "RES ID " + (String) resource_properties.get("resourceId"));
                    libraryButtons[ButtonCnt].setTextColor(getResources().getColor(R.color.ole_white));
                } catch (Exception errs) {
                    libraryButtons[ButtonCnt].setTextColor(getResources().getColor(R.color.ole_yellow));
                    Log.e("tag", "OBJECT ERROR " + errs.toString());
                }

                libraryButtons[ButtonCnt].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (libraryButtons[view.getId()].getCurrentTextColor() == getResources().getColor(R.color.ole_yellow)) {
                            MaterialClickDialog(false, resourceTitleList[view.getId()], resourceIdList[view.getId()], view.getId());
                        } else {
                            mDialog = new ProgressDialog(context);
                            mDialog.setMessage("Opening please " + resourceTitleList[view.getId()] + " wait...");
                            mDialog.setCancelable(true);
                            mDialog.show();
                            openedResourceId = resourceIdList[view.getId()];
                            openedResourceTitle = resourceTitleList[view.getId()];
                            openedResource = true;
                            openDoc(resourceIdList[view.getId()]);
                            Log.e("MyCouch", "Clicked to open " + resourceIdList[view.getId()]);

                        }

                    }

                });
                //////////// Save list in Preferences
            }
            db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void MaterialClickDialog(boolean online, String title, String resId, int buttonPressedId) {
        clicked_rs_status = online;
        clicked_rs_title = title;
        clicked_rs_ID = resId;
        resButtonId = buttonPressedId;
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(this);
        // custom dialog
        dialogB2.setView(R.layout.dialog_prompt_resource_location);
        dialogB2.setCancelable(true);
        dialog2 = dialogB2.create();
        dialog2.show();
        TextView txtResourceId = (TextView) dialog2.findViewById(R.id.txtResourceID);
        txtResourceId.setText(title);
        //// Open material online
        dialogBtnOpenFileOnline = (Button) dialog2.findViewById(R.id.btnOpenOnline);
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

        //// Download Only selected file
        dialogBtnDownoadFile = (Button) dialog2.findViewById(R.id.btnDownloadFile);
        dialogBtnDownoadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
                OneByOneResID = clicked_rs_ID;
                try {
                    mDialog = new ProgressDialog(context);
                    mDialog.setMessage("Please wait...");
                    mDialog.setCancelable(false);
                    mDialog.show();

                    ///syncThreadHandler();

                    //// Todo Decide which option is best
                    singleFiledownload = true;
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
        dialogBtnDownoadAll = (Button) dialog2.findViewById(R.id.btnDownloadAll);
        dialogBtnDownoadAll.setOnClickListener(new View.OnClickListener() {
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
                    new downloadAllResourceToDisk().execute();
                    //new SyncAllResource().execute();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                }
            }
        });
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("This resource is not downloaded on this device. \n Please wait. Establishing connection with to server so you can download it...");
        mDialog.setCancelable(false);
        mDialog.show();
        new AsyncCheckConnection().execute();
    }

    ///todo check and replace below
    private class AsyncCheckConnection extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Fuel ful = new Fuel();
            ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                @Override
                public void success(Request request, Response response, String s) {
                    try {
                        List<String> myList = new ArrayList<String>();
                        myList.clear();
                        myList = Arrays.asList(s.split(","));
                        Log.e("MyCouch", "-- " + myList.size());
                        if (myList.size() < 8) {
                            mDialog.dismiss();
                            dialog2.dismiss();
                            alertDialogOkay("Check the server address again. Saved address isn't the BeLL server");
                            calbackStatus = false;
                        } else {
                            calbackStatus = true;
                            mDialog.dismiss();
                            dialog2.show();
                            ///alertDialogOkay("Test successful. You can now click on \"Save and Proceed\" ");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failure(Request request, Response response, FuelError fuelError) {
                    dialog2.dismiss();
                    mDialog.dismiss();
                    calbackStatus = false;
                    alertDialogOkay("Device couldn't reach server [" + sys_oldSyncServerURL + "]. \n Check server address and try again");
                    Log.e("MyCouch", " " + fuelError);

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

    class SyncResource extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL remote = getReplicationURL();
                CountDownLatch replicationDoneSignal = new CountDownLatch(1);
                database = manager.getDatabase("resources");
                repl = database.createPullReplication(remote);
                repl.setContinuous(false);
//// Todo Decide either use design document in app or not
//                repl.setFilter("bell/by_resource");
                repl.setFilter("apps/by_resource");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("_id", OneByOneResID);
                repl.setFilterParams(map);
                repl.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        Log.e("MyCouch", "Current Status " + repl.getStatus());
                        if (repl.isRunning()) {
                            if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                                Log.e("MyCouch", " " + event.getChangeCount());
                                Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                            } else if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")) {
                                mDialog.dismiss();
                                checkAllDocsInDB();
                                libraryButtons[resButtonId].setTextColor(getResources().getColor(R.color.ole_white));
                            } else {
                                mDialog.setMessage("Data transfer error. Check connection to server.");
                            }
                        } else {
                            Log.e("MyCouch", "Document Count " + database.getDocumentCount());
                            mDialog.dismiss();
                            checkAllDocsInDB();
                            libraryButtons[resButtonId].setTextColor(getResources().getColor(R.color.ole_white));

                        }
                    }
                });
                repl.start();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class SyncAllHTMLResource extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL remote = getReplicationURL();
                CountDownLatch replicationDoneSignal = new CountDownLatch(1);
                final Database database;
                database = manager.getDatabase("resources");
                final Replication repl = (Replication) database.createPullReplication(remote);
                repl.setContinuous(false);
                repl.setDocIds(htmlResourceList);
                repl.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        Log.e("MyCouch", "Current Status " + repl.getStatus());
                        if (repl.isRunning()) {
                            if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                                Log.e("MyCouch", " " + event.getChangeCount());
                                Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                                mDialog.setMessage("Downloading HTML resources now .. " + database.getDocumentCount() + "/" + htmlResourceList.size());
                            } else if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")) {
                                mDialog.dismiss();
                                alertDialogOkay("Download Completed");

                            } else {
                                mDialog.setMessage("Data transfer error. Check connection to server.");

                            }
                        } else {
                            Log.e("MyCouch", "Document Count Last " + database.getDocumentCount());
                            mDialog.dismiss();
                            // dbDiagnosticCheck();
                        }
                    }
                });
                repl.start();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(final Boolean success) {
            mDialog.dismiss();
            alertDialogOkay("Download Completed");
        }
    }

    class SyncSingleHTMLResource extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL remote = getReplicationURL();
                CountDownLatch replicationDoneSignal = new CountDownLatch(1);
                final Database database;
                database = manager.getDatabase("resources");
                final Replication repl = (Replication) database.createPullReplication(remote);
                repl.setContinuous(false);
                repl.setDocIds(htmlResourceList);
                repl.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        Log.e("MyCouch", "Current Status " + repl.getStatus());
                        if (repl.isRunning()) {
                            if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                                Log.e("MyCouch", " " + event.getChangeCount());
                                Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                                mDialog.setMessage("Downloading HTML resources now .. " + database.getDocumentCount() + "/" + htmlResourceList.size());
                            } else if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")) {
                                mDialog.dismiss();
                                alertDialogOkay("Download Completed");
                            } else {
                                mDialog.setMessage("Data transfer error. Check connection to server.");
                            }
                        } else {
                            Log.e("MyCouch", "Document Count Last " + database.getDocumentCount());
                            mDialog.dismiss();
                            // dbDiagnosticCheck();
                        }
                    }
                });
                repl.start();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(final Boolean success) {
            mDialog.dismiss();
            alertDialogOkay("Download Completed");
        }
    }

    public URL getReplicationURL() {
        URL url = null;
        try {
            url = new URL(sys_oldSyncServerURL + "/resources");
        } catch (Exception err) {
            Log.d("TAG", "Error with URL ");
        }
        return url;
    }

    public void checkAllDocsInDB() {
        try {
            Database database = manager.getDatabase("resources");
            database.getAllDocs(new QueryOptions());
            Query queryAllDocs = database.createAllDocumentsQuery();
            QueryEnumerator queryEnumerator = queryAllDocs.run();
            for (Iterator<QueryRow> it = queryEnumerator; it.hasNext(); ) {
                QueryRow row = it.next();
                Document document = row.getDocument();
                Revision revision = document.getCurrentRevision();
                Log.d("MyCouch", document.getId() + " : " + revision.getAttachments().size());
                if (document.getId().equalsIgnoreCase(OneByOneResID)) {
                    Resource resource = resourceList.get(resButtonId);
                    resource.setTitle((String) document.getProperty("title"));
                    String OpenWith = (String) document.getProperty("openWith");
                    if (OpenWith.equalsIgnoreCase("Flow Video Player") || OpenWith.equalsIgnoreCase("MP3") || OpenWith.equalsIgnoreCase("PDF.js") || OpenWith.equalsIgnoreCase("HTML")) {
                        resource.setThumbnailUrl(getIconType((String) document.getProperty("openWith")));
                    } else {
                        resource.setThumbnailUrl(getIconType("-"));
                    }
                    Log.d("MyCouch", "Found resource : Making " + (String) document.getProperty("openWith"));
                    resourceList.set(resButtonId, resource);
                }
            }

            Log.d("MyCouch", "done looping over all docs ");
            ///mDialog.dismiss();

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void dbDiagnosticCheck() {
        try {
            Database database = manager.getDatabase("resources");
            database.getAllDocs(new QueryOptions());
            Query queryAllDocs = database.createAllDocumentsQuery();
            QueryEnumerator queryEnumerator = queryAllDocs.run();
            for (Iterator<QueryRow> it = queryEnumerator; it.hasNext(); ) {
                QueryRow row = it.next();
                Document document = row.getDocument();
                Revision revision = document.getCurrentRevision();
                Log.d("MyCouch", document.getId() + " : " + revision.getAttachments().size());
                for (int cnt = 0; cnt <= resourceIdList.length; cnt++) {
                    if (document.getId().equalsIgnoreCase(resourceIdList[cnt])) {
                        libraryButtons[cnt].setTextColor(getResources().getColor(R.color.ole_white));
                        Resource resource = resourceList.get(cnt);
                        resource.setTitle((String) document.getProperty("title"));
                        String OpenWith = (String) document.getProperty("openWith");
                        if (OpenWith.equalsIgnoreCase("Flow Video Player") || OpenWith.equalsIgnoreCase("MP3") || OpenWith.equalsIgnoreCase("PDF.js") || OpenWith.equalsIgnoreCase("HTML")) {
                            resource.setThumbnailUrl(getIconType((String) document.getProperty("openWith")));
                        } else {
                            resource.setThumbnailUrl(getIconType("-"));
                        }
                        Log.d("MyCouch", "Found resource : Making " + (String) document.getProperty("openWith"));
                        resourceList.set(cnt, resource);
                        Log.d("MyCouch", "done looping over all docs ");
                    }
                }

            }


            ///mDialog.dismiss();

        } catch (CouchbaseLiteException e) {
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

    public void openDoc(String docId) {
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            String openwith = (String) res_doc.getProperty("openWith");
            String openResName = (String) res_doc.getProperty("title");
            openFromDiskDirectly = true;
            updateActivityOpenedResources(openResName, docId);
            Log.e("MYAPP", " member opening resource  = " + docId + " and Open with " + openwith);
            List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
/////HTML
            if (openwith.equalsIgnoreCase("HTML")) {
                indexFilePath = null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(docId, (String) attmentNames.get(cnt));
                    }
                    if (indexFilePath != null) {
                        openHTML(indexFilePath);
                    }
                } else {
                    openImage(docId, (String) attmentNames.get(0), getExtension(attmentNames.get(0)));
                }
////PDF
            } else if (openwith.equalsIgnoreCase("Just download")) {
                //// Todo work to get just download
            } else if (openwith.equalsIgnoreCase("PDF.js")) {
                if (openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  " + docId);
                    String filenameOnly = "";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                            if (filenameOnly.equalsIgnoreCase(docId)) {
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
                } else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openPDF(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }
////MP3
            } else if (openwith.equalsIgnoreCase("MP3")) {
                if (openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  " + docId);
                    String filenameOnly = "";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                            if (filenameOnly.equalsIgnoreCase(docId)) {
                                mDialog.dismiss();
                                intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                intent.setDataAndType(Uri.fromFile(f), mimetype);
                                this.startActivity(intent);
                            }
                        }
                    }
                } else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }
/// BELL READER
            } else if (openwith.equalsIgnoreCase("Bell-Reader")) {
                if (openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  " + docId);
                    String filenameOnly = "";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                            if (filenameOnly.equalsIgnoreCase(docId)) {
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
                } else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openPDF(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }
                /////VIDEO
            } else if (openwith.equalsIgnoreCase("Flow Video Player")) {
                if (openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  " + docId);
                    String filenameOnly = "";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                            if (filenameOnly.equalsIgnoreCase(docId)) {
                                mDialog.dismiss();
                                intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                intent.setDataAndType(Uri.fromFile(f), mimetype);
                                this.startActivity(intent);
                            }
                        }
                    }
                } else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }

            } else if (openwith.equalsIgnoreCase("BeLL Video Book Player")) {
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {

                    }
                }
/// Native Video
            } else if (openwith.equalsIgnoreCase("Native Video")) {
                if (openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  " + docId);
                    String filenameOnly = "";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                            if (filenameOnly.equalsIgnoreCase(docId)) {
                                mDialog.dismiss();
                                intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                intent.setDataAndType(Uri.fromFile(f), mimetype);
                                this.startActivity(intent);
                            }
                        }
                    }
                } else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }

            }
        } catch (Exception Er) {
            Log.d("MyCouch", "Opening resource error " + Er.getMessage());
        }
    }

    public void openHTML(String index) {
        final String mainFile = index;
        try {
            try {
                mDialog.dismiss();
                ComponentName componentName = getPackageManager().getLaunchIntentForPackage("org.mozilla.firefox").getComponent();
                Intent firefoxIntent = IntentCompat.makeRestartActivityTask(componentName);
                firefoxIntent.setDataAndType(Uri.parse(mainFile), "text/html");
                startActivity(firefoxIntent);

                //startActivity(intent);
            } catch (Exception err) {
                mDialog.dismiss();
                Log.e("Error", err.getMessage());
                File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                File dst = new File(myDir, "firefox_49_0_multi_android.apk");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        } catch (Exception Er) {
            Er.printStackTrace();
            mDialog.dismiss();
            alertDialogOkay("Couldn't open resource try again");

        }

    }

    public void downloadHTMLContent(String docId, final String fileName) {
        final String myfilename = fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);
            int lth = (int) fileAttachment.getLength();
            try {
                InputStream in = fileAttachment.getContent();
                String root = Environment.getExternalStorageDirectory().toString();
                File newDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2/" + docId);
                if (!newDir.exists()) {
                    newDir.mkdirs();
                }

                File myDir = new File(root + "/ole_temp2/" + docId);
                File dst = new File(myDir, fileAttachment.getName().replace(" ", ""));
                String filepath[] = dst.toString().split("/");
                int defaultLength = myDir.getPath().split("/").length;
                String path = myDir.getPath();
                //Log.e("tag", " Location  "+ dst.toString() + " Default :" + defaultLength + " fpath: "+filepath.length);
                for (int cnt = defaultLength; cnt < (filepath.length - 1); cnt++) {
                    path = path + "/" + filepath[cnt];
                    myDir = new File(path);
                    if (!myDir.exists()) {
                        myDir.mkdirs();
                    }
                }
                try {
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dst));
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = in.read(buff)) > 0) {
                        out.write(buff, 0, len);
                    }

                    Log.e("tag", " Saved " + dst.toString() + " Original length: " + lth);

                    in.close();
                    out.close();
                    if (dst.getName().equalsIgnoreCase("index.html") && (filepath.length - defaultLength) == 1) {
                        indexFilePath = dst.toString();
                    }

                } catch (Exception err) {
                    Log.e("tag", " Saving " + err.getMessage());
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        } catch (Exception Er) {
            Er.printStackTrace();

        }
    }

    public void openImage(String docId, final String fileName, String player) {
        final String myfilename = fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);
            try {
                File src = new File(fileAttachment.getContentURL().getPath());
                InputStream in = new FileInputStream(src);
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                File dst = new File(myDir, fileAttachment.getName().replace(" ", ""));
                try {
                    FileOutputStream out = new FileOutputStream(dst);
                    byte[] buff = new byte[1024];
                    int read = 0;
                    while ((read = in.read(buff)) > 0) {
                        out.write(buff, 0, read);
                    }
                    in.close();
                    out.close();
                    Log.e("tag", " Copied PDF " + dst.toString());
                } catch (Exception err) {
                    err.printStackTrace();
                    mDialog.dismiss();
                    alertDialogOkay("Couldn't open resource try again");
                }
                mDialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(dst), "image/*");
                //Log.e("tag", " URL Path "+ Uri.fromFile(dst).getPath());;
                startActivity(intent);
            } catch (Exception err) {

                mDialog.dismiss();
                alertDialogOkay("Couldn't open resource try again");
            }
        } catch (Exception Er) {
            Er.printStackTrace();

            mDialog.dismiss();
            alertDialogOkay("Couldn't open resource try again");
        }

    }

    public static void mkDirs(File root, List<String> dirs, int depth) {
        if (depth == 0) return;
        for (String s : dirs) {
            File subdir = new File(root, s);
            subdir.mkdir();
            mkDirs(subdir, dirs, depth - 1);
        }
    }

    public void openPDF(String docId, final String fileName, String player) {
        final String myfilename = fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);

            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setTitle("Open Document");
            alertDialog.setMessage("Select which application you wish to open document with");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Adobe PDF Reader", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        File src = new File(fileAttachment.getContentURL().getPath());
                        InputStream in = new FileInputStream(src);
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/ole_temp");
                        if (!myDir.exists()) {
                            myDir.mkdirs();
                        }
                        File dst = new File(myDir, fileAttachment.getName().replace(" ", ""));
                        try {
                            FileOutputStream out = new FileOutputStream(dst);
                            byte[] buff = new byte[1024];
                            int read = 0;
                            while ((read = in.read(buff)) > 0) {
                                out.write(buff, 0, read);
                            }
                            in.close();
                            out.close();
                            Log.e("tag", " Copied PDF " + dst.toString());
                        } catch (Exception err) {
                            err.printStackTrace();
                            mDialog.dismiss();
                            alertDialogOkay("Couldn't open resource try again");
                        } ///
                        mDialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setPackage("com.adobe.reader");
                        intent.setDataAndType(Uri.fromFile(dst), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    } catch (Exception err) {
                        File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                        File dst = new File(myDir, "adobe_reader.apk");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                }
            });

            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "In-App PDF Viewer", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    try {
                        File src = new File(fileAttachment.getContentURL().getPath());
                        InputStream in = new FileInputStream(src);
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/ole_temp");
                        if (!myDir.exists()) {
                            myDir.mkdirs();
                        }
                        File dst = new File(myDir, fileAttachment.getName().replace(" ", ""));
                        try {
                            FileOutputStream out = new FileOutputStream(dst);
                            byte[] buff = new byte[1024];
                            int read = 0;
                            while ((read = in.read(buff)) > 0) {
                                out.write(buff, 0, read);
                            }
                            in.close();
                            out.close();
                            Log.e("tag", " Copied PDF " + dst.toString());
                        } catch (Exception err) {
                            err.printStackTrace();
                            mDialog.dismiss();
                            alertDialogOkay("Couldn't open resource try again");
                        } ///

                        mDialog.dismiss();
                        Intent intent = new Intent(FullscreenActivity.this, MyPdfViewerActivity.class);
                        Log.e("tag", " URL Path " + Uri.fromFile(dst).getPath());
                        intent.putExtra(net.sf.andpdf.pdfviewer.PdfViewerActivity.EXTRA_PDFFILENAME, Uri.fromFile(dst).getPath());
                        startActivity(intent);


                    } catch (Exception err) {
                        mDialog.dismiss();
                        alertDialogOkay("Couldn't open resource try again");
                    }
                }
            });
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {


                }
            });
            alertDialog.show();

        } catch (Exception Er) {
            Er.printStackTrace();

        }

    }
    public void openAudioVideo(String docId, String fileName, String player) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {

            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);
            File src = new File(fileAttachment.getContentURL().getPath());
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/ole_temp");
            deleteDirectory(myDir);
            myDir.mkdirs();
            String diskFileName = fileAttachment.getName();
            diskFileName = diskFileName.replace(" ", "");
            File dst = new File(myDir, diskFileName);

            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            dst.setReadable(true);
            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(dst).toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Log.e("tag", "- " + mimetype + " - ");

            if (mimetype == "audio/mpeg") {
                mDialog.dismiss();
                intent.setDataAndType(Uri.fromFile(dst), mimetype);
                this.startActivity(intent);
            } else {
                try {
                    mDialog.dismiss();
                    intent.setDataAndType(Uri.fromFile(dst), mimetype);
                    this.startActivity(intent);
                } catch (Exception Er) {
                    Log.e("tag", Er.getMessage());
                    mDialog.dismiss();
                    alertDialogOkay("Couldn't open resource try again");
                }
            }
        } catch (Exception Er) {
            Log.e("tag", Er.getMessage());
            mDialog.dismiss();
            alertDialogOkay("Couldn't open resource try again");
        }

    }

    public String getExtension(final String filename) {
        if (filename == null) {
            return null;
        }
        final String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        final int afterLastBackslash = afterLastSlash.lastIndexOf('\\') + 1;
        final int dotIndex = afterLastSlash.indexOf('.', afterLastBackslash);
        return (dotIndex == -1) ? "" : afterLastSlash.substring(dotIndex + 1);
    }

    public boolean updateActivityOpenedResources(String resource_name, String resourceid) {
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        Database activityLog;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activityLog = manager.getDatabase("activitylog");
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            //This is for setting the MAC address if it is being run in a android emulator.
            String m_WLANMAC;
            m_WLANMAC = wm.getConnectionInfo().getMacAddress();
            if(m_WLANMAC == null) {
                m_WLANMAC = "mymac";
            }

            Document retrievedDocument = activityLog.getDocument(m_WLANMAC);
            Map<String, Object> properties = retrievedDocument.getProperties();
            if ((ArrayList<String>) properties.get("female_opened") != null) {
                try {
                    ArrayList female_opened = (ArrayList<String>) properties.get("female_opened");
                    ArrayList male_opened = (ArrayList<String>) properties.get("male_opened");
                    ArrayList resources_names = (ArrayList<String>) properties.get("resources_names");
                    ArrayList resources_opened = (ArrayList<String>) properties.get("resources_opened");
                    Log.e("MyCouch", "Option 1 "+ sys_usergender.toLowerCase());
                    if (sys_usergender.toLowerCase().equalsIgnoreCase("female")) {
                        female_opened.add(1);
                        male_opened.add(0);
                    } else {
                        female_opened.add(0);
                        male_opened.add(1);
                    }
                    resources_names.add(resource_name);
                    resources_opened.add(resourceid);
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("female_opened", female_opened);
                    newProperties.put("male_opened", male_opened);
                    newProperties.put("resources_names", resources_names);
                    newProperties.put("resources_opened", resources_opened);
                    retrievedDocument.putProperties(newProperties);
                    Log.e("MyCouch", "Saved resource open in local Activity Log "+newProperties.toString());
                    return true;
                } catch (Exception err) {
                    Log.e("MyCouch", "Option 1 Failed " + err.getMessage());
                    return false;
                }
            } else {
                try {
                    Log.e("MyCouch", "Option 2 gender is "+ sys_usergender.toLowerCase());
                    Document newdocument = activityLog.getDocument(m_WLANMAC);
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    ArrayList female_opened = new ArrayList<String>();
                    ArrayList male_opened = new ArrayList<String>();
                    ArrayList resources_names = new ArrayList<String>();
                    ArrayList resources_opened = new ArrayList<String>();
                    if (sys_usergender.toLowerCase().equalsIgnoreCase("female")) {
                        female_opened.add(1);
                        male_opened.add(0);
                    } else {
                        female_opened.add(0);
                        male_opened.add(1);
                    }
                    resources_names.add(resource_name);
                    resources_opened.add(resourceid);
                    newProperties.put("female_opened", female_opened);
                    newProperties.put("male_opened", male_opened);
                    newProperties.put("resources_names", resources_names);
                    newProperties.put("resources_opened", resources_opened);
                    newdocument.putProperties(newProperties);
                    Log.e("MyCouch", "Saved resource open in local Activity Log "+newProperties.toString());
                    return true;
                } catch (Exception er) {
                    Log.e("MyCouch", "Option 2 Failed" + er.getMessage());
                    return false;
                }
            }
            /*
                try {
                    Log.e("MyCouch", "Option 1b");
                    Document newdocument = activityLog.getDocument(m_WLANMAC);
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    ArrayList female_opened = new ArrayList<String>();
                    ArrayList male_opened = new ArrayList<String>();
                    ArrayList resources_names = new ArrayList<String>();
                    ArrayList resources_opened = new ArrayList<String>();
                    if (sys_usergender.toLowerCase().equalsIgnoreCase("female")) {
                        female_opened.add(1);
                        male_opened.add(0);
                    } else {
                        female_opened.add(0);
                        male_opened.add(1);
                    }
                    resources_names.add(resource_name);
                    resources_opened.add(resourceid);
                    newProperties.put("female_opened", female_opened);
                    newProperties.put("male_opened", male_opened);
                    newProperties.put("resources_names", resources_names);
                    newProperties.put("resources_opened", resources_opened);
                    newdocument.putProperties(newProperties);
                    Log.e("MyCouch", "Saved resource open in local Activity Log ");
                    return true;
                }catch(Exception err) {
                    Log.e("MyCouch", "Opetion 1b Failed : " + err.getMessage());
                    return false;
                }
            */
        } catch (Exception err) {
            Log.e("MyCouch", "Updating Activity Log : " + err.getMessage());
            return false;
        }
    }

    public boolean updateActivityRatingResources(float rate, String resourceid) {
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        Database activityLog;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activityLog = manager.getDatabase("activitylog");
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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
                    Map<String, Object> newProperties = new HashMap<String, Object>();
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
                        Map<String, Object> newProperties = new HashMap<String, Object>();
                        newProperties.putAll(retrievedDocument.getProperties());
                        ArrayList female_rating = new ArrayList<String>();
                        ArrayList female_timesRated = new ArrayList<String>();
                        ArrayList male_rating = new ArrayList<String>();
                        ArrayList male_timesRated = new ArrayList<String>();
                        ArrayList resourcesIds = new ArrayList<String>();
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
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    ArrayList female_rating = new ArrayList<String>();
                    ArrayList female_timesRated = new ArrayList<String>();
                    ArrayList male_rating = new ArrayList<String>();
                    ArrayList male_timesRated = new ArrayList<String>();
                    ArrayList resourcesIds = new ArrayList<String>();
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

    public void animateLayoutBars() {

        /////////  Layout Slide in Animation from bottom to up
        final LinearLayout libraryLayout = (LinearLayout) findViewById(R.id.layoutMasterLibrary);
        libraryLayout.setVisibility(View.INVISIBLE);
        final TranslateAnimation translateAnimationLibrary = new TranslateAnimation(-1000, 0, 0, 0);
        translateAnimationLibrary.setInterpolator(new LinearInterpolator());
        translateAnimationLibrary.setDuration(500);
        translateAnimationLibrary.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                libraryLayout.setVisibility(View.VISIBLE);


            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //sd_Slidin.stop();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        final LinearLayout coursesLayout = (LinearLayout) findViewById(R.id.layoutMasterCoursesProgress);
        coursesLayout.setVisibility(View.INVISIBLE);
        final TranslateAnimation translateAnimationCourses = new TranslateAnimation(1000, 0, 0, 0);
        translateAnimationCourses.setInterpolator(new LinearInterpolator());
        translateAnimationCourses.setDuration(500);
        translateAnimationCourses.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                coursesLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                libraryLayout.startAnimation(translateAnimationLibrary);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        final LinearLayout MeetupLayout = (LinearLayout) findViewById(R.id.layoutMasterMeetup);
        MeetupLayout.setVisibility(View.INVISIBLE);
        final TranslateAnimation translateAnimationMeetup = new TranslateAnimation(-1000, 0, 0, 0);
        translateAnimationMeetup.setInterpolator(new LinearInterpolator());
        translateAnimationMeetup.setDuration(500);
        translateAnimationMeetup.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                MeetupLayout.setVisibility(View.VISIBLE);


            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //sd_Slidin = null;
                coursesLayout.startAnimation(translateAnimationCourses);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        final LinearLayout TutorsLayout = (LinearLayout) findViewById(R.id.layoutMasterTutors);
        TutorsLayout.setVisibility(View.INVISIBLE);
        final TranslateAnimation translateAnimationTutors = new TranslateAnimation(1000, 0, 0, 0);
        translateAnimationTutors.setInterpolator(new LinearInterpolator());
        translateAnimationTutors.setDuration(500);
        translateAnimationTutors.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                TutorsLayout.setVisibility(View.VISIBLE);
                //sd_Slidin.start();

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                MeetupLayout.startAnimation(translateAnimationMeetup);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        TutorsLayout.startAnimation(translateAnimationTutors);


    }

    public void toggleTopUserInfo() {
        final LinearLayout userInfoLayout = (LinearLayout) findViewById(R.id.layoutUserInfo);
        //userInfoLayout.setVisibility(View.INVISIBLE);

        final TranslateAnimation translateShowUserInfo = new TranslateAnimation(0, 0, 0, -200);
        translateShowUserInfo.setInterpolator(new LinearInterpolator());
        translateShowUserInfo.setDuration(500);
        translateShowUserInfo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                userInfoLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                userInfoDisplayed = true;
                //userInfoLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        final TranslateAnimation translateHideUserInfo = new TranslateAnimation(0, 0, -100, 0);
        translateHideUserInfo.setInterpolator(new LinearInterpolator());
        translateHideUserInfo.setDuration(500);
        translateHideUserInfo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                userInfoLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                userInfoDisplayed = false;
                //userInfoLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        ImageView imgProfileImage = (ImageView) findViewById(R.id.imageProfilePics);
        imgProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userInfoDisplayed) {
                    userInfoLayout.startAnimation(translateShowUserInfo);
                } else {
                    userInfoLayout.startAnimation(translateHideUserInfo);
                }
            }
        });
    }

    public void updateUI() {
        TextView lblName = (TextView) findViewById(R.id.lblName);
        lblName.setText(" " + sys_userfirstname + " " + sys_userlastname);
        TextView lblVisits = (TextView) findViewById(R.id.lblVisits);
        if (sys_uservisits == "") {
            //// Todo change word 'Visits' to be read from languages
            lblVisits.setText("" + sys_uservisits_Int + " Visits");
        } else {
            lblVisits.setText("" + sys_uservisits + " Visits");
        }
        TextView lblServerName = (TextView) findViewById(R.id.lbl_SeverName);
        lblServerName.setText("" + sys_servername.toUpperCase());

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
    public void downloadOneResourceToDisk() {
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
            Log.e("MyCouch", "Here Now");
            if (dbClient.contains(OneByOneResID)) {
                /// Handle with Json
                JsonObject jsonObject = dbClient.find(JsonObject.class, OneByOneResID);
                JsonObject jsonAttachments = jsonObject.getAsJsonObject("_attachments");
                String openWith = (String) jsonObject.get("openWith").getAsString();
                Log.e("MyCouch", "Open With -- " + openWith);
                if (!openWith.equalsIgnoreCase("HTML")) {
                    JSONObject _attachments = new JSONObject(jsonAttachments.toString());
                    Iterator<String> keys = _attachments.keys();
                    if (keys.hasNext()) {
                        String key = (String) keys.next();
                        Log.e("MyCouch", "-- " + key);
                        String encodedkey = URLEncoder.encode(key, "utf-8");
                        File file = new File(encodedkey);
                        String extension = encodedkey.substring(encodedkey.lastIndexOf("."));
                        String diskFileName = OneByOneResID + extension;
                        //createResourceDoc(OneByOneResID, jsonObject.get("title").getAsString(), jsonObject.get("openWith").getAsString());
                        //downloadWithDownloadManagerSingleFile(sys_oldSyncServerURL + "/resources/" + OneByOneResID + "/" + encodedkey, diskFileName);
                    }
                } else {
                    Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS ");
                    htmlResourceList.add(OneByOneResID);
                    if (allhtmlDownload < htmlResourceList.size()) {
                        try {
                            URL remote = getReplicationURL();
                            CountDownLatch replicationDoneSignal = new CountDownLatch(1);
                            final Database database;
                            database = manager.getDatabase("resources");
                            final Replication repl = (Replication) database.createPullReplication(remote);
                            repl.setContinuous(false);
                            repl.setDocIds(htmlResourceList);
                            repl.addChangeListener(new Replication.ChangeListener() {
                                @Override
                                public void changed(Replication.ChangeEvent event) {
                                    Log.e("MyCouch", "Current Status " + repl.getStatus());
                                    if (repl.isRunning()) {
                                        if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                                            Log.e("MyCouch", " " + event.getChangeCount());
                                            Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                                            mDialog.setMessage("Downloading HTML resources now .. " + database.getDocumentCount() + "/" + htmlResourceList.size());
                                        } else if (repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")) {
                                            mDialog.dismiss();
                                            alertDialogOkay("Download Completed");
                                        } else {
                                            mDialog.setMessage("Data transfer error. Check connection to server.");
                                        }
                                    } else {
                                        Log.e("MyCouch", "Document Count Last " + database.getDocumentCount());
                                        mDialog.dismiss();
                                        // dbDiagnosticCheck();
                                    }
                                }
                            });
                            repl.start();
                        } catch (CouchbaseLiteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mDialog.dismiss();
                        alertDialogOkay("Download Completed");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MyCouch", "Download this resource error " + e.getMessage());
            mDialog.dismiss();
            alertDialogOkay("Error downloading file, check connection and try again");
        }
    }

    /// Todo Review Code and test class
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
                        Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS ");
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
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("MyCouch", "Download this resource error " + e.getMessage());
                mDialog.dismiss();
                alertDialogOkay("Error downloading file, check connection and try again");
                return null;
            }
            return null;
        }
    }

    public void callSuncOneHTMLResource() {
        SyncSingleHTMLResource ssHTML = new SyncSingleHTMLResource();
        ssHTML.execute();
    }
/*
    class downloadAllResourceToDisk extends AsyncTask<String, Void, Boolean> {
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
                if (dbClient.contains(resourceIdList[allresDownload])) {
                    /// Handle with Json
                    JsonObject jsonObject = dbClient.find(JsonObject.class, resourceIdList[allresDownload]);
                    JsonObject jsonAttachments = jsonObject.getAsJsonObject("_attachments");
                    final String openWith = (String) jsonObject.get("openWith").getAsString();
                    final String title = jsonObject.get("title").getAsString();
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
                            final String diskFileName = resourceIdList[allresDownload] + extension;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadWithDownloadManagerSingleFile(sys_oldSyncServerURL + "/resources/" + resourceIdList[allresDownload] + "/" + encodedkey, diskFileName);
                                    createResourceDoc(resourceIdList[allresDownload], title, openWith);
                                }
                            });
                        }
                    } else {
                        Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS ");
                        htmlResourceList.add(resourceIdList[allresDownload]);
                        if (allresDownload < libraryButtons.length) {
                            allresDownload++;
                            if (resourceTitleList[allresDownload] != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDialog.setMessage("Download please wait ...");
                                        new downloadAllResourceToDisk().execute();
                                    }
                                });
                            } else {
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
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("MyCouch", "Download this resource error " + e.getMessage());
                mDialog.dismiss();
                alertDialogOkay("Error downloading file, check connection and try again");
                return null;
            }
            return null;
        }

        public void callSuncOneHTMLResource() {
            SyncSingleHTMLResource ssHTML = new SyncSingleHTMLResource();
            ssHTML.execute();
        }
    }
    */
    class downloadAllResourceToDisk extends AsyncTask<String, Void, Boolean> {
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
                if (dbClient.contains(resourceIdList[allresDownload])) {
                    /// Handle with Json
                    JsonObject jsonObject = dbClient.find(JsonObject.class, resourceIdList[allresDownload]);
                    JsonObject jsonAttachments = jsonObject.getAsJsonObject("_attachments");
                    final String openWith = (String) jsonObject.get("openWith").getAsString();
                    final String title = jsonObject.get("title").getAsString();
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
                            final String diskFileName = resourceIdList[allresDownload] + extension;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadWithDownloadManagerSingleFile(sys_oldSyncServerURL + "/resources/" + resourceIdList[allresDownload] + "/" + encodedkey, diskFileName);
                                    createResourceDoc(resourceIdList[allresDownload], title, openWith);
                                }
                            });
                        }
                    } else {
                        Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS ");
                        htmlResourceList.add(resourceIdList[allresDownload]);
                        if (allresDownload < libraryButtons.length) {
                            allresDownload++;
                            if (resourceTitleList[allresDownload] != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDialog.setMessage("Download please wait ...");
                                        new downloadAllResourceToDisk().execute();
                                    }
                                });
                            } else {
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
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("MyCouch", "Download this resource error " + e.getMessage());
                mDialog.dismiss();
                alertDialogOkay("Error downloading file, check connection and try again");
                return null;
            }
            return null;
            /*
            Fuel ful = new Fuel();
            ful.get(sys_oldSyncServerURL+"/resources/" + resourceIdList[allresDownload]).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                @Override
                public void success(Request request, Response response, String s) {
                    try {
                        try {
                            openFromDiskDirectly = true;
                            jsonData = new JSONObject(s);
                            String openWith = (String) jsonData.get("openWith");
                            Log.e("MyCouch", "Open With -- " + openWith);
                            if(!openWith.equalsIgnoreCase("HTML")) {
                                JSONObject _attachments = (JSONObject) jsonData.get("_attachments");
                                Iterator<String> keys = _attachments.keys();
                                if (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    Log.e("MyCouch", "-- " + key);
                                    String encodedkey = URLEncoder.encode(key, "utf-8");
                                    File file = new File(encodedkey);
                                    String extension = encodedkey.substring(encodedkey.lastIndexOf("."));
                                    String diskFileName = resourceIdList[allresDownload] + extension;
                                    createResourceDoc(resourceIdList[allresDownload],(String) jsonData.get("title"), (String) jsonData.get("openWith"));
                                    downloadWithDownloadManager(sys_oldSyncServerURL + "/resources/" + resourceIdList[allresDownload] + "/" + encodedkey, diskFileName);
                                }
                            }else{
                                Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS " );
                                htmlResourceList.add(resourceIdList[allresDownload]);
                                if(allresDownload<libraryButtons.length) {
                                    allresDownload++;
                                    if(resourceTitleList[allresDownload]!=null) {
                                        new downloadAllResourceToDisk().execute();
                                    }else{
                                        if(allhtmlDownload<htmlResourceList.size()) {
                                            new SyncAllHTMLResource().execute();
                                        }else{
                                            mDialog.dismiss();
                                            alertDialogOkay("Download Completed");
                                        }
                                    }
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
                    Log.e("MyCouch", " "+fuelError);
                }
            });
            return null;

            */

        }
    }

    public void downloadWithDownloadManager(String fileURL, String FileName) {
        String url = fileURL;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(resourceIdList[allresDownload] + "-" + resourceTitleList[allresDownload]);
        request.setTitle(resourceTitleList[allresDownload]);
// in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        Log.e("MyCouch", " Destination is " + FileName);
        request.setDestinationInExternalPublicDir("ole_temp", FileName);

// get download service and enqueue file
        mDialog.setMessage("Downloading  \" " + resourceTitleList[allresDownload] + " \" . please wait...");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = downloadManager.enqueue(request);
    }

    public void downloadWithDownloadManagerSingleFile(String fileURL, String FileName) {
        String url = fileURL;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(clicked_rs_ID + "-" + clicked_rs_title);
        request.setTitle(clicked_rs_title);
// in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        Log.e("MyCouch", " Destination is " + FileName);
        request.setDestinationInExternalPublicDir("ole_temp", FileName);

// get download service and enqueue file
        mDialog.setMessage("Downloading  \" " + clicked_rs_title + " \" . please wait...");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = downloadManager.enqueue(request);
    }

    public void createResourceDoc(String manualResId, String manualResTitle, String manualResopenWith) {
        Database database = null;
        try {
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
                saveRating((int) ratingBar.getRating(), String.valueOf(txtComment.getText()), resourceID);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void saveRating(int rate, String comment, String resourceId) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        Database resourceRating;
        int doc_rating;
        int doc_timesRated;
        ArrayList<String> commentList = new ArrayList<String>();
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
                    Map<String, Object> newProperties = new HashMap<String, Object>();
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
                Map<String, Object> newProperties = new HashMap<String, Object>();
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

    public void checkResourceOpened() {
        if (openedResource) {
            rateResourceDialog(openedResourceId, openedResourceTitle);
            openedResource = false;
        }
    }
    class checkServerConnection extends AsyncTask<String, Void, String> {
        private Exception exception;
        protected String doInBackground(String... urls) {
            runOnUiThread(new Runnable() {
                public void run()
                {
                    final Fuel ful = new Fuel();
                    final ProgressDialog connectionDialog = new ProgressDialog(context);
                    connectionDialog.setMessage("Checking Connection. Please wait ..");
                    connectionDialog.setCancelable(false);
                    connectionDialog.show();
                    ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                        @Override
                        public void success(Request request, Response response, String s) {
                            try {
                                List<String> myList = new ArrayList<String>();
                                myList.clear();
                                myList = Arrays.asList(s.split(","));
                                Log.e("MyCouch", "-- " + myList.size());
                                if (myList.size() < 8) {
                                    alertDialogOkay("Check WiFi connection and try again");
                                    connectionDialog.dismiss();
                                } else {
                                    connectionDialog.dismiss();
                                    final ProgressDialog progressDialog = ProgressDialog.show(FullscreenActivity.this, "Please wait ...", "Syncing", false);
                                    //////////////////////////////
                                    URL url = new URL(sys_oldSyncServerURL+"/shelf");
                                    database = manager.getDatabase("shelf");
                                    final Replication pull = database.createPullReplication(url);
                                    final Replication push = database.createPushReplication(url);
                                    pull.setContinuous(false);
                                    push.setContinuous(false);
                                    pull.addChangeListener(new Replication.ChangeListener() {
                                        @Override
                                        public void changed(Replication.ChangeEvent event) {
                                            boolean active = (pull.getStatus() == Replication.ReplicationStatus.REPLICATION_ACTIVE) ||
                                                    (push.getStatus() == Replication.ReplicationStatus.REPLICATION_ACTIVE);
                                            if (!active) {
                                                progressDialog.dismiss();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        alertDialogOkay("Tablet Updated Successfully. Logout and Login again to see changes");
                                                    }
                                                });
                                            } else {
                                                double total = push.getCompletedChangesCount() + pull.getCompletedChangesCount();
                                                progressDialog.setMax((int) total);
                                                progressDialog.setProgress(push.getChangesCount() + pull.getChangesCount());
                                            }
                                        }
                                    });
                                    pull.start();
                                    push.start();
                                }
                            } catch (Exception e) {
                                connectionDialog.dismiss();
                                alertDialogOkay("Device couldn't reach server. Check and try again");
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void failure(Request request, Response response, FuelError fuelError) {
                            connectionDialog.dismiss();
                            alertDialogOkay("Device couldn't reach server. Check and try again");
                            Log.e("MyCouch", " " + fuelError);
                        }
                    });
                }
            });
            return "";
        }
        protected void onPostExecute(String data) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

    public void openServerPage(String pageUrl){
        URI uri = URI.create(sys_oldSyncServerURL);
        String url_Scheme = uri.getScheme();
        String url_Host = uri.getHost();
        int url_Port = uri.getPort();
        String url_user = null, url_pwd = null;
        if(sys_oldSyncServerURL.contains("@")){
            String[] userinfo = uri.getUserInfo().split(":");
            url_user = userinfo[0];
            url_pwd = userinfo[1];
        }
        final String mainFile = url_Scheme+"://"+url_Host+":"+url_Port+""+ pageUrl;
        Log.e("Error", mainFile+" --- URL");
        try {
            try {
                mDialog.dismiss();
                ComponentName componentName = getPackageManager().getLaunchIntentForPackage("org.mozilla.firefox").getComponent();
                Intent firefoxIntent = IntentCompat.makeRestartActivityTask(componentName);
                firefoxIntent.setDataAndType(Uri.parse(mainFile), "text/html");
                startActivity(firefoxIntent);
            } catch (Exception err) {
                mDialog.dismiss();
                File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                File dst = new File(myDir, "firefox_49_0_multi_android.apk");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception Er) {
            mDialog.dismiss();
            Er.printStackTrace();
            alertDialogOkay("Couldn't open page try again");
        }
    }
}
