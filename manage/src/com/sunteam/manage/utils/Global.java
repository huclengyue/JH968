package com.sunteam.manage.utils;

import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class Global {
	public static final String TAG = "zbc";

	public static final int  MAIN_INTERFACE = 1;   // 主界面
	public static final int MENU_INTERFACE = 2;		// menu界面
	
	public static final int MENU_INTERFACE_FLAG = 0x100;		// menu界面
	
	public static int MAX_RANK = 128;// 最大级数

	public static final String ROOT_PATH = Environment.getExternalStorageDirectory().toString();// java.io.File.separator;
	public static final String USER_PATH = "/mnt/extsd"; // 外部 sd卡
	// private final String USB_PATH =
	// Environment.getExternalStorageDirectory().toString();;
	public static final String USB_PATH = "/mnt/usbhost1"; // 优盘
	public static final String ROOT_PATH_FLAG = "/root";
	
	
	public static final String MENU_PATH_EXTSD = "/mnt/extsd"; // 扩展SD卡
	public static final String MENU_PATH_UDISK = "/mnt/usbhost1"; // U盘

	//private boolean gMeunViewFlag = false; // menu 界面标志

	public static final int COPY_ID = 0; // 复制
	public static final int CUT_ID = 1; // 剪切
	public static final int DEl_ID = 2; // 删除
	public static final int PASTE_ID = 3; // 粘贴
	
	public static String gCopyPath_src = null; // 复制分原始目录

	public static String gCopyPath_desk = null; // 复制到目录
	public static String gCopyName = null; // 复制文件名
	
	public static String gPastName = null; // 粘贴文件名

	public static boolean gCopyFlag = false;
	public static boolean gCutFlag = false;
	public static int gtempID = 0;   // 记录选中的select
	
	// 全局保存文件名
	public static ArrayList<String> gFileName = null;
	// 全局保存路径
	public static ArrayList<String> gFilePaths = null;
	public static ArrayList<String> gPath = null;
	public static ArrayList<String> gName = null;
	public static int gPathNum = 0;
	
	public static final int MSG_DEL_OK = 1; // 删除成功提示
	public static final int MSG_PAST_FINSH = 2; // 粘贴完成
	public static final int MSG_PAST_CHECK = 3; // 粘贴路径不对
	public static final int MSG_STRAT_PAST = 4; // 开始粘贴
	
	public static final int MSG_MAIN = 5; // 开始粘贴
	
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
