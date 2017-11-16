package org.ole.learning.planet.planetlearning;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by leonardmensah on 17/06/2017.
 */

public class LogHouse {
    String userId, userPlanetId, userName, userGender, userVisitSessionNumber, userDateOfBirth, userEmail, userNation, userPhone, userRegion, userServerAddress, userServerName, userServerVersion, userServersParent;
    String deviceLastSyncedWithParent, deviceParentName, deviceMacAddress, deviceDate, deviceManufacturer, deviceModel, deviceAndroidVersionRelease;
    ArrayList<String> userRoles = new ArrayList<>();
    long deviceFreeStorage, deviceTotalStorage;
    int deviceAndroidVersion;
    CouchViews chViews = new CouchViews();
    SharedPreferences settings;
    public static final String PREFS_NAME = "MyPrefsFile";


    public void basicLogInfo(Context context) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("members");
            Document doc = db.getExistingDocument(userId);
            Map<String, Object> properties = doc.getProperties();
            userDateOfBirth = (String) properties.get("BirthDate");
            userEmail = (String) properties.get("email");
            userNation = (String) properties.get("nation");
            userPhone = (String) properties.get("phone");
            userRegion = (String) properties.get("region");
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /// Device MacAddress
        try {
            @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //This is for setting the MAC address if it is being run in a android emulator.
            String m_WLANMAC;
            m_WLANMAC = wm.getConnectionInfo().getMacAddress();
            if (m_WLANMAC == null) {
                m_WLANMAC = "mymac";
            }
            deviceMacAddress = m_WLANMAC;
        } catch (Exception err) {
            err.printStackTrace();
        }
        //Device Storage
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                deviceTotalStorage = ((long) statFs.getBlockCountLong() * (long) statFs.getBlockSizeLong()) / 1048576;
                deviceFreeStorage = ((long) statFs.getAvailableBlocksLong() * (long) statFs.getBlockSizeLong()) / 1048576;
            } else {
                deviceTotalStorage = ((long) statFs.getBlockCount() * (long) statFs.getBlockSize()) / 1048576;
                deviceFreeStorage = ((long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize()) / 1048576;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        // Device Date
        try {
            SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
            Date todayDate = new Date();
            deviceDate = currentDate.format(todayDate);
        } catch (Exception err) {
            err.printStackTrace();
        }
        // Device Manufacturer, Model SDk and Version Release
        try {
            deviceManufacturer = Build.MANUFACTURER;
            deviceModel = Build.MODEL;
            deviceAndroidVersion = Build.VERSION.SDK_INT;
            deviceAndroidVersionRelease = Build.VERSION.RELEASE;
        } catch (Exception err) {
            err.printStackTrace();
        }


    }

    public void restorePreferences(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        userPlanetId = settings.getString("pf_username", "");
        userServerAddress = settings.getString("pf_sysncUrl", "http://");
        userId = settings.getString("pf_usercouchId", "");
        userName = settings.getString("pf_userfirstname", "") + " " + settings.getString("pf_userlastname", "");
        userGender = settings.getString("pf_usergender", "");
        userVisitSessionNumber = settings.getString("pf_uservisits", "");
        userServerName = settings.getString("pf_server_name", " ");
        userServerVersion = settings.getString("pf_server_version", " ");
        userServersParent = settings.getString("pf_server_nation", " ");
        deviceParentName = settings.getString("pf_server_nation", " ");
        deviceLastSyncedWithParent = settings.getString("pf_lastSyncDate", "");
        basicLogInfo(context);
    }

    public boolean updateActivityOpenedResources(Context context, String userId, String resourceid, String resource_name) {
        restorePreferences(context);
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        Database activityLog;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activityLog = manager.getDatabase("activitylog");
            Document retrievedDocument = activityLog.getDocument(deviceMacAddress);
            Map<String, Object> properties = retrievedDocument.getProperties();
            if ((ArrayList<String>) properties.get("female_opened") != null) {
                try {
                    ArrayList female_opened = (ArrayList<String>) properties.get("female_opened");
                    ArrayList male_opened = (ArrayList<String>) properties.get("male_opened");
                    ArrayList resources_names = (ArrayList<String>) properties.get("resources_names");
                    ArrayList resources_opened = (ArrayList<String>) properties.get("resources_opened");
                    Log.e("MyCouch", "Option 1 " + userGender.toLowerCase());
                    if (userGender.equalsIgnoreCase("female")) {
                        female_opened.add(1);
                        male_opened.add(0);
                    } else {
                        female_opened.add(0);
                        male_opened.add(1);
                    }
                    resources_names.add(resource_name);
                    resources_opened.add(resourceid);
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("female_opened", female_opened);
                    newProperties.put("male_opened", male_opened);
                    newProperties.put("resources_names", resources_names);
                    newProperties.put("resources_opened", resources_opened);
                    retrievedDocument.putProperties(newProperties);
                    Log.e("MyCouch", "Saved resource open in local Activity Log " + newProperties.toString());
                    return true;
                } catch (Exception err) {
                    Log.e("MyCouch", "Option 1 Failed " + err.getMessage());
                    return false;
                }
            } else {
                try {
                    Log.e("MyCouch", "Option 2 gender is " + userGender.toLowerCase());
                    Document newdocument = activityLog.getDocument(deviceMacAddress);
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    ArrayList female_opened = new ArrayList<>();
                    ArrayList male_opened = new ArrayList<>();
                    ArrayList resources_names = new ArrayList<>();
                    ArrayList resources_opened = new ArrayList<>();
                    if (userGender.equalsIgnoreCase("female")) {
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
                    Log.e("MyCouch", "Saved resource open in local Activity Log " + newProperties.toString());
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

    public void saveRating(Context context, int rate, String comment, String resourceId) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        Database resourceRating;
        int doc_rating;
        int doc_timesRated;
        ArrayList<String> commentList = new ArrayList<>();
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
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("sum", (doc_rating + rate));
                    newProperties.put("timesRated", doc_timesRated + 1);
                    newProperties.put("comments", commentList);
                    retrievedDocument.putProperties(newProperties);
                    updateActivityRatingResources(context, rate, resourceId);
                    Toast.makeText(context, String.valueOf(rate), Toast.LENGTH_SHORT).show();
                }
            } else {
                Document newdocument = resourceRating.getDocument(resourceId);
                Map<String, Object> newProperties = new HashMap<>();
                newProperties.put("sum", rate);
                newProperties.put("timesRated", 1);
                commentList.add(comment);
                newProperties.put("comments", commentList);
                newdocument.putProperties(newProperties);
                /// todo check updating resource to see it works
                updateActivityRatingResources(context, rate, resourceId);
                Toast.makeText(context, String.valueOf(rate), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception err) {
            Log.e("MyCouch", "ERR : " + err.getMessage());
        }
    }


    public boolean updateActivityRatingResources(Context context, float rate, String resourceid) {
        restorePreferences(context);
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        Database activityLog;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            activityLog = manager.getDatabase("activitylog");
            Document retrievedDocument = activityLog.getDocument(deviceMacAddress);
            if (retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                try {
                    ArrayList female_rating = (ArrayList<String>) properties.get("female_rating");
                    ArrayList female_timesRated = (ArrayList<String>) properties.get("female_timesRated");
                    ArrayList male_rating = (ArrayList<String>) properties.get("male_rating");
                    ArrayList male_timesRated = (ArrayList<String>) properties.get("male_timesRated");
                    ArrayList resourcesIds = (ArrayList<String>) properties.get("resourcesIds");
                    Log.e("MyCouch", "Option Rating 1");
                    if (userGender.equalsIgnoreCase("female")) {
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
                    Map<String, Object> newProperties = new HashMap<>();
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
                        Map<String, Object> newProperties = new HashMap<>();
                        newProperties.putAll(retrievedDocument.getProperties());
                        ArrayList female_rating = new ArrayList<>();
                        ArrayList female_timesRated = new ArrayList<>();
                        ArrayList male_rating = new ArrayList<>();
                        ArrayList male_timesRated = new ArrayList<>();
                        ArrayList resourcesIds = new ArrayList<>();
                        if (userGender.equalsIgnoreCase("female")) {
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
                    Map<String, Object> newProperties = new HashMap<>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    ArrayList female_rating = new ArrayList<>();
                    ArrayList female_timesRated = new ArrayList<>();
                    ArrayList male_rating = new ArrayList<>();
                    ArrayList male_timesRated = new ArrayList<>();
                    ArrayList resourcesIds = new ArrayList<>();
                    if (userGender.equalsIgnoreCase("female")) {
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


}
