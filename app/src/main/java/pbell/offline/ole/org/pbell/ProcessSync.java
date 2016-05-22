package pbell.offline.ole.org.pbell;

import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.net.URL;

/**
 * Created by leonardmensah on 21/05/16.
 */
public class ProcessSync {
    boolean wipeClearn;
    String sys_oldSyncServerURL;
    AndroidContext androidContext;
    public void ProcessSync(boolean wipe,String url,AndroidContext andContext){
        wipeClearn = wipe;
        sys_oldSyncServerURL = url;
        androidContext = andContext;
        Rep_Members();

    }
    public void Rep_Members(){
        final String databaseName = "members";
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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
    public void Rep_membercourseprogress(){
        final String databaseName = "membercourseprogress";
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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
    public void Rep_meetups(){
        final String databaseName = "meetups";
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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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
    public void Rep_requests(){
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
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());
                    }
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if(pull.isRunning()){
                        Log.e("MyCouch", databaseName+" "+event.getChangeCount());
                    }else {
                        Log.e("Finished", databaseName+" "+db.getDocumentCount());

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

}
