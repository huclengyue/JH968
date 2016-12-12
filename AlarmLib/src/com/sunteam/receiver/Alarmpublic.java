package com.sunteam.receiver;

import java.util.ArrayList;
import java.util.Calendar;

import com.sunteam.common.utils.SunteamDateUtils;
import com.sunteam.dao.Alarminfo;
import com.sunteam.dao.GetDbInfo;

import android.R.id;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.provider.Settings.Global;
import android.util.Log;

public class Alarmpublic {
	public static final String TAG = "alarmlib";
	Context context;
	public static final String DB_ALARM = Environment.getExternalStorageDirectory() + "/alarm.db";
	public static final String ALARM_TABLE = "alarm";  // 数据库 定时闹钟 table
	public static final String ANNIVERSARY_TABLE = "anniversary";  // 数据库纪念日 table
	public static final String REMIND_TABLE = "remind";  // 数据库行程提醒 table
	public static final String CMD_TABLE = " (_id integer primary key, year integer, month integer,day integer, hour integer, minute integer, filename string, path string, type integer, onoff integer)";  // 数据库行程提醒 table
	
	public static final int MAX_NUM = 8;  // 数据最大记录数
	
	public static final String ALARM_FILE_NAME = "alarm.mp3";
	public static final String ALARM_FILE_PATH = Environment.getExternalStorageDirectory() + "//Alarms//";
	
	public static final int DEF_HOUR = 8;  //  默认小时
	public static final int DEF_MIN = 0;  //  默认分
	public static final int DEF_MONTH = 10;  // 默认月
	public static final int DEF_DAY = 1;  // 默认日
		
	public static final int ALARM_TYPE1 = 0;  // 仅闹一次
	public static final int ALARM_TYPE2 = 1;  // 工作日
	public static final int ALARM_TYPE3 = 2;  // 每天
	
	public static final int DEF_TYPE = ALARM_TYPE1;  // 默 关
	
	public static final int ALARM_OFF = 0;  // 关
	public static final int ALARM_ON = 1;  // 开
	public static final int DEF_ONOFF = ALARM_OFF;  // 默 关
	
	public static final int ALARM_TYPE_ALARM = 1;  // 闹钟
	public static final int ALARM_TYPE_ANN = 2;  // j纪念日
	public static final int ALARM_TYPE_REMIND = 3;  // 行程提醒
	
	public static final int DAY_MAX_SEC = 24*60*60;  // 一天最大的秒
	
	public static void debug(String s) {
		Log.d(TAG, s);
	}
	// 更新 闹钟
	public static void UpateAlarm(Context context){
		
		int alarm_min = 0;
		int ann_min = 0;
		int remind_min = 0;
		int min_sec = 0;
		
		Alarminfo alInfo = new Alarminfo();  // 
		
		alarm_min = GetNearAlarm(context);   // 获取最近的 定时闹钟
		ann_min = GetNearAnn(context);		// 获取最近的 纪念日
		remind_min = GetNearRemind(context);	// 获取最近的 行程提醒
		
		alInfo.onoff = Alarmpublic.ALARM_OFF;

		if(alarm_min > 0){
			min_sec = alarm_min;
		}
		
		if(ann_min > 0){
			if(min_sec > 0){
				if(min_sec > ann_min){
					min_sec = ann_min;
				}
			}else{
				min_sec = ann_min;
			}
		}
		
		if(remind_min > 0){
			if(min_sec > 0){
				if(min_sec > remind_min){
					min_sec = remind_min;
				}
			}else{
				min_sec = remind_min;
			}
		}
		
		// 启动 闹钟
		
		// 进行闹铃注册  
		Intent intent = new Intent(context, Alarm_Receiver.class);  
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0); 
		if(min_sec > 0){ 
			// 过XXs 执行这个闹铃  
			Calendar calendar = Calendar.getInstance();  
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, min_sec);
			//calendar.add(Calendar.SECOND, 5);
	
			Alarmpublic.debug("\r\n calendar.getTimeInMillis() - calendar_now.getTimeInMillis() =  "+ (calendar.getTimeInMillis() - System.currentTimeMillis())/1000);

			//AlarmManager manager = (AlarmManager)getSystemService((String)Context.ALARM_SERVICE);
			AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			//manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
			manager.cancel(sender);
			Alarmpublic.debug("\r\n =================1==========================1\r\n");
			manager.set(/*AlarmManager.RTC_SHUTDOWN_WAKEUP*/4, calendar.getTimeInMillis(), sender);
			
			//manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1*1000, sender);
		}
		else{
			
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);
		}
	}
	// 获取最近的 闹钟
	public static int GetNearAlarm(Context context){
		
		int week = 0;
		Alarminfo tempinfo_later = new Alarminfo();  // 
		Calendar calendar_later = Calendar.getInstance();  // 获取日历
		GetDbInfo dbAlarmInfo = new GetDbInfo( context ); // 打开数据库
		
		ArrayList<Alarminfo> mAlarmAll = new ArrayList<Alarminfo>();
		 
		week = calendar_later.get(Calendar.DAY_OF_WEEK) - 1;  // 获取星期几 0-> 星期天  1--星期一       。。。。。 6--> 星期六 
 		
		int now_sec = SunteamDateUtils.getTimeStampSecond(); // 获取当前时间 秒
		
		// 获取全部数据
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.ALARM_TABLE);
		int later_sec = 0;
		int min_sec = 0;
		int data_sec = 0;
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo_later = mAlarmAll.get(i);
			//Global.debug("\r\nGetNearAlarm === tempinfo_later.hour = "+ tempinfo_later.hour + " tempinfo_later.minute = "+ tempinfo_later.minute);			
			if(tempinfo_later.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
			//	Global.debug("\r\nGetNearAlarm === i = "+ i);	
				
				
				calendar_later.set(Calendar.HOUR_OF_DAY, tempinfo_later.hour);
				calendar_later.set(Calendar.MINUTE, tempinfo_later.minute);
				
				later_sec = (int) (calendar_later.getTimeInMillis()/ 1000);  //秒
				//
				data_sec = later_sec - now_sec;
				Alarmpublic.debug("\r\n[11] data_sec = " + data_sec);
				if(data_sec < 0) // 时间过啦
				{
					if(tempinfo_later.type == Alarmpublic.ALARM_TYPE2)  // 工作日 需要判断是不是 周5，6
					{
						if(week == 5){ // 周5
							data_sec += DAY_MAX_SEC*3;  // 秒
						}
						else if(week == 6){  // 周六
							data_sec += DAY_MAX_SEC*2;  // 秒
						}
						else if(week == 0){ // 周日
							data_sec += DAY_MAX_SEC;  // 秒
						}
						
					}
					else if(tempinfo_later.type == Alarmpublic.ALARM_TYPE3){  // 每天
						data_sec += DAY_MAX_SEC;  // 秒
					}
					else if(tempinfo_later.type == Alarmpublic.ALARM_TYPE1){  // 近闹一次
						data_sec += DAY_MAX_SEC;  // 秒
					}
				}
				
				Alarmpublic.debug("\r\n[22]data_sec = " + data_sec);
				if(data_sec > 0 && (min_sec == 0)) // 第一个需要的闹钟
				{
					min_sec = data_sec;
				//	Alarmpublic.debug("\r\n [***** ] min_sec  = "+ min_sec);
				}
				else if ((data_sec > 0) && (min_sec > 0))  // 找到最接近的
				{
					if(min_sec > data_sec){
						min_sec = data_sec;
					}
				}
				else {	  // 已经过了的
					Alarmpublic.debug("[***** ]data_sec = " + data_sec);
					if(Alarmpublic.ALARM_TYPE1 ==  tempinfo_later.type){ // 仅闹一次 则关闭
						tempinfo_later.onoff = Alarmpublic.ALARM_OFF;
						dbAlarmInfo.update(tempinfo_later, Alarmpublic.ALARM_TABLE);
					}
					else if(Alarmpublic.ALARM_TYPE2 ==  tempinfo_later.type){  // 工作日
						
					}
					else if(Alarmpublic.ALARM_TYPE3 ==  tempinfo_later.type){  // 每天
						
					}
				}
			}
		}
		dbAlarmInfo.closeDb();  // 关闭数据库
				
//		Alarmpublic.debug("\r\n[GetNearAlarm] min_sec == "+ min_sec);
		
		return min_sec;
	}

	// 获取最近的 纪念日
	public static int GetNearAnn(Context context){
		
		Alarminfo tempinfo_later = new Alarminfo();  // 
		Calendar calendar_later = Calendar.getInstance();  // 获取日历
		GetDbInfo dbAlarmInfo = new GetDbInfo( context ); // 打开数据库
		
		ArrayList<Alarminfo> mAlarmAll = new ArrayList<Alarminfo>();
		 
		int now_sec = SunteamDateUtils.getTimeStampSecond(); // 获取当前时间 秒
		
		// 获取全部数据
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.ANNIVERSARY_TABLE);
		int later_sec = 0;
		int min_sec = 0;
		int data_sec = 0;
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo_later = mAlarmAll.get(i);
		//	Global.debug("\r\nGetNearAlarm === tempinfo_later.hour = "+ tempinfo_later.hour + " tempinfo_later.minute = "+ tempinfo_later.minute);
			
			if(tempinfo_later.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
				//Global.debug("\r\nGetNearAlarm === i = "+ i);	
				
				calendar_later.set(Calendar.MONTH, tempinfo_later.month -1);
				calendar_later.set(Calendar.DAY_OF_MONTH , tempinfo_later.day);
				calendar_later.set(Calendar.HOUR_OF_DAY, tempinfo_later.hour);
				calendar_later.set(Calendar.MINUTE, tempinfo_later.minute);
				
				later_sec = (int) (calendar_later.getTimeInMillis()/ 1000);
				
				data_sec = later_sec - now_sec;
				Alarmpublic.debug("data_sec = " + data_sec);
				if(data_sec > 0 && (min_sec == 0))
				{
					min_sec = data_sec;
					//Alarmpublic.debug("\r\n [***** ] min_sec  = "+ min_sec);
				}
				else if ((data_sec > 0) && (min_sec > 0))
				{
					if(min_sec > data_sec){
						min_sec = data_sec;
					}
				}
				else {
					/*
					if(Alarmpublic.ALARM_TYPE1 ==  tempinfo_later.type){ // 仅闹一次 则关闭
						tempinfo_later.onoff = Alarmpublic.ALARM_OFF;
						dbAlarmInfo.update(tempinfo_later, Alarmpublic.ANNIVERSARY_TABLE);
					}
					*/
				}
			}
		}
		dbAlarmInfo.closeDb();  // 关闭数据库
				
		//Alarmpublic.debug("\r\n[GetNearAnn] min_sec == "+ min_sec);
		
		return min_sec;
	}
	// 获取最近的 行程提醒
	public static int GetNearRemind(Context context){
		
		Alarminfo tempinfo_later = new Alarminfo();  // 
		Calendar calendar_later = Calendar.getInstance();  // 获取日历
		GetDbInfo dbAlarmInfo = new GetDbInfo( context ); // 打开数据库
		
		ArrayList<Alarminfo> mAlarmAll = new ArrayList<Alarminfo>();
		 
		int now_sec = SunteamDateUtils.getTimeStampSecond(); // 获取当前时间 秒
		
		// 获取全部数据
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.REMIND_TABLE);
		int later_sec = 0;
		int min_sec = 0;
		int data_sec = 0;
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo_later = mAlarmAll.get(i);
//			Global.debug("\r\nGetNearAlarm === tempinfo_later.hour = "+ tempinfo_later.hour + " tempinfo_later.minute = "+ tempinfo_later.minute);
			
			if(tempinfo_later.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
				//Global.debug("\r\nGetNearAlarm === i = "+ i);	
				calendar_later.set(Calendar.YEAR, tempinfo_later.year);
				calendar_later.set(Calendar.MONTH, tempinfo_later.month-1);
				calendar_later.set(Calendar.DAY_OF_MONTH , tempinfo_later.day);
				calendar_later.set(Calendar.HOUR_OF_DAY, tempinfo_later.hour);
				calendar_later.set(Calendar.MINUTE, tempinfo_later.minute);
				
				later_sec = (int) (calendar_later.getTimeInMillis()/ 1000);
				//Global.debug("later_sec = " + later_sec + " now_sec = " + now_sec);
				data_sec = later_sec - now_sec;
				//Alarmpublic.debug("later_sec == " + data_sec);
				if(data_sec > 0 && (min_sec == 0))
				{
					min_sec = data_sec;
					Alarmpublic.debug("\r\n [***** ] min_sec  = "+ min_sec);
				}
				else if ((data_sec > 0) && (min_sec > 0))
				{
					if(min_sec > data_sec){
						min_sec = data_sec;
					}
				}
				else {
					if(Alarmpublic.ALARM_TYPE1 ==  tempinfo_later.type){ // 仅闹一次 则关闭
						tempinfo_later.onoff = Alarmpublic.ALARM_OFF;
						//dbAlarmInfo.update(tempinfo_later, Alarmpublic.REMIND_TABLE);
					}
				}
			}
		}
		dbAlarmInfo.closeDb();  // 关闭数据库
				
		//Alarmpublic.debug("\r\n[GetNearRemind] min_sec == "+ min_sec);
		
		return min_sec;		
	}
	
	
}
