package org.ole.learning.planet.planetlearning;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.marcoscg.dialogsheet.DialogSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
public class Fragm_TakeCourse extends OpenResource {
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
    Dialog dialogTest, dialogResources;
    private ProgressDialog mDialog;
    String openedResourceId, openedResourceTitle;
    /////////////////////////////
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    String sys_oldSyncServerURL, sys_username, sys_lastSyncDate,
            sys_password, sys_usercouchId, sys_userfirstname, sys_userlastname,
            sys_usergender, sys_uservisits, sys_servername, sys_serverversion = "";
    Boolean sys_singlefilestreamdownload, sys_multiplefilestreamdownload;
    int sys_uservisits_Int=0;

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
    List<ArrayList> qn_CorrectAnswer = new ArrayList<>();
    List<Integer> qn_NoOptions = new ArrayList<>();
    List<String[]> qn_StepOptions = new ArrayList<>();

    MarkdownView markdownQueDescContent;
    EditText txt_QueSinglelineAns, txt_QueMultilineAns;
    LinearLayout lt_QueSinglelineHolder, lt_QueMultilineHolder, lt_QueMultipleChoiceHolder;
    TextView lbl_QueStepTitle, lbl_QueStatus;
    int quesionCurrentIndex, totalNumOfQuestions = 0;
    Button btnQueSubmitAns, btnQueBack;
    CheckBox qn_OptionsCheckbox[];

    ///// Resource Details
    List<String> rs_Ids = new ArrayList<>();
    List<String> rs_OpenWith = new ArrayList<>();
    List<String> rs_Title = new ArrayList<>();
    List<String> rs_Description = new ArrayList<>();
    List<Boolean> stepQuizResultHolder = new ArrayList<>();
    List<String> stepTextResultHolder = new ArrayList<>();
    TextView lbl_ResStepTitle;

    int points = 0;
    String scoreMessage = "";
    private String mCourseId;
    private String mMemberId;

    //// Course Steps Items
    MarkdownView markdownCourseDescContent;
    Button btnBack, btnNext, btnTakeTest, btnResources;
    TextView lblCourseTitle, lblStepTitle, lblCourseStepStatus;

    LogHouse logHouse = new LogHouse();

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
        markdownCourseDescContent = view.findViewById(R.id.markdownCourseDescriptionContent);
        markdownCourseDescContent.loadMarkdown("");

        btnBack = view.findViewById(R.id.btnCourseBack);
        btnNext = view.findViewById(R.id.btnCourseNext);
        btnResources = view.findViewById(R.id.btnCourseResources);
        btnTakeTest =  view.findViewById(R.id.btnCourseTakeTest);
        lblCourseTitle =  view.findViewById(R.id.lbl_CourseTitle);
        lblStepTitle =  view.findViewById(R.id.lbl_CourseStepTitle);
        lblCourseStepStatus =  view.findViewById(R.id.lbl_CourseStepStatus);


        btnBack.setVisibility(View.VISIBLE);
        btnBack.setText("< "+getResources().getString( R.string.bbtext_Previous));
        btnBack.setShadowLayer(5f, -2, 1, Color.BLACK);
        btnNext.setVisibility(View.VISIBLE);
        btnNext.setText(getResources().getString(R.string.bbtext_Next)+" >");
        btnNext.setShadowLayer(5f, -2, 1, Color.BLACK);

        btnResources.setVisibility(View.VISIBLE);
        btnResources.setShadowLayer(5f, -2, 1, Color.BLACK);
        btnTakeTest.setVisibility(View.VISIBLE);
        btnTakeTest.setShadowLayer(5f, -2, 1, Color.BLACK);

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
        void onResourceOpened(String resourceId, String resourceTitle);
    }

    public void loadCourseIntoArray(String courseId) {
        try {
            restorePref();
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
            orderedQuery.setDescending(false);
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
                    crs_tempStepSeqNum.add(String.valueOf(coursestep_properties.get("step")));
                    crs_tempAidStepSeqNum.add(String.valueOf( coursestep_properties.get("step")));
                    crs_StepSeqNum.add(String.valueOf( coursestep_properties.get("step")));
                    List course_Questions;
                    try {
                        Log.e(TAG, "Questions " + coursestep_properties.get("questionslist") + " ");
                        course_Questions = (ArrayList) coursestep_properties.get("questionslist");
                        if(course_Questions!=null){
                            crs_tempStepNumOfQuestions.add(course_Questions.size());
                        }else{
                            crs_tempStepNumOfQuestions.add(0);
                        }
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
            // Add in the right order the arraylist to all items
            for (int x = 0; x < crs_tempStepSeqNum.size(); x++) {
                int indexItem = crs_StepSeqNum.indexOf(crs_tempStepSeqNum.get(x));
                crs_StepIds.add(crs_tempStepIds.get(indexItem));
                crs_StepTitles.add(crs_tempStepTitles.get(indexItem));
                crs_StepDescription.add(crs_tempStepDescription.get(indexItem));
                crs_StepNumOfResources.add(crs_tempStepNumOfResources.get(indexItem));
                crs_StepNumOfQuestions.add(crs_tempStepNumOfQuestions.get(indexItem));
            }
            stepUILoader(crs_StepIds.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stepUILoader(String StepId) {


        Log.e(TAG, "Step ID " + StepId + " -  Course " + crs_MainTId);

        stepCurrentIndex = crs_StepIds.indexOf(StepId);
        if (stepCurrentIndex < crs_StepIds.size() - 1) {
            btnNext.setClickable(true);
            btnNext.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
            //btnNext.setVisibility(View.VISIBLE);
        } else {
            btnNext.setClickable(false);
            btnNext.setTextColor(ContextCompat.getColor(getContext(),R.color.ole_ash));
            //btnNext.setVisibility(View.GONE);
        }
        if (stepCurrentIndex > 0) {
            btnBack.setClickable(true);
            btnBack.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
            //btnBack.setVisibility(View.VISIBLE);
        } else {
            btnBack.setClickable(false);
            btnBack.setTextColor(ContextCompat.getColor(getContext(),R.color.ole_ash));
           // btnBack.setVisibility(View.GONE);
        }
        if (crs_StepNumOfQuestions.get(stepCurrentIndex) > 0) {
            btnTakeTest.setClickable(true);
            btnTakeTest.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
            //btnTakeTest.setVisibility(View.VISIBLE);

        } else {
            btnTakeTest.setClickable(false);
            btnTakeTest.setTextColor(ContextCompat.getColor(getContext(),R.color.ole_ash));
            //btnTakeTest.setVisibility(View.GONE);
        }
        if (crs_StepNumOfResources.get(stepCurrentIndex) > 0) {
            btnResources.setClickable(true);
            btnResources.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
            //btnResources.setVisibility(View.VISIBLE);
        } else {
            btnResources.setClickable(false);
            btnResources.setTextColor(ContextCompat.getColor(getContext(),R.color.ole_ash));
            //btnResources.setVisibility(View.GONE);
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
                if(crs_StepNumOfQuestions.get(stepCurrentIndex) > 0) {
                    try {
                        Log.d(TAG, "TakeTest clicked");
                        takeTestDialog(crs_StepIds.get(stepCurrentIndex), crs_StepTitles.get(stepCurrentIndex));
                    } catch (Exception except) {
                        Log.d(TAG, "TakeTest clicked error  " + except.getMessage());
                        except.printStackTrace();
                    }
                }
            }
        });
        btnResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(crs_StepNumOfResources.get(stepCurrentIndex)>0) {
                    try {
                        showResourcesDialog(crs_StepIds.get(stepCurrentIndex), crs_StepTitles.get(stepCurrentIndex));
                    } catch (Exception except) {
                        Log.d(TAG, "Resource clicked error " + except.getMessage());
                    }
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
        stepQuizResultHolder.clear();
        stepTextResultHolder.clear();
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
                    qn_CorrectAnswer.add((ArrayList) question_properties.get("CorrectAnswer"));
                    try {
                        ArrayList tempHolder = (ArrayList) question_properties.get("Options");
                        Log.e(TAG, "Multiple choice questions list " + tempHolder);
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
            btnQueSubmitAns = (Button) dialogTest.findViewById(R.id.btn_QueSubmitAns);
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
            btnQueSubmitAns.setText(R.string.submit);
            btnQueSubmitAns.setVisibility(View.VISIBLE);
        } else {
            btnQueSubmitAns.setText(R.string.finish);
            btnQueSubmitAns.setVisibility(View.VISIBLE);
            ///btnQueSubmitAns.setVisibility(View.GONE);
        }

        if (quesionCurrentIndex > 0) {
            btnQueBack.setVisibility(View.GONE);

        } else {
            btnQueBack.setVisibility(View.GONE);
        }

        btnQueSubmitAns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (CheckAnsBeforeNext(qn_Ids.get(quesionCurrentIndex), totalNumOfQuestions)) {
                        if ((quesionCurrentIndex + 1) == totalNumOfQuestions) {
                            Log.d(TAG, "Last question ");
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    getSavePoints(crs_StepIds.get(stepCurrentIndex), stepCurrentIndex);
                                }
                            }, 500);
                        } else {
                            QuestionUILoader(qn_Ids.get(quesionCurrentIndex + 1), totalNumOfQuestions);
                        }
                    }

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

        lt_QueMultilineHolder = dialogTest.findViewById(R.id.lt_QueMultilineHolder);
        lbl_QueStatus.setText((quesionCurrentIndex + 1) + " of " + numberOfQuestions);
        markdownQueDescContent.loadMarkdown(qn_Statement.get(quesionCurrentIndex));
        lt_QueMultipleChoiceHolder.setVisibility(View.GONE);
        lt_QueMultilineHolder.setVisibility(View.GONE);
        lt_QueSinglelineHolder.setVisibility(View.GONE);

        if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Multiple Choice")) {
            lt_QueMultipleChoiceHolder.setVisibility(View.VISIBLE);
            Log.e(TAG,"Multiple choice UI Display "+qn_StepOptions.get(quesionCurrentIndex)[0]);
            if (((LinearLayout) lt_QueMultipleChoiceHolder).getChildCount() > 0) {
                ((LinearLayout) lt_QueMultipleChoiceHolder).removeAllViews();
            }
            String tempOptionsHolder = qn_StepOptions.get(quesionCurrentIndex)[0];
            List<String> items = Arrays.asList(tempOptionsHolder.split("\\s*,\\s*"));
            qn_OptionsCheckbox = new CheckBox[items.size()];
            for (int x = 0; x < items.size(); x++) {
                qn_OptionsCheckbox[x] = new CheckBox(getContext());
                qn_OptionsCheckbox[x].setId(x);
                qn_OptionsCheckbox[x].setTag(x);
                qn_OptionsCheckbox[x].setText(items.get(x));
                lt_QueMultipleChoiceHolder.addView(qn_OptionsCheckbox[x]);
            }
        } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Comment/Essay Box")) {
            lt_QueMultilineHolder.setVisibility(View.VISIBLE);
        } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Single Textbox")) {
            lt_QueSinglelineHolder.setVisibility(View.VISIBLE);
        } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Attachment")) {

        }
    }
    private class AnswerChecker extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
        }
    }
    public boolean CheckAnsBeforeNext(String questionID, int numberOfQuestions) {
        totalNumOfQuestions = numberOfQuestions;
        quesionCurrentIndex = qn_Ids.indexOf(questionID);
        boolean correctAnswrGiven = false;
        //// Save response in local_course_answer
        AndroidContext androidContext = new AndroidContext(getContext());
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database local_member_course_answer = manager.getDatabase("local_member_course_answer");
            Map<String, Object> newCourseAnsProperties = new HashMap<>();
            List<String> ary_Answer = new ArrayList<>();
            ary_Answer.clear();
            if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Multiple Choice")) {
                int noOfOptions = qn_OptionsCheckbox.length;
                for (int x = 0; x < noOfOptions; x++) {
                    if (qn_OptionsCheckbox[x].isChecked()) {
                        ArrayList qCorrectAnsArray = qn_CorrectAnswer.get(quesionCurrentIndex);
                        //save user's selection
                        ary_Answer.add(qn_OptionsCheckbox[x].getText().toString());
                        for (int ca = 0; ca < qCorrectAnsArray.size(); ca++) {
                            //check selection with right answer array
                            if ((qCorrectAnsArray.get(ca) + "").equalsIgnoreCase((qn_OptionsCheckbox[x].getText() + ""))) {
                                correctAnswrGiven = true;
                            } else {
                                if(!qn_Marks.get(quesionCurrentIndex).equalsIgnoreCase("")){
                                    newCourseAnsProperties.put("AttemptMarks",Integer.parseInt(qn_Marks.get(quesionCurrentIndex)));
                                }else {
                                    newCourseAnsProperties.put("AttemptMarks",1);
                                }
                                correctAnswrGiven = false;
                            }
                            Log.e(TAG, qCorrectAnsArray.get(ca) + "");
                        }
                        Log.d(TAG, quesionCurrentIndex + " Size " + qn_CorrectAnswer.get(quesionCurrentIndex) + " Marks " + qn_Marks.get(quesionCurrentIndex));
                    } else {

                    }
                }
                if (correctAnswrGiven) {
                    stepQuizResultHolder.add(correctAnswrGiven);
                    if ((quesionCurrentIndex + 1) != totalNumOfQuestions) {
                        alertDialogOkay("Ques #"+quesionCurrentIndex,"That's the correct answer, well done");
                    }
                    Log.d(TAG, "That's the correct answer, Well Done");

                } else {
                    stepQuizResultHolder.add(correctAnswrGiven);
                    if ((quesionCurrentIndex + 1) == totalNumOfQuestions) {
                        alertDialogOkay("Ques #"+quesionCurrentIndex,"Oops, Wrong answer!! ");
                    }
                    Log.d(TAG, "Oops, Wrong answer!!");
                }
                newCourseAnsProperties.put("kind","courseanswer");
                newCourseAnsProperties.put("pqattempts",getCourseAttempt());
                newCourseAnsProperties.put("QuestionID",qn_Ids.get(quesionCurrentIndex));
                newCourseAnsProperties.put("MemberID",sys_usercouchId);
                newCourseAnsProperties.put("StepID",crs_StepIds.get(stepCurrentIndex));
                newCourseAnsProperties.put("Answer",ary_Answer);
                Document document = local_member_course_answer.createDocument();
                document.putProperties(newCourseAnsProperties);
                Log.e(TAG, "Course Ans DB properties = "+ newCourseAnsProperties);

                stepTextResultHolder.add(null);
                return true;
            } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Comment/Essay Box")) {

                stepQuizResultHolder.add(null);
                stepTextResultHolder.add(txt_QueMultilineAns.getText().toString());
                Log.d(TAG, "Comment/Essay Box");
                txt_QueMultilineAns.setText("");
                return true;
            } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Single Textbox")) {
                stepQuizResultHolder.add(null);
                stepTextResultHolder.add(txt_QueSinglelineAns.getText().toString());
                Log.d(TAG, "Single Textbox");
                txt_QueSinglelineAns.setText("");
                return true;
            } else if (qn_Type.get(quesionCurrentIndex).equalsIgnoreCase("Attachment")) {
                stepQuizResultHolder.add(null);
                stepTextResultHolder.add(null);
                Log.d(TAG, "Attachment");
                return true;
            }
            Log.d(TAG, "Not handled.." + qn_Type.get(quesionCurrentIndex));
            return true;
        }catch(Exception err){
            Log.d(TAG, "Quiz Error.." + err.getMessage());
            err.printStackTrace();
            return false;
        }

    }
    public int getCourseAttempt(){
        AndroidContext androidContext = new AndroidContext(getContext());
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database local_member_course_progress = manager.getDatabase("local_member_course_progress");
            Query orderedQuery = chViews.ReadMemberCourseProgByMemberIdCourceId(local_member_course_progress,mCourseId,sys_usercouchId).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            if(results.getCount()>0) {
                for (Iterator<QueryRow> c_prg = results; c_prg.hasNext(); ) {
                    QueryRow row = c_prg.next();
                    String docId = (String) row.getValue();
                    Document mem_course_prog_doc = local_member_course_progress.getExistingDocument(docId);
                    Map<String, Object> mem_course_prog_properties = new HashMap<>();
                    mem_course_prog_properties.putAll(mem_course_prog_doc.getProperties());
                    return ((List<Integer>) mem_course_prog_properties.get("pqAttempts")).size();
                }
            }else{
                return 1;
            }
        } catch (Exception err) {
            Log.e(TAG, "getCourseAttempt - local_courses_admission " + err.getMessage());
            err.printStackTrace();
        }
        return 0;
    }
    public void getSavePoints(String stepId, int stepNo){
        Log.e(TAG, "Save Details = courseId : "+ mCourseId + " StepID "+ stepId + " stepNo "+ stepNo );
        Log.e(TAG, "Raw Result : "+ stepQuizResultHolder);
        AndroidContext androidContext = new AndroidContext(getContext());
        Manager manager = null;
        try {
            manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database local_member_course_progress = manager.getDatabase("local_member_course_progress");
            //local_member_course_progress.delete();

            //local_member_course_progress = manager.getDatabase("local_member_course_progress");
            Query orderedQuery = chViews.ReadMemberCourseProgByMemberIdCourceId(local_member_course_progress,mCourseId,sys_usercouchId).createQuery();
            orderedQuery.setDescending(true);
            QueryEnumerator results = orderedQuery.run();
            if(results.getCount()>0) {
                for (Iterator<QueryRow> c_prg = results; c_prg.hasNext(); ) {
                    QueryRow row = c_prg.next();
                    String docId = (String) row.getValue();
                    Document mem_course_prog_doc = local_member_course_progress.getExistingDocument(docId);
                    Map<String, Object> mem_course_prog_properties = new HashMap<>();
                    mem_course_prog_properties.putAll(mem_course_prog_doc.getProperties());
                    Log.e(TAG, "STEPS NO " + crs_StepIds.size() + " IDS" + crs_StepIds);
                    List<Integer> ary_pqAttempts = (List<Integer>) mem_course_prog_properties.get("pqAttempts");
                    List<List> ary_LstResults = (List<List>) mem_course_prog_properties.get("stepsResult");
                    List<List> ary_LstStatus = (List<List>) mem_course_prog_properties.get("stepsStatus");
                    List<List> temp_ary_LstResult = new ArrayList<>();
                    List<List> temp_ary_LstStatus = new ArrayList<>();
                    List<String> Result=null;
                    List<String> Status=null;
                    points = 0;
                    scoreMessage="";
                    for (int x=0; x<crs_StepIds.size();x++) {
                        Result = (List<String>) ary_LstResults.get(x);
                        Status = (List<String>) ary_LstStatus.get(x);
                        if(crs_StepIds.get(x).equalsIgnoreCase(stepId)) {
                            for(int cnt=0;cnt<stepQuizResultHolder.size();cnt++) {
                                if (stepQuizResultHolder.get(cnt) != null) {
                                    if (stepQuizResultHolder.get(cnt)) {
                                        try {
                                            points = (points + Integer.parseInt(qn_Marks.get(x)));
                                        } catch (Exception intErr) {
                                            points++;
                                        }
                                    }

                                }else{
                                    scoreMessage = "\n Essay/Text/Attachment will scored later";
                                }
                            }

                            Result.add(points+"");
                            Status.add("1");
                            ary_pqAttempts.add(ary_pqAttempts.get(x)+1);
                        }
                        temp_ary_LstResult.add(Result);
                        temp_ary_LstStatus.add(Status);
                        Log.e(TAG, " Inside Inside  " + Result);
                    }

                    mem_course_prog_properties.put("pqAttempts",ary_pqAttempts);
                    mem_course_prog_properties.put("stepsResult",temp_ary_LstResult);
                    mem_course_prog_properties.put("stepsStatus", temp_ary_LstStatus);
                    mem_course_prog_doc.putProperties(mem_course_prog_properties);
                    Log.e(TAG, "member course progress Exist data " + mem_course_prog_properties);

                    stepQuizResultHolder.clear();
                    stepTextResultHolder.clear();
                }
            }else{
                //// Following BeLL structure
                Map<String, Object> newProperties = new HashMap<>();
                List<Integer> ary_pqAttempts = new ArrayList<>();
                List<ArrayList> ary_LstResults = new ArrayList<>();
                List<ArrayList> ary_LstStatus = new ArrayList<>();
                points=0;
                for(int cnt=0;cnt < stepQuizResultHolder.size();cnt++){
                    if(stepQuizResultHolder.get(cnt)){
                        try{
                            points = (points + Integer.parseInt(qn_Marks.get(cnt)));
                        }catch (Exception intErr){
                            points++;
                        }

                    }
                }
                for (int x=0; x<crs_StepIds.size();x++){
                    ary_pqAttempts.add(0);
                    ArrayList<String> listResult = new ArrayList<String>();
                    ArrayList<String> listStatus = new ArrayList<String>();
                    listResult.add(null);
                    listStatus.add(null);
                    if(crs_StepIds.get(x).equalsIgnoreCase(stepId)){
                        listResult.add(points+"");
                        listStatus.add("1");
                    }
                    ary_LstResults.add(listResult);
                    ary_LstStatus.add(listStatus);
                }
                newProperties.put("stepsIds", crs_StepIds);
                newProperties.put("memberId", sys_usercouchId);
                newProperties.put("courseId", mCourseId);
                newProperties.put("kind", "course-member-result");
                ary_pqAttempts.set(0,1); // step index
                newProperties.put("pqAttempts",ary_pqAttempts);
                newProperties.put("stepsResult",ary_LstResults);
                newProperties.put("stepsStatus",ary_LstStatus);
                ////// Saving course step result.
                Document document = local_member_course_progress.createDocument();
                document.putProperties(newProperties);
                Log.e(TAG, "New member course progress data " + newProperties);

                stepQuizResultHolder.clear();
                stepTextResultHolder.clear();
            }
            dialogTest.dismiss();
            alertDialogOkay("Score for Step "+stepCurrentIndex,"Multiple Choice questions) : "+ points+ " "+ scoreMessage);
        } catch (Exception err) {
            Log.e(TAG, "local_courses_admission on device " + err.getMessage());
            err.printStackTrace();
        }


    }

    public void restorePref() {
        // Restore preferences
        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sys_username = settings.getString("pf_username", "");
        sys_oldSyncServerURL = settings.getString("pf_sysncUrl", "http://");
        sys_lastSyncDate = settings.getString("pf_lastSyncDate", "");
        sys_password = settings.getString("pf_password", "");
        sys_usercouchId = settings.getString("pf_usercouchId", "");
        sys_userfirstname = settings.getString("pf_userfirstname", "");
        sys_userlastname = settings.getString("pf_userlastname", "");
        sys_usergender = settings.getString("pf_usergender", "");
        sys_uservisits = settings.getString("pf_uservisits", "");
        sys_uservisits_Int = settings.getInt("pf_uservisits_Int", 0);
        sys_singlefilestreamdownload = settings.getBoolean("pf_singlefilestreamdownload", true);
        sys_multiplefilestreamdownload = settings.getBoolean("multiplefilestreamdownload", true);
        sys_servername = settings.getString("pf_server_name", " ");
        sys_serverversion = settings.getString("pf_server_version", " ");
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
        try {

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
            for (int x = 0; x < rs_Ids.size(); x++) {
                arrayAdapter.add(rs_Title.get(x));
            }
            dialogResources = dialogB2.create();
            dialogResources.show();
            ListView lv = (ListView) dialogResources.findViewById(R.id.course_resource_list);
            lv.setAdapter(arrayAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                    String value = (String) adapter.getItemAtPosition(position);
                    Log.e(TAG, "Clicked item " + value);
                    openedResourceId = rs_Ids.get(position);
                    openedResourceTitle = value;
                    mDialog = new ProgressDialog(getContext());
                    mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    mDialog.setMessage("Please wait...");
                    mDialog.setCancelable(false);
                    mDialog.show();
                    if (openResources(openedResourceId)) {
                        Log.i(TAG, "Open Clicked ********** " + openedResourceId);
                    } else {
                        Log.i(TAG, "Open  ********** " + openedResourceId);
                    }
                    ;
                }
            });
            lbl_ResStepTitle = (TextView) dialogResources.findViewById(R.id.lbl_ResTopTitle);
            lbl_ResStepTitle.setText("Step : " + StepTitle);

            Button btnClose = (Button) dialogResources.findViewById(R.id.btnCloseDlg);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogResources.dismiss();
                }
            });

        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    public Boolean openResources(String id) {
        String resourceIdTobeOpened = id;
        Log.d(TAG, "Trying to open resource " + id);
        try {
            AndroidContext androidContext = new AndroidContext(getContext());
            Manager manager = new Manager(androidContext, Manager.DEFAULT_OPTIONS);
            Database res_Db = manager.getExistingDatabase("offline_course_resources");
            Document res_doc = res_Db.getExistingDocument(resourceIdTobeOpened);
            String openwith = (String) res_doc.getProperty("openWith");
            String openResName = (String) res_doc.getProperty("title");
            openedResourceId = resourceIdTobeOpened;
            openedResourceTitle = openResName;
            ///openFromDiskDirectly = true;
            //////////////////logHouse.updateActivityOpenedResources(getContext(), sys_usercouchId, resourceIdTobeOpened, openResName);
            Log.e("MYAPP", " member opening resource  = " + resourceIdTobeOpened + " and Open with " + openwith);
            //// PDF and Bell-Reader
            if (openwith.equalsIgnoreCase("PDF.js") || (openwith.equalsIgnoreCase("Bell-Reader"))) {
                Log.e(TAG, " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e(TAG, " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            try {
                                mDialog.dismiss();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setPackage("com.adobe.reader");
                                intent.setDataAndType(Uri.fromFile(f), "application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(intent);
                            } catch (Exception err) {
                                err.printStackTrace();
                                myDir = new File(Environment.getExternalStorageDirectory().toString() + "/ole_temp2");
                                File dst = new File(myDir, "adobe_reader.apk");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(dst), "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(intent);
                            }
                        }
                    }
                }
////MP3
            } else if (openwith.equalsIgnoreCase("MP3")) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            getContext().startActivity(intent);
                        }
                    }
                }
///// VIDEO or Video Book Player
            } else if (openwith.equalsIgnoreCase("Flow Video Player") || (openwith.equalsIgnoreCase("Native Video"))) {
                Log.e("MyCouch", " Command Video name -:  " + resourceIdTobeOpened);
                String filenameOnly = "";
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ole_temp");
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        if (f.getName().indexOf(".") > 0) {
                            filenameOnly = f.getName().substring(0, f.getName().lastIndexOf("."));
                        }
                        Log.e("MyCouch", " File name -:  " + f.getName() + " Filename only " + filenameOnly);
                        if (filenameOnly.equalsIgnoreCase(resourceIdTobeOpened)) {
                            mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(Uri.fromFile(f), mimetype);
                            getContext().startActivity(intent);
                        }
                    }
                }
            }
///// HTML
            else if (openwith.equalsIgnoreCase("HTML")) {
               /* indexFilePath = null;
                if (attmentNames.size() > 1) {
                    for (int cnt = 0; cnt < attmentNames.size(); cnt++) {
                        downloadHTMLContent(resourceIdTobeOpened, (String) attmentNames.get(cnt));
                    }
                    if (indexFilePath != null) {
                        openHTML(indexFilePath);
                    }
                } else {
                    openImage(resourceIdTobeOpened, (String) attmentNames.get(0), getExtension(attmentNames.get(0)));
                }*/
//// PDF
            } else if (openwith.equalsIgnoreCase("Just download")) {
                //// Todo work to get just download
            } else if (openwith.equalsIgnoreCase("BeLL Video Book Player")) {
            }
            mListener.onResourceOpened(openedResourceId, openedResourceTitle);
        } catch (Exception Er) {
            Log.d("MyCouch", "Opening resource error " + Er.getMessage());
            Er.printStackTrace();
        }
        return true;
    }
    public void alertDialogOkay(String Heading,String Message) {
        final DialogSheet sh =  new DialogSheet(getContext());
        sh.setTitle(Heading)
                .setMessage(Message)
                .setPositiveButton(android.R.string.ok, new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        sh.dismiss();
                    }
                })
                .setButtonsColorRes(R.color.colorPrimary)  // Default color is accent
                .show();
        /*.setNegativeButton(android.R.string.cancel, new DialogSheet.OnNegativeClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Your action
                    }
                })*/
    }
}
