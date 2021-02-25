package com.example.ping;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

public class PingSnackbar {

    private final Resources resources;
    private final Theme theme;
    private final View layout;
    private Snackbar snackbar;

    public PingSnackbar(Resources resources, Theme theme, View layout){
        this.resources = resources;
        this.theme = theme;
        this.layout = layout;
    }

    public Snackbar getSnackbar(){
        return snackbar;
    }

    public void show(){
        snackbar.show();
    }

    public void set(String text, int drawable, int length){
        snackbar = Snackbar.make(layout, text, length);
        snackbar.setBackgroundTint(resources.getColor(R.color.colorPrimary, theme));
        snackbar.setTextColor(resources.getColor(R.color.colorAccent, theme));
        View snackBarLayout = snackbar.getView();

        TextView textView = snackBarLayout.findViewById(R.id.snackbar_text);
        textView.setTextSize(20);

        if (drawable != -1) {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
            textView.setCompoundDrawablePadding(resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding));
        }
    }

}
