package com.example.gui.wakiewakie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String TABLE_ALARM = "alarm";
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_REPEAT = "repeat";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_FLASH = "flash";
    private static final String KEY_GRADUALBRIGHTNESS = "gradualBrightness";
    private static final String KEY_RINGTONE = "ringtone";
    private static final String KEY_SNOOZE = "snooze";
    private static final String KEY_ISON = "ison";
    private static final String[] COLUMNS = {KEY_ID,KEY_TIME,KEY_REPEAT,KEY_VIBRATION,KEY_FLASH,KEY_GRADUALBRIGHTNESS,KEY_RINGTONE,KEY_SNOOZE,KEY_ISON};

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "AlarmDB";
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ALARM_TABLE = "CREATE TABLE alarm ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "time FLOAT, "+
                "repeat TEXT, "+
                "vibration BOOLEAN, "+
                "flash BOOLEAN, "+
                "gradualBrightness BOOLEAN, "+
                "ringtone TEXT, "+
                "snooze INTEGER, "+
                "ison BOOLEAN )";
        db.execSQL(CREATE_ALARM_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS alarm");
        this.onCreate(db);
    }

    public void addAlarm(Alarm alarm){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, alarm.getTime());
        values.put(KEY_REPEAT, alarm.getRepeat());
        values.put(KEY_VIBRATION, alarm.isVibration());
        values.put(KEY_FLASH, alarm.isFlash());
        values.put(KEY_GRADUALBRIGHTNESS, alarm.isGradualBrightness());
        values.put(KEY_RINGTONE, alarm.getRingtone());
        values.put(KEY_SNOOZE, alarm.getSnooze());
        values.put(KEY_ISON, alarm.isIson());
        db.insert(TABLE_ALARM,
                null,
                values);
        db.close();
    }

    public Alarm getAlarm(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =
                db.query(TABLE_ALARM,
                        COLUMNS,
                        " id = ?",
                        new String[] { String.valueOf(id) },
                        null,
                        null,
                        null,
                        null);
        if (cursor != null)
            cursor.moveToFirst();

        Alarm alarm = new Alarm();
        alarm.setId(Integer.parseInt(cursor.getString(0)));
        alarm.setTime(cursor.getFloat(1));
        alarm.setRepeat(cursor.getString(2));
        alarm.setVibration(cursor.getInt(3)>0);
        alarm.setFlash(cursor.getInt(4)>0);
        alarm.setGradualBrightness(cursor.getInt(5)>0);
        alarm.setRingtone(cursor.getString(6));
        alarm.setSnooze(Integer.parseInt(cursor.getString(7)));
        alarm.setIson(cursor.getInt(8)>0);
        return alarm;
    }

    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new LinkedList<Alarm>();
        String query = "SELECT * FROM " + TABLE_ALARM + " ORDER BY " + KEY_TIME + " asc";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Alarm alarm = null;
        if (cursor.moveToFirst()) {
            do {
                alarm = new Alarm();
                alarm.setId(Integer.parseInt(cursor.getString(0)));
                alarm.setTime(cursor.getFloat(1));
                alarm.setRepeat(cursor.getString(2));
                alarm.setVibration(cursor.getInt(3)>0);
                alarm.setFlash(cursor.getInt(4)>0);
                alarm.setGradualBrightness(cursor.getInt(5)>0);
                alarm.setRingtone(cursor.getString(6));
                alarm.setSnooze(Integer.parseInt(cursor.getString(7)));
                alarm.setIson(cursor.getInt(8)>0);
                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        return alarms;
    }


    public int updateAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, alarm.getTime());
        values.put(KEY_REPEAT, alarm.getRepeat());
        values.put(KEY_VIBRATION, alarm.isVibration());
        values.put(KEY_FLASH, alarm.isFlash());
        values.put(KEY_GRADUALBRIGHTNESS, alarm.isGradualBrightness());
        values.put(KEY_RINGTONE, alarm.getRingtone());
        values.put(KEY_SNOOZE, alarm.getSnooze());
        values.put(KEY_ISON, alarm.isIson());
        int i = db.update(TABLE_ALARM,
                values,
                KEY_ID+" = ?",
                new String[] { String.valueOf(alarm.getId()) });
        db.close();
        return i;
    }
    public void deleteAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALARM,
                KEY_ID+" = ?",
                new String[] { String.valueOf(alarm.getId()) });
        db.close();
    }


}
