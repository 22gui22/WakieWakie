package com.example.gui.wakiewakie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class MySQLiteHelperStatistics extends SQLiteOpenHelper {

    private static final String TABLE_TIME = "wakeuptime";
    private static final String KEY_ID = "id";
    private static final String KEY_HOUR = "alarmHours";
    private static final String KEY_MINUTES = "alarmMinutes";
    private static final String KEY_TURNOFF = "turnOffTime";
    private static final String KEY_DATE = "alarmDate";
    private static final String[] COLUMNS = {KEY_ID,KEY_HOUR,KEY_MINUTES,KEY_TURNOFF,KEY_DATE};
    private Context context;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StatisticsDB";
    public MySQLiteHelperStatistics(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ALARM_TABLE = "CREATE TABLE wakeuptime ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "alarmHours INTEGER, "+
                "alarmMinutes INTEGER, "+
                "turnOffTime INTEGER, "+
                "alarmDate DATETIME DEFAULT CURRENT_DATE )";
        db.execSQL(CREATE_ALARM_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS wakeuptime");
        this.onCreate(db);
    }

    public void addTime(int hours, int minutes, int turnOffTime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_HOUR, hours);
        values.put(KEY_MINUTES, minutes);
        values.put(KEY_TURNOFF, turnOffTime);
        //values.put(KEY_DATE, datetime());
        db.insert(TABLE_TIME,
                null,
                values);
        db.close();
    }

    public WakeupTime getTime(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =
                db.query(TABLE_TIME,
                        COLUMNS,
                        " id = ?",
                        new String[] { String.valueOf(id) },
                        null,
                        null,
                        null,
                        null);
        if (cursor != null)
            cursor.moveToFirst();

        WakeupTime wakeup = new WakeupTime();
        wakeup.setId(Integer.parseInt(cursor.getString(0)));
        wakeup.setAlarmHours(cursor.getInt(1));
        wakeup.setAlarmMinutes(cursor.getInt(2));
        wakeup.setTurnOffTime(cursor.getInt(3));
        wakeup.setAlarmDate(cursor.getString(4));
        return wakeup;
    }

    public List<WakeupTime> getAllTimes() {
        List<WakeupTime> times = new LinkedList<WakeupTime>();
        String query = "SELECT * FROM " + TABLE_TIME + " ORDER BY " + KEY_DATE + " asc";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Alarm alarm = null;
        if (cursor.moveToFirst()) {
            do {
                WakeupTime wakeup = new WakeupTime();
                wakeup.setId(Integer.parseInt(cursor.getString(0)));
                wakeup.setAlarmHours(cursor.getInt(1));
                wakeup.setAlarmMinutes(cursor.getInt(2));
                wakeup.setTurnOffTime(cursor.getInt(3));
                wakeup.setAlarmDate(cursor.getString(4));
                times.add(wakeup);
            } while (cursor.moveToNext());
        }
        return times;
    }





}
