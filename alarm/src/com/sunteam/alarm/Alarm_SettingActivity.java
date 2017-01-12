package com.sunteam.alarm;

import com.sunteam.alarm.utils.Global;
import com.sunteam.common.menu.BaseActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
// 时间设置界面
public class Alarm_SettingActivity extends BaseActivity {
	
	private int gInterfaceFlag = 0;   // 界面标志
	private int gSelectID = 0;   // 界面标志
	
	private int gHour = 0;   // 小时
	private int gMin = 0;   //  分钟
	private int gMonth = 0;   // 月
	private int gDay = 0;   // 日
	
	private int gHour_bk = 0;   // 小时
	private int gMin_bk = 0;   //  分钟
	private int gMonth_bk = 0;   // 月
	private int gDay_bk = 0;   // 日
	
	private int gFlag = 0;   // 修改标志
	
	private int FLAG1 = 0;   // 标志1  编辑 1
	private int FLAG2 = 1;   // 标志2   编辑2
	
	private TextView tv1 = null;
	private TextView tv2 = null;
	private TextView tv3 = null;
	
	TextView mTvTitle = null;
	View mLine = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_setting);
		
		//Global.debug("AlarmSettingActivity ======111===\r\n");
		Global.debug("[AlarmSettingActivity] ==     onCreate == \r\n");

		Intent intent=getIntent();	//获取Intent
		Bundle bundle=intent.getExtras();	//获取 Bundle
		
		gSelectID = bundle.getInt("ID");  // 获取 反显位置
	//	Global.debug("\r\n[^^^^^www^^^^^] gSelectID ===== " + gSelectID);
		gInterfaceFlag = bundle.getInt("FLAG"); // 获取界面标志
		Global.debug("gSelectID=="+ gSelectID + " gInterfaceFlag == "+ gInterfaceFlag);
		
		if(gInterfaceFlag == Global.ALARM_INFO_INTERFACE)  // 闹钟
		{
			gHour_bk = gHour = bundle.getInt("HOUR");
			gMin_bk = gMin = bundle.getInt("MIN");
		}
		else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){	// 纪念日
			gHour_bk = gHour = bundle.getInt("HOUR");
			gMin_bk = gMin = bundle.getInt("MIN");
			
			gMonth_bk = gMonth = bundle.getInt("MONTH");
			gDay_bk = gDay = bundle.getInt("DAY");
		}
		Init();

	}
	
	// 初始化相关
	private void Init() {
		Tools mTools = new Tools(this);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(mTools.getBackgroundColor()));
				
		mTvTitle = (TextView) this.findViewById(R.id.title); // 获取控件
		mLine = this.findViewById(R.id.line); // 获取
		
		tv1 = (TextView)findViewById(R.id.tv1);
		tv2 = (TextView)findViewById(R.id.tv2);
		tv3 = (TextView)findViewById(R.id.tv3);
		
		// 设置显示属性
		int fontSize = mTools.getFontSize();
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // 设置title字号
		mTvTitle.setHeight(mTools.convertSpToPixel(fontSize));
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色
		
		mTvTitle.setTextColor(mTools.getFontColor()); //  设置字体颜色
		tv1.setTextColor(mTools.getFontColor()); //  设置字体颜色
		tv2.setTextColor(mTools.getFontColor()); // 设置字体颜色
		tv3.setTextColor(mTools.getFontColor()); //  设置字体颜色
		
		String title = null;
		
	//	cNum = "0";

		if(gInterfaceFlag == Global.ALARM_INFO_INTERFACE)  // 闹钟
		{
			if(gSelectID == Global.ALARM_SET_TIME){  // 时间
				String  temp = "";
				if(gHour < 10){
					temp = "0";
				}
				tv1.setText(temp + gHour);
				tv2.setText(":");
				
				if(gMin < 10){
					temp = "0";
				}
				else{
					temp = "";
				}
				tv3.setText(temp + gMin);
				
				gFlag = FLAG1;
				
				title = getResources().getString(R.string.time_title);
				
			}			
			
			tv1.setBackgroundColor(new Tools(this).getHighlightColor());
		}
		else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){    // 纪念日
			title = getResources().getString(R.string.time_title);
			
			if(gSelectID == Global.ANNIVERSARY_SET_DATE){  // 日期
				title = getResources().getString(R.string.date_title);
			}
			else if(gSelectID == Global.ANNIVERSARY_SET_TIME){  // 时间	
				title = getResources().getString(R.string.time_title);
			}
			
			gFlag = FLAG1;	
			tv1.setBackgroundColor(new Tools(this).getHighlightColor());
		}
		else if(gInterfaceFlag == Global.COUNTDOWN_INFO_INTERFACE){
			
		}
		mTvTitle.setText(title);
		TtsUtils.getInstance().speak(title);
		showInfo(true);
		
	}
	// 按键处理
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		// 确认键
		if(keyCode == KeyEvent.KEYCODE_ENTER ||
				keyCode == KeyEvent.KEYCODE_DPAD_CENTER)  // 确认键
		{			
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.set_ok));
			mPromptDialog.show();
			mPromptDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					mHandler.sendEmptyMessage(Global.MSG_SETTING_BACK);
				}
			});
					
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){  // 上键

//			Global.debug("KEYCODE_DPAD_UP === 1111");
			if(gSelectID == 0 && (gInterfaceFlag == Global.ALARM_INFO_INTERFACE)){  // 闹钟
				if(gFlag == FLAG1){  // 小时
					if(gHour > 0){
						gHour --;
					}else{
						gHour = 23;
					}
				}
				else if(gFlag == FLAG2) {  // 分钟
					if(gMin > 0){
						gMin --;
					}
					else{
						gMin = 59;
					}
				}
			}
			else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){  // 纪念日
				if(1== gSelectID){
					if(gFlag == FLAG1){  // 小时
						if(gHour > 0){
							gHour --;
						}else{
							gHour = 23;
						}
					}
					else if(gFlag == FLAG2) {  // 分钟
						if(gMin > 0){
							gMin --;
						}else{
							gMin = 59;
						}
					}
				}
				else if(0 ==gSelectID ){
					if(gFlag == FLAG1){  // 月份						
						if(gMonth > 1){
							gMonth --;
						}else{
							gMonth = 12;
						}
						if(gDay > getMonthOfDay(gMonth)){
							gDay = getMonthOfDay(gMonth);
						}
					}
					else if(gFlag == FLAG2){ // 日
						if(gDay > 1){
							gDay --;
						}else{
							gDay = getMonthOfDay(gMonth);
						}
					}
				}
			}
			showInfo(false);
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){  // 下键
			if(gSelectID == 0 && (gInterfaceFlag == Global.ALARM_INFO_INTERFACE)){
				if(gFlag == FLAG1){  // 小时
					if(gHour < 23){
						gHour ++;
					}else{
						gHour = 0;
					}
				}
				else if(gFlag == FLAG2) {  // 分钟
					if(gMin < 59){
						gMin ++;
					}else{
						gMin = 0;
					}
				}
			}
			else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){
				if(Global.ANNIVERSARY_SET_TIME== gSelectID){
					if(gFlag == FLAG1){  // 小时
						if(gHour < 23){
							gHour ++;
						}else{
							gHour = 0;
						}
					}
					else if(gFlag == FLAG2) {  // 分钟
						if(gMin < 59){
							gMin ++;
						}else{
							gMin = 0;
						}
					}
				}
				else if(Global.ANNIVERSARY_SET_DATE ==gSelectID ){

					if(gFlag == FLAG1){  // 月份						
						if(gMonth < 12){
							gMonth ++;
						}else{
							gMonth = 1;
						}
						if(gDay > getMonthOfDay(gMonth)){
							gDay = getMonthOfDay(gMonth);
						}
					}
					else if(gFlag == FLAG2){ // 日
						if(gDay < getMonthOfDay(gMonth)){
							gDay ++;
						}else{
							gDay = 1;
						}
					}
				}
			}
			showInfo(false);
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if(gFlag == FLAG1){
				gFlag = FLAG2;
			}
			else if(gFlag == FLAG2){
				gFlag = FLAG1;
			}
			showInfo(false);
		}
		else if(keyCode == KeyEvent.KEYCODE_BACK){ // 返回
//			Global.debug("{}        gInterfaceFlag === "+ gInterfaceFlag);
			if(gInterfaceFlag == Global.ALARM_INFO_INTERFACE)  // 闹钟
			{
				if((gHour != gHour_bk || gMin != gMin_bk) && (Global.ALARM_SET_TIME == gSelectID))  // 有改变
				{
					ConfirmDialog gConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.canel_set),
																			getResources().getString(R.string.ok), 
																			getResources().getString(R.string.canel));
					gConfirmDialog.show();
					gConfirmDialog.setConfirmListener(new ConfirmListener() {
						
						@Override
						public void doConfirm() {  // 保存数据
							mHandler.sendEmptyMessage(Global.MSG_SETTING_ASK_TIMEBACK);
						}
						
						@Override
						public void doCancel() {   // 直接退出
							mHandler.sendEmptyMessage(Global.MSG_SETTING_FINISH);
						}
					});
				}
				else {
					mHandler.sendEmptyMessage(Global.MSG_SETTING_FINISH);
				}
			}
			else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){	// 纪念日
				Global.debug("[]    gSelectID ==="+ gSelectID);
				if((Global.ANNIVERSARY_SET_DATE == gSelectID) &&( gDay != gDay_bk || gMonth != gMonth_bk)){
					ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.canel_set), getResources().getString(R.string.ok), getResources().getString(R.string.canel));
					mConfirmDialog.show();
					mConfirmDialog.setConfirmListener(new ConfirmListener() {
						
						@Override
						public void doConfirm() {
							mHandler.sendEmptyMessage(Global.MSG_SETTING_ASK_DATEBACK);	
						}
						
						@Override
						public void doCancel() {
							mHandler.sendEmptyMessage(Global.MSG_SETTING_FINISH);
						}
					});
					
				}
				else if((Global.ANNIVERSARY_SET_TIME == gSelectID) &&( gHour != gHour_bk || gMin != gMin_bk)){
					ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.canel_set), getResources().getString(R.string.ok), getResources().getString(R.string.canel));
					mConfirmDialog.show();			
					mConfirmDialog.setConfirmListener(new ConfirmListener() {
						
						@Override
						public void doConfirm() {
							mHandler.sendEmptyMessage(Global.MSG_SETTING_ASK_DATEBACK);
						}
						
						@Override
						public void doCancel() {
							mHandler.sendEmptyMessage(Global.MSG_SETTING_FINISH);
						}
					});
				}
				else {
					mHandler.sendEmptyMessage(Global.MSG_SETTING_FINISH);
				}
			}
			return true;
		}			
		return super.onKeyUp(keyCode, event);	
	}
	
	private int getMonthOfDay(int month) {
		// TODO 自动生成的方法存根
		if((month == 1) ||(month == 3) ||(month == 5) ||(month == 7) ||(month == 8) ||(month == 12) ||(month == 10) ){
			return 31;
		}
		else if(month == 2){
			return 29;
		}
		else{
			return 30;
		}
	}
	// 显示界面
	private void showInfo(boolean flag){
		int ttsFlag = TtsUtils.TTS_QUEUE_FLUSH;
		
		if(flag == true){
			ttsFlag = TtsUtils.TTS_QUEUE_ADD;
		}
		
		if(gInterfaceFlag == Global.ALARM_INFO_INTERFACE)  // 闹钟
		{
			if(gSelectID == Global.ALARM_SET_TIME){  // 时间
				String  temp = "";
				if(gHour < 10){
					temp = "0";
				}
				tv1.setText(temp + gHour);
				tv2.setText(":");
				
				if(gMin < 10){
					temp = "0";
				}
				else{
					temp = "";
				}
				tv3.setText(temp + gMin);
			}
			
			Global.debug("showInfo gFlag == " + gFlag);
			if(gFlag == FLAG1){
				tv1.setBackgroundColor(new Tools(this).getHighlightColor()); // 设置颜色反显	
				TtsUtils.getInstance().speak(gHour + getResources().getString(R.string.hour_time1), ttsFlag);
			}
			else{
				tv1.setBackgroundColor(new Tools(this).getBackgroundColor()); // 设置颜色反显
			}
			
			if(gFlag == FLAG2){
				tv3.setBackgroundColor(new Tools(this).getHighlightColor()); // 设置颜色反显
				TtsUtils.getInstance().speak(gMin + getResources().getString(R.string.min_time), ttsFlag);
			}
			else{
				tv3.setBackgroundColor(new Tools(this).getBackgroundColor()); // 设置颜色反显
			}
			
		}
		else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){
			String  temp = "";
			if(gSelectID == Global.ANNIVERSARY_SET_DATE){  // 日期
				if(gMonth < 10){
					temp = "0";
				}
				tv1.setText(temp + gMonth);
				tv2.setText("-");
				temp = "";
				if(gDay < 10){
					temp = "0";
				}
				tv3.setText(temp + gDay);
				
				if(gFlag == FLAG1){
					tv1.setBackgroundColor(new Tools(this).getHighlightColor()); // 设置颜色反显	
					TtsUtils.getInstance().speak(gMonth +getResources().getString(R.string.month), ttsFlag);
				}
				else{
					tv1.setBackgroundColor(new Tools(this).getBackgroundColor()); // 设置颜色反显
				}
				
				if(gFlag == FLAG2){
					tv3.setBackgroundColor(new Tools(this).getHighlightColor()); // 设置颜色反显
					TtsUtils.getInstance().speak(gDay +getResources().getString(R.string.day), ttsFlag);
				}
				else{
					tv3.setBackgroundColor(new Tools(this).getBackgroundColor()); // 设置颜色反显
				}
			}
			else if(Global.ANNIVERSARY_SET_TIME == gSelectID){
				if(gHour < 10){
					temp = "0";
				}
				tv1.setText(temp + gHour);
				tv2.setText(":");
				
				if(gMin < 10){
					temp = "0";
				}
				else{
					temp = "";
				}
				tv3.setText(temp + gMin);
				
				Global.debug("showInfo gFlag == " + gFlag);
				if(gFlag == FLAG1){
					tv1.setBackgroundColor(new Tools(this).getHighlightColor()); // 设置颜色反显	
					TtsUtils.getInstance().speak(gHour + getResources().getString(R.string.hour_time1), ttsFlag);
				}
				else{
					tv1.setBackgroundColor(new Tools(this).getBackgroundColor()); // 设置颜色反显
				}
				
				if(gFlag == FLAG2){
					tv3.setBackgroundColor(new Tools(this).getHighlightColor()); // 设置颜色反显
					TtsUtils.getInstance().speak(gMin + getResources().getString(R.string.min_time), ttsFlag);
				}
				else{
					tv3.setBackgroundColor(new Tools(this).getBackgroundColor()); // 设置颜色反显
				}
			}
		}
	}
	
	 private Handler mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				Global.debug("\r\n[Alarm_SettingActivity] handleMessage == msg.what = " + msg.what);
				if(msg.what == Global.MSG_SETTING_BACK){   // 音乐播放结束消息
					goBack();
				}
				else if(msg.what == Global.MSG_SETTING_ASK_DATEBACK){
					goBackAskDate();
				}
				else if(msg.what == Global.MSG_SETTING_FINISH){
					finish();
				}
				else if (msg.what == Global.MSG_SETTING_ASK_TIMEBACK){
					goBackAskTime();
				}
				
				super.handleMessage(msg);
			}
		};
		
		private void goBackAskTime() {

			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
			Global.debug("onKeyDown gMin === " + gMin);
			Global.debug("onKeyDown gHour === " + gHour);
			
			bundle.putInt("HOUR", gHour);
			bundle.putInt("MIN", gMin);	
			bundle.putInt("FLAG", gInterfaceFlag);
			bundle.putInt("SELECTID", gSelectID);
			intent.putExtras(bundle); // 参数传递
			setResult(Global.FLAG_CODE,intent);	//返回界面
			finish();
		}
		
		private void goBackAskDate() {
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
			
			Global.debug("onKeyDown gmonth === " + gMonth);
			Global.debug("onKeyDown gday === " + gDay);
			
			Global.debug("onKeyDown gMin === " + gMin);
			Global.debug("onKeyDown gHour === " + gHour);
			
			bundle.putInt("MONTH", gMonth);
			bundle.putInt("DAY", gDay);
			
			bundle.putInt("HOUR", gHour);
			bundle.putInt("MIN", gMin);
	
			bundle.putInt("FLAG", gInterfaceFlag);
			bundle.putInt("SELECTID", gSelectID);
			intent.putExtras(bundle); // 参数传递
			setResult(Global.FLAG_CODE,intent);	//返回界面
			finish();	
		}
		
		// 返回上一界面
		private void goBack() {
			
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
			
			Global.debug("onKeyDown gmonth === " + gMonth);
			Global.debug("onKeyDown gday === " + gDay);
			
			Global.debug("onKeyDown gMin === " + gMin);
			Global.debug("onKeyDown gHour === " + gHour);
			
			if(gInterfaceFlag == Global.ALARM_INFO_INTERFACE)  // 闹钟
			{
				bundle.putInt("HOUR", gHour);
				bundle.putInt("MIN", gMin);
			}
			else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){	// 纪念日
				bundle.putInt("MONTH", gMonth);
				bundle.putInt("DAY", gDay);
				
				bundle.putInt("HOUR", gHour);
				bundle.putInt("MIN", gMin);
			}
			bundle.putInt("FLAG", gInterfaceFlag);
			bundle.putInt("SELECTID", gSelectID);
			
			//Global.debug("\r\n[^^^^^^^^^^] gSelectID ===== " + gSelectID);
			intent.putExtras(bundle); // 参数传递
			setResult(Global.FLAG_CODE,intent);	//返回界面
			
			finish();
		}
}
