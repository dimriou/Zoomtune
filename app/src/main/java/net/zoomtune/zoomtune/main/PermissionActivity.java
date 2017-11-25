package net.zoomtune.zoomtune.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import net.zoomtune.zoomtune.R;
import net.zoomtune.zoomtune.camera.CameraActivity;

public class PermissionActivity extends Activity {

    public static final int PERMISSION_READ_WRITE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_activity);

        String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_READ_WRITE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_READ_WRITE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent2 = new Intent();
                intent2.setClass(PermissionActivity.this, CameraActivity.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                PermissionActivity.this.finish();
            } else {
                Toast.makeText(PermissionActivity.this, "Access internal storage permission denied!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
