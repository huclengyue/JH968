package com.sunteam.calendar;

import java.util.Calendar;

import com.sunteam.calendar.constant.Global;
import com.sunteam.common.menu.BaseActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.dao.Alarminfo;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class TimeSetActivity extends BaseActivity {

	private int gyear = 0;
	private int gmonth = 0;
	private int gday = 0;
	
	@SuppressWarnings("unused")
	private boolean d = false;
	
	private String FileName = null;  // 录音文件名    getResources().getString(R.string.remind_noFile);
	private String Path = null;   // 录音文件路径
	private int gonoff = 0; //开关标志
	private int ghour = 0;  // 小时
	private int gminute = 0;  // 分钟
	private int ghour_bk = 0;  // 小时
	private int gminute_bk = 0;  // 分钟
	
	private int gSelectFlag = 0;  // 选中项
	private int SELECT_HOUR_ID = 0;  // 选中 时
	private int SELECT_MINUTE_ID = 1;  // 选中 分
	
	private TextView mTvTitle = null;
	//private View mView = null;
	private View mLine = null;
	
	private TextView mTvHour = null;
	private TextView mTvMinute = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_time_set);
		
		Intent intent = getIntent();
		Bundle bundle =intent.getExtras();
		
		ghour_bk = ghour = bundle.getInt("HOUR");
		gminute_bk = gminute = bundle.getInt("MINUTE");
		
		gyear = bundle.getInt("YEAR");
		gmonth = bundle.getInt("MONTH");
		gday = bundle.getInt("DAY");
		
		Global.debug("TimeSetActivity gyear = "+ gyear + " gmonth =" + gmonth +  " gday ="+ gday);
		
		gonoff = bundle.getInt("ONOFF");
		FileName = bundle.getString("FILENAME");
		Path = bundle.getString("PATH");
	
		TextView tv1 = (TextView)findViewById(R.id.test);
		tv1.setText(":");
		
		mTvTitle = (TextView) findViewById(R.id.title);
		mLine = findViewById(R.id.line);
		mTvHour = (TextView) findViewById(R.id.hour);
		mTvMinute = (TextView) findViewById(R.id.minute);
		//mTvTitle.setText(getResources().getString(R.string.minute));
		
		gSelectFlag = SELECT_HOUR_ID;

	}
	
	@Override
	public void onResume() {
		
		Tools mTools = new Tools(this);	
				
		this.getWindow().setBackgroundDrawable(new ColorDrawable(new Tools(this).getBackgroundColor()));
		mTvTitle.setTextColor(mTools.getFontColor()); // 设置title的文字颜色
				
		int fontSize = mTools.getFontSize();
		
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // 设置title字号
		mTvTitle.setHeight(mTools.convertSpToPixel(fontSize));
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色
		
		mTvHour.setTextColor(mTools.getFontColor());
		mTvMinute.setTextColor(mTools.getFontColor());
		TextView Tv = (TextView)findViewById(R.id.test);
		Tv.setTextColor(mTools.getFontColor());
		
		TtsUtils.getInstance().stop();
		
		// 此处需要加TTS朗读item内容。
		TtsUtils.getInstance().speak(mTvTitle.getText().toString());
		
		showTextView(ghour, gminute);
		
		super.onResume();
	}
	
	// 键按下
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		
		switch (keyCode) {   
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_DOWN:  // 下键
		case KeyEvent.KEYCODE_DPAD_UP: // 上键
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左键
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右键
		case KeyEvent.KEYCODE_MENU:   // menu界面启动
		case KeyEvent.KEYCODE_F2:
		case KeyEvent.KEYCODE_PAGE_UP:
					
			break;
			
		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	// 键抬起
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {	
		switch (keyCode) {   
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			//saveDataRemind();	
			saveDataForDate();
			return true;
	
		case KeyEvent.KEYCODE_DPAD_DOWN:  // 下键
			if(gSelectFlag == SELECT_HOUR_ID)
			{
				if(ghour < Global.MAX_HOUR){
					ghour++;
				}
				else{
					ghour = 0;
				}
			}
			else{
				if(gminute < Global.MAX_MINUTE){
					gminute++;
				}
				else{
					gminute = 0;
				}
			}
			Global.debug("====ghour = "+ghour + " gminute == "+gminute);
			showTextView(ghour, gminute);
			return true;
			
		case KeyEvent.KEYCODE_DPAD_UP: // 上键
			if(gSelectFlag == SELECT_HOUR_ID)
			{
				if(ghour > 0){
					ghour --;
				}
				else{
					ghour = Global.MAX_HOUR;
				}
			}
			else{
				if(gminute > 0){
					gminute--;
				}
				else{
					gminute = Global.MAX_MINUTE;
				}
			}
			Global.debug("====ghour = "+ghour + " gminute == "+gminute);
			showTextView(ghour, gminute);
			return true;
			
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左键
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右键
			if(gSelectFlag == SELECT_HOUR_ID){
				gSelectFlag = SELECT_MINUTE_ID; 
			}
			else{
				gSelectFlag = SELECT_HOUR_ID; 
			}
			showTextView(ghour, gminute);
			return true;
					
		case KeyEvent.KEYCODE_MENU:   // menu界面启动
		case KeyEvent.KEYCODE_F2:
		case KeyEvent.KEYCODE_PAGE_UP:
					
			break;
		case KeyEvent.KEYCODE_BACK:
			KeyBack();
			
			return true;
			
		default:
			break;
		}

		return super.onKeyUp(keyCode, event);
	}	
	
	// back 键  确认设置
	private void KeyBack_doConfirm(){
		mHandler.sendEmptyMessage(Global.MSG_SETOK);
	
	}
	
	private void KeyBack() {
			// TODO 自动生成的方法存根
			if((ghour != ghour_bk) || (gminute != gminute_bk))
			{
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.canel_set), 
																getResources().getString(R.string.ok),
																getResources().getString(R.string.canel));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {
					
					@Override
					public void doConfirm() {
						// TODO 自动生成的方法存根
						KeyBack_doConfirm();
					}
					
					@Override
					public void doCancel() {
						// TODO 自动生成的方法存根
						finish();
					}
				});
			}
			else{
				finish();
			}
		}

	private void showTextView(int hour, int minute) {
		// TODO 自动生成的方法存根
		String str_temp;
		mTvTitle.setText(getResources().getString(R.string.title_time_set));
			
		if(hour < 10)
		{
			str_temp = "0"+hour;
		}
		else{
			str_temp = ""+hour;
		}
		mTvHour.setText(str_temp);
		
		str_temp = null;
		
		if(minute < 10){
			str_temp = "0" + minute;
		}
		else{
			str_temp = "" + minute;
		}
		mTvMinute.setText(str_temp);
		
		Tools mTools = new Tools(this);
		if(gSelectFlag == SELECT_HOUR_ID){
			mTvHour.setBackgroundColor(mTools.getHighlightColor());
			
			TtsUtils.getInstance().speak(ghour + getResources().getString(R.string.hour));
		}
		else{
			mTvHour.setBackgroundColor(mTools.getBackgroundColor());
		}
		
		if(gSelectFlag == SELECT_MINUTE_ID){
			mTvMinute.setBackgroundColor(mTools.getHighlightColor());
			TtsUtils.getInstance().speak(gminute + getResources().getString(R.string.minute));
		}
		else{
			mTvMinute.setBackgroundColor(mTools.getBackgroundColor());
		}
				
	}
	
	// 保存提醒数据
	private void saveDataForDate() {
		// TODO 自动生成的方法存根
		Alarminfo tempinfo = new Alarminfo();  // 
		
		
		tempinfo.year = gyear;
		tempinfo.month = gmonth;
		tempinfo.day = gday;
		
		tempinfo.hour = ghour;
		tempinfo.minute = gminute;
		
		tempinfo.filename = FileName;
		tempinfo.path = Path;
		tempinfo.onoff = gonoff;
		
		Global.debug("saveDataForDate gyear = "+ gyear + " gmonth =" + gmonth +  " gday ="+ gday);
		
		Global.debug("saveDataForDate time ===111=== \r\n");
		
		Calendar calendar = Calendar.getInstance();  // 获取日历

		Calendar tempcalendar = Calendar.getInstance();  // 获取日历
		
		tempcalendar.set(Calendar.YEAR, gyear);
		tempcalendar.set(Calendar.MONTH, gmonth -1);
		tempcalendar.set(Calendar.DAY_OF_MONTH, gday);
		tempcalendar.set(Calendar.HOUR_OF_DAY, ghour);
		tempcalendar.set(Calendar.MINUTE, gminute);
		
		Global.debug("saveDataRemind ==== gyear ="+ gyear);
		Global.debug("saveDataRemind ==== gmonth ="+ gmonth);
		Global.debug("saveDataRemind ==== gday ="+ gday);
		Global.debug("saveDataRemind ==== ghour ="+ ghour);
		Global.debug("saveDataRemind ==== gminute ="+ gminute);
		
		int time_len = (int) (tempcalendar.getTimeInMillis() - calendar.getTimeInMillis());
		Global.debug("saveDataRemind ==== time_len ="+ time_len);
		
		//Global.debug("maxId ===" + max_num);
		if(calendar.after(tempcalendar)) // 时间超前 不记录
		{
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.time_after));
			mPromptDialog.show();
			mPromptDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					
					showTextView(ghour, gminute);
				}
			});
		}
		else{
			Global.debug("\r\n ghour ===111=="+ghour);
			Global.debug("\r\n gminute ==1111==="+gminute);
		
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.set_ok));
			mPromptDialog.show();
			mPromptDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					Intent intent = new Intent();			
					Bundle bundle = new Bundle();//

					bundle.putInt("HOUR", ghour); // 传入参数 年			
					bundle.putInt("MINUTE", gminute); // 传入参数 年
					Global.debug("\r\n ghour ====="+ghour);
					Global.debug("\r\n gminute ====="+gminute);
					//intent.setClass(MainActivity.this, MenuActivity.class);
					intent.putExtras(bundle); // 传入参数
					intent.setAction("remind_action");// 启动界面
					setResult(Global.FLAG_TIME_ID,intent);	//返回界面
					
					// Ok键 
					finish();						
				}
			});
			
		}
	}
	
	// 处理弹框
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_SETOK){   // 音乐播放结束消息
				showSetOkPromptDialog();
			}else if(msg.what == Global.MSG_NO_REMIND){
			
			}else if(msg.what == Global.MSG_DEL){
			
			}
			else if(msg.what == Global.MSG_NO_REMIND_TOMAIN){
				
			}
			else if(msg.what == Global.MSG_ONRESUM){
				onResume();
			}
			
			super.handleMessage(msg);
		}
	};
	// 显示设置成功
	private void showSetOkPromptDialog() {
		PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.set_ok));
		mDialog.show();
		mDialog.setPromptListener(new PromptListener() {
			
			@Override
			public void onComplete() {
				// TODO 自动生成的方法存根
				Intent intent = new Intent();			
				Bundle bundle = new Bundle();//

				bundle.putInt("HOUR", ghour); // 传入参数 年			
				bundle.putInt("MINUTE", gminute); // 传入参数 年
				
				//intent.setClass(MainActivity.this, MenuActivity.class);
				intent.putExtras(bundle); // 传入参数
				intent.setAction("remind_action");// 启动界面
				setResult(Global.FLAG_TIME_ID,intent);	//返回界面
				
				// Ok键 
				finish();
			}
		});
		
	}
}
