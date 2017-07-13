package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 06/06/2017.
 */
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

public class ListViewAdapter_Library extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
    private static final String TAG = "MYAPP";

    public ListViewAdapter_Library(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.listview_row_library, null);

        TextView title = (TextView)vi.findViewById(R.id.list_title); // title
        TextView description = (TextView)vi.findViewById(R.id.list_desc); // description
        Button details = (Button)vi.findViewById(R.id.btn_listVewDetails); // details
        Button feedback = (Button)vi.findViewById(R.id.btn_listFeedback); // feedback
        Button addToMyLibrary = (Button)vi.findViewById(R.id.btn_listAddToMyLibrary); // delete
        TextView ratingAvgNum = (TextView)vi.findViewById(R.id.lbl_listAvgRating); // delete
        TextView totalNum = (TextView)vi.findViewById(R.id.lbl_listTotalrating); // delete
        RatingBar ratingStars = (RatingBar) vi.findViewById(R.id.list_rating);
        LayerDrawable stars = (LayerDrawable) ratingStars.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffa500"), PorterDuff.Mode.SRC_ATOP);

        ProgressBar femalerating = (ProgressBar)vi.findViewById(R.id.female_progressbar); // delete
        ProgressBar malerating = (ProgressBar)vi.findViewById(R.id.male_progressbar); // delete
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); //  image

        HashMap<String, String> material = new HashMap<String, String>();
        material = data.get(position);

                // Setting all values in listview
        title.setText(material.get(ListView_Library.KEY_TITLE));
        description.setText(material.get(ListView_Library.KEY_DESCRIPTION));
        details.setText("Details");
        details.setTag(material.get(ListView_Library.KEY_DETAILS));
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Details Clicked ********** "+v.getTag());
            }
        });
        feedback.setText("Feedback");
        feedback.setTag(material.get(ListView_Library.KEY_FEEDBACK));
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Feedback Clicked ********** "+v.getTag());
            }
        });
        addToMyLibrary.setText("Add to myLibrary");

        addToMyLibrary.setTag(material.get(ListView_Library.KEY_DELETE));
        addToMyLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "addToMyLibrary Clicked ********** "+v.getTag());
            }
        });
        ratingAvgNum.setText(""+material.get(ListView_Library.KEY_RATING));
        ratingStars.setRating(Float.parseFloat(""+material.get(ListView_Library.KEY_RATING)));
        totalNum.setText(material.get(ListView_Library.KEY_TOTALNUM_RATING));
        femalerating.setProgress(Integer.parseInt("1"));
        //femalerating.setProgress(Integer.parseInt(material.get(ListView_Library.KEY_FEMALE_RATING)));
        malerating.setProgress(Integer.parseInt("1"));
        //malerating.setProgress(Integer.parseInt(material.get(ListView_Library.KEY_MALE_RATING)));
        imageLoader.DisplayImage(material.get(ListView_Library.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}