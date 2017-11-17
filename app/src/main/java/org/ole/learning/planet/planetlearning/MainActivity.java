package org.ole.learning.planet.planetlearning;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isAppInstalled = appInstalledOrNot("com.adobe.reader");
        if(isAppInstalled) {
            Intent intent = new Intent(this, FullscreenLogin.class);
            startActivity(intent);
            finish();
        }else{
            try{
                copyAPK(R.raw.adobe_reader, "adobe_reader.apk");
                copyAPK(R.raw.firefox_49_0_multi_android, "firefox_49_0_multi_android.apk");
                Intent intent = new Intent(this, FullscreenLogin.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e("SplashScreen", "- Error - "+e.getMessage() );
            }

        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    public void alertDialogOkay(String Message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this.getApplicationContext());
        builder1.setMessage(Message);
        builder1.setCancelable(true);
        builder1.setNegativeButton("Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void copyAPK(int resource, String apkUrl) {
        InputStream in = getResources().openRawResource(resource);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/ole_temp2");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File dst = new File(myDir, apkUrl);
        try {
            FileOutputStream out = new FileOutputStream(dst);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
            Log.e("tag", "Adobe Reader Copied " + dst.toString());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
