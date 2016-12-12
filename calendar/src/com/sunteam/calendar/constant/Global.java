package com.sunteam.calendar.constant;


import android.util.Log;

public class Global {
	public static final String TAG = "zbc";
	
	public static int REMIND_CALL_MAIN = 1;  //主界面 进入
	
	public static int REMIND_CALL_MENU = 2;  //主界面  查看界面
	
	public static int REMIND_FLAG_ID = 0x300;  //菜单
	
	public static int MENU_FLAG_ID = 0x200;  //菜单
	public static int FLAG_RECORD_ID = 0x100;  // 录音界面
	public static int FLAG_TIME_ID = 0x101;   // 时间修改界面
	public static int FLAG_ONOFF_ID = 0x102;   // 时间修改界面
	
	public static int MAX_HOUR = 23;  // 选中 分
	public static int MAX_MINUTE = 59;  // 选中 分
	
	public static int MAX_YEAR = 2049;
	public static int MIN_YEAR = 1901;
	
	public static int MAX_MONTH = 12;  // 月份最大值	
	public static int MIN_MONTH = 1;  // 月份最大值	
	
	public static final int ALARM_OFF = 1;  // 关
	public static final int ALARM_ON = 0;  // 开
	public static final int DEF_ONOFF = ALARM_ON;  // 默 关
	
	public static  String ALARM_FILE_NAME = null;//getResources().getsting(R.string.remind_noFile);
	
	public static void debug(String s) {
		Log.d(TAG, s);
	}

}
