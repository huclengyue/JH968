package com.sunteam.fmradio.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Global {
	public static final String TAG = "zbc";

	   /* GUI message codes. */
	public static final int GUI_UPDATE_MSG_SIGNAL_STATUS = 1;
	public static final int GUI_UPDATE_MSG_MUTE_STATUS = 2;
	public static final int GUI_UPDATE_MSG_FREQ_STATUS = 3;
	public static final int GUI_UPDATE_MSG_WORLD_STATUS = 4;
	public static final int GUI_UPDATE_MSG_RDS_STATUS = 5;
	public static final int GUI_UPDATE_MSG_AF_STATUS = 6;
	public static final int GUI_UPDATE_MSG_RDS_DATA = 7;
    
	public static final int GUI_GET_NEXT_CHANEL = 8;
	public static final int GUI_UPDTAE_CHANEL = 9;
	public static final int GUI_UPDTAE_LIST = 10;
    
    
	public static final int SIGNAL_CHECK_PENDING_EVENTS = 20;
	public static final int NFL_TIMER_EVENT = 21;

	public static final int MENU_CH_SET = 1;
    public static final int MENU_CH_CLEAR = 2;
    public static final int MENU_CH_CANCEL = 3;

    /* Default frequency. */
    public static final int DEFAULT_FREQUENCY = 9050;  // 默认频道
    
    // add by zhd@20161027
    public static final int DEFAULT_RADIO_FREQUENCY = 9050;  // 默认频道： 100KHz为单位
    public final int freqStep = 10; // 步进频率为 100KHz
    public final static float fmScale = (float) 100.0; // 把10kHz单位转换成MHz单位

	
	public static final String DB_PATH = Environment.getExternalStorageDirectory() + "//fm.db"; // 数据库路径
	public static final String FM_LIST = "fm";   // 最近播放 
	public static Context mContext;
	public static Handler mHandler;
	public static int MENU_FLAG = 0x100;
	
	public static int SEARCH_ALL_ID	=	0 ;  // 搜索电台
	public static int AUDIO_APEAK_ID	=	1 ;  // 外放开关
	public static int SAVE_CHANEL_ID	=	2 ;  // 保存电台
	public static int DEL_CHANEL_ID	=	3 ;  // 删除电台
	public static int DELALL_CHANEL_ID	=	4 ;  // 删除所有电台
	public static int RECORD_CHANEL_ID	=	5 ;  // 内录电台
	
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
