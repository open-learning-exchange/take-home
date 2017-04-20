package pbell.offline.ole.org.pbell;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

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
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClientAndroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("ALL")
public class SyncDevice extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate= "";

    CouchViews chViews = new CouchViews();

    TextView tv;
    View clcview;
    String message ="";
    String str_memberID,str_resourceId;
    //////Replication push,pull;
    FloatingActionButton fab;
    Boolean wipeClearn =false;
    final Context context = this;
    String[] databaseList = {"members","membercourseprogress","meetups","usermeetups","assignments","coursestep",
            "calendar","groups","invitations","configurations","requests","shelf","languages"};
    Replication[] pull= new Replication[databaseList.length];
    Database[] db = new Database[databaseList.length];
    Manager[] manager = new Manager[databaseList.length];

    String[] pushdatabaseList = {"members"};
    Replication[] push = new Replication[pushdatabaseList.length];
    Database[] push_db = new Database[pushdatabaseList.length];
    Manager[] push_manager = new Manager[pushdatabaseList.length];

    ProgressDialog[] progressDialog = new ProgressDialog[databaseList.length];

    JSONObject designViewDoc;

    AndroidContext androidContext;
    int syncCnt=0;
    int pushsyncCnt=0;
    Button btnStartSyncPush,btnStartSyncPull;
    String[] str_memberIdList,str_memberNameList,str_memberLoginIdList;
    int[] str_memberResourceNo;

    Boolean synchronizingPull = true;
    Boolean synchronizingPush = true;
    JSONObject jsonData;
    int doc_rating,doc_timesRated;
    ArrayList<String> doc_comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        androidContext = new AndroidContext(this);
        // Restore preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
        //tv = (TextView)findViewById(R.id.txtLogConsole);

        TextView lblSyncURL =  (TextView)findViewById(R.id.lblSyncSyncUrl);
        lblSyncURL.setText(sys_oldSyncServerURL);

        TextView lblLastSyncDate = (TextView)findViewById(R.id.lblLastDateSync);
        lblLastSyncDate.setText(sys_lastSyncDate);

        tv = (TextView)findViewById(R.id.txtLogConsole);
        tv.setMovementMethod(new ScrollingMovementMethod());

        btnStartSyncPull = (Button)findViewById(R.id.btnstartSyncPull);
        btnStartSyncPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushsyncCnt=0;
                tv.setText(" Sync Started, please wait ... " );
                tv.scrollTo(0,tv.getTop());
                pullSyncNotifier();
                ///new TestAsyncPull().execute();
            }

        });

        btnStartSyncPush = (Button)findViewById(R.id.btnstartSyncPush);
        btnStartSyncPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncCnt=0;
                tv.setText(" Sync Started, please wait ... " );
                tv.scrollTo(0,tv.getTop());
                pushSyncNotifier();
            }

        });

        Button btnSyncResources = (Button)findViewById(R.id.btnsyncResources);
        btnSyncResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synchronizingPull=false;

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
                    ///err.printStackTrace();
                }

                triggerMemberResourceDownload();
            }

        });

        Switch sw_wipeClean = (Switch) findViewById(R.id.swWipeClean);
        sw_wipeClean.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 wipeClearn = isChecked;
                ///Log.v("Switch State=", ""+isChecked);
            }
        });


        fab = (FloatingActionButton) findViewById(R.id.checkConnection);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clcview = view;
                try {
                    if (checkConnectionURL()) {
                        fab.setVisibility(View.INVISIBLE);
                        Snackbar.make(clcview, "Checking connection to "+sys_oldSyncServerURL +".... please wait", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", null).show();
                        new TryDownloadTask().execute(sys_oldSyncServerURL);

                    } else {

                    }
                }catch(Exception e) {
                    Log.v("myErrorTag","Error "+e.getLocalizedMessage());

                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    class RunCreateDocTask extends AsyncTask<String, Void, Boolean> {
        private Exception exception;
        private String cls_SyncServerURL;
        private String cls_DbName;
        private String cls_DocNameId;
        private JsonObject cls_ViewContent;
        public String getSyncServerURL(){
            return cls_SyncServerURL;
        }
        public void setSyncServerURL(String oldSyncServerURL){
            cls_SyncServerURL = oldSyncServerURL;
        }
        public String getDbName(){
            return cls_DbName;
        }
        public void setDbName(String dbName){
            cls_DbName = dbName;
        }
        public String getDocNameId(){
            return cls_DocNameId;
        }
        public void setDocNameId(String docNameId){
            cls_DocNameId = docNameId;
        }
        public JsonObject getViewContent(){
            return cls_ViewContent;
        }
        public JsonObject setViewContent(JsonObject viewContent) {
            cls_ViewContent = viewContent;
            return cls_ViewContent;
        }
        protected Boolean doInBackground(String... urls) {
            try {
                Log.e("MyCouch", "URL = "+getSyncServerURL());
                URI uri = URI.create(getSyncServerURL());
                String url_Scheme = uri.getScheme();
                String url_Host = uri.getHost();
                int url_Port = uri.getPort();
                String url_user = "", url_pwd = "";
                if (uri.getUserInfo() != null) {
                    String[] userinfo = uri.getUserInfo().split(":");
                    url_user = userinfo[0];
                    url_pwd = userinfo[1];
                }
                CouchDbClientAndroid dbClient = new CouchDbClientAndroid(getDbName(), true, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                Log.e("MyCouch", "Creating design document "+getDocNameId()+" --- "+getDbName()+" --- "+url_Scheme+" --- " +url_Host+" --- " +url_Port+" --- " + url_user+" --- " + url_pwd);
                if(!dbClient.contains(URLEncoder.encode(getDocNameId(), "UTF-8"))){
                    JsonObject json = new JsonObject();
                    json.addProperty("_id", getDocNameId());
                    json.add("filters", getViewContent());
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

        }
    }

    /////// End Filtered Replication View File

    public void triggerMemberResourceDownload(){
        if(!synchronizingPull) {
            synchronizingPull = true;
            Log.e("MyCouch", "Sync Variable false");
            if (BuildMemberListArray()) {
                for (int cnt = 0; cnt < str_memberIdList.length; cnt++) {
                    ArrayList<String> strings = CheckShelfForResources(cnt);
                }
                Bundle b = new Bundle();
                b.putStringArray("memberNameList", str_memberNameList);
                b.putStringArray("memberIdList", str_memberIdList);
                b.putIntArray("memberResourceNo", str_memberResourceNo);
                b.putStringArray("memberLoginIdList", str_memberLoginIdList);
                Intent intent = new Intent(this, MemberListDownloadRes.class);
                intent.putExtras(b);
                startActivity(intent);
            } else {
                Log.e("MyCouch", "triggerMemberResourceDownload function error");
            }
            synchronizingPull = true;
        }else{
            Log.e("MyCouch", "Sync Variable True");
        }
    }

    public void pullSyncNotifier(){
         final AsyncTask<Void, Integer, String> execute = new TestAsyncPull().execute();
         Log.e("MyCouch", "pull_syncNotifier Running");
         final Thread th = new Thread(new Runnable() {
             private long startTime = System.currentTimeMillis();
             public void run() {
                 while (synchronizingPull) {
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                            ///triggerMemberResourceDownload();
                             ///Log.d("runOnUiThread", "running");
                             //mydialog.setMessage("Downloading, please wait .... " + (syncCnt + 1));
                         }
                     });
                     try {
                         Thread.sleep(9000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
         th.start();
     }

    public void pushSyncNotifier(){
        BuildMemberListArray();
        for(int cnt=0;cnt<str_memberIdList.length;cnt++){
            ///updateLocalMembers(str_memberIdList[cnt]);
        }
        //Todo read resource database and pass id's to function update it.

        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database resources_db = manager.getExistingDatabase("resources");
            Query query = resources_db.createAllDocumentsQuery();
            query.setAllDocsMode(Query.AllDocsMode.ONLY_CONFLICTS);
            QueryEnumerator result = query.run();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                if (row.getConflictingRevisions().size() > 0) {
                    Log.e("MyCouch", "Resource Id "+row.getDocumentId());;
                   // Log.w("MYAPP", "Conflict in document: %s", row.getDocumentId());
                   /// beginConflictResolution(row.getDocument());
                }
            }

        }catch(Exception err){

        }


        /////updateLocalResources(resourceId);
    /*
        final AsyncTask<Void, Integer, String> execute = new TestAsyncPush().execute();
        Log.e("MyCouch", "push_SyncNotifier Running");
        final Thread th = new Thread(new Runnable() {
            private long startTime = System.currentTimeMillis();
            public void run() {
                while (synchronizingPush) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    try {
                        Thread.sleep(9000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();

        */

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

    private class TryDownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            InputStream is = null;
            int response=0;
            try {
                    URL url = new URL(sys_oldSyncServerURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000 /* milliseconds */);
                    conn.setConnectTimeout(10000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    response = conn.getResponseCode();
                    Log.d("Responce", "The response is: " + response);
                    is = conn.getInputStream();
                return "";
            }catch (Exception err) {

            }finally
            {
                if(response!=200){
                    displayDownMesage(false);
                }else{
                    displayDownMesage(true);
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
           /// textView.setText(result);
        }
    }

    public void displayDownMesage(boolean status){
        if(status){
            Snackbar.make(clcview, "Connection to established successful", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }else{
            Snackbar.make(clcview, "Sorry , server was unreachable, check url and device connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    class TestAsyncPull extends AsyncTask<Void, Integer, String> {
        protected void onPreExecute (){
            Log.d("PreExceute","On pre Exceute......");
        }

        protected String doInBackground(Void...arg0) {
            Log.d("DoINBackGround","On doInBackground...");
            synchronizingPull=true;
            pull= new Replication[databaseList.length];
            db = new Database[databaseList.length];
            manager = new Manager[databaseList.length];
                try {
                    manager[syncCnt] = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                    if(wipeClearn){
                        try{
                            db[syncCnt] = manager[syncCnt].getExistingDatabase(databaseList[syncCnt]);
                            db[syncCnt].delete();
                        }catch(Exception err){
                            Log.e("MyCouch", "Delete Error "+ err.getLocalizedMessage());
                        }
                    }

                    db[syncCnt] = manager[syncCnt].getDatabase(databaseList[syncCnt]);
                    URL url = new URL(sys_oldSyncServerURL+"/"+databaseList[syncCnt]);
                    pull[syncCnt]=  db[syncCnt].createPullReplication(url);
                    pull[syncCnt].setContinuous(false);
                    pull[syncCnt].addChangeListener(new Replication.ChangeListener() {
                        @Override
                        public void changed(Replication.ChangeEvent event) {
                            if(pull[syncCnt] .isRunning()){
                                Log.e("MyCouch", databaseList[syncCnt]+" "+event.getChangeCount());
                                message = String.valueOf(event.getChangeCount());
                            }else {
                                Log.e("Finished", databaseList[syncCnt]+" "+ db[syncCnt].getDocumentCount());
                                if(syncCnt < (databaseList.length-2)){
                                    syncCnt++;
                                    new TestAsyncPull().execute();
                                }else{
                                    Log.e("MyCouch","Sync Completed");
                                    triggerMemberResourceDownload();
                                    synchronizingPull=true;
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
                tv.setText(tv.getText().toString()+" \n Pulled "+databaseList[syncCnt]+ "\n Pulling "+ databaseList[syncCnt+1]+"....." );
                tv.scrollTo(0,(tv.getLineCount()*20+syncCnt));

                tv.requestFocus();
            }else{
                tv.setText(tv.getText().toString()+" \n Pulled "+ databaseList[syncCnt] );
                tv.scrollTo(0,(tv.getLineCount()*20)+syncCnt);

                tv.requestFocus();
            }
        }

        protected void onPostExecute(String result) {
            Log.d("OnPostExec",""+result);
        }
    }

    class TestAsyncPush extends AsyncTask<Void, Integer, String> {
        protected void onPreExecute (){
            Log.d("PreExceute","On pre Exceute......");
        }

        protected String doInBackground(Void...arg0) {
            Log.d("DoINBackGround","On doInBackground...");
            synchronizingPush=true;
            push = new Replication[pushdatabaseList.length];
            push_db = new Database[pushdatabaseList.length];
            push_manager = new Manager[pushdatabaseList.length];
            try {
                push_manager[pushsyncCnt] = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                push_db[pushsyncCnt] = push_manager[syncCnt].getDatabase(pushdatabaseList[pushsyncCnt]);
                URL url = new URL(sys_oldSyncServerURL+"/"+pushdatabaseList[pushsyncCnt]);
                push[pushsyncCnt]=  push_db[pushsyncCnt].createPushReplication(url);
                push[pushsyncCnt].setContinuous(false);
                push[pushsyncCnt].addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        if(push[pushsyncCnt] .isRunning()){
                            Log.e("MyCouch", pushdatabaseList[pushsyncCnt]+" "+event.getChangeCount());
                            message = String.valueOf(event.getChangeCount());
                        }else {
                            Log.e("Finished", pushdatabaseList[pushsyncCnt]+" "+ push_db[pushsyncCnt].getDocumentCount());
                            if(pushsyncCnt+1 < (pushdatabaseList.length)){
                                pushsyncCnt++;
                                new TestAsyncPush().execute();
                            }else{
                                Log.e("MyCouch","Sync Completed");
                                synchronizingPush=false;
                                mydeleteDatabase("visits");
                            }

                        }
                    }
                });
                push[pushsyncCnt].start();

            } catch (Exception e) {
                Log.e("MyCouch", pushdatabaseList[pushsyncCnt]+" "+" Cannot create database", e);


            }
            publishProgress(pushsyncCnt);
            return "You are at PostExecute";
        }
        protected void onProgressUpdate(Integer...a){
            Log.d("onProgress","You are in progress update ... " + a[0]);
                tv.setText(tv.getText().toString()+" \n Pushed "+ pushdatabaseList[pushsyncCnt] );
                tv.scrollTo(0,(tv.getLineCount()*20)+pushsyncCnt);

                tv.requestFocus();
        }

        protected void onPostExecute(String result) {
            Log.d("OnPostExec",""+result);
        }
    }

    public ArrayList<String> CheckShelfForResources(int index) {
        int array_index = index;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database db = manager.getExistingDatabase("shelf");
                Query orderedQuery = chViews.ReadShelfByIdView(db).createQuery();
                orderedQuery.setDescending(true);
                //Todo startKey and EndKey generate inaccurate result
                ///orderedQuery.setStartKey(str_memberIdList[array_index]);
                //orderedQuery.setLimit(0);
                ArrayList<String> lst = new ArrayList<String>();
                QueryEnumerator results = orderedQuery.run();
                for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                    QueryRow row = it.next();
                    String docId = (String) row.getValue();
                    Document doc = db.getExistingDocument(docId);
                    Map<String, Object> properties = doc.getProperties();
                    if(str_memberIdList[array_index].equals((String)properties.get("memberId"))){
                        lst.add((String)properties.get("resourceId"));
                    }
                }
                Object[] st = lst.toArray();
                for (Object s : st) {
                    if (lst.indexOf(s) != lst.lastIndexOf(s)) {
                        lst.remove(lst.lastIndexOf(s));
                    }
                }
                str_memberResourceNo[array_index]=lst.size();

            db.close();
            return lst;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public boolean BuildMemberListArray(){
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("members");
            Query orderedQuery = chViews.CreateListByNameView(db).createQuery();
            int memberCounter = 0;
            orderedQuery.setStartKey("A");
            //orderedQuery.setEndKey("a");
            ///orderedQuery.setDescending(false);
            //orderedQuery.setLimit(0);


            QueryEnumerator results = orderedQuery.run();
            str_memberIdList = new String[results.getCount()];
            str_memberNameList = new String[results.getCount()];
            str_memberLoginIdList = new String[results.getCount()];
            str_memberResourceNo = new int[results.getCount()];
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                str_memberIdList[memberCounter] = (String) properties.get("_id");
                str_memberNameList[memberCounter] = (String) properties.get("firstName") +" "+(String) properties.get("lastName");
                str_memberLoginIdList[memberCounter] = (String) properties.get("login");
                str_memberResourceNo[memberCounter] = 0;
                Log.e("MYAPP", " Member Name List: " + str_memberNameList[memberCounter] +" ("+str_memberLoginIdList[memberCounter]+")");
                memberCounter++;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }



    }

    public void updateLocalResources(String resourceId){
        str_resourceId = resourceId;
        final Fuel ful = new Fuel();

        ful.get(sys_oldSyncServerURL+"/resources/"+resourceId).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
            @Override
            public void success(Request request, Response response, String s) {
                try {
                    jsonData = new JSONObject(s);
                    Log.e("MyCouch", "-- "+jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    int remote_rating = (int) jsonData.get("sum");
                    int remote_timesRated = (int) jsonData.get("timesRated");
                    ArrayList<String> remote_comments = (ArrayList<String>) jsonData.get("comments");
                    Manager manager = null;
                    Database ratingHolder;
                    //int doc_noOfVisits;
                    //int total_visits=0;
                    //String max_lastLoginDate="";

                    try {
                        manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                        ratingHolder = manager.getDatabase("resourcerating");
                        Document retrievedDocument = ratingHolder.getExistingDocument(str_resourceId);
                        if(retrievedDocument != null) {
                            Map<String, Object> properties = retrievedDocument.getProperties();
                            if (properties.containsKey("sum")) {
                                doc_rating = (int) properties.get("sum");
                                doc_timesRated = (int) properties.get("timesRated");
                                ArrayList<String> doc_comments = (ArrayList<String>) properties.get("comments");
                                ///total_visits = doc_noOfVisits + remote_NoOfVisits;

                                Database resources_db = manager.getExistingDatabase("resources");
                                Document resourceDoc = resources_db.getExistingDocument(str_resourceId);

                                /// Save total no of visits for member in member database
                                Map<String, Object> doc_properties = new HashMap<String, Object>();
                                doc_properties.putAll(resourceDoc.getProperties());
                                doc_properties.put("sum", (doc_rating+remote_rating));
                                doc_properties.put("timesRated", (doc_timesRated+remote_timesRated));
                                doc_properties.put("timesRated", (doc_timesRated+remote_timesRated));
                                resourceDoc.putProperties(doc_properties);

                                Log.e("MyCouch Remote", "Rating Sum :  -----  "+(doc_rating+remote_rating));
                                Log.e("MyCouch Remote", "Times rated :  -----  "+ (doc_timesRated+remote_timesRated));
                                Log.e("MyCouch Remote", "Times rated :  -----  "+ (doc_timesRated+remote_timesRated));

                            }
                        }
                    }catch(Exception Err){
                        Log.e("MyCouch Remote Error", " :  "+ Err.getMessage());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failure(Request request, Response response, FuelError fuelError) {
                Log.e("MyCouch", " "+fuelError);

            }
        });
    }

    public void updateLocalMembers(String memberId){
        str_memberID = memberId;
        final Fuel ful = new Fuel();
        ful.get(sys_oldSyncServerURL+"/members/"+memberId).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
            @Override
            public void success(Request request, Response response, String s) {
                try {
                    jsonData = new JSONObject(s);
                    Log.e("MyCouch", "-- "+jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    int remote_NoOfVisits = (int) jsonData.get("visits");
                    String remote_lastLoginDate = (String) jsonData.get("lastLoginDate");
                    Manager manager = null;
                    Database visitHolder;
                    int doc_noOfVisits;
                    int total_visits=0;
                    String max_lastLoginDate="";

                    try {
                        manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                        visitHolder = manager.getDatabase("visits");
                        Document retrievedDocument = visitHolder.getExistingDocument(str_memberID);
                        if(retrievedDocument != null) {
                            Map<String, Object> properties = retrievedDocument.getProperties();
                            if (properties.containsKey("noOfVisits")) {
                                doc_noOfVisits = (int) properties.get("noOfVisits");
                                total_visits = doc_noOfVisits + remote_NoOfVisits;
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                String localLastVisit = (String) properties.get("lastVisits");
                                Date localDate = sdf.parse(localLastVisit.substring(0,10));
                                Date remoteDate = sdf.parse(remote_lastLoginDate.substring(0,10));
                               if(localDate.after(remoteDate)){
                                    System.out.println("Local is latest date");
                                    max_lastLoginDate = localLastVisit.toString();

                                }else if(remoteDate.before(localDate)){
                                    System.out.println("Remote is latest date");
                                    max_lastLoginDate = remote_lastLoginDate.toString();

                                }else if(localDate.equals(remoteDate)){
                                    System.out.println("Remote is equal to Local");
                                    max_lastLoginDate = localLastVisit.toString();

                                }

                                Database members_db = manager.getExistingDatabase("members");
                                Document memberDoc = members_db.getExistingDocument(str_memberID);

                                /// Save total no of visits for member in member database
                                Map<String, Object> doc_properties = new HashMap<String, Object>();
                                doc_properties.putAll(memberDoc.getProperties());
                                doc_properties.put("visits", doc_noOfVisits);
                                doc_properties.put("lastLoginDate", max_lastLoginDate);
                                memberDoc.putProperties(doc_properties);

                                Log.e("MyCouch Remote", "Visits :  -----  "+total_visits);
                                Log.e("MyCouch Remote", "Visit Max Date :  -----  "+max_lastLoginDate);

                            }
                        }
                    }catch(Exception Err){
                        Log.e("MyCouch Remote Error", " :  "+ Err.getMessage());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failure(Request request, Response response, FuelError fuelError) {
                Log.e("MyCouch", " "+fuelError);

            }
        });
    }

    public boolean mydeleteDatabase(String databaseName){
        ///// Delete local visit database (truncate it)
        try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database visitHolder = manager.getDatabase(databaseName);
                visitHolder.delete();
            return true;
        }catch(Exception err){
                Log.e("VISITS", "ERR : " +err.getMessage());
            return false;
        }
    }

}
