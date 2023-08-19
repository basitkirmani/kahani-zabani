package com.storiestech.org.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.stories.storyappn.R;
import com.storiestech.org.adapters.AdManager;
import com.storiestech.org.adapters.DBAdapter;
import com.storiestech.org.adapters.RecyclerAdapterStory;
import com.storiestech.org.datamodels.Story;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static com.storiestech.org.activities.MainActivity.checkInternet;
import static com.storiestech.org.adapters.DBManager.isBookmarked;
import static com.storiestech.org.adapters.DBManager.isFav;

public class StoryListActivity extends AppCompatActivity {

    private String CategoryName;
    private String CategoryImage;
    private int CategoryId;
    private RecyclerView data_list;
    private ProgressBar progressBar;
    public static ArrayList<Story> dataList;
    private TextView tv_noData;
    private ImageView imgBack, imgFilter;
    private RadioButton rbNew, rbOld;
    private String type = "new";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeId = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE).getInt(getString(R.string.theme_pref), R.style.AppTheme);
        setTheme(themeId);

        setContentView(R.layout.activity_story_list);

        //getting Intents
        gettingIntents();

        //initializing views
        initViews();


        AdManager.setUpBanner(this);
        AdManager.setUpInterstitialAd(this);

        if (checkInternet(StoryListActivity.this)) {
            //get Data From New Server And Populate Database or update data from server
            getDataFromServer();
        } else {
            tv_noData.setVisibility(View.VISIBLE);
            tv_noData.setText(R.string.nointernet);
        }

    }

    private void gettingIntents() {
        Intent intent = getIntent();
        CategoryId = intent.getIntExtra("categoryId", -1);
        CategoryName = intent.getStringExtra("categoryName");
        CategoryImage = intent.getStringExtra("categoryDetail");
    }

    private void initViews() {
        SimpleDraweeView app_bar_image = findViewById(R.id.app_bar_image);
        TextView list_title = findViewById(R.id.list_title);
        Toolbar story_list_toolbar = findViewById(R.id.story_list_toolbar);
        View fragment_list = findViewById(R.id.list_fragment);
        progressBar = fragment_list.findViewById(R.id.progressBar);
        data_list = fragment_list.findViewById(R.id.data_list);
        tv_noData = fragment_list.findViewById(R.id.favs);
        imgBack = findViewById(R.id.imgBack2);
        imgFilter = findViewById(R.id.imgFilter);

        //setting text and image
        list_title.setText(CategoryName);
        story_list_toolbar.setTitle(CategoryName);

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
        Uri uri = Uri.parse(getString(R.string.imagePath) + CategoryImage);
        app_bar_image.setImageURI(uri);

        story_list_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getDataFromServer() {

        String hp = getString(R.string.link) + getString(R.string.story) + "&id=" + CategoryId;
        ;
        //creating a string request to send request to the url
        hp = hp.replace(" ", "%20");
        Log.w(getClass().getName(), hp);

        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        progressBar.setVisibility(View.GONE);
                        Log.e("Response", response);
                        try {
                            JSONObject jo_data = new JSONObject(response);
                            JSONArray ja_data_category = jo_data.getJSONArray("story");
                            dataList = new ArrayList<>();
                            DBAdapter adapter = new DBAdapter(StoryListActivity.this);

                            for (int i = 0; i < ja_data_category.length(); i++) {
                                JSONObject jo_story_detail = ja_data_category.getJSONObject(i);
                                Story temp = new Story();
                                temp.setCat_id(jo_story_detail.getInt("category_id"));
                                temp.setName(jo_story_detail.getString("title"));
                                temp.setDetails(jo_story_detail.getString("story"));
                                temp.setId(jo_story_detail.getInt("id"));
                                temp.setBookmarked(isBookmarked(temp.getId(), adapter));
                                temp.setFav(isFav(temp.getId(), adapter));
                                temp.setAudioFile(jo_story_detail.getString("audio"));
                                dataList.add(temp);
                            }
                            adapter.close();
                            RecyclerAdapterStory recyclerAdapter = new RecyclerAdapterStory(StoryListActivity.this, dataList, new RecyclerAdapterStory.onCategoryListClick() {
                                @Override
                                public void onItemClick(View v, int position) {

                                    Intent intent = new Intent(StoryListActivity.this, DetailActivity.class);
                                    intent.putExtra("storyId", dataList.get(position).getId());
                                    intent.putExtra("storyName", dataList.get(position).getName());
                                    intent.putExtra("storyDetail", dataList.get(position).getDetails());
                                    intent.putExtra("isFav", dataList.get(position).isFav());
                                    intent.putExtra("isBookMark", dataList.get(position).isBookmarked());
                                    intent.putExtra("audio", dataList.get(position).getAudioFile());
                                    intent.putExtra("Activity", "StoryList");
                                    intent.putExtra("position", "" + position);
                                    intent.putExtra("type", "fromstorylist");
                                    startActivity(intent);

                                }
                            });
                            if (type.equals("new")) {

                            } else {
                                Collections.reverse(dataList);
                            }
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(StoryListActivity.this);
                            data_list.setLayoutManager(linearLayoutManager);
                            data_list.setAdapter(recyclerAdapter);
                            if (dataList.size() == 0) {
                                tv_noData.setVisibility(View.VISIBLE);
                                tv_noData.setText(R.string.txt_no_Stories);
                            } else tv_noData.setVisibility(View.GONE);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurs

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            Log.e("Status code", String.valueOf(networkResponse.statusCode));
                            Toast.makeText(getApplicationContext(), String.valueOf(networkResponse.statusCode), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);

    }

    @Override
    public void onBackPressed() {
        AdManager.onBackPress(StoryListActivity.this);
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
                getDataFromServer();
                bottomSheetDialog.cancel();
                rbNew.setChecked(true);
                rbOld.setChecked(false);
            }
        });

        rbOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "old";
                getDataFromServer();
                bottomSheetDialog.cancel();
                rbOld.setChecked(true);
                rbNew.setChecked(false);
            }
        });

        linCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();
                getDataFromServer();
            }
        });
        bottomSheetDialog.show();
    }

}
