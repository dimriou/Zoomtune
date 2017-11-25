package net.zoomtune.zoomtune.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import net.zoomtune.zoomtune.GlobalVariables;
import net.zoomtune.zoomtune.R;
import net.zoomtune.zoomtune.camera.CameraActivity;
import net.zoomtune.zoomtune.zoom.Sound;

import java.util.ArrayList;

public class SplashActivity extends Activity {

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        new GlobalVariables();

        GlobalVariables.soundList = new ArrayList<>();
        for (String name : GlobalVariables.getNames()) {
            GlobalVariables.soundList.add(new Sound(name.toUpperCase(),
                    this.getResources().getIdentifier(name, "drawable", this.getPackageName())));
        }

        if ( checkCameraPermission() && checkReadExternalPermission() && checkWriteExternalPermission()) {
            flag = true;
        }
        else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Toast.makeText(SplashActivity.this, "Check permissions!",
                        Toast.LENGTH_LONG).show();
                SplashActivity.this.finish();
            }
            else {
                flag = false;
            }
        }

        int SPLASH_TIME_OUT = 600;
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app icon / company
             */
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                if (flag) {
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, CameraActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    // close this activity
                    finish();
                }
                else {
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, PermissionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    // close this activity
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);

    }

    private boolean checkWriteExternalPermission()
    {

        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = SplashActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkReadExternalPermission()
    {

        String permission = "android.permission.READ_EXTERNAL_STORAGE";
        int res = SplashActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkCameraPermission()
    {

        String permission = "android.permission.CAMERA";
        int res = SplashActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

}
