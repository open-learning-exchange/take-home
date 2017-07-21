package pbell.offline.ole.org.pbell;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
@SuppressWarnings("ALL")
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    CouchViews chViews = new CouchViews();
    final Context context = this;
    String doc_lastVisit;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsername;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    String sys_oldSyncServerURL,sys_username,sys_lastSyncDate,
            sys_password,sys_usercouchId,sys_userfirstname,sys_userlastname,
            sys_usergender,sys_uservisits,lbl_SeverName,sys_servername,sys_serverversion= "";
    Object[] sys_membersWithResource;
    ///int sys_uservisits=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        // Set up the login form.
        mUsername = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });



        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();


            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        // Restore preferences
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
        sys_servername = settings.getString("pf_server_name"," -- ");
        sys_serverversion = settings.getString("pf_server_version"," ---");



        if(sys_username!=""){
            mUsername.setText(sys_username);
        }else{
            mUsername.setText("");
        }
        Set<String>  mwr = settings.getStringSet("membersWithResource",null);
        try{
            sys_membersWithResource = mwr.toArray();
            Log.e("MYAPP", " membersWithResource  = "+sys_membersWithResource.length);

        }catch(Exception err){
            Log.e("MYAPP", " Error creating  sys_membersWithResource");
        }


        try {
            AndroidContext androidContext = new AndroidContext(this);
            Manager manager = null;
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("members");
            if(db.getDocumentCount()<1){
                Log.e("MYAPP", " Device Not Synced  ");
                db.close();
                getSyncURLDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
            getSyncURLDialog();
            Log.e("MYAPP", " Device Not Synced  ");

        }


    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUsername, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsername.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsername.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            ///mPasswordView.setError(getString(R.string.error_invalid_password));
            ///focusView = mPasswordView;
            //cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsername.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public boolean authenticateUser(){
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("members");
            Query orderedQuery = chViews.CreateLoginByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                String doc_loginId = (String) properties.get("login");
                String doc_password = (String) properties.get("password");

                if(mUsername.getText().toString().equals(doc_loginId)) {
                    Log.e("MYAPP", "Authentiicating User");

                    if (mPasswordView.getText().toString().equals(doc_password) && !properties.containsKey("credentials") ) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("pf_username", (String) properties.get("login"));
                        editor.putString("pf_password", (String) properties.get("password"));
                        editor.putString("pf_usercouchId", (String) properties.get("_id"));
                        editor.putString("pf_userfirstname", (String) properties.get("firstName"));
                        editor.putString("pf_userlastname", (String) properties.get("lastName"));
                        editor.putString("pf_usergender", (String) properties.get("Gender"));
                        editor.putString("pf_lastVisitDate", doc_lastVisit);
                        try {
                            String noOfVisits = properties.get("visits").toString();
                            editor.putInt("pf_uservisits_Int", (Integer.parseInt(noOfVisits) + totalVisits((String) properties.get("_id"))));
                        } catch (Exception err) {
                        }
                        Set<String> stgSet = settings.getStringSet("pf_userroles", new HashSet<String>());
                        ArrayList roleList = (ArrayList<String>) properties.get("roles");
                        for (int cnt = 0; cnt < roleList.size(); cnt++) {
                            stgSet.add(String.valueOf(roleList.get(cnt)));
                        }
                        editor.putStringSet("pf_userroles", stgSet);
                        editor.commit();
                        Log.e("MYAPP", " Data Login OLD encryption: " + doc_loginId + " Password: " + doc_password);
                        Intent intent = new Intent(this, Dashboard.class);
                        startActivity(intent);
                        return true;

                    }else if (doc_password == "" && !mPasswordView.getText().toString().equals("")) {
                        try {
                            Map<String, Object> doc_credentials = (Map<String, Object>) properties.get("credentials");
                            AndroidDecrypter adc = new AndroidDecrypter();
                            if(adc.AndroidDecrypter(doc_loginId, mPasswordView.getText().toString(), doc_credentials.get("value").toString())){
                               SharedPreferences.Editor editor = settings.edit();
                                editor.putString("pf_username", (String) properties.get("login"));
                                editor.putString("pf_password", (String) properties.get("password"));
                                editor.putString("pf_usercouchId", (String) properties.get("_id"));
                                editor.putString("pf_userfirstname", (String) properties.get("firstName"));
                                editor.putString("pf_userlastname", (String) properties.get("lastName"));
                                editor.putString("pf_usergender", (String) properties.get("Gender"));
                                editor.putString("pf_lastVisitDate", doc_lastVisit);
                                try {
                                    String noOfVisits = properties.get("visits").toString();
                                    editor.putInt("pf_uservisits_Int", (Integer.parseInt(noOfVisits) + totalVisits((String) properties.get("_id"))));
                                } catch (Exception err) {

                                }
                                Set<String> stgSet = settings.getStringSet("pf_userroles", new HashSet<String>());
                                ArrayList roleList = (ArrayList<String>) properties.get("roles");
                                for (int cnt = 0; cnt < roleList.size(); cnt++) {
                                    stgSet.add(String.valueOf(roleList.get(cnt)));
                                }
                                editor.putStringSet("pf_userroles", stgSet);
                                editor.commit();
                                Log.e("MYAPP", " Data Login Id: " + doc_loginId + " Password: " + doc_password);
                                Intent intent = new Intent(this, Dashboard.class);
                                startActivity(intent);
                                return true;

                            }

                           ////doc_credentials.get("salt").toString());
                            ///doc_credentials.get("value").toString()
                        } catch (Exception err) {
                            Log.e("MYAPP", " Encryption Err  " + err.getMessage());
                        }

                    } else{
                        return false;
                    }
                }

            }
            db.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getSystemInfo(){
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database db = manager.getExistingDatabase("configurations");
            Query orderedQuery = chViews.CreateLoginByIdView(db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                QueryRow row = it.next();
                String docId = (String) row.getValue();
                Document doc = db.getExistingDocument(docId);
                Map<String, Object> properties = doc.getProperties();
                String Server_name = (String) properties.get("name");
                String Server_nationName = (String) properties.get("nationName");
                String Server_version = (String) properties.get("version");

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pf_server_name", Server_name);
                editor.putString("pf_server_nation", Server_nationName);
                editor.putString("pf_server_version", Server_version);

            }
            db.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                ///Thread.sleep(1000);

                return authenticateUser();

            } catch (Exception e) {
                Log.v("myLoginTag","Login Error "+e.getLocalizedMessage());
                return false;
            }

            // TODO: register the new account here.
            //return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    public void getSyncURLDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.newsetup_dialog_welcome);
        dialog.setTitle("WELCOME");
        dialog.setCancelable(false);


        final EditText txtSuncURL = (EditText)dialog.findViewById(R.id.txtNewSyncURL);
        txtSuncURL.setText(sys_oldSyncServerURL);
        Button dialogButton = (Button) dialog.findViewById(R.id.btnNewSaveSyncURL);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sys_oldSyncServerURL = txtSuncURL.getText().toString();

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pf_sysncUrl", sys_oldSyncServerURL);
                editor.commit();

                ///dialog.dismiss();
                Intent intent = new Intent(context,NewSync.class);
                startActivity(intent);
                ///dialog.getOwnerActivity().setVisible(false);
            }
        });
        Button btnCloseWindow = (Button) dialog.findViewById(R.id.btnCloseDialog);
        btnCloseWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        dialog.show();

    }
    public int totalVisits(String memberId){
        AndroidContext androidContext = new AndroidContext(this);
        Manager manager = null;
        Database visitHolder;
        int doc_noOfVisits;


       try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            visitHolder = manager.getDatabase("visits");
            Document retrievedDocument = visitHolder.getExistingDocument(memberId);
           if(retrievedDocument != null) {
                Map<String, Object> properties = retrievedDocument.getProperties();
                 if(properties.containsKey("noOfVisits")){
                     doc_noOfVisits = (int) properties.get("noOfVisits") ;
                     doc_lastVisit = (String) properties.get("lastVisits");
                    /// Increase No Of visits by 1
                     Map<String, Object> newProperties = new HashMap<String, Object>();
                     newProperties.putAll(retrievedDocument.getProperties());
                     doc_noOfVisits += 1;
                     newProperties.put("noOfVisits", doc_noOfVisits);
                     newProperties.put("lastVisits", todaysDate());
                     retrievedDocument.putProperties(newProperties);
                     return doc_noOfVisits;
                 }
            }
            else{
                Document newdocument = visitHolder.getDocument(memberId);
                Map<String, Object> newProperties = new HashMap<String, Object>();
                newProperties.put("noOfVisits", 1);
                doc_lastVisit = todaysDate();
                newProperties.put("lastVisits", doc_lastVisit);
                newdocument.putProperties(newProperties);
               return 1;
            }


        }catch(Exception err){
           Log.e("VISITS", "ERR : " +err.getMessage());

       }

        return 0;

    }
    public String todaysDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println(dateFormat.format(cal.getTime()));
        return dateFormat.format(cal.getTime());

    }
}

