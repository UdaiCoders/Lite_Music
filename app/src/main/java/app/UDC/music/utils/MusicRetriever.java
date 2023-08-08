package app.UDC.music.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import app.UDC.music.data.ThisApp;
import app.UDC.music.model.MusicItem;

/**
 * Retrieves and organizes media to play. Before being used, you must call {@link #prepare()},
 * which will retrieve all of the music on the user's device (by performing a query on a content
 * resolver). After that, it's ready to retrieve a random song, with its title and URI, upon
 * request.
 */
public class MusicRetriever {
    final String TAG = "MusicRetriever";

    private static ContentResolver mContentResolver;
    private Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    // the items (songs) we have queried
    ArrayList<MusicItem> mItems = new ArrayList<MusicItem>();

    Random mRandom = new Random();

    public MusicRetriever() {
        mContentResolver = ThisApp.get().getContentResolver();
    }

    /**
     * Loads music data. This method may take long, so be sure to call it asynchronously without
     * blocking the main thread.
     */
    public void prepare() {
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = mContentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);

            while (cursor.moveToNext()) {
                // Get values of columns for a given Audio.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                String artist = cursor.getString(artistColumn);
                String album = cursor.getString(albumColumn);

                Log.i(TAG, "ID: " + id + " Title: " + name);
                mItems.add(new MusicItem(id, artist, name, album, duration));
                Log.i(TAG, "Done querying media. MusicRetriever is ready.");
            }
        }
    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    public ArrayList<MusicItem> getAllItem() {
        return mItems;
    }

    /**
     * Returns a random Item. If there are no items available, returns null.
     */
    public MusicItem getRandomItem() {
        if (mItems.size() <= 0) return null;
        return mItems.get(mRandom.nextInt(mItems.size()));
    }

}
