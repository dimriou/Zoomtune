package net.zoomtune.zoomtune.zoom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import net.zoomtune.zoomtune.GlobalVariables;
import net.zoomtune.zoomtune.R;
import net.zoomtune.zoomtune.camera.CameraActivity;
import net.zoomtune.zoomtune.preview.PreviewActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProgressActivity extends Activity {

    private final String TAG = ProgressActivity.class.getSimpleName();

    // Zoom variables.
    private final String SCALE = "1200";          //Scale of image before process.
    private final String ZOOM_FACTOR = "1.5";
    private final String DURATION = "7";
    private final String OUTPUT_RESO_X = "480";
    private String OUTPUT_RESO_Y;
    private final String FRAMES = "125";
    private final String ZOOM_SPEED = "0.004";   //ZOOM_SPEED = (ZOOM_FACTOR - 1)/FRAMES

    private FFmpeg ffloader;
    private ProgressBar progressBar;
    private String videoName;
    public Intent newIntent = null;

    public String videoPath;
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_activity);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        String picturePath = intent.getStringExtra("picturePath");
        String audioPath = intent.getStringExtra("audioPath");
        Double selectorX = intent.getDoubleExtra("selectorX", 0);
        Double selectorY = intent.getDoubleExtra("selectorY", 0);
        String timestamp = intent.getStringExtra("timestamp");
        videoName = intent.getStringExtra("videoName");
        OUTPUT_RESO_Y = intent.getStringExtra("OUTPUT_RESO_Y");
        videoPath = intent.getStringExtra("id");
        final String id = intent.getStringExtra("id");
        final String modified = intent.getStringExtra("date");

        newIntent = new Intent(ProgressActivity.this, PreviewActivity.class);
        newIntent.putExtra("id", id);
        newIntent.putExtra("date", modified);
        newIntent.putExtra("state", 1);                        //State coming from Zoom

        ffloader = FFmpeg.getInstance(this);
        try {
            ffloader.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        String[] cmd = new String[]{"-i", picturePath, "-i", audioPath, "-preset", "ultrafast", "-vf",
                "scale=" + SCALE + ":-1,zoompan=z='min(zoom+" + ZOOM_SPEED + "," +
                        ZOOM_FACTOR + ")':d=" + FRAMES + ":x='if(gte(zoom," + ZOOM_FACTOR +
                        "),x,x+" + String.valueOf(selectorX) + ")':y='if(gte(zoom," + ZOOM_FACTOR +
                        "),y,y+" + String.valueOf(selectorY) + ")':s=" + OUTPUT_RESO_X + "x" +
                        OUTPUT_RESO_Y + ",drawtext=fontfile=/system/fonts/DroidSansMono.ttf:fontcolor=white:text='zoomtune.net'" +
                        ":fontsize=30:x=(w-text_w-50):y=(h-text_h-10)",
                "-r", "25","-pix_fmt", "yuv420p", "-t", DURATION,
                GlobalVariables.getStoragePath() + "VID_" + timestamp + ".mp4"
        };

        setProgress();
        try {
            ffloader.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, "Error" + message);
                    Toast.makeText(ProgressActivity.this, "There was an error creating zoomtune!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProgressActivity.this, CameraActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onSuccess(String message) {
                    Log.v(TAG, "Success" + message);
                    new videoNotify().execute();
                    GlobalVariables.getFileList().add(0, videoName );
                    Bitmap bmThumbnail;
                    bmThumbnail = ThumbnailUtils.createVideoThumbnail(id,
                            MediaStore.Video.Thumbnails.MINI_KIND);
                    GlobalVariables.getDataList().put(videoName,bmThumbnail);
                    success = true;
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
    }

    public void activateIntent() {
        startActivity(newIntent);
    }

    private void setProgress() {

        // set the progress
        progressBar.setProgress(2);
        // thread is used to change the progress value
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!success) {
                        if (progressBar.getProgress() < progressBar.getMax() - 4) {
                            progressBar.incrementProgressBy(1);
                        }
                        Thread.sleep(200);
                    }
                    while (progressBar.getProgress() < progressBar.getMax()) {
                        progressBar.incrementProgressBy(1);
                        Thread.sleep(10);
                    }
                    activateIntent();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public class videoNotify extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(videoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            ProgressActivity.this.sendBroadcast(mediaScanIntent);

            return null;
        }
    }
}
