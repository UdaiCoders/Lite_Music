package app.UDC.music;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import app.UDC.music.adapter.MusicFileListAdapter;
import app.UDC.music.advertise.AdNetworkHelper;
import app.UDC.music.data.AppConfig;
import app.UDC.music.data.DatabaseHandler;
import app.UDC.music.data.SharedPref;
import app.UDC.music.model.MusicItem;
import app.UDC.music.utils.MusicRetriever;
import app.UDC.music.utils.MusicUtils;
import app.UDC.music.utils.PermissionUtil;
import app.UDC.music.utils.Tools;

public class ActivityMain extends AppCompatActivity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private View parent_view;
    private MusicFileListAdapter mAdapter;
    private LinearLayout lyt_progress;
    private View lyt_no_music;
    private LinearLayout lyt_list;
    private FloatingActionButton bt_scan;
    private Button bt_playlist, bt_favorites;

    // player
    private Button bt_play;
    private Button bt_next;
    private Button bt_prev;
    private Button btn_repeat;
    private Button btn_shuffle;
    private AppCompatSeekBar seek_song_progressbar;
    private TextView tv_song_title;
    private TextView tv_song_current_duration;
    private TextView tv_song_total_duration;

    // search view
    private View search_bar;
    private ImageView action_back;
    private ImageView action_clear;
    private EditText et_search;

    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    //private SongsManager songManager;
    private MusicUtils utils;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    // the items (songs) we have queried
    private ArrayList<MusicItem> mItems = new ArrayList<>();
    private MusicItem cur_music = null;
    private DatabaseHandler db;
    Dialog dialog_timer = null;

    private ActionBar actionBar;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private SharedPref sharedPref;

    static ActivityMain activityMain;

    public static ActivityMain getInstance() {
        return activityMain;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityMain = this;
        parent_view = findViewById(R.id.main_content);

        prepareAds();
        sharedPref = new SharedPref(this);
        db = new DatabaseHandler(getApplicationContext());

        initialPlaylistComponent();
        initialPlayerComponent();
        initSearchViewComponent();

        loadMusicFromStorage(false);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(null);
        Tools.setActionBarColor(this, actionBar);
    }

    private void initDrawerMenu() {
        navigationView = findViewById(R.id.nav_view);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                updateNavFlag(navigationView);
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(item -> {
            if (showInterstitialAd()) return false;
            int id = item.getItemId();
            if (id == R.id.nav_theme) {
                dialogColorChooser(ActivityMain.this);
            } else if (id == R.id.nav_timer) {
                dialogSleepTime();
            } else if (id == R.id.nav_rate) {
                Tools.rateAction(ActivityMain.this);
            } else if (id == R.id.nav_about) {
                dialogAbout();
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void initialPlayerComponent() {
        // All player buttons
        bt_play = findViewById(R.id.bt_play);
        bt_next = findViewById(R.id.bt_next);
        bt_prev = findViewById(R.id.bt_prev);
        btn_repeat = findViewById(R.id.btn_repeat);
        btn_shuffle = findViewById(R.id.btn_shuffle);
        seek_song_progressbar = findViewById(R.id.seek_song_progressbar);
        tv_song_title = findViewById(R.id.tv_song_title);
        tv_song_title.setSelected(true);
        tv_song_current_duration = findViewById(R.id.tv_song_current_duration);
        tv_song_total_duration = findViewById(R.id.tv_song_total_duration);

        // Media Player
        mp = new MediaPlayer();
        //songManager = new SongsManager();
        utils = new MusicUtils();
        // Listeners
        seek_song_progressbar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        // Getting all songs list songsList = songManager.getPlayList();
        buttonPlayerAction();
    }

    private void initialPlaylistComponent() {

        // playlist
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lyt_progress = findViewById(R.id.lyt_progress);
        lyt_no_music = findViewById(R.id.lyt_no_music);
        lyt_list = findViewById(R.id.lyt_list);
        bt_scan = findViewById(R.id.bt_scan);
        bt_playlist = findViewById(R.id.bt_playlist);
        bt_favorites = findViewById(R.id.bt_favorites);
        lyt_progress.setVisibility(View.GONE);
        lyt_no_music.setVisibility(View.GONE);

        bt_scan.setOnClickListener(v -> loadMusicFromStorage(true));
        mItems.clear();
        mItems = db.getAllMusicFiles();
        mAdapter = new MusicFileListAdapter(this, mItems);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((view, obj, pos) -> playSong(obj));
        noMusicChecker();

        toggleButton(bt_playlist, false);
        toggleButton(bt_favorites, true);
        bt_playlist.setOnClickListener(v -> {
            showInterstitialAd();
            toggleButton(bt_playlist, false);
            toggleButton(bt_favorites, true);
            mItems = db.getAllMusicFiles();
            mAdapter.setItems(mItems);
            bt_scan.show();
            noMusicChecker();
            hideSearchView();
        });
        bt_favorites.setOnClickListener(v -> {
            showInterstitialAd();
            toggleButton(bt_playlist, true);
            toggleButton(bt_favorites, false);
            mItems = db.getAllFavorites();
            mAdapter.setItems(mItems);
            bt_scan.hide();
            noMusicChecker();
            hideSearchView();
        });
    }

    private void initSearchViewComponent() {
        et_search = findViewById(R.id.et_search);
        search_bar = findViewById(R.id.search_bar);
        action_back = findViewById(R.id.action_back);
        action_clear = findViewById(R.id.action_clear);
        action_back.setOnClickListener(view -> {
            mAdapter.getFilter().filter("");
            hideSearchView();
        });

        action_clear.setOnClickListener(view -> {
            et_search.setText("");
            showKeyboard();
            mAdapter.getFilter().filter("");
        });

        et_search.setOnEditorActionListener((v, actionId, event) -> {
            String query = et_search.getText().toString().trim();
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                mAdapter.getFilter().filter(query);
                return true;
            }
            return false;
        });
    }

    private void hideSearchView() {
        search_bar.setVisibility(View.GONE);
        et_search.setText("");
        hideKeyboard();
    }

    public void toggleButton(Button bt, boolean light) {
        if (light) {
            bt.setTextColor(getResources().getColor(R.color.white_overlay));
        } else {
            bt.setTextColor(Color.WHITE);
        }
    }

    /**
     * Play button click event plays a song and changes button to pause image
     * pauses a song and changes button to play image
     */
    private void buttonPlayerAction() {
        bt_play.setOnClickListener(arg0 -> {
            if (mItems.size() > 0) {
                // check for already playing
                if (mp != null && mp.isPlaying()) {
                    mp.pause();
                    // Changing button image to play button
                    bt_play.setBackgroundResource(R.drawable.btn_play);
                } else if (mp != null) {
                    // Resume song
                    mp.start();
                    // Changing button image to pause button
                    bt_play.setBackgroundResource(R.drawable.btn_pause);
                }
            } else {
                Snackbar.make(parent_view, "Music not found", Snackbar.LENGTH_SHORT).show();
            }

        });


        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        bt_next.setOnClickListener(arg0 -> {
            // check if next song is there or not
            if (mItems == null || mItems.size() == 0) return;
            if (currentSongIndex < (mItems.size() - 1)) {
                playSong(mItems.get(currentSongIndex + 1));
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                playSong(mItems.get(0));
                currentSongIndex = 0;
            }

        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        bt_prev.setOnClickListener(arg0 -> {
            if (mItems == null || mItems.size() == 0) return;
            if (currentSongIndex > 0) {
                playSong(mItems.get(currentSongIndex - 1));
                currentSongIndex = currentSongIndex - 1;
            } else {
                // play last song
                playSong(mItems.get(mItems.size() - 1));
                currentSongIndex = mItems.size() - 1;
            }

        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btn_repeat.setOnClickListener(arg0 -> {
            if (isRepeat) {
                isRepeat = false;
                Snackbar.make(parent_view, "Repeat is OFF", Snackbar.LENGTH_SHORT).show();
                btn_repeat.setBackgroundResource(R.drawable.btn_repeat);
            } else {
                // make repeat to true
                isRepeat = true;
                Snackbar.make(parent_view, "Repeat is ON", Snackbar.LENGTH_SHORT).show();
                // make shuffle to false
                isShuffle = false;
                btn_repeat.setBackgroundResource(R.drawable.btn_repeat_focused);
                btn_shuffle.setBackgroundResource(R.drawable.btn_shuffle);
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btn_shuffle.setOnClickListener(arg0 -> {
            if (isShuffle) {
                isShuffle = false;
                Snackbar.make(parent_view, "Shuffle is OFF", Snackbar.LENGTH_SHORT).show();
                btn_shuffle.setBackgroundResource(R.drawable.btn_shuffle);
            } else {
                // make repeat to true
                isShuffle = true;
                Snackbar.make(parent_view, "Shuffle is ON", Snackbar.LENGTH_SHORT).show();
                // make shuffle to false
                isRepeat = false;
                btn_shuffle.setBackgroundResource(R.drawable.btn_shuffle_focused);
                btn_repeat.setBackgroundResource(R.drawable.btn_repeat);
            }
        });

    }

    /**
     * Function to play a song
     */
    public void playSong(MusicItem msc) {
        showInterstitialAd();
        // Play song
        try {
            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(getApplicationContext(), msc.getURI());
            mp.prepare();
            mp.start();
            cur_music = msc;
            if (db.isFavoritesExist(msc.getId() + "")) {
                menu_favorites.setIcon(R.drawable.ic_favorites_solid);
            } else {
                menu_favorites.setIcon(R.drawable.ic_favorites_outline);
            }
            // Displaying Song title
            String songTitle = msc.getTitle();
            tv_song_title.setText(songTitle);
            // Changing Button Image to pause image
            bt_play.setBackgroundResource(R.drawable.btn_pause);

            // set Progress bar values
            seek_song_progressbar.setProgress(0);
            seek_song_progressbar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            tv_song_total_duration.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            tv_song_current_duration.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            seek_song_progressbar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };


    public class loadFiles extends AsyncTask<String, String, String> {
        MusicRetriever mRetriever;
        String status = "success";

        public loadFiles() {
            mRetriever = new MusicRetriever();
        }

        @Override
        protected void onPreExecute() {
            mItems.clear();
            bt_scan.hide();
            lyt_no_music.setVisibility(View.GONE);
            lyt_progress.setVisibility(View.VISIBLE);
            lyt_list.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(1000);
                mRetriever.prepare();
                mItems = mRetriever.getAllItem();
                status = "success";
            } catch (Exception e) {
                status = "failed";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            bt_scan.show();
            if (!status.equals("failed")) {
                lyt_progress.setVisibility(View.GONE);
                lyt_list.setVisibility(View.VISIBLE);
                db.truncateTableScan();
                db.addMusicFiles(mItems);
                mItems = db.getAllMusicFiles();
                mAdapter.setItems(mItems);
                noMusicChecker();
            }
            super.onPostExecute(result);
        }
    }

    public void noMusicChecker() {
        if (mAdapter.getItemCount() <= 0) {
            lyt_no_music.setVisibility(View.VISIBLE);
        } else {
            lyt_no_music.setVisibility(View.GONE);
        }
    }

    // 2.0 and above
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            mHandler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = mp.getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            mp.seekTo(currentPosition);

            // update timer progress again
            updateProgressBar();
        } catch (Exception e) {
        }
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSong(mItems.get(currentSongIndex));
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((mItems.size() - 1) - 0 + 1) + 0;
            playSong(mItems.get(currentSongIndex));
        } else {
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < (mItems.size() - 1)) {
                playSong(mItems.get(currentSongIndex + 1));
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                if (mItems.size() <= 0) return;
                playSong(mItems.get(0));
                currentSongIndex = 0;
            }
        }
    }

    // stop player when destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.release();
    }

    @Override
    protected void onResume() {
        if (mp.isPlaying()) {
            updateProgressBar();
            bt_play.setBackgroundResource(R.drawable.btn_pause);
        } else {
            bt_play.setBackgroundResource(R.drawable.btn_play);
        }

        LinearLayout lyt_main = findViewById(R.id.lyt_main);
        lyt_main.setBackgroundColor(sharedPref.getThemeColorInt());
        bt_scan.setBackgroundTintList(ColorStateList.valueOf(sharedPref.getThemeColorInt()));

        initToolbar();
        initDrawerMenu();
        // for system bar in lollipop
        Tools.systemBarLolipop(this);
        updateNavFlag(navigationView);
        super.onResume();
    }

    private void loadMusicFromStorage(boolean click) {
        // permission checker for android M or higher
        if (Tools.needRequestPermission()) {
            String[] permission = PermissionUtil.getDeniedPermission(this);
            if (permission.length != 0) {
                requestPermissions(permission, 200);
                if (sharedPref.getNeverAskAgain(PermissionUtil.STORAGE)) {
                    PermissionUtil.showDialogPermission(this);
                }
            } else {
                if (db.getAllMusicFiles().size() == 0 || click) {
                    new loadFiles().execute("");
                }
            }
        } else {
            if (db.getAllMusicFiles().size() == 0 || click) {
                new loadFiles().execute("");
            }
        }
    }


    /* Dialog about */
    protected void dialogAbout() {
        final Dialog dialog = new Dialog(ActivityMain.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_about);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        final Button button_ok = dialog.findViewById(R.id.button_ok);

        button_ok.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    /* Dialog for Sleep Timer */
    protected void dialogSleepTime() {
        dialog_timer = new Dialog(ActivityMain.this);
        dialog_timer.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog_timer.setContentView(R.layout.dialog_timer);
        dialog_timer.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_timer.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        final Button button_stop = dialog_timer.findViewById(R.id.button_stop);
        final Button button_hide = dialog_timer.findViewById(R.id.button_hide);
        final TextView status = dialog_timer.findViewById(R.id.status);
        final AppCompatSeekBar timer_seek_bar = dialog_timer.findViewById(R.id.timer_seek_bar);
        timer_seek_bar.setProgress(0);
        timer_seek_bar.setMax(60);
        if (!is_timer_running) button_stop.setVisibility(View.GONE);

        button_stop.setOnClickListener(v -> {
            is_timer_running = false;
            count_down_timer.cancel();
            timer_seek_bar.setProgress(0);
            status.setText(timer_seek_bar.getProgress() + " minutes");
            button_stop.setVisibility(View.GONE);
        });

        button_hide.setOnClickListener(view -> {
            updateNavFlag(navigationView);
            dialog_timer.dismiss();
        });

        timer_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                status.setText(seekBar.getProgress() + " minutes");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() == 0) {
                    if (count_down_timer != null) count_down_timer.cancel();
                    is_timer_running = false;
                    button_stop.setVisibility(View.GONE);
                } else {
                    startSleepTimer(seekBar.getProgress());
                    button_stop.setVisibility(View.VISIBLE);
                }
            }
        });
        dialog_timer.show();
        dialog_timer.getWindow().setAttributes(lp);
    }

    private void dialogColorChooser(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_color_theme);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        ListView list = dialog.findViewById(R.id.list_view);
        final String stringArray[] = getResources().getStringArray(R.array.arr_main_color_name);
        final String colorCode[] = getResources().getStringArray(R.array.arr_main_color_code);
        list.setAdapter(new ArrayAdapter<String>(ActivityMain.this, android.R.layout.simple_list_item_1, stringArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                textView.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                textView.setBackgroundColor(Color.parseColor(colorCode[position]));
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });

        list.setOnItemClickListener((av, v, pos, id) -> {
            //mHandler.removeCallbacks(mUpdateTimeTask);
            sharedPref.setThemeColor(colorCode[pos]);
            dialog.dismiss();
            onResume();
            //Tools.restartApplication(ActivityMain.this);
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200) {
            for (String perm : permissions) {
                boolean rationale = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    rationale = shouldShowRequestPermissionRationale(perm);
                }
                sharedPref.setNeverAskAgain(perm, !rationale);
            }
            if (PermissionUtil.isAllPermissionGranted(this)) {
                loadMusicFromStorage(true);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private MenuItem menu_favorites;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        menu_favorites = menu.getItem(1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_fav) {
            if (cur_music != null) {
                if (!db.isFavoritesExist(cur_music.getId() + "")) {
                    db.addFavorites(cur_music);
                    menu_favorites.setIcon(R.drawable.ic_favorites_solid);
                    Snackbar.make(parent_view, "Add to favorites", Snackbar.LENGTH_SHORT).show();
                } else {
                    db.deleteFavorites(cur_music);
                    menu_favorites.setIcon(R.drawable.ic_favorites_outline);
                    Snackbar.make(parent_view, "Remove from favorites", Snackbar.LENGTH_SHORT).show();
                }

            } else {
                Snackbar.make(parent_view, "No playing music", Snackbar.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_search) {
            search_bar.setVisibility(View.VISIBLE);
            et_search.requestFocus();
            showKeyboard();
        }
        return super.onOptionsItemSelected(item);
    }

    // class for starting sleep Timer
    private CountDownTimer count_down_timer = null;
    boolean is_timer_running = false;

    private void startSleepTimer(int minute) {
        if (count_down_timer != null) count_down_timer.cancel();
        long min = 60 * 1000 * minute;
        count_down_timer = new CountDownTimer(min, 100) {
            public void onTick(long millisTick) {
                is_timer_running = true;
                if (dialog_timer != null && dialog_timer.isShowing()) {
                    int cur_secs = (int) (millisTick / 1000);
                    int cur_min = cur_secs / 60;
                    cur_secs = cur_secs % 60;
                    String status = String.format(Locale.US, "%02d", cur_min) + ":" + String.format(Locale.US, "%02d", cur_secs) + " remaining";
                    ((TextView) dialog_timer.findViewById(R.id.status)).setText(status);
                    ((AppCompatSeekBar) dialog_timer.findViewById(R.id.timer_seek_bar)).setProgress(cur_min);
                }
            }

            public void onFinish() {
                is_timer_running = false;
                if (mp != null && mp.isPlaying()) {
                    mp.pause();
                    bt_play.setBackgroundResource(R.drawable.btn_play);
                    Toast.makeText(getApplicationContext(), "Music stopped by timer", Toast.LENGTH_SHORT).show();
                }
            }

        };
        count_down_timer.start();
    }

    private void updateNavFlag(NavigationView nav) {
        Menu menu = nav.getMenu();
        View dot_sign = (View) menu.findItem(R.id.nav_timer).getActionView().findViewById(R.id.dot);
        dot_sign.setVisibility(View.GONE);
        if (is_timer_running) dot_sign.setVisibility(View.VISIBLE);
    }


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private AdNetworkHelper adNetworkHelper;

    private void prepareAds() {
        adNetworkHelper = new AdNetworkHelper(this);
        adNetworkHelper.showGDPR();
        adNetworkHelper.loadBannerAd(AppConfig.ADS_MAIN_BANNER);
        adNetworkHelper.loadInterstitialAd(AppConfig.ADS_MAIN_INTERSTITIAL);
    }

    public boolean showInterstitialAd() {
        return adNetworkHelper.showInterstitialAd(AppConfig.ADS_MAIN_INTERSTITIAL);
    }
}
