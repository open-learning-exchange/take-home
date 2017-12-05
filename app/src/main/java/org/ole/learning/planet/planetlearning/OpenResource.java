package org.ole.learning.planet.planetlearning;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.File;

/**
 * Created by leonardmensah on 12/3/17.
 */

public abstract class OpenResource extends Fragment {
    public String TAG;

    public Boolean open(String id, Context context, ProgressDialog mDialog) {
        String resourceIdTobeOpened = id;
        Log.d(TAG, "Trying to open resource " + id);
        try {
            AndroidContext androidContext = new AndroidContext(context);
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("offline_course_resources");
            Document res_doc = res_Db.getExistingDocument(resourceIdTobeOpened);
            String openwith = (String) res_doc.getProperty("openWith");
            String openResName = (String) res_doc.getProperty("title");
            //openedResourceId = resourceIdTobeOpened;
            //openedResourceTitle = openResName;
            ///openFromDiskDirectly = true;
            //////////////////logHouse.updateActivityOpenedResources(getContext(), sys_usercouchId, resourceIdTobeOpened, openResName);
            Log.e("MYAPP", " member opening resource  = " + resourceIdTobeOpened + " and Open with " + openwith);
            //// PDF and Bell-Reader
            if (openwith.equalsIgnoreCase("PDF.js") || (openwith.equalsIgnoreCase("Bell-Reader"))) {
                Log.e(TAG, " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e(TAG, " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            try {
                                mDialog.dismiss();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setPackage("com.adobe.reader");
                                intent.setDataAndType(Uri.fromFile(f), "application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            } catch (Exception err) {
                                err.printStackTrace();
                                myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                                File dst = new File(myDir, "adobe_reader.apk");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        }
                    }
                }
////MP3
            } else if (openwith.equalsIgnoreCase("MP3")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            context.startActivity(intent);
                        }
                    }
                }
///// VIDEO or Video Book Player
            } else if (openwith.equalsIgnoreCase("Flow Video Player") || (openwith.equalsIgnoreCase("Native Video"))) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            context.startActivity(intent);
                        }
                    }
                }
            }
///// HTML
            else if (openwith.equalsIgnoreCase("HTML")) {
               /* indexFilePath = null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(resourceIdTobeOpened, (String) attmentNames.get(cnt));
                    }
                    if (indexFilePath != null) {
                        openHTML(indexFilePath);
                    }
                } else {
                    openImage(resourceIdTobeOpened, (String) attmentNames.get(0), getExtension(attmentNames.get(0)));
                }*/
//// PDF
            } else if (openwith.equalsIgnoreCase("Just download")) {
                //// Todo work to get just download
            } else if (openwith.equalsIgnoreCase("BeLL Video Book Player")) {
            }
 //           mListener.onResourceOpened(openedResourceId, openedResourceTitle);
        } catch (Exception Er) {
            Log.d("MyCouch", "Opening resource error " + Er.getMessage());
            Er.printStackTrace();
        }
        return true;
    }
}
