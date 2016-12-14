package com.sunteam.recorder.dialog;

import com.sunteam.common.tts.TtsUtils;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class CustomPromptDialog extends Dialog {

	private String hint;
	private Context context;

	public CustomPromptDialog(Context context, String hint) {
		super(context, R.style.dialog);
		this.hint = hint;
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.hint_dialog, null);
		view.setBackgroundColor(Global.getApp_text_color());
		setContentView(view);

		TextView tvHint = (TextView) view.findViewById(R.id.hint);
		tvHint.setText(hint);

		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics d = context.getResources().getDisplayMetrics();// 获取屏幕宽、高用
		lp.width = (int) (d.widthPixels * 0.9);
		// lp.height = (int)(d.widthPixels*0.6);
		dialogWindow.setAttributes(lp);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		TtsUtils.getInstance().stop();
		// MyLog.e("promptdialog", "code:"+keyCode+",action"+event.getAction());
		this.dismiss();
		return false;
		// return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
		// return super.onKeyUp(keyCode, event);
	}
}
