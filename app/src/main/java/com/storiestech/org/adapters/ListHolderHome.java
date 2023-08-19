package com.storiestech.org.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.stories.storyappn.R;

class ListHolderHome extends RecyclerView.ViewHolder{
final TextView tv_category,tv_totStory;
final SimpleDraweeView iv_category;

    public ListHolderHome(View itemView) {
        super(itemView);
        tv_category = itemView.findViewById(R.id.tv_category);
        iv_category  = itemView.findViewById(R.id.iv_category);
        tv_totStory  = itemView.findViewById(R.id.tv_totStory);
    }
}
