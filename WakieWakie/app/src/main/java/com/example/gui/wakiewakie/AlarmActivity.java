package com.example.gui.wakiewakie;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtonePickerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    TimePicker alarmTimePicker;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    MySQLiteHelper db;
    String ringtoneAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        if(mPreferences.getBoolean("settingNightMode",false)){
            setTheme(R.style.DarkTheme);
        }else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        setTitle(R.string.title_activity_add_alarm);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner spinnerSnooze = (Spinner) findViewById(R.id.spinnerSnooze);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.snoze_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSnooze.setAdapter(adapter);



        alarmTimePicker = (TimePicker) findViewById(R.id.simpleTimePicker);
        //alarmTimePicker = new TimePicker(new ContextThemeWrapper(AlarmActivity.this, R.style.MyTimePickerWidgetStyle));
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Button btnAddAlarm = (Button)findViewById(R.id.btnSaveAlarm);
        final Switch brightnessSwitch = (Switch)findViewById(R.id.switchBright);

        brightnessSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Settings.System.canWrite(AlarmActivity.this)){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    //AlarmActivity.this.startActivity(intent);
                    startActivityForResult(intent,44);
                    brightnessSwitch.setChecked(false);
                }
            }
        });

        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isCameraPermissionGranted() && isStoragePermissionGranted()) {
                    //Get repeat value
                    CheckBox checkBoxMonday = (CheckBox) findViewById(R.id.checkBoxMonday);
                    CheckBox checkBoxTuesday = (CheckBox) findViewById(R.id.checkBoxTuesday);
                    CheckBox checkBoxWednesday = (CheckBox) findViewById(R.id.checkBoxWednesday);
                    CheckBox checkBoxThursday = (CheckBox) findViewById(R.id.checkBoxThursday);
                    CheckBox checkBoxFriday = (CheckBox) findViewById(R.id.checkBoxFriday);
                    CheckBox checkBoxSaturday = (CheckBox) findViewById(R.id.checkBoxSaturday);
                    CheckBox checkBoxSunday = (CheckBox) findViewById(R.id.checkBoxSunday);

                    String repeat = "";
                    if (checkBoxMonday.isChecked())
                        repeat += "T";
                    else
                        repeat += "F";
                    if (checkBoxTuesday.isChecked())
                        repeat += "T";
                    else
                        repeat += "F";
                    if (checkBoxWednesday.isChecked())
                        repeat += "T";
                    else
                        repeat += "F";
                    if (checkBoxThursday.isChecked())
                        repeat += "T";
                    else
                        repeat += "F";
                    if (checkBoxFriday.isChecked())
                        repeat += "T";
                    else
                        repeat += "F";
                    if (checkBoxSaturday.isChecked())
                        repeat += "T";
                    else
                        repeat += "F";
                    if (checkBoxSunday.isChecked())
                        repeat += "T";
                    else
                        repeat += "F";

                    //Gets values of switches
                    Switch vibrationSwitch = (Switch) findViewById(R.id.switchVibration);
                    Switch flashSwitch = (Switch) findViewById(R.id.switchFlash);
                    Switch brightnessSwitch = (Switch) findViewById(R.id.switchBright);


                    Boolean vibration = vibrationSwitch.isChecked();
                    Boolean flash = flashSwitch.isChecked();
                    Boolean brightness = brightnessSwitch.isChecked();

                    Spinner snoozeSpinner = (Spinner) findViewById(R.id.spinnerSnooze);
                    int snoozePosition = snoozeSpinner.getSelectedItemPosition();
                    int snooze = 0;
                    if (snoozePosition == 0)
                        snooze = 0;
                    else if (snoozePosition == 1)
                        snooze = 5;
                    else if (snoozePosition == 2)
                        snooze = 10;
                    else if (snoozePosition == 3)
                        snooze = 15;

                    float time;
                    TimePicker timepicker = (TimePicker) findViewById(R.id.simpleTimePicker);
                    time = (float) (timepicker.getHour() + (timepicker.getMinute() * 0.01));

                    db = new MySQLiteHelper(AlarmActivity.this);

                    Alarm a = new Alarm(0, time, repeat, vibration, flash, brightness, ringtoneAlarm, snooze, true);
                    db.addAlarm(a);

                    int id = 0;
                    List<Alarm> alar = db.getAllAlarms();
                    for (Alarm b : alar) {
                        id = b.getId();
                    }

                    setAlarmRepeatDays(repeat, id);

                    addToSharedPreferencesCounter();

                    finish();
                    overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);
                }

            }
        });

        final Button ringtone = (Button)findViewById(R.id.buttonRingtone);
        ringtone.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 55);

                    return;
                } else {
                    RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog.Builder(AlarmActivity.this, getSupportFragmentManager());
                    //Set title of the dialog
                    ringtonePickerBuilder.setTitle(getResources().getString(R.string.text_select_ringtone));
                    //Add the desirable ringtone types.
                    ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC);
                    ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_NOTIFICATION);
                    ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE);
                    ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);
                    //set the text to display of the positive (ok) button
                    ringtonePickerBuilder.setPositiveButtonText(getResources().getString(R.string.text_set_ringtone));
                    //set text to display as negative button
                    ringtonePickerBuilder.setCancelButtonText(getResources().getString(R.string.text_cancel));
                    //Set flag true to play sample
                    ringtonePickerBuilder.setPlaySampleWhileSelection(true);
                    //Set the callback listener.
                    ringtonePickerBuilder.setListener(new RingtonePickerListener() {

                                                          @Override
                                                          public void OnRingtoneSelected(String ringtoneName, Uri ringtoneUri) {
                                                              ringtoneAlarm = ringtoneUri.toString();
                                                              ringtone.setText(ringtoneName);

                                                          }

                                                      }
                    );
                    //Display the dialog.
                    ringtonePickerBuilder.show();

                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);
        return true;
    }

    private void addToSharedPreferencesCounter(){
        int total = mPreferences.getInt("setalarmcounter",0);
        total++;
        mEditor.putInt("setalarmcounter",total);
        mEditor.commit();
        if(total == 1 || total == 5 || total == 10 || total == 30 || total == 50)
            Toast.makeText(AlarmActivity.this, R.string.toast_new_achivement, Toast.LENGTH_SHORT).show();
    }

    private void setAlarmRepeatDays(String days, int id){
        String dia = "";
        if(days.equals("FFFFFFF")){
            setSingleAlarm(id);
        }else{
            if(days.charAt(0) == 'T')
                setAlarmRepeat(2, id);
            if(days.charAt(1) == 'T')
                setAlarmRepeat(3,id);
            if(days.charAt(2) == 'T')
                setAlarmRepeat(4,id);
            if(days.charAt(3) == 'T')
                setAlarmRepeat(5,id);
            if(days.charAt(4) == 'T')
                setAlarmRepeat(6,id);
            if(days.charAt(5) == 'T')
                setAlarmRepeat(7,id);
            if(days.charAt(6) == 'T')
                setAlarmRepeat(1,id);
        }


    }

    private void setAlarmRepeat(int week, int id){
        String dayOfTheWeek = "";
        switch(week){
            case Calendar.SUNDAY: dayOfTheWeek = "SUNDAY";
                break;
            case Calendar.MONDAY: dayOfTheWeek = "MONDAY";
                break;
            case Calendar.TUESDAY: dayOfTheWeek = "TUESDAY";
                break;
            case Calendar.WEDNESDAY: dayOfTheWeek = "WEDNESDAY";
                break;
            case Calendar.THURSDAY: dayOfTheWeek = "THURSDAY";
                break;
            case Calendar.FRIDAY: dayOfTheWeek = "FRIDAY";
                break;
            case Calendar.SATURDAY: dayOfTheWeek = "SATURDAY";
                break;
        }

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setData(Uri.parse(dayOfTheWeek));
        intent.putExtra("AlarmID",id);
        pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, week);
        calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getHour());
        calendar.set(Calendar.MINUTE, alarmTimePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if(calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.set(Calendar.WEEK_OF_MONTH, calendar.get(Calendar.WEEK_OF_MONTH) + 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()-AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_DAY * 7, pendingIntent);

    }

    private void setSingleAlarm(int id){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getHour());
        calendar.set(Calendar.MINUTE, alarmTimePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if(calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        }

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("AlarmID",id);
        pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public  boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(AlarmActivity.this, new String[]{Manifest.permission.CAMERA}, 91);
                return false;
            }
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 44: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Switch brightnessSwitch = (Switch)findViewById(R.id.switchBright);
                    brightnessSwitch.performClick();
                } else {
                    Toast.makeText(AlarmActivity.this, R.string.toast_permission_camera, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 55: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Button ringtone = (Button)findViewById(R.id.buttonRingtone);
                    ringtone.performClick();
                } else {
                    Toast.makeText(AlarmActivity.this, R.string.toast_permission_camera, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 91: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Button btnAddAlarm = (Button)findViewById(R.id.btnSaveAlarm);
                    btnAddAlarm.performClick();
                } else {
                    Toast.makeText(AlarmActivity.this, R.string.toast_permission_camera, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 99: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Button btnAddAlarm = (Button)findViewById(R.id.btnSaveAlarm);
                    btnAddAlarm.performClick();
                } else {
                    Toast.makeText(AlarmActivity.this, R.string.toast_permission_camera, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(AlarmActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private boolean hasQRCodes(){
        List<String> rooms = new ArrayList<String>();
        String path = Environment.getExternalStorageDirectory().toString()+"/WakieWakieQRCodes/";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                rooms.add(files[i].getName().substring(0, files[i].getName().length() - 4));
            }
        }
        if(rooms != null)
            return true;
        else
            return false;
    }



}
