package pbell.offline.ole.org.pbell;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import net.sf.andpdf.pdfviewer.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static pbell.offline.ole.org.pbell.R.color.red;

public class FullscreenActivity extends AppCompatActivity {

    private View mContentView;
    final Context context = this;

    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;

    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
            sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
            sys_usergender, sys_uservisits= "";
    int sys_uservisits_Int=0;
    Object[] sys_membersWithResource;
    boolean userShelfSynced =true;
    boolean synchronizing = true;
    JSONObject jsonData;
    Database dbResources;
    int syncCnt,resourceNo,allresDownload=0;
    AndroidContext androidContext;
    Replication pull;
    Manager manager;
    boolean status_SyncOneByOneResource = false;
    private ProgressDialog mDialog;
    String indexFilePath;


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

    int resourceCntr,attachmentLength;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.fullscreen_content);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        androidContext = new AndroidContext(this);
       /* mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show();
            }
        });*/
        ///////////////////////////////////
        TextView txtcurDate = (TextView) findViewById(R.id.lblDate);
        txtcurDate.setText(curdate());

        /////////////////////////////////
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
        sys_password = settings.getString("pf_password","");
        sys_usercouchId = settings.getString("pf_usercouchId","");
        sys_userfirstname = settings.getString("pf_userfirstname","");
        sys_userlastname = settings.getString("pf_userlastname","");
        sys_usergender = settings.getString("pf_usergender","");
        sys_uservisits = settings.getString("pf_uservisits","");

        /////////////////////////

        LoadShelfResourceList();

        TextView lblName = (TextView) findViewById(R.id.lblName);
        lblName.setText(" "+sys_userfirstname +" "+sys_userlastname);

        TextView lblVisits= (TextView) findViewById(R.id.lblVisits);
        if(sys_uservisits==""){
            lblVisits.setText(""+sys_uservisits_Int+" Visits");
        }else{
            lblVisits.setText(""+sys_uservisits+" Visits");
        }


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

        sd_Slidin = MediaPlayer.create(this, R.raw.wave);

        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        animateLayoutBars();
        /*
        final LinearLayout libraryLayout = (LinearLayout) findViewById(R.id.layoutMasterLibrary);
        int layoutWidth = libraryLayout.getLayoutParams().width;
        int animationSpeed = (layoutWidth * 1000) / 100;
        libraryLayout.animate().translationXBy(20).setDuration(500).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //libraryLayout.setVisibility(View.GONE);
            }
        });
        */

    }
/*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
         LinearLayout libraryLayout = (LinearLayout) findViewById(R.id.layholder_library);
        /*libraryLayout.animate()
                .translationY(0)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        libraryLayout.setVisibility(View.GONE);
                    }
                });
        if(hasFocus){
            libraryLayout.getId().startAnimation(AnimationUtils.loadAnimation(FullscreenActivity.this,
                    android.R.anim.slide_in_left|android.R.anim.fade_in));
        }
    }*/


    public String curdate(){
        Calendar cal= Calendar.getInstance();
        Date d = new Date();

        SimpleDateFormat s_df = new SimpleDateFormat("EEEE");
        String dayOfTheWeek = s_df.format(d);
        SimpleDateFormat date_df = new SimpleDateFormat("d");
        String dayNumber = date_df.format(d);
        SimpleDateFormat month_df = new SimpleDateFormat("MMMM");
        String month_name = month_df.format(cal.getTime());
        SimpleDateFormat year_df = new SimpleDateFormat("yyyy");
        String year = year_df.format(d);
        String displayedDate = dayOfTheWeek + "  |  "+dayNumber+" " + month_name+" "+year;
        return displayedDate;
    }
/*
    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
    }
    */

    public void LoadShelfResourceList() {
        String memberId = sys_usercouchId;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("shelf");
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
                if(memberId.equals((String) properties.get("memberId"))) {
                    String myresTitile = ((String) properties.get("resourceTitle"));
                    String myresId = ((String) properties.get("resourceId"));
                    resourceTitleList[rsLstCnt]=myresTitile;
                    resourceIdList[rsLstCnt]=myresId;
                    Log.e("tag", "MEMBER ID "+ (String) properties.get("resourceTitle"));
                    rsLstCnt++;
                }
            }

            LinearLayout row2 = (LinearLayout) findViewById(R.id.layholder_library);
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
                    Database resource_Db = manager.getExistingDatabase("resources");
                    Document resource_doc = resource_Db.getExistingDocument((String) resourceIdList[ButtonCnt]);
                    Map<String, Object> resource_properties = resource_doc.getProperties();
                    Log.e("tag", "RES ID " + (String) resource_properties.get("resourceId"));
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
                            openDoc(resourceIdList[view.getId()]);
                        }

                    }

                });
                //////////// Save list in Preferences

                /*     SharedPreferences.Editor editor = settings.edit();
                Set<String> set = new HashSet<String>(Arrays.asList(resourceIdList));
                editor.putStringSet("membersNoOfResources", set);
            */
            }
            db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void MaterialClickDialog(boolean online,String title,String resId,int buttonPressedId){
        final boolean rs_status = online;
        final String rs_title = title;
        final String rs_ID = resId;
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
        Button dialogBtnOpenFileOnline = (Button) dialog2.findViewById(R.id.btnOpenOnline);
        dialogBtnOpenFileOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo Open resource in a browser


            }
        });

        if (checkConnectionURL()) {
            Snackbar.make(mContentView, "Checking connection to "+sys_oldSyncServerURL +".... please wait", Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
            new FullscreenActivity.TestConnection().execute(sys_oldSyncServerURL);
        } else {
            dialogBtnOpenFileOnline.setText("Not Connected");
            dialogBtnOpenFileOnline.setEnabled(false);
        }

        //// Download Only selected file
        Button dialogBtnDownoadFile = (Button) dialog2.findViewById(R.id.btnDownloadFile);
        dialogBtnDownoadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
                ///OneByOneResID = rs_ID;
                try {
                    //testFilteredPuller();

                    mDialog = new ProgressDialog(context);
                    mDialog.setMessage("Please wait...");
                    mDialog.setCancelable(true);
                    mDialog.show();
                    final AsyncTask<String, Void, Boolean> execute = new SyncResource().execute();

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

        //// Download all offline resources on button click
        Button dialogBtnDownoadAll= (Button) dialog2.findViewById(R.id.btnDownloadAll);
        dialogBtnDownoadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
                OneByOneResID = rs_ID;
                try {
                    //testFilteredPuller();
                    mDialog = new ProgressDialog(context);
                    mDialog.setMessage("Please wait...");
                    mDialog.setCancelable(true);
                    mDialog.show();
                    allresDownload=0;
                    final AsyncTask<String, Void, Boolean> executeAll = new SyncAllResource().execute();

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

    }

    private boolean checkConnectionURL() {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.v("myErrorTag","Network Connected");
            return true;
            // fetch data
        } else {

            Log.v("myErrorTag","Network Not Connected");
            return false;
        }

    }

    private class TestConnection extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            InputStream is = null;
            int response=0;
            try {
                URL url = new URL(sys_oldSyncServerURL+"/resources");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000 /* milliseconds */);
                conn.setConnectTimeout(10000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                response = conn.getResponseCode();
                Log.d("MyCouch", "The response is: " + response);
                is = conn.getInputStream();
                return "";
            }catch (Exception err) {

            }finally
            {
                if(response!=200){
                    Snackbar.make(mContentView, "Sorry , server was unreachable, check url & device connection "+ response, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }else{
                    Snackbar.make(mContentView, "Server connection established", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return "";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        }
    }

    //// NOT IN USE CURRENT VERSION
    public void syncOneByOne(){
        status_SyncOneByOneResource=true;
        final AsyncTask<String, Void, Boolean> execute = new FullscreenActivity.SyncOneByOneResource().execute();
        Log.e("MyCouch", "syncNotifier Running");
        final Thread th = new Thread(new Runnable() {
            public void run() {
                while (status_SyncOneByOneResource) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!status_SyncOneByOneResource) {
                                mDialog.dismiss();
                                status_SyncOneByOneResource=false;
                                return;
                            }else {
                                Log.d("runOnUiThread", "running");
                                mDialog.setMessage("Downloading, please wait .... ");
                            }
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
    public void syncThreadHandler(){
        final AsyncTask<String, Void, Boolean> execute = new FullscreenActivity.SyncResource().execute();
        final Thread th = new Thread(new Runnable() {
            public void run() {
                while (status_SyncOneByOneResource) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

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
    class SyncOneByOneResource extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute (){
            synchronizing = true;
            Log.d("PreExceute","On pre Exceute......");
        }

        protected Boolean doInBackground(final String... args) {
            Log.d("DoINBackGround","On doInBackground...");
            final Database res_Db;
            final Fuel ful = new Fuel();
            jsonData = null;
            attachmentLength= -1;
            try {
                URL url = new URL(sys_oldSyncServerURL+"/resources");
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                dbResources = manager.getDatabase("resources");
                dbResources.delete();
                res_Db = manager.getExistingDatabase("resources");
                pull = dbResources.createPullReplication(url);
                pull.setFilter("apps/by_resource");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("_id", OneByOneResID);
                //Log.e("MyCouch", " Resource ID "+ lst.get(syncCnt));
                pull.setFilterParams(params);
                pull.setContinuous(false);
                ////pull.setAuthenticator(new BasicAuthenticator(userName, userPw));
                pull.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        if(pull.isRunning()){
                            Log.e("MyCouch", " "+event.getChangeCount());
                            Log.e("MyCouch", " Document Count "+dbResources.getDocumentCount());
                        }else {
                            Log.e("Finished", ""+dbResources.getDocumentCount());
                            ////// CHECK REMOTE ATTACHMENT FILE SIZE VS LOCAL ATTACHMENT FILE SIZE (length)
                            Document res_doc = res_Db.getExistingDocument(OneByOneResID);
                            final List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
                            /// IF local document has attachments
                            if (attmentNames.size() > 0) {
                                for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                                    resourceCntr = cnt;
                                    Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment((String) attmentNames.get(cnt));
                                    ///
                                    ful.get(sys_oldSyncServerURL+"/resources/"+OneByOneResID).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                                        @Override
                                        public void success(Request request, Response response, String s) {
                                            try {
                                                jsonData = new JSONObject(s);
                                                //Log.e("MyCouch", "-- "+jsonData);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                JSONObject jsob = jsonData.getJSONObject("_attachments");
                                                JSONObject jsoAttachments = jsob.getJSONObject((String) attmentNames.get(resourceCntr));
                                                attachmentLength = jsoAttachments.getInt("length");
                                                Log.e("MyCouch", "Attachment Object Content "+jsoAttachments);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        @Override
                                        public void failure(Request request, Response response, FuelError fuelError) {
                                            Log.e("MyCouch", " "+fuelError);

                                        }
                                    });
                                    /// If local attachment length is greater or equal to remote attachment, continue
                                    /// Else run the replication again
                                    if(fileAttachment.getLength() >= attachmentLength){

                                        Log.e("MyCouch", "Local = "+fileAttachment.getLength() + "  Remote = " +attachmentLength);
                                        if(syncCnt <= 1){
                                            syncCnt++;
                                            new FullscreenActivity.SyncOneByOneResource().execute();
                                        }else{
                                            if (status_SyncOneByOneResource){
                                            }
                                            status_SyncOneByOneResource = false;
                                        }


                                    }else{
                                        Log.e("MyCouch", "Local = "+fileAttachment.getLength() + "  Remote = " +attachmentLength);
                                        new FullscreenActivity.SyncOneByOneResource().execute();
                                    }
                                }
                            } else if(syncCnt <= 1){
                                syncCnt++;
                                new FullscreenActivity.SyncOneByOneResource().execute();
                            }else{
                                if (status_SyncOneByOneResource){
                                    //..mydialog.dismiss();
                                }
                                status_SyncOneByOneResource = false;
                            }
                            //////////////////////

                        }
                    }
                });
                pull.start();


            } catch (Exception e) {
                Log.e("MyCouch", " "+" Cannot create database", e);
                return false;

            }
            return true;
        }
        protected void onProgressUpdate(Integer...a){
            Log.d("onProgress","You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(final Boolean success) {
            if (success){
                Log.d("MyCouch","Download Triggered");
                status_SyncOneByOneResource=false;
                mDialog.dismiss();
                libraryButtons[resButtonId].setTextColor(getResources().getColor(R.color.ole_white));
            }else{
                Log.d("OnPostExec","");
            }
        }
    }
    public void testFilteredPuller() throws Throwable {

        URL remote = getReplicationURL();

        CountDownLatch replicationDoneSignal = new CountDownLatch(1);
        final Database database = manager.getDatabase("resources");
        final Replication repl = (Replication) database.createPullReplication(remote);
        repl.setContinuous(false);
        repl.setFilter("apps/by_resource");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("_id", OneByOneResID);
        repl.setFilterParams(map);

        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(true);
        mDialog.show();


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
                        libraryButtons[resButtonId].setTextColor(getResources().getColor(R.color.ole_yellow));
                    }
                }else {
                    Log.e("MyCouch", "" + database.getDocumentCount());
                    mDialog.dismiss();
                    checkAllDocsInDB();
                }

            }
        });
        repl.start();


    }
    //////////////////


    class SyncResource extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL remote = getReplicationURL();
                CountDownLatch replicationDoneSignal = new CountDownLatch(1);
                final Database database;
                database = manager.getDatabase("resources");
                final Replication repl = (Replication) database.createPullReplication(remote);
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

    class SyncAllResource extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL remote = getReplicationURL();
                CountDownLatch replicationDoneSignal = new CountDownLatch(1);
                final Database database;
                database = manager.getDatabase("resources");
                final Replication repl = (Replication) database.createPullReplication(remote);
                repl.setContinuous(false);
                repl.setFilter("apps/by_resource");

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("_id", resourceIdList[allresDownload]);
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
                                //mDialog.dismiss();
                                //checkAllDocsInDB();
                               // libraryButtons[allresDownload].setTextColor(getResources().getColor(R.color.ole_white));
                            }
                            else{
                                mDialog.setMessage("Data transfer error. Check connection to server.");
                            }
                        }else {
                            Log.e("MyCouch", "Document Count " + database.getDocumentCount());
                            if(allresDownload<libraryButtons.length){
                                //mDialog.show();
                                final AsyncTask<String, Void, Boolean> executeAll = new SyncAllResource().execute();
                                libraryButtons[allresDownload].setTextColor(getResources().getColor(R.color.ole_white));
                                allresDownload++;
                            }else{
                                mDialog.dismiss();
                                checkAllDocsInDB();
                            }

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
            }

            Log.d("MyCouch", "done looping over all docs ");
            mDialog.dismiss();

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }


    public void openDoc(String docId) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            String oppenwith = (String) res_doc.getProperty("openWith");
            Log.e("MYAPP", " membersWithID  = " + docId +"and Open with "+ oppenwith);
            List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
            if(oppenwith.equalsIgnoreCase("HTML")){
                /*if (attmentNames.size() <= 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openImage(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                    }
                }*/
                indexFilePath=null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(docId, (String) attmentNames.get(cnt));
                    }
                    if(indexFilePath!=null){
                        openHTML(indexFilePath);
                    }
                }else{
                    openImage(docId, (String) attmentNames.get(0), getExtension(attmentNames.get(0)));
                }

            }else if(oppenwith.equalsIgnoreCase("PDF.js")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openPDF(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }else if(oppenwith.equalsIgnoreCase("MP3")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }else if(oppenwith.equalsIgnoreCase("Bell-Reader")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openPDF(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }
            }else if(oppenwith.equalsIgnoreCase("Flow Video Player")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }else if(oppenwith.equalsIgnoreCase("BeLL Video Book Player")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {

                    }
                }

            }else if(oppenwith.equalsIgnoreCase("Native Video")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }
        } catch (Exception Er) {

        }
    }

    public void openHTML(String index) {
        final String mainFile =  index;
        try {
            try{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("org.mozilla.firefox");
                intent.setDataAndType(Uri.parse(mainFile),"text/html");
                intent.setComponent(new ComponentName("org.mozilla.firefox", "org.mozilla.firefox.App"));
                this.startActivity(intent);
                ///intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }catch(Exception err){
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
                }
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(dst), "image/*");
                //Log.e("tag", " URL Path "+ Uri.fromFile(dst).getPath());;
                startActivity(intent);
            }catch(Exception err){

            }
        } catch (Exception Er) {
            Er.printStackTrace();

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
                        } ///

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
                        } ///


                        Intent intent = new Intent(FullscreenActivity.this, MyPdfViewerActivity.class);
                        Log.e("tag", " URL Path "+ Uri.fromFile(dst).getPath());
                        intent.putExtra(net.sf.andpdf.pdfviewer.PdfViewerActivity.EXTRA_PDFFILENAME, Uri.fromFile(dst).getPath());
                        startActivity(intent);


                    }catch(Exception err){

                    }
                }});
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
            File dst = new File(myDir,diskFileName);

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
            Log.e("tag","- "+ mimetype +" - ");

            if(mimetype=="audio/mpeg"){
                intent.setDataAndType(Uri.fromFile(dst),mimetype);
                this.startActivity(intent);
            }else{
                try {
                    intent.setDataAndType(Uri.fromFile(dst),mimetype);
                    this.startActivity(intent);
                }catch (Exception Er) {
                    Log.e("tag", Er.getMessage());
                }
            }
        } catch (Exception Er) {
            Log.e("tag", Er.getMessage());
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


}
