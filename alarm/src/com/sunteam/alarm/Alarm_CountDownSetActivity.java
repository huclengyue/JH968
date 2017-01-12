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

public class Alarm_CountDownSetActivity extends BaseActivity {


	private int gSelectID = 0;   // 界面标志

	private int HOUR_FLAG = 0;
	private int MINUTE_FLAG = 1;
	private int gFlag = HOUR_FLAG;

	private int gHour = 0;
	private int gMinute = 0;
	
	private int gCountdaownTime = 1;   //  倒计时时间
	private TextView tv1 = null;
	private TextView tv2 = null;
	private TextView tv3 = null;
	private TextView tv4 = null;
	
	TextView mTvTitle = null;
	View mLine = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm__count_down_set);
		
		Global.debug("AlarmSettingActivity ======111===\r\n");
		

		Intent intent=getIntent();	//获取Intent
		Bundle bundle=intent.getExtras();	//获取 Bundle
		
		gSelectID = bundle.getInt("ID");  // 获取 反显位置
		Init();

	}
	
	// 初始化相关
	private void Init() {
		// TODO 自动生成的方法存根
		Tools mTools = new Tools(this);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(mTools.getBackgroundColor()));
				
		mTvTitle = (TextView) this.findViewById(R.id.title); // 获取控件
		mLine = this.findViewById(R.id.line); // 获取
		
		tv1 = (TextView)findViewById(R.id.tv1);
		tv2 = (TextView)findViewById(R.id.tv2);
		tv3 = (TextView)findViewById(R.id.tv3);
		tv4 = (TextView)findViewById(R.id.tv4);
		
		
		int fontSize = mTools.getFontSize();
		
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // 设置title字号
		mTvTitle.setHeight(mTools.convertSpToPixel(fontSize));
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色
		
		mTvTitle.setTextColor(mTools.getFontColor()); //  设置字体颜色
		tv1.setTextColor(mTools.getFontColor()); //  设置字体颜色
		tv2.setTextColor(mTools.getFontColor()); // 设置字体颜色
		tv3.setTextColor(mTools.getFontColor()); //  设置字体颜色
		tv4.setTextColor(mTools.getFontColor()); //  设置字体颜色
		
		//tv1.setTextColor(new Tools(this).getHighlightColor()); // 设置颜色反显	

		//tv1.setBackgroundColor(mTools.getHighlightColor());

		tv1.setBackgroundColor(mTools.getBackgroundColor());
		tv2.setBackgroundColor(mTools.getBackgroundColor());
		tv3.setBackgroundColor(mTools.getBackgroundColor());
		tv4.setBackgroundColor(mTools.getBackgroundColor());
	/*
		tv1.setText(""+ gCountdaownTime/60);
		tv2.setText(":");  // 
		tv3.setText(""+gCountdaownTime%60);
		*/	
		showInfo();
		mTvTitle.setText(getResources().getString(R.string.time_self));
	}
	// 按键处理
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		// 确认键
		if(keyCode == KeyEvent.KEYCODE_ENTER ||
				keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
		{			
			gCountdaownTime = (gHour*60 + gMinute)*60;
			if(gCountdaownTime > 0){ 

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				
				bundle.putInt("TIMELEN", gCountdaownTime); // 修改项
				
				// intent.setClass(MainActivity.this, MenuActivity.class);
				intent.putExtras(bundle); // 传入参数
				intent.setClass(Alarm_CountDownSetActivity.this, Alarm_countdownActivity.class);
	
				startActivity(intent);
				
				
				finish();
			}
			else{
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.count_down_set_error));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						mHandler.sendEmptyMessage(Global.MSG_COUNTDOWN_ERROR);
					}
				});
			}
	
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
			if(gFlag == HOUR_FLAG){
				if(gHour > 0){
					gHour --;
				}
				else{
					if(gMinute > 0){
						gHour = Global.COUNTDOWN_MAX_TIME/60 - 1;
					}
					else{
						gHour = Global.COUNTDOWN_MAX_TIME/60;
					}
				}
			}
			else if(gFlag == MINUTE_FLAG){
				if(gMinute > 0){
					gMinute --;
				}
				else{
					gMinute = 59;
				}
			}
			showInfo();
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			if(gFlag == HOUR_FLAG){
				int hour = 0;
				if(gMinute > 0){
					hour = Global.COUNTDOWN_MAX_TIME/60 - 1;
				}
				else{
					hour = Global.COUNTDOWN_MAX_TIME/60;
				}
					
				if(gHour < hour){
					gHour ++;
				}
				else{
					gHour = 0;
				}
			}
			else if(gFlag == MINUTE_FLAG){
				if(gMinute <59){
					gMinute ++;
				}
				else{
					gMinute = 0;
				}
			}
			showInfo();
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT ){  // 增加一位
	//		Global.debug("LEFT RIHKT  gFlag == " + gFlag);
			if(gFlag == HOUR_FLAG){
				gFlag = MINUTE_FLAG;
			}
			else{
				gFlag = HOUR_FLAG;
			}
			showInfo();
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){ // 减少一位
		
			if(gFlag == HOUR_FLAG){
				gFlag = MINUTE_FLAG;
			}
			else{
				gFlag = HOUR_FLAG;
			}
			
			showInfo();
		}
		else if(keyCode == KeyEvent.KEYCODE_BACK){ // 返回
			ConfirmDialog gConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.canel_set_countdown), getResources().getString(R.string.ok), getResources().getString(R.string.canel));
			gConfirmDialog.show();
			gConfirmDialog.setConfirmListener(new ConfirmListener() {
				
				@Override
				public void doConfirm() {  // 保存数据
					
					Intent intent = new Intent();	//新建 INtent
					Bundle bundle = new Bundle();	//新建 bundle
					
					bundle.putInt("SELECTID", gSelectID);
					intent.putExtras(bundle); // 参数传递
					setResult(Global.FLAG_CODE,intent);	//返回界面
					finish();
				}
				
				@Override
				public void doCancel() {   // 直接退出
					mHandler.sendEmptyMessage(Global.MSG_COUNTDOWN_ERROR);
				}
			});
			
			return true;
		}			
		return super.onKeyUp(keyCode, event);	
	}
	

	// 显示界面
	private void showInfo(){
		Tools mTools = new Tools(this);
		
		if(gHour < 10){
			tv1.setText("0"+gHour);	
		}
		else{
			tv1.setText(+gHour);
		}
		
		tv2.setText(getResources().getString(R.string.hour_time));
		if(gMinute < 10){
			tv3.setText("0"+gMinute);
		}
		else{
			tv3.setText(""+gMinute);
		}
		tv4.setText(getResources().getString(R.string.min_time));
		
		if(gFlag == HOUR_FLAG){
			tv1.setBackgroundColor(mTools.getHighlightColor());
			
			TtsUtils.getInstance().speak(gHour + getResources().getString(R.string.hour_time));
		}
		else {
			tv1.setBackgroundColor(mTools.getBackgroundColor());
		}
		
		if(gFlag == MINUTE_FLAG){
			tv3.setBackgroundColor(mTools.getHighlightColor());
			TtsUtils.getInstance().speak(gMinute + getResources().getString(R.string.min_time));
		}
		else{
			tv3.setBackgroundColor(mTools.getBackgroundColor());
		}
	}
	
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_COUNTDOWN_ERROR){   // 音乐播放结束消息
				showCountDownErrorPromptDialog();
			}
			
			super.handleMessage(msg);
		}
	};
	
	
	private void showCountDownErrorPromptDialog() {
		showInfo();
	}
}
