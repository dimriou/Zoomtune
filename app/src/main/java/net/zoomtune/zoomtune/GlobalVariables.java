package net.zoomtune.zoomtune;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;

import net.zoomtune.zoomtune.zoom.Sound;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class GlobalVariables {
    private static HashMap<String, Bitmap> dataList = null;
    private static ArrayList<String> fileList = null;
    private final static String STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.
            DIRECTORY_DCIM).toString() + "/Zoomtune/";

    private final static String TUNE_PATH = STORAGE_PATH + ".Tunes/";
    public static ArrayList<Sound> soundList = null;

    public static String[] getNames() {
        return names;
    }

    private static String [] names = {"angel", "angry", "confused", "creep", "misery"
            , "playful", "rip", "scary", "sexy", "silence", "sleep"
            , "thug", "wink"
    };

    public GlobalVariables() {
        dataList = new HashMap<>();
        fileList = new ArrayList<>();
        soundList = new ArrayList<>();
    }

    public static HashMap getDataList() {
        return dataList;
    }

    public static ArrayList getFileList() {
        return fileList;
    }

    public static String getStoragePath() {
        return STORAGE_PATH;
    }

    public static String getTunePath() {
        return TUNE_PATH;
    }

    public static void createList() {
        dataList.clear();
        fileList.clear();

        File videoFiles = new File(STORAGE_PATH);

        if (videoFiles.isDirectory()) {
            for (File f : videoFiles.listFiles()) {
                if (f.isFile()) {
                    if(f.getName().startsWith("VID")) {
                        if(f.length() == 0) {
                            f.delete();
                        } else {
                            fileList.add(f.getName());
                            Bitmap bmThumbnail;
                            bmThumbnail = ThumbnailUtils.createVideoThumbnail(STORAGE_PATH
                                            + f.getName(),
                                    MediaStore.Video.Thumbnails.MINI_KIND);
                            dataList.put(f.getName(), bmThumbnail);
                        }
                    }
                }
            }
            // Sort fileList.
            Collections.sort(fileList, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });
            Collections.reverse(fileList);
        }
        if (fileList == null) {
            System.out.println("File does not exist");
        }

    }
}
