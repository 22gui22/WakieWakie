package com.example.gui.wakiewakie;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AchievementsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private int[] achivementsIcons = { R.drawable.icon_alarm_add_black,
            R.drawable.icon_alarm_black,
            R.drawable.icon_settings_black,
            R.drawable.icon_alarm_black,
            R.drawable.icon_alarm_add_black,
            R.drawable.icon_alarm_black,
            R.drawable.icon_alarm_add_black,
            R.drawable.icon_alarm_black,
            R.drawable.icon_alarm_add_black,
            R.drawable.icon_alarm_black,
            R.drawable.icon_alarm_add_black};

    private String[] achivementsNames;
    private int[] medals;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPreferences.getBoolean("settingNightMode",false)){
            setTheme(R.style.DarkTheme);
        }else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        setTitle(R.string.title_activity_achievements);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        achivementsNames = getResources().getStringArray(R.array.achievements_array);
        medals = getResources().getIntArray(R.array.medals);

        GridView grid = (GridView) findViewById(R.id.GridViewAchievements);
        grid.setAdapter(new MenuItemAdapter(this));
        grid.setOnItemClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(position) {
            case 0:
                Toast.makeText(AchievementsActivity.this, "Item About", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(AchievementsActivity.this, "Item About", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(AchievementsActivity.this, "Item About", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(AchievementsActivity.this, "Item About", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(AchievementsActivity.this, "Item About", Toast.LENGTH_SHORT).show();
                break;
            case 5:
                Toast.makeText(AchievementsActivity.this, "Item About", Toast.LENGTH_SHORT).show();
                break;
            case 6:
                Toast.makeText(AchievementsActivity.this, "Item About", Toast.LENGTH_SHORT).show();
                break;
        }
    }

private class MenuItemAdapter extends BaseAdapter {
    private Context context;
    public MenuItemAdapter(Context context)
    {
        this.context = context;
    }
    @Override
    public int getCount()
    {
        return achivementsNames.length;
    }
    @Override
    public Object getItem(int position)
    {
        return achivementsNames[position];
    }
    @Override
    public long getItemId(int position)
    {return position;
    }
    private class ViewHolder
    {
        public ImageView icon;
        public TextView label;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;
        ViewHolder holder;
        if(convertView == null)
        {
            vi = LayoutInflater.from(context).inflate(R.layout.grid_menu_achievements,
                    null);
            holder = new ViewHolder();
            holder.icon = (ImageView) vi.findViewById(R.id.imageViewAchievement);
            holder.label = (TextView) vi.findViewById(R.id.textViewAchievement);
            vi.setTag(holder);

        }
        else
        {
            holder = (ViewHolder) vi.getTag();
        }
        holder.icon.setImageResource(achivementsIcons[position]);
        holder.label.setText(achivementsNames[position]);
        LinearLayout color = (LinearLayout) vi.findViewById(R.id.LayoutColor);
        color.setBackgroundColor(medals[position]);
        color.setForeground(ContextCompat.getDrawable(context, R.drawable.icon_lock));
        switch(position){
            case 0:
                if(mPreferences.getInt("setalarmcounter",0) >= 1){
                    color.setForeground(null);
                }
                break;
            case 1:
                if(mPreferences.getInt("wokeupalarmcounter",0) >= 1){
                    color.setForeground(null);
                }
                break;
            case 2:
                if(mPreferences.getInt("editsettingscounter",0) >= 1){
                    color.setForeground(null);
                }
                break;
            case 3:
                if(mPreferences.getInt("setalarmcounter",0) >= 5){
                    color.setForeground(null);
                }
                break;
            case 4:
                if(mPreferences.getInt("wokeupalarmcounter",0) >= 5){
                    color.setForeground(null);
                }
                break;
            case 5:
                if(mPreferences.getInt("setalarmcounter",0) >= 10){
                    color.setForeground(null);
                }
                break;
            case 6:
                if(mPreferences.getInt("wokeupalarmcounter",0) >= 10){
                    color.setForeground(null);
                }
                break;
            case 7:
                if(mPreferences.getInt("setalarmcounter",0) >= 30){
                    color.setForeground(null);
                }
                break;
            case 8:
                if(mPreferences.getInt("wokeupalarmcounter",0) >= 30){
                    color.setForeground(null);
                }
                break;
            case 9:
                if(mPreferences.getInt("setalarmcounter",0) >= 50){
                    color.setForeground(null);
                }
                break;
            case 10:
                if(mPreferences.getInt("wokeupalarmcounter",0) >= 50){
                    color.setForeground(null);
                }
                break;
        }
        return vi;
    }
}
}
