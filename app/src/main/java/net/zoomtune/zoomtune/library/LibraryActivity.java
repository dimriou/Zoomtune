package net.zoomtune.zoomtune.library;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import net.zoomtune.zoomtune.GlobalVariables;
import net.zoomtune.zoomtune.R;
import net.zoomtune.zoomtune.preview.PreviewActivity;

import java.io.File;
import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity {

    final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Zoomtune");
    private final static String TAG = LibraryActivity.class.getSimpleName();
    private VideoAdapter mAdapter;
    // *** WARNING: fileList--bitmapList MUST ALWAYS point at the same position (linked)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Back button.
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Check if folder is present.
        if (!file.exists()) {
            boolean rv = file.mkdir();
            Log.d(TAG, "Folder creation " + (rv ? "success" : "failed"));
        } else {
            Log.d(TAG, "Folder already exists.");
        }

        // Create library list.
        Thread thread = new Thread() {
          public void run () {
              try {
                  GlobalVariables.createList();
              } catch (RuntimeException e) {
                  Log.e(TAG, e.toString());
              }
          }
        };
        thread.run();


        GridView gridView = findViewById(R.id.gridView1);
        ViewCompat.setNestedScrollingEnabled(gridView,true);
        mAdapter = new VideoAdapter(this, GlobalVariables.getFileList());
        if (GlobalVariables.getFileList() != null) {
            gridView.setAdapter(mAdapter);
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        mAdapter.refresh();
    }

    public class VideoAdapter extends BaseAdapter {
        private Context context;

        private final ArrayList<String> VideoValues ;

        VideoAdapter(Context context, ArrayList<String> VideoValues) {
            this.context = context;
            this.VideoValues = VideoValues;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = convertView;

            if (view == null) {
                view = inflater.inflate(R.layout.grid_item, null);
            }

            // set value into textview
            TextView textView = view.findViewById(R.id.grid_item_label);
            final String temp = (String) GlobalVariables.getFileList().get(position);
            final String modified = temp.substring(4,8) + "/" + temp.substring(8,10) + "/" + temp.substring(10,12) +
                    "  " + temp.substring(13,15) + ":" + temp.substring(15,17);
            textView.setText( modified );

            // set image
            ImageView imageThumbnail = view.findViewById(R.id.grid_item_image);

            Bitmap bmThumbnail;
            bmThumbnail = (Bitmap) GlobalVariables.getDataList().get(temp);

            if (bmThumbnail != null) {
                imageThumbnail.setImageBitmap(bmThumbnail);
                imageThumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String videoFilePath = GlobalVariables.getStoragePath() + GlobalVariables.getFileList().get(position);
                        Intent intent = new Intent(LibraryActivity.this, PreviewActivity.class);
                        intent.putExtra("id", videoFilePath);
                        intent.putExtra("date", modified);
                        intent.putExtra("position", position);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in_preview, R.anim.fade_out_preview);
                    }
                });

                imageThumbnail.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Snackbar snackbar = Snackbar
                                .make(v, "Are you sure you want to delete this Zoomtune?", Snackbar.LENGTH_LONG)
                                .setAction("YES", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        String videoFilePath = GlobalVariables.getStoragePath() + GlobalVariables.getFileList().get(position);
                                        File file = new File(videoFilePath);
                                        boolean deleted = file.delete();

                                        if (!deleted) {
                                            Toast.makeText(LibraryActivity.this, "Error on delete!", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            GlobalVariables.getDataList().remove(GlobalVariables.getFileList().get(position));
                                            GlobalVariables.getFileList().remove(position);
                                            //gb.createList();
                                            refresh();
                                            Toast.makeText(LibraryActivity.this, "Zoomtune deleted!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        snackbar.show();
                        return true;
                    }
                });

            } else {
                Log.e(TAG, "Error on creating thumbnail!");
            }

            return view;
        }

        private void refresh(){
            this.notifyDataSetChanged();
        }

        public int getCount() { return VideoValues.size(); }

        public Object getItem(int position) { return position; }

        public long getItemId(int position) { return 0; }
    }

}
