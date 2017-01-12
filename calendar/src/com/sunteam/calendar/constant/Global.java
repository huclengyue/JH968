package com.sunteam.calendar.constant;


import android.os.Environment;
import android.util.Log;

public class Global {
	public static final String TAG = "zbc";
	
	public static int REMIND_CALL_MAIN = 1;  //主界面 进入
	public static int REMIND_CALL_MENU = 2;  //主界面  查看界面
	public static int REMIND_CALL_ADD_MENU = 3;  //主界面  增加提醒
	
	public static int REMIND_FLAG_ID = 0x300;  //菜单
	public static int REMIND_ADD_FLAG_ID = 0x301;  //菜单
	
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
	
	public static String ALARM_FILE_NAME = null;//getResources().getsting(R.string.remind_noFile);
	public static String TIXING_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/提醒";
	
	public static final int MSG_DEL_ALL = 1;  // 关delall_Remind 提示
	public static final int MSG_NO_REMIND = 2;  // 无提醒 _Remind 提示
	public static final int MSG_NO_REMIND_TOMAIN = 3;  // 无提醒 _Remind返回主菜单 提示
	public static final int MSG_TOMAIN = 15;  //返回主菜单 提示
	public static final int MSG_DEL = 4;  // 删除提醒
	public static final int MSG_ONRESUM = 5;  // 刷新界面
	
	public static final int MSG_TO_MAIN = 6;  // 刷新界面
	
	public static final int MSG_SETOK = 7;  // 设置成功
	
	public static final int MSG_START_RECORD = 8;  // 开始录音
	public static final int MSG_START_RECORD_NO = 9;  // 取消录音
	public static final int MSG_SAVE_EXIT = 10;  // 保存退出
	
	public static final int MSG_TIME_AFTER_YES = 11;  // 保存退出
	public static final int MSG_TIME_AFTER_NO = 12;  // 保存退出
	
	public static final int MSG_SAVE_UPDATE_YES = 13;  // 保存退出
	public static final int MSG_SAVE_UPDATE_NO = 14;  // 保存退出
	
	public static final int MSG_FINISH = 16;  // 刷新界面
	public static final int MSG_TIME_AFTER = 17;  // 保存退出
	
	public static final int MSG_DEL_OK = 18;  // 保存退出
	
	public static final int MSG_MSG_NO_REMIND_TOMAIN_OK = 19;  // 保存退出
	
	public static void debug(String s) {
		Log.d(TAG, s);
	}

}
