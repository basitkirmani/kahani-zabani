package com.storiestech.org.activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.stories.storyappn.R;
import com.storiestech.org.adapters.AdManager;
import com.storiestech.org.adapters.DBAdapter;
import com.storiestech.org.adapters.RecyclerAdapterHome;
import com.storiestech.org.adapters.RecyclerAdapterStory;
import com.storiestech.org.datamodels.Category;
import com.storiestech.org.datamodels.Story;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static com.storiestech.org.adapters.DBManager.isBookmarked;
import static com.storiestech.org.adapters.DBManager.isFav;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Mainactivity";
    private DrawerLayout drawer;
    private RecyclerView recyclerView;
    private ArrayList<Category> categoryArrayList;
    private ProgressBar progressBar;
    private TextView tv_noData;
    ArrayList<Story> dataList;
    private ImageView imgCat;
    public static String type = "new";
    private RadioButton rbNew, rbOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get SharedPref and set Theme
        int themeId = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE).getInt(getString(R.string.theme_pref), R.style.AppTheme);
        setTheme(themeId);
//        gettingSharedPref();
        setContentView(R.layout.activity_main);

        imgCat = findViewById(R.id.imgCat);
        imgCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_Dialog();
            }
        });

        //initiate views and navigation Drawer
        initViews();

        if (checkInternet(MainActivity.this)) {
            //get Data From Server And Populate Database or update data from server
            getDataFromServer();

        } else {
            tv_noData.setVisibility(View.VISIBLE);
            tv_noData.setText(R.string.nointernet);
        }
        AdManager.setUpBanner(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        // Retrieve the SearchView and plug it into SearchManager

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    //     populateData();
                    return true;
                }
            });

            EditText searchPlate = searchView.findViewById(R.id.search_src_text);
            searchPlate.setHint("Search");
            searchPlate.setTextColor(Color.WHITE);
            searchPlate.setHintTextColor(Color.WHITE);
            View searchPlateView = searchView.findViewById(R.id.search_plate);
            searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            // use this method for search process
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // use this method when query submitted

                    if (checkInternet(MainActivity.this)) {
                        updateUI(query);
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // use this method for auto complete search process
                    if (newText.length() == 0) {
                        if (checkInternet(MainActivity.this)) {
                            //get Data From Server And Populate Database or update data from server
                            getDataFromServer();
                        } else {
                            tv_noData.setVisibility(View.VISIBLE);
                            tv_noData.setText(R.string.nointernet);
                        }
                    }
                    return false;
                }
            });
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            }

        }

        return true;
    }

    private void updateUI(String query) {

        String hp = getString(R.string.link) + getString(R.string.search) + "&search=" + query;
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
                            DBAdapter adapter = new DBAdapter(MainActivity.this);

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

                            RecyclerAdapterStory recyclerAdapter = new RecyclerAdapterStory(MainActivity.this, dataList, new RecyclerAdapterStory.onCategoryListClick() {
                                @Override
                                public void onItemClick(View v, int position) {
                                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                    intent.putExtra("storyId", dataList.get(position).getId());
                                    intent.putExtra("storyName", dataList.get(position).getName());
                                    intent.putExtra("storyDetail", dataList.get(position).getDetails());
                                    intent.putExtra("isFav", dataList.get(position).isFav());
                                    intent.putExtra("isBookMark", dataList.get(position).isBookmarked());
                                    intent.putExtra("audio", dataList.get(position).getAudioFile());
                                    startActivity(intent);
                                }
                            });
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setAdapter(recyclerAdapter);

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

    private void getDataFromServer() {

        String hp;
        //creating a string request to send request to the url

        hp = getString(R.string.link) + getString(R.string.category);
        Log.w(getClass().getName(), hp);
        hp = hp.replace(" ", "%20");

        progressBar = findViewById(R.id.progressBar);
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

                            JSONArray ja_data_category = jo_data.getJSONArray("category");
                            categoryArrayList = new ArrayList<>();

                            for (int i = 0; i < ja_data_category.length(); i++) {
                                JSONObject jo_category_detail = ja_data_category.getJSONObject(i);
                                Category temp = new Category();
                                temp.setCat_id(jo_category_detail.getInt("id"));
                                temp.setCat_image(jo_category_detail.getString("img"));
                                temp.setCat_name(jo_category_detail.getString("name"));
                                temp.setStory_count(jo_category_detail.getString("story_count"));
                                categoryArrayList.add(temp);
                            }

                            RecyclerAdapterHome recyclerAdapter = new RecyclerAdapterHome(MainActivity.this, categoryArrayList, new RecyclerAdapterHome.onCategoryListClick() {
                                @Override
                                public void onItemClick(View v, int position) {
                                    Intent intent = new Intent(MainActivity.this, StoryListActivity.class);
                                    intent.putExtra("categoryId", categoryArrayList.get(position).getCat_id());
                                    intent.putExtra("categoryName", categoryArrayList.get(position).getCat_name());
                                    intent.putExtra("categoryDetail", categoryArrayList.get(position).getCat_image());
                                    startActivity(intent);
                                }
                            });
                            if (type.equals("new")) {

                            } else {
                                Collections.reverse(categoryArrayList);
                            }
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setAdapter(recyclerAdapter);
                            if (categoryArrayList.size() == 0) {
                                tv_noData.setVisibility(View.VISIBLE);
                                tv_noData.setText(R.string.txt_nocategories_error);
                            } else tv_noData.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e(TAG, "onErrorResponse: " + error.getMessage());
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

    private void gettingSharedPref() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        int themeId = prefs.getInt(getString(R.string.theme_pref), R.style.AppTheme);
        setTheme(themeId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        View view = findViewById(R.id.fragmentData);
        recyclerView = view.findViewById(R.id.data_list);
        tv_noData = view.findViewById(R.id.favs);
        setSupportActionBar(toolbar);

        NavigationView nav_view = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer);
        nav_view.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        toggle.syncState();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()) {

            case R.id.drw_nav_read_later:
                i = new Intent(this, ReadLaterActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            case R.id.drw_nav_recents:
                i = new Intent(this, RecentActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("type", "recent");
                startActivity(i);
                break;
            case R.id.drw_nav_settings:
                i = new Intent(this, SettingActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            case R.id.drw_nav_rate:
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                break;
            case R.id.drw_nav_share:
                startActivity((new Intent("android.intent.action.SEND")).putExtra("android.intent.extra.TEXT", "Download App \n https://play.google.com/store/apps/details?id=" + getPackageName()).setType("text/plain"));
                break;
            case R.id.drw_nav_more:
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/developer?id=" + getString(R.string.MORE_APP_LINK))));
                break;

        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    public static boolean checkInternet(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = new NetworkInfo[0];
        if (connectivityManager != null) {
            netInfo = connectivityManager.getAllNetworkInfo();
        }
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        try {
            drawer.closeDrawer(Gravity.LEFT);
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            View view = getLayoutInflater().inflate(R.layout.dialog_exit_app, null, false);

            LinearLayout linExit = view.findViewById(R.id.linExit);
            TextView txtExit = view.findViewById(R.id.txtExit);
            TextView txtExitMsg = view.findViewById(R.id.txtExitMsg);
            TextView tx_yes = view.findViewById(R.id.tx_yes);
            TextView tx_no = view.findViewById(R.id.tx_no);

            tx_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAffinity();
                }
            });

            tx_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            e.getMessage();
            Log.e(TAG, "exitApp: " + e.getMessage());
        }
    }

}
