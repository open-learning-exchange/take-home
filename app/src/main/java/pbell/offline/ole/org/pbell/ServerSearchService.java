package pbell.offline.ole.org.pbell;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by leonardmensah on 30/03/2017.
 */

@SuppressWarnings("ALL")
public class ServerSearchService extends Service {
    private static final String TAG = "MYAPP";
    private Fuel ful = new Fuel();

    String sys_oldSyncServerURL;
    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    private static final int NOTIFICATION = 1;
    public static final String CLOSE_ACTION = "close";
    @Nullable
    private NotificationManager mNotificationManager = null;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                Log.e(TAG, " Service is still running");
                final WifiManager wifiManager = (WifiManager) ServerSearchService.this.getSystemService(Context.WIFI_SERVICE);
                if(wifiManager.isWifiEnabled()) {
                    if(!sys_oldSyncServerURL.equalsIgnoreCase("http://")) {
                        final Fuel ful = new Fuel();
                        ful.get(sys_oldSyncServerURL + "/_all_dbs").responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                            @Override
                            public void success(Request request, Response response, String s) {
                                try {
                                    List<String> myList = new ArrayList<String>();
                                    myList.clear();
                                    myList = Arrays.asList(s.split(","));
                                    Log.e("MyCouch", "-- " + myList.size());
                                    if (myList.size() < 8) {
                                        showNotificationMessage("Failed connection","Connection to "+sys_oldSyncServerURL +" failed. Check Wi-Fi");
                                        Log.e(TAG, "Check WiFi connection and try again");
                                    } else {
                                        showNotificationMessage("Connected","Planet connected successfully.");
                                        Log.e(TAG, "Connected to server");
                                        //alertDialogOkay("Connected to server");
                                        //connectionDialog.dismiss();
                                        // final ProgressDialog progressDialog = ProgressDialog.show(FullscreenActivity.this, "Please wait ...", "Syncing", false);
                                        //////////////////////////////
                                URL url = new URL(sys_oldSyncServerURL+"/shelf");
                                AndroidContext androidContext = new AndroidContext(context);
                                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                                Database database = manager.getDatabase("shelf");
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
                                            //progressDialog.dismiss();
                                            //runOnUiThread(new Runnable() {
                                            //    public void run() {
                                            //        alertDialogOkay("Tablet Updated Successfully. Logout and Login again to see changes");
                                            //    }
                                            //});
                                        } else {
                                            double total = push.getCompletedChangesCount() + pull.getCompletedChangesCount();
                                            ///showNotificationMessage("Updating device",(push.getChangesCount() + pull.getChangesCount()) + " / "+(int) total);
                                        }
                                    }
                                });
                                pull.start();
                                push.start();

                                        /////////////////////////
                                    }

                                } catch (Exception e) {
                                    showNotificationMessage("Failed connection","Connection to "+sys_oldSyncServerURL +" failed. Check Wi-Fi");
                                    Log.e(TAG, "Device couldn't reach server. Error");
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void failure(Request request, Response response, FuelError fuelError) {
                                showNotificationMessage("Failed connection","Connection to "+sys_oldSyncServerURL +" failed. Check Wi-Fi");
                                Log.e(TAG, "Device couldn't reach server. Check and try again");
                                Log.e(TAG, " " + fuelError);
                            }
                        });
                    }
                }else{
                    showNotificationMessage("Failed connection","You can not receive data from Planet ("+sys_oldSyncServerURL +") " +
                            "because Wi-Fi is OFF. Turn Wi-Fi on when you want to update Take-Home");
                }
                handler.postDelayed(runnable, 60000);
            }
        };

        handler.postDelayed(runnable, 120000);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        Log.e(TAG, " Service stopped");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        sys_oldSyncServerURL = intent.getStringExtra("serverUrl");
        setupNotifications();
        return START_STICKY;
    }

    private void setupNotifications() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        ///////PendingIntent pendingCloseIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).setAction(CLOSE_ACTION), 0);
        //.setContentIntent(pendingIntent)
        //.addAction(android.R.drawable.ic_menu_close_clear_cancel, "OK",null)
        mNotificationBuilder
                .setSmallIcon(R.drawable.ic_stat_ole_logo_trans_web)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getText(R.string.app_name)+" Planet Connection Status")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
    }

    private void showNotificationMessage(String sticker, String message) {
        mNotificationBuilder.setTicker(sticker).setContentText(message);
        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION, mNotificationBuilder.build());
        }
    }
}
