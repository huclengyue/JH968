package com.sunteam.alarm.utils;

import android.content.Context;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class Global {
	public static final String TAG = "zbc";

	public static final int ALARM_INTERFACE = 1; // 定时闹钟界面
	public static final int ANNIVERSARY_INTERFACE = 2; // 纪念日界面
	public static final int COUNTDOWN_INTERFACE = 3; // 倒计时界面

	public static final int ALARM_INFO_INTERFACE = 4; // 定时闹钟详情界面
	public static final int ANNIVERSARY_INFO_INTERFACE = 5; // 纪念日详情界面
	public static final int COUNTDOWN_INFO_INTERFACE = 6; // 倒计时界面

	public static final int FLAG_CODE = 0x100; // 界面跳转标志
	public static final int FLAG_CODE_SET_LIST = 0x101; // 界面跳转标志 列表模式

	public static final int ALARM_SET_TIME = 0; // 时间
	public static final int ALARM_SET_MUSIC = 1; // 音乐
	public static final int ALARM_SET_TYPE = 2; // 类型
	public static final int ALARM_SET_ONOFF = 3; // 开关

	public static final int ANNIVERSARY_SET_TIME = 1; // 时间
	public static final int ANNIVERSARY_SET_MUSIC = 2; // 音乐
	public static final int ANNIVERSARY_SET_DATE = 0; // 日期
	public static final int ANNIVERSARY_SET_ONOFF = 3; // 开关

	public static final int ALARM_TYPE_ALARM = 1; // 闹钟
	public static final int ALARM_TYPE_ANN = 2; // j纪念日
	public static final int ALARM_TYPE_REMIND = 3; // 行程提醒
	public static final int COUNTDOWN_MAX_TIME = 6 * 60; // 行程提醒

	public static int COUNT_DOWN_ID0 = 0;
	public static int COUNT_DOWN_ID1 = 1;
	public static int COUNT_DOWN_ID2 = 2;
	public static int COUNT_DOWN_ID3 = 3;
	public static int COUNT_DOWN_ID4 = 4;

	public static int MSG_COUNTDOWN_ERROR = 1;
	public static int MSG_COUNTDOWN_END = 2;

	public static int MSG_SETTING_BACK = 3; // 设置后返回
	public static int MSG_SETTING_ASK_TIMEBACK = 4; // 设置后返回
	public static int MSG_SETTING_FINISH = 5; // 设置后返回

	public static int MSG_SETTING_ASK_DATEBACK = 4; // 设置后返回

	public static int MSG_COUNTDOWN_PLAYMUSIC = 6;   //  倒计时结束 播放音乐 
	
	public static int TIME_LEN0 = 30 * 60;
	public static int TIME_LEN1 = 60 * 60;
	public static int TIME_LEN2 = 90 * 60;
	public static int TIME_LEN3 = 120 * 60;
	
	public static String FILE_TIMEOUT = "timeout.mp3";

	// public static final String ALARM_FILE_PATH =
	// Environment.getExternalStorageDirectory() + "//Alarms//" +
	// ALARM_FILE_NAME;
	// public static final String ALARM_FILE_PATH1 =
	// Environment.getExternalStorageDirectory() + "/"+
	// getResources().getString(R.string.alarm_title) + "/";

	public static void debug(String s) {
		Log.d(TAG, s);
	}

	public static WakeLock mWakeLock; // 禁止休眠

	@SuppressWarnings("deprecation")
	public static void acquireWakeLock(Context context) {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, context.getClass().getName());
			mWakeLock.acquire();
		}
	}

	public static void releaseWakeLock() {
		if (null != mWakeLock && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

}
