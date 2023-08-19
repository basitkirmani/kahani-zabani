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
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.stories.storyappn.R;
import com.storiestech.org.adapters.AdManager;
import com.storiestech.org.adapters.DBAdapter;
import com.storiestech.org.adapters.DBManager;
import com.storiestech.org.datamodels.Story;
import com.storiestech.org.utilities.MusicController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.storiestech.org.activities.SettingActivity.MIN_SIZE;

public class RecentActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {
    private static final String TAG = "RecentActivity";
    //    private RecyclerView data_list;
    private TextView story_title;
    private TextView story_details, txtTit, txtNoData;
    private ScrollView sv_background;
    private SharedPreferences prefs;
    private Toolbar detail_toolbar;
    private LinearLayout linTTS;
    private ImageView btn_speak_;
    private ArrayList<Story> list;
    private boolean isSpeaking = false;
    private MediaPlayer mpintro;
    String audioFileName;
    private TextToSpeech tts;
    private MusicController controller;
    private static final int TYPE_EXTERNAL_SERVER = 0;
    private static final int TYPE_INTERNAL_SERVER = 1;
    private static final int TYPE_NO_AUDIO = 2;
    private int AUDIO_TYPE = -1;
    String audioServicePath;
    private LinearLayout ll_background;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
        int themeId = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE).getInt(getString(R.string.theme_pref), R.style.AppTheme);
        setTheme(themeId);
        setContentView(R.layout.activity_detail);

        //initializing data
        initViews();

        AdManager.setUpBanner(this);
        AdManager.setUpInterstitialAd(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //populate Data
        populateData();
    }

    private void initViews() {
        View fragment_list = findViewById(R.id.fragmentData);
        TextView list_title = findViewById(R.id.main_title);
//        data_list = fragment_list.findViewById(R.id.data_list);
//        tv_noData = fragment_list.findViewById(R.id.favs);

        story_title = findViewById(R.id.story_title);
        story_details = findViewById(R.id.story_details);
        sv_background = findViewById(R.id.sv_background);
        detail_toolbar = findViewById(R.id.detail_toolbar);
        btn_speak_ = findViewById(R.id.btn_speak);
        linTTS = findViewById(R.id.linTTS);
        txtTit = findViewById(R.id.txtTit);
        txtNoData = findViewById(R.id.txtNoData);
        ll_background = findViewById(R.id.ll_background);
        controller = new MusicController(this);

        initTts();

        if (getString(R.string.isAudio).equals("false")) {
            linTTS.setVisibility(View.GONE);
        }

        txtTit.setText("Recent");

        detail_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        detail_toolbar.inflateMenu(R.menu.detail_action_menu);

        //setting text and image
//        list_title.setText(R.string.txt_recent);


        //setting of night mode
        toggleUIMode(prefs.getBoolean(getString(R.string.isNight), false));

        // getting Default Size
        int tSize = prefs.getInt(getString(R.string.font_pref), MIN_SIZE + 10);
        story_details.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
        story_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize + 4);

        btn_speak_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.get(0).getAudioFile() != null) {
                    if (list.get(0).getAudioFile().isEmpty()) {
                        speak();
                    }
                }
            }
        });

    }

    //populating Data
    private void populateData() {

        DBAdapter adapter = new DBAdapter(this);
        list = DBManager.getRecent(adapter);
        adapter.close();

        if (list.size() > 0) {

            txtNoData.setVisibility(View.GONE);
            ll_background.setVisibility(View.VISIBLE);
            linTTS.setVisibility(View.VISIBLE);

            String storyName = list.get(0).getName();
            story_title.setText(storyName);
            story_details.setText(list.get(0).getDetails());

            AUDIO_TYPE = getTypeOfAudio(list.get(0).getAudioFile());
            if (AUDIO_TYPE == TYPE_EXTERNAL_SERVER) {
                audioServicePath = list.get(0).getAudioFile();
                audioFileName = getFileNameFromURL(audioServicePath);
            } else {
                audioServicePath = getString(R.string.audioPath) + list.get(0).getAudioFile();
                audioFileName = list.get(0).getAudioFile();
            }

            if (getString(R.string.isAudio).equals("false")) {
                linTTS.setVisibility(View.GONE);
            } else {
                linTTS.setVisibility(View.VISIBLE);
            }

            if (list.get(0).getAudioFile() != null) {
                if (!list.get(0).getAudioFile().isEmpty()) {
                    playSound(audioFileName);
                }
            }
        } else {
            linTTS.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
            ll_background.setVisibility(View.GONE);

        }

        if (detail_toolbar.getMenu() != null) {
            if (list.size() != 0) {
                txtNoData.setVisibility(View.GONE);
                ll_background.setVisibility(View.VISIBLE);
                linTTS.setVisibility(View.VISIBLE);
                if (list.get(0).isBookmarked()) {
                    detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_filled);
                } else {
                    detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_outline);
                }

                if (list.get(0).isFav()) {
                    detail_toolbar.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_black_24dp);
                } else {
                    detail_toolbar.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_border_black_24dp);
                }

                if (prefs.getBoolean(getString(R.string.isNight), false)) {
                    detail_toolbar.getMenu().findItem(R.id.nav_night_mode).setIcon(R.drawable.ic_brightness_7_black_24dp);
                } else {
                    detail_toolbar.getMenu().findItem(R.id.nav_night_mode).setIcon(R.drawable.ic_brightness_3_black_24dp);
                }
            } else {
                txtNoData.setVisibility(View.VISIBLE);
                ll_background.setVisibility(View.GONE);
                linTTS.setVisibility(View.GONE);

            }
        }

        detail_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String type;
                switch (item.getItemId()) {
                    case R.id.nav_bookmark:
                        try {
                            if (list.get(0).isBookmarked()) {
                                DBAdapter adapter = new DBAdapter(RecentActivity.this);
                                DBManager.remove(DBManager.CompanionQuery.getTBL_READ_LATER(), list.get(0).getId(), adapter);
                                type = "Removed from";
                            } else {
                                DBAdapter adapter = new DBAdapter(RecentActivity.this);
                                DBManager.addToStory(list.get(0), adapter, RecentActivity.this);
                                DBManager.addToBookmark(list.get(0).getId(), adapter);
                                type = "Added to";
                            }
                            Toast.makeText(RecentActivity.this, type + " Bookmark", Toast.LENGTH_SHORT).show();
                            list.get(0).setBookmarked(!list.get(0).isBookmarked());

                            if (list.get(0).isBookmarked()) {
                                detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_filled);
                            } else {
                                detail_toolbar.getMenu().findItem(R.id.nav_bookmark).setIcon(R.drawable.ic_bookmark_outline);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onMenuItemClick: " + e.getMessage());
                        }
                        break;

                    case R.id.nav_send:
                        try {
                            startActivity(new Intent(Intent.ACTION_SEND)
                                    .putExtra(Intent.EXTRA_TEXT, list.get(0).getName() + "\n\n" + list.get(0).getDetails())
                                    .setType("text/plain"));
                        } catch (Exception e) {
                            Log.e(TAG, "onMenuItemClick: " + e.getMessage());
                        }
                        break;

                }
                return true;
            }
        });

    }

    public void speak() {
        if (isSpeaking) {
            tts.stop();
//            btn_speak.setImageResource(R.drawable.ic_volume_up_black_24dp);
            btn_speak_.setImageResource(R.drawable.ic_play);
        } else {

            if (list.get(0).getName().length() + list.get(0).getDetails().length() < TextToSpeech.getMaxSpeechInputLength()) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    tts.speak(list.get(0).getName() + list.get(0).getDetails(), TextToSpeech.QUEUE_ADD, null, null);
                else
                    tts.speak(list.get(0).getName() + list.get(0).getDetails(), TextToSpeech.QUEUE_ADD, null);
//                btn_speak.setImageResource(R.drawable.ic_volume_off_black_24dp);
                btn_speak_.setImageResource(R.drawable.ic_pause_button);

                Toast.makeText(this, "Please wait....Audio is starting", Toast.LENGTH_LONG).show();
            } else {
                String tempText = list.get(0).getName() + list.get(0).getDetails();
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

    private void initTts() {

        tts = new TextToSpeech(RecentActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale(getString(R.string.tts_locale)));
                    tts.setSpeechRate(0.8f);
                } else {
                    Toast.makeText(RecentActivity.this, "Not Supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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

    public static String[] splitT(String src, int len) {
        String[] result = new String[(int) Math.ceil((double) src.length() / (double) len)];
        for (int i = 0; i < result.length; i++)
            result[i] = src.substring(i * len, Math.min(src.length(), (i + 1) * len));
        return result;
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
        //}
    }

    private void playOnline() {
//        btn_speak_.setVisibility(View.GONE);
        linTTS.setVisibility(View.GONE);

        mpintro = new MediaPlayer();
        mpintro.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mpintro.setDataSource(audioServicePath);
            mpintro.prepareAsync();
            Toast.makeText(this, "Please wait....Audio is starting", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "playOnline: " + e.getMessage());
        }

        mpintro.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                controller.show();
//                adView.setVisibility(View.GONE);
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

    private int getTypeOfAudio(String audioFile) {

        if (Patterns.WEB_URL.matcher(audioFile).matches()) {
            return TYPE_EXTERNAL_SERVER;
        } else if (audioFile.isEmpty()) {
            return TYPE_NO_AUDIO;
        } else
            return TYPE_INTERNAL_SERVER;
    }

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


    @Override
    public void onBackPressed() {
        AdManager.onBackPress(RecentActivity.this);
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
        AdManager.onBackPress(RecentActivity.this);
        super.onBackPressed();
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

}

