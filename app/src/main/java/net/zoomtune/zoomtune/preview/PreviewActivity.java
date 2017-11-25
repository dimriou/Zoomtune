package net.zoomtune.zoomtune.preview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import net.zoomtune.zoomtune.GlobalVariables;
import net.zoomtune.zoomtune.R;
import net.zoomtune.zoomtune.camera.CameraActivity;

import java.io.File;

public class PreviewActivity extends Activity {
    private final static String TAG = PreviewActivity.class.getSimpleName();

    public String videoFilePath;
    public Button previewDelete;
    public Button previewShare;
    public VideoView previewVideo;
    public MediaController mediaControls;
    public TextView dateText;
    // *** WARNING: fileList--bitmapList MUST ALWAYS point at the same position (linked)
    public int position;
    public int state;
    private long mLastClickDelete = 0;
    private long mLastClickShare = 0;

    String MiME_TYPE = "video/mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_activity);
        Intent intent = getIntent();

        videoFilePath = intent.getStringExtra("id");
        position = intent.getIntExtra("position",0);
        dateText = findViewById(R.id.dateText);
        dateText.setText(intent.getStringExtra("date"));
        state = intent.getIntExtra("state", 0);

        previewDelete = findViewById(R.id.previewDelete);
        previewShare = findViewById(R.id.previewShare);
        previewVideo = findViewById(R.id.previewVideo);


        previewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickDelete < 1500){
                    return;
                }
                mLastClickDelete = SystemClock.elapsedRealtime();
                AlertDialog diaBox = AskOption();
                diaBox.show();

            }
        });

        previewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickShare < 1500){
                    return;
                }
                mLastClickShare = SystemClock.elapsedRealtime();
                File file = new File(videoFilePath);
                Log.d(TAG, "-------------newFile:"+file.exists() + "    "  + file.toString());//True here
                Uri uri = FileProvider.getUriForFile(PreviewActivity.this,
                        "net.zoomtune.zoomtune.fileprovider", file);

                Intent intent = ShareCompat.IntentBuilder.from(PreviewActivity.this)
                        .setType(MiME_TYPE)
                        .setStream(uri)
                        .setChooserTitle("Choose Application")
                        .createChooserIntent()
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                PackageManager pm = PreviewActivity.this.getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    PreviewActivity.this.startActivity(intent);
                }
            }
        });


        //set the media controller buttons
        if (mediaControls == null) {
            mediaControls = new MediaController(this);
        }
        try {

            //set the media controller in the VideoView
            previewVideo.setMediaController(mediaControls);
            previewVideo.setVideoPath(videoFilePath);
            previewVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mediaControls.show(0);
                    previewVideo.start();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_change, R.anim.slide_out);
    }

    private AlertDialog AskOption()
    {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this Zoomtune?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        File file = new File(videoFilePath);
                        boolean deleted = file.delete();

                        // On delete check if we come from Library or Zoom activity.
                        if (!deleted) {
                            Toast.makeText(PreviewActivity.this, "Error on delete!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            if (state == 1) {
                                Intent intent = new Intent(PreviewActivity.this, CameraActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            PreviewActivity.this.finish();
                        } else {
                            GlobalVariables.getDataList().remove(position);
                            GlobalVariables.getFileList().remove(position);
                            Toast.makeText(PreviewActivity.this, "Zoomtune was deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            if (state == 1) {
                                Intent intent = new Intent(PreviewActivity.this, CameraActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            PreviewActivity.this.finish();
                        }

                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (state == 1) {
            Intent intent = new Intent(PreviewActivity.this, CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
