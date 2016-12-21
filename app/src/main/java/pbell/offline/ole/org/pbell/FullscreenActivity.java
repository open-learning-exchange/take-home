package pbell.offline.ole.org.pbell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static pbell.offline.ole.org.pbell.R.color.red;

public class FullscreenActivity extends AppCompatActivity {

    private View mContentView;
    final Context context = this;

    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;

    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
            sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
            sys_usergender, sys_uservisits= "";
    int sys_uservisits_Int=0;
    Object[] sys_membersWithResource;
    boolean userShelfSynced =false;

    CouchViews chViews = new CouchViews();
    String resourceIdList[];
    String resourceTitleList[];
    int rsLstCnt=0;
    Button[] libraryButtons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.fullscreen_content);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
       /* mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show();
            }
        });*/
        ///////////////////////////////////
        TextView txtcurDate = (TextView) findViewById(R.id.lblDate);
        txtcurDate.setText(curdate());

        /////////////////////////////////
        settings = getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
        sys_password = settings.getString("pf_password","");
        sys_usercouchId = settings.getString("pf_usercouchId","");
        sys_userfirstname = settings.getString("pf_userfirstname","");
        sys_userlastname = settings.getString("pf_userlastname","");
        sys_usergender = settings.getString("pf_usergender","");
        sys_uservisits = settings.getString("pf_uservisits","");

        try {
            Set<String> mwr = settings.getStringSet("membersWithResource", null);
            sys_membersWithResource = mwr.toArray();
            for (int cnt = 0; cnt < sys_membersWithResource.length; cnt++) {

                Log.e("MYAPP", " members With Resource Synced  = " + sys_membersWithResource[cnt]);
                if (sys_membersWithResource[cnt].equals(sys_usercouchId)) {
                    userShelfSynced = true;
                    break;
                }
            }
        }catch(Exception err){
            Log.e("TakeHome", " MembersWithResource Array" + err.getMessage());
        }

        TextView lblName = (TextView) findViewById(R.id.lblName);
        lblName.setText(" "+sys_userfirstname +" "+sys_userlastname);

        TextView lblVisits= (TextView) findViewById(R.id.lblVisits);
        if(sys_uservisits==""){
            lblVisits.setText(""+sys_uservisits_Int+" Visits");
        }else{
            lblVisits.setText(""+sys_uservisits+" Visits");
        }


        if (!userShelfSynced){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Materials on your shelf are NOT yet synchronized unto this device. " +
                    "You can only view title of items on your shelf BUT you can not launch or read them." +
                    "To access your materials on this device, please sync device with server.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog closeDialogue = builder.create();
            closeDialogue.show();
        }

        LoadShelfResourceList();

    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    public String curdate(){
        Calendar cal= Calendar.getInstance();
        Date d = new Date();

        SimpleDateFormat s_df = new SimpleDateFormat("EEEE");
        String dayOfTheWeek = s_df.format(d);
        SimpleDateFormat date_df = new SimpleDateFormat("d");
        String dayNumber = date_df.format(d);
        SimpleDateFormat month_df = new SimpleDateFormat("MMMM");
        String month_name = month_df.format(cal.getTime());
        SimpleDateFormat year_df = new SimpleDateFormat("yyyy");
        String year = year_df.format(d);
        String displayedDate = dayOfTheWeek + "  |  "+dayNumber+" " + month_name+" "+year;
        return displayedDate;
    }
/*
    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
    }
    */

    public void LoadShelfResourceList() {
        String memberId = sys_usercouchId;
        AndroidContext androidContext = new AndroidContext(context);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("shelf");
            Query orderedQuery = chViews.ReadShelfByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            resourceIdList = new String[results.getCount()];
            resourceTitleList= new String[results.getCount()];
            rsLstCnt = 0;
            for (Iterator<QueryRow> it = results; it.hasNext(); ) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                if(memberId.equals((String) properties.get("memberId"))) {
                    String myresTitile = ((String) properties.get("resourceTitle"));
                    String myresId = ((String) properties.get("resourceId"));
                    resourceTitleList[rsLstCnt]=myresTitile;
                    resourceIdList[rsLstCnt]=myresId;
                    Log.e("tag", "MEMBER ID "+ (String) properties.get("resourceTitle"));
                    rsLstCnt++;
                }
            }

            LinearLayout row2 = (LinearLayout) findViewById(R.id.layholder_library);
            libraryButtons = new Button[rsLstCnt];
            for( int ButtonCnt=0;ButtonCnt< rsLstCnt;ButtonCnt++) {
                libraryButtons[ButtonCnt] = new Button(this);
                libraryButtons[ButtonCnt].setText(resourceTitleList[ButtonCnt]);
                libraryButtons[ButtonCnt].setId(ButtonCnt);
                libraryButtons[ButtonCnt].setBackgroundResource(R.drawable.rounded_corners_black_blue);
                libraryButtons[ButtonCnt].setTextColor(getResources().getColor(R.color.white));
                libraryButtons[ButtonCnt].setAllCaps(false);
                libraryButtons[ButtonCnt].setPadding(10, 5, 10, 5);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    libraryButtons[ButtonCnt].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(170,MATCH_PARENT);
                layoutParams.setMargins(1, 0, 1, 0); // left, top, right, bottom
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setStroke(2, Color.WHITE);
                drawable.setCornerRadius(2);
                drawable.setColor(getResources().getColor(R.color.ole_black_blue));
                libraryButtons[ButtonCnt].setBackgroundDrawable(drawable);
                libraryButtons[ButtonCnt].setLayoutParams(layoutParams);
                row2.addView(libraryButtons[ButtonCnt]);
                try {
                    manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
                    Database resource_Db = manager.getExistingDatabase("resources");
                    Document resource_doc = resource_Db.getExistingDocument((String) resourceTitleList[ButtonCnt]);
                    Map<String, Object> resource_properties = resource_doc.getProperties();
                    Log.e("tag", "RES ID " + (String) resource_properties.get("resourceId"));
                }catch(Exception errs){
                    libraryButtons[ButtonCnt].setTextColor(getResources().getColor(R.color.ole_yellow));
                    Log.e("tag", "OBJECT ERROR "+ errs.toString());
                }

                libraryButtons[ButtonCnt].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(libraryButtons[view.getId()].getCurrentTextColor()==getResources().getColor(R.color.ole_yellow)){
                            Toast.makeText(context, "Online " + resourceTitleList[view.getId()], Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Offline " + resourceTitleList[view.getId()], Toast.LENGTH_SHORT).show();
                        }

                    }

                });
            }
            db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
