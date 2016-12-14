package com.sunteam.recorder.textview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sunteam.recorder.Global;

public class CustomDialogTextView extends TextView {

	public CustomDialogTextView(Context context) {
		super(context);
		setTextColor(Global.getBackgroundColor());
		setTextSize(Global.getApp_text_size());
	}

	public CustomDialogTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTextColor(Global.getBackgroundColor());
		setTextSize(Global.getApp_text_size());
	}

	public CustomDialogTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTextColor(Global.getBackgroundColor());
		setTextSize(Global.getApp_text_size());
	}

}
