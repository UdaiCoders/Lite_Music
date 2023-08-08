package app.UDC.music.model;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

public class MusicItem {
    long id;
    String artist;
    String title;
    String album;
    long duration;
    boolean fav = false;

    public MusicItem() {
        super();
    }

    public MusicItem(long id, String artist, String title, String album, long duration) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public long getDuration() {
        return duration;
    }

    public Uri getURI() {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
