package com.storiestech.org.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.stories.storyappn.R;
import com.storiestech.org.activities.MainActivity;
import com.storiestech.org.activities.SettingActivity;

import org.jetbrains.annotations.NotNull;

public class RecyclerAdapterColor extends RecyclerView.Adapter<RecyclerAdapterColor.ColorHolder> {

    private final int[] themes = {R.style.AppTheme
            , R.style.GreenTheme
            , R.style.RedTheme
            , R.style.PurpleTheme
            , R.style.TealTheme
            , R.style.GreenBlueTheme
            , R.style.LimeTheme
            , R.style.BlueGrayTheme
            , R.style.PinkTheme
            , R.style.DarkBlueTheme
            , R.style.DarkYellowTheme
            , R.style.BrownTheme};

    private ToggleButton toggleButton;
    private final SharedPreferences prefs;
    private final Context context;
    private Activity activity;

    public RecyclerAdapterColor(SettingActivity context) {
        this.context = context;
        this.activity = context;
        prefs = context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ColorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.theme_chooser, parent, false);
        return new ColorHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorHolder holder, int position) {

        if (this.themes[position] == context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE).getInt(context.getString(R.string.theme_pref), R.style.AppTheme)) {
            holder.view.setChecked(true);
            this.toggleButton = holder.view;
        }

        TypedArray array = context.obtainStyledAttributes(themes[position], new int[]{R.attr.colorPrimary});
        Drawable drawable = ContextCompat.getDrawable(this.context, R.drawable.color_state).mutate();
        int clr = array.getColor(0, Color.WHITE);
        array.recycle();
        drawable.setColorFilter(clr, PorterDuff.Mode.SRC_IN);
        drawable = getDrawable(drawable);
        holder.view.setButtonDrawable(drawable);

    }

    @Override
    public int getItemCount() {
        return themes.length;
    }

    private Drawable getDrawable(Drawable main_drawable) {
        Drawable drawable = ResourcesCompat.getDrawable(this.context.getResources(), R.drawable.ic_check2, null);
        StateListDrawable stateListDrawable = new StateListDrawable();
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{main_drawable, drawable});

        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, layerDrawable);
        stateListDrawable.addState(new int[0], main_drawable);
        return stateListDrawable;

    }

    class ColorHolder extends RecyclerView.ViewHolder {
        @NotNull
        private final ToggleButton view;

        ColorHolder(View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.toggleBtn);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (view.isChecked()) {
                        if (toggleButton != null)
                            toggleButton.setChecked(false);
                        toggleButton = view;
                        prefs.edit().putInt(context.getString(R.string.theme_pref), themes[getAdapterPosition()]).apply();
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        activity.finish();
                    }
                }
            });
        }
    }
}
