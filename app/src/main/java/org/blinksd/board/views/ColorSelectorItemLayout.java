package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.LayoutCreator;

import java.util.TreeMap;

@SuppressLint("ViewConstructor")
public class ColorSelectorItemLayout extends LinearLayout {

    private final ImageView img;
    private TreeMap<Integer, Integer> colorList;

    public ColorSelectorItemLayout(Context ctx, int index, TreeMap<Integer, Integer> colors, View.OnClickListener gradientAddColorListener, View.OnClickListener gradientDelColorListener, View.OnClickListener colorSelectorListener) {
        super(ctx);
        setLayoutParams(new LayoutParams(-1, -2));
        img = LayoutCreator.createImageView(ctx);
        int size = (int) getListPreferredItemHeight();
        img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, size, size));
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = size / 4;
        img.setPadding(pad, pad, pad, pad);
        TextView btn = LayoutCreator.createTextView(ctx);
        LayoutParams lp = new LayoutParams(-1, -1, 1);
        btn.setLayoutParams(lp);
        btn.setId(android.R.id.text1);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(0xFFFFFFFF);
        btn.setMinHeight(size);
        addView(img);
        addView(btn);
        setMinimumHeight(size);
        setId(index);
        switch (index) {
            case -1:
                img.setImageResource(android.R.drawable.ic_input_add);
                img.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
                btn.setText(SettingsCategorizedListAdapter.getTranslation(ctx, "image_selector_gradient_add_item"));
                setOnClickListener(gradientAddColorListener);
                return;
            case -2:
                img.setImageResource(android.R.drawable.ic_media_next);
                img.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
                btn.setText(SettingsCategorizedListAdapter.getTranslation(ctx, "image_selector_gradient_change_orientation"));
                setOnClickListener(gradientAddColorListener);
                return;
        }
        colorList = colors;
        int color = 0xFF000000;
        updateColorView(color);
        btn.setText(SettingsCategorizedListAdapter.getTranslation(ctx, "image_selector_gradient_item"));
        ImageView del = LayoutCreator.createImageView(ctx);
        lp = new LayoutParams(size, size, 0);
        del.setLayoutParams(lp);
        del.setScaleType(img.getScaleType());
        del.setImageResource(R.drawable.sym_keyboard_close);
        del.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
        pad = (int) (pad * 1.5f);
        del.setPadding(pad, pad, pad, pad);
        del.setOnClickListener(gradientDelColorListener);
        del.setId(index);
        setTag(color);
        setOnClickListener(colorSelectorListener);
        addView(del, 0);
    }

    private void updateColorView(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(1000);
        img.setImageDrawable(gd);
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        colorList.put(getId(), (int) tag);
        int color = (int) tag;
        updateColorView(color);
    }

    private float getListPreferredItemHeight() {
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        return TypedValue.complexToDimension(value.data, getResources().getDisplayMetrics());
    }

}
