package pbell.offline.ole.org.pbell;

import android.util.Log;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.BasicAuthenticator;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.ReplicationState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by leonardmensah on 21/05/16.
 */
public class ProcessSync {
    boolean wipeClearn;
    String sys_oldSyncServerURL;
    AndroidContext androidContext;
    TextView viewer;
    Replication push_1,pull_1;

    Boolean members,membercourseprogress,meetups,
            usermeetups,assignments,calendar,groups,
            invitations,languages,shelf,requests = false;

    Boolean members_push,membercourseprogress_push,meetups_push,
            usermeetups_push,assignments_push,calendar_push,groups_push,
            invitations_push,languages_push,shelf_push,requests_push = false;

    public void ProcessSync(boolean wipe,String url,AndroidContext andContext,TextView tv){

        viewer=tv;
        wipeClearn = wipe;
        sys_oldSyncServerURL = url;
        androidContext = andContext;
        Rep_Members();

    }
    public void Rep_Members(){
         String databaseName = "members";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }
            Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            Replication push = db.createPushReplication(url);
            Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            CountDownLatch replicationIdle_pull = new CountDownLatch(1);
            CountDownLatch replicationIdle_push = new CountDownLatch(1);
            ReplicationIdleObserver idleObserver_pull = new ReplicationIdleObserver(replicationIdle_pull);
            ReplicationIdleObserver idleObserver_push = new ReplicationIdleObserver(replicationIdle_push);

            pull.addChangeListener(idleObserver_pull);
            pull.addChangeListener(idleObserver_push);
            Authenticator auth = new BasicAuthenticator("appuser", "appuser");
            pull.removeChangeListener(idleObserver_pull);
            push.removeChangeListener(idleObserver_push);
            try{
                push.setAuthenticator(auth);
                push.start();
            }catch(Exception err){
                Log.v("MyCouch", databaseName+" "+" Nothing to push create database", err);
            }
            try{
                pull.setAuthenticator(auth);
                pull.start();;
            }catch(Exception err){
                Log.v("MyCouch", databaseName+" "+" Nothing to pull create database", err);
            }

            boolean success_pull = replicationIdle_pull.await(30, TimeUnit.SECONDS);
            boolean success_push = replicationIdle_push.await(30, TimeUnit.SECONDS);
            Log.v("MyCouch", databaseName+" "+" Action Result Pull "+ success_pull);
            Log.v("MyCouch", databaseName+" "+" Action Result  Push "+ success_pull);

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.v("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }

    public void Rep_membercourseprogress(){
         String databaseName = "membercourseprogress";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            Replication push = db.createPushReplication(url);
            Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            CountDownLatch replicationIdle_pull = new CountDownLatch(1);
            CountDownLatch replicationIdle_push = new CountDownLatch(1);
            ReplicationIdleObserver idleObserver_pull = new ReplicationIdleObserver(replicationIdle_pull);
            ReplicationIdleObserver idleObserver_push = new ReplicationIdleObserver(replicationIdle_push);

            pull.addChangeListener(idleObserver_pull);
            pull.addChangeListener(idleObserver_push);
            Authenticator auth = new BasicAuthenticator("appuser", "appuser");
            pull.removeChangeListener(idleObserver_pull);
            push.removeChangeListener(idleObserver_push);
            try{
                push.setAuthenticator(auth);
                push.start();
            }catch(Exception err){
                Log.v("MyCouch", databaseName+" "+" Nothing to push create database", err);
            }
            try{
                pull.setAuthenticator(auth);
                pull.start();;
            }catch(Exception err){
                Log.v("MyCouch", databaseName+" "+" Nothing to pull create database", err);
            }

            boolean success_pull = replicationIdle_pull.await(30, TimeUnit.SECONDS);
            boolean success_push = replicationIdle_push.await(30, TimeUnit.SECONDS);
            Log.v("MyCouch", databaseName+" "+" Action Result Pull "+ success_pull);
            Log.v("MyCouch", databaseName+" "+" Action Result  Push "+ success_pull);


            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.v("MyCouch", databaseName+" "+" Cannot create database " + e.getLocalizedMessage());
            return;
        }
    }
    public void Rep_meetups(){
        String databaseName = "meetups";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            Replication push_3 = db.createPushReplication(url);
            Replication pull_3 = db.createPullReplication(url);
            pull_3.setContinuous(false);
            push_3.setContinuous(false);
            Authenticator auth = new BasicAuthenticator("appuser", "appuser");
            try{
                push_3.setAuthenticator(auth);
                //push_3.addChangeListener(getReplicationListener());
                push_3.start();
            }catch(Exception err){
                Log.e("MyCouch", databaseName+" "+" Nothing to push create database", err);
            }
            try{
                pull_3.setAuthenticator(auth);
                //pull_3.addChangeListener(getReplicationListener());
                pull_3.start();;
            }catch(Exception err){
                Log.e("MyCouch", databaseName+" "+" Nothing to pull create database", err);
            }
            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database "+e.getLocalizedMessage());
            return;
        }
    }
    /*
    public void Rep_usermeetups(){
        final String databaseName = "usermeetups";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!usermeetups_push){
                            Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            usermeetups_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!usermeetups){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            usermeetups=true;
                            Rep_assignmentss();
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }
    public void Rep_assignmentss(){
        final String databaseName = "assignments";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!assignments_push){
                            Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            assignments_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!assignments){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            assignments=true;
                            Rep_calendar();
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }
    public void Rep_calendar(){
        final String databaseName = "calendar";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!calendar_push){
                        Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            calendar_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!calendar){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            calendar =true;
                            Rep_groups();
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }
    public void Rep_groups(){
        final String databaseName = "groups";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!groups_push){
                            Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            groups_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!groups){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            groups=true;
                            Rep_invitations();
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }
    public void Rep_invitations(){
        final String databaseName = "invitations";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!invitations_push){
                            Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            invitations_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!invitations){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            invitations=true;
                            Rep_languages();
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }
    public void Rep_languages(){
        final String databaseName = "languages";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!languages_push){
                            Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            languages_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!languages){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            languages=true;
                            Rep_shelf();
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }
    public void Rep_shelf(){
        final String databaseName = "shelf";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!shelf_push){
                            Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            shelf_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!shelf){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            shelf=true;
                            Rep_requests();
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return;
        }
    }
    public boolean Rep_requests(){
        final String databaseName = "requests";
        try {
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            if(wipeClearn){
                Database db = manager.getExistingDatabase(databaseName);
                db.delete();
            }

            final Database db = manager.getDatabase(databaseName);
            URL url = new URL(sys_oldSyncServerURL+"/"+databaseName);
            final Replication push = db.createPushReplication(url);
            final Replication pull = db.createPullReplication(url);
            pull.setContinuous(false);
            push.setContinuous(false);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(push.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!requests_push){
                            Log.e("Finished Pushing", databaseName+" "+db.getDocumentCount());
                            requests_push=true;
                        }
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        if(!requests){
                            Log.e("Finished Pulling", databaseName+" "+db.getDocumentCount());
                            requests=true;
                        }
                    }
                }
            });
            push.start();
            pull.start();

            //this.push = push;
            //this.pull = pull;
            //Authenticator auth = new BasicAuthenticator(username, password);
            //push.setAuthenticator(auth);
            //pull.setAuthenticator(auth);
            return true;

        } catch (Exception e) {
            Log.e("MyCouch", databaseName+" "+" Cannot create database", e);
            return false;
        }
    }
*/

    /*

    private Replication.ChangeListener getReplicationListener() {
        return new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                Log.i("GCC", "replication status is : " + event.getSource().getStatus());
                if (event.getSource().getStatus() == Replication.ReplicationStatus.REPLICATION_STOPPED) {
                    if(!members){
                        ///pull_1.removeChangeListener(pull_1.li);
                       try{
                           Log.i("GCMM", " Replication Process +++ : ");

                           Rep_membercourseprogress();
                           members=true;
                       }catch (Exception err){
                           Log.i("GCMM", " Replication Process : "+ err.getLocalizedMessage());
                           members=true;
                       }

                    }
                    else if(!membercourseprogress){
                        try{
                            //Rep_meetups();
                            membercourseprogress = true;
                        }catch (Exception err){
                            Log.i("GCMM", " Replication Process : " + err.getLocalizedMessage());
                            membercourseprogress = true;
                        }
                    }else if(!meetups){
                        try{
                            Log.i("GCMM", "Completed Replication Process : ");
                            meetups=true;
                        }catch (Exception err){
                            Log.i("GCMM", " Replication Process : " + err.getLocalizedMessage());
                            meetups = true;
                        }

                        Log.i("GCM", "Completed Replication Process : ");
                    }

                    ///GcmBroadcastReceiver.completeWakefulIntent(mIntent);
                }
            }
        };
    }
    */

    public static class ReplicationIdleObserver implements Replication.ChangeListener {
        private CountDownLatch idleSignal;

        public ReplicationIdleObserver(CountDownLatch idleSignal) {
            this.idleSignal = idleSignal;
        }

        @Override
        public void changed(Replication.ChangeEvent event) {
            if (event.getTransition() != null &&
                    event.getTransition().getDestination() == ReplicationState.IDLE) {
                idleSignal.countDown();
            }
        }
    }


}
