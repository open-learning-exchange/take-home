package pbell.offline.ole.org.pbell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Attachment;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
public class MemberListDownloadRes extends AppCompatActivity {

    MyCustomAdapter dataAdapter = null;
    String[] str_memberIdList,str_memberNameList,str_memberLoginIdList;
    int[] str_memberResourceNo;
    String[] array_SelectedMembers;
    ArrayList<String> lst;

    CouchViews chViews = new CouchViews();
    final Context context = this;
    EditText inputSearch;

    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate= "";
    Object[] sys_membersWithResource;
    AndroidContext androidContext;

    Replication pull;
    Database dbResources;
    int syncCnt,resourceNo=0;

    Activity mActivity;
    ProgressDialog mydialog;
    AlertDialog closeDialogue;


    boolean synchronizing = false;
    boolean wipeClearn =false;


    JSONObject jsonData;
    int resourceCntr,attachmentLength;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list_download_res);


        androidContext = new AndroidContext(this);
        // Restore preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");

        Set<String>  mwr = settings.getStringSet("membersWithResource",null);
        try{
            sys_membersWithResource = mwr.toArray();
            Log.e("MYAPP", " membersWithResource  = "+sys_membersWithResource.length);

        }catch(Exception err){
            Log.e("MYAPP", " Error creating  sys_membersWithResource");
        }


        Bundle b=this.getIntent().getExtras();
        str_memberNameList=b.getStringArray("memberNameList");
        str_memberIdList=b.getStringArray("memberIdList");
        str_memberResourceNo=b.getIntArray("memberResourceNo");
        str_memberLoginIdList=b.getStringArray("memberLoginIdList");

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        mydialog = new ProgressDialog(MemberListDownloadRes.this);





        ///
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Resources synchronized successfully. ")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
               /* .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });*/
        closeDialogue = builder.create();

        Switch sw_wipeClean = (Switch) findViewById(R.id.switch1);
        sw_wipeClean.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wipeClearn = isChecked;
                ///Log.v("Switch State=", ""+isChecked);
            }
        });

        displayListView();

        checkButtonClick();

        final ArrayList<Members> memberList = new ArrayList<Members>();
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                dataAdapter.getFilter().filter(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void displayListView() {
        ArrayList<Members> countryList = new ArrayList<Members>();
        Members member;
        for(int cnt=0;cnt<str_memberLoginIdList.length;cnt++) {
            member = new Members(str_memberLoginIdList[cnt],str_memberNameList[cnt],str_memberIdList[cnt],str_memberResourceNo[cnt],false);
            countryList.add(member);
        }
        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,R.layout.members_list, countryList);
        ListView listView = (ListView) findViewById(R.id.memberList_listView);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        final Collator col = Collator.getInstance();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text
                Members country = (Members) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Clicked on Row: " + country.getName(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private class MyCustomAdapter extends ArrayAdapter<Members> {
        private ArrayList<Members> memberList;
        public MyCustomAdapter(Context context, int textViewResourceId,ArrayList<Members> countryList) {
            super(context, textViewResourceId, countryList);
            this.memberList = new ArrayList<Members>();
            this.memberList.addAll(countryList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.members_list, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Members member = (Members) cb.getTag();
                        member.setSelected(cb.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Members member = memberList.get(position);
            holder.code.setText(" (" +  member.getCode() + ")     [ "+member.getResNo()+ " ] ");
            holder.name.setText(member.getName());
            try{
                if (Arrays.asList(sys_membersWithResource).contains(member.getId())) {
                    holder.name.setChecked(true);
                }else{
                    holder.name.setChecked(false);
                }
            }catch(Exception err){
                holder.name.setChecked(false);
                Log.e("MYAPP", " Error creating  sys_membersWithResource");
            }
            holder.name.setTag(member);

            return convertView;

        }

    }

    private void checkButtonClick() {
        Button myButton = (Button) findViewById(R.id.findSelected);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Members> countryList = dataAdapter.memberList;
                int selectedCntr=0;
                for(int i=0;i<countryList.size();i++){
                    Members member = countryList.get(i);
                    if(member.isSelected()){
                        selectedCntr++;
                    }
                }
                array_SelectedMembers = new String[selectedCntr];
                selectedCntr = 0;
                for(int is=0;is<countryList.size();is++){
                    Members member = countryList.get(is);
                    if(member.isSelected()){
                        array_SelectedMembers[selectedCntr] = member.getId();
                        selectedCntr++;
                    }
                }

                SharedPreferences.Editor editor = settings.edit();
                Set<String> set = new HashSet<String>(Arrays.asList(array_SelectedMembers));
                editor.putStringSet("membersWithResource", set);
                editor.commit();

                CheckShelfForResources();
            }
        });
    }

    public void CheckShelfForResources(){
        this.lst = new ArrayList<String>();
        this.lst.clear();
        resourceNo=0;
        for(int cnt=0;cnt<array_SelectedMembers.length;cnt++) {
            Log.d("MyCouch", " Selected members "+array_SelectedMembers[cnt]);
            LoadShelfResourceList(array_SelectedMembers[cnt]);
        }

        Log.d("MYCouch", " Filtered Selected Items "+resourceNo +" - " +this.lst.size());

        Object[] st = lst.toArray();
        for (Object s : st) {
            if (lst.indexOf(s) != lst.lastIndexOf(s)) {
                lst.remove(lst.lastIndexOf(s));
            }
        }
        if(lst.size()>0) {
            if(wipeClearn) {
                try {
                    Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                    dbResources = manager.getExistingDatabase("resources");
                    dbResources.delete();
                    Log.e("MyCouch", " Deleted Resources DB");
                } catch (Exception err) {
                    Log.e("MyCouch", "Delete Error " + err.getLocalizedMessage());
                }
            }

            closeDialogue.show();
            mActivity = MemberListDownloadRes.this;
            mydialog.setMessage("Downloading, please wait .... ");
            mydialog.show();

            final AsyncTask<String, Void, Boolean> execute = new TestAsyncPull().execute();
            Thread th = new Thread(new Runnable() {
                private long startTime = System.currentTimeMillis();

                public void run() {
                    while (synchronizing) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("runOnUiThread", "running");
                                mydialog.setMessage("Downloading, please wait .... " + (syncCnt + 1));
                                if (!synchronizing) {

                                }

                            }
                        });
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            th.start();
        }else{
            mydialog.dismiss();
        }
    }

    public void LoadShelfResourceList(String memId) {
        String memberId = memId;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("shelf");
            Query orderedQuery = chViews.ReadShelfByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            //orderedQuery.setStartKey(memberId);
            //orderedQuery.setLimit(0);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                if(memberId.equals((String) properties.get("memberId"))) {
                    this.lst.add((String) properties.get("resourceId"));
                    resourceNo++;
                }
            }
            ///Log.d("PreExceute","Items "+ db.getDocumentCount());

            db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class TestAsyncPull extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute (){
            synchronizing = true;
            Log.d("PreExceute","On pre Exceute......");
        }

        protected Boolean doInBackground(final String... args) {
            Log.d("DoINBackGround","On doInBackground...");
            final Manager manager ;
            final Database res_Db;
            final Fuel ful = new Fuel();
            jsonData = null;
            attachmentLength= -1;
            try {
                URL url = new URL(sys_oldSyncServerURL+"/resources");
                manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
                dbResources = manager.getDatabase("resources");
                res_Db = manager.getExistingDatabase("resources");
                pull = dbResources.createPullReplication(url);
                pull.setFilter("apps/by_resource");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("_id", lst.get(syncCnt));
                //Log.e("MyCouch", " Resource ID "+ lst.get(syncCnt));
                pull.setFilterParams(params);
                pull.setContinuous(false);
                ////pull.setAuthenticator(new BasicAuthenticator(userName, userPw));
                pull.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        if(pull.isRunning()){
                            Log.e("MyCouch", " "+event.getChangeCount());
                            Log.e("MyCouch", " Document Count "+dbResources.getDocumentCount());
                        }else {
                            Log.e("Finished", ""+dbResources.getDocumentCount());
                            ////// CHECK REMOTE ATTACHMENT FILE SIZE VS LOCAL ATTACHMENT FILE SIZE (length)
                            Document res_doc = res_Db.getExistingDocument(lst.get(syncCnt));
                            final List<String> attmentNames = res_doc.getCurrentRevision().getAttachmentNames();
                            /// IF local document has attachments
                            if (attmentNames.size() > 0) {
                                for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                                    resourceCntr = cnt;
                                    Attachment fileAttachment = res_doc.getCurrentRevision().getAttachment((String) attmentNames.get(cnt));
                                    ///
                                    ful.get(sys_oldSyncServerURL+"/resources/"+lst.get(syncCnt)).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                                        @Override
                                        public void success(Request request, Response response, String s) {
                                            try {
                                                jsonData = new JSONObject(s);
                                                //Log.e("MyCouch", "-- "+jsonData);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                JSONObject jsob = jsonData.getJSONObject("_attachments");
                                                JSONObject jsoAttachments = jsob.getJSONObject((String) attmentNames.get(resourceCntr));
                                                attachmentLength = jsoAttachments.getInt("length");
                                                Log.e("MyCouch", "Attachment Object Content "+jsoAttachments);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        @Override
                                        public void failure(Request request, Response response, FuelError fuelError) {
                                            Log.e("MyCouch", " "+fuelError);

                                        }
                                    });
                                    /// If local attachment length is greater or equal to remote attachment, continue
                                    /// Else run the replication again
                                    if(fileAttachment.getLength() >= attachmentLength){
                                        Log.e("MyCouch", "Local = "+fileAttachment.getLength() + "  Remote = " +attachmentLength);
                                        if(syncCnt < (lst.size()-1)){
                                            syncCnt++;
                                            new TestAsyncPull().execute();
                                        }else{
                                            if (synchronizing){
                                                mydialog.dismiss();
                                            }
                                            synchronizing = false;
                                        }

                                    }else{
                                        Log.e("MyCouch", "Local = "+fileAttachment.getLength() + "  Remote = " +attachmentLength);

                                        new TestAsyncPull().execute();
                                    }
                                }
                            } else if(syncCnt < (lst.size()-1)){
                                syncCnt++;
                                new TestAsyncPull().execute();
                            }else{
                                if (synchronizing){
                                    mydialog.dismiss();
                                }
                                synchronizing = false;
                            }
                            //////////////////////

                        }
                    }
                });
                pull.start();


            } catch (Exception e) {
                Log.e("MyCouch", " "+" Cannot create database", e);
                return false;

            }
            return true;
        }
        protected void onProgressUpdate(Integer...a){
            Log.d("onProgress","You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(final Boolean success) {
            if (success){
                Log.d("MyCouch","Download Triggered");
            }else{
                Log.d("OnPostExec","");
            }
        }
    }

}


