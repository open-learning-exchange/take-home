package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 17/05/16.
 */
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.couchbase.lite.Attachment;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;

import net.sf.andpdf.pdfviewer.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TabFragment2 extends Fragment {

        ListView list;
        public  TabFragment2 CustomListView = null;
        public ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<ListModel>();

        public static final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings;
        CouchViews chViews = new CouchViews();
        Context context = null;

        String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
                sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
                sys_usergender, sys_uservisits= "";
        Object[] sys_membersWithResource;
        int resourceNo=0;

        String resourceIdList[];
        String courseIdList[];
        int rsLstCnt=0;
        int csLstCnt=0;

        ImageView[] imageView;
        static Uri videoURl;
        static Intent intent;
        ///////////////////////////

        // Log tag
        private static final String TAG = TabFragment2.class.getSimpleName();

        private List<Resource> resourceList = new ArrayList<Resource>();
        private ListView listView;
        private CustomListAdapter adapter;

        AssetManager assetManager;
        AssetFileDescriptor afd;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            context = container.getContext();

            CustomListView = this;
            assetManager = getActivity().getAssets();

            settings = context.getSharedPreferences(PREFS_NAME, 0);
            sys_username = settings.getString("pf_username","");
            sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
            sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
            sys_password = settings.getString("pf_password","");
            sys_usercouchId = settings.getString("pf_usercouchId","");
            sys_userfirstname = settings.getString("pf_userfirstname","");
            sys_userlastname = settings.getString("pf_userlastname","");
            sys_usergender = settings.getString("pf_usergender","");
            sys_uservisits = settings.getString("pf_uservisits","");


            Set<String> mwr = settings.getStringSet("membersWithResource",null);
            try{
                sys_membersWithResource = mwr.toArray();
                Log.e("MYAPP", " membersWithResource  = "+sys_membersWithResource.length);

            }catch(Exception err){
                Log.e("MYAPP", " Error creating  sys_membersWithResource");
            }

            resourceList.clear();
            LoadCourseList();

            View rootView = inflater.inflate(R.layout.tab_fragment_2, container, false);

            listView = (ListView) rootView.findViewById(R.id.list);
            adapter = new CustomListAdapter(this.getActivity(), resourceList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    //openDoc(resourceIdList[position]);
                }
            });

            return rootView;

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

        public void LoadCourseList() {

            String memberId = sys_usercouchId;
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = null;
            try {
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                Database db = manager.getExistingDatabase("membercourseprogress");
                Database resource_Db = manager.getExistingDatabase("resources");
                Database coursestep_Db = manager.getExistingDatabase("coursestep");
                Query orderedQuery = chViews.ReadCourseList(db).createQuery();
                orderedQuery.setDescending(true);

                //orderedQuery.setStartKey(memberId);
                //orderedQuery.setLimit(0);
                QueryEnumerator results = orderedQuery.run();
                courseIdList = new String[results.getCount()];
                csLstCnt = 0;
                Map<String, Object> course_properties = null;

                for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                    Log.d("tag2", "HEREVEE ");

                    QueryRow row = it.next();
                    String docId = (String) row.getValue();
                    Document doc = db.getExistingDocument(docId);
                    Map<String, Object> membercourseProperties = doc.getProperties();
                    if(memberId.equals((String) membercourseProperties.get("memberId"))) {
                        ArrayList arr_courseStepsIds = new ArrayList();
                        ArrayList obj_courseResult = new ArrayList();
                        ArrayList obj_courseStatus = new ArrayList();
                        Log.e("tag2", "Course ID "+ (String) membercourseProperties.get("courseId"));
                        arr_courseStepsIds = (ArrayList) membercourseProperties.get("stepsIds");
                        obj_courseResult = (ArrayList) membercourseProperties.get("stepsResult");
                        obj_courseStatus = (ArrayList) membercourseProperties.get("stepsStatus");

                        ///String value = arr_courseStepsIds.toString();
                        //for (int cnt=0;cnt<)
                        Log.d("tag2", "Value is: "+arr_courseStepsIds.size());



                       /* String mycourseDocId= "";
                        String mycourseId = "";
                        String mykind = "";
                        String courseIdList[] = new String[properties.get("stepsIds").length()];
                        String mystepsIds[] = new String[];
                        String mystepsResult[] = new String[];
                        String mystepsStatus [] = new String[];
                        try {
                            Document coursestep_doc = coursestep_Db.getExistingDocument((String) properties.get("courseId"));
                            Log.e("tag", "Course ID "+ (String) properties.get("courseId"));
                            try {
                                course_properties = coursestep_doc.getProperties();
                            }catch(Exception errs){
                                Log.e("tag", "OBJECT ERROR "+ errs.toString());
                            }
                            //myresTitile = (String) course_properties.get("title")+"";
                            //myresId = (String) properties.get("resourceId")+"";
                            //myresDec = (String) course_properties.get("author")+"";
                            //myresType = (String) course_properties.get("averageRating")+"";
                            //myresExt = (String) course_properties.get("openWith")+"";
                            ///resourceIdList[rsLstCnt]=myresId;
                            csLstCnt++;
                        }catch(Exception err){

                            Log.e("tag", "ERROR "+ err.getMessage());
                            mycourseDocId = "Unknown CourseId .. ";
                            mycourseId = "";
                            mykind = "";
                            csLstCnt++;

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
*/
                    }


                }

                ///adapter.notifyDataSetChanged();

                ///Log.d("PreExceute","Items "+ db.getDocumentCount());

                db.close();

            } catch (Exception e) {
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
                case "":
                    img = R.drawable.web;
                    break;
                default:
                    img = R.drawable.web;
                    break;
            }
            return img;
        }
    }