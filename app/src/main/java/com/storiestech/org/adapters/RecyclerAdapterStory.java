package com.storiestech.org.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.stories.storyappn.R;
import com.storiestech.org.activities.ReadLaterActivity;
import com.storiestech.org.activities.StoryListActivity;
import com.storiestech.org.datamodels.Story;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RecyclerAdapterStory extends RecyclerView.Adapter<RecyclerAdapterStory.ListHolderStory> {

    private final onCategoryListClick onCategoryListClick;

    @NotNull
    private final Context context;
    @NotNull
    private final ArrayList<Story> list;


    public RecyclerAdapterStory(@NotNull Context context, @NotNull ArrayList<Story> list, onCategoryListClick onCategoryListClick) {
        this.context = context;
        this.list = list;
        this.onCategoryListClick = onCategoryListClick;
    }


    @NonNull
    @Override
    public ListHolderStory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_list_item, parent, false);
        final ListHolderStory myViewHolder = new ListHolderStory(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCategoryListClick.onItemClick(itemView, myViewHolder.getLayoutPosition());
            }
        });
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolderStory holder, int position) {
        String temp = position + 1 + "";
        holder.tv_thumb.setText(temp);
        holder.item_title.setText(list.get(position).getName());

        holder.btn_bookmark.setOnClickListener(holder.onClickListener);
        holder.btn_fav.setOnClickListener(holder.onClickListener);
        holder.btn_send.setOnClickListener(holder.onClickListener);


        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE);
        int themeId = prefs.getInt(context.getString(R.string.theme_pref), R.style.AppTheme);

        TypedArray array = context.obtainStyledAttributes(themeId, new int[]{R.attr.colorPrimary});
        int clr = array.getColor(0, Color.WHITE);
        array.recycle();
        holder.tv_thumb.setBackgroundColor(clr);


        if (list.get(position).isBookmarked()) {
            holder.btn_bookmark.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            holder.btn_bookmark.setImageResource(R.drawable.ic_bookmark_outline);
        }

        if (list.get(position).isFav()) {
            holder.btn_fav.setImageResource(R.drawable.ic_favorite_black_24dp);
        } else {
            holder.btn_fav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private ArrayList<Story> getList() {
        return list;
    }


    public interface onCategoryListClick {
        void onItemClick(View v, int position);
    }

    public class ListHolderStory extends RecyclerView.ViewHolder {
        final TextView tv_thumb;
        final TextView item_title;
        final ImageView btn_bookmark;
        final ImageView btn_send;
        final ImageView btn_fav;
        final View.OnClickListener onClickListener;

        ListHolderStory(View itemView) {
            super(itemView);
            tv_thumb = itemView.findViewById(R.id.thumb);
            item_title = itemView.findViewById(R.id.item_title);
            btn_bookmark = itemView.findViewById(R.id.btn_bookmark);
            btn_send = itemView.findViewById(R.id.btn_send);
            btn_fav = itemView.findViewById(R.id.btn_fav);

            onClickListener = new View.OnClickListener() {
                String type;

                @Override
                public void onClick(View v) {
                    Story temp = RecyclerAdapterStory.this.getList().get(ListHolderStory.this.getAdapterPosition());
                    switch (v.getId()) {

                        case R.id.btn_bookmark:
                            type = "";
                            if (temp.isBookmarked()) {
                                DBAdapter adapter = new DBAdapter(context);
                                DBManager.remove(DBManager.CompanionQuery.getTBL_READ_LATER(), temp.getId(), adapter);
                                type = "Removed from";
                            } else {
                                DBAdapter adapter = new DBAdapter(context);
                                DBManager.addToStory(temp, adapter, (Activity) context);
                                DBManager.addToBookmark(temp.getId(), adapter);
                                type = "Added to";
                            }
                            Snackbar.make(v, type + " Read Later", Snackbar.LENGTH_SHORT).show();
                            list.get(getAdapterPosition()).setBookmarked(!list.get(getAdapterPosition()).isBookmarked());

                            if (list.get(getAdapterPosition()).isBookmarked()) {
                                btn_bookmark.setImageResource(R.drawable.ic_bookmark_filled);
                            } else {
                                btn_bookmark.setImageResource(R.drawable.ic_bookmark_outline);
                                getList().remove(getList().get(getAdapterPosition()));
                            }

                            break;
                        case R.id.btn_fav:
                            type = "";
                            if (temp.isFav()) {
                                DBAdapter adapter = new DBAdapter(context);
                                DBManager.remove(DBManager.CompanionQuery.getTBL_FAV(), temp.getId(), adapter);
                                type = "Removed from";
                            } else {
                                DBAdapter adapter = new DBAdapter(context);
                                DBManager.addToStory(temp, adapter, (Activity) context);
                                DBManager.addTofav(temp.getId(), adapter);
                                type = "Added to";
                            }
                            Snackbar.make(v, type + " Favourites", Snackbar.LENGTH_SHORT).show();
                            list.get(getAdapterPosition()).setFav(!list.get(getAdapterPosition()).isFav());

                            if (list.get(getAdapterPosition()).isFav()) {
                                btn_fav.setImageResource(R.drawable.ic_favorite_black_24dp);

                            } else {
                                btn_fav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                getList().remove(getList().get(getAdapterPosition()));
                            }
                            break;
                        case R.id.btn_send:
                            DBAdapter adapter = new DBAdapter(context);
                            temp.setDetails(DBManager.getStoryDetailById(temp.getId(), adapter));
                            adapter.close();
                            context.startActivity(new Intent(Intent.ACTION_SEND)
                                    .putExtra(Intent.EXTRA_TEXT, temp.getName() + "\n\n" + temp.getDetails())
                                    .setType("text/plain"));
                            break;
                    }

                    if (v.getId() != R.id.btn_send && !(context instanceof StoryListActivity)) {
                        notifyDataSetChanged();
                    }

                    if (getList().size() == 0) {
                        String tempError;
                       /* if (context instanceof FavoritesActivity) {
                            tempError = context.getString(R.string.txt_no_fav);
                        } else*/
                        if (context instanceof ReadLaterActivity) {
                            tempError = context.getString(R.string.txt_no_read);
                        } else {
                            tempError = context.getString(R.string.txt_no_recent);
                        }
                        Activity activity = (Activity) context;
                        noDataCheck(activity, false, tempError);
                    }
                }
            };
        }
    }

    private static void noDataCheck(Activity activity, Boolean isData, String error) {
        TextView textView = activity.findViewById(R.id.favs);
        if (isData) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(error);
        }
    }
}
