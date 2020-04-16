package com.example.gui.wakiewakie;

import java.util.Date;

public class WakeupTime {
    private int id;
    private int alarmHours;
    private int alarmMinutes;
    private int turnOffTime;
    private String alarmDate;

    public WakeupTime(int alarmHours, int alarmMinutes, int turnOffTime, String alarmDate) {
        this.id = 0;
        this.alarmHours = alarmHours;
        this.alarmMinutes = alarmMinutes;
        this.turnOffTime = turnOffTime;
        this.alarmDate = alarmDate;
    }

    public WakeupTime() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlarmHours() {
        return alarmHours;
    }

    public void setAlarmHours(int alarmHours) {
        this.alarmHours = alarmHours;
    }

    public int getAlarmMinutes() {
        return alarmMinutes;
    }

    public void setAlarmMinutes(int alarmMinutes) {
        this.alarmMinutes = alarmMinutes;
    }

    public int getTurnOffTime() {
        return turnOffTime;
    }

    public void setTurnOffTime(int turnOffTime) {
        this.turnOffTime = turnOffTime;
    }

    public String getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }
}
