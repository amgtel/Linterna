package com.linternagratismovil;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.squareup.seismic.ShakeDetector;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener {

    private boolean state = false; //True (light on) - False (light off)
    private boolean shaking = false; //False (shaking not active) - True (shaking active)
    private SensorManager sensorManager;
    private ShakeDetector sd;

    private String cameraId;
    private CameraManager cameraManager;


    //Layout elements
    @BindView(R.id.turn_on_off) ImageView turn_on_off;
    @BindView(R.id.valorar) ImageView valorar;
    @BindView(R.id.compartir) ImageView compartir;
    @BindView(R.id.contacto) ImageView contacto;
    @BindView(R.id.shake) ImageView shake;
    @BindView(R.id.banner_main) AdView banner_main;

    //Channel
    private String CHANNEL_ID = "555";
    private int notificationId = 555;
    private NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get references layout
        ButterKnife.bind(this);

        //Admob
        //Initialize Admob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //init banner
        banner_main = findViewById(R.id.banner_main);
        AdRequest adRequest = new AdRequest.Builder().build();
        banner_main.loadAd(adRequest);
        //----End Admob


        //Flash control
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        //Shake control
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.setSensitivity(ShakeDetector.SENSITIVITY_MEDIUM);

        //Options control
        valorar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent rateIntent = new Intent(Intent.ACTION_VIEW);
                rateIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.linternagratismovil"));
                startActivity(rateIntent);

            }
        });

        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Download " + " https://play.google.com/store/apps/details?id=com.linternagratismovil");
                shareIntent.setType("text/plain");
                startActivity(shareIntent);

            }
        });

        contacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:info@justradioapp.com"));
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            }
        });

        shake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!shaking){
                    shake.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shake_on));
                    sd.start(sensorManager);
                    Toast.makeText(getApplicationContext(),"Shake ON!!",Toast.LENGTH_SHORT).show();
                }
                else{
                    shake.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shake_off));
                    sd.stop();
                    Toast.makeText(getApplicationContext(),"Shake OFF!!",Toast.LENGTH_SHORT).show();

                }
                shaking = !shaking;

            }
        });


        //Ligh control
        turn_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    cameraId = cameraManager.getCameraIdList()[0]; //Back camera
                    cameraManager.setTorchMode(cameraId, !state);
                    state = !state;
                    if (state) {
                        turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_on));
                        notificationManager.notify(notificationId, builder.build());


                    } else {
                        turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_off));
                        notificationManager.cancel(notificationId);
                    }

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    getParent().finish();
                }

            }
        });

        //Notification control
        createNotificaton();

    }



    @Override
    protected void onDestroy() {
        notificationManager.cancel(notificationId);
        sd.stop();

        super.onDestroy();

    }

    //Shake result
    @Override
    public void hearShake() {
        try {
            cameraId = cameraManager.getCameraIdList()[0]; //Back camera
            cameraManager.setTorchMode(cameraId, !state);
            state = !state;
            if (state) {
                turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_on));
                notificationManager.notify(notificationId, builder.build());


            } else {
                turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_off));
                notificationManager.cancel(notificationId);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void createNotificaton(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notification";
            String description = "torch on";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
        .setOngoing(true);

        notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        //notificationManager.notify(notificationId, builder.build());

    }
}