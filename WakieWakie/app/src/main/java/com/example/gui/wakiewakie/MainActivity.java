package com.example.gui.wakiewakie;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    MySQLiteHelper db;
    String savePath = Environment.getExternalStorageDirectory().getPath() + "/WakieWakieQRCodes/";
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //LocaleHelper.setLocale(this, "pt");
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPreferences.getBoolean("settingNightMode",false)){
            setTheme(R.style.DarkTheme);
        }else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        db = new MySQLiteHelper(this);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Button btnSettings = (Button)findViewById(R.id.btnConfig);

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);

            }
        });

        Button btnAddAlarm = (Button)findViewById(R.id.btnAddAlarm);

        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasQRCodes()) {
                    startActivity(new Intent(MainActivity.this, AlarmActivity.class));
                    overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);
                }else{
                    Toast.makeText(getApplicationContext(), R.string.toast_need_qr_code , Toast.LENGTH_LONG).show();
                }

            }
        });

        Button btnLogin = (Button)findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);

            }
        });

        Button btnQr = (Button)findViewById(R.id.btnQr);

        btnQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isStoragePermissionGranted()) {
                    openQrDialog();
                }

            }
        });


        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                recreate();
            }
        };

        mPreferences.registerOnSharedPreferenceChangeListener(listener);

        populateAlarm();


    }

    private void openQrDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final EditText et = new EditText(MainActivity.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_title_room));

        et.setHint(getResources().getString(R.string.alert_room_hint));

        alertDialogBuilder.setView(et);

        alertDialogBuilder.setCancelable(true).setPositiveButton(getResources().getString(R.string.alert_room_save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveQrCode(et.getText().toString());
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void saveQrCode(String inputValue){
        inputValue = inputValue.trim();
        String qrcode = "{\"name\":\"" + inputValue + "\"}";
        if (inputValue.length() > 0) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    qrcode, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
            } catch (WriterException e) {
            }
        boolean save;
        String result;
        try {
            save = QRGSaver.save(savePath, inputValue, bitmap, QRGContents.ImageType.IMAGE_JPEG);
            result = save ? getResources().getString(R.string.alert_qr_saved) : getResources().getString(R.string.alert_qr_not_saved);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        populateAlarm();
    }

    private void languageChange(){
        Locale locale = new Locale("pt");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.finish();
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
    }

    public void populateAlarm(){
        LinearLayout alarmList = (LinearLayout) findViewById(R.id.LinearListAlarms);
        alarmList.removeAllViews();

        List<Alarm> a = db.getAllAlarms();
        for (Alarm b: a) {
            final Alarm alarmeTemp = b;
            final View alarm = getLayoutInflater().inflate(R.layout.grid_menu_alarm, null);
            alarm.setId(b.getId());
            TextView time = (TextView) alarm.findViewById(R.id.txtTime);
            TextView day = (TextView) alarm.findViewById(R.id.txtdia);
            final Switch onoff = (Switch) alarm.findViewById(R.id.switchAlarm);
            onoff.setId(b.getId());
            int hours = (int)Math.floor(b.getTime());
            int minutes = (int)Math.round((b.getTime() - Math.floor(b.getTime()))* 100.0);
            if(minutes >=10)
                time.setText(hours + ":" + minutes);
            else
                time.setText(hours + ":0" + minutes);

            day.setText("" + GetRepeateDays(b.getRepeat(),hours,minutes));
            onoff.setChecked(alarmeTemp.isIson());

            alarmList.addView(alarm);

            alarm.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.delete_alarm) , Toast.LENGTH_LONG).show();
                    db.deleteAlarm(alarmeTemp);
                    populateAlarm();
                    return true;
                }
            });

            onoff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasQRCodes()) {
                        alarmeTemp.setIson(onoff.isChecked());
                        db.updateAlarm(alarmeTemp);

                        if (onoff.isChecked()) {
                            int hours = (int) Math.floor(alarmeTemp.getTime());
                            int minutes = (int) Math.round((alarmeTemp.getTime() - Math.floor(alarmeTemp.getTime())) * 100.0);
                            setAlarmRepeatDays(alarmeTemp.getRepeat(), alarmeTemp.getId(), hours, minutes);
                        } else {
                            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            intent.setData(Uri.parse("SUNDAY"));
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            intent.setData(Uri.parse("MONDAY"));
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            intent.setData(Uri.parse("TUESDAY"));
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            intent.setData(Uri.parse("WEDNESDAY"));
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            intent.setData(Uri.parse("THURSDAY"));
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            intent.setData(Uri.parse("FRIDAY"));
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            intent.setData(Uri.parse("SATURDAY"));
                            intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, alarmeTemp.getId(), intent, 0);
                            alarmManager.cancel(pendingIntent);
                            pendingIntent.cancel();

                        }
                    }else{
                        Toast.makeText(getApplicationContext(), R.string.toast_need_qr_code , Toast.LENGTH_LONG).show();
                        onoff.setChecked(false);
                    }
                }
            });


        }
    }

    public String GetRepeateDays(String days,Integer hour,Integer min){

        if(days.equals("FFFFFFF")) {
            Calendar rightNow = Calendar.getInstance();
            if (hour > rightNow.get(Calendar.HOUR_OF_DAY))
                return getResources().getString(R.string.repeat_today);
            else if(hour == rightNow.get(Calendar.HOUR_OF_DAY) && min > rightNow.get(Calendar.MINUTE))
                return getResources().getString(R.string.repeat_today);
            else if(min == rightNow.get(Calendar.MINUTE) && rightNow.get(Calendar.SECOND) <= 0)
                return getResources().getString(R.string.repeat_today);
            else
                return getResources().getString(R.string.repeat_tomorrow);
        }else if(days.equals("TTTTTTT"))
            return getResources().getString(R.string.repeat_every_day);
        else if(days.equals("TTTTTFF"))
            return getResources().getString(R.string.repeat_work_days);
        else if(days.equals("FFFFFTT"))
            return getResources().getString(R.string.repeat_weekend);
        else{
            String finnalText = "";
            if(days.charAt(0) == 'T')
                finnalText += getResources().getString(R.string.week_day_monday) + " | ";
            if(days.charAt(1) == 'T')
                finnalText += getResources().getString(R.string.week_day_tuesday) + " | ";
            if(days.charAt(2) == 'T')
                finnalText += getResources().getString(R.string.week_day_wednesday) + " | ";
            if(days.charAt(3) == 'T')
                finnalText += getResources().getString(R.string.week_day_thursday) + " | ";
            if(days.charAt(4) == 'T')
                finnalText += getResources().getString(R.string.week_day_friday) + " | ";
            if(days.charAt(5) == 'T')
                finnalText += getResources().getString(R.string.week_day_saturday) + " | ";
            if(days.charAt(6) == 'T')
                finnalText += getResources().getString(R.string.week_day_sunday) + " | ";

            if (finnalText.endsWith(" | ")) {
                finnalText = finnalText.substring(0, finnalText.length() - 3);
            }
            return finnalText;
        }

    }

    private void setAlarmRepeatDays(String days, int id, int hour, int minutes){
        if(days.equals("FFFFFFF")){
            setSingleAlarm(id,hour,minutes);
        }else{
            if(days.charAt(0) == 'T')
                setAlarmRepeat(Calendar.MONDAY,id,hour,minutes);
            if(days.charAt(1) == 'T')
                setAlarmRepeat(Calendar.TUESDAY,id,hour,minutes);
            if(days.charAt(2) == 'T')
                setAlarmRepeat(Calendar.WEDNESDAY,id,hour,minutes);
            if(days.charAt(3) == 'T')
                setAlarmRepeat(Calendar.THURSDAY,id,hour,minutes);
            if(days.charAt(4) == 'T')
                setAlarmRepeat(Calendar.FRIDAY,id,hour,minutes);
            if(days.charAt(5) == 'T')
                setAlarmRepeat(Calendar.SATURDAY,id,hour,minutes);
            if(days.charAt(6) == 'T')
                setAlarmRepeat(Calendar.SUNDAY,id,hour,minutes);
        }


    }

    private void setAlarmRepeat(int week, int id, int hour, int minutes){
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
        pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, week);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        if(calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.set(Calendar.WEEK_OF_MONTH, calendar.get(Calendar.WEEK_OF_MONTH) + 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()-AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    private void setSingleAlarm(int id, int hour, int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        if(calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        }

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
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
            case 99: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openQrDialog();
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_permission_read_external_storage, Toast.LENGTH_SHORT).show();
                }
                return;
            }
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
        if(rooms.size() > 0)
            return true;
        else
            return false;
    }










}
