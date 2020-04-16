package com.example.gui.wakiewakie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent intentTemp = new Intent(context, CameraActivity.class);
        intentTemp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentTemp.setData(intent.getData());
        intentTemp.putExtra("AlarmID",intent.getExtras().getInt("AlarmID"));
        context.startActivity(intentTemp);
    }




}
