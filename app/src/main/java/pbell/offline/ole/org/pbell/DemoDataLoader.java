package pbell.offline.ole.org.pbell;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leonardmensah on 8/2/17.
 */

public class DemoDataLoader {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;
    private static final String TAG = "MYAPP";
    Context context;
    private ProgressDialog mDialog;
    private long enqueue;
    private DownloadManager downloadManager;
    boolean singleFileDownload = true;
    public static final String PREFS_NAME = "MyPrefsFile";
    CouchViews chViews = new CouchViews();

    /// String
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    Boolean sys_demoMode;
    AndroidContext androidContext;

    String OneByOneResID, OneByOneResTitle, OneByOneCourseId;
    int courseStepsCounter =0;
    int action_button_id = 0;
    SharedPreferences settings;
    List<String> resIDArrayList = new ArrayList<>();
    View vi;

    TextView title,description,ratingAvgNum,totalNum;
    Button open;
    RatingBar ratingStars;
    LayerDrawable stars;
    ProgressBar femalerating, malerating;
    String activityName ="myCourses";


    protected int _splashTime = 5000;
    private Thread splashTread;

    private long enqueues;
    private DownloadManager dm;
    Cursor c;
    LogHouse logHouse = new LogHouse();
    private ListViewAdapter_myCourses.OnCourseListListener mListener;

    String[] databaseList = {"members", "meetups", "usermeetups", "assignments",
            "assignmentpaper","courseanswer","coursequestion","courses","courseschedule","coursestep","membercourseprogress",
            "calendar", "groups", "invitations", "configurations", "requests", "shelf", "languages"};

    public DemoDataLoader(Context context){
        this.context = context;
        androidContext = new AndroidContext(this.context);

        /// Start
        new startHere().execute();

    }

    public void restorePreferences() {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username", "learner");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl", "http://demo:oleoleole@demo.ole.org:5995");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate", "");
        sys_password = settings.getString("pf_password", "learner");
        sys_usercouchId = settings.getString("pf_usercouchId", "");
        sys_userfirstname = settings.getString("pf_userfirstname", "Demo");
        sys_userlastname = settings.getString("pf_userlastname", "Learner");
        sys_usergender = settings.getString("pf_usergender", "Female");
        sys_uservisits = settings.getString("pf_uservisits", "5");
        sys_servername = settings.getString("pf_server_name", "demo");
        sys_serverversion = settings.getString("pf_server_version", " ");
        sys_serverversion = settings.getString("pf_server_code", "demo");
        sys_demoMode = settings.getBoolean("sys_demoMode",true);

        sys_oldSyncServerURL = "http://demo:oleoleole@demo.ole.org:5995";
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("pf_sysncUrl", sys_oldSyncServerURL);
        editor.commit();
    }
    private class startHere extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            emptyAllDbs();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            ResourcesJson rsj = new ResourcesJson();
            createConfigurations();
            restorePreferences();
            createMembers();
            createShelf();
            createCourses();
            createCoursestep();
            createMembercourseprogress();
            if(rsj.ResourcesJson(sys_oldSyncServerURL,"resources",androidContext)){

            }

        }
    }

    public void emptyAllDbs() {
        for (int cnt = 0; cnt < databaseList.length; cnt++) {
                try {
                    Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                    Database db = manager.getDatabase(databaseList[cnt]);
                    Log.e("MYAPP","Deleting "+databaseList[cnt]);
                    db.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //// Delete Device Created Databases
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbResources = manager.getDatabase("resources");
                dbResources.delete();
            }catch(Exception err){
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbResources = manager.getDatabase("shadowresources");
                dbResources.delete();
            }catch(Exception err){
                err.printStackTrace();
            }
            try {
                Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database dbOffline_courses = manager.getDatabase("offline_courses");
                dbOffline_courses.delete();
            }catch(Exception err){
                err.printStackTrace();
            }

            try{
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                String[] flist = myDir.list();
                for (int i = 0; i < flist.length; i++) {
                    System.out.println(" " + myDir.getAbsolutePath());
                    File temp = new File(myDir.getAbsolutePath() + "/" + flist[i]);
                    if (temp.isDirectory()) {
                        Log.d("Delete ", " Deleting " + temp.getName());
                        temp.delete();
                    } else {
                        temp.delete();
                    }
                }
            }catch (Exception err){
                Log.e("MYAPP", " Deleting materials from ole_temp directory ");
            }

    }

    public void createConfigurations(){
        Manager manager = null;
        Database configuration;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            configuration = manager.getDatabase("configurations");
            Document newdocument = configuration.getDocument("e3d7ca503eef922213e36bf3d6001389");
            Map<String, Object> newProperties = new HashMap<String, Object>();
            //newProperties.put("_rev", "1-51967df030f45163a9a5dc53dbfdbdca");
            newProperties.put("name", "demo");
            newProperties.put("nationName", "earthbell");
            newProperties.put("code", "demo");
            newProperties.put("type", "nation");
            newProperties.put("nationUrl", "earthbell.ole.org:5989");
            newProperties.put("version", "0.13.5");
            newProperties.put("notes", "Nation Bell");
            newProperties.put("currentLanguage", "English");
            newProperties.put("register", "nbs.ole.org:5997");
            newdocument.putProperties(newProperties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createMembers(){
        Manager manager = null;
        Database members;
        ArrayList<String> rolesList = new ArrayList<String>();
        Map<String, String> credentialMap = new HashMap<String, String>();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            members = manager.getDatabase("members");
            Document newdocument = members.getDocument("e3d7ca503eef922213e36bf3d60080c2");
            Map<String, Object> newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Member");
            newProperties.put("bellLanguage", "English");
            newProperties.put("firstName", "Demo");
            newProperties.put("lastName", "Learner");
            newProperties.put("password", "");
            newProperties.put("phone", "234562345");
            newProperties.put("kind", "Member");
            newProperties.put("language","English");
            newProperties.put("BirthDate", "1988-04-20T00:00:00.000Z");
            newProperties.put("visits", 5);
            newProperties.put("Gender", "Female");
            newProperties.put("levels", "Higher");
            newProperties.put("community", "demo");
            newProperties.put("nation", "earthbell");
            newProperties.put("login","learner");
            rolesList.add("Learner");
            rolesList.add("Leader");
            newProperties.put("roles", rolesList);
            credentialMap.put("salt","c0a24b98e089b6b0f5d3674430cebe0c");
            credentialMap.put("value","53580b4213093aa7bda95c597d3eddfd7d5005d5");
            credentialMap.put("login","learner");
            newProperties.put("credentials", credentialMap);
            newdocument.putProperties(newProperties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createShelf(){
        Manager manager = null;
        Database shelf;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            shelf = manager.getDatabase("shelf");
            Document newdocument1 = shelf.getDocument("e3d7ca503eef922213e36bf3d600d0ea");
            Map<String, Object> newProperties1 = new HashMap<String, Object>();
            newProperties1.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties1.put("resourceId", "d3f3756c79722902f983267053025c21");
            newProperties1.put("resourceTitle", "الأميرة القاسِيَة");
            newdocument1.putProperties(newProperties1);

            Document newdocument2 = shelf.getDocument("e3d7ca503eef922213e36bf3d6017dd7");
            Map<String, Object> newProperties2 = new HashMap<String, Object>();
            newProperties2.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties2.put("resourceId", "3a162fea1d56beb7a4441a4a8018e5b0");
            newProperties2.put("resourceTitle", "Mental Health Strategies for Refugee Children");
            newdocument2.putProperties(newProperties2);

            Document newdocument3 = shelf.getDocument("e3d7ca503eef922213e36bf3d6018b07");
            Map<String, Object> newProperties3 = new HashMap<String, Object>();
            newProperties3.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties3.put("resourceId", "61461c3cf80877891652a143c3c77fed");
            newProperties3.put("resourceTitle", "Education in Emergency");
            newdocument3.putProperties(newProperties3);

            Document newdocument4 = shelf.getDocument("e3d7ca503eef922213e36bf3d6019428");
            Map<String, Object> newProperties4 = new HashMap<String, Object>();
            newProperties4.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties4.put("resourceId", "a28c81eac20e23529a5eda8e850eaeb3");
            newProperties4.put("resourceTitle", "Emergency Preparedness Plans");
            newdocument4.putProperties(newProperties4);


            Document newdocument5 = shelf.getDocument("e3d7ca503eef922213e36bf3d601a08a");
            Map<String, Object> newProperties5 = new HashMap<String, Object>();
            newProperties5.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties5.put("resourceId", "68ec3d6c83989920cb75df8e5d01c449");
            newProperties5.put("resourceTitle", "Healthy Child Uganda VHT Training Manual: Paring a Community Emergency Transport Plan (CETP)");
            newdocument5.putProperties(newProperties5);

            Document newdocument6 = shelf.getDocument("e3d7ca503eef922213e36bf3d601a50a");
            Map<String, Object> newProperties6 = new HashMap<String, Object>();
            newProperties6.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties6.put("resourceId", "86ba8cb7d819aa7068748640fb347ab1");
            newProperties6.put("resourceTitle", "Children in War: The role of Child to Child Activities in the Therapy and Care of Displaced Unaccompanied Children");
            newdocument6.putProperties(newProperties6);


            Document newdocument7 = shelf.getDocument("e3d7ca503eef922213e36bf3d601aac0");
            Map<String, Object> newProperties7 = new HashMap<String, Object>();
            newProperties7.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties7.put("resourceId", "326b61c2ae2be1b31cb65891cbbf1536");
            newProperties7.put("resourceTitle", "Because I Am a Girl");
            newdocument7.putProperties(newProperties7);


            Document newdocument8 = shelf.getDocument("e3d7ca503eef922213e36bf3d601ba3d");
            Map<String, Object> newProperties8 = new HashMap<String, Object>();
            newProperties8.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties8.put("resourceId", "78c923afd67a7dc59b3f68b03b63bb17");
            newProperties8.put("resourceTitle", "Because I Am a Girl: A Year of Action and Innovation / Parce que je suis une fille: Une annee d’action et d’innovation (French)");
            newdocument8.putProperties(newProperties8);


            Document newdocument9 = shelf.getDocument("e3d7ca503eef922213e36bf3d601c141");
            Map<String, Object> newProperties9 = new HashMap<String, Object>();
            newProperties9.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties9.put("resourceId", "242a13d826420075a3ece5f0ea31b1fb");
            newProperties9.put("resourceTitle", "Gender Awareness Workshop (en)");
            newdocument9.putProperties(newProperties9);


            Document newdocument10 = shelf.getDocument("e3d7ca503eef922213e36bf3d601c3c5");
            Map<String, Object> newProperties10 = new HashMap<String, Object>();
            newProperties10.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            newProperties10.put("resourceId", "acbdd7ae8325d8ff27fed560779f2fd1");
            newProperties10.put("resourceTitle", "To Educate a Girl - Lesson Plan (Grade 9-12)");
            newdocument10.putProperties(newProperties10);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void createMeetups(){
        /*AndroidContext androidContext = new AndroidContext(context);
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
            }
        } catch (Exception err) {
            Log.e("MyCouch", "ERR : " + err.getMessage());
        }*/
    }
    public void createUsermeetups(){

    }
    public void createAssignments(){

    }
    public void createAssignmentpaper(){

    }
    public void createCourseanswer(){

    }
    public void createCoursequestion(){

    }
    public void createCourses(){
        Manager manager = null;
        Database course;
        ArrayList<String> courseLeaderList = new ArrayList<String>();
        ArrayList<String> DayList = new ArrayList<String>();
        ArrayList<String> membersList = new ArrayList<String>();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            course = manager.getDatabase("courses");
            Document newdocument = course.getDocument("e3d7ca503eef922213e36bf3d600eb0c");
            Map<String, Object> newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course");
            newProperties.put("languageOfInstruction", "English");
            newProperties.put("CourseTitle", "Kangaroo Mother Care");
            newProperties.put("memberLimit", "");
            courseLeaderList.add("e3d7ca503eef922213e36bf3d60004b7");
            newProperties.put("courseLeader", courseLeaderList);
            newProperties.put("description", "This course covers basics on Kangaroo Mother Care");
            newProperties.put("method", "");
            newProperties.put("gradeLevel", "Post-Graduate");
            newProperties.put("subjectLevel", "Beginner");
            newProperties.put("startDate", "08/01/2017");
            newProperties.put("endDate", "12/31/2017");
            newProperties.put("frequency", "Daily");
            newProperties.put("Day", DayList);
            newProperties.put("startTime", "");
            newProperties.put("endTime", "");
            newProperties.put("location", "");
            newProperties.put("backgroundColor", "");
            newProperties.put("foregroundColor", "");
            membersList.add("e3d7ca503eef922213e36bf3d60080c2");
            membersList.add("e3d7ca503eef922213e36bf3d60004b7");
            newProperties.put("members", membersList);
            newProperties.put("name", "Kangaroo Mother Care");
            newdocument.putProperties(newProperties);
        } catch (Exception e) {
            e.printStackTrace();
        }

       /* {
            "_id": "e3d7ca503eef922213e36bf3d600eb0c",
                "_rev": "1-6c382f914e468b760d7956202fc5d017",
                "kind": "Course",
                "languageOfInstruction": "English",
                "CourseTitle": "Kangaroo Mother Care",
                "memberLimit": "",
                "courseLeader": [
            "e3d7ca503eef922213e36bf3d60004b7"
   ],
            "description": "This course covers basics on Kangaroo Mother Care",
                "method": "",
                "gradeLevel": "Post-Graduate",
                "subjectLevel": "Beginner",
                "startDate": "08/01/2017",
                "endDate": "12/31/2017",
                "frequency": "Daily",
                "Day": [
   ],
            "startTime": "",
                "endTime": "",
                "location": "",
                "backgroundColor": "",
                "foregroundColor": "",
                "members": [
            "e3d7ca503eef922213e36bf3d60080c2",
                    "e3d7ca503eef922213e36bf3d60004b7"
   ],
            "name": "Kangaroo Mother Care"
        }*/

    }
    public void createCourseschedule(){

    }
    public void createCoursestep(){
        Manager manager = null;
        Database coursestep;
        ArrayList<String> resourceIdList = new ArrayList<String>();
        ArrayList<String> resourceTitlesList = new ArrayList<String>();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            coursestep = manager.getDatabase("coursestep");

            Document newdocument = coursestep.getDocument("e3d7ca503eef922213e36bf3d6010d84");
            Map<String, Object> newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "Introduction");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "This is Ekua. She was born a few weeks early and is very small. What can you do to improve Ekua’s chances?");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "1");
            newProperties.put("resourceId",resourceIdList );
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument.putProperties(newProperties);

            Document newdocument2 = coursestep.getDocument("e3d7ca503eef922213e36bf3d6011162");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "Objectives");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "In this learning object you will practice identifying babies that need kangaroo mother care and describe components of kangaroo mother care.\\n\\nThis lesson supports Physiology and Management of the High Risk Neonate in the Nursing Midwifery course.");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "2");
            newProperties.put("resourceId",resourceIdList );
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument2.putProperties(newProperties);

            Document newdocument3 = coursestep.getDocument("e3d7ca503eef922213e36bf3d601124b");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "What is KMC?");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "Watch this video on kangaroo mother care from World Health Organization. The attached Kangaroo Mother Care job aid provides a summary of care.");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "3");
            resourceIdList.add("58a9ffe3ead5d4f48866867f02002dc6");
            newProperties.put("resourceId",resourceIdList );
            resourceTitlesList.add("KMC_WHO_What_is_KMC");
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument3.putProperties(newProperties);

            Document newdocument4 = coursestep.getDocument("e3d7ca503eef922213e36bf3d6011437");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "WHO KMC Job Aid");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "Review the Kangaroo Mother Care job aid for a summary of care.");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "4");
            resourceIdList.add("58a9ffe3ead5d4f48866867f0200385d");
            newProperties.put("resourceId",resourceIdList );
            resourceTitlesList.add("Kangaroo Mother Care job");
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument4.putProperties(newProperties);

            Document newdocument5 = coursestep.getDocument("e3d7ca503eef922213e36bf3d601232b");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "Low Birth Weight");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "Low birth weight (LBW) is defined as birth weight of less than 2500 grams.  There are three types of LBW babies: \\nPreterm: born before 37 completed weeks\\nVery preterm: born before 32 completed weeks\\nSmall for Gestational Age (SGA) or Small For Date (SFD): birth weight lower than expected for gestational age (may be term or preterm).\\nLBW infants may be born at any term.");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "5");
            newProperties.put("resourceId",resourceIdList );
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument5.putProperties(newProperties);

            Document newdocument6 = coursestep.getDocument("e3d7ca503eef922213e36bf3d6012a16");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "Review KMC Main Components");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "Remember the main components of KMC from the video and job aid? What are the three main components?");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "6");
            newProperties.put("resourceId",resourceIdList );
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument6.putProperties(newProperties);

            Document newdocument7 = coursestep.getDocument("e3d7ca503eef922213e36bf3d60137d8");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "What did you say were the main components?");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "What did you say were the main components? Your answer should have included the following: \\nskin-to-skin positioning of a baby on the mother’s chest;\\nadequate nutrition through breastfeeding;\\nambulatory care as a result of earlier discharge from hospital; and support for the mother and her family in caring for the baby.");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "7");
            newProperties.put("resourceId",resourceIdList );
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument7.putProperties(newProperties);

            Document newdocument8 = coursestep.getDocument("e3d7ca503eef922213e36bf3d6013ed5");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "Course Step");
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newProperties.put("totalMarks", "0");
            newProperties.put("stepType", "Objective");
            newProperties.put("title", "Time to Practice!");
            newProperties.put("stepMethod", "");
            newProperties.put("description", "Practice assessing a baby for gestational age and size in the simulation lab, use this checklist.\\n\\nNEED TO GET NEWBORN ASSESSMENT CHECKLIST FROM NMC");
            newProperties.put("stepGoals", "");
            newProperties.put("step", "8");
            newProperties.put("resourceId",resourceIdList );
            newProperties.put("resourceTitles", resourceTitlesList);
            newProperties.put("questionslist", null);
            newProperties.put("passingPercentage", "10");
            newdocument8.putProperties(newProperties);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void createMembercourseprogress(){
        Manager manager = null;
        Database membercourseprogress;
        ArrayList<String> stepsIdsList = new ArrayList<String>();
        ArrayList<String> stepsResultList = new ArrayList<String>();
        ArrayList<String> stepsStatusList = new ArrayList<String>();
        ArrayList<String> pqAttemptsList = new ArrayList<String>();
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            membercourseprogress = manager.getDatabase("membercourseprogress");

            Document newdocument = membercourseprogress.getDocument("e3d7ca503eef922213e36bf3d600faf6");
            Map<String, Object> newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "course-member-result");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6010d84");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6011162");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d601124b");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6011437");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d601232b");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6012a16");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d60137d8");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6013ed5");
            newProperties.put("stepsIds", stepsIdsList);
            newProperties.put("memberId", "e3d7ca503eef922213e36bf3d60004b7");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            newProperties.put("stepsResult", stepsResultList);
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            newProperties.put("stepsStatus", stepsStatusList);
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            newProperties.put("pqAttempts", pqAttemptsList);
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newdocument.putProperties(newProperties);


       /* {
            "_id": "e3d7ca503eef922213e36bf3d600faf6",
                "_rev": "9-496a84c07fb7c13678247490c80ba1b7",
                "kind": "course-member-result",
                "stepsIds": [
            "e3d7ca503eef922213e36bf3d6010d84",
                    "e3d7ca503eef922213e36bf3d6011162",
                    "e3d7ca503eef922213e36bf3d601124b",
                    "e3d7ca503eef922213e36bf3d6011437",
                    "e3d7ca503eef922213e36bf3d601232b",
                    "e3d7ca503eef922213e36bf3d6012a16",
                    "e3d7ca503eef922213e36bf3d60137d8",
                    "e3d7ca503eef922213e36bf3d6013ed5"
   ],
            "memberId": "e3d7ca503eef922213e36bf3d60004b7",
                "stepsResult": [
            "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
   ],
            "stepsStatus": [
            "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0"
   ],
            "pqAttempts": [
            0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
   ],
            "courseId": "e3d7ca503eef922213e36bf3d600eb0c"
        }

        ////

        {
            "_id": "e3d7ca503eef922213e36bf3d60101ef",
                "_rev": "9-5858aa5a132b086621836cde7e273a75",
                "kind": "course-member-result",
                "stepsIds": [
            "e3d7ca503eef922213e36bf3d6010d84",
                    "e3d7ca503eef922213e36bf3d6011162",
                    "e3d7ca503eef922213e36bf3d601124b",
                    "e3d7ca503eef922213e36bf3d6011437",
                    "e3d7ca503eef922213e36bf3d601232b",
                    "e3d7ca503eef922213e36bf3d6012a16",
                    "e3d7ca503eef922213e36bf3d60137d8",
                    "e3d7ca503eef922213e36bf3d6013ed5"
   ],
            "memberId": "e3d7ca503eef922213e36bf3d60080c2",
                "stepsResult": [
            "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
   ],
            "stepsStatus": [
            "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0"
   ],
            "pqAttempts": [
            0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
   ],
            "courseId": "e3d7ca503eef922213e36bf3d600eb0c"
        }
        */

            Document newdocument2 = membercourseprogress.getDocument("e3d7ca503eef922213e36bf3d60101ef");
            newProperties = new HashMap<String, Object>();
            newProperties.put("kind", "course-member-result");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6010d84");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6011162");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d601124b");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6011437");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d601232b");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6012a16");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d60137d8");
            stepsIdsList.add("e3d7ca503eef922213e36bf3d6013ed5");
            newProperties.put("stepsIds", stepsIdsList);
            newProperties.put("memberId", "e3d7ca503eef922213e36bf3d60080c2");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            stepsResultList.add("");
            newProperties.put("stepsResult", stepsResultList);
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            stepsStatusList.add("");
            newProperties.put("stepsStatus", stepsStatusList);
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            pqAttemptsList.add("");
            newProperties.put("pqAttempts", pqAttemptsList);
            newProperties.put("courseId", "e3d7ca503eef922213e36bf3d600eb0c");
            newdocument2.putProperties(newProperties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void createCalendar(){

    }
    public void createGroups(){

    }
    public void createInvitations(){

    }
    public void createRequests(){

    }

    public void createLanguages(){

    }

}

