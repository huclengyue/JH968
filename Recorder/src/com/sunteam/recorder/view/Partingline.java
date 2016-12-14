package com.sunteam.recorder.view;
import com.sunteam.recorder.Global;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * 分割线
 * @author Administrator
 *
 */
public class Partingline extends View {

	public Partingline(Context context) {
		super(context);
		if (isInEditMode()) { return; }
		setBackgroundColor(Global.getApp_text_color());
	}

	public Partingline(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) { return; }
		setBackgroundColor(Global.getApp_text_color());
	}

	public Partingline(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) { return; }
		setBackgroundColor(Global.getApp_text_color());
	}

}
