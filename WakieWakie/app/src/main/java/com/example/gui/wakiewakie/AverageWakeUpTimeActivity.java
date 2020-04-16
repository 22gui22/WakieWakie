package com.example.gui.wakiewakie;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AverageWakeUpTimeActivity extends AppCompatActivity {
    private SharedPreferences mPreferences;
    private MySQLiteHelperStatistics dbstats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPreferences.getBoolean("settingNightMode",false)){
            setTheme(R.style.DarkTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_wake_up_time);

        setTitle(R.string.wake_up_time_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView stats = (TextView) findViewById(R.id.textViewStatsState);
        if(mPreferences.getBoolean("settingAllowStatistics",true)){
            getStats();
            stats.setText(getResources().getText(R.string.statistics_enabled_wakeup));
        }else{
            hideGraph();
            stats.setText(getResources().getText(R.string.statistics_disabled));
        }


    }

    @Override
    public boolean onSupportNavigateUp(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        finish();
        overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);
        return true;
    }

    public void getStats(){
        dbstats = new MySQLiteHelperStatistics(this);
        List<WakeupTime> times = dbstats.getAllTimes();
        String[]multi = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        SimpleDateFormat sdfdate = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        // get starting date
        cal.add(Calendar.DAY_OF_MONTH, -7);

        // loop adding one day in each iteration
        for(int i = 0; i< 7; i++){
            cal.add(Calendar.DAY_OF_MONTH, 1);
            multi[i] = sdf.format(cal.getTime());
        }
        int[] i = new int[7];
        for(WakeupTime t : times){

                //int turnoff = t.getTurnOffTime();
                String strdate = t.getAlarmDate();
                Date date = new Date();
                try{date = sdfdate.parse(strdate);}catch (Exception e){}

                for(int e = 0; e< 7; e++){
                    if(multi[e].equals(sdf.format(date))){
                        i[e] = t.getTurnOffTime();
                    }
                }

        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, i[0]),
                new DataPoint(1, i[1]),
                new DataPoint(2, i[2]),
                new DataPoint(3, i[3]),
                new DataPoint(4, i[4]),
                new DataPoint(5, i[5]),
                new DataPoint(6, i[6])
        });
        populateGraph(series,multi);
    }

    public void populateGraph(LineGraphSeries<DataPoint> series,String[] datas){
        GraphView graph = (GraphView) findViewById(R.id.graphwakeup);
        //LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                //new DataPoint(0, 5),
                //new DataPoint(1, 10),
                //new DataPoint(2, 5),
                //new DataPoint(3, 20),
              //  new DataPoint(4, 10),
            //    new DataPoint(5, 30),
          //      new DataPoint(6, 20)
        //});
        graph.addSeries(series);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(datas);
        staticLabelsFormatter.setVerticalLabels(new String[] {"0","5", "10","15", "20","25", "30","35", "40","45","50"});

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = AverageWakeUpTimeActivity.this.getTheme();
        theme.resolveAttribute(R.attr.textcolor, typedValue, true);
        graph.getGridLabelRenderer().setGridColor(typedValue.data);
        graph.getGridLabelRenderer().setVerticalLabelsColor(typedValue.data);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(typedValue.data);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(6);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(50);
    }

    public void hideGraph() {
        GraphView graph = (GraphView) findViewById(R.id.graphwakeup);
        graph.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        finish();
        overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);
    }
}
