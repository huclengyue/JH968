package com.sunteam.fmradio.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Global {
	public static final String TAG = "zbc";

	public static final String DB_PATH = Environment.getExternalStorageDirectory() + "//fm.db"; // 数据库路径
	public static final String FM_LIST = "fm";   // 最近播放 
	public static Context mContext;
	public static Handler mHandler;
	public static int MENU_FLAG = 0x100;
	
	public static final String MHZ = "Mhz";   // 最近播放

	public static Context getContext() {
		return mContext;
	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static Handler getHandler() {
		return mHandler;
	}

	public static void setHandler(Handler h) {
		mHandler = h;
	}

	public static void sendMessage(int msgType, Object obj) {
		if (null != mHandler) {
			Message m = mHandler.obtainMessage(msgType, 0, 0, obj);
			mHandler.sendMessage(m);
		}
	}

	public static void debug(String s) {
		Log.d(TAG, s);
	}
}
