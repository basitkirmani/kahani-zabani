package com.storiestech.org.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.stories.storyappn.R;
import com.storiestech.org.adapters.AdManager;
import com.storiestech.org.adapters.RecyclerAdapterColor;

public class SettingActivity extends AppCompatActivity {
    public static final int MAX_SIZE = 30;
    public static final int MIN_SIZE = 12;
    private ImageView imgBack;
    private SharedPreferences prefs;
    private TextView tv_sample;
    // private UiModeManager uiModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeId = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE).getInt(getString(R.string.theme_pref), R.style.AppTheme);
        setTheme(themeId);

        setContentView(R.layout.activity_setting);
        //initializing views

        intitViews();
        AdManager.setUpBanner(this);
        AdManager.setUpInterstitialAd(this);

    }

    private void intitViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView main_title = findViewById(R.id.main_title);
        prefs = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
        View view = findViewById(R.id.fragmentSetting);
        RecyclerView color_grid = view.findViewById(R.id.color_grid);
        SwitchCompat night_switch = view.findViewById(R.id.night_switch);
        tv_sample = view.findViewById(R.id.sample);
        SeekBar font_bar = view.findViewById(R.id.font_bar);
        imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        main_title.setText(R.string.txt_settings);

        //settings on font bar
        font_bar.setMax(MAX_SIZE - MIN_SIZE);
        int cr = prefs.getInt(getString(R.string.font_pref), MIN_SIZE + 5);
        font_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_sample.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) progress + (float) MIN_SIZE);
                prefs.edit().putInt(getString(R.string.font_pref), progress + MIN_SIZE).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        font_bar.setProgress(cr - MIN_SIZE);

        //setting of night mode

        if (prefs.getBoolean(getString(R.string.isNight), false)) {
            night_switch.setChecked(true);
        } else {
            night_switch.setChecked(false);
        }

        night_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    prefs.edit().putBoolean(getString(R.string.isNight), true).apply();
                } else {
                    prefs.edit().putBoolean(getString(R.string.isNight), false).apply();
                }
            }
        });

        //setting for theme
        RecyclerAdapterColor recyclerAdapterColor = new RecyclerAdapterColor(SettingActivity.this);
        color_grid.setAdapter(recyclerAdapterColor);

    }

    @Override
    public void onBackPressed() {
        AdManager.onBackPress(SettingActivity.this);
        super.onBackPressed();
        finish();
    }

}
