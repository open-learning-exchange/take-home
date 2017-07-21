package pbell.offline.ole.org.pbell;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

@SuppressWarnings("ALL")
public class CourceListCustomAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<CourseList> resourceItems;
    public Resources res;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    public CourceListCustomAdapter(Activity activity, List<CourseList> resourceItems) {
        this.activity = activity;
        this.resourceItems = resourceItems;
    }

    @Override
    public int getCount() {
        return resourceItems.size();
    }

    @Override
    public Object getItem(int location) {
        return resourceItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        // NetworkImageView thumbNail = (NetworkImageView) convertView.findViewById(R.id.thumbnail);
        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView rating = (TextView) convertView.findViewById(R.id.rating);
        TextView genre = (TextView) convertView.findViewById(R.id.genre);
        TextView year = (TextView) convertView.findViewById(R.id.releaseYear);

        // getting resource data for the row
        CourseList r = resourceItems.get(position);

        // thumbnail image
        thumbNail.setBackgroundResource(r.getThumbnailUrl());
        //thumbNail.setImageUrl(r.getThumbnailUrl(), imageLoader);

        // title
        title.setText(r.getTitle());

        // rating
        rating.setText("" + String.valueOf(r.getDescription()));

        // genre
        String genreStr = "";
        genreStr = genreStr.length() > 0 ? genreStr.substring(0,
                genreStr.length() - 2) : genreStr;
        genre.setText(genreStr);

        // release Rating
        year.setText("End date : " + String.valueOf(r.getRating()));

        return convertView;
    }

}