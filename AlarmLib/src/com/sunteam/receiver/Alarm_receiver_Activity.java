package com.sunteam.receiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import com.sunteam.alarmlib.R;
import com.sunteam.common.menu.BaseActivity;
import com.sunteam.common.menu.MenuGlobal;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.CommonUtils;
//import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.Tools;
import com.sunteam.dao.Alarminfo;
import com.sunteam.dao.GetDbInfo;
import com.sunteam.player.MyPlayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class Alarm_receiver_Activity extends BaseActivity implements MyPlayer.OnStateChangedListener{

	private MyPlayer myPlayer = null;
	private WakeLock mWakeLock; // 禁止休眠
	private TextView mTvTitle = null; // 标题栏
	private View mLine = null; // 分割线
	
	private TextView mTv_Year1 = null; // 标题栏
	private TextView mTv_Year2 = null; // 标题栏
	
	private TextView mTv_Date1 = null; // 标题栏
	private TextView mTv_Date2 = null; // 标题栏
	private TextView mTv_Date3 = null; // 标题栏
	private TextView mTv_Time1 = null; // 标题栏
	private TextView mTv_Time2 = null; // 标题栏
	private TextView mTv_Time3 = null; // 标题栏
	private TextView mFilename = null; // 标题栏
	
	private static boolean isClose = false;
	private String gFilename = null; // 标题栏
	private int gyear = 0;
	private int gmonth = 0;
	private int gday = 0;
	
	private int gFlag = 0;
	Alarminfo alarminfo = null;
	int alarm_flag = 0;
 //   private MyPlayer mInstance = null;
	Alarm_receiver_Activity gthis ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_receiver_activity);
//		Alarmpublic.debug("\r\n Alarm_receiver_Activity ========[onCreate]============this ="+ this);
		Tools mTools = new Tools(this);
		
		Intent intent = getIntent();	//获取Intent
		//Bundle bundle = intent.getExtras();	//获取 Bundle
//		Alarmpublic.debug("\r\n Alarm_receiver_Activity =========2222===========\r\n");
		gFlag = intent.getIntExtra("FLAG", gFlag);  // 获取进入标志
//		Alarmpublic.debug("\r\n Alarm_receiver_Activity ===========3333=========gFlag ="+ gFlag);
				
		mTvTitle = (TextView) this.findViewById(R.id.title); // 标题栏
		mTv_Year1 = (TextView) this.findViewById(R.id.Year_tv1); // 年
		mTv_Year2 = (TextView) this.findViewById(R.id.Year_tv2); // -
		mTv_Date1 = (TextView) this.findViewById(R.id.date_tv1); // 月
		mTv_Date2 = (TextView) this.findViewById(R.id.date_tv2); // -
		mTv_Date3 = (TextView) this.findViewById(R.id.date_tv3); // 日
		
		mTv_Time1 = (TextView) this.findViewById(R.id.time_tv1); // 标题栏
		mTv_Time2 = (TextView) this.findViewById(R.id.time_tv2); // 标题栏
		mTv_Time3 = (TextView) this.findViewById(R.id.time_tv3); // 标题栏
		mFilename = (TextView) this.findViewById(R.id.filname); // 标题栏
		
		//mDataInfo = (TextView) this.findViewById(R.id.dateinfo);
		mLine = this.findViewById(R.id.line); // 分割线
		this.getWindow().setBackgroundDrawable(new ColorDrawable(mTools.getBackgroundColor()));

		mTvTitle.setTextColor(mTools.getFontColor()); // 设置title的文字颜色	
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色
		mTv_Date1.setTextColor(mTools.getFontColor());
		mTv_Date2.setTextColor(mTools.getFontColor());
		mTv_Date3.setTextColor(mTools.getFontColor());
		
		mTv_Time1.setTextColor(mTools.getFontColor());
		mTv_Time2.setTextColor(mTools.getFontColor());
		mTv_Time3.setTextColor(mTools.getFontColor());
		
		mTv_Year1.setTextColor(mTools.getFontColor());
		mTv_Year2.setTextColor(mTools.getFontColor());

		mFilename.setTextColor(mTools.getFontColor());

		mTvTitle.setText(getResources().getString(R.string.alarm_title));
		
		// 设置显示属性
		int fontSize = mTools.getFontSize();
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // 设置title字号
		mTvTitle.setHeight(mTools.convertSpToPixel(fontSize));	
		//int flag = getAlarmType();
		
		alarminfo = new Alarminfo();  //
		
		alarminfo = getAlarmData();
//		Alarmpublic.debug("\r\n[] alarminfo   ===== " + alarminfo);
		if(alarminfo == null){
			
			Alarmpublic.debug("  没有找到闹钟=================\r\n");
			
			Alarmpublic.UpateAlarm(this);
			Alarmpublic.debug("  没有找到闹钟========呜呜呜=========\r\n");
			finish();
			
			return ;
		}
		
/*		if(myPlayer != null){
			myPlayer.stopPlayback();
		}*/
		
		myPlayer = MyPlayer.getInstance(this,mHandler);
		myPlayer.setOnStateChangedListener(this);
		gFilename = alarminfo.path;   // 获取文件名
		File mFile = new File(gFilename);
		if(mFile.exists()){   // 文件不存在
			Alarmpublic.debug("\r\n[11111111111] gFilename ======="+ gFilename);
			myPlayer.startPlayback(myPlayer.playProgress(), gFilename, true);	
		}
		else{
			Alarmpublic.debug("\r\n[22222222] gFilename =======");
			
			Alarmpublic.debug("\r\n[33333] gFilename =======");
			//AssetFileDescriptor fd = getResources().openRawResourceFd();
			//gFilename = 
			//Alarmpublic.debug("\r\n[4444] gFilename =====fileDescriptor.getFileDescriptor()==" + fd.getFileDescriptor());
			myPlayer.startPlayback2(this, R.raw.alarm, true);
			
			Alarmpublic.debug("\r\n[33333] gFilename =======");
		}
		
		alarm_flag = getAlarmType();
		
		// 获取时间
		Calendar calendar = Calendar.getInstance();  // 获取日历
		
		gyear = calendar.get(Calendar.YEAR);
		gmonth = calendar.get(Calendar.MONTH) +1;  // 月份从0开始，需要+1
		gday = calendar.get(Calendar.DAY_OF_MONTH);
				
		showallinfo();
		
		//Alarmpublic.debug("\r\n ================alarm_flag ============ "+ alarm_flag);
		MenuGlobal.debug("[alarmlib-Alarm_receiver_Activity][onCreate], this = " + this);
		
		Alarmpublic.UpateAlarm(this);
//		total = myPlayer.fileDuration();

	}
	
	
	private void showallinfo(){
		if(Alarmpublic.ALARM_TYPE_ALARM == alarm_flag){
			mTv_Year1.setText("" + gyear);
			mTv_Year2.setText("-");
			if(gmonth < 10){
				mTv_Date1.setText("0" + gmonth);
			}
			else{
				mTv_Date1.setText(gmonth);
			}
			
			mTv_Date2.setText("-");
			if(gday < 10){
				mTv_Date3.setText("0" + gday);	
			}
			else{
				mTv_Date3.setText("" + gday);
			}
			if(alarminfo.hour < 10){
				mTv_Time1.setText("0" + alarminfo.hour);
			}
			else{
				mTv_Time1.setText("" + alarminfo.hour);
			}
			mTv_Time2.setText(":");
			if(alarminfo.minute < 10){
				mTv_Time3.setText("0" + alarminfo.minute);
			}
			else{
				mTv_Time3.setText("" + alarminfo.minute);
			}
			mTvTitle.setText(getResources().getString(R.string.alarm_title));
			//TtsUtils.getInstance().speak(getResources().getString(R.string.alarm_title));
		}
		else if (Alarmpublic.ALARM_TYPE_ANN == alarm_flag){
			mTv_Year1.setText("" + gyear);
			mTv_Year2.setText("-");
			
			if(alarminfo.month < 10){
				mTv_Date1.setText("0"+alarminfo.month);
			}
			else{
				mTv_Date1.setText(""+alarminfo.month);
			}
			mTv_Date2.setText("-");
			if(alarminfo.day < 10){
				mTv_Date3.setText("0"+alarminfo.day);
			}
			else{
				mTv_Date3.setText(""+alarminfo.day);
			}
			if(alarminfo.hour < 10){
				mTv_Time1.setText("0" + alarminfo.hour);
			}
			else{
				mTv_Time1.setText("" + alarminfo.hour);
			}
			mTv_Time2.setText(":");
			if(alarminfo.minute < 10){
				mTv_Time3.setText("0" + alarminfo.minute);
			}
			else{
				mTv_Time3.setText("" + alarminfo.minute);
			}
			if(Alarmpublic.ALARM_TYPE_ANN == alarm_flag){
				mTvTitle.setText(getResources().getString(R.string.annive_title));
				//TtsUtils.getInstance().speak(getResources().getString(R.string.annive_title));
			}
			else{
				mTvTitle.setText(getResources().getString(R.string.remid_title));
				//TtsUtils.getInstance().speak(getResources().getString(R.string.remid_title));
			}
		}
		else if( Alarmpublic.ALARM_TYPE_REMIND == alarm_flag){
			mTv_Year1.setText("" + alarminfo.year);
			mTv_Year2.setText("-");
			if(alarminfo.month < 10){
				mTv_Date1.setText("0"+alarminfo.month);
			}
			else{
				mTv_Date1.setText(""+alarminfo.month);
			}
			mTv_Date2.setText("-");
			if(alarminfo.day < 10){
				mTv_Date3.setText("0"+alarminfo.day);
			}
			else{
				mTv_Date3.setText(""+alarminfo.day);
			}
			if(alarminfo.hour < 10){
				mTv_Time1.setText("0" + alarminfo.hour);
			}
			else{
				mTv_Time1.setText("" + alarminfo.hour);
			}
			mTv_Time2.setText(":");
			if(alarminfo.minute < 10){
				mTv_Time3.setText("0" + alarminfo.minute);
			}
			else{
				mTv_Time3.setText("" + alarminfo.minute);
			}
			if(Alarmpublic.ALARM_TYPE_ANN == alarm_flag){
				mTvTitle.setText(getResources().getString(R.string.annive_title));
				//TtsUtils.getInstance().speak(getResources().getString(R.string.annive_title));
			}
			else{
				mTvTitle.setText(getResources().getString(R.string.remid_title));
				//TtsUtils.getInstance().speak(getResources().getString(R.string.remid_title));
			}
		}
		mFilename.setText(alarminfo.filename);
		//Alarmpublic.debug("\r\n alarm_flag ====999999" );
	}
	
	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		if (!CommonUtils.isAppOnForeground(this)) { // 如果是自己启动自己，则不必销毁TtsUtils实例!
			TtsUtils.getInstance().destroy();
		}
	}
	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();
		//MenuGlobal.debug("[alarmlib-Alarm_receiver_Activity][onResume], this = " + this);

		//Alarmpublic.debug("\r\n [onResume] =========================== this= "+ this);
		acquireWakeLock(this);
		
		showallinfo();
		gthis = this;
		
	}


	@Override
	public void onPanelClosed(int featureId, Menu menu) {
		// TODO 自动生成的方法存根
		super.onPanelClosed(featureId, menu);
		releaseWakeLock();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
//			Alarmpublic.debug(" \r\n mHandler  ={}====1110000000000000");
			
			if(msg.what == 1){   // 音乐播放结束消息
				Intent intent = new Intent();
				myPlayer.stopPlayback();
				setResult(RESULT_OK, intent);
				finish();
				Alarmpublic.debug(" \r\n 音频播放结束  mHandler  ={}====111");
				
			}else if(msg.what == 0){
				myPlayer.startPlayback(0, gFilename, false);
				Alarmpublic.debug("mHandler  ={}====0000");
			}else if(msg.what == 2){
				mHandler.postDelayed(finishActivity, 2000);
				Alarmpublic.debug("mHandler  ={}====2222");
			}else if(msg.what == 3){
				mHandler.postDelayed(finishActivityDelayed, 2000);
				Alarmpublic.debug("mHandler  ={}====3333");
			}
			super.handleMessage(msg);
		}
		
	};	
	
	private void closeActivity(){
		 Log.e("isClose", isClose?"true":"false");
		 if(isClose){
		//	 Global.showToast(this, R.string.speed2end, mHandler,3);
		 }else{
			 mHandler.postDelayed(finishActivity, 2000);	//1s���ٴ�ִ�д˷���
		 }
	 }
	
	Runnable finishActivity = new Runnable() {
		
		@Override
		public void run() {
			closeActivity();		
		}
	};
	
	Runnable finishActivityDelayed = new Runnable() {
		
		@Override
		public void run() {
			mHandler.sendEmptyMessage(1);
		}
	};
	
	
	 // 获取是哪个闹钟
	private int getAlarmType() {

		Alarminfo tempinfo = new Alarminfo();  // 
		GetDbInfo dbAlarmInfo = new GetDbInfo( this ); // 打开数据库
		
		Calendar calendar = Calendar.getInstance();  // 获取当前日历
		int gyear = calendar.get(Calendar.YEAR);
		int gmonth = calendar.get(Calendar.MONTH) +1;  // 月份从0开始，需要+1
		int gday = calendar.get(Calendar.DAY_OF_MONTH);
		int gHour = calendar.get(Calendar.HOUR_OF_DAY);
		int gmin = calendar.get(Calendar.MINUTE);
		
		ArrayList<Alarminfo> mAlarmAll = new ArrayList<Alarminfo>();
		
		// 获取全部数据
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.ALARM_TABLE);
	
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo = mAlarmAll.get(i);
			//Global.debug("\r\nGetNearAlarm === tempinfo_later.hour = "+ tempinfo_later.hour + " tempinfo_later.minute = "+ tempinfo_later.minute);
			
			if(tempinfo.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
			//	Global.debug("\r\nGetNearAlarm === i = "+ i);	
				if((gFlag == Alarmpublic.BOOT_FLAG) && (gHour == tempinfo.hour) && ((gmin - tempinfo.minute) == 0 ||((gmin - tempinfo.minute) == 1))){
					dbAlarmInfo.closeDb();  // 关闭数据库
					return Alarmpublic.ALARM_TYPE_ALARM;
				}
				else if ((gFlag == Alarmpublic.NORMAL_FLAG) && (gHour == tempinfo.hour) && (gmin == tempinfo.minute)){
					dbAlarmInfo.closeDb();  // 关闭数据库
			//		Alarmpublic.debug("找到数据   === 闹钟===2===\r\n");
					return Alarmpublic.ALARM_TYPE_ALARM;
				}
			}
		}
		
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.ANNIVERSARY_TABLE);
		
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo = mAlarmAll.get(i);
			if(tempinfo.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
			//	Global.debug("\r\nGetNearAlarm === i = "+ i);	
				if((gHour == tempinfo.hour) && ((gmin - tempinfo.minute) == 0 ||((gmin - tempinfo.minute) == 1)) &&
						(gday == tempinfo.day) && (gmonth == tempinfo.month)){
					dbAlarmInfo.closeDb();  // 关闭数据库
					return Alarmpublic.ALARM_TYPE_ANN;
				}
			}
		}
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.REMIND_TABLE);
		
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo = mAlarmAll.get(i);
			if(tempinfo.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
			//	Global.debug("\r\nGetNearAlarm === i = "+ i);	
				if((gHour == tempinfo.hour) && ((gmin - tempinfo.minute) == 0 ||((gmin - tempinfo.minute) == 1)) &&
						(gday == tempinfo.day) && (gmonth == tempinfo.month)&&(gyear == tempinfo.year)){
					dbAlarmInfo.closeDb();  // 关闭数据库
					return Alarmpublic.ALARM_TYPE_REMIND;
				}
			}
		}
		
		dbAlarmInfo.closeDb();  // 关闭数据库
		// 启动 闹钟
		return 0;
	}
	
	 // 获取是哪个闹钟
	private Alarminfo getAlarmData() {

		Alarminfo tempinfo = new Alarminfo();  // 
		GetDbInfo dbAlarmInfo = new GetDbInfo( this ); // 打开数据库
		
		Calendar calendar = Calendar.getInstance();  // 获取当前日历
		int gyear = calendar.get(Calendar.YEAR);
		int gmonth = calendar.get(Calendar.MONTH) +1;  // 月份从0开始，需要+1
		int gday = calendar.get(Calendar.DAY_OF_MONTH);
		int gHour = calendar.get(Calendar.HOUR_OF_DAY);
		int gmin = calendar.get(Calendar.MINUTE);
		
		ArrayList<Alarminfo> mAlarmAll = new ArrayList<Alarminfo>();
		
		// 获取全部数据
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.ALARM_TABLE);   // 定时闹钟
	
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo = mAlarmAll.get(i);
		//	Alarmpublic.debug("\r\nGetNearAlarm === tempinfo_later.hour = "+ tempinfo.hour + " tempinfo.minute = "+ tempinfo.minute);
			
			if(tempinfo.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
			//	Global.debug("\r\nGetNearAlarm === i = "+ i);	
				
				if((gFlag == Alarmpublic.BOOT_FLAG) && (gHour == tempinfo.hour) && ((gmin - tempinfo.minute) == 0 ||((gmin - tempinfo.minute) == 1))){
					dbAlarmInfo.closeDb();  // 关闭数据库
			//		Alarmpublic.debug("找到数据   === 闹钟==1====\r\n");
					return tempinfo;
				}
				else if ((gFlag == Alarmpublic.NORMAL_FLAG) && (gHour == tempinfo.hour) && (gmin == tempinfo.minute)){
					dbAlarmInfo.closeDb();  // 关闭数据库
			//		Alarmpublic.debug("找到数据   === 闹钟===2===\r\n");
					return tempinfo;
				}
			}
		}
		  
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.ANNIVERSARY_TABLE);  // 纪念日
		
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo = mAlarmAll.get(i);
			if(tempinfo.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
			//	Global.debug("\r\nGetNearAlarm === i = "+ i);
			//	Alarmpublic.debug("\r\nGetNearAlarm === tempinfo_later.hour = "+ tempinfo.hour + " tempinfo.minute = "+ tempinfo.minute);
			//	Alarmpublic.debug("\r\nGetNearAlarm === tempinfo.day = "+ tempinfo.day + " tempinfo.month = "+ tempinfo.month);
				if((gHour == tempinfo.hour) && ((gmin - tempinfo.minute) == 0 ||((gmin - tempinfo.minute) == 1)) &&
						(gday == tempinfo.day) && (gmonth == tempinfo.month)){
					dbAlarmInfo.closeDb();  // 关闭数据库
					return tempinfo;
				}
			}
		}
		mAlarmAll = dbAlarmInfo.getAllData(Alarmpublic.REMIND_TABLE);  // 行程提醒
		
		for(int i= 0; i < mAlarmAll.size(); i++)
		{
			tempinfo = mAlarmAll.get(i);
			if(tempinfo.onoff == Alarmpublic.ALARM_ON){  // 是打开状态
			//	Global.debug("\r\nGetNearAlarm === i = "+ i);
			//	Alarmpublic.debug("\r\nGetNearAlarm =1== tempinfo_later.hour = "+ tempinfo.hour + " tempinfo.minute = "+ tempinfo.minute);
			//	Alarmpublic.debug("\r\nGetNearAlarm =2== tempinfo.day = "+ tempinfo.day + " tempinfo.month = "+ tempinfo.month);
				if((gHour == tempinfo.hour) && ((gmin - tempinfo.minute) == 0 ||((gmin - tempinfo.minute) == 1)) &&
						(gday == tempinfo.day) && (gmonth == tempinfo.month)&&(gyear == tempinfo.year)){
					dbAlarmInfo.closeDb();  // 关闭数据库
					return tempinfo;
				}
			}
		}
		
		dbAlarmInfo.closeDb();  // 关闭数据库
		// 启动 闹钟
		return null;
	}	
	
	@SuppressWarnings("static-access")
	@Override
	public void onStateChanged(int state) {
		if (state == MyPlayer.PLAYING_STATE || state == MyPlayer.RECORDING_STATE) {
//          mSampleInterrupted = false;
//          mErrorUiMessage = null;
		}
		else if(state == myPlayer.IDLE_STATE){
			myPlayer.stopPlayback();
			//Alarmpublic.debug("onStateChanged ===== 1111=================myPlayer.IDLE_STATE==" + myPlayer.IDLE_STATE);
			//finish();  // 在第一个闹钟来时 会改变播放状态 这里也会调 这样会将界面销毁    这里不需要
		}
	}
	@Override
	public void onError(int error) {
	   	
	}
	
	// 按键处理 抬起处理
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Alarmpublic.debug("\r\nonKeyUp  ==================== ");
		// 确认键
		myPlayer.stopPlayback();
	//	myPlayer.release();
		finish();
		return true;
	//	return super.onKeyUp(keyCode, event);	
	}
	
	
	@SuppressWarnings("deprecation")
	private void acquireWakeLock(Context context) {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, context.getClass().getName());
			mWakeLock.acquire();
		}
	}

	private void releaseWakeLock() {
		if (null != mWakeLock && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
