package org.ole.learning.planet.planetlearning;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.tdscientist.shelfview.BookModel;
import com.tdscientist.shelfview.ShelfView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.couchbase.lite.replicator.Replication;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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


public class Main_Dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {
    private View mContentView;
    final Context context = this;

    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;

    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
            sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
            sys_usergender, sys_uservisits,sys_servername,sys_serverversion = "";
    int sys_uservisits_Int=0;
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    Boolean sys_appInDemoMode = false;

    Object[] sys_membersWithResource;
    boolean userShelfSynced =true;
    boolean synchronizing = true;
    JSONObject jsonData;
    Database dbResources;
    int syncCnt,resourceNo,allresDownload,allhtmlDownload=0;
    AndroidContext androidContext;
    Replication pull;
    Manager manager;
    boolean status_SyncOneByOneResource = false;
    private ProgressDialog mDialog;
    String indexFilePath;
    String openedResourceId,openedResourceTitle="";
    boolean openedResource =false;
    boolean openFromDiskDirectly = false;
    boolean singleFiledownload =false;
    boolean openFromOnlineServer =false;
    boolean clicked_rs_status;
    String clicked_rs_title,clicked_rs_ID ;
    String onlinecouchresource;

    private static final String TAG = "MYAPP";
    CouchViews chViews = new CouchViews();
    String resourceIdList[];
    String resourceTitleList[];
    int rsLstCnt=0;
    Button[] libraryButtons;
    Dialog dialog2;
    String OneByOneResID="";
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
    Boolean initialActivityLoad =false;


    private List<Resource> resourceList = new ArrayList<Resource>();
    private List<String> resIDArrayList = new ArrayList<String>();
    private ListView listView;
    private CustomListAdapter adapter;
    Boolean calbackStatus,syncALLInOneStarted=false;
    Button dialogBtnDownoadAll,dialogBtnDownoadFile,dialogBtnOpenFileOnline;
    private long enqueue;
    int resourceCntr,attachmentLength;
    List<String> htmlResourceList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_dashboard);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        restorePref();
        androidContext = new AndroidContext(this);
        initialActivityLoad=true;

        resourceList.clear();
        resIDArrayList.clear();
        LoadShelfResourceList();

        ///Update user info
        //////updateUI();

        /// Todo :- Sync user usage data back to server


        if (!userShelfSynced){
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

        /*
        sd_Slidin = MediaPlayer.create(this, R.raw.wave);

        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnMyLibrary = (Button) findViewById(R.id.btnLibrary);
        btnMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateLibraryDialogList();
            }
        });

        Button btnSettings = (Button) findViewById(R.id.btnSetting);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingDialogList();
            }
        });

        TextView lblMyLibrary = (TextView) findViewById(R.id.lblMyLibrary);
        lblMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateLibraryDialogList();
            }
        });
        */




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
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int", 0);
        sys_singlefilestreamdownload = settings.getBoolean("pf_singlefilestreamdownload", true);
        sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload", true);
        sys_servername = settings.getString("pf_server_name", " ");
        sys_serverversion = settings.getString("pf_server_version", " ");

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
            resourceTitleList= new String[results.getCount()];
            rsLstCnt = 0;
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                Map<String, Object> resource_properties = null;
                if(memberId.equals(properties.get("memberId"))) {
                    String myresTitile = ((String) properties.get("resourceTitle"));
                    String myresId = ((String) properties.get("resourceId"));
                    String myresType,myresDec,myresExt = "";
                    resourceTitleList[rsLstCnt]=myresTitile;
                    resourceIdList[rsLstCnt]=myresId;
                    resIDArrayList.add(myresId);
                    Log.e("tag", "MEMBER ID "+ properties.get("resourceTitle"));
                    try {
                        Document resource_doc = resource_Db.getExistingDocument((String) properties.get("resourceId"));
                        Log.e("tag", "RES ID "+ properties.get("resourceId"));
                        try {
                            resource_properties = resource_doc.getProperties();
                        }catch(Exception errs){
                            Log.e("tag", "OBJECT ERROR "+ errs.toString());
                        }
                        //myresTitile = (String) resource_properties.get("title")+"";
                        //myresId = (String) properties.get("resourceId")+"";
                        myresDec = resource_properties.get("author") +"";
                        myresType = resource_properties.get("averageRating") +"";
                        myresExt = resource_properties.get("openWith") +"";
                        rsLstCnt++;
                    }catch(Exception err){
                        Log.e("tag", "ERROR "+ err.getMessage());
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
            LinearLayout row2 = findViewById(R.id.layholder_library);
            libraryButtons = new Button[rsLstCnt];
            for( int ButtonCnt=0;ButtonCnt< rsLstCnt;ButtonCnt++) {
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
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(170,MATCH_PARENT);
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
                    Document resource_doc = resource_Db.getExistingDocument(resourceIdList[ButtonCnt]);
                    Map<String, Object> resource_properties = resource_doc.getProperties();
                    Log.e("tag", "RES ID " + resource_properties.get("resourceId"));
                    libraryButtons[ButtonCnt].setTextColor(getResources().getColor(R.color.ole_white));
                }catch(Exception errs){
                    libraryButtons[ButtonCnt].setTextColor(getResources().getColor(R.color.ole_yellow));
                    Log.e("tag", "OBJECT ERROR "+ errs.toString());
                }

                libraryButtons[ButtonCnt].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(libraryButtons[view.getId()].getCurrentTextColor()==getResources().getColor(R.color.ole_yellow)){
                            MaterialClickDialog(false,resourceTitleList[view.getId()],resourceIdList[view.getId()],view.getId());
                        }else{
                            mDialog = new ProgressDialog(context);
                            mDialog.setMessage("Opening please "+resourceTitleList[view.getId()]+" wait...");
                            mDialog.setCancelable(true);
                            mDialog.show();
                            openedResourceId=resourceIdList[view.getId()];
                            openedResourceTitle = resourceTitleList[view.getId()];
                            openedResource =true;
                            openDoc(resourceIdList[view.getId()]);
                            Log.e("MyCouch", "Clicked to open "+ resourceIdList[view.getId()]);

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
    public void MaterialClickDialog(boolean online,String title,String resId,int buttonPressedId){
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
        TextView txtResourceId = dialog2.findViewById(R.id.txtResourceID);
        txtResourceId.setText(title);
        //// Open material online
        dialogBtnOpenFileOnline = dialog2.findViewById(R.id.btnOpenOnline);
        dialogBtnOpenFileOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo Open resource in a browser
                dialog2.dismiss();
                OneByOneResID = clicked_rs_ID;
                Fuel ful = new Fuel();
                onlinecouchresource = sys_oldSyncServerURL+"/resources/" + OneByOneResID;
                Fuel.get(sys_oldSyncServerURL+"/resources/" + OneByOneResID).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
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
                                if(!openWith.equalsIgnoreCase("HTML")) {
                                    Iterator<String> keys = _attachments.keys();
                                    if (keys.hasNext()) {
                                        String key = keys.next();
                                        String encodedkey = URLEncoder.encode(key, "utf-8");
                                        onlinecouchresource = onlinecouchresource+"/"+encodedkey;
                                        mDialog.dismiss();
                                        openHTML(onlinecouchresource);
                                    }
                                }else{
                                    if(_attachments.length() <= 1){
                                        Iterator<String> keys = _attachments.keys();
                                        if (keys.hasNext()) {
                                            String key = keys.next();
                                            String encodedkey = URLEncoder.encode(key, "utf-8");
                                            onlinecouchresource = onlinecouchresource+"/"+encodedkey;
                                            mDialog.dismiss();
                                            openHTML(onlinecouchresource);
                                        }
                                    }else {
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
                        Log.e("MyCouch", " "+fuelError);
                    }
                });
            }
        });

        //// Download Only selected file
        dialogBtnDownoadFile = dialog2.findViewById(R.id.btnDownloadFile);
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
                    singleFiledownload=true;
                    // alertDialogOkay(OneByOneResID+"");
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
        dialogBtnDownoadAll= dialog2.findViewById(R.id.btnDownloadAll);
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
                    mDialog.setMessage("Downloading resource, please wait..."+resourceTitleList[allresDownload]);
                    mDialog.setCancelable(true);
                    mDialog.show();
                    htmlResourceList.clear();
                    allhtmlDownload=0;
                    //// Todo Decide which option is best
                    singleFiledownload=false;
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
            Fuel.get(sys_oldSyncServerURL+"/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                @Override
                public void success(Request request, Response response, String s) {
                    try {
                        List<String> myList = new ArrayList<String>();
                        myList.clear();
                        myList=Arrays.asList(s.split(","));
                        Log.e("MyCouch", "-- "+myList.size());
                        if(myList.size() < 8){
                            mDialog.dismiss();
                            dialog2.dismiss();
                            alertDialogOkay("Check the server address again. Saved address isn't the BeLL server");
                            calbackStatus =  false;
                        }else{
                            calbackStatus=true;
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
                    calbackStatus =  false;
                    alertDialogOkay("Device couldn't reach server ["+sys_oldSyncServerURL+"]. \n Check server address and try again");
                    Log.e("MyCouch", " "+fuelError);

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
                repl.setFilter("apps/by_resource");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("_id", OneByOneResID);
                repl.setFilterParams(map);
                repl.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        Log.e("MyCouch", "Current Status "+repl.getStatus());
                        if(repl.isRunning()){
                            if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                                Log.e("MyCouch", " " + event.getChangeCount());
                                Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                            }else if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")){
                                mDialog.dismiss();
                                checkAllDocsInDB();
                                libraryButtons[resButtonId].setTextColor(getResources().getColor(R.color.ole_white));
                            }
                            else{
                                mDialog.setMessage("Data transfer error. Check connection to server.");
                            }
                        }else {
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
                final Replication repl = database.createPullReplication(remote);
                repl.setContinuous(false);
                repl.setDocIds(htmlResourceList);
                repl.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        Log.e("MyCouch", "Current Status "+repl.getStatus());
                        if(repl.isRunning()){
                            if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                                Log.e("MyCouch", " " + event.getChangeCount());
                                Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                                mDialog.setMessage("Downloading HTML resources now .. "+ database.getDocumentCount() +"/"+ htmlResourceList.size());
                            }else if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")){
                                mDialog.dismiss();
                                alertDialogOkay("Download Completed");

                            }
                            else{
                                mDialog.setMessage("Data transfer error. Check connection to server.");

                            }
                        }else {
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
                final Replication repl = database.createPullReplication(remote);
                repl.setContinuous(false);
                repl.setDocIds(htmlResourceList);
                repl.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        Log.e("MyCouch", "Current Status "+repl.getStatus());
                        if(repl.isRunning()){
                            if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                                Log.e("MyCouch", " " + event.getChangeCount());
                                Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                                mDialog.setMessage("Downloading HTML resources now .. "+ database.getDocumentCount() +"/"+ htmlResourceList.size());
                            }else if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")){
                                mDialog.dismiss();
                                alertDialogOkay("Download Completed");
                            }
                            else{
                                mDialog.setMessage("Data transfer error. Check connection to server.");
                            }
                        }else {
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

    public URL getReplicationURL(){
        URL url=null;
        try {
            url = new URL(sys_oldSyncServerURL + "/resources");
        }catch(Exception err){
            Log.d("TAG", "Error with URL ");
        }
        return url;
    }

    public void checkAllDocsInDB(){
        try {
            Database database = manager.getDatabase("resources");
            database.getAllDocs(new QueryOptions());
            Query queryAllDocs = database.createAllDocumentsQuery();
            QueryEnumerator queryEnumerator = queryAllDocs.run();
            for (Iterator<QueryRow> it = queryEnumerator; it.hasNext();) {
                QueryRow row = it.next();
                Document document = row.getDocument();
                Revision revision = document.getCurrentRevision();
                Log.d("MyCouch", document.getId() + " : " +  revision.getAttachments().size());
                if(document.getId().equalsIgnoreCase(OneByOneResID)){
                    Resource resource = resourceList.get(resButtonId);
                    resource.setTitle((String) document.getProperty("title"));
                    String OpenWith = (String) document.getProperty("openWith");
                    if( OpenWith.equalsIgnoreCase("Flow Video Player") || OpenWith.equalsIgnoreCase("MP3") || OpenWith.equalsIgnoreCase("PDF.js")   ||OpenWith.equalsIgnoreCase("HTML")){
                        resource.setThumbnailUrl(getIconType((String) document.getProperty("openWith")));
                    }else{
                        resource.setThumbnailUrl(getIconType("-"));
                    }
                    Log.d("MyCouch", "Found resource : Making "+ document.getProperty("openWith"));
                    resourceList.set(resButtonId,resource);
                }
            }

            Log.d("MyCouch", "done looping over all docs ");
            ///mDialog.dismiss();

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void dbDiagnosticCheck(){
        try {
            Database database = manager.getDatabase("resources");
            database.getAllDocs(new QueryOptions());
            Query queryAllDocs = database.createAllDocumentsQuery();
            QueryEnumerator queryEnumerator = queryAllDocs.run();
            for (Iterator<QueryRow> it = queryEnumerator; it.hasNext();) {
                QueryRow row = it.next();
                Document document = row.getDocument();
                Revision revision = document.getCurrentRevision();
                Log.d("MyCouch", document.getId() + " : " +  revision.getAttachments().size());
                for(int cnt=0; cnt<=resourceIdList.length;cnt++) {
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
                        Log.d("MyCouch", "Found resource : Making " + document.getProperty("openWith"));
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

    public int getIconType(String myresExt){

        int img = R.drawable.web;
        switch (myresExt){
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
            openFromDiskDirectly = true;
            Log.e("MYAPP", " membersWithID  = " + docId +" and Open with "+ openwith);
            List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
/////HTML
            if(openwith.equalsIgnoreCase("HTML")) {
                indexFilePath = null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(docId, attmentNames.get(cnt));
                    }
                    if (indexFilePath != null) {
                        openHTML(indexFilePath);
                    }
                } else {
                    openImage(docId, attmentNames.get(0), getExtension(attmentNames.get(0)));
                }
////PDF
            }else if(openwith.equalsIgnoreCase("Just download")){
                //// Todo work to get just download


            }else if(openwith.equalsIgnoreCase("PDF.js")){
                if(openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  "+docId);
                    String filenameOnly="";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  "+ f.getName() +" Filename only "+filenameOnly);
                            if (filenameOnly.equalsIgnoreCase(docId)) {
                                try{
                                    mDialog.dismiss();
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setPackage("com.adobe.reader");
                                    intent.setDataAndType(Uri.fromFile(f), "application/pdf");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }catch(Exception err){
                                    myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                                    File dst = new File(myDir,"adobe_reader.apk");
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                }else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openPDF(docId, attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }
////MP3
            }else if(openwith.equalsIgnoreCase("MP3")){
                if(openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  "+docId);
                    String filenameOnly="";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  "+ f.getName() +" Filename only "+filenameOnly);
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
                }else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openAudioVideo(docId, attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }
/// BELL READER
            }else if(openwith.equalsIgnoreCase("Bell-Reader")){
                if(openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  "+docId);
                    String filenameOnly="";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  "+ f.getName() +" Filename only "+filenameOnly);
                            if (filenameOnly.equalsIgnoreCase(docId)) {
                                try{
                                    mDialog.dismiss();
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setPackage("com.adobe.reader");
                                    intent.setDataAndType(Uri.fromFile(f), "application/pdf");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }catch(Exception err){
                                    myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                                    File dst = new File(myDir,"adobe_reader.apk");
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                }else{
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openPDF(docId, attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }
                /////VIDEO
            }else if(openwith.equalsIgnoreCase("Flow Video Player")){

                if(openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  "+docId);
                    String filenameOnly="";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  "+ f.getName() +" Filename only "+filenameOnly);
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
                }else{
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openAudioVideo(docId, attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }

            }else if(openwith.equalsIgnoreCase("BeLL Video Book Player")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {

                    }
                }
/// Native Video
            }else if(openwith.equalsIgnoreCase("Native Video")){
                if(openFromDiskDirectly) {
                    Log.e("MyCouch", " Command Video name -:  "+docId);
                    String filenameOnly="";
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/ole_temp");
                    for (File f : myDir.listFiles()) {
                        if (f.isFile()) {
                            if (f.getName().indexOf(".") > 0) {
                                filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                            }
                            Log.e("MyCouch", " File name -:  "+ f.getName() +" Filename only "+filenameOnly);
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
                }else {
                    if (attmentNames.size() > 0) {
                        for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                            openAudioVideo(docId, attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                            break;
                        }
                    }
                }

            }
        } catch (Exception Er) {
            Log.d("MyCouch", "Opening resource error "+Er.getMessage());
        }
    }

    public void openHTML(String index) {
        final String mainFile =  index;
        try {
            try{
                mDialog.dismiss();
                ComponentName componentName = getPackageManager().getLaunchIntentForPackage("org.mozilla.firefox").getComponent();
 //               Intent firefoxIntent = IntentCompat.makeRestartActivityTask(componentName);
 //               firefoxIntent.setDataAndType(Uri.parse(mainFile),"text/html");
 //               startActivity(firefoxIntent);

                //startActivity(intent);
            }catch(Exception err){
                mDialog.dismiss();
                Log.e("Error", err.getMessage());
                File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                File dst = new File(myDir,"firefox_49_0_multi_android.apk");
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
        final String myfilename =  fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);
            int lth= (int) fileAttachment.getLength();
            try{
                InputStream in = fileAttachment.getContent();
                String root = Environment.getExternalStorageDirectory().toString();
                File newDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2/"+docId);
                if (!newDir.exists()){
                    newDir.mkdirs();
                }

                File myDir = new File(root + "/ole_temp2/"+docId);
                File dst = new File(myDir,fileAttachment.getName().replace(" ", ""));
                String filepath[]= dst.toString().split("/");
                int defaultLength = myDir.getPath().split("/").length;
                String path= myDir.getPath();
                //Log.e("tag", " Location  "+ dst.toString() + " Default :" + defaultLength + " fpath: "+filepath.length);
                for(int cnt= defaultLength; cnt < (filepath.length-1);cnt++){
                    path = path +"/"+ filepath[cnt];
                    myDir = new File(path);
                    if (!myDir.exists()){
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

                    Log.e("tag", " Saved "+ dst.toString()+" Original length: "+ lth );

                    in.close();
                    out.close();
                    if(dst.getName().equalsIgnoreCase("index.html") && (filepath.length - defaultLength)==1 ){
                        indexFilePath = dst.toString();
                    }

                }catch(Exception err){
                    Log.e("tag", " Saving "+ err.getMessage());
                }
            }catch(Exception err){
                err.printStackTrace();
            }
        } catch (Exception Er) {
            Er.printStackTrace();

        }
    }

    public void openImage(String docId, final String fileName, String player) {
        final String myfilename =  fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);
            try{
                File src = new File(fileAttachment.getContentURL().getPath());
                InputStream in = new FileInputStream(src);
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                if (!myDir.exists()){
                    myDir.mkdirs();
                }
                File dst = new File(myDir,fileAttachment.getName().replace(" ", ""));
                try {
                    FileOutputStream out = new FileOutputStream(dst);
                    byte[] buff = new byte[1024];
                    int read = 0;
                    while ((read = in.read(buff)) > 0) {
                        out.write(buff, 0, read);
                    }
                    in.close();
                    out.close();
                    Log.e("tag", " Copied PDF "+ dst.toString());
                }catch(Exception err){
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
            }catch(Exception err){

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
        final String myfilename =  fileName;
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
                    try{
                        File src = new File(fileAttachment.getContentURL().getPath());
                        InputStream in = new FileInputStream(src);
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/ole_temp");
                        if (!myDir.exists()){
                            myDir.mkdirs();
                        }
                        File dst = new File(myDir,fileAttachment.getName().replace(" ", ""));
                        try {
                            FileOutputStream out = new FileOutputStream(dst);
                            byte[] buff = new byte[1024];
                            int read = 0;
                            while ((read = in.read(buff)) > 0) {
                                out.write(buff, 0, read);
                            }
                            in.close();
                            out.close();
                            Log.e("tag", " Copied PDF "+ dst.toString());
                        }catch(Exception err){
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


                    }catch(Exception err){
                        File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                        File dst = new File(myDir,"adobe_reader.apk");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                } });

            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "In-App PDF Viewer", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}});
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {


                }});
            alertDialog.show();

        } catch (Exception Er) {
            Er.printStackTrace();

        }

    }

    public void openAudioVideo(String docId,String fileName, String player) {

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
        if (filename == null){
            return null;
        }
        final String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        final int afterLastBackslash = afterLastSlash.lastIndexOf('\\') + 1;
        final int dotIndex = afterLastSlash.indexOf('.', afterLastBackslash);
        return (dotIndex == -1) ? "" : afterLastSlash.substring(dotIndex + 1);
    }

    public boolean deleteDirectory(File path) {
        if(path.exists()) {
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                for (int i=0; i<files.length; i++) {
                    deleteDirectory(files[i]);
                }
            }
            return path.delete();
        }

        return false;
    }

    public void animateLayoutBars(){

        /////////  Layout Slide in Animation from bottom to up
        final LinearLayout libraryLayout = findViewById(R.id.layoutMasterLibrary);
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


        final LinearLayout coursesLayout = findViewById(R.id.layoutMasterCoursesProgress);
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

        final LinearLayout MeetupLayout = findViewById(R.id.layoutMasterMeetup);
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


        final LinearLayout TutorsLayout = findViewById(R.id.layoutMasterTutors);
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

    /*
    public void toggleTopUserInfo(){
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
                userInfoDisplayed=true;
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
                userInfoDisplayed=false;
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
                if(!userInfoDisplayed){
                    userInfoLayout.startAnimation(translateShowUserInfo);
                }else{
                    userInfoLayout.startAnimation(translateHideUserInfo);
                }
            }
        });
    }

    public void updateUI(){
        TextView lblName = (TextView) findViewById(R.id.lblName);
        lblName.setText(" "+sys_userfirstname +" "+sys_userlastname);
        TextView lblVisits = (TextView) findViewById(R.id.lblVisits);
        if(sys_uservisits==""){
            //// Todo change word 'Visits' to be read from languages
            lblVisits.setText(""+sys_uservisits_Int + " Visits");
        }else{
            lblVisits.setText(""+sys_uservisits + " Visits");
        }
        TextView lblServerName = (TextView) findViewById(R.id.lbl_SeverName);
        lblServerName.setText(""+sys_servername.toUpperCase());

    }

    */
    public void alertDialogOkay(String Message){
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


    /// Todo Review Code and test class

    class downloadSpecificResourceToDisk extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            Fuel ful = new Fuel();
            Log.e("Called", " class- downloadSpecificResourceToDisk " );
            Fuel.get(sys_oldSyncServerURL+"/resources/" + OneByOneResID).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                @Override
                public void success(Request request, Response response, String s) {
                    try {
                        try {
                            /// alertDialogOkay(OneByOneResID+"");
                            openFromDiskDirectly = true;
                            jsonData = new JSONObject(s);
                            String openWith = (String) jsonData.get("openWith");
                            Log.e("MyCouch", "Open With -- " + openWith);
                            if(!openWith.equalsIgnoreCase("HTML")) {
                                JSONObject _attachments = (JSONObject) jsonData.get("_attachments");
                                Iterator<String> keys = _attachments.keys();
                                if (keys.hasNext()) {
                                    String key = keys.next();
                                    Log.e("MyCouch", "-- " + key);
                                    String encodedkey = URLEncoder.encode(key, "utf-8");
                                    File file = new File(encodedkey);
                                    String extension = encodedkey.substring(encodedkey.lastIndexOf("."));
                                    String diskFileName = OneByOneResID + extension;
                                    createResourceDoc(OneByOneResID,(String) jsonData.get("title"), (String) jsonData.get("openWith"));
                                    downloadWithDownloadManagerSingleFile(sys_oldSyncServerURL + "/resources/" + OneByOneResID + "/" + encodedkey, diskFileName);
                                }
                            }else{
                                Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS " );
                                htmlResourceList.add(OneByOneResID);
                                if(allhtmlDownload<htmlResourceList.size()) {
                                    new SyncSingleHTMLResource().execute();
                                }else{
                                    mDialog.dismiss();
                                    alertDialogOkay("Download Completed");
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
        }
    }

    class downloadAllResourceToDisk extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            Fuel ful = new Fuel();
            Fuel.get(sys_oldSyncServerURL+"/resources/" + resourceIdList[allresDownload]).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
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
                                    String key = keys.next();
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
        }
    }

    public void downloadWithDownloadManager(String fileURL, String FileName){
        String url = fileURL;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(resourceIdList[allresDownload]+"-"+resourceTitleList[allresDownload]);
        request.setTitle(resourceTitleList[allresDownload]);
// in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        Log.e("MyCouch", " Destination is "+FileName);
        request.setDestinationInExternalPublicDir("ole_temp",FileName);

// get download service and enqueue file
        mDialog.setMessage("Downloading  \" "+resourceTitleList[allresDownload]+" \" . please wait...");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = downloadManager.enqueue(request);
    }

    public void downloadWithDownloadManagerSingleFile(String fileURL, String FileName){
        String url = fileURL;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(clicked_rs_ID +"-"+ clicked_rs_title);
        request.setTitle(clicked_rs_title);
// in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        Log.e("MyCouch", " Destination is "+FileName);
        request.setDestinationInExternalPublicDir("ole_temp",FileName);

// get download service and enqueue file
        mDialog.setMessage("Downloading  \" "+clicked_rs_title+" \" . please wait...");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = downloadManager.enqueue(request);
    }

    public void createResourceDoc(String manualResId,String manualResTitle, String manualResopenWith){
        Database database = null;
        try {
            database = manager.getDatabase("resources");
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("title", manualResTitle);
            properties.put("openWith", manualResopenWith);
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

    public void rateResourceDialog(String resourceId, String title){
        // custom dialog
        final String resourceID = resourceId;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rate_resource_dialog);
        dialog.setTitle("Add Feedback For \n" );

        final TextView txtResTitle = dialog.findViewById(R.id.txtResTitle);
        txtResTitle.setText(title);
        final EditText txtComment = dialog.findViewById(R.id.editTextComment);
        final RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,boolean fromUser) {

            }
        });
        Button dialogButton = dialog.findViewById(R.id.btnRateResource);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRating(ratingBar.getRating(),String.valueOf(txtComment.getText()),resourceID);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void saveRating(float rate,String comment,String resourceId){
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        Database resourceRating;
        double doc_rating;
        int doc_timesRated;
        ArrayList<String> commentList = new ArrayList<String>();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            resourceRating = manager.getDatabase("resourcerating");
            Document retrievedDocument = resourceRating.getExistingDocument(resourceId);
            if(retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                if(properties.containsKey("sum")){
                    doc_rating = (double) properties.get("sum") ;
                    doc_timesRated  = (int) properties.get("timesRated") ;
                    commentList = (ArrayList<String>) properties.get("comments");
                    commentList.add(comment);
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("sum", (doc_rating + rate));
                    newProperties.put("timesRated", doc_timesRated + 1);
                    newProperties.put("comments", commentList);
                    retrievedDocument.putProperties(newProperties);
                    Toast.makeText(context,String.valueOf(rate),Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Document newdocument = resourceRating.getDocument(resourceId);
                Map<String, Object> newProperties = new HashMap<String, Object>();
                newProperties.put("sum", rate);
                newProperties.put("timesRated", 1);
                commentList.add(comment);
                newProperties.put("comments", commentList);
                newdocument.putProperties(newProperties);
                Toast.makeText(context,String.valueOf(rate), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception err){
            Log.e("MyCouch", "ERR : " +err.getMessage());
        }
    }

    public void checkResourceOpened(){
        if(openedResource) {
            rateResourceDialog(openedResourceId,openedResourceTitle);
            openedResource=false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main__dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
