package pbell.offline.ole.org.pbell;

import android.app.Dialog;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String ARG_PARAM1 = "sys_usercouchId";
    private static final String ARG_PARAM2 = "courseId";


    private static final String TAG = "MYAPP";
    CouchViews chViews = new CouchViews();
    String resourceIdList[], resourceTitleList[], courseIdList[], courseTitleList[];
    int rsLstCnt, csLstCnt = 0;
    private List<String> resIDArrayList = new ArrayList<>();
    private List<String> courseIDArrayList = new ArrayList<>();
    Dialog dialogTest;

    ///////////////////////////////////////////

    private String crs_MainTId, crs_MainTitle, crs_MainName, crs_MainDescription, crs_MainMethod, crs_MainGradeLevel, crs_MainLocation = "";
    private List<String> crs_StepIds = new ArrayList<>();
    private List<String> crs_tempStepIds = new ArrayList<>();
    private List<String> crs_StepTitles = new ArrayList<>();
    private List<String> crs_tempStepTitles = new ArrayList<>();
    private List<String> crs_StepDescription = new ArrayList<>();
    private List<String> crs_tempStepDescription = new ArrayList<>();
    private List<String> crs_StepSeqNum = new ArrayList<>();
    private List<String> crs_tempStepSeqNum = new ArrayList<>();
    private List<String> crs_tempAidStepSeqNum = new ArrayList<>();
    private List<Integer> crs_StepNumOfResources = new ArrayList<>();
    private List<Integer> crs_tempStepNumOfResources = new ArrayList<>();
    private List<Integer> crs_StepNumOfQuestions = new ArrayList<>();
    private List<Integer> crs_tempStepNumOfQuestions = new ArrayList<>();
    int crs_NumberOfSteps, stepCurrentIndex = 0;


    //////Step Questions Items /
    List<String> qn_Ids = new ArrayList<>();
    List<String> qn_Type = new ArrayList<>();
    List<String> qn_Statement = new ArrayList<>();
    List<String> qn_Marks = new ArrayList<>();
    List<String> qn_CorrectAnswer = new ArrayList<>();
    List<Integer> qn_NoOptions = new ArrayList<>();
    List<String[]> qn_StepOptions = new ArrayList<>();

    MarkdownView markdownQueDescContent;
    EditText txt_QueSinglelineAns, txt_QueMultilineAns;
    LinearLayout lt_QueSinglelineHolder, lt_QueMultilineHolder, lt_QueMultipleChoiceHolder;
    TextView lbl_QueStepTitle, lbl_QueStatus;
    int quesionCurrentIndex, totalNumOfQuestions = 0;
    Button btnQueNext, btnQueBack;
    CheckBox qn_OptionsCheckbox[];

    ///// Resource Details
    List<String> rs_Ids = new ArrayList<>();
    List<String> rs_OpenWith = new ArrayList<>();
    List<String> rs_Title = new ArrayList<>();
    List<String> rs_Description = new ArrayList<>();

    LinearLayout lt_ResListHolder;
    TextView lbl_ResStepTitle;
    TextView rs_ResourceTextView[];

    // TODO: Rename and change types of parameters
    private String mCourseId;
    private String mMemberId;

    //// Course Steps Items
    MarkdownView markdownCourseDescContent;
    Button btnBack, btnNext, btnTakeTest, btnResources;
    TextView lblCourseTitle, lblStepTitle, lblCourseStepStatus;

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
            mMemberId = getArguments().getString(ARG_PARAM1);
            mCourseId = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragm_take_course, container, false);
       /* String description = "1. First ordered list item\n" +
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
                "+ Or pluses";*/
        markdownCourseDescContent = (MarkdownView) view.findViewById(R.id.markdownCourseDescriptionContent);
        markdownCourseDescContent.loadMarkdown("");

        btnBack = (Button) view.findViewById(R.id.btnCourseBack);
        btnNext = (Button) view.findViewById(R.id.btnCourseNext);
        btnResources = (Button) view.findViewById(R.id.btnCourseResources);
        btnTakeTest = (Button) view.findViewById(R.id.btnCourseTakeTest);
        lblCourseTitle = (TextView) view.findViewById(R.id.lbl_CourseTitle);
        lblStepTitle = (TextView) view.findViewById(R.id.lbl_CourseStepTitle);
        lblCourseStepStatus = (TextView) view.findViewById(R.id.lbl_CourseStepStatus);

        loadCourseIntoArray(mCourseId);

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void loadCourseIntoArray(String courseId) {
        try {
            Log.e(TAG, "Take Course " + " " + courseId);
            AndroidContext androidContext = new AndroidContext(getContext());
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database course_db = manager.getExistingDatabase("courses");
            Document doc = course_db.getExistingDocument(courseId);
            Map<String, Object> properties = doc.getProperties();
            crs_MainTId = ((String) properties.get("CourseTitle"));
            crs_MainTitle = ((String) properties.get("CourseTitle"));
            crs_MainName = ((String) properties.get("name"));
            crs_MainDescription = ((String) properties.get("description"));
            crs_MainMethod = ((String) properties.get("method"));
            crs_MainGradeLevel = ((String) properties.get("gradeLevel"));
            crs_MainLocation = ((String) properties.get("location"));
            //// Get Steps
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database coursestep_Db = manager.getExistingDatabase("coursestep");
            Query orderedQuery = chViews.ReadCourseSteps(coursestep_Db).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            crs_NumberOfSteps = 0;
            for (Iterator<QueryRow> item = results; item.hasNext(); ) {
                QueryRow row = item.next();
                String docId = (String) row.getValue();
                Document stepdoc = coursestep_Db.getExistingDocument(docId);
                Map<String, Object> coursestep_properties = stepdoc.getProperties();
                if (((String) coursestep_properties.get("courseId")).equalsIgnoreCase(courseId)) {
                    Log.e(TAG, "Take Course Step - " + " " + (String) coursestep_properties.get("_id"));
                    crs_tempStepIds.add((String) coursestep_properties.get("_id"));
                    crs_tempStepTitles.add((String) coursestep_properties.get("title"));
                    crs_tempStepDescription.add((String) coursestep_properties.get("description"));
                    crs_tempStepSeqNum.add((String) coursestep_properties.get("step"));
                    crs_tempAidStepSeqNum.add((String) coursestep_properties.get("step"));
                    crs_StepSeqNum.add((String) coursestep_properties.get("step"));
                    ArrayList course_Questions;
                    try {
                        course_Questions = (ArrayList) coursestep_properties.get("questionslist");
                        Log.e(TAG, "Questions " + course_Questions.size() + " ");
                        crs_tempStepNumOfQuestions.add(course_Questions.size());
                    } catch (Exception err) {
                        crs_tempStepNumOfQuestions.add(0);
                        err.printStackTrace();
                    }
                    ArrayList course_Resource;
                    try {
                        course_Resource = (ArrayList) coursestep_properties.get("resourceId");
                        crs_tempStepNumOfResources.add(course_Resource.size());
                    } catch (Exception err) {
                        crs_tempStepNumOfResources.add(0);
                        err.printStackTrace();
                    }

                    crs_NumberOfSteps++;
                }
            }
            lblCourseTitle.setText(crs_MainTId);
            coursestep_Db.close();
            course_db.close();

            //Log.e(TAG, " SeqNum was " + crs_tempStepSeqNum.toString() +" - "+ crs_tempStepTitles);
            // Rearrange steps in the right order according to SeqNum  ////
            for (int x = 0; x < crs_tempStepSeqNum.size(); x++) {
                if ((x + 1) < crs_tempStepSeqNum.size()) {
                    if (Integer.parseInt(crs_tempStepSeqNum.get(x)) > Integer.parseInt(crs_tempStepSeqNum.get(x + 1))) {
                        crs_tempStepSeqNum.set(x, crs_tempStepSeqNum.get(x + 1));
                        crs_tempStepSeqNum.set(x + 1, crs_tempAidStepSeqNum.get(x));
                        crs_tempAidStepSeqNum.clear();
                        crs_tempAidStepSeqNum.addAll(crs_tempStepSeqNum);
                        x = 0;
                    }
                }
            }
            // Add in the right order the arraylist to all items
            for (int x = 0; x < crs_tempStepSeqNum.size(); x++) {
                int indexItem = crs_StepSeqNum.indexOf(crs_tempStepSeqNum.get(x));
                crs_StepIds.add(crs_tempStepIds.get(indexItem));
                crs_StepTitles.add(crs_tempStepTitles.get(indexItem));
                crs_StepDescription.add(crs_tempStepDescription.get(indexItem));
                crs_StepNumOfResources.add(crs_tempStepNumOfResources.get(indexItem));
                crs_StepNumOfQuestions.add(crs_tempStepNumOfQuestions.get(indexItem));
            }
            //Log.e(TAG, " SeqNum is now " + crs_tempStepSeqNum.toString() +"  - "+ crs_StepTitles);
            stepUILoader(crs_StepIds.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stepUILoader(String StepId) {

        Log.e(TAG, "Step ID " + StepId + " -  Course " + crs_MainTId);

        stepCurrentIndex = crs_StepIds.indexOf(StepId);
        if (stepCurrentIndex < crs_StepIds.size() - 1) {
            btnNext.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.GONE);
        }
        if (stepCurrentIndex > 0) {
            btnBack.setVisibility(View.VISIBLE);
        } else {
            btnBack.setVisibility(View.GONE);
        }
        if (crs_StepNumOfQuestions.get(stepCurrentIndex) > 0) {
            btnTakeTest.setVisibility(View.VISIBLE);
        } else {
            btnTakeTest.setVisibility(View.GONE);
        }
        if (crs_StepNumOfResources.get(stepCurrentIndex) > 0) {
            btnResources.setVisibility(View.VISIBLE);
        } else {
            btnResources.setVisibility(View.GONE);
        }


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stepUILoader(crs_StepIds.get(stepCurrentIndex - 1));
                } catch (Exception except) {
                    Log.d(TAG, "Back clicked error " + except.getMessage());
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stepUILoader(crs_StepIds.get(stepCurrentIndex + 1));
                } catch (Exception except) {
                    Log.d(TAG, "Back clicked error " + except.getMessage());
                }
            }
        });

        btnTakeTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    takeTestDialog(crs_StepIds.get(stepCurrentIndex), crs_StepTitles.get(stepCurrentIndex));
                } catch (Exception except) {
                    Log.d(TAG, "TakeTest clicked error  " + except.getMessage());
                }
            }
        });
        btnResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showResourcesDialog(crs_StepIds.get(stepCurrentIndex), crs_StepTitles.get(stepCurrentIndex));
                } catch (Exception except) {
                    Log.d(TAG, "Resource clicked error " + except.getMessage());
                }
            }
        });
        lblCourseStepStatus.setText("Step " + (stepCurrentIndex + 1) + " of " + crs_NumberOfSteps);

        lblStepTitle.setText(crs_StepTitles.get(stepCurrentIndex));
        markdownCourseDescContent.loadMarkdown(crs_StepDescription.get(stepCurrentIndex));
    }


    public void takeTestDialog(String StepId, String StepTitle) {
        qn_Ids.clear();
        qn_Type.clear();
        qn_Statement.clear();
        qn_Marks.clear();
        qn_CorrectAnswer.clear();
        qn_NoOptions.clear();
        ArrayList course_QuestionList = null;
        try {
            AndroidContext androidContext = new AndroidContext(getContext());
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database coursestep_Db = manager.getExistingDatabase("coursestep");
            Document stepdoc = coursestep_Db.getExistingDocument(StepId);
            Map<String, Object> coursestep_properties = stepdoc.getProperties();
            course_QuestionList = (ArrayList) coursestep_properties.get("questionslist");
            for (int x = 0; x < course_QuestionList.size(); x++) {
                Database stepQuestion_Db = manager.getExistingDatabase("coursequestion");
                Document questionDoc = stepQuestion_Db.getExistingDocument(course_QuestionList.get(x).toString());
                Map<String, Object> question_properties = questionDoc.getProperties();
                qn_Ids.add((String) question_properties.get("_id"));
                qn_Type.add((String) question_properties.get("Type"));
                qn_Statement.add((String) question_properties.get("Statement"));
                qn_Marks.add((String) question_properties.get("Marks"));

                if (((String) question_properties.get("Type")).equalsIgnoreCase("Multiple Choice")) {
                    qn_CorrectAnswer.add((String) question_properties.get("correctAnswer"));
                    try {
                        ArrayList tempHolder = (ArrayList) question_properties.get("Options");
                        qn_StepOptions.add(new String[]{android.text.TextUtils.join(",", tempHolder)});
                        qn_NoOptions.add(tempHolder.size());
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                } else if (((String) question_properties.get("Type")).equalsIgnoreCase("Attachment")) {
                    qn_NoOptions.add(0);
                } else if (((String) question_properties.get("Type")).equalsIgnoreCase("Comment/Essay Box")) {
                    qn_NoOptions.add(0);
                } else {
                    /// Textbox
                    qn_NoOptions.add(0);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(getActivity());
        dialogB2.setView(R.layout.dialog_course_step_questions);
        dialogB2.setCancelable(true);
        dialogTest = dialogB2.create();
        dialogTest.show();
        try {
            lbl_QueStepTitle = (TextView) dialogTest.findViewById(R.id.lbl_QueTopTitle);
            lbl_QueStepTitle.setText("Step : " + StepTitle);
            lbl_QueStatus = (TextView) dialogTest.findViewById(R.id.lbl_QueStatus);
            lbl_QueStatus.setText("1 / " + course_QuestionList.size());

            markdownQueDescContent = (MarkdownView) dialogTest.findViewById(R.id.markdownQueDescriptionContent);
            markdownQueDescContent.loadMarkdown("");
            txt_QueSinglelineAns = (EditText) dialogTest.findViewById(R.id.txt_QueSingleLine);
            txt_QueMultilineAns = (EditText) dialogTest.findViewById(R.id.txt_QueMultiline);
            lt_QueSinglelineHolder = (LinearLayout) dialogTest.findViewById(R.id.lt_QueSinglelineHolder);
            lt_QueMultilineHolder = (LinearLayout) dialogTest.findViewById(R.id.lt_QueMultilineHolder);
            lt_QueMultipleChoiceHolder = (LinearLayout) dialogTest.findViewById(R.id.lt_QueMultipleChoiceHolder);
            btnQueNext = (Button) dialogTest.findViewById(R.id.btn_QueNext);
            btnQueBack = (Button) dialogTest.findViewById(R.id.btn_QueBack);


        } catch (Exception err) {
            err.printStackTrace();
        }
        QuestionUILoader(qn_Ids.get(0), course_QuestionList.size());
    }

    public void QuestionUILoader(String questionID, int numberOfQuestions) {
        totalNumOfQuestions = numberOfQuestions;
        quesionCurrentIndex = qn_Ids.indexOf(questionID);

        if (quesionCurrentIndex < qn_Ids.size() - 1) {
            btnQueNext.setVisibility(View.VISIBLE);
        } else {
            btnQueNext.setVisibility(View.GONE);
        }

        if (quesionCurrentIndex > 0) {
            btnQueBack.setVisibility(View.VISIBLE);
        } else {
            btnQueBack.setVisibility(View.GONE);
        }

        btnQueNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    QuestionUILoader(qn_Ids.get(quesionCurrentIndex + 1), totalNumOfQuestions);
                } catch (Exception except) {
                    Log.d(TAG, "Next clicked error " + except.getMessage());
                }
            }
        });
        btnQueBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    QuestionUILoader(qn_Ids.get(quesionCurrentIndex - 1), totalNumOfQuestions);
                } catch (Exception except) {
                    Log.d(TAG, "Back clicked error " + except.getMessage());
                }
            }
        });

        lt_QueMultilineHolder = (LinearLayout) dialogTest.findViewById(R.id.lt_QueMultilineHolder);
        lbl_QueStatus.setText((quesionCurrentIndex + 1) + " of " + numberOfQuestions);
        markdownQueDescContent.loadMarkdown(qn_Statement.get(quesionCurrentIndex));
        lt_QueMultipleChoiceHolder.setVisibility(View.GONE);
        lt_QueMultilineHolder.setVisibility(View.GONE);
        lt_QueSinglelineHolder.setVisibility(View.GONE);

        if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Multiple Choice")) {
            lt_QueMultipleChoiceHolder.setVisibility(View.VISIBLE);
            if (((LinearLayout) lt_QueMultipleChoiceHolder).getChildCount() > 0) {
                ((LinearLayout) lt_QueMultipleChoiceHolder).removeAllViews();
            }
            String tempOptionsHolder = qn_StepOptions.get(quesionCurrentIndex)[0];
            List<String> items = Arrays.asList(tempOptionsHolder.split("\\s*,\\s*"));
            qn_OptionsCheckbox = new CheckBox[items.size()];
            for (int x = 0; x < items.size(); x++) {
                //Log.e(TAG, "Loop No " + quesionCurrentIndex + " " + items.get(x));
                qn_OptionsCheckbox[x] = new CheckBox(getContext());
                qn_OptionsCheckbox[x].setId(x);
                qn_OptionsCheckbox[x].setTag(x);
                qn_OptionsCheckbox[x].setText(items.get(x));
                lt_QueMultipleChoiceHolder.addView(qn_OptionsCheckbox[x]);
            }
        } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Comment/Essay Box")) {
            lt_QueMultilineHolder.setVisibility(View.VISIBLE);
        } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Textbox")) {
            lt_QueSinglelineHolder.setVisibility(View.VISIBLE);
        } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Attachment")) {

        }
    }


    public void showResourcesDialog(String StepId, String StepTitle) {
        rs_Ids.clear();
        rs_OpenWith.clear();
        rs_Description.clear();
        rs_Title.clear();
        ArrayList coursestep_ResourceList = null;
        try {
            AndroidContext androidContext = new AndroidContext(getContext());
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database coursestep_Db = manager.getExistingDatabase("coursestep");
            Document stepdoc = coursestep_Db.getExistingDocument(StepId);
            Map<String, Object> coursestep_properties = stepdoc.getProperties();
            coursestep_ResourceList = (ArrayList) coursestep_properties.get("resourceId");
            for (int x = 0; x < coursestep_ResourceList.size(); x++) {
                Database course_resources_Db = manager.getExistingDatabase("offline_course_resources");
                Document courseResouceDoc = course_resources_Db.getExistingDocument(coursestep_ResourceList.get(x).toString());
                Map<String, Object> question_properties = courseResouceDoc.getProperties();
                rs_Ids.add((String) question_properties.get("_id"));
                rs_OpenWith.add((String) question_properties.get("openWith"));
                rs_Title.add((String) question_properties.get("title"));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        AlertDialog.Builder dialogB2 = new AlertDialog.Builder(getActivity());
        dialogB2.setView(R.layout.dialog_course_step_resources);
        dialogB2.setCancelable(true);
        dialogTest = dialogB2.create();
        dialogTest.show();
        try {
            lbl_ResStepTitle = (TextView) dialogTest.findViewById(R.id.lbl_ResTopTitle);
            lbl_ResStepTitle.setText("Step : " + StepTitle);
            lt_ResListHolder = (LinearLayout) dialogTest.findViewById(R.id.lbl_ResListHolder);
            rs_ResourceTextView = new TextView[rs_Ids.size()];
            for (int x = 0; x < rs_Ids.size(); x++) {
                rs_ResourceTextView[x].setTag(rs_Ids.get(x));
                rs_ResourceTextView[x].setText(rs_Title.get(x));
                lt_ResListHolder.addView(rs_ResourceTextView[x]);
            }

        } catch (Exception err) {
            err.printStackTrace();
        }

       /// QuestionUILoader(qn_Ids.get(0), course_QuestionList.size());

    }



}
