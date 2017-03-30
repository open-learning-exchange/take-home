package pbell.offline.ole.org.pbell;
import android.app.ProgressDialog;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.couchbase.lite.replicator.Replication;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by leonardmensah on 30/03/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ServerSearchService extends JobService {
    private static final String TAG = "ServerSearchService";
    private Fuel ful = new Fuel();
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;

    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    int sys_uservisits_Int = 0;
    Object[] sys_membersWithResource;

    @Override
    public boolean onStartJob(JobParameters params) {
        //restorePref();
        /*PersistableBundle pb =params.getExtras();
        sys_oldSyncServerURL = pb.getPersistableBundle("serverUrl").toString();
        Log.e("MyCouch", "Joe title " + params.getJobId());
        ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
            @Override
            public void success(Request request, Response response, String s) {
                try {
                    List<String> myList = new ArrayList<String>();
                    myList.clear();
                    myList = Arrays.asList(s.split(","));
                    Log.e("MyCouch", "-- " + myList.size());
                    if (myList.size() < 8) {
                        //alertDialogOkay("Check WiFi connection and try again");
                        //connectionDialog.dismiss();
                    } else {
                        //alertDialogOkay("Connected to server");
                        //connectionDialog.dismiss();
                        /////////////////////////
                    }

                } catch (Exception e) {
                    //connectionDialog.dismiss();
                    //alertDialogOkay("Device couldn't reach server. Check and try again");
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(Request request, Response response, FuelError fuelError) {
                //connectionDialog.dismiss();
                //alertDialogOkay("Device couldn't reach server. Check and try again");
                Log.e("MyCouch", " " + fuelError);
            }
        });*/
        Log.i(TAG, "onStartJob:");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob:");
        return false;
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
}
