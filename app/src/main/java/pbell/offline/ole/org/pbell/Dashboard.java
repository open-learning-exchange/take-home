package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 19/05/16.
 */
    import android.app.Dialog;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.support.annotation.IdRes;
    import android.support.design.widget.CoordinatorLayout;
    import android.support.design.widget.TabLayout;
    import android.support.v4.app.Fragment;
    import android.support.v4.app.FragmentManager;
    import android.support.v4.app.FragmentPagerAdapter;
    import android.support.v4.app.FragmentTransaction;
    import android.support.v4.content.ContextCompat;
    import android.support.v4.view.ViewPager;
    import android.support.v7.app.AlertDialog;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.Toolbar;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.roughike.bottombar.BottomBar;
    import com.roughike.bottombar.OnMenuTabClickListener;

    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.Set;

@SuppressWarnings("ALL")
public class Dashboard extends AppCompatActivity {
        private BottomBar mBottomBar;
        private TextView mMessageView;
        final Context context = this;
        private EditText txtSuncURL = null;


        private ViewPager mViewPager;
        private SectionsPagerAdapter mySectionsPagerAdapter;
        TabFragment0 tab0 = new TabFragment0();
        TabFragment1 tab1= new TabFragment1();
        TabFragment2 tab2= new TabFragment2();
        TabFragment3 tab3= new TabFragment3();

        public static final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings;

        String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
                sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
                sys_usergender, sys_uservisits= "";
        int sys_uservisits_Int=0;
        Object[] sys_membersWithResource;
        boolean userShelfSynced =false;





    FragmentManager fragMgr = getSupportFragmentManager();
        FragmentTransaction fragTrans = fragMgr.beginTransaction();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dashboard);

            Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
            setSupportActionBar(toolbar);

            ///settings = context.getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
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

         ///   Log.e("MYAPP", " membersWithResource  = "+sys_membersWithResource.length);


            TextView lbldate = (TextView) findViewById(R.id.lblDate);

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
            lbldate.setText(displayedDate);



            mySectionsPagerAdapter = new SectionsPagerAdapter(fragMgr);
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mySectionsPagerAdapter);

            mBottomBar = BottomBar.attach(this, savedInstanceState);
            mBottomBar.useOnlyStatusBarTopOffset();
            //mBottomBar.setMaxFixedTabs(n-1);
            //mBottomBar.noNavBarGoodness();
           // mBottomBar = BottomBar.attachShy((CoordinatorLayout) findViewById(R.id.main_content),findViewById(R.id.container), savedInstanceState);

            mBottomBar.noTabletGoodness();
            mBottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabClickListener() {
                @Override
                public void onMenuTabSelected(@IdRes int menuItemId) {
                   /// mMessageView.setText(getMessage(menuItemId, false));
                }

                @Override
                public void onMenuTabReSelected(@IdRes int menuItemId) {
                    Toast.makeText(getApplicationContext(), getMessage(menuItemId, true), Toast.LENGTH_SHORT).show();
                }
            });



            // Setting colors for different tabs when there's more than three of them.
            // You can set colors for tabs in three different ways as shown below.
            mBottomBar.mapColorForTab(0, ContextCompat.getColor(this, R.color.colorAccent));
            mBottomBar.mapColorForTab(1, 0xFF5D4037);
            mBottomBar.mapColorForTab(2, "#7B1FA2");
            mBottomBar.mapColorForTab(3, "#FF5252");






            //mBottomBar.hide();
        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_syncserverUrl) {
                SyncDialog(sys_oldSyncServerURL);
                return true;
            }else if (id == R.id.action_syncNow) {
                Intent syncdeviceIntent = new Intent(this,SyncDevice.class);
                ///intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(syncdeviceIntent);
                return true;
            }else if (id == R.id.action_about) {
                return true;
            }else if (id == R.id.action_help) {
                return true;
            }else if (id == R.id.action_profile) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }



        private String getMessage(int menuItemId, boolean isReselection) {
            String message = "Content for ";

            switch (menuItemId) {
                case R.id.bb_menu_friends:
                    mViewPager.setCurrentItem(0);
                    message += "Achievements";
                    break;
                case R.id.bb_menu_favorites:
                    mViewPager.setCurrentItem(2);
                    message += "My Library";
                    break;
                case R.id.bb_menu_recents:
                    mViewPager.setCurrentItem(1);
                    message += "";
                    break;
                case R.id.bb_menu_food:
                    mViewPager.setCurrentItem(3);
                    message += "";
                    break;
            }

            if (isReselection) {
                message += "";
            }

            return message;
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            // Necessary to restore the BottomBar's state, otherwise we would
            // lose the current tab on orientation change.
            mBottomBar.onSaveInstanceState(outState);
        }


        public class SectionsPagerAdapter extends FragmentPagerAdapter {

            public SectionsPagerAdapter(FragmentManager fm) {

                super(fm);
            }

            @Override
            public Fragment getItem(int position) {

                switch (position) {
                    case 0:
                        return tab0;
                    case 1:
                        return tab1;
                    case 2:
                        return tab2;
                    case 3:
                        return tab3;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                // Show 3 total pages.
                return 4;
            }

        }

    /*
        public static class PlaceholderFragment extends Fragment {
            //The fragment argument representing the section number for this fragment.
            private static final String ARG_SECTION_NUMBER = "section_number";

            //Returns a new instance of this fragment for the given section

            public static PlaceholderFragment newInstance(int sectionNumber) {
                PlaceholderFragment fragment = new PlaceholderFragment();
                Bundle args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, sectionNumber);
                fragment.setArguments(args);
                return fragment;
            }

            public PlaceholderFragment() {
            }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
                View rootView = inflater.inflate(R.layout.fragment_main, container, false);
                return rootView;
            }
        }

    */
        public void SyncDialog(String oldSyncURL){
            // custom dialog
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.syncurl_dialog);
            dialog.setTitle("Synchronization Server Url");

            txtSuncURL = (EditText)dialog.findViewById(R.id.txtsyncURL);
            txtSuncURL.setText(oldSyncURL);
            Button dialogButton = (Button) dialog.findViewById(R.id.btnSaveSyncURL);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sys_oldSyncServerURL = txtSuncURL.getText().toString();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("pf_sysncUrl", sys_oldSyncServerURL);
                    editor.commit();

                    dialog.dismiss();
                }
            });

            dialog.show();

        }

    }

