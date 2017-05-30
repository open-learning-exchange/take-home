package pbell.offline.ole.org.pbell;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CalendarView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Home extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    //private View mControlsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //  toggle();
            }
        });
        */
        //CalendarView calendarView = (CalendarView)findViewById(R.id.calendarView);
        ///long a = calendarView.getDate();
        //calendarView.setMaxDate(a);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        //mControlsView.setVisibility(View.GONE);
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
