package com.storiestech.org.datamodels;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class Story  {

    @NotNull
    private String name;
    @Nullable
    private String details;
    private int id = -1;
    private int cat_id = -1;
    private boolean isRead;
    private boolean isBookmarked;
    private boolean isFav;

    @Nullable
    private String audioFile;


    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Nullable
    public String getDetails() {
        return details;
    }

    public void setDetails(@Nullable String details) {
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }


    @Nullable
    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(@Nullable String audioFile) {
        this.audioFile = audioFile;
    }
}
