package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 20/05/16.
 */
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

@SuppressWarnings("ALL")
public class TabFragment0 extends Fragment {
    TextView lblWelcome,lblNoResouces,lblVisits;
    View rootView;
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;

    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
            sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
            sys_usergender, sys_uservisits= "";
    int sys_uservisits_Int=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_fragment_0, container, false);
        // Restore preferences
        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username","");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl","");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate","");
        sys_password = settings.getString("pf_password","");
        sys_usercouchId = settings.getString("pf_usercouchId","");
        sys_userfirstname = settings.getString("pf_userfirstname","");
        sys_userlastname = settings.getString("pf_userlastname","");
        sys_usergender = settings.getString("pf_usergender","");
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int",0);
        sys_uservisits= settings.getString("pf_uservisits","");

        updateUI();

        return rootView;


    }
    public void updateUI(){
        lblWelcome = (TextView)rootView.findViewById(R.id.lblWelcome);
        lblWelcome.setText("Welcome "+sys_userfirstname +" "+sys_userlastname);

        lblVisits= (TextView)rootView.findViewById(R.id.lblVisits);
        if(sys_uservisits==""){
            lblVisits.setText(""+sys_uservisits_Int);
        }else{
            lblVisits.setText(""+sys_uservisits);
        }

    }
}