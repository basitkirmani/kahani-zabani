package com.storiestech.org.utilities;

import android.content.Context;

import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

/**
 * Created by RedixbitUser on 6/19/2018.
 */

public class MusicController extends MediaController {

    public MusicController(Context context) {
        super(context);
    }

    @Override
    public void hide() {
    }
    public void removee() {
        super.hide();
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);


    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            removee();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
