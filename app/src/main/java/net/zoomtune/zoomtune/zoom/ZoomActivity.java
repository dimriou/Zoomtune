package net.zoomtune.zoomtune.zoom;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.zoomtune.zoomtune.GlobalVariables;
import net.zoomtune.zoomtune.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ZoomActivity extends Activity {

    private final String TAG = ZoomActivity.class.getSimpleName();
    private final int AUDIO_INTENT = 2;

    // Zoom variables.
    private final String SCALE = "1200";          //Scale of image before process.
    private final String ZOOM_FACTOR = "1.5";
    private final String DURATION = "7";
    private final String OUTPUT_RESO_X = "480";
    private String OUTPUT_RESO_Y;
    private final String FRAMES = "125";
    private final String ZOOM_SPEED = "0.004";   //ZOOM_SPEED = (ZOOM_FACTOR - 1)/FRAMES

    private long mLastClickLibrary = 0;
    private long mLastClickCreate = 0;

    public Intent intent;

    public static MediaPlayer mPlayer = null;
    private RecyclerView recyclerViewer;
    private HorizontalAdapter horizontalAdapter;
    private ImageButton musicLibrary, homeButton, createButton;
    private ImageView customImageHolder;
    private TextView title;

    private String audioPath,picturePath;

    public static int sizeX, sizeY;
    public double selectorX = 0;
    public double selectorY = 0;

    public static int getSizeX() {
        return sizeX;
    }

    public static int getSizeY() {
        return sizeY;
    }

    public void setTitle(String title) {

        this.title.setText(title);
        audioPath = GlobalVariables.getTunePath() + title.toLowerCase() + ".mp3";

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.zoom_activity);

        Intent intent = getIntent();
        picturePath = intent.getStringExtra("path");
        sizeX = intent.getIntExtra("sizeX", 0);
        sizeY = intent.getIntExtra("sizeY", 0);

        // Create OUTPUT_RESO_Y based on the aspect ratio of the camera.
        // The output should be divisible by 2.
        int temp = ( Integer.parseInt(OUTPUT_RESO_X) * sizeY ) / sizeX;
        if ( (temp%2) != 0) {
            temp = temp - 1;
            OUTPUT_RESO_Y = String.valueOf(temp);
        } else {
            OUTPUT_RESO_Y = String.valueOf(temp);
        }

        new notifyGallery().execute();

        customImageHolder = findViewById(R.id.imageView);

        musicLibrary = findViewById(R.id.button_musiclibrary);
        musicLibrary.setOnClickListener(musicLibraryListener);

        homeButton = findViewById(R.id.button_back);
        homeButton.setOnClickListener(backButtonListener);

        createButton = findViewById(R.id.button_create);
        createButton.setOnClickListener(createButtonListener);

        title = findViewById(R.id.soundTitle);
        title.setText(GlobalVariables.soundList.get(0).name);
        setBitmap();

        recyclerViewer = findViewById(R.id.musicList);
        horizontalAdapter = new HorizontalAdapter(GlobalVariables.soundList, this);

        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(ZoomActivity.this, LinearLayoutManager.HORIZONTAL,
                false);
        recyclerViewer.setLayoutManager(horizontalLayoutManager);
        recyclerViewer.setAdapter(horizontalAdapter);

    }



    View.OnClickListener createButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickCreate < 2000){
                return;
            }
            mLastClickCreate = SystemClock.elapsedRealtime();
            stopPlaying();
            if (audioPath == null){
                Toast.makeText(ZoomActivity.this, "Please choose tune first!", Toast.LENGTH_SHORT).show();
                return;
            }
            final String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            selector();

            intent = new Intent(ZoomActivity.this, ProgressActivity.class);
            intent.putExtra("picturePath", picturePath);
            intent.putExtra("audioPath", audioPath);
            intent.putExtra("selectorX", selectorX);
            intent.putExtra("selectorY", selectorY);
            intent.putExtra("timestamp", timestamp);
            intent.putExtra("OUTPUT_RESO_Y", OUTPUT_RESO_Y);

            String id = GlobalVariables.getStoragePath() + "VID_" + timestamp + ".mp4";
            String name = "VID_" + timestamp + ".mp4";
            intent.putExtra("videoName", name);
            final String modified = name.substring(4,8) + "/" + name.substring(8,10) + "/" + name.substring(10,12) +
                    "  " + name.substring(13,15) + ":" + name.substring(15,17);
            intent.putExtra("id", id);
            intent.putExtra("date", modified);
            intent.putExtra("state", 1);                        //State coming from Zoom
            picturePath = null;
            audioPath = null;
            startActivity(intent);

        }
    };

    View.OnClickListener backButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopPlaying();
            File file = new File(picturePath);
            boolean deleted = file.delete();

            if (!deleted) {
                ZoomActivity.this.finish();
            } else {
                ZoomActivity.this.finish();
            }
        }
    };

    View.OnClickListener musicLibraryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickLibrary < 1000){
                return;
            }
            mLastClickLibrary = SystemClock.elapsedRealtime();
            stopPlaying();
            Intent intent_upload = new Intent();
            intent_upload.setType("audio/*");
            intent_upload.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent_upload, AUDIO_INTENT);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUDIO_INTENT && resultCode == RESULT_OK && null != data) {
            Uri selectedSound = data.getData();
            getPathfromUri(selectedSound);
        }
    }

    private void getPathfromUri(Uri uri){
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri,  projection,
                null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            audioPath = cursor.getString(column_index);
            cursor.close();
        }
    }

    public void selector(){
        CircleView.CircleArea ca ;
        ca = CircleView.getCircle();

        if(ca != null) {
            double ratio;
            Log.d(TAG, "SizeXY: : " + sizeX + " - " + sizeY);
            if (sizeY > sizeX) {
                ratio = (double) sizeY / (double) sizeX;
            }
            else {
                ratio = (double) sizeX / (double) sizeY;
            }
            Log.d(TAG, "Ratio : " + ratio);
            sizeX = Integer.parseInt(SCALE);
            Log.d(TAG, "Size X : " + sizeX);
            double temp = sizeX * ratio;
            Log.d(TAG, "Temp: " + temp);
            sizeY = (int) Math.round(temp);
            Log.d(TAG, "Size Y : " + sizeY);

            double actualCenterX = (ca.centerX * sizeX) / CircleView.get_width();
            Log.d(TAG, "Actual center x : " + actualCenterX);
            double rectSizeX = sizeX / Float.parseFloat(ZOOM_FACTOR);
            Log.d(TAG, "RectSizeX: " + rectSizeX);
            double startX = actualCenterX - (rectSizeX / 2);
            Log.d(TAG, "StartX: " + startX);
            selectorX = startX/Integer.parseInt(FRAMES);
            Log.d(TAG, "SelectorX: " + selectorX);


            double actualCenterY = (ca.centerY * sizeY) / CircleView.get_height();
            Log.d(TAG, "Actual center y : " + actualCenterY);
            double rectSizeY = sizeY / Float.parseFloat(ZOOM_FACTOR);
            Log.d(TAG, "RectSizeY: " + rectSizeY);
            double startY = actualCenterY - (rectSizeY / 2);
            Log.d(TAG, "StartY: " + startY);
            selectorY = startY/Integer.parseInt(FRAMES);
            Log.d(TAG, "SelectorY: " + selectorY);
        }
    }

    private void setBitmap() {
        BitmapFactory.Options options;

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            customImageHolder.setImageBitmap(bitmap);
        } catch (OutOfMemoryError e) {
            try {
                options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
                customImageHolder.setImageBitmap(bitmap);
            } catch(Exception a) {
            }
        }

    }

    private class notifyGallery extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(picturePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            ZoomActivity.this.sendBroadcast(mediaScanIntent);

            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlaying();
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
