package com.example.gui.wakiewakie;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class CameraActivity extends Activity {
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Ringtone ringtone;
    private Vibrator v;
    private MySQLiteHelper db;
    private boolean flashLightStatus = false;
    boolean hasCameraFlash;
    int currBrightness = 0;
    Thread t1;
    Thread tMusic;
    MediaPlayer mp;
    Boolean phoneTurnOff;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private AlarmManager alarmManager;
    private int snoozed;
    private IntentIntegrator qrScan;
    private String objective;
    private int count;
    private Timer T;
    private MySQLiteHelperStatistics dbstats;
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        ///////

        ///////

        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dbstats = new MySQLiteHelperStatistics(CameraActivity.this);
        count = 1;
        T = new Timer();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        count++;
                    }
                });
            }
        }, 1000, 1000);

        snoozed = 0;

        qrScan = new IntentIntegrator(this);
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        qrScan.setPrompt("Scan the Qr code");
        qrScan.setOrientationLocked(true);
        qrScan.setBeepEnabled(false);

        Button turnoff = (Button)findViewById(R.id.buttonTurnOff);
        turnoff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                QRCodeReader();
            }
        });

        db = new MySQLiteHelper(this);

        int alarmid = getIntent().getExtras().getInt("AlarmID");

        alarm = db.getAlarm(alarmid);
        final String ringtoneAlarm = alarm.getRingtone();
        final Alarm alarmSnooze = alarm;


        Button snooze = (Button)findViewById(R.id.buttonSnooze);
        snooze.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                snooze(alarmSnooze.getId(),(int)Math.round((alarmSnooze.getTime() - Math.floor(alarmSnooze.getTime()))* 100.0),alarmSnooze.getSnooze());
                turnAlarmOff();
            }
        });

        if(alarm.getSnooze() == 0)
            snooze.setVisibility(View.GONE);


        if(alarm.getRepeat().equals("FFFFFFF")){
            alarm.setIson(false);
            db.updateAlarm(alarm);
        }

        if(alarm.isFlash())
            flashLightOn();

        currBrightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,0);

        if(alarm.isGradualBrightness()){
            Context context = CameraActivity.this;
            boolean canWriteSettings = Settings.System.canWrite(context);
            if(canWriteSettings) {
                t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                            try {
                                int brightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,0);
                                int temp = (255 - brightness)/5;
                                while(brightness < 255) {
                                    brightness += temp;
                                    Thread.sleep(1000);
                                    if (brightness > 255)
                                        brightness = 255;
                                    Settings.System.putInt(CameraActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                    Settings.System.putInt(CameraActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                                }
                            } catch (InterruptedException e) {
                            }
                    }
                });
            }else
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                context.startActivity(intent);
            }
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        phoneTurnOff = mPreferences.getBoolean("settingPhoneTurnOff",false);


        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "app:mywakelock");
        wakeLock.acquire();

        if(alarm.isVibration()) {
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {1000, 1000, 1000};
            v.vibrate(pattern, 0);
        }


        mp = new MediaPlayer();

        tMusic = new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();
                try {
                    Uri alarmUri;
                    if(ringtoneAlarm == null) {
                        alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                        if (alarmUri == null) {
                            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        }
                    }else{
                        alarmUri = Uri.parse(ringtoneAlarm);
                    }

                    mp.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mp.setDataSource(CameraActivity.this, alarmUri);
                    mp.prepare();
                    mp.setLooping(true);
                    mp.start();
                    mp.setOnErrorListener(new android.media.MediaPlayer.OnErrorListener() {

                        public boolean onError(MediaPlayer mediaplayer, int i, int j)
                        {
                            return false;
                        }
                    });


                } catch (Exception e) {
                }
                if(Thread.interrupted()) {
                    mp.release();
                    return;
                }
                Looper.loop();
            }
        });

        if(tMusic != null)
            tMusic.start();

        List<String> rooms = new ArrayList<String>();
        String path = Environment.getExternalStorageDirectory().toString()+"/WakieWakieQRCodes/";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                rooms.add(files[i].getName().substring(0, files[i].getName().length() - 4));
            }
        }

        Random rand = new Random();
        int n = 0;
        if(rooms != null)
            n = rand.nextInt(rooms.size());
        objective = rooms.get(n);
        TextView txtview = (TextView)findViewById(R.id.textViewOverCamera);
        txtview.setText(getResources().getString(R.string.qr_code_location) + " " + objective);


        if(alarm.isGradualBrightness() && t1 != null && Settings.System.canWrite(CameraActivity.this))
            t1.start();


    }

    private void turnAlarmOff(){
        if(alarm.getRepeat().equals("FFFFFFF")){
            alarm.setIson(false);
            db.updateAlarm(alarm);
        }
        dbstats.addTime((int)Math.floor(alarm.getTime()),(int)Math.round((alarm.getTime() - Math.floor(alarm.getTime()))* 100.0),count);
        T.cancel();
        if(snoozed != 1) {
            if (50 - count > 0)
                mEditor.putInt("points", mPreferences.getInt("points", 0) + (50 - count) + 5);
            else
                mEditor.putInt("points", mPreferences.getInt("points", 0) + 5);
            mEditor.commit();
        }
        if(v != null)
            v.cancel();
        flashLightOff();
        clearFlags();
        closeCamera();
        if(tMusic != null) {
            tMusic.interrupt();
            try{tMusic.stop();}catch (Exception e){}
            mp.release();
        }
        if(t1 != null)
            t1.interrupt();
        if(Settings.System.canWrite(CameraActivity.this)) {
            Settings.System.putInt(CameraActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(CameraActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, currBrightness);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            if(getFrontFacingCameraId(cameraManager) != null && hasCameraFlash) {
                String cameraId = cameraManager.getCameraIdList()[Integer.parseInt(getFrontFacingCameraId(cameraManager))];
                cameraManager.setTorchMode(cameraId, true);
                flashLightStatus = true;
            }
        } catch (CameraAccessException e) {
        }
    }

    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            if(getFrontFacingCameraId(cameraManager) != null && hasCameraFlash) {
            String cameraId = cameraManager.getCameraIdList()[Integer.parseInt(getFrontFacingCameraId(cameraManager))];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
            }
        } catch (CameraAccessException e) {
        }
    }

    String getFrontFacingCameraId(CameraManager cManager){
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId;
            }
        }catch (CameraAccessException e) {
        }
        return null;
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            texture.setDefaultBufferSize(height, width);
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(CameraActivity.this, R.string.error_message_camera_no_permission, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void clearFlags() {
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(phoneTurnOff) {
            super.onWindowFocusChanged(hasFocus);
            if (!hasFocus) {
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(closeDialog);
            }
        }
    }

    private void snooze(int id, int minutes, int snooze){
        snoozed = 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, minutes + snooze);
        calendar.set(Calendar.SECOND, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void QRCodeReader(){
        qrScan.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() == null){
            }else{
                try{
                    JSONObject obj = new JSONObject(result.getContents());
                    if(objective.equals(obj.getString("name")))
                        turnAlarmOff();
                    else
                        Toast.makeText(this, getResources().getString(R.string.qr_code_wrong), Toast.LENGTH_LONG).show();
                }catch(JSONException e){

                }
            }
        }else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }



}