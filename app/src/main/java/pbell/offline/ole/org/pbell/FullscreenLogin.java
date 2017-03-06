package pbell.offline.ole.org.pbell;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.Pair;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenLogin extends AppCompatActivity {


    private View mContentView;

    private static final int REQUEST_READ_CONTACTS = 0;
    public static final String PREFS_NAME = "MyPrefsFile";

    SharedPreferences settings;
    CouchViews chViews = new CouchViews();
    String doc_lastVisit;
    private EditText mUsername;
    private EditText mPasswordView;
    private LoginActivity.UserLoginTask mAuthTask = null;

    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
            sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
            sys_usergender,sys_uservisits,sys_servername,sys_serverversion="";
    Boolean sys_singlefilestreamdownload,sys_multiplefilestreamdownload;
    Object[] sys_membersWithResource;
    int sys_uservisits_Int;
    private Dialog dialog,promptDialog;
    private ProgressDialog mDialog;
    JSONObject jsonServerData;



    final Context context = this;
    String[] databaseList = {"members","membercourseprogress","meetups","usermeetups","assignments",
            "calendar","groups","invitations","configurations","requests","shelf","languages"};

    Replication[] push = new Replication[databaseList.length];
    Replication[] pull= new Replication[databaseList.length];


    Database[] db = new Database[databaseList.length];
    Manager[] manager = new Manager[databaseList.length];
    boolean syncmembers,openMemberList= false;
    int syncCnt =0;
    AndroidContext androidContext;
    JSONObject designViewDoc;
    Database database;
    Replication pullReplication;
    Button dialogSyncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_login_new);
        mContentView = findViewById(R.id.fullscreen_content2);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        androidContext = new AndroidContext(this);
        // Todo - : Decide on either to clear resource database and file storage anytime user syncs or rather keep old resources only if user doesn't change server url
        /////////////////////////////////////
        // Set up the login form.
        mUsername = (EditText) mContentView.findViewById(R.id.txtUsername);
        mPasswordView = (EditText) findViewById(R.id.txtPassword);


        Button SignInButton = (Button) findViewById(R.id.btnSignIn);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(authenticateUser()){

                }else {
                    alertDialogOkay("Login incorrect or Not found. Check and try again.");
                }
            }
        });

        Button SetupButton = (Button) findViewById(R.id.btnSetup);
        SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSyncURLDialog();
            }
        });

        Button btnFeedback = (Button) findViewById(R.id.btnFeedback);
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database resourceRating;
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
                        Double sum = ((Double) properties.get("sum"));
                        int timesRated = ((Integer) properties.get("timesRated"));
                        updateRemoteResourceRating(docId,sum,timesRated,((String) properties.get("_rev")));
                    }
                }catch (Exception err){
                    Log.e("MyCouch", "reading resource rating error "+err.getMessage());
                }

            }
        });
        restorePref();
        copyAPK(R.raw.adobe_reader, "adobe_reader.apk");
        copyAPK(R.raw.firefox_49_0_multi_android, "firefox_49_0_multi_android.apk");

    }
////////
    private void TestConnectionToServer(String textURL) {
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Please wait. Connecting to server...");
        mDialog.setCancelable(false);
        mDialog.show();

        final Fuel ful = new Fuel();

        ful.get(textURL+"/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
            @Override
            public void success(Request request, Response response, String s) {
                try {
                    List<String> myList = new ArrayList<String>();
                    myList.clear();
                    myList=Arrays.asList(s.split(","));
                    Log.e("MyCouch", "-- "+myList.size());
                    if(myList.size() < 8){
                        mDialog.dismiss();
                        alertDialogOkay("Check the server address again. What i connected to wasn't the BeLL Server");
                        dialogSyncButton.setVisibility(View.INVISIBLE);
                    }else{
                        mDialog.dismiss();
                        alertDialogOkay("Test successful. You can now click on \"Save and Proceed\" ");
                        dialogSyncButton.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void failure(Request request, Response response, FuelError fuelError) {
                mDialog.dismiss();
                alertDialogOkay("Device couldn't reach server. Check and try again");
                dialogSyncButton.setVisibility(View.INVISIBLE);
                Log.e("MyCouch", " "+fuelError);

            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public boolean authenticateUser(){
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        getSystemInfo();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("members");
            Query orderedQuery = chViews.CreateLoginByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                String doc_loginId = (String) properties.get("login");
                String doc_password = (String) properties.get("password");

                if(mUsername.getText().toString().equals(doc_loginId)) {
                    Log.e("MYAPP", "Authenticating User");

                    if (mPasswordView.getText().toString().equals(doc_password) && !properties.containsKey("credentials") ) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("pf_username", (String) properties.get("login"));
                        editor.putString("pf_password", (String) properties.get("password"));
                        editor.putString("pf_usercouchId", (String) properties.get("_id"));
                        editor.putString("pf_userfirstname", (String) properties.get("firstName"));
                        editor.putString("pf_userlastname", (String) properties.get("lastName"));
                        editor.putString("pf_usergender", (String) properties.get("Gender"));
                        try {
                            String noOfVisits = properties.get("visits").toString();
                            int currentTotalVisits = Integer.parseInt(noOfVisits) + totalVisits((String) properties.get("_id"));
                            editor.putInt("pf_uservisits_Int", currentTotalVisits);
                            editor.putString("pf_uservisits", currentTotalVisits+"");
                            editor.putString("pf_lastVisitDate", doc_lastVisit);

                        } catch (Exception err) {
                        }
                        Set<String> stgSet = settings.getStringSet("pf_userroles", new HashSet<String>());
                        ArrayList roleList = (ArrayList<String>) properties.get("roles");
                        for (int cnt = 0; cnt < roleList.size(); cnt++) {
                            stgSet.add(String.valueOf(roleList.get(cnt)));
                        }
                        editor.putStringSet("pf_userroles", stgSet);
                        editor.commit();
                        Log.e("MYAPP", " RowChipsView Login OLD encryption: " + doc_loginId + " Password: " + doc_password);
                        Intent intent = new Intent(this, FullscreenActivity.class);
                        startActivity(intent);
                        return true;

                    }else if (doc_password == "" && !mPasswordView.getText().toString().equals("")) {
                        try {
                            Map<String, Object> doc_credentials = (Map<String, Object>) properties.get("credentials");
                            AndroidDecrypter adc = new AndroidDecrypter();
                            if(adc.AndroidDecrypter(doc_loginId, mPasswordView.getText().toString(), doc_credentials.get("value").toString())){
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("pf_username", (String) properties.get("login"));
                                editor.putString("pf_password", (String) properties.get("password"));
                                editor.putString("pf_usercouchId", (String) properties.get("_id"));
                                editor.putString("pf_userfirstname", (String) properties.get("firstName"));
                                editor.putString("pf_userlastname", (String) properties.get("lastName"));
                                editor.putString("pf_usergender", (String) properties.get("Gender"));

                                try {
                                    String noOfVisits = properties.get("visits").toString();
                                    int currentTotalVisits = Integer.parseInt(noOfVisits) + totalVisits((String) properties.get("_id"));
                                    editor.putInt("pf_uservisits_Int", currentTotalVisits);
                                    editor.putString("pf_uservisits", currentTotalVisits+"");
                                    editor.putString("pf_lastVisitDate", doc_lastVisit);

                                } catch (Exception err) {
                                    alertDialogOkay(err.getMessage());
                                }
                                Set<String> stgSet = settings.getStringSet("pf_userroles", new HashSet<String>());
                                ArrayList roleList = (ArrayList<String>) properties.get("roles");
                                for (int cnt = 0; cnt < roleList.size(); cnt++) {
                                    stgSet.add(String.valueOf(roleList.get(cnt)));
                                }
                                editor.putStringSet("pf_userroles", stgSet);
                                editor.commit();
                                Log.e("MYAPP", " RowChipsView Login Id: " + doc_loginId + " Password: " + doc_password);
                                Intent intent = new Intent(this, FullscreenActivity.class);
                                startActivity(intent);
                                return true;

                            }

                            ////doc_credentials.get("salt").toString());
                            ///doc_credentials.get("value").toString()
                        } catch (Exception err) {
                            Log.e("MYAPP", " Encryption Err  " + err.getMessage());
                        }

                    } else{
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

    public boolean getSystemInfo(){
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("configurations");
            Query orderedQuery = chViews.LocalServerInfo(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                String Server_name = (String) properties.get("name");
                String Server_nationName = (String) properties.get("nationName");
                String Server_version = (String) properties.get("version");

                //////alertDialogOkay(Server_name+" Names");
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pf_server_name", Server_name);
                editor.putString("pf_server_nation", Server_nationName);
                editor.putString("pf_server_version", Server_version);
                editor.commit();

            }
            db.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int totalVisits(String memberId){
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        Database visitHolder;
        int doc_noOfVisits;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            visitHolder = manager.getDatabase("visits");
            Document retrievedDocument = visitHolder.getExistingDocument(memberId);
            if(retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                if(properties.containsKey("noOfVisits")){
                    doc_noOfVisits = (int) properties.get("noOfVisits") ;
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
            }
            else{
                Document newdocument = visitHolder.getDocument(memberId);
                Map<String, Object> newProperties = new HashMap<String, Object>();
                newProperties.put("noOfVisits", 1);
                doc_lastVisit = todaysDate();
                newProperties.put("lastVisits", doc_lastVisit);
                newdocument.putProperties(newProperties);
                return 1;
            }
        }catch(Exception err){
            Log.e("MyCouch", "ERR : " +err.getMessage());

        }

        return -1;

    }

    public String todaysDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println(dateFormat.format(cal.getTime()));
        return dateFormat.format(cal.getTime());

    }

    public void getSyncURLDialog(){

        final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        AlertDialog.Builder dialogB = new AlertDialog.Builder(this);
        dialogB.setView(R.layout.dialog_setup);
        dialogB.setCancelable(true);
        dialog = dialogB.create();
        dialog.show();
        final EditText txtSuncURL = (EditText) dialog.findViewById(R.id.txtNewSyncURL);
        txtSuncURL.setText(sys_oldSyncServerURL);

        Button TestConnButton = (Button) dialog.findViewById(R.id.btnTestCnnection);
        TestConnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TestConnectionToServer(txtSuncURL.getText().toString());
            }
        });

        ////
       // sys_singlefilestreamdownload =settings.getBoolean("pf_singlefilestreamdownload",true);
       /// sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload",true);

        dialogSyncButton = (Button) dialog.findViewById(R.id.btnNewSaveSyncURL);
        dialogSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sys_oldSyncServerURL = txtSuncURL.getText().toString();
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pf_sysncUrl", sys_oldSyncServerURL);
                editor.commit();
                dialog.dismiss();
                mDialog = new ProgressDialog(context);
                mDialog.setMessage("Please wait...");
                mDialog.setCancelable(false);
                try {
                    mDialog.show();
                    syncNotifier();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dialogSyncButton.setVisibility(View.INVISIBLE);

        if(wifiManager.isWifiEnabled()) {
            dialog.show();
            ////
        }else{
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface int_dialog, int which) {
                    switch (which){
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

    public void restorePref(){
        // Restore preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
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

        if(sys_username!=""){
            mUsername.setText(sys_username);
        }else{
            mUsername.setText("");
        }
        Set<String>  mwr = settings.getStringSet("membersWithResource",null);
        try{
            sys_membersWithResource = mwr.toArray();
            Log.e("MYAPP", " membersWithResource  = "+sys_membersWithResource.length);

        }catch(Exception err){
            Log.e("MYAPP", " Error creating  sys_membersWithResource");
        }
    }

    public void syncNotifier(){
        emptyAllDbs();
        //// Start creating filtered replication design Document
        try{
            designViewDoc = new JSONObject();
            JSONObject filter = new JSONObject();
            try {
                filter.put("by_resource","function(doc, req){return doc._id === req.query._id;}");
                designViewDoc.put("filters",filter);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            new RunCreateDocTask().execute("");
        }catch(Exception err){
            err.printStackTrace();
        }
        ///// End creating design Document
    }


    //////// Start creating filtered replication file in couchdb //

    public static String createDocument(String hostUrl, String databaseName, JSONObject jsonDoc,String DocId) {
        try {
            HttpPut httpPutRequest = new HttpPut(hostUrl +"/"+ databaseName+"/"+DocId);
            StringEntity body = new StringEntity(jsonDoc.toString(), "utf8");
            httpPutRequest.setEntity(body);
            httpPutRequest.setHeader("Accept", "application/json");
            httpPutRequest.setHeader("Content-type", "application/json");
            // timeout params
            HttpParams params = httpPutRequest.getParams();
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(1000));
            params.setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(1000));
            httpPutRequest.setParams(params);

            JSONObject jsonResult = sendCouchRequest(httpPutRequest);

            Log.e("MyCouch",  ""+hostUrl);
            Log.e("MyCouch",  ""+jsonResult);

            if (!jsonResult.getBoolean("ok")) {
                return null;
            }else if(jsonResult.getString("error")=="conflict"){
                Log.e("MyCouch", jsonResult.getString("reason"));
            }
            return jsonResult.getString("rev");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject sendCouchRequest(HttpUriRequest request) {
        try {
            HttpResponse httpResponse = (HttpResponse) new DefaultHttpClient().execute(request);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                // Read the content stream
                InputStream instream = entity.getContent();
                // Convert content stream to a String
                String resultString = convertStreamToString(instream);
                instream.close();
                // Transform the String into a JSONObject
                JSONObject jsonResult = new JSONObject(resultString);
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    class RunCreateDocTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;

        protected Boolean doInBackground(String... urls) {
            try {
                createDocument(sys_oldSyncServerURL, "resources", designViewDoc,"_design/apps");
                return true;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }
        protected void onPostExecute(Boolean docResult) {
            /// Start Syncing databases from server
            final AsyncTask<Void, Integer, String> execute = new FullscreenLogin.TestAsyncPull().execute();
            Log.e("MyCouch", "syncNotifier Running");
            final Thread th = new Thread(new Runnable() {
                private long startTime = System.currentTimeMillis();
                public void run() {
                    while (syncmembers) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(openMemberList) {
                                    mDialog.dismiss();
                                    openMemberList=false;
                                    alertDialogOkay("Completed. Thank you for waiting, you can now \" Sign In \" .");
                                    syncmembers=false;
                                    return;
                                }

                                Log.d("runOnUiThread", "running pull members");
                                mDialog.setMessage("Downloading, please wait ... " + databaseList[syncCnt] +" ["+ (syncCnt+1) +" / "+ databaseList.length+"]");
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
    }

    //////// End creating filtered replication file

    class TestAsyncPull extends AsyncTask<Void, Integer, String> {
        protected void onPreExecute (){
            Log.d("PreExceute","On pre Exceute......");
        }

        protected String doInBackground(Void...arg0) {
            Log.d("DoINBackGround","On doInBackground...");
            syncmembers =true;
            pull= new Replication[databaseList.length];
            db = new Database[databaseList.length];
            manager = new Manager[databaseList.length];
            try {
                manager[syncCnt] = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                db[syncCnt] = manager[syncCnt].getDatabase(databaseList[syncCnt]);
                URL url = new URL(sys_oldSyncServerURL+"/"+databaseList[syncCnt]);
                pull[syncCnt]=  db[syncCnt].createPullReplication(url);
                pull[syncCnt].setContinuous(false);
                pull[syncCnt].addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        if(pull[syncCnt] .isRunning()){
                            Log.e("MyCouch", databaseList[syncCnt]+" "+event.getChangeCount());
                        }else {
                            Log.e("Finished", databaseList[syncCnt]+" "+ db[syncCnt].getDocumentCount());
                            if(syncCnt < (databaseList.length-2)){
                                syncCnt++;
                                new FullscreenLogin.TestAsyncPull().execute();
                            }else{
                                Log.e("MyCouch","Sync Completed");
                                if(!openMemberList) {
                                    openMemberList = true;
                                }
                            }
                        }
                    }
                });
                pull[syncCnt].start();

            } catch (Exception e) {
                Log.e("MyCouch", databaseList[syncCnt]+" "+" Cannot create database", e);


            }
            publishProgress(syncCnt);
            return "You are at PostExecute";
        }
        protected void onProgressUpdate(Integer...a){
            Log.d("onProgress","You are in progress update ... " + a[0]);
            if(syncCnt != (databaseList.length-2)){
            }else{
            }
        }

        protected void onPostExecute(String result) {
            Log.d("OnPostExec",""+result);
        }
    }

    public void emptyAllDbs(){
        for (int cnt = 0; cnt < databaseList.length; cnt++) {
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbResources = manager.getDatabase(databaseList[cnt]);
                dbResources.delete();

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                String[] flist = myDir.list();
                for(int i=0;i<flist.length;i++) {
                    System.out.println(" " + myDir.getAbsolutePath());
                    File temp = new File(myDir.getAbsolutePath() + "/" + flist[i]);
                    if (temp.isDirectory()) {
                        Log.d("Delete "," Deleting "+temp.getName());
                        temp.delete();
                    } else {
                        temp.delete();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database dbResources = manager.getDatabase("resources");
            dbResources.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectWiFi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        String networkSSID = "\"Leonard's iPhone\"";
        String password="\"0l3gh@n@\"";

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.preSharedKey = "\"" + password + "\"";
//      conf.hiddenSSID = true;
//      conf.wepTxKeyIndex = 0;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//      conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        int netId = wifiManager.addNetwork(conf);
        wifiManager.enableNetwork(netId, true);
        wifiManager.setWifiEnabled(true);
    }

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

    private void copyAPK(int resource, String apkUrl) {
        InputStream in = getResources().openRawResource(resource);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/ole_temp2");
        if (!myDir.exists()){
            myDir.mkdirs();
        }
        File dst = new File(myDir,apkUrl);
        try {
            FileOutputStream out = new FileOutputStream(dst);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
            Log.e("tag", "Adobe Reader Copied "+ dst.toString());
        }catch(Exception err){
            err.printStackTrace();
        }
    }

    public void updateRemoteResourceRating(final String resouceId,final Double sum,final int timesRated,final String revision){
            final Fuel ful = new Fuel();
            ful.get(sys_oldSyncServerURL+"/resources/"+resouceId).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                @Override
                public void success(Request request, Response response, String s) {
                    try {
                        jsonServerData = new JSONObject(s);
                        Log.e("MyCouch", "resource current info "+jsonServerData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        final double remote_sum =  Double.parseDouble(jsonServerData.get("sum").toString());
                        final int remote_timesRated = (int) jsonServerData.get("timesRated");
                        if(remote_timesRated > timesRated){
                            Fuel nwfuel = new Fuel();
                            final List<Pair<String, Integer>> params = new ArrayList<Pair<String, Integer>>() {{
                                add(new Pair<String, Integer>("timesRated", (remote_timesRated+timesRated)));
                                add(new Pair<String, Integer>("sum", (int) (sum + remote_sum)));
                            }};
                            nwfuel.put(sys_oldSyncServerURL+"/resources/"+resouceId+"?new_edits=false&rev="+revision,params).responseString(new Handler<String>()  {
                                @Override
                                public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                                    updateUI(error, null);
                                }

                                @Override
                                public void success(@NotNull Request request, @NotNull Response response, String data) {
                                    updateUI(null, data);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void failure(Request request, Response response, FuelError fuelError) {
                    Log.e("MyCouch", "Reading reources error "+fuelError);

                }
            });
    }

    private void updateUI(final FuelError error, final String result) {
        runOnUiThread(new Runnable() {
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
        });
    }
}
