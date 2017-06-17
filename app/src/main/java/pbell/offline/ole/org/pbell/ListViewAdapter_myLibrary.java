package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 06/06/2017.
 */

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

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewAdapter_myLibrary extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;
    private static final String TAG = "MYAPP";
    User_Dashboard user_dashboard = new User_Dashboard();

    public ListViewAdapter_myLibrary(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
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
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.listview_row_mylibrary, null);

        TextView title = (TextView) vi.findViewById(R.id.list_title); // title
        TextView description = (TextView) vi.findViewById(R.id.list_desc); // description
        Button details = (Button) vi.findViewById(R.id.btn_listVewDetails); // details
        Button feedback = (Button) vi.findViewById(R.id.btn_listFeedback); // feedback
        Button delete = (Button) vi.findViewById(R.id.btn_listDelete); // delete
        Button open = (Button) vi.findViewById(R.id.btn_listOpen); // delete
        TextView ratingAvgNum = (TextView) vi.findViewById(R.id.lbl_listAvgRating); // delete
        TextView totalNum = (TextView) vi.findViewById(R.id.lbl_listTotalrating); // delete
        RatingBar ratingStars = (RatingBar) vi.findViewById(R.id.list_rating);
        LayerDrawable stars = (LayerDrawable) ratingStars.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffa500"), PorterDuff.Mode.SRC_ATOP);

        ProgressBar femalerating = (ProgressBar) vi.findViewById(R.id.female_progressbar); // delete
        ProgressBar malerating = (ProgressBar) vi.findViewById(R.id.male_progressbar); // delete
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image); //  image

        HashMap<String, String> material = new HashMap<String, String>();
        material = data.get(position);

        // Setting all values in ListView_myLibrary
        title.setText(material.get(ListView_myLibrary.KEY_TITLE));
        description.setText(material.get(ListView_myLibrary.KEY_DESCRIPTION));
        details.setText("Details");
        details.setTag(material.get(ListView_myLibrary.KEY_DETAILS));
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(v.getTag().toString(), "Details");
                Log.i(TAG, "Details Clicked ********** " + v.getTag());
            }
        });
        feedback.setText("Feedback");
        feedback.setTag(material.get(ListView_myLibrary.KEY_FEEDBACK));
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(v.getTag().toString(), "Feedback");
                Log.i(TAG, "Feedback Clicked ********** " + v.getTag());
            }
        });
        delete.setText("Delete");
        delete.setTag(material.get(ListView_myLibrary.KEY_DELETE));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(v.getTag().toString(), "Delete");
                Log.i(TAG, "Delete Clicked ********** " + v.getTag());
            }
        });

        if(material.get(ListView_myLibrary.KEY_RESOURCE_STATUS).equalsIgnoreCase("downloaded")){
            open.setText("Open");
            open.setTag(material.get(ListView_myLibrary.KEY_DELETE));
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonAction(v.getTag().toString(), "Open");
                }
            });
        }else{
            open.setText("Download");
            open.setTag(material.get(ListView_myLibrary.KEY_DELETE));
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonAction(v.getTag().toString(), "Download");
                }
            });
        }

        ratingAvgNum.setText("" + material.get(ListView_myLibrary.KEY_RATING));
        ratingStars.setRating(Float.parseFloat("" + material.get(ListView_myLibrary.KEY_RATING)));
        totalNum.setText(material.get(ListView_myLibrary.KEY_TOTALNUM_RATING));
        femalerating.setProgress(Integer.parseInt("1"));
        //femalerating.setProgress(Integer.parseInt(material.get(ListView_myLibrary.KEY_FEMALE_RATING)));
        malerating.setProgress(Integer.parseInt("1"));
        //malerating.setProgress(Integer.parseInt(material.get(ListView_myLibrary.KEY_MALE_RATING)));
        imageLoader.DisplayImage(material.get(ListView_myLibrary.KEY_THUMB_URL), thumb_image);
        return vi;
    }

    public void buttonAction(String resourceId, String action) {
        switch (action) {
            case "Delete":
                break;
            case "Details":
                break;
            case "Feedback":
                break;
            case "Open":
                if (user_dashboard.openResources(resourceId)) {
                    Log.i(TAG, "Open Clicked ********** " + resourceId);
                } else {
                    Log.i(TAG, "Open  ********** " + resourceId);
                }
                break;
            case "Download":
                if (user_dashboard.downloadResources(resourceId)) {
                    Log.i(TAG, "Download Clicked ********** " + resourceId);
                } else {
                    Log.i(TAG, "Download  ********** " + resourceId);
                }
                break;
        }
    }
}