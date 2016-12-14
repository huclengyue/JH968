package com.sunteam.recorder;

import java.util.HashMap;

import com.sunteam.common.tts.TtsCompletedListener;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.recorder.dialog.CustomPromptDialog;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class Global {
	private static SunteamApplication gApp;
	private static int backgroundColor;
	private static int foregroundColor;
	private static int app_text_color;
	private static int app_text_size;
	private HashMap<Object, Object> map;
	public static int LONG_PRESS_DOWN = 0;
	public static int LONG_PRESS_UP = 1;
	public static String storagePath;

	public static final String TAG = "zbc";
	
	public static void debug(String s) {
		Log.d(TAG, s);
	}
	
	public static SunteamApplication getApplication() {
		return gApp;
	}

	public static void setApplication(SunteamApplication app) {
		setSavePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + app.getResources().getString(R.string.storage));
		gApp = app;
	}

	public static void setSavePath(String path) {
		storagePath = path;
	}

	public static int getBackgroundColor() {
		return backgroundColor;
	}

	public static void setBackgroundColor(int color) {
		backgroundColor = color;
	}

	public static int getForegroundColor() {
		return foregroundColor;
	}

	public static void setForegroundColor(int color) {
		foregroundColor = color;
	}

	public static int getApp_text_color() {
		return app_text_color;
	}

	public static void setApp_text_color(int app_text_color) {
		Global.app_text_color = app_text_color;
	}

	public static int getApp_text_size() {
		return app_text_size;
	}

	public static void setApp_text_size(int app_text_size) {
		Global.app_text_size = app_text_size;
	}

	public Object getValue(String key) {
		return map.get(key);
	}

	public void setValue(String key, Object value) {
		map.put(key, value);
	}

	/**
	 * 显示提示
	 * 
	 * @param context
	 *            上下文
	 * @param StringID
	 *            现实的资源ID
	 * @param handler
	 *            处理消息的handler
	 * @param what
	 *            消息类别
	 */

	public static void showToast(Context context, int StringID, final Handler handler, final int what) {
		String hint = context.getResources().getString(StringID);
		final CustomPromptDialog hintDialog = new CustomPromptDialog(context, hint);

		TtsUtils.getInstance().setCompletedListener(new TtsCompletedListener() {

			@Override
			public void onCompleted(String arg0) {
				hintDialog.dismiss();
				if (handler != null) {
					handler.sendEmptyMessage(what);
					TtsUtils.getInstance().setCompletedListener(null);
				}
			}
		});
		TtsUtils.getInstance().speak(hint);
		try {
			hintDialog.show();
		} catch (Exception e) {
		}
	}

}
