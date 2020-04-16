package com.example.gui.wakiewakie;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private static final String IMAGE_DIRECTORY = "/wakiewakie";
    private int GALLERY = 1;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        if(mPreferences.getBoolean("settingNightMode",false)){
            setTheme(R.style.DarkTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        setTitle(R.string.title_activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView userPoints = (TextView)findViewById(R.id.textUserPoints);
        userPoints.setText("" + mPreferences.getInt("points",0));


        Button topScore = (Button)findViewById(R.id.buttonTopScore);
        topScore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, TopScoreActivity.class));
                overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);
            }
        });

        Button friends = (Button)findViewById(R.id.buttonFriends);
        friends.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, FriendsActivity.class));
                overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);
            }
        });

        Button achievements = (Button)findViewById(R.id.buttonAchivements);
        achievements.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, AchievementsActivity.class));
                overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);
            }
        });

        Button averageSleep = (Button)findViewById(R.id.buttonAverageSleep);
        averageSleep.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, AverageSleepTimeActivity.class));
                overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);
            }
        });

        Button averageWakeUp = (Button)findViewById(R.id.buttonAverageWakeUp);
        averageWakeUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, AverageWakeUpTimeActivity.class));
                overridePendingTransition(R.transition.slide_from_right, R.transition.slide_to_left);
            }
        });

        final TextView username = (TextView)findViewById(R.id.textViewUsername);
        username.setText(mPreferences.getString("username",mPreferences.getString("email",getResources().getString(R.string.text_Username))));
        username.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);

                final EditText et = new EditText(ProfileActivity.this);

                et.setText(username.getText());

                alertDialogBuilder.setView(et);

                alertDialogBuilder.setCancelable(false).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        username.setText(et.getText().toString());
                        mEditor.putString("username",et.getText().toString());
                        mEditor.commit();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                return false;
            }
        });

        img = (ImageView)findViewById(R.id.imageViewProfile);

        File imgFile = new  File(mPreferences.getString("profilephoto",""));

        if(mPreferences.getString("profilephoto","") != ""){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            img.setImageBitmap(myBitmap);
        }else{
            img.setImageResource(R.drawable.default_profile_photo);
        }

        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(isStoragePermissionGranted())
                    showPictureDialog();
                return false;
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        overridePendingTransition(R.transition.slide_from_left, R.transition.slide_to_right);
        return true;
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getResources().getString(R.string.photo_dialog_title));
        String[] pictureDialogItems = {
                getResources().getString(R.string.photo_dialog_gallery)};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    Toast.makeText(ProfileActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    img.setImageBitmap(bitmap);
                    if(isStoragePermissionGranted()){
                        SaveImage(bitmap);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(ProfileActivity.this, R.string.toast_permission_read_external_storage, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPictureDialog();
                } else {
                    Toast.makeText(ProfileActivity.this, R.string.toast_permission_read_external_storage, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/wakiewakie/images");
        myDir.mkdirs();
        String fname = "ProfilePhoto.jpg";
        File file = new File (myDir, fname);
        mEditor.putString("profilephoto", file.getAbsolutePath());
        mEditor.commit();
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted before api 23
            return true;
        }
    }

}
