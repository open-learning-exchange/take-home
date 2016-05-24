package pbell.offline.ole.org.pbell;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MemberListDownloadRes extends AppCompatActivity {

    ListView list;
    CustomAdapter adapter;
    public ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<ListModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list_download_res);

        setListData();

        ListView lv = (ListView) findViewById(R.id.memberList_listView);
        MyLibraryArrayAdapter adapter = new MyLibraryArrayAdapter(this, R.id.memberList_listView, CustomListViewValuesArr);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Toast.makeText(getParent(), position + " Clicked Item ", Toast.LENGTH_LONG).show();
            }
        });

    }

    /******
     * Function to set data in ArrayList
     *************/
    public void setListData() {

        for (int i = 0; i < 8; i++) {

            final ListModel sched = new ListModel();

            /******* Firstly take data in model object ******/
            sched.setTitle("Resource Title : " + i);
            ///sched.setImage("image"+i);
            sched.setImage("image" + (i + 1) + "");
            sched.setDescription("Resource Type : " + i + "");

            /******** Take Model Object in ArrayList **********/
            CustomListViewValuesArr.add(sched);
        }

    }


    /*****************
     * This function used by adapter
     ****************/
    public void onItemClick(int mPosition) {
        ListModel tempValues = (ListModel) CustomListViewValuesArr.get(mPosition);


        // SHOW ALERT

        // Toast.makeText(CustomListView,""+tempValues.getCompanyName()+" Image:"+tempValues.getImage() +" Url:"+tempValues.getUrl(), Toast.LENGTH_LONG).show();
    }
}

