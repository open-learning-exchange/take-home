package pbell.offline.ole.org.pbell;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.BasicAuthenticator;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SyncDevice extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate= "";
    View clcview;
    FloatingActionButton fab;
    Boolean wipeClearn =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Restore preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");

        TextView lblSyncURL =  (TextView)findViewById(R.id.lblSyncSyncUrl);
        lblSyncURL.setText(sys_oldSyncServerURL);

        TextView lblLastSyncDate = (TextView)findViewById(R.id.lblLastDateSync);
        lblLastSyncDate.setText(sys_lastSyncDate);

        Button btnStartSync = (Button)findViewById(R.id.btnstartSync);
        btnStartSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                couchbaseInitDatabese();
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

    public void couchbaseInitDatabese(){
        try {
            Manager manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase("members");
                db.delete();
            }

            Database db = manager.getDatabase("members");
            URL url = new URL(sys_oldSyncServerURL+"/"+"members");
            Replication push = db.createPushReplication(url);
            Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    Log.e("MyCouch", ""+event.getChangeCount());
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    Log.e("MyCouch", ""+event.getChangeCount());
                    // will be called back when the pull replication status changes
                }
            });
            push.start();
            pull.start();
            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", "Cannot create database", e);
            return;
        }
    }

    public void ReplicateDatabeses(String databaseName){
        final String dbName = databaseName;
        try {
            Manager manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(dbName);
                db.delete();
            }

            Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            Replication push = db.createPushReplication(url);
            Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    Log.e("MyCouch", dbName+" "+event.getChangeCount());
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    Log.e("MyCouch", dbName+" "+event.getChangeCount());
                    // will be called back when the pull replication status changes
                }
            });
            push.start();
            pull.start();
            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", dbName+" "+" Cannot create database", e);
            return;
        }
    }

    /*
    * couchdb_members = TiTouchDB.databaseManager.getDatabase('members');
	couchdb_membercourseprogress = TiTouchDB.databaseManager.getDatabase('membercourseprogress');
	couchdb_meetups = TiTouchDB.databaseManager.getDatabase('meetups');
	couchdb_usermeetups = TiTouchDB.databaseManager.getDatabase('usermeetups');
	couchdb_assignments = TiTouchDB.databaseManager.getDatabase('assignments');
	couchdb_calendar = TiTouchDB.databaseManager.getDatabase('calendar');
	couchdb_groups = TiTouchDB.databaseManager.getDatabase('groups');
	couchdb_invitations = TiTouchDB.databaseManager.getDatabase('invitations');
	couchdb_languages = TiTouchDB.databaseManager.getDatabase('languages');
	couchdb_shelf = TiTouchDB.databaseManager.getDatabase('shelf');
	couchdb_requests = TiTouchDB.databaseManager.getDatabase('requests');
	*/
}
