package net.zoomtune.zoomtune.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;

import net.zoomtune.zoomtune.GlobalVariables;
import net.zoomtune.zoomtune.R;
import net.zoomtune.zoomtune.library.LibraryActivity;
import net.zoomtune.zoomtune.zoom.ZoomActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity {

    private static String TAG = CameraActivity.class.getSimpleName();

    private CameraView camera;
    private ImageButton capture, library, gallery, switchCamera;

    public String picturePath = null;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int MIN_RESO = 439;
    private static final int OUTPUT_SIZE = 1400;         //1500
    private static final String HELPER = ".zoomtune_helper.jpg";
    private static final String HELPER_DIR = ".Helper/";

    private Dialog dialog;
    private CropImageView mCropView;

    private long mLastClickCamera = 0;
    private long mLastClickGallery = 0;
    private long mLastClickCrop = 0;
    private int height;
    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        if (!hasCamera()) {
            Toast toast = Toast.makeText(this, "Sorry, your phone does not have a camera!",
                    Toast.LENGTH_LONG);
            toast.show();
            this.finish();
        }
        initialize();

    }

    private void initialize() {

        camera = findViewById(R.id.camera);
        camera.setFacing(Facing.FRONT);
        camera.setCameraListener(cameraListener);

        capture = findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        switchCamera = findViewById(R.id.button_swap);
        switchCamera.setOnClickListener(switchCameraListener);

        library = findViewById(R.id.button_library);
        library.setOnClickListener(libraryListener);

        gallery = findViewById(R.id.button_gallery);
        gallery.setOnClickListener(galleryListener);

        if (!checkStoragePaths()){
            for (String name : GlobalVariables.getNames()) {
                saveas(getResources().getIdentifier(name, "raw", getPackageName()), name);
            }
        }

    }

    public boolean checkStoragePaths(){
        boolean create;
        boolean exists = (new File(GlobalVariables.getStoragePath())).exists();
        if (!exists) {
            new File(GlobalVariables.getStoragePath()).mkdirs();
        }

        boolean exists2 = (new File(GlobalVariables.getTunePath())).exists();
        if (!exists2) {
            new File(GlobalVariables.getTunePath()).mkdirs();
        }
        create = exists && exists2;

        return create;
    }

    private boolean saveas(int res, String name) {
        byte[] buffer;
        InputStream fIn = getBaseContext().getResources().openRawResource(res);
        int size ;

        try {
            size = fIn.available();
            buffer = new byte[size];
            fIn.read(buffer);
            fIn.close();
        } catch (IOException e) {
            return false;
        }

        String filename = name + ".mp3";

        FileOutputStream save;
        try {
            save = new FileOutputStream(GlobalVariables.getTunePath() + filename);
            save.write(buffer);
            save.flush();
            save.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean hasCamera() {
        //check if the device has camera
        int numCameras = Camera.getNumberOfCameras();
        return numCameras > 0;
    }

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            camera.toggleFacing();
        }
    };

    View.OnClickListener libraryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(CameraActivity.this, LibraryActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener galleryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickGallery < 1000){
                return;
            }
            mLastClickGallery = SystemClock.elapsedRealtime();
            Intent i = new Intent(
                    Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    };

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickCamera < 6000){
                return;
            }
            mLastClickCamera = SystemClock.elapsedRealtime();
//            camera.capturePicture();
            camera.captureSnapshot();
        }
    };

    CameraListener cameraListener = new CameraListener() {
        @Override
        public void onPictureTaken(byte[] data) {
            File pictureFile = getOutputMediaFile();
            camera.stop();
            if (pictureFile == null) {
                Toast.makeText(CameraActivity.this, "Can't create file, check permissions!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap temp = makeBitmap(data);
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                temp.compress(Bitmap.CompressFormat.JPEG,80,fos);
                fos.close();
                temp.recycle();
                Intent intent = new Intent(CameraActivity.this, ZoomActivity.class);
                intent.putExtra("path", pictureFile.getAbsolutePath());
                intent.putExtra("sizeX", width);
                intent.putExtra("sizeY", height);
                startActivity(intent);
            } catch (IOException e) {
                Toast.makeText(CameraActivity.this, "Error saving picture!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "getPictureCallback:" + e);
            }


        }
    };

    public Bitmap makeBitmap(byte[] data) {
        Matrix matrix = new Matrix();
        // Convert ByteArray to Bitmap
        Bitmap bitPic = BitmapFactory.decodeByteArray(data, 0, data.length);
        Log.d(TAG, "Bitmap:" + bitPic.getWidth() + "-" + bitPic.getHeight());
        height = bitPic.getHeight();
        width = bitPic.getWidth();

        // Perform matrix rotations/mirrors depending on camera that took the photo
        if (Facing.FRONT == camera.getFacing()) {
            float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);
            matrix.postConcat(matrixMirrorY);
            // Create new Bitmap out of the old one
            return Bitmap.createBitmap(bitPic, 0, 0, bitPic.getWidth(),
                    bitPic.getHeight(),matrix, true);
        }

//        Bitmap croppedBitmap = Bitmap.createBitmap(bitPicFinal, 0,bitPicFinal.getHeight() / 2 - bitPicFinal.getWidth()
//                / 2,1080, 1920);
//        croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 1080, 1920, true);
        return bitPic;
    }

    /** Get image capture save path. **/
    private static File getOutputMediaFile() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File(GlobalVariables.getStoragePath());

        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //and make a media file:
        mediaStorageDir = new File(GlobalVariables.getStoragePath() + "IMG_" + timeStamp + ".jpeg");

        return mediaStorageDir;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            // Get image path.
            Uri selectedImage = data.getData();
            getPathfromUri(selectedImage);
            createDialog();

        } else {
            Log.e(TAG, "No Activity Catch!");
        }
    }

    private void getPathfromUri(Uri uri){
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri,  projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            picturePath = cursor.getString(column_index);
            Log.d(TAG, "Path created:" + picturePath);

            cursor.close();
        }
    }

    private void createDialog(){
        // Get size of image.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, options);
        Log.d(TAG, "Image("+picturePath+") h:" + options.outHeight + " Image w:" + options.outWidth);

        File file = new File(GlobalVariables.getStoragePath() + HELPER_DIR);
        // Check if folder is present.
        if (!file.exists()) {
            boolean rv = file.mkdir();
            Log.d(TAG, "Folder creation " + (rv ? "success" : "failed"));
        } else {
            Log.d(TAG, "Folder already exists.");
        }

        if (options.outHeight > MIN_RESO && options.outWidth > MIN_RESO) {
            showMyDialog(CameraActivity.this);
        } else {
            Toast.makeText(CameraActivity.this, "Invalid image size!", Toast.LENGTH_SHORT).show();
        }

    }

    private void showMyDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.crop_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        mCropView = dialog.findViewById(R.id.imgCrop);

        Button cropButton = dialog.findViewById(R.id.crop);
        cropButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickCrop < 5000){
                    return;
                }
                mLastClickCrop = SystemClock.elapsedRealtime();
                picturePath = GlobalVariables.getStoragePath() + HELPER_DIR + HELPER;
                mCropView.startCrop(Uri.fromFile(new File(picturePath)),
                        new CropCallback() {
                            @Override
                            public void onSuccess(Bitmap cropped) {
                                System.out.println("H/W:" + cropped.getHeight() + "/" + cropped.getWidth());
                                File pictureFile = getCropMediaFile(picturePath);
                                if (pictureFile == null) {
                                    Toast.makeText(CameraActivity.this, "Can't create file, check permissions!",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                try {
                                    FileOutputStream fos = new FileOutputStream(pictureFile);
                                    cropped.compress(Bitmap.CompressFormat.PNG,60,fos);
                                    fos.close();
                                } catch (IOException e) {
                                    Toast.makeText(CameraActivity.this, "Error saving picture!" + e, Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "getPictureCallback:" + e);
                                }
                                dialog.hide();
                                Intent intent = new Intent(CameraActivity.this, ZoomActivity.class);
                                intent.putExtra("path", picturePath);
                                intent.putExtra("sizeX", cropped.getWidth());
                                intent.putExtra("sizeY", cropped.getHeight());
                                startActivity(intent);

                            }

                            @Override
                            public void onError() {}
                        },

                        new SaveCallback() {
                            @Override
                            public void onSuccess(Uri outputUri) {
                            }

                            @Override
                            public void onError() {}
                        }
                );


            }
        });

        Uri sourceUri = Uri.fromFile(new File(picturePath));
        mCropView.setCropMode(CropImageView.CropMode.RATIO_3_4);
        mCropView.setHandleShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH);
        mCropView.setCompressQuality(0);
        mCropView.startLoad(sourceUri,
                new LoadCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });
        mCropView.setOutputMaxSize(OUTPUT_SIZE,OUTPUT_SIZE);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dialogWidth = (displayMetrics.widthPixels);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.90 );
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);

        dialog.show();
    }

    /** Get image capture save path. **/
    private static File getCropMediaFile(String path) {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File(path);

        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //Make a media file:
        mediaStorageDir = new File(path);

        return mediaStorageDir;
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
        mLastClickCamera = 0;
        mLastClickCrop = 0;
        mLastClickGallery = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }
}
