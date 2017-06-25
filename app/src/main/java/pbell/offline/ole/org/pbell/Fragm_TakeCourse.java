package pbell.offline.ole.org.pbell;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import us.feras.mdv.MarkdownView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragm_TakeCourse.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragm_TakeCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragm_TakeCourse extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private static final String TAG = "MYAPP";
    CouchViews chViews = new CouchViews();
    String resourceIdList[], resourceTitleList[],courseIdList[],courseTitleList[];
    int rsLstCnt, csLstCnt = 0;
    private List<String> resIDArrayList = new ArrayList<String>();
    private List<String> courseIDArrayList = new ArrayList<String>();


    ///////////////////////////////////////////

    private String crs_MainTId, crs_MainTitle,crs_MainName,crs_MainDescription, crs_MainMethod ,crs_MainGradeLevel,crs_MainLocation ="";
    private List<String> crs_StepTitles = new ArrayList<String>();
    private List<String> crs_StepsDescription = new ArrayList<String>();
    private List<Integer> crs_StepsNumOfResources = new ArrayList<Integer>();
    private List<Integer> crs_StepsNumOfQuestions = new ArrayList<Integer>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Fragm_TakeCourse() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragm_TakeCourse.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragm_TakeCourse newInstance(String param1, String param2) {
        Fragm_TakeCourse fragment = new Fragm_TakeCourse();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragm_course_information, container, false);
        String description = "1. First ordered list item\n" +
                "2. Another item\n" +
                "⋅⋅* Unordered sub-list. \n" +
                "1. Actual numbers don't matter, just that it's a number\n" +
                "⋅⋅1. Ordered sub-list\n" +
                "4. And another item.\n" +
                "\n" +
                "⋅⋅⋅#You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).\n" +
                "\n" +
                "⋅⋅⋅To have a line break without a paragraph, you will need to use two trailing spaces.⋅⋅\n" +
                "⋅⋅⋅Note that this line is separate, but within the same paragraph.⋅⋅\n" +
                "### (This is contrary to the typical GFM line break behaviour, where trailing spaces are not required.)\n" +
                "\n" +
                "* Unordered list can use asterisks\n" +
                "- Or minuses\n" +
                "+ Or pluses";
        MarkdownView markdownView = (MarkdownView) view.findViewById(R.id.markdownView);
        markdownView.loadMarkdown(description);
        loadCourseIntoArray(mParam1);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void loadCourseIntoArray(String courseId){
                //maximus
        try{
            AndroidContext androidContext = new AndroidContext(getContext());
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database course_db = manager.getExistingDatabase("courses");
            Document doc = course_db.getExistingDocument(courseId);
            Map<String, Object> properties = doc.getProperties();
            crs_MainTId = ((String) properties.get("CourseTitle"));
            crs_MainTitle = ((String) properties.get("CourseTitle"));
            crs_MainName= ((String) properties.get("name"));
            crs_MainDescription = ((String) properties.get("description"));
            crs_MainMethod = ((String) properties.get("method"));
            crs_MainGradeLevel = ((String) properties.get("gradeLevel"));
            crs_MainLocation= ((String) properties.get("location"));

            //// Get Steps
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database coursestep_Db = manager.getExistingDatabase("coursestep");
           /// Query orderedQuery = chViews.ReadCourseSteps(coursestep_Db).createQuery();
            Query orderedQuery = chViews.ReadCourseStepsByCourseID(coursestep_Db,crs_MainTId).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            int courseStepsCounter = 0;
            for (Iterator<QueryRow> item = results; item.hasNext(); ) {
                QueryRow row = item.next();
                String docId = (String) row.getValue();
                Document stepdoc = coursestep_Db.getExistingDocument(docId);
                Map<String, Object> coursestep_properties = stepdoc.getProperties();
                /*if (courseId.equals((String) coursestep_properties.get("courseId"))) {
                              /*  ArrayList course_step_resourceId = (ArrayList) coursestep_properties.get("resourceId");
                                ArrayList course_step_resourceTitles = (ArrayList) coursestep_properties.get("resourceTitles");
                                String course_step_title = (String) coursestep_properties.get("title");
                                String course_step_id = (String) coursestep_properties.get("_id");
                                String course_step_descr = (String) coursestep_properties.get("description");
                                int course_step_No = (Integer) coursestep_properties.get("step");*/
                    Log.e(TAG, "Course Step title " + ((String) coursestep_properties.get("title")) + " ");
                    courseStepsCounter++;
                //}*/
            }

            HashMap<String, String> map = new HashMap<String, String>();
            //map.put(KEY_ID, ((String) properties.get("_id")));
            //map.put(KEY_TITLE, ((String) properties.get("CourseTitle")));
            Log.e(TAG, "Course item title " + ((String) properties.get("CourseTitle")) + " ");
            String desc = (String) properties.get("description");
            if (desc.length() > 100) {
                desc = desc.substring(0, 100);
            }
            String dateFrom, dateTo, timeFrom, timeTo, buildDescription;
            try {
                dateFrom = ((String) properties.get("startDate") == "") ? "" : ("Date : " + ((String) properties.get("startDate")));
            } catch (Exception err) {
                dateFrom = "";
            }
            try {
                dateTo = ((String) properties.get("endDate") == "") ? "" : (" - " + ((String) properties.get("endDate")));
            } catch (Exception err) {
                dateTo = "";
            }
            try {
                timeFrom = ((String) properties.get("startTime") == "") ? "" : ("       Time : " + ((String) properties.get("startTime")));
            } catch (Exception err) {
                timeFrom = "";
            }
            try {
                timeTo = ((String) properties.get("endTime") == "") ? "" : (" - " + ((String) properties.get("endTime")));
            } catch (Exception err) {
                timeTo = "";
            }
            buildDescription = "Description : " + desc + " \n" +
                    "Number Of Steps : " + courseStepsCounter + "  " + dateFrom + dateTo + timeFrom + timeTo + "\n";

           /* map.put(KEY_DESCRIPTION, buildDescription);
            map.put(KEY_DETAILS, ((String) properties.get("_id")));
            map.put(KEY_FEEDBACK, ((String) properties.get("_id")));
            map.put(KEY_DELETE, ((String) properties.get("_id")));
            map.put(KEY_RATING, (((String) properties.get("averageRating")) == "") ? "2.2" : (String) properties.get("averageRating"));
            map.put(KEY_TOTALNUM_RATING, "Rating  (" + properties.get("averageRating") + ")");
            map.put(KEY_FEMALE_RATING, "");
            map.put(KEY_MALE_RATING, "");
            ///map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));
            Database local_downloaded_courses = manager.getDatabase("offline_courses");
            Document local_downloaded_doc = local_downloaded_courses.getExistingDocument((String) properties.get("_id"));
            if (local_downloaded_doc != null) {
                map.put(KEY_RESOURCE_STATUS, "downloaded");
            } else {
                map.put(KEY_RESOURCE_STATUS, "not downloaded");
            }
            materialList.add(map);
            rsLstCnt++;
            csLstCnt++;*/
            coursestep_Db.close();


            course_db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
