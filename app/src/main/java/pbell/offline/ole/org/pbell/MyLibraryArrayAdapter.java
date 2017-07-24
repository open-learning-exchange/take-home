package pbell.offline.ole.org.pbell;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leonardmensah on 17/05/16.
 */
@SuppressWarnings("ALL")
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
        map.put("image1", R.drawable.pdf);
        map.put("image2", R.drawable.web);
        map.put("image3", R.drawable.mp3);
        map.put("image4", R.drawable.video);
       // dh = new DataHelper(getApplicationContext());
    }

    /*@Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vi = convertView;
        CustomAdapter.ViewHolder holder;

        if(convertView==null){
            holder = new CustomAdapter.ViewHolder();
            vi = inflater.inflate(R.layout.tabitem, null);

            ///View rowView = inflater.inflate(R.layout.tabitem, parent, false);
            holder.text = (TextView) vi.findViewById(R.id.text);
            holder.text1 = (TextView) vi.findViewById(R.id.text1);
            holder.image = (ImageView) vi.findViewById(R.id.image);

            vi.setTag(holder);
        }
        else {
            holder = (CustomAdapter.ViewHolder) vi.getTag();
        }

        if(data.size()<=0)
        {
            text.setText("No Data");

        }
        else
        {

                tempValues=null;
                tempValues = ( ListModel ) data.get( position );


            holder.text.setText( tempValues.getTitle() );
            holder.text1.setText( tempValues.getDescription() );
            holder.image.setImageResource(map.get("image3"));
            vi.setTag(holder);
            Log.e("MYAPP", "Added Id = "+tempValues.getTitle());


        }


        return vi;
    }
    */

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater minflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewHolder mHolder;
        if (convertView == null) {
            convertView = minflater.inflate(R.layout.tabitem, null);
            mHolder = new ViewHolder();

            mHolder.mText=(TextView) convertView.findViewById(R.id.text);
            mHolder.mText1=(TextView) convertView.findViewById(R.id.text1);
            mHolder.mImage=(ImageView) convertView.findViewById(R.id.image);

            //convertView.setTag(mHolder);

            tempValues=null;
            tempValues = ( ListModel ) data.get( position );
            mHolder.mText.setText( tempValues.getTitle() );
            mHolder.mText1.setText( tempValues.getDescription() );
            mHolder.mImage.setImageResource(map.get("image3"));

            ///convertView.setTag(mHolder);
            Log.e("MYAPP", "Added Id = "+tempValues.getTitle());

        } else {
           /// mHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView mText;
        private TextView mText1;
        private ImageView mImage;

    }

}