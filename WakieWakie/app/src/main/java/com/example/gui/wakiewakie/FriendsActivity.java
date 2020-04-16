package com.example.gui.wakiewakie;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private List<Contact> contactList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPreferences.getBoolean("settingNightMode",false)){
            setTheme(R.style.DarkTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setTitle(R.string.friends_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(checkPermissionContacts()){
            contactList = getContacts(FriendsActivity.this);
            populateList();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0 : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                    onSupportNavigateUp();
                }
                return;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);
        return true;
    }

    public void populateList(){
        LinearLayout friendList = (LinearLayout) findViewById(R.id.friendsLayout);
        friendList.removeAllViews();
        String username = "";
        int counter = 1;
        int pointList = 151;

        for (Contact b: contactList) {
            if(!username.equals(b.name)){
                final View contact = getLayoutInflater().inflate(R.layout.grid_menu_contacts, null);
                final Contact c = b;
                TextView contactName = (TextView) contact.findViewById(R.id.contactName);
                TextView phoneNumber = (TextView) contact.findViewById(R.id.phoneNumber);
                TextView points = (TextView) contact.findViewById(R.id.points);
                TextView textpoints = (TextView) contact.findViewById(R.id.textPoints);
                contactName.setText(b.name);
                phoneNumber.setText(b.mobileNumber);
                points.setText("" + pointList);
                if(counter > 6){
                    points.setText(R.string.friends_invite);
                    textpoints.setText(R.string.friends_friend);

                    contact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            enviarSMS(c.mobileNumber);
                        }
                    });

                }
                username = b.name;
                counter++;
                pointList -= 7;
                friendList.addView(contact);
            }
        }
    }

    public List<Contact> getContacts(Context ctx) {
        List<Contact> list = new ArrayList<>();
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));

                    while (cursorInfo.moveToNext()) {
                        Contact info = new Contact();
                        info.id = id;
                        info.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        info.mobileNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        list.add(info);
                    }

                    cursorInfo.close();
                }
            }
            cursor.close();
        }
        return list;
    }

    private void enviarSMS(String number) {
        if(checkPermissionSMS()) {
            SmsManager sm = SmsManager.getDefault();
            String msg = getResources().getString(R.string.friends_invitation_message);
            sm.sendTextMessage(number, null, msg, null, null);
        }
    }

    private boolean checkPermissionSMS(){
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.SEND_SMS ) == PackageManager.PERMISSION_GRANTED ) {
            return true;
        } else {
            ActivityCompat.requestPermissions(FriendsActivity.this, new String[]{Manifest.permission.SEND_SMS},33);
            return false;
        }
    }

    private boolean checkPermissionContacts(){
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.READ_CONTACTS ) == PackageManager.PERMISSION_GRANTED ) {
            return true;
        } else {
            ActivityCompat.requestPermissions(FriendsActivity.this, new String[]{Manifest.permission.READ_CONTACTS},0);
            return false;
        }
    }

}
