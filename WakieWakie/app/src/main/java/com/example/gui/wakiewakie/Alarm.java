package com.example.gui.wakiewakie;

public class Alarm {

    private int id;
    private float time;
    private String repeat;
    private boolean vibration;
    private boolean flash;
    private boolean gradualBrightness;
    private String ringtone;
    private int snooze;
    private boolean ison;

    public Alarm(int id, float time, String repeat, boolean vibration, boolean flash, boolean gradualBrightness, String ringtone, int snooze, boolean ison) {
        this.id = id;
        this.time = time;
        this.repeat = repeat;
        this.vibration = vibration;
        this.flash = flash;
        this.gradualBrightness = gradualBrightness;
        this.ringtone = ringtone;
        this.snooze = snooze;
        this.ison = ison;
    }

    public Alarm() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public boolean isVibration() {
        return vibration;
    }

    public void setVibration(boolean vibration) {
        this.vibration = vibration;
    }

    public boolean isFlash() {
        return flash;
    }

    public void setFlash(boolean flash) {
        this.flash = flash;
    }

    public boolean isGradualBrightness() {
        return gradualBrightness;
    }

    public void setGradualBrightness(boolean gradualBrightness) {
        this.gradualBrightness = gradualBrightness;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public int getSnooze() {
        return snooze;
    }

    public void setSnooze(int snooze) {
        this.snooze = snooze;
    }

    public boolean isIson() {
        return ison;
    }

    public void setIson(boolean ison) {
        this.ison = ison;
    }
}
