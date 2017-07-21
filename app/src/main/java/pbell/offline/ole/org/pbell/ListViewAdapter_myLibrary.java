package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 06/06/2017.
 */

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.lightcouch.CouchDbClientAndroid;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ListViewAdapter_myLibrary extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;
    private static final String TAG = "MYAPP";
    Context context;
    User_Dashboard user_dashboard = new User_Dashboard();
    private ProgressDialog mDialog;
    private long resources_enqueue;
    boolean singleFileDownload = true;
    public static final String PREFS_NAME = "MyPrefsFile";

    /// String
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    String OneByOneResID, OneByOneResTitle;
    SharedPreferences settings;
    List<String> resIDArrayList = new ArrayList<>();

    LogHouse logHouse = new LogHouse();
    protected int _splashTime = 5000;
    private Thread splashTread;

    String openedResourceId, openedResourceTitle = "";
    boolean openedResource = false;



    private String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);
    private  OnResouceListListener mListener;

    public ListViewAdapter_myLibrary(final List<String> resIDsList, Activity a, Context cont, ArrayList<HashMap<String, String>> d) {
        resIDArrayList.addAll(resIDsList);
        activity = a;
        data = d;
        context = cont.getApplicationContext();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());


        BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                if (id != resources_enqueue) {
                    Log.v(TAG, "Ingnoring unrelated download " + id);
                    return;
                }
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);
                // it shouldn't be empty, but just in case
                if (!cursor.moveToFirst()) {
                    mDialog.dismiss();
                    alertDialogOkay("Download not responding, check WiFi connection");
                    mListener.onResourceDownloadCompleted(OneByOneResTitle, resIDArrayList);
                    return;
                }
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                    Log.w(TAG, "Download Failed");
                    mDialog.dismiss();
                    alertDialogOkay("Download failed check WiFi connection");
                    mListener.onResourceDownloadCompleted(OneByOneResTitle, resIDArrayList);
                    return;
                }
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(statusIndex)) {
                    Log.w(TAG, "Download successfully");
                    mDialog.dismiss();
                    alertDialogOkay("Download Completed");
                    mListener.onResourceDownloadCompleted(OneByOneResTitle, resIDArrayList);
                    return;
                }

                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String downloadedPackageUriString = cursor.getString(uriIndex);
            }
        };

        context.registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);
        this.mListener = null;
        mListener = (OnResouceListListener) cont;
    }



    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.listview_row_mylibrary, null);
        }

        TextView title = (TextView) vi.findViewById(R.id.list_title); // title
        TextView description = (TextView) vi.findViewById(R.id.list_desc); // description
        Button details = (Button) vi.findViewById(R.id.btn_listVewDetails); // details
        Button feedback = (Button) vi.findViewById(R.id.btn_listFeedback); // feedback
        Button delete = (Button) vi.findViewById(R.id.btn_listDelete); // delete
        Button open = (Button) vi.findViewById(R.id.btn_listOpen); // delete
        TextView ratingAvgNum = (TextView) vi.findViewById(R.id.lbl_listAvgRating); // delete
        TextView totalNum = (TextView) vi.findViewById(R.id.lbl_listTotalrating); // delete
        RatingBar ratingStars = (RatingBar) vi.findViewById(R.id.list_rating);
        LayerDrawable stars = (LayerDrawable) ratingStars.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffa500"), PorterDuff.Mode.SRC_ATOP);

        ProgressBar femalerating = (ProgressBar) vi.findViewById(R.id.female_progressbar); // delete
        ProgressBar malerating = (ProgressBar) vi.findViewById(R.id.male_progressbar); // delete
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image); //  image

        HashMap<String, String> material = new HashMap<>();
        material = data.get(position);

        // Setting all values in Fragm_myLibrary
        title.setText(material.get(Fragm_myLibrary.KEY_TITLE));
        description.setText(material.get(Fragm_myLibrary.KEY_DESCRIPTION));
        details.setText("Details");
        details.setTag(material.get(Fragm_myLibrary.KEY_DETAILS));
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(v.getTag().toString(), "Details");
                Log.i(TAG, "Details Clicked ********** " + v.getTag());
            }
        });
        feedback.setText("Feedback");
        feedback.setTag(material.get(Fragm_myLibrary.KEY_FEEDBACK));
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(v.getTag().toString(), "Feedback");
                Log.i(TAG, "Feedback Clicked ********** " + v.getTag());
            }
        });
        delete.setText("Delete");
        delete.setTag(material.get(Fragm_myLibrary.KEY_DELETE));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(v.getTag().toString(), "Delete");
                Log.i(TAG, "Delete Clicked ********** " + v.getTag());
            }
        });

        if (material.get(Fragm_myLibrary.KEY_RESOURCE_STATUS).equalsIgnoreCase("downloaded")) {
            open.setText("Open");
            open.setTag(material.get(Fragm_myLibrary.KEY_ID));
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonAction(v.getTag().toString(), "Open");
                }
            });
        } else {
            open.setText("Download");
            open.setTag(material.get(Fragm_myLibrary.KEY_ID));
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonAction(v.getTag().toString(), "Download");
                }
            });
        }

        ratingAvgNum.setText("" + material.get(Fragm_myLibrary.KEY_RATING));
        ratingStars.setRating(Float.parseFloat("" + material.get(Fragm_myLibrary.KEY_RATING)));
        totalNum.setText(material.get(Fragm_myLibrary.KEY_TOTALNUM_RATING));
        femalerating.setProgress(Integer.parseInt("1"));
        //femalerating.setProgress(Integer.parseInt(material.get(Fragm_myLibrary.KEY_FEMALE_RATING)));
        malerating.setProgress(Integer.parseInt("1"));
        //malerating.setProgress(Integer.parseInt(material.get(Fragm_myLibrary.KEY_MALE_RATING)));
        imageLoader.DisplayImage(material.get(Fragm_myLibrary.KEY_THUMB_URL), thumb_image);
        return vi;
    }

    public void alertDialogOkay(String Message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
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


    public void restorePreferences(Activity activity) {
        settings = activity.getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username", "");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl", "http://");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate", "");
        sys_password = settings.getString("pf_password", "");
        sys_usercouchId = settings.getString("pf_usercouchId", "");
        sys_userfirstname = settings.getString("pf_userfirstname", "");
        sys_userlastname = settings.getString("pf_userlastname", "");
        sys_usergender = settings.getString("pf_usergender", "");
        sys_uservisits = settings.getString("pf_uservisits", "");
        sys_servername = settings.getString("pf_server_name", " ");
        sys_serverversion = settings.getString("pf_server_version", " ");
    }

    public void createResourceDoc(String manualResId, String manualResTitle, String manualResopenWith) {
        Database database = null;
        try {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase("resources");
            Map<String, Object> properties = new HashMap<>();
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

    public void buttonAction(String resourceId, String action) {
        switch (action) {
            case "Delete":
                break;
            case "Details":
                break;
            case "Feedback":
                sendResourceRatingFeedback(resourceId);
                break;
            case "Open":
                mDialog = new ProgressDialog(activity.getWindow().getContext());
                mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mDialog.setMessage("Please wait...");
                mDialog.setCancelable(false);
                mDialog.show();
                if (openResources(resourceId)) {
                    Log.i(TAG, "Open Clicked ********** " + resourceId);
                } else {
                    Log.i(TAG, "Open  ********** " + resourceId);
                }
                break;
            case "Download":
                OneByOneResID = resourceId;
                restorePreferences(activity);
                mDialog = new ProgressDialog(activity.getWindow().getContext());
                mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mDialog.setMessage("Please wait...");
                mDialog.setCancelable(false);
                mDialog.show();
                singleFileDownload = true;
                new downloadSpecificResourceToDisk().execute();
                Log.i(TAG, "Downloading  ********** " + resourceId);
                break;
        }
    }

    public void downloadWithDownloadManagerSingleFile(String fileURL, String FileName) {
        String url = fileURL;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(OneByOneResID + "-" + OneByOneResTitle);
        request.setTitle(OneByOneResTitle);
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        Log.e(TAG, " Destination is " + FileName);
        request.setDestinationInExternalPublicDir("ole_temp", FileName);
        // get download service and enqueue file
        mDialog.setMessage("Downloading  \" " + OneByOneResTitle + " \" . please wait...");
 //       resources_downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
 //       resources_enqueue = resources_downloadManager.enqueue(request);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        resources_enqueue = downloadManager.enqueue(request);
    }

    public interface OnResouceListListener {
        void onResourceDownloadCompleted(String CourseId, Object data);
        void onResourceOpened(String resourceId, String resourceTitle);
    }

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
                    OneByOneResTitle = title;
                    Log.e(TAG, "Open With -- " + openWith);
                    if (!openWith.equalsIgnoreCase("HTML")) {
                        JSONObject _attachments = new JSONObject(jsonAttachments.toString());
                        Iterator<String> keys = _attachments.keys();
                        if (keys.hasNext()) {
                            String key = (String) keys.next();
                            Log.e(TAG, "-- " + key);
                            final String encodedkey = URLEncoder.encode(key, "utf-8");
                            File file = new File(encodedkey);
                            String extension = encodedkey.substring(encodedkey.lastIndexOf("."));
                            final String diskFileName = OneByOneResID + extension;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadWithDownloadManagerSingleFile(sys_oldSyncServerURL + "/resources/" + OneByOneResID + "/" + encodedkey, diskFileName);
                                    createResourceDoc(OneByOneResID, title, openWith);
                                }
                            });
                        }
                    } else {
                        /*Log.e("MyCouch", "-- HTML NOT PART OF DOWNLOADS ");
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
                        }*/
                    }
                }else{
                    mDialog.dismiss();
                    alertDialogOkay("This item wasn't found on the server, check connection and try again");
                }
            } catch (Exception e) {
                mDialog.dismiss();
                e.printStackTrace();
                Log.e(TAG, "Download this resource error " + e.getMessage());
                ///alertDialogOkay("Error downloading file, check connection and try again");
                return null;
            }
            return null;
        }
    }

    public Boolean openResources(String id) {
        String resourceIdTobeOpened = id;
        Log.d(TAG, "Trying to open resource " + id);
        try {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(resourceIdTobeOpened);
            String openwith = (String) res_doc.getProperty("openWith");
            String openResName = (String) res_doc.getProperty("title");
            openedResourceId = resourceIdTobeOpened;
            openedResourceTitle = openResName;
            ///openFromDiskDirectly = true;
            logHouse.updateActivityOpenedResources(context, sys_usercouchId, resourceIdTobeOpened, openResName);
            Log.e("MYAPP", " member opening resource  = " + resourceIdTobeOpened + " and Open with " + openwith);
            List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
 //// PDF and Bell-Reader
             if (openwith.equalsIgnoreCase("PDF.js") || (openwith.equalsIgnoreCase("Bell-Reader"))) {
                Log.e(TAG, " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e(TAG, " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            try {
                                mDialog.dismiss();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setPackage("com.adobe.reader");
                                intent.setDataAndType(Uri.fromFile(f), "application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            } catch (Exception err) {
                                err.printStackTrace();
                                myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                                File dst = new File(myDir, "adobe_reader.apk");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        }
                    }
                }
////MP3
            } else if (openwith.equalsIgnoreCase("MP3")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            context.startActivity(intent);
                        }
                    }
                }
///// VIDEO or Video Book Player
            } else if (openwith.equalsIgnoreCase("Flow Video Player") || (openwith.equalsIgnoreCase("Native Video"))) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            context.startActivity(intent);
                        }
                    }
                }
            }
///// HTML
            else if (openwith.equalsIgnoreCase("HTML")) {
               /* indexFilePath = null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(resourceIdTobeOpened, (String) attmentNames.get(cnt));
                    }
                    if (indexFilePath != null) {
                        openHTML(indexFilePath);
                    }
                } else {
                    openImage(resourceIdTobeOpened, (String) attmentNames.get(0), getExtension(attmentNames.get(0)));
                }*/
//// PDF
            } else if (openwith.equalsIgnoreCase("Just download")) {
                //// Todo work to get just download
            }
             else if (openwith.equalsIgnoreCase("BeLL Video Book Player")) {
             }
            sendResourceRatingFeedback(openedResourceId);
        } catch (Exception Er) {
            Log.d("MyCouch", "Opening resource error " + Er.getMessage());
        }
        return true;
    }



    ////////// Rating
    public void sendResourceRatingFeedback(String id){
        try {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(id);
            String openResName = (String) res_doc.getProperty("title");
            openedResourceId = id;
            openedResourceTitle = openResName;
            mListener.onResourceOpened(openedResourceId,openedResourceTitle);
        }catch(Exception err){
            Log.d("MyCouch", "Sending feedback on resource error " + err.getMessage());
        }
    }


}