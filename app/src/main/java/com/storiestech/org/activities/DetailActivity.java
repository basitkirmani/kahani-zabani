package com.storiestech.org.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stories.storyappn.R;
import com.storiestech.org.adapters.AdManager;
import com.storiestech.org.adapters.DBAdapter;
import com.storiestech.org.adapters.DBManager;
import com.storiestech.org.datamodels.Story;
import com.storiestech.org.datamodels.downloadGetSet;
import com.storiestech.org.utilities.MusicController;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.storiestech.org.activities.SettingActivity.MIN_SIZE;

public class DetailActivity extends AppCompatActivity implements MediaPlayerControl {

    @NotNull
    private Story story;
    @NotNull
    private TextToSpeech tts;
    private boolean isSpeaking = false;
    private SharedPreferences prefs;
    private Toolbar detail_toolbar;
    private TextView story_title;
    private TextView story_details;
    private FloatingActionButton btn_speak;
    private ScrollView sv_background;
    private MediaPlayer mpintro;
    private ImageView btn_speak_;
    private LinearLayout linTTS;
    private String audioFile;
    private static final int TYPE_EXTERNAL_SERVER = 0;
    private static final int TYPE_INTERNAL_SERVER = 1;
    private static final int TYPE_NO_AUDIO = 2;
    private int AUDIO_TYPE = -1;

    private ArrayList<downloadGetSet> downloadList;
    String audioFileName;
    String audioServicePath;
    private MusicController controller;
    private AdView adView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setting Theme
        prefs = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
        int themeId = prefs.getInt(getString(R.string.theme_pref), R.style.AppTheme);
        setTheme(themeId);

        setContentView(R.layout.activity_detail);

        //getting Intents
        getIntents();

        //initializing Views
        iniViews();
        AdManager.setUpBanner(this);
        AdManager.setUpInterstitialAd(this);

    }

    private void getIntents() {
        story = new Story();
        Intent intent = getIntent();
        story.setId(intent.getIntExtra("storyId", -1));
        story.setName(intent.getStringExtra("storyName"));
        story.setDetails(intent.getStringExtra("storyDetail"));
        story.setAudioFile(intent.getStringExtra("audio"));
        story.setFav(intent.getBooleanExtra("isFav", false));
        story.setBookmarked(intent.getBooleanExtra("isBookMark", false));

        //getting filename and file path
        AUDIO_TYPE = getTypeOfAudio(story.getAudioFile());
        if (AUDIO_TYPE == TYPE_EXTERNAL_SERVER) {
            audioServicePath = story.getAudioFile();
            audioFileName = getFileNameFromURL(audioServicePath);
        } else {
            audioServicePath = getString(R.string.audioPath) + story.getAudioFile();
            audioFileName = story.getAudioFile();
        }

        btn_speak = findViewById(R.id.btn_speak2);

        if (getString(R.string.isAudio).equals("false")) {
            linTTS.setVisibility(View.GONE);
        }
    }



    private boolean hasCurrentAudio(String filename) {
        // Create a path where we will place our video in the user's
        // public pictures directory and check if the file exists.  If
        String path = getFilesDir() + "/audioFiles/" + filename;
        File file = new File(path);
        Log.e("check", filename + " " + file.exists());
        return file.exists();
    }

    private int getTypeOfAudio(String audioFile) {

        if (Patterns.WEB_URL.matcher(audioFile).matches()) {
            return TYPE_EXTERNAL_SERVER;
        } else if (audioFile.isEmpty()) {
            return TYPE_NO_AUDIO;
        } else
            return TYPE_INTERNAL_SERVER;
    }

    private void iniViews() {

        detail_toolbar = findViewById(R.id.detail_toolbar);
        story_title = findViewById(R.id.story_title);
        story_details = findViewById(R.id.story_details);
        adView = findViewById(R.id.adView);
        btn_speak_ = findViewById(R.id.btn_speak);
        sv_background = findViewById(R.id.sv_background);
        linTTS = findViewById(R.id.linTTS);
        controller = new MusicController(this);

        if (!story.getAudioFile().isEmpty()) {
            playSound(audioFileName);
        }

        btn_speak_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (story.getAudioFile() != null) {
                    if (story.getAudioFile().isEmpty()) {
                        speak();
                    }
                }
            }
        });

        //setting of night mode
        toggleUIMode(prefs.getBoolean(getString(R.string.isNight), false));

        // getting Default Size
        int tSize = prefs.getInt(getString(R.string.font_pref), MIN_SIZE + 10);
        story_details.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
        story_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize + 4);

        String storyName = story.getName();
        story_title.setText(storyName);
        story_details.setText(story.getDetails());

        detail_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        detail_toolbar.inflateMenu(R.menu.detail_action_menu);

        //setting initial imageResource
        if (detail_toolbar.getMenu() != null) {
            if (story.isBookmarked()) {
                detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_filled);
            } else {
                detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_outline);
            }

            if (story.isFav()) {
                detail_toolbar.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_black_24dp);
            } else {
                detail_toolbar.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_border_black_24dp);
            }

            if (prefs.getBoolean(getString(R.string.isNight), false)) {
                detail_toolbar.getMenu().findItem(R.id.nav_night_mode).setIcon(R.drawable.ic_brightness_7_black_24dp);
            } else {
                detail_toolbar.getMenu().findItem(R.id.nav_night_mode).setIcon(R.drawable.ic_brightness_3_black_24dp);
            }
        }

        detail_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String type;
                switch (item.getItemId()) {
                    case R.id.nav_bookmark:

                        if (story.isBookmarked()) {
                            DBAdapter adapter = new DBAdapter(DetailActivity.this);
                            DBManager.remove(DBManager.CompanionQuery.getTBL_READ_LATER(), story.getId(), adapter);
                            type = "Removed from";
                        } else {
                            DBAdapter adapter = new DBAdapter(DetailActivity.this);
                            DBManager.addToStory(story, adapter, DetailActivity.this);
                            DBManager.addToBookmark(story.getId(), adapter);
                            type = "Added to";
                        }
                        Toast.makeText(DetailActivity.this, type + " Bookmark", Toast.LENGTH_SHORT).show();
//                        Snackbar.make(detail_toolbar, type + " Read Later", Snackbar.LENGTH_SHORT).show();
                        story.setBookmarked(!story.isBookmarked());

                        if (story.isBookmarked()) {
                            detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_filled);
                        } else {
                            detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_outline);
                        }

                        break;
                    case R.id.nav_fav:
                        if (story.isFav()) {
                            DBAdapter adapter = new DBAdapter(DetailActivity.this);
                            DBManager.remove(DBManager.CompanionQuery.getTBL_FAV(), story.getId(), adapter);
                            type = "Removed from";
                        } else {
                            DBAdapter adapter = new DBAdapter(DetailActivity.this);
                            DBManager.addToStory(story, adapter, DetailActivity.this);
                            DBManager.addTofav(story.getId(), adapter);
                            type = "Added to";
                        }
                        Toast.makeText(DetailActivity.this, type + " Bookmark", Toast.LENGTH_SHORT).show();
//                        Snackbar.make(detail_toolbar, type + " Favourites", Snackbar.LENGTH_SHORT).show();
                        story.setFav(!story.isFav());
                        if (story.isFav()) {
                            detail_toolbar.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_black_24dp);
                        } else {
                            detail_toolbar.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_border_black_24dp);
                        }
                        break;
                    case R.id.nav_send:
                        startActivity(new Intent(Intent.ACTION_SEND)
                                .putExtra(Intent.EXTRA_TEXT, story.getName() + "\n\n" + story.getDetails())
                                .setType("text/plain"));
                        break;
                    case R.id.nav_night_mode:
                        if (prefs.getBoolean(getString(R.string.isNight), false)) {
                            item.setIcon(R.drawable.ic_brightness_3_black_24dp);
                            toggleUIMode(false);
                            prefs.edit().putBoolean(getString(R.string.isNight), false).apply();

                        } else {
                            item.setIcon(R.drawable.ic_brightness_7_black_24dp);
                            toggleUIMode(true);
                            prefs.edit().putBoolean(getString(R.string.isNight), true).apply();
                        }
                        break;

                    case R.id.nav_text_size:
                        BottomSheetDialog textSize = new BottomSheetDialog(DetailActivity.this);
                        SeekBar textBar = new SeekBar(DetailActivity.this);
                        int p = (int) TypedValue.applyDimension(1, 20.0F, DetailActivity.this.getResources().getDisplayMetrics());
                        textBar.setPadding(p, p, p, p);
                        textBar.setMax(SettingActivity.MAX_SIZE - MIN_SIZE);
                        textSize.setContentView(textBar);
                        textSize.show();
                        textBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                story_details.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) progress + (float) MIN_SIZE);
                                story_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, progress + (float) MIN_SIZE + 4);
                                prefs.edit().putInt(getString(R.string.font_pref), progress + MIN_SIZE).apply();
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        textBar.setProgress(prefs.getInt(getString(R.string.font_pref), MIN_SIZE + 10) - MIN_SIZE);

                        break;
                }
                return true;
            }
        });

        //initiate text to speech
        initTts();

        //setting if user enter  detail page
        DBAdapter adapter = new DBAdapter(this);
//        if (DBManager.isBookmarked(story.getId(), adapter)) {
//            DBManager.remove(DBManager.CompanionQuery.getTBL_READ_LATER(), story.getId(), adapter);
//        }
        DBManager.addToStory(story, adapter, DetailActivity.this);
        DBManager.addToRecent(story.getId(), adapter);
        adapter.close();

    }

    private void playSound(String audioFile) {

        controller.setMediaPlayer(this);
        controller.setEnabled(true);
        controller.setAnchorView(findViewById(R.id.rel_detail));
        controller.setVisibility(View.VISIBLE);

        if (MainActivity.checkInternet(this))
            playOnline();
        else {
            speak();
        }
    }

    private void playOnline() {
//        btn_speak_.setVisibility(View.GONE);
        linTTS.setVisibility(View.GONE);

        mpintro = new MediaPlayer();
        mpintro.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mpintro.setDataSource(audioServicePath);
            mpintro.prepareAsync();
            Toast.makeText(this, "Audio is Loading", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "playOnline: " + e.getMessage());
        }

        mpintro.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                controller.show();
                adView.setVisibility(View.GONE);
            }
        });

        mpintro.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                btn_speak.setImageResource(R.drawable.ic_volume_up_black_24dp);
                btn_speak_.setImageResource(R.drawable.ic_pause_button);
                mpintro.reset();
                mpintro.release();
                mpintro = null;
                controller.setVisibility(View.GONE);
//                btn_speak.setVisibility(View.VISIBLE);
                btn_speak_.setVisibility(View.VISIBLE);

            }
        });
    }

    private void initTts() {

        tts = new TextToSpeech(DetailActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale(getString(R.string.tts_locale)));
                    tts.setSpeechRate(0.8f);
                } else {
                    Toast.makeText(DetailActivity.this, "Not Supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void speak() {
        if (isSpeaking) {
            tts.stop();
//            btn_speak.setImageResource(R.drawable.ic_volume_up_black_24dp);
            btn_speak_.setImageResource(R.drawable.ic_play);
        } else {

            if (story.getName().length() + story.getDetails().length() < TextToSpeech.getMaxSpeechInputLength()) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    tts.speak(story.getName() + story.getDetails(), TextToSpeech.QUEUE_ADD, null, null);
                else
                    tts.speak(story.getName() + story.getDetails(), TextToSpeech.QUEUE_ADD, null);
//                btn_speak.setImageResource(R.drawable.ic_volume_off_black_24dp);
                btn_speak_.setImageResource(R.drawable.ic_pause_button);

                Toast.makeText(this, "Please wait....Audio is starting", Toast.LENGTH_LONG).show();
            } else {
                String tempText = story.getName() + story.getDetails();
                String[] tempArray = splitT(tempText, TextToSpeech.getMaxSpeechInputLength() - 1000);

                HashMap<String, String> map = new HashMap<>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");

                for (String aTempArray : tempArray) {
                    tts.speak(aTempArray, TextToSpeech.QUEUE_ADD, map);
                    tts.playSilence(250, TextToSpeech.QUEUE_ADD, null);
                }
            }
//            btn_speak.setImageResource(R.drawable.ic_volume_off_black_24dp);
            btn_speak_.setImageResource(R.drawable.ic_pause_button);
            Toast.makeText(this, "Please wait....Audio is starting", Toast.LENGTH_LONG).show();

        }
        isSpeaking = !isSpeaking;
    }

    public static String[] splitT(String src, int len) {
        String[] result = new String[(int) Math.ceil((double) src.length() / (double) len)];
        for (int i = 0; i < result.length; i++)
            result[i] = src.substring(i * len, Math.min(src.length(), (i + 1) * len));
        return result;
    }

    @Override
    protected void onStop() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mpintro != null) {
            mpintro.release();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            if (tts.isSpeaking()) {
                tts.stop();
                tts.shutdown();
            }
        }
        if (mpintro != null) {
            mpintro.release();
        }
        if (controller != null) {
            controller.removee();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (tts != null) {
            if (tts.isSpeaking()) {
                tts.stop();
                tts.shutdown();
            }
        }
        if (mpintro != null) {
            mpintro.release();
        }
        if (controller != null) {
            controller.removee();
        }
        AdManager.onBackPress(DetailActivity.this);
        super.onBackPressed();
    }

    private void toggleUIMode(Boolean isNight) {
        if (isNight) {
            sv_background.setBackgroundColor(Color.BLACK);
            story_title.setTextColor(Color.WHITE);
            story_details.setTextColor(Color.WHITE);
        } else {
            sv_background.setBackgroundColor(Color.WHITE);
            story_title.setTextColor(Color.BLACK);
            story_details.setTextColor(Color.BLACK);
        }

    }

    //get file name from url
    private static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (host.length() > 0 && url.endsWith(host)) {
                return "";
            }
        } catch (MalformedURLException e) {
            return "";
        }
        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();

        // find end index for ?
        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        // find end index for #
        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }
        // calculate the end index
        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }

    //Methods for mediaPlayer when played online
    @Override
    public void start() {

        if (mpintro == null) return;
        mpintro.start();
    }

    @Override
    public void pause() {

        if (mpintro == null) return;
        mpintro.pause();
    }

    @Override
    public int getDuration() {

        if (mpintro == null) return 0;
        return mpintro.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        if (mpintro == null) return 0;
        return mpintro.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        if (mpintro == null) return;
        mpintro.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if (mpintro == null) return false;
        return mpintro.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        try {
            return mpintro.getDuration() != 0 ? (mpintro.getCurrentPosition() * 100) / mpintro.getDuration() : 0;

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}

