package com.linternagratismovil;


import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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
    private String cameraId;
    private CameraManager cameraManager;

    private SensorManager sensorManager;
    private ShakeDetector sd;

    private String master;

    //Layout elements
    @BindView(R.id.turn_on_off) ImageView turn_on_off;
    @BindView(R.id.valorar) ImageView valorar;
    @BindView(R.id.compartir) ImageView compartir;
    @BindView(R.id.contacto) ImageView contacto;
    @BindView(R.id.shake) ImageView shake;
    @BindView(R.id.banner_main) AdView banner_main;


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
        //cameraManagerG = cameraManager;


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.setSensitivity(ShakeDetector.SENSITIVITY_MEDIUM);
        sd.start(sensorManager);

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

                shaking = !shaking;
                if(shaking){
                    shake.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shake_on));
                    sd.start(sensorManager);
                }
                else{
                    shake.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shake_off));
                    sd.stop();
                }

            }
        });


        turn_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    cameraId = cameraManager.getCameraIdList()[0]; //Back camera
                    cameraManager.setTorchMode(cameraId, !state);
                    state = !state;
                    if (state) {
                        turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_on));

                    } else {
                        turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_off));
                    }

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    getParent().finish();
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sd.stop();
    }

    @Override
    public void hearShake() {
        try {
            cameraId = cameraManager.getCameraIdList()[0]; //Back camera
            cameraManager.setTorchMode(cameraId, !state);
            state = !state;
            if (state) {
                turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_on));

            } else {
                turn_on_off.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.torch_off));
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
}