package com.storiestech.org.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.stories.storyappn.R;
import com.storiestech.org.adapters.AdManager;
import com.storiestech.org.adapters.DBAdapter;
import com.storiestech.org.adapters.DBManager;
import com.storiestech.org.adapters.RecyclerAdapterStory;
import com.storiestech.org.datamodels.Story;

import java.util.ArrayList;
import java.util.Collections;

public class ReadLaterActivity extends AppCompatActivity {
    private RecyclerView data_list;
    private TextView tv_noData;
    private ImageView imgBack, imgFilter;
    private RadioButton rbNew, rbOld;
    private String type = "new";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeId = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE).getInt(getString(R.string.theme_pref), R.style.AppTheme);
        setTheme(themeId);
        setContentView(R.layout.activity_common);

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
        data_list = fragment_list.findViewById(R.id.data_list);
        tv_noData = fragment_list.findViewById(R.id.favs);
        imgBack = findViewById(R.id.imgBack);
        imgFilter = findViewById(R.id.imgFilter);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filter_Dialog();

            }
        });

        //setting text and image
        list_title.setText(R.string.txt_read_later);


    }

    //populating Data
    private void populateData() {

        DBAdapter adapter = new DBAdapter(this);
        final ArrayList<Story> list = DBManager.getBookmarked(adapter);
        adapter.close();

        if (list.isEmpty()) {
            data_list.setVisibility(View.GONE);
            tv_noData.setVisibility(View.VISIBLE);
            tv_noData.setText(R.string.txt_no_read);
        } else {
            data_list.setVisibility(View.VISIBLE);
            tv_noData.setVisibility(View.GONE);
        }

        if (list.size() > 0) {
            imgFilter.setEnabled(true);
            RecyclerAdapterStory recyclerAdapter = new RecyclerAdapterStory(this, list, new RecyclerAdapterStory.onCategoryListClick() {
                @Override
                public void onItemClick(View v, int position) {
                    Intent intent = new Intent(ReadLaterActivity.this, DetailActivity.class);
                    intent.putExtra("storyId", list.get(position).getId());
                    intent.putExtra("storyName", list.get(position).getName());
                    intent.putExtra("storyDetail", list.get(position).getDetails());
                    intent.putExtra("isFav", list.get(position).isFav());
                    intent.putExtra("isBookMark", list.get(position).isBookmarked());
                    intent.putExtra("audio", list.get(position).getAudioFile());
                    startActivity(intent);
                }
            });
            if (type.equals("new")) {

            } else {
                Collections.reverse(list);
            }
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            data_list.setLayoutManager(linearLayoutManager);
            data_list.setAdapter(recyclerAdapter);
            tv_noData.setVisibility(View.GONE);
        } else {
            imgFilter.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        AdManager.onBackPress(ReadLaterActivity.this);
        super.onBackPressed();

    }

    private void filter_Dialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        bottomSheetDialog.setContentView(R.layout.dialog_filter);
//        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        rbNew = bottomSheetDialog.findViewById(R.id.rbNew);
        rbOld = bottomSheetDialog.findViewById(R.id.rbOld);
        LinearLayout linCancle = bottomSheetDialog.findViewById(R.id.linCancle);

        if (type.equals("new")) {
            rbNew.setChecked(true);
            rbOld.setChecked(false);
        } else {
            rbOld.setChecked(true);
            rbNew.setChecked(false);
        }

        rbNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "new";
                populateData();
                bottomSheetDialog.cancel();
                rbNew.setChecked(true);
                rbOld.setChecked(false);
            }
        });

        rbOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "old";
                populateData();
                bottomSheetDialog.cancel();
                rbOld.setChecked(true);
                rbNew.setChecked(false);
            }
        });

        linCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();
                populateData();
            }
        });
        bottomSheetDialog.show();
    }

}