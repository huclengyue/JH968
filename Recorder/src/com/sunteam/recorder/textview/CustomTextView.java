package com.sunteam.recorder.textview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sunteam.recorder.Global;

public class CustomTextView extends TextView {

	public CustomTextView(Context context) {
		super(context);
		if (isInEditMode()) { return; }
		setTextColor(Global.getApp_text_color());
		setTextSize(Global.getApp_text_size());
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) { return; }
		setTextColor(Global.getApp_text_color());
		setTextSize(Global.getApp_text_size());
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) { return; }
		setTextColor(Global.getApp_text_color());
		setTextSize(Global.getApp_text_size());
	}

}
