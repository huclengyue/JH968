package com.sunteam.massage.utils;

import android.util.Log;

public class Global {
	public static int MIN_YEAR = 1970;  // 最小1970
	public static int MAX_YEAR = 2099;  // 最大2099
	public static int MIN_MONTH = 1;  // 最小月份
	public static int MAX_MONTH = 12;  // 最大月份

	public static int MAX_HOUR = 24;  // 最大小时
	
	// 焦点标志
	public static int AT_YEAR = 0;  // 焦点在年
	public static int AT_MONTH = 1;	// 焦点在月
	public static int AT_DAY = 2;	// 焦点在日
	public static int MAX_MONEY = 9999;	// 最大金额数
	public static int FLAG_CODE = 0x505;
	
	public static int DATE_SET = 0; // 日期设置
	public static int FORWORK_SET = 1; // 排钟
	public static int OVERWORK_SET = 2; // 点钟
	public static int MONEY_SET = 3; // 金额
	
	
	public static final int MAX_USER_NUM = 10; // 用户最大数
	public static final int USER_PATH = 0;// 内存数据
	
	public static final String TAG = "zbc";

	public static void debug(String s) {
		Log.d(TAG, s);
	}
}
