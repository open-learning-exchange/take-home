package pbell.offline.ole.org.pbell;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Loading_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        copyAPK(R.raw.adobe_reader, "adobe_reader.apk");
//        copyAPK(R.raw.firefox_49_0_multi_android, "firefox_49_0_multi_android.apk");


        Intent intent = new Intent(this, FullscreenLogin.class);
        startActivity(intent);
        finish();
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
