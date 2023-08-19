package com.storiestech.org.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stories.storyappn.R;
import com.storiestech.org.datamodels.Category;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RecyclerAdapterHome extends RecyclerView.Adapter<ListHolderHome> {

    @NotNull
    private BitmapFactory.Options options;

    private final onCategoryListClick onCategoryListClick;

    @NotNull
    private final Context context;
    @NotNull
    private final ArrayList<Category> list;

    public RecyclerAdapterHome(@NotNull Context context, @NotNull ArrayList<Category> list, onCategoryListClick onCategoryListClick) {
        this.context = context;
        this.list = list;
        this.onCategoryListClick = onCategoryListClick;

    }

    @NonNull
    @Override
    public ListHolderHome onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        final ListHolderHome myViewHolder = new ListHolderHome(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCategoryListClick.onItemClick(itemView, myViewHolder.getLayoutPosition());
            }
        });
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolderHome holder, int position) {
        Bitmap tmp = null;
//        try {
//          //  tmp = BitmapFactory.decodeStream(context.getAssets().open("images/" + list.get(position).getDetails()), null, options);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        DBAdapter dbAdapter = new DBAdapter(context);
//        SpannableString str = new SpannableString(list.get(position).getName() + "\n" + DBManager.getStoryCount(dbAdapter, list.get(position).getId()) + " Stories");
//        str.setSpan(new RelativeSizeSpan(0.9f), str.toString().indexOf("\n"), str.length(), 0);
//        str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.toString().indexOf("\n"), 0);
//        dbAdapter.close();

        //setting text and image
        Uri uri = Uri.parse(context.getString(R.string.imagePath) + list.get(position).getCat_image());
        holder.iv_category.setImageURI(uri);
        holder.tv_category.setText(list.get(position).getCat_name());
        holder.tv_totStory.setText(list.get(position).getStory_count() + " Stories");

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public interface onCategoryListClick {
        void onItemClick(View v, int position);
    }

}
