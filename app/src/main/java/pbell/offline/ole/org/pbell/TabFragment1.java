package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 17/05/16.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import net.sf.andpdf.pdfviewer.PdfViewerActivity;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;

@SuppressWarnings("ALL")
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
    String resourceTitleList[];
    int rsLstCnt=0;

    ImageView[] imageView;
    static Uri videoURl;
    static Intent intent;

    ///////////////////////////
    private RatingBar ratingBar;
    private TextView txtRatingValue;
    private EditText txtComment;

    ///////////////////
    // Log tag
    private static final String TAG = TabFragment1.class.getSimpleName();

    // Movies json url
    private static final String url = "http://api.androidhive.info/json/movies.json";
    private ProgressDialog pDialog;
    private List<Resource> resourceList = new ArrayList<Resource>();
    private ListView listView;
    private CustomListAdapter adapter;

    AssetManager assetManager;
    AssetFileDescriptor afd;
    String indexFilePath;



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


        Set<String>  mwr = settings.getStringSet("membersWithResource",null);
        try{
            sys_membersWithResource = mwr.toArray();
            Log.e("MYAPP", " membersWithResource  = "+sys_membersWithResource.length);

        }catch(Exception err){
            Log.e("MYAPP", " Error creating  sys_membersWithResource");
        }


        resourceList.clear();
        LoadShelfResourceList();

        View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);

        listView = (ListView) rootView.findViewById(R.id.list);
        adapter = new CustomListAdapter(this.getActivity(), resourceList);
        try {
            adapter = new CustomListAdapter(this.getActivity(), resourceList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    // todo change rating style
              RateResourceDialog(resourceIdList[position],resourceTitleList[position]);
                    openDoc(resourceIdList[position]);
                }
            });
        }catch (Exception err){
            Log.e("adapter", " "+err);
        }

        //////copyAssets();
        copyAPK(R.raw.adobe_reader, "adobe_reader.apk");
        copyAPK(R.raw.firefox_49_0_multi_android, "firefox_49_0_multi_android.apk");
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1b1b1b")));
        return rootView;

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
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
        } ///


    }

    public void openDoc(String docId) {
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            String oppenwith = (String) res_doc.getProperty("openWith");
            Log.e("MYAPP", " membersWithID  = " + docId +"and Open with "+ oppenwith);
            List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
            if(oppenwith.equalsIgnoreCase("HTML")){
                /*if (attmentNames.size() <= 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openImage(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                    }
                }*/
                indexFilePath=null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(docId, (String) attmentNames.get(cnt));
                    }
                    if(indexFilePath!=null){
                        openHTML(indexFilePath);
                    }
                }else{
                    openImage(docId, (String) attmentNames.get(0), getExtension(attmentNames.get(0)));
                }

            }else if(oppenwith.equalsIgnoreCase("PDF.js")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openPDF(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }else if(oppenwith.equalsIgnoreCase("MP3")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }else if(oppenwith.equalsIgnoreCase("Bell-Reader")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openPDF(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }
            }else if(oppenwith.equalsIgnoreCase("Flow Video Player")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }else if(oppenwith.equalsIgnoreCase("BeLL Video Book Player")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {

                    }
                }

            }else if(oppenwith.equalsIgnoreCase("Native Video")){
                if (attmentNames.size() > 0) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        openAudioVideo(docId, (String) attmentNames.get(cnt), getExtension(attmentNames.get(cnt)));
                        break;
                    }
                }

            }
        } catch (Exception Er) {

        }
    }

    public void openHTML(String index) {
        final String mainFile =  index;
        try {
            try{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("org.mozilla.firefox");
                intent.setDataAndType(Uri.parse(mainFile),"text/html");
                intent.setComponent(new ComponentName("org.mozilla.firefox", "org.mozilla.firefox.App"));
                this.startActivity(intent);
                ///intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }catch(Exception err){
                Log.e("Error", err.getMessage());
                File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                File dst = new File(myDir,"firefox_49_0_multi_android.apk");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        } catch (Exception Er) {
            Er.printStackTrace();

        }

    }

    public void downloadHTMLContent(String docId, final String fileName) {
        final String myfilename =  fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);
            int lth= (int) fileAttachment.getLength();
            try{
                    InputStream in = fileAttachment.getContent();
                    String root = Environment.getExternalStorageDirectory().toString();
                    File newDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2/"+docId);
                    if (!newDir.exists()){
                        newDir.mkdirs();
                    }

                    File myDir = new File(root + "/ole_temp2/"+docId);
                    File dst = new File(myDir,fileAttachment.getName().replace(" ", ""));
                    String filepath[]= dst.toString().split("/");
                    int defaultLength = myDir.getPath().split("/").length;
                    String path= myDir.getPath();
                    //Log.e("tag", " Location  "+ dst.toString() + " Default :" + defaultLength + " fpath: "+filepath.length);
                    for(int cnt= defaultLength; cnt < (filepath.length-1);cnt++){
                        path = path +"/"+ filepath[cnt];
                        myDir = new File(path);
                        if (!myDir.exists()){
                            myDir.mkdirs();
                        }
                    }
                    try {
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dst));
                        byte[] buff = new byte[1024];
                        int len;
                        while ((len = in.read(buff)) > 0) {
                            out.write(buff, 0, len);
                        }

                        Log.e("tag", " Saved "+ dst.toString()+" Original length: "+ lth );

                        in.close();
                        out.close();
                        if(dst.getName().equalsIgnoreCase("index.html") && (filepath.length - defaultLength)==1 ){
                            indexFilePath = dst.toString();
                        }

                    }catch(Exception err){
                        Log.e("tag", " Saving "+ err.getMessage());
                    }
            }catch(Exception err){
                err.printStackTrace();
            }
        } catch (Exception Er) {
            Er.printStackTrace();

        }
    }

    public void openImage(String docId, final String fileName, String player) {
        final String myfilename =  fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);
            try{
                File src = new File(fileAttachment.getContentURL().getPath());
                InputStream in = new FileInputStream(src);
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                if (!myDir.exists()){
                    myDir.mkdirs();
                }
                File dst = new File(myDir,fileAttachment.getName().replace(" ", ""));
                try {
                    FileOutputStream out = new FileOutputStream(dst);
                    byte[] buff = new byte[1024];
                    int read = 0;
                    while ((read = in.read(buff)) > 0) {
                        out.write(buff, 0, read);
                    }
                    in.close();
                    out.close();
                    Log.e("tag", " Copied PDF "+ dst.toString());
                }catch(Exception err){
                    err.printStackTrace();
                }
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(dst), "image/*");
                //Log.e("tag", " URL Path "+ Uri.fromFile(dst).getPath());;
                startActivity(intent);
            }catch(Exception err){

            }
        } catch (Exception Er) {
            Er.printStackTrace();

        }

    }

    public static void mkDirs(File root, List<String> dirs, int depth) {
        if (depth == 0) return;
        for (String s : dirs) {
            File subdir = new File(root, s);
            subdir.mkdir();
            mkDirs(subdir, dirs, depth - 1);
        }
    }

    public void openPDF(String docId, final String fileName, String player) {
        final String myfilename =  fileName;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("resources");
            Document res_doc = res_Db.getExistingDocument(docId);
            final Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment(fileName);

            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Open Document");
            alertDialog.setMessage("Select which application you wish to open document with");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Adobe PDF Reader", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try{
                        File src = new File(fileAttachment.getContentURL().getPath());
                        InputStream in = new FileInputStream(src);
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/ole_temp");
                        if (!myDir.exists()){
                            myDir.mkdirs();
                        }
                        File dst = new File(myDir,fileAttachment.getName().replace(" ", ""));
                        try {
                            FileOutputStream out = new FileOutputStream(dst);
                            byte[] buff = new byte[1024];
                            int read = 0;
                            while ((read = in.read(buff)) > 0) {
                                out.write(buff, 0, read);
                            }
                            in.close();
                            out.close();
                            Log.e("tag", " Copied PDF "+ dst.toString());
                        }catch(Exception err){
                            err.printStackTrace();
                        } ///

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setPackage("com.adobe.reader");
                        intent.setDataAndType(Uri.fromFile(dst), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    }catch(Exception err){
                        File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                        File dst = new File(myDir,"adobe_reader.apk");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                } });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "In-App PDF Viewer", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    try{
                        File src = new File(fileAttachment.getContentURL().getPath());
                        InputStream in = new FileInputStream(src);
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/ole_temp");
                        if (!myDir.exists()){
                            myDir.mkdirs();
                        }
                        File dst = new File(myDir,fileAttachment.getName().replace(" ", ""));
                        try {
                            FileOutputStream out = new FileOutputStream(dst);
                            byte[] buff = new byte[1024];
                            int read = 0;
                            while ((read = in.read(buff)) > 0) {
                                out.write(buff, 0, read);
                            }
                            in.close();
                            out.close();
                            Log.e("tag", " Copied PDF "+ dst.toString());
                        }catch(Exception err){
                            err.printStackTrace();
                        } ///


                        Intent intent = new Intent(getActivity(), MyPdfViewerActivity.class);
                        Log.e("tag", " URL Path "+ Uri.fromFile(dst).getPath());
                        intent.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, Uri.fromFile(dst).getPath());
                        startActivity(intent);


                    }catch(Exception err){

                    }
                }});
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {


                }});
            alertDialog.show();

        } catch (Exception Er) {
            Er.printStackTrace();

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
            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(dst).toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Log.e("tag","- "+ mimetype +" - ");

            if(mimetype=="audio/mpeg"){
                intent.setDataAndType(Uri.fromFile(dst),mimetype);
                getActivity().startActivity(intent);
            }else{
                try {
                    intent.setDataAndType(Uri.fromFile(dst),mimetype);
                    getActivity().startActivity(intent);
                }catch (Exception Er) {
                    Log.e("tag", Er.getMessage());
                }
            }
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
            resourceTitleList= new String[results.getCount()];
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
                    String myresExt = "";
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
                        myresExt = (String) resource_properties.get("openWith")+"";
                        resourceIdList[rsLstCnt]=myresId;
                        resourceTitleList[rsLstCnt]=myresTitile;
                        rsLstCnt++;
                    }catch(Exception err){


                        Log.e("tag", "ERROR "+ err.getMessage());
                        myresTitile = "Unknown resource .. ";
                        myresId = "";
                        myresDec = "Not yet downloaded.. Please sync";
                        myresType = "";
                        rsLstCnt++;

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
                }
            }
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


    public void RateResourceDialog(String resourceId, String title){
        // custom dialog
        final String resourceID = resourceId;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rate_resource_dialog);
        dialog.setTitle("Rate this resource");

        txtComment = (EditText) dialog.findViewById(R.id.editTextComment);


        ratingBar = (RatingBar) dialog.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,boolean fromUser) {
                ///txtRatingValue.setText(String.valueOf(rating));

            }
        });

        Button dialogButton = (Button) dialog.findViewById(R.id.btnRateResource);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRating(ratingBar.getRating(),String.valueOf(txtComment.getText()),resourceID);
                // openDoc(resourceIdList[position]);
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    public int saveRating(float rate,String comment,String resourceId){
        AndroidContext androidContext = new AndroidContext(getActivity());
        Manager manager = null;
        Database resourceRating;
        int doc_rating,doc_timesRated; String doc_comments;

        Toast.makeText(getActivity(),String.valueOf(rate),Toast.LENGTH_SHORT).show();
        ArrayList<String> commentList = new ArrayList<String>();

        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            resourceRating = manager.getDatabase("resourcerating");
            Document retrievedDocument = resourceRating.getExistingDocument(resourceId);
            if(retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                if(properties.containsKey("sum")){
                    doc_rating = (int) properties.get("sum") ;
                    doc_timesRated  = (int) properties.get("timesRated") ;
                    ///doc_comments = (String) properties.get("comments");
                    commentList = (ArrayList<String>) properties.get("comments");
                    commentList.add(comment);
                    Map<String, Object> newProperties = new HashMap<String, Object>();
                    newProperties.putAll(retrievedDocument.getProperties());
                    newProperties.put("sum", (doc_rating + rate));
                    newProperties.put("timesRated", doc_timesRated + 1);
                    newProperties.put("comments", commentList);
                    retrievedDocument.putProperties(newProperties);
                    return doc_rating;
                }
            }
            else{
                Document newdocument = resourceRating.getDocument(resourceId);
                Map<String, Object> newProperties = new HashMap<String, Object>();
                newProperties.put("sum", 1);
                newProperties.put("timesRated", 1);
                commentList.add(comment);
                newProperties.put("comments", commentList);
                newdocument.putProperties(newProperties);
                return 1;
            }


        }catch(Exception err){
            Log.e("VISITS", "ERR : " +err.getMessage());


        }

        return 0;

    }



    public class EditMovieTask extends AsyncTaskLoader<Boolean> {

        private int mType;

        public EditMovieTask(Context context, int type) {
            super(context);
            mType = type;
            forceLoad();
        }

        @Override
        public Boolean loadInBackground() {
            switch (mType) {
                case 0:
                    return append();
                case 1:
                    return crop();
                case 2:
                    return subTitle();
            }

            return false;
        }
        private boolean append() {
            try {
                // 複数の動画を読み込み
                String f1 = videoURl.getPath();
                //String f1 = Environment.getExternalStorageDirectory() + "/ole_temp2/bgin.mp4";
                ////String f2 = Environment.getExternalStorageDirectory() + "/sample2.mp4";
                String f2 = videoURl.getPath();
                Log.e("tag","Video URL "+ f2 +" - ");

                Movie[] inMovies = new Movie[]{
                        MovieCreator.build(f1),
                        MovieCreator.build(f2)};

                // 1つのファイルに結合
                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();
                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        if (t.getHandler().equals("soun")) {
                            audioTracks.add(t);
                        }
                        if (t.getHandler().equals("vide")) {
                            videoTracks.add(t);
                        }
                    }
                }
                Movie result = new Movie();
                if (audioTracks.size() > 0) {
                    result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                }
                if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                }

                // 出力
                Container out = new DefaultMp4Builder().build(result);
                String outputFilePath = Environment.getExternalStorageDirectory() + "/ole_temp/output_append.mp4";
                FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                out.writeContainer(fos.getChannel());
                fos.close();

                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(outputFilePath)).toString());
                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                intent.setDataAndType(Uri.fromFile(new File(outputFilePath)),mimetype);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        private boolean crop() {
            try {
                // オリジナル動画を読み込み
                ///String filePath = Environment.getExternalStorageDirectory() + "/sample1.mp4";
                String filePath = videoURl.getPath();
                Movie originalMovie = MovieCreator.build(filePath);

                // 分割
                Track track = originalMovie.getTracks().get(0);
                Movie movie = new Movie();
                movie.addTrack(new AppendTrack(new CroppedTrack(track, 200, 400)));

                // 出力
                Container out = new DefaultMp4Builder().build(movie);
                String outputFilePath = Environment.getExternalStorageDirectory() + "/output_crop.mp4";
                FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                out.writeContainer(fos.getChannel());
                fos.close();

                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(outputFilePath)).toString());
                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                intent.setDataAndType(Uri.fromFile(new File(outputFilePath)),mimetype);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean subTitle() {
            try {
                // オリジナル動画を読み込み
                ///String filePath = Environment.getExternalStorageDirectory() + "/sample1.mp4";
                String filePath = videoURl.getPath();
                Movie countVideo = MovieCreator.build(filePath);

                // SubTitleを追加
                TextTrackImpl subTitleEng = new TextTrackImpl();
                subTitleEng.getTrackMetaData().setLanguage("eng");

                subTitleEng.getSubs().add(new TextTrackImpl.Line(0, 1000, "Five"));
                subTitleEng.getSubs().add(new TextTrackImpl.Line(1000, 2000, "Four"));
                subTitleEng.getSubs().add(new TextTrackImpl.Line(2000, 3000, "Three"));
                subTitleEng.getSubs().add(new TextTrackImpl.Line(3000, 4000, "Two"));
                subTitleEng.getSubs().add(new TextTrackImpl.Line(4000, 5000, "one"));
                countVideo.addTrack(subTitleEng);

                // 出力
                Container container = new DefaultMp4Builder().build(countVideo);
                String outputFilePath = Environment.getExternalStorageDirectory() + "/output_subtitle.mp4";
                FileOutputStream fos = new FileOutputStream(outputFilePath);
                FileChannel channel = fos.getChannel();
                container.writeContainer(channel);
                fos.close();


                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(outputFilePath)).toString());
                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                intent.setDataAndType(Uri.fromFile(new File(outputFilePath)),mimetype);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }



    }

}

