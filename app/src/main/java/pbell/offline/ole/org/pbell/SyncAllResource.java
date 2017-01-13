package pbell.offline.ole.org.pbell;

import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.replicator.Replication;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * Created by leonardmensah on 13/01/2017.
 */

public class SyncAllResource {
   /* @Override
    protected Boolean doInBackground(String... params) {
        try {
            URL remote = getReplicationURL();
            CountDownLatch replicationDoneSignal = new CountDownLatch(1);
            final Database database;
            database = manager.getDatabase("resources");
            final Replication repl = database.createPullReplication(remote);
            repl.setContinuous(false);
            repl.setDocIds(resIDArrayList);
            repl.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    Log.e("MyCouch", "Current Status "+repl.getStatus());
                    if(repl.isRunning()){
                        if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_ACTIVE")) {
                            openFromDiskDirectly = false;
                            Log.e("MyCouch", " " + event.getChangeCount());
                            Log.e("MyCouch", " Document Count " + database.getDocumentCount());
                            libraryButtons[database.getDocumentCount()].setTextColor(getResources().getColor(R.color.ole_white));
                            mDialog.setMessage("Please wait, downloading resource.\n  This action might take a while.");

                        }else if(repl.getStatus().toString().equalsIgnoreCase("REPLICATION_STOPPED")){
                            checkAllDocsInDB();
                            mDialog.dismiss();
                        }
                        else{
                            mDialog.setMessage("Data transfer error. Check connection to server.");
                        }
                    }else {
                        Log.e("MyCouch", "Document Count " + database.getDocumentCount());
                        syncALLInOneStarted=false;
                        mDialog.dismiss();
                        alertDialogOkay("Downloaded complete. Thank you for waiting. Enjoy !! ");

                    }
                }
            });
            repl.start();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return null;
    }
    */
}
