package com.storiestech.org.adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.storiestech.org.datamodels.Category;
import com.storiestech.org.datamodels.Story;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DBManager {
    @NotNull
    private DBAdapter adapter;
    private static String TBL_CATEGORY = "tbl_category";
    private static String COL_ID = "id";
    private static String COL_NAME = "name";
    private static String COL_IMAGE = "image";
    @NotNull
    private static String TBL_FAV = "tbl_fav";
    private static String COL_STORY_ID = "story_id";
    private static String TBL_STORIES = "tbl_stories";
    private static String COL_STORY_TITLE = "story_title";
    private static String COL_STORY = "story";
    private static String COL_CAT_ID = "category_id";
    @NotNull
    private static String TBL_READ_LATER = "tbl_read_later";
    @NotNull
    private static String TBL_RECENTS = "tbl_recents";

    @NotNull
    public DBAdapter getAdapter() {
        return this.adapter;
    }

    public void setAdapter(@NotNull DBAdapter var1) {
        this.adapter = var1;
    }

    @NotNull
    public static ArrayList<Category> getCategories(DBAdapter adapter) {
        ArrayList<Category> stories = new ArrayList<>();

        SQLiteDatabase SQLDataBase = adapter.db;
        Cursor cr = SQLDataBase != null ? SQLDataBase.query(DBManager.TBL_CATEGORY, null, null, null, null, null, null, null) : null;
        if (cr != null) {
            while (cr.moveToNext()) {
                Category category = new Category();
                category.setCat_id(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_ID())));
                String var10001 = cr.getString(cr.getColumnIndex(CompanionQuery.getCOL_NAME()));
                category.setCat_name(var10001);
                category.setCat_image(cr.getString(cr.getColumnIndex(CompanionQuery.getCOL_IMAGE())));
                stories.add(category);
            }
            cr.close();
            SQLDataBase.close();
        }
        return stories;
    }

    @NotNull
    public static ArrayList getFavs(DBAdapter adapter) {
        ArrayList<Story> stories = new ArrayList<>();
        SQLiteDatabase SQLDataBase = adapter.db;
        Story var10001;
        Cursor cr = SQLDataBase.query(CompanionQuery.getTBL_FAV(), null, null, null, null, null, null);
        if (cr != null) {
            while (cr.moveToNext()) {
                Story story = getStoryById(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_STORY_ID())), adapter);
                stories.add(story);
            }
            cr.close();
            SQLDataBase.close();
        }
        return stories;
    }

    public static void addToStory(Story data, final DBAdapter adapter, final Activity activity) {

        SQLiteDatabase SQLDataBase = adapter.db;
        ContentValues cv = new ContentValues();
        cv.put(DBManager.COL_CAT_ID, data.getCat_id());
        cv.put(DBManager.COL_STORY_TITLE, data.getName());
        cv.put(DBManager.COL_STORY, data.getDetails());
        cv.put(DBManager.COL_STORY_ID, data.getId());
        cv.put("audio", data.getAudioFile());
        SQLDataBase.insertWithOnConflict(DBManager.TBL_STORIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

    }

    @NotNull
    public static ArrayList getStories(int catId, DBAdapter adapter) {
        ArrayList<Story> stories = new ArrayList<>();
        SQLiteDatabase SQLDataBase = adapter.db;
        Cursor cr = SQLDataBase.query(CompanionQuery.getTBL_STORIES(), new String[]{CompanionQuery.getCOL_STORY_ID(), CompanionQuery.getCOL_STORY_TITLE(), CompanionQuery.getCOL_CAT_ID(), "story", "audio"}, "" + CompanionQuery.getCOL_CAT_ID() + "=?", new String[]{String.valueOf(catId)}, null, null, null);
        while (cr.moveToNext()) {
            Story story = new Story();
            story.setId(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_STORY_ID())));
            String var10001 = cr.getString(cr.getColumnIndex(CompanionQuery.getCOL_STORY_TITLE()));
            story.setName(var10001);
            story.setCat_id(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_CAT_ID())));
            story.setFav(isFav(story.getId(), adapter));
            story.setBookmarked(isBookmarked(story.getId(), adapter));
            story.setDetails(cr.getString(cr.getColumnIndex("story")));
            story.setAudioFile(cr.getString(cr.getColumnIndex("audio")));
            stories.add(story);
        }
        cr.close();
        SQLDataBase.close();
        return stories;
    }

    @Nullable
    private static Story getStoryById(int id, DBAdapter adapter) {
        SQLiteDatabase SQLDataBase = adapter.db;
        Cursor cr = SQLDataBase.query(CompanionQuery.getTBL_STORIES(), null, "" + CompanionQuery.getCOL_STORY_ID() + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cr.moveToNext()) {
            Story tmp = new Story();
            tmp.setCat_id(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_CAT_ID())));
            tmp.setId(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_STORY_ID())));
            String var10001 = cr.getString(cr.getColumnIndex(CompanionQuery.getCOL_STORY_TITLE()));
            tmp.setName(var10001);
            String txt_detail = cr.getString(cr.getColumnIndex(CompanionQuery.getCOL_STORY()));
            tmp.setDetails(txt_detail);
            tmp.setFav(isFav(tmp.getId(), adapter));
            tmp.setBookmarked(isBookmarked(tmp.getId(), adapter));
            tmp.setAudioFile(cr.getString(cr.getColumnIndex("audio")));
            cr.close();
            return tmp;
        } else {
            return null;
        }
    }

    @Nullable
    public static String getStoryDetailById(int id, DBAdapter adapter) {
        SQLiteDatabase SQLDataBase = adapter.db;
        Cursor cr = SQLDataBase.query(CompanionQuery.getTBL_STORIES(), new String[]{CompanionQuery.getCOL_STORY()}, "" + CompanionQuery.getCOL_STORY_ID() + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cr.moveToNext()) {
            String temp = cr.getString(cr.getColumnIndex(CompanionQuery.getCOL_STORY()));
            cr.close();
            return temp;

        }
        return null;
    }

    public static ArrayList<Story> searchStories(String query, DBAdapter adapter) {
        ArrayList<Story> stories = new ArrayList<>();
        SQLiteDatabase SQLDataBase = adapter.db;
        Cursor tbl_stories;
        if (SQLDataBase != null) {
            tbl_stories = SQLDataBase.query("tbl_stories", null, "story_title like " + "'%" + query + "%'", null, null, null, null, null);
            while (tbl_stories.moveToNext()) {
                Story story = new Story();
                story.setId(tbl_stories.getInt(tbl_stories.getColumnIndex(CompanionQuery.getCOL_STORY_ID())));
                String var10001 = tbl_stories.getString(tbl_stories.getColumnIndex(CompanionQuery.getCOL_STORY_TITLE()));
                story.setName(var10001);
                story.setDetails(tbl_stories.getString(tbl_stories.getColumnIndex("story")));
                story.setCat_id(tbl_stories.getInt(tbl_stories.getColumnIndex(CompanionQuery.getCOL_CAT_ID())));
                story.setFav(isFav(story.getId(), adapter));
                story.setBookmarked(isBookmarked(story.getId(), adapter));
                story.setAudioFile(tbl_stories.getString(tbl_stories.getColumnIndex("audio")));
                stories.add(story);
            }
            tbl_stories.close();
            SQLDataBase.close();
            adapter.close();
        }

        return stories;
    }

    public static void addToCategory(final ArrayList<Category> data, final DBAdapter adapter, final Activity activity) {

        SQLiteDatabase SQLDataBase = adapter.db;
        for (int i = 0; i < data.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(DBManager.COL_ID, data.get(i).getCat_id());
            cv.put(DBManager.COL_NAME, data.get(i).getCat_name());
            cv.put(DBManager.COL_IMAGE, data.get(i).getCat_image());
            SQLDataBase.insertWithOnConflict(DBManager.TBL_CATEGORY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }

    }

    public static void addTofav(int id, DBAdapter adapter) {
        ContentValues cv = new ContentValues();
        cv.put(CompanionQuery.getCOL_STORY_ID(), id);
        SQLiteDatabase SQLDataBase = adapter.db;
        SQLDataBase.insert(CompanionQuery.getTBL_FAV(), null, cv);
    }

    public static boolean isFav(int id, DBAdapter adapter) {
        SQLiteDatabase SQLDataBase = adapter.db;
        return SQLDataBase.query(CompanionQuery.getTBL_FAV(), null, "" + CompanionQuery.getCOL_STORY_ID() + "=?", new String[]{String.valueOf(id)}, null, null, null).moveToNext();
    }

    public static boolean isBookmarked(int id, DBAdapter adapter) {

        SQLiteDatabase SQLDataBase = adapter.db;
        return SQLDataBase.query(CompanionQuery.getTBL_READ_LATER(), null, "" + CompanionQuery.getCOL_STORY_ID() + "=?", new String[]{String.valueOf(id)}, null, null, null).moveToNext();
    }

    @NotNull
    public static ArrayList<Story> getBookmarked(DBAdapter adapter) {
        ArrayList<Story> stories = new ArrayList<>();
        SQLiteDatabase SQLDataBase = adapter.db;

        Story var10001;
        Cursor cr;
        for (cr = SQLDataBase.query(CompanionQuery.getTBL_READ_LATER(), null, null, null, null, null, null); cr.moveToNext(); stories.add(var10001)) {
            var10001 = getStoryById(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_STORY_ID())), adapter);
        }

        cr.close();
        return stories;
    }

    public static void addToBookmark(int id, DBAdapter adapter) {
        ContentValues cv = new ContentValues();
        cv.put(CompanionQuery.getCOL_STORY_ID(), id);
        SQLiteDatabase SQLDataBase = adapter.db;
        SQLDataBase.insert(CompanionQuery.getTBL_READ_LATER(), null, cv);
    }

    @NotNull
    public static ArrayList<Story> getRecent(DBAdapter adapter) {
        ArrayList<Story> stories = new ArrayList<>();
        SQLiteDatabase SQLDataBase = adapter.db;
        Story var10001;
        Cursor cr;
        for (cr = SQLDataBase.query(CompanionQuery.getTBL_RECENTS(), null, null, null, null, null, "" + CompanionQuery.getCOL_ID() + " desc"); cr.moveToNext(); stories.add(var10001)) {
            var10001 = getStoryById(cr.getInt(cr.getColumnIndex(CompanionQuery.getCOL_STORY_ID())), adapter);
        }

        cr.close();
        return stories;
    }

    public static void addToRecent(int id, DBAdapter adapter) {
        ContentValues cv = new ContentValues();
        cv.put(CompanionQuery.getCOL_STORY_ID(), id);
        SQLiteDatabase SQLDataBase = adapter.db;
        if (SQLDataBase != null) {
            SQLDataBase.insertWithOnConflict(CompanionQuery.getTBL_RECENTS(), null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public static int getStoryCount(DBAdapter adapter, int catId) {
        SQLiteDatabase SQLDataBase = adapter.db;
        Cursor cr;
        if (SQLDataBase != null) {
            cr = SQLDataBase.rawQuery("select count(" + CompanionQuery.getCOL_STORY_ID() + ") from " + CompanionQuery.getTBL_STORIES() + " where " + CompanionQuery.getCOL_CAT_ID() + "=?", new String[]{String.valueOf(catId)});
            cr.moveToNext();
            int temp = cr.getInt(0);
            cr.close();
            return temp;
        }
        return -1;
    }

    public static void clearAll(@NotNull String tbl, DBAdapter adapter) {
        SQLiteDatabase SQLDataBase = adapter.db;
        if (SQLDataBase != null) {
            SQLDataBase.delete(tbl, null, null);
        }
    }

    public static void remove(@NotNull String tbl, int id, DBAdapter adapter) {
        SQLiteDatabase SQLDataBase = adapter.db;
        if (SQLDataBase != null) {
            SQLDataBase.delete(tbl, "" + CompanionQuery.getCOL_STORY_ID() + "=?", new String[]{String.valueOf(id)});
        }
    }

    public static void removeCategory(@NotNull String tbl, String[] id, DBAdapter adapter) {
        SQLiteDatabase SQLDataBase = adapter.db;
        if (SQLDataBase != null) {
            String whereClause = "id IN  (" + TextUtils.join(",", id) + ")";
            SQLDataBase.delete(tbl, whereClause, null);
        }
    }

    public static void removeStory(String[] story_id, DBAdapter dbAdapter) {
        SQLiteDatabase SQLDataBase = dbAdapter.db;
        if (SQLDataBase != null) {
            String whereClause = "story_id IN  (" + TextUtils.join(",", story_id) + ")";
            SQLDataBase.delete(DBManager.TBL_STORIES, whereClause, null);

        }

    }

    public DBManager(@NotNull Context context) {
        super();
        this.adapter = new DBAdapter(context);
    }

    public static class CompanionQuery {
        public String getTBL_CATEGORY() {
            return DBManager.TBL_CATEGORY;
        }

        public void setTBL_CATEGORY(String var1) {
            DBManager.TBL_CATEGORY = var1;
        }

        static String getCOL_ID() {
            return DBManager.COL_ID;
        }

        public static void setCOL_ID(String var1) {
            DBManager.COL_ID = var1;
        }

        static String getCOL_NAME() {
            return DBManager.COL_NAME;
        }

        public void setCOL_NAME(String var1) {
            DBManager.COL_NAME = var1;
        }

        static String getCOL_IMAGE() {
            return DBManager.COL_IMAGE;
        }

        public void setCOL_IMAGE(String var1) {
            DBManager.COL_IMAGE = var1;
        }

        @NotNull
        public static String getTBL_FAV() {
            return DBManager.TBL_FAV;
        }

        public void setTBL_FAV(@NotNull String var1) {
            DBManager.TBL_FAV = var1;
        }

        static String getCOL_STORY_ID() {
            return DBManager.COL_STORY_ID;
        }

        public void setCOL_STORY_ID(String var1) {
            DBManager.COL_STORY_ID = var1;
        }

        static String getTBL_STORIES() {
            return DBManager.TBL_STORIES;
        }

        public void setTBL_STORIES(String var1) {
            DBManager.TBL_STORIES = var1;
        }

        static String getCOL_STORY_TITLE() {
            return DBManager.COL_STORY_TITLE;
        }

        public void setCOL_STORY_TITLE(String var1) {
            DBManager.COL_STORY_TITLE = var1;
        }

        static String getCOL_STORY() {
            return DBManager.COL_STORY;
        }

        public void setCOL_STORY(String var1) {
            DBManager.COL_STORY = var1;
        }

        static String getCOL_CAT_ID() {
            return DBManager.COL_CAT_ID;
        }

        public void setCOL_CAT_ID(String var1) {
            DBManager.COL_CAT_ID = var1;
        }

        @NotNull
        public static String getTBL_READ_LATER() {
            return DBManager.TBL_READ_LATER;
        }

        public void setTBL_READ_LATER(@NotNull String var1) {
            DBManager.TBL_READ_LATER = var1;
        }

        @NotNull
        static String getTBL_RECENTS() {
            return DBManager.TBL_RECENTS;
        }

        public void setTBL_RECENTS(@NotNull String var1) {
            DBManager.TBL_RECENTS = var1;
        }

    }
}