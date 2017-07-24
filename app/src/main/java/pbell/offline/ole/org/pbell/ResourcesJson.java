package pbell.offline.ole.org.pbell;

import android.content.SharedPreferences;
import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.lightcouch.CouchDbClientAndroid;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by leonardmensah on 07/06/2017.
 */

public class ResourcesJson {
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    AndroidContext query_context;
    String query_serverURL;
    String query_databaseName;
    String url_Scheme ,url_Host;
    int url_Port;
    String url_user, url_pwd;
    JsonObject json;
    CouchViews chViews = new CouchViews();
    public Boolean ResourcesJson(String serverURL, String databaseName, final AndroidContext context){
        query_context = context;
        query_serverURL= serverURL;
        query_databaseName = databaseName;
        try {
            Log.e("MyCouch", "URL = "+serverURL);
            URI uri = URI.create(serverURL);
             url_Scheme = uri.getScheme();
             url_Host = uri.getHost();
             url_Port = uri.getPort();
             url_user = null;
             url_pwd = null;
            if(serverURL.contains("@")){
                String[] userinfo = uri.getUserInfo().split(":");
                url_user = userinfo[0];
                url_pwd = userinfo[1];
            }

            new Thread(new Runnable() {
                public void run(){
                    try {
                        CouchDbClientAndroid dbClient = new CouchDbClientAndroid(query_databaseName, false, url_Scheme, url_Host, url_Port, url_user, url_pwd);
                        List<JsonObject> allDocs = dbClient.view("_all_docs").includeDocs(true).query(JsonObject.class);
                        int x;
                        Manager manager = new Manager(query_context, Manager.DEFAULT_OPTIONS);
                        //Database shadowresourceslocal = manager.getDatabase("shadowresources");
                        //shadowresourceslocal.delete();
                        Database shadowresources = manager.getDatabase("shadowresources");
                        Map<String, Object> newshadowdocProperties;
                        String[] shdResIds = new String[allDocs.size()];
                        for(x=0;x<allDocs.size();x++) {
                            Log.e("MyCouch", "Remote View = " + allDocs.get(x));
                            JsonObject json = allDocs.get(x);
                            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                                if ("_id".equalsIgnoreCase(entry.getKey())) {
                                    shdResIds[x] = entry.getValue().getAsString();
                                }
                            }
                        }
                        JsonObject jsonData=null;
                        for (String shdResId : shdResIds) {
                            if (!shdResId.contains("_design")) {
                                jsonData = dbClient.find(JsonObject.class, shdResId);
                                JSONObject isJSONnow = new JSONObject(String.valueOf(jsonData));
                                try {
                                    Map<String, Object> properties = new HashMap<>();
                                    JsonFactory factory = new JsonFactory();
                                    ObjectMapper mapper = new ObjectMapper(factory);
                                    Document shadowresdocument = shadowresources.getDocument(shdResId);
                                    // convert you json string to Jackson JsonNode
                                    JsonNode rootNode = mapper.readTree(isJSONnow.toString());

                                    Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields();
                                    while (it.hasNext()) {
                                        Map.Entry<String, JsonNode> pair = it.next();
                                        String key = pair.getKey();
                                        String value = pair.getValue().toString();
                                        if (Arrays.asList(value).contains("")) {
                                            Log.e("MyCouch", "Is a json array = " + key);
                                            ArrayNode arrNode = (ArrayNode) pair.getValue();
                                            properties.put(key, arrNode);
                                        } else if (key.startsWith("_")) {
                                            Log.e("MyCouch", "skipping _rev = " + key);
                                        } else {
                                            Log.e("MyCouch", "its a json object: = " + key);
                                            properties.put(key, pair.getValue());
                                        }
                                    }
                                    shadowresdocument.putProperties(properties);
                                    /*
                                    try {
                                        Database shadowres = manager.getDatabase("shadowresources");
                                        Query orderedQuery = chViews.ReadMemberVisitsId(shadowres).createQuery();
                                        orderedQuery.setDescending(true);
                                        QueryEnumerator results = orderedQuery.run();
                                        for (Iterator<QueryRow> shr = results; shr.hasNext(); ) {
                                            QueryRow row = shr.next();
                                            String docId = (String) row.getValue();
                                            Document doc = shadowres.getExistingDocument(docId);
                                            Map<String, Object> properties2 = doc.getProperties();
                                            String doc_id = ((String) properties2.get("_id"));
                                            Log.e("MyCouch", "Number shadow items " + doc_id + " ");
                                            // Update server members with visits
                                        }
                                    } catch (Exception err) {
                                        Log.e("MyCouch", "reading visits error " + err);
                                    }*/
                                } catch (Exception e) {
                                    Log.e("MyCouch", "its a json object: = " + e.toString());
                                    e.printStackTrace();
                                }
                            }
                        }
                        json = new JsonObject();
                    }catch (Exception er){
                        er.printStackTrace();
                    }

                }
            }).start();
            return true;
        } catch (Exception e) {
            Log.e("MyCouch", "Loading resources from remote to device"+e.toString());
            e.printStackTrace();
            return false;
        }
    }

}
