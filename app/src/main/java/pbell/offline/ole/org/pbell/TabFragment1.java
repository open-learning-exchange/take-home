package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 17/05/16.
 */
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.andpdf.pdfviewer.PdfViewerActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TabFragment1 extends Fragment {

    ListView list;
    public  TabFragment1 CustomListView = null;
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
    int rsLstCnt=0;

    ImageView[] imageView;
    ///////////////////////////

    // Log tag
    private static final String TAG = TabFragment1.class.getSimpleName();

    // Movies json url
    private static final String url = "http://api.androidhive.info/json/movies.json";
    private ProgressDialog pDialog;
    private List<Resource> resourceList = new ArrayList<Resource>();
    private ListView listView;
    private CustomListAdapter adapter;

    AssetManager assetManager;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();

        CustomListView = this;
        assetManager = getActivity().getAssets();

        //Just Download
        //HTML
        //PDF.js
        //Bell-Reader
        //MP3
        //Flow Video Player
        //BeLL Video Book Player
        //Native Video
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


        Set<String>  mwr = settings.getStringSet("membersWithResource",null);
        try{
            sys_membersWithResource = mwr.toArray();
            Log.e("MYAPP", " membersWithResource  = "+sys_membersWithResource.length);

        }catch(Exception err){
            Log.e("MYAPP", " Error creating  sys_membersWithResource");
        }


        ///loadUserDetails();
        ///if (!resourceList.isEmpty()) {
            resourceList.clear();
            LoadShelfResourceList();
        //}

        //setListData();

        View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);

        listView = (ListView) rootView.findViewById(R.id.list);
        adapter = new CustomListAdapter(this.getActivity(), resourceList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    openDoc(resourceIdList[position]);
               }
        });

        //pDialog = new ProgressDialog(context);
        // Showing progress dialog before making http request
        //pDialog.setMessage("Loading...");
        //pDialog.show();

        // changing action bar color
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1b1b1b")));


        /*
        // Creating volley request obj
        JsonArrayRequest movieReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        hidePDialog();

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Movie movie = new Movie();
                                movie.setTitle(obj.getString("title"));
                                movie.setThumbnailUrl(obj.getString("image"));
                                movie.setRating(((Number) obj.get("rating")).doubleValue());
                                movie.setYear(obj.getInt("releaseYear"));

                                // Genre is json array
                                JSONArray genreArry = obj.getJSONArray("genre");
                                ArrayList<String> genre = new ArrayList<String>();
                                for (int j = 0; j < genreArry.length(); j++) {
                                    genre.add((String) genreArry.get(j));
                                }
                                movie.setGenre(genre);

                                // adding movie to movies array
                                movieList.add(movie);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

            }
        });


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(resourceList);
        */



        return rootView;

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }

    public void openDoc(String docId) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            Log.e("MYAPP", " membersWithID  = " + docId);

            List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
            if (attmentNames.size() > 0) {
                for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                    Log.e("MYAPP", " membersWithResource  = " + getExtension(attmentNames.get(cnt)));
                    switch (getExtension(attmentNames.get(cnt))){
                        case "mp3":
                            openAudioVideo(docId,(String) attmentNames.get(cnt),getExtension(attmentNames.get(cnt)));
                            break;
                        case "mov":
                            openAudioVideo(docId,(String) attmentNames.get(cnt),getExtension(attmentNames.get(cnt)));
                            break;
                        case "mp4":
                            openAudioVideo(docId,(String) attmentNames.get(cnt),getExtension(attmentNames.get(cnt)));
                           break;
                        case "pdf":
                            openPDF(docId,(String) attmentNames.get(cnt),getExtension(attmentNames.get(cnt)));
                            break;
                        default:
                            Toast.makeText(getContext(), getExtension(attmentNames.get(cnt)) + " File type not supported yet ", Toast.LENGTH_LONG).show();
                            break;
                    }
                }

            }
        } catch (Exception Er) {

        }
    }

    public void openPDF(String docId,String fileName, String player) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);

            InputStream in = null;
            OutputStream out = null;
            ////File file = new File(getActivity().getFilesDir(), "ABC.pdf");
            try {
                //in = assetManager.open("ABC.pdf");
                out = getActivity().openFileOutput(fileName, Context.MODE_WORLD_READABLE);
                copyFile(fileAttachment.getContent(), out);
                //in.close();
                //in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }

        } catch (Exception Er) {

        }
        Intent intent = new Intent(getActivity(), MyPdfViewerActivity.class);
        intent.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, getActivity().getFilesDir() + "/"+fileName);
        startActivity(intent);
    }

    public void openVideo(String docId,String fileName, String player) {
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
            Log.e("tag", src.getPath()+" S " +src.length());
            Log.e("tag", dst.getCanonicalPath()+" D " +dst.length());

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            //String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(dst).toString());
            //String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            ///intent.setDataAndType(Uri.fromFile(dst),mimetype);
            //startActivity(intent);

            ///Log.e("tag", fileAttachment.getContentURL().getPath());


            Intent myIntent = new Intent(getActivity(),VideoViewActivity.class);
            myIntent.putExtra("VIDEO_URL",Uri.fromFile(src).toString());

            Log.e("tag", Uri.fromFile(dst).toString());
            startActivity(myIntent);

        } catch (Exception Er) {

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
            Log.e("tag", src.getPath()+" S " +src.length());
            Log.e("tag", dst.getCanonicalPath()+" D " +dst.length());

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(dst).toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            ///intent.setDataAndType(Uri.fromFile(dst).toString(),mimetype);
            intent.setDataAndType(Uri.fromFile(dst),mimetype);
            startActivity(intent);

            Log.e("tag", fileAttachment.getContentURL().getPath());

        } catch (Exception Er) {
            Log.e("tag", Er.getMessage());
        }
    }

    boolean deleteDirectory(File path) {
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

    ///Intent intent = new Intent(Intent.ACTION_VIEW);
	    ///intent.setDataAndType( Uri.parse("file://" + getFilesDir() + "/ABC.pdf"),"application/pdf");


    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
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







    public void setListData() {
        for (int i = 0; i < 8; i++) {

            final ListModel sched = new ListModel();

            /******* Firstly take data in model object ******/
            sched.setTitle("Resource Title : "+i);
            ///sched.setImage("image"+i);
            sched.setImage("image"+(i+1)+"");
            sched.setDescription("Resource Type : "+i+"");

            /******** Take Model Object in ArrayList **********/
            CustomListViewValuesArr.add( sched );
        }

    }

    /*****************  This function used by adapter ****************/
    /*public void onItemClick(int mPosition) {
        ListModel tempValues = ( ListModel ) CustomListViewValuesArr.get(mPosition);
        // SHOW ALERT
       // Toast.makeText(CustomListView,""+tempValues.getCompanyName()+" Image:"+tempValues.getImage() +" Url:"+tempValues.getUrl(), Toast.LENGTH_LONG).show();
    }*/

    public void LoadShelfResourceList() {
        String memberId = sys_usercouchId;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("shelf");
            Database resource_Db = manager.getExistingDatabase("resources");
            Query orderedQuery = chViews.ReadShelfByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            //orderedQuery.setStartKey(memberId);
            //orderedQuery.setLimit(0);
            QueryEnumerator results = orderedQuery.run();
            resourceIdList = new String[results.getCount()];
            rsLstCnt = 0;
            Map<String, Object> resource_properties = null;
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                if(memberId.equals((String) properties.get("memberId"))) {
                    String myresTitile = "";
                    String myresId = "";
                    String myresType = "";
                    String myresDec = "";
                    try {
                        Document resource_doc = resource_Db.getExistingDocument((String) properties.get("resourceId"));
                        Log.e("tag", "RES ID "+ (String) properties.get("resourceId"));
                        try {
                            resource_properties = resource_doc.getProperties();
                        }catch(Exception errs){
                            Log.e("tag", "OBJECT ERROR "+ errs.toString());
                        }
                        myresTitile = (String) resource_properties.get("title")+"";
                        myresId = (String) properties.get("resourceId")+"";
                        myresDec = (String) resource_properties.get("author")+"";
                        myresType = (String) resource_properties.get("averageRating")+"";
                        resourceIdList[rsLstCnt]=myresId;
                        rsLstCnt++;
                    }catch(Exception err){


                        Log.e("tag", "ERROR "+ err.getMessage());
                        myresTitile = "Unknown resource .. ";
                        myresId = "";
                        myresDec = "Not yet downloaded.. Please sync";
                        myresType = "";
                        rsLstCnt++;

                    }
                    ListModel sched = new ListModel();
                    ///Log.e("MYAPP", "Resource Here = "+(String) properties.get("resourceId"));
                    //sched.setTitle(myresTitile);
                    //sched.setImage("image1");
                    //sched.setDescription(myresDec);
                    //CustomListViewValuesArr.add( sched );
                    Resource resource = new Resource();
                    resource.setTitle(myresTitile);
                    resource.setThumbnailUrl(null);
                    resource.setDescription(myresDec);
                    resource.setRating(myresType);

                    // Genre is json array
                    /*JSONArray genreArry = obj.getJSONArray("genre");
                    ArrayList<String> genre = new ArrayList<String>();
                    for (int j = 0; j < genreArry.length(); j++) {
                        genre.add((String) genreArry.get(j));
                    }*/
                    resource.setGenre(null);

                    // adding movie to movies array
                    resourceList.add(resource);
                    resourceNo++;
                }
            }
            ///adapter.notifyDataSetChanged();

            ///Log.d("PreExceute","Items "+ db.getDocumentCount());

            db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
