package app.UDC.music.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import app.UDC.music.model.MusicItem;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "music_db";

    // Contacts table name
    private static final String TABLE_FAVORITES = "music_table_favorites";


    // Contacts Table Columns
    private static final String KEY_ID = "id";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_DURATION = "duration";

    // Contacts table name
    private static final String TABLE_MUSIC_SCAN = "music_table_scan";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableBookmark(db);
        createTableScan(db);
    }

    private void createTableBookmark(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + KEY_ID + " TEXT,"
                + KEY_ARTIST + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_ALBUM + " TEXT,"
                + KEY_DURATION + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableScan(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_MUSIC_SCAN + "("
                + KEY_ID + " TEXT,"
                + KEY_ARTIST + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_ALBUM + " TEXT,"
                + KEY_DURATION + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC_SCAN);
        // Create tables again
        onCreate(db);
    }

    public void truncateTableScan() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC_SCAN);
        createTableScan(db);
    }

    // Adding new pdf file
    public void addMusicFiles(ArrayList<MusicItem> obj) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < obj.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_ID, obj.get(i).getId() + "");
            values.put(KEY_ARTIST, obj.get(i).getArtist());
            values.put(KEY_TITLE, obj.get(i).getTitle());
            values.put(KEY_ALBUM, obj.get(i).getAlbum());
            values.put(KEY_DURATION, obj.get(i).getDuration() + "");
            db.insert(TABLE_MUSIC_SCAN, null, values);
        }
        db.close(); // Closing database connection
    }

    // Getting All pdf file
    public ArrayList<MusicItem> getAllMusicFiles() {
        ArrayList<MusicItem> files = new ArrayList<MusicItem>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_MUSIC_SCAN + " ORDER BY " + KEY_TITLE + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MusicItem p = new MusicItem();
                p.setId(Long.valueOf(cursor.getString(0)));
                p.setArtist(cursor.getString(1));
                p.setTitle(cursor.getString(2));
                p.setAlbum(cursor.getString(3));
                p.setDuration(Long.valueOf(cursor.getString(4)));
                files.add(p);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return files;
    }


    // Adding new bookmark
    public void addFavorites(MusicItem obj) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, obj.getId() + "");
        values.put(KEY_ARTIST, obj.getArtist());
        values.put(KEY_TITLE, obj.getTitle());
        values.put(KEY_ALBUM, obj.getAlbum());
        values.put(KEY_DURATION, obj.getDuration() + "");
        if (isFavoritesExist(obj.getId() + "")) {
            // updating row
            db.update(TABLE_FAVORITES, values, KEY_ID + " = ?", new String[]{obj.getId() + ""});
        } else {
            // Inserting Row
            db.insert(TABLE_FAVORITES, null, values);
        }
        db.close(); // Closing database connection

    }


    // Getting All Contacts
    public ArrayList<MusicItem> getAllFavorites() {
        ArrayList<MusicItem> files = new ArrayList<MusicItem>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_FAVORITES + " ORDER BY " + KEY_TITLE + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MusicItem p = new MusicItem();
                p.setId(Long.valueOf(cursor.getString(0)));
                p.setArtist(cursor.getString(1));
                p.setTitle(cursor.getString(2));
                p.setAlbum(cursor.getString(3));
                p.setDuration(Long.valueOf(cursor.getString(4)));
                p.setFav(true);
                files.add(p);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return files;
    }

    // Deleting single contact
    public void deleteFavorites(MusicItem book) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, KEY_ID + " = ?", new String[]{book.getId() + ""});
        db.close();
    }


    // Getting contacts Count
    public int getFavoritesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FAVORITES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    // Getting contacts Count
    public boolean isFavoritesExist(String id) {
        String q = "SELECT  * FROM " + TABLE_FAVORITES + " WHERE " + KEY_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(q, new String[]{id});
        int count = cursor.getCount();
        cursor.close();
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

}
