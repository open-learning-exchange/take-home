package pbell.offline.ole.org.pbell;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.*;

/**
 * Created by leonardmensah on 17/05/16.
 */
public class MyLibraryArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    ///private final String[] values;
    private ArrayList data;
    //Datahelper dh;

    public TextView text;
    public TextView text1;
    public TextView textWide;
    public ImageView image;
    public Map<String, Integer> map;


    ListModel tempValues=null;
    int i=0;

    public MyLibraryArrayAdapter(Context context, int textViewResourceId, ArrayList d) {
        super(context, textViewResourceId, d);
        this.context = context;
        //this.values = values;
        this.data=d;
        map = new HashMap<String, Integer>();
        map.put("image1", R.drawable.image1);
        map.put("image2", R.drawable.image2);
        map.put("image3", R.drawable.image3);
        map.put("image4", R.drawable.image4);
       // dh = new DataHelper(getApplicationContext());
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vi = convertView;
        CustomAdapter.ViewHolder holder;

        if(convertView==null){
        holder = new CustomAdapter.ViewHolder();
        vi = inflater.inflate(R.layout.tabitem, null);

        ///View rowView = inflater.inflate(R.layout.tabitem, parent, false);
        text = (TextView) vi.findViewById(R.id.text);
        text1 = (TextView) vi.findViewById(R.id.text1);
        image = (ImageView) vi.findViewById(R.id.image);
        }
        else
            holder=(CustomAdapter.ViewHolder)vi.getTag();

        if(data.size()<=0)
        {
            text.setText("No Data");

        }
        else
        {

                tempValues=null;
                tempValues = ( ListModel ) data.get( position );

                /************  Set Model values in Holder elements ***********/

                text.setText( tempValues.getTitle() );
                text1.setText( tempValues.getDescription() );
                image.setImageResource(map.get("image3"));





                ///image.setImageResource( Resources.getSystem().getIdentifier("pbell.offline.ole.org.pbell:drawable/"+tempValues.getImage() ,null,null));
        /*

            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), v.getId() + " this is my  =)", Toast.LENGTH_LONG).show();
                }
            });

         */
            ///vi.setOnClickListener(new OnItemClickListener( position ));


               /// holder.image.setImageResource( res.getIdentifier("com.androidexample.customlistview:drawable/"+tempValues.getImage() ,null,null));

                /******** Set Item Click Listner for LayoutInflater for each row *******/

                ////vi.setOnClickListener(new AdapterView.OnItemClickListener( position ));





        }


        return vi;
    }
}