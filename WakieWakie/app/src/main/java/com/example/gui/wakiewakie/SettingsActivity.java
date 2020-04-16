package com.example.gui.wakiewakie;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Layout;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private Switch switchPhoneTurnOff;
    private Switch switchAllowStatistics;
    private Switch switchNightMode;
    private Switch switchPreventAppUninstall;
    private PolicyManager policyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        if(mPreferences.getBoolean("settingNightMode",false)){
            setTheme(R.style.DarkTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(R.string.title_activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        policyManager = new PolicyManager(this);

        switchPhoneTurnOff = (Switch) findViewById(R.id.switchPhoneTurnOff);
        switchAllowStatistics = (Switch) findViewById(R.id.switchAllowStatistics);
        switchNightMode = (Switch) findViewById(R.id.switchNightMode);
        switchPreventAppUninstall = (Switch) findViewById(R.id.switchPreventAppUninstall);

        switchNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked != mPreferences.getBoolean("settingNightMode",false)) {
                    if(isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        //setTheme(R.style.DarkTheme);
                        saveSharedPreferences();
                        recreate();
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        //setTheme(R.style.AppTheme);
                        saveSharedPreferences();
                        recreate();
                    }
                }
            }
        });

        switchPreventAppUninstall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked != mPreferences.getBoolean("settingPhoneTurnOff",false)) {
                    if(isChecked) {

                    }else{
                        //if (policyManager.isAdminActive())
                          //  policyManager.disableAdmin();
                        saveSharedPreferences();
                        recreate();
                    }
                }
            }
        });

        switchPreventAppUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchPreventAppUninstall.isChecked()) {
                    Intent activateDeviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, policyManager.getAdminComponent());
                    activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "After activating admin, you will be able to block application uninstallation.");
                    startActivityForResult(activateDeviceAdmin, PolicyManager.DPM_ACTIVATION_REQUEST_CODE);
                    switchPreventAppUninstall.setClickable(false);
                }else{
                    if (policyManager.isAdminActive())
                      policyManager.disableAdmin();
                    saveSharedPreferences();
                    recreate();
                }
            }
        });
        checkSharedPreferences();
    }

    @Override
    public boolean onSupportNavigateUp(){
        saveSharedPreferences();
        addToSharedPreferencesCounter();
        finish();
        overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);
        //recreate();
        return true;
    }

    private void checkSharedPreferences(){

        Boolean phoneTurnOff = mPreferences.getBoolean("settingPhoneTurnOff",false);
        Boolean preventAppUninstall = mPreferences.getBoolean("settingPreventAppUninstall",false);
        Boolean allowStatistics = mPreferences.getBoolean("settingAllowStatistics",true);
        Boolean nightMode = mPreferences.getBoolean("settingNightMode",false);

        if(phoneTurnOff == true){
            switchPhoneTurnOff.setChecked(true);
        }else{
            switchPhoneTurnOff.setChecked(false);
        }

        if(preventAppUninstall == true){
            switchPreventAppUninstall.setChecked(true);
        }else{
            switchPreventAppUninstall.setChecked(false);
        }

        if(allowStatistics == true){
            switchAllowStatistics.setChecked(true);
        }else{
            switchAllowStatistics.setChecked(false);
        }

        if(nightMode == true){
            switchNightMode.setChecked(true);
            setTheme(R.style.DarkTheme);
        }else{
            switchNightMode.setChecked(false);
        }

    }

    private void saveSharedPreferences(){

        mEditor.putBoolean("settingPhoneTurnOff",switchPhoneTurnOff.isChecked());
        mEditor.putBoolean("settingPreventAppUninstall",switchPreventAppUninstall.isChecked());
        mEditor.putBoolean("settingAllowStatistics",switchAllowStatistics.isChecked());
        mEditor.putBoolean("settingNightMode",switchNightMode.isChecked());



        mEditor.commit();

    }

    private void addToSharedPreferencesCounter(){
        int total = mPreferences.getInt("editsettingscounter",0);
        total++;
        mEditor.putInt("editsettingscounter",total);
        mEditor.commit();
        if(total == 1)
            Toast.makeText(SettingsActivity.this, R.string.toast_new_achivement, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == PolicyManager.DPM_ACTIVATION_REQUEST_CODE) {
            recreate();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            switchPreventAppUninstall.setChecked(false);
            recreate();
        }
    }

}
