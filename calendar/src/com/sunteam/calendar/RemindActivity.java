package com.sunteam.calendar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.sunteam.calendar.constant.Global;
import com.sunteam.calendar.player.MyPlayer;
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.dao.Alarminfo;
import com.sunteam.dao.GetDbInfo;
import com.sunteam.receiver.Alarmpublic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class RemindActivity extends MenuActivity{
	// 全局变量
	//private MainView mRemindView = null;			// 主界面

	private int gID = 0;
	private int gyear = 0;
	private int gmonth = 0;
	private int gday = 0;
	private int gCall_Flag = 0;// 界面进入标志
	//private String  HOUR = "08:00";  // 
	
	private int RECORD_ID = 0;   // 录音
	private int TIME_ID = 1;   // 时间
	private int EN_ID = 2;   // 开光
	
	private int ghour = Alarmpublic.DEF_HOUR;   // 开始
	private int gmin = Alarmpublic.DEF_MIN;   // 开始
	
	private String gFileName_bk = null; 
	private String gFileName = Global.ALARM_FILE_NAME; //Alarmpublic.ALARM_FILE_NAME;  // 录音文件名    getResources().getString(R.string.remind_noFile);
	private String gPath = Alarmpublic.ALARM_PATH;   // 录音文件路径
	private int gonoff = Alarmpublic.ALARM_ON;   // 开关标志
	
	private int gSelectID = 0;   // 选择标志
	private MyPlayer myPlayer = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Intent intent=getIntent();	// 获取Intent
		Bundle bundle=intent.getExtras();	// 获取Bundle
		
		gCall_Flag = bundle.getInt("CALLID");  // 获取界面进入标志
		
		
		// 传入参数
		gyear = bundle.getInt("YEAR");  // 获取用户ID
		gmonth = bundle.getInt("MONTH");  // 获取用户ID
		gday = bundle.getInt("DAY");  // 获取用户ID
		
		if(gCall_Flag == Global.REMIND_CALL_MENU){
			ghour = bundle.getInt("HOUR");
			gmin = bundle.getInt("MINUTE");
			
			gonoff = bundle.getInt("ONOFF");
			
			gID = bundle.getInt("ID");
			gFileName = bundle.getString("FILENAME");
			gPath = bundle.getString("PATH");
			
			mTitle = getResources().getString(R.string.title_remind);
		}
		else if(gCall_Flag == Global.REMIND_CALL_ADD_MENU){
			mTitle = getResources().getString(R.string.add_Remind);
		}
		else if(gCall_Flag == Global.REMIND_CALL_MAIN){
			Global.debug("\r\n RemindActivity gyear = "+ gyear + " gmonth =" + gmonth +  " gday ="+ gday);
			mTitle = getResources().getString(R.string.title_remind);
		}
		String[] mlist = getResources().getStringArray(R.array.remind_list);
		//FileName = getResources().getString(R.string.remind_noFile);
		mlist[0] = mlist[0] + gFileName;
		Global.debug("\r\n FileName ====" + gFileName);
		if(ghour < 10){
			mlist[1] = mlist[1] + "0" +ghour + ":" ;
		}
		else{
			mlist[1] = mlist[1] + ghour + ":";
		}
		
		if(gmin < 10){
			mlist[1] = mlist[1] + "0"+gmin;
		}
		else{
			mlist[1] = mlist[1] +gmin;
		}
		if(Alarmpublic.ALARM_OFF == gonoff){
			mlist[2] = mlist[2] + getResources().getString(R.string.remind_off);
		}
		else if(Alarmpublic.ALARM_ON == gonoff){
			mlist[2] = mlist[2] + getResources().getString(R.string.remind_on);
		}
		
		mMenuList = ArrayUtils.strArray2List(mlist);
		selectItem = 0;		
		super.onCreate(savedInstanceState);
		
		myPlayer = MyPlayer.getInstance(this,null);
		
		playRecordSound();
	}
	private void playRecordSound(){
		
		Global.debug("\r\n [playRecordSound]    getSelectItem() == " + getSelectItem());
		if(0 == getSelectItem()){  // 反显第一项 录音项
			if(!gFileName.equals(Global.ALARM_FILE_NAME)){  // 有文件 播放
				
				if(gPath.equals(Global.TIXING_PATH)){
					Global.debug("\r\n [playRecordSound] [1]   gFileName == " + gPath+ "/" +gFileName);
					myPlayer.startPlayback(myPlayer.playProgress(), gPath+ "/" +gFileName ,true);
				}
				else{
					Global.debug("\r\n [playRecordSound]  [2]  gFileName == " + gPath);
					myPlayer.startPlayback(myPlayer.playProgress(), gPath ,true);
				}
			}
			else{
				Global.debug("\r\n [playRecordSound]    stop == " );
				myPlayer.stopPlayback();
			}
		}
		else{
			myPlayer.stopPlayback();
		}
	}
	// 键按下
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	
		if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN||
				keyCode == KeyEvent.KEYCODE_DPAD_UP||
				keyCode == KeyEvent.KEYCODE_DPAD_LEFT||
				keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			
			super.onKeyDown(keyCode, event);
			playRecordSound();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	// 键抬起
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	
		if(keyCode == KeyEvent.KEYCODE_BACK){  // 返回  
			if(gFileName.equals(Global.ALARM_FILE_NAME)){  // 没有保存记录
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.no_record),
						getResources().getString(R.string.ok), getResources().getString(R.string.canel));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {
					
					@Override
					public void doConfirm() {  // 进入录音
						mHandler.sendEmptyMessage(Global.MSG_START_RECORD);
					}
					
					@Override
					public void doCancel() {
						mHandler.sendEmptyMessage(Global.MSG_START_RECORD_NO);
					}
				});
			}
			else{
				Global.debug("\r\n 可以保存 ------------------- \r\n");
				boolean rtn = false;
				if(gCall_Flag == Global.REMIND_CALL_MENU){
					rtn = saveDataRemindForMenu();
				}
				else{
					rtn = saveDataRemind();
				}
				if(false == rtn){
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.save_exit));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							mHandler.sendEmptyMessage(Global.MSG_SAVE_EXIT);
						}
					});
				}
			}
			
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	// 重新实现 方法
	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		gSelectID = selectItem;
		Global.debug("\r\nsetResultCode ======================gSelectID==" + gSelectID);
		if(selectItem == RECORD_ID)  // 录音
		{
			if(gFileName.equals(Global.ALARM_FILE_NAME)){  // 没有文件
				startRecord();
			}
			else{
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.ask_record),		
																	   getResources().getString(R.string.ok), 
																	   getResources().getString(R.string.canel));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {
					
					@Override
					public void doConfirm() {
						//startRecord();
						mHandler.sendEmptyMessage(Global.MSG_START_RECORD);
					}
					
					@Override
					public void doCancel() {
						mHandler.sendEmptyMessage(Global.MSG_ONRESUM);	
					}
				});
			}
		}
		else if(selectItem == TIME_ID) // 时间设置
		{
			Intent intent = new Intent();			
			Bundle bundle = new Bundle();//

			bundle.putInt("HOUR", ghour); // 传入参数 时			
			bundle.putInt("MINUTE", gmin); // 传入参数 分
			
			bundle.putInt("YEAR", gyear); // 传入参数 年			
			bundle.putInt("MONTH", gmonth); // 传入参数 月
			bundle.putInt("DAY", gday); // 传入参数 日
			bundle.putInt("ONOFF", gonoff); // 传入参数 日
						
			bundle.putString("FILENAME", gFileName); // 传入参数 日
			bundle.putString("PATH", gPath); // 传入参数 日
			
			//intent.setClass(MainActivity.this, MenuActivity.class);
			intent.putExtras(bundle); // 传入参数
			//intent.setAction("TimeSetActivity");// 启动界面
			intent.setClass(this, TimeSetActivity.class);
			
			startActivityForResult(intent , Global.FLAG_TIME_ID);  // 设置标志
		}
		else if(EN_ID == selectItem){  // 开 关    保存数据
			Intent intent = new Intent();			
			Bundle bundle = new Bundle();//

			bundle.putInt("HOUR", ghour); // 传入参数 时			
			bundle.putInt("MINUTE", gmin); // 传入参数 分
			
			bundle.putInt("YEAR", gyear); // 传入参数 年			
			bundle.putInt("MONTH", gmonth); // 传入参数 月
			bundle.putInt("DAY", gday); // 传入参数 日
			bundle.putInt("ONOFF", gonoff); // 传入参数 日
			Global.debug("setResultCode gyear = "+ gyear + " gmonth =" + gmonth +  " gday ="+ gday);
			
			bundle.putString("FILENAME", gFileName); // 传入参数 日
			bundle.putString("PATH", gPath); // 传入参数 日
			
			intent.putExtras(bundle); // 传入参数
			//intent.setAction("TimeSetActivity");// 启动界面
			intent.setClass(this, OnoffSetActivity.class);
			
			startActivityForResult(intent, Global.FLAG_ONOFF_ID);  // 设置标志
		}	
	}

// 进入录音界面
	@SuppressLint("SimpleDateFormat")
	private void startRecord() {
	
		myPlayer.stopPlayback();
		
		Intent intent = new Intent();
		//String packageName = "com.sunteam.calendar";
		String packageName = "com.sunteam.recorder";
		String className = "com.sunteam.recorder.activity.RecordActivity";
		String path = Global.TIXING_PATH; //Environment.getExternalStorageDirectory().getAbsolutePath() + "/tixing";
		gPath = path;
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = "NT" + sdf.format(dt) + ".wav";
		gFileName_bk = gFileName;
		gFileName = fileName;
		intent.putExtra("callerId", 2); // 0: 默认由录音机调用; 1: 热键进入录音; 2: 万年历中提醒录音;
										// 3: 语音备忘录音; 后两者在退出时返回到调用者!
		intent.putExtra("path", path); // 文件路径
		intent.putExtra("fileName", fileName); // 文件名
		intent.setClassName(packageName, className);
		
		startActivityForResult(intent , Global.FLAG_RECORD_ID);  // 设置标志
	}

	// 参数返回 从设置界面返回
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Global.debug("[*********]onActivityResult requestCode =" + requestCode + " resultCode = " + resultCode);
		
		if (requestCode == Global.FLAG_RECORD_ID /*&& resultCode == Global.FLAG_RECORD_ID*/) {   // 录音界面返回
			File mFile = new File(gPath+ "/" +gFileName);
			if(!mFile.exists()){ // 不存在  还用原来的文件
				gFileName = gFileName_bk;
			}
			showList();
		}
		else if(requestCode == Global.FLAG_TIME_ID && resultCode == Global.FLAG_TIME_ID) {   // 时间界面返回
			Global.debug("onActivityResult ========FLAG_TIME_ID=");
			
			Bundle bundle = data.getExtras();
			ghour = bundle.getInt("HOUR");
			gmin = bundle.getInt("MINUTE");
			
			Global.debug("\r\n ghour =="+ ghour);
			Global.debug(" \r\n gmin =="+ gmin);
			
			showList();
		}
		else if(requestCode == Global.FLAG_ONOFF_ID && resultCode == Global.FLAG_ONOFF_ID) {   // 时间界面返回
			Bundle bundle = data.getExtras();
			//int temp_id = bundle.getInt("SELECID");		
			gonoff = bundle.getInt("ONOFF");

			showList();
		}
	}
	
	// 更新显示
	public void showList()
	{
		String[] mlist = getResources().getStringArray(R.array.remind_list);
		mlist[0] = mlist[0] + gFileName;//getResources().getString(R.string.remind_noFile);
		if(ghour < 10){
			mlist[1] = mlist[1] + "0" +ghour + ":" ;
		}
		else{
			mlist[1] = mlist[1] + ghour + ":";
		}
		
		if(gmin < 10){
			mlist[1] = mlist[1] + "0"+gmin;
		}
		else{
			mlist[1] = mlist[1] +gmin;
		}

		if(gonoff == Alarmpublic.ALARM_OFF){
			mlist[2] = mlist[2] + getResources().getString(R.string.remind_off);
		}
		else{
			mlist[2] = mlist[2] + getResources().getString(R.string.remind_on);
		}

		Global.debug(mlist[0]);
		Global.debug(mlist[1]);
		Global.debug(mlist[2]);
		
		mMenuList = null;
		mMenuList = ArrayUtils.strArray2List(mlist);
		setListData(mMenuList);	
		
		mMenuView.setSelectItem(gSelectID);
		onResume();
		
	}
	// 保存提醒数据
	private Boolean saveDataRemind() {
		// TODO 自动生成的方法存根
		final Alarminfo tempinfo = new Alarminfo();  // 
		boolean flag = false;
		
		tempinfo.year = gyear;
		tempinfo.month = gmonth;
		tempinfo.day = gday;
		
		tempinfo.hour = ghour;
		tempinfo.minute = gmin;
		
		tempinfo.filename = gFileName;
		tempinfo.path = gPath + "/" + gFileName;
		tempinfo.onoff = gonoff;
		
		Global.debug("saveDataRemind ===111=== tempinfo.onoff ==" + tempinfo.onoff);
			
		Calendar calendar = Calendar.getInstance();  // 获取日历

		Calendar tempcalendar = Calendar.getInstance();  // 获取日历
		
		tempcalendar.set(Calendar.YEAR, gyear);
		tempcalendar.set(Calendar.MONTH, gmonth -1);
		tempcalendar.set(Calendar.DAY_OF_MONTH, gday);
		tempcalendar.set(Calendar.HOUR_OF_DAY, ghour);
		tempcalendar.set(Calendar.MINUTE, gmin);
		
		
		if(calendar.after(tempcalendar))  // 时间已过
		{
			flag = true;
			ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.time_after),
					getResources().getString(R.string.ok), 
					getResources().getString(R.string.canel));
			mConfirmDialog.show();
			mConfirmDialog.setConfirmListener(new ConfirmListener() {
			
			@Override
				public void doConfirm() {
					mHandler.sendEmptyMessage(Global.MSG_TIME_AFTER_YES);
				}
				
				@Override
				public void doCancel() {
					mHandler.sendEmptyMessage(Global.MSG_TIME_AFTER_NO);
				}
			});
			return flag;
		}
		
		
		final GetDbInfo dbInfo = new GetDbInfo(RemindActivity.this);  // 打开数据库
		int max_num = dbInfo.getCount(Alarmpublic.REMIND_TABLE); // 记录条数
		int max_Id = dbInfo.getMaxId(Alarmpublic.REMIND_TABLE); // 记录条数
		if(max_num <= 0) // 无数据
		{
			 // 增加数据
			tempinfo._id = max_Id +1;
			dbInfo.add(tempinfo, Alarmpublic.REMIND_TABLE);	
		}
		else{  // 已经有数据 
			Alarminfo tempinfo_1 = new Alarminfo();  // 临时变量
			
			ArrayList<Alarminfo> allData = new ArrayList<Alarminfo>();
			allData = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
			for(int i = 0; i < max_num; i++) // 查找相同的 替换
			{
				tempinfo_1 = allData.get(i);
				if((tempinfo.year ==  tempinfo_1.year) &&
						(tempinfo.month == tempinfo_1.month)&&
						(tempinfo.day == tempinfo_1.day)&&
						(tempinfo.hour == tempinfo_1.hour)&&
						(tempinfo.minute == tempinfo_1.minute))  // 同一天
				{
					tempinfo._id = tempinfo_1._id;
					ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.save_update),
																			getResources().getString(R.string.ok), 
																			getResources().getString(R.string.canel));
					mConfirmDialog.show();
					mConfirmDialog.setConfirmListener(new ConfirmListener() {
						
						@Override
						public void doConfirm() {
							dbInfo.update(tempinfo, Alarmpublic.REMIND_TABLE);
							mHandler.sendEmptyMessage(Global.MSG_SAVE_UPDATE_YES);
						}
						
						@Override
						public void doCancel() {
							mHandler.sendEmptyMessage(Global.MSG_SAVE_UPDATE_NO);		
						}
					});
					
					flag = true;   // 数据以保存
					break;
				}
			}
			if(false == flag)  // 数据未保存
			{
				tempinfo._id = max_Id +1;
				dbInfo.add(tempinfo, Alarmpublic.REMIND_TABLE);	// 增加数据
			}
			dbInfo.closeDb();
		}
		Global.debug("\r\n saveDataRemind  ========9999===========");
		Alarmpublic.UpateAlarm(this);
		
		return flag;
	}
	
	// 保存提醒数据
	private Boolean saveDataRemindForMenu() {
		// TODO 自动生成的方法存根
		final Alarminfo tempinfo = new Alarminfo();  // 
		Boolean flag = false;
		
		tempinfo.year = gyear;
		tempinfo.month = gmonth;
		tempinfo.day = gday;
		
		tempinfo.hour = ghour;
		tempinfo.minute = gmin;
		
		tempinfo.filename = gFileName;
		tempinfo.path = gPath + "/" + gFileName;
		tempinfo.onoff = gonoff;
		
		Global.debug("saveDataRemind ===111=== tempinfo.onoff ==" + tempinfo.onoff);
	
		final GetDbInfo dbInfo = new GetDbInfo(RemindActivity.this);  // 打开数据库

		tempinfo._id = gID;
		dbInfo.update(tempinfo, Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
		Global.debug("\r\n saveDataRemindForMenu ------------------- \r\n");
		Alarmpublic.UpateAlarm(this);
		
		return flag;
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_START_RECORD){   // 开始录音
				startRecord();
			}else if(msg.what == Global.MSG_START_RECORD_NO){  
				startRecord_canel();		
			}
			else if(msg.what == Global.MSG_ONRESUM){
				onResume();
			}
			else if(msg.what == Global.MSG_SAVE_EXIT){
				saveExit();
			}
			else if(msg.what == Global.MSG_TIME_AFTER_YES){
				timeAfterYes();
			}
			else if(msg.what == Global.MSG_TIME_AFTER_NO){
				timeAfterNo();
			}
			else if(msg.what == Global.MSG_SAVE_UPDATE_YES){
				saveUpdateYes();
			}
			else if(msg.what == Global.MSG_SAVE_UPDATE_NO){
				saveUpdateNo();
			}
			
			super.handleMessage(msg);
		}
	};
	
	
	private void saveUpdateYes() {
	
		// TODO 自动生成的方法存根
		if(gCall_Flag == Global.REMIND_CALL_MENU){
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
			
			intent.putExtras(bundle); // 参数传递
			
			setResult(Global.REMIND_FLAG_ID,intent);	//返回界面
		}
		myPlayer.stopPlayback();
		finish();
	}
	
	private void saveUpdateNo() {
		if(gCall_Flag == Global.REMIND_CALL_MENU){
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
			
			intent.putExtras(bundle); // 参数传递
			
			setResult(Global.REMIND_FLAG_ID,intent);	//返回界面
		}
		myPlayer.stopPlayback();
		finish();	
	}
	private void timeAfterYes() {
		mMenuView.setSelectItem(1);
		onResume();
	}
	
	private void timeAfterNo() {
		if(gCall_Flag == Global.REMIND_CALL_MENU){
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
		
			intent.putExtras(bundle); // 参数传递
		
			setResult(Global.REMIND_FLAG_ID,intent);	//返回界面
		}
		myPlayer.stopPlayback();
		finish();
	}
	// 保存退出
	private void saveExit() {
		if(gCall_Flag == Global.REMIND_CALL_MENU){
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
			
			intent.putExtras(bundle); // 参数传递
			
			setResult(Global.REMIND_FLAG_ID,intent);	//返回界面
		}
		myPlayer.stopPlayback();
		finish();
		
	}
	
	// 
	private void startRecord_canel() {
		if(gCall_Flag == Global.REMIND_CALL_MENU){
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle
			
			intent.putExtras(bundle); // 参数传递
			Global.debug("\r\n ******* Global.REMIND_FLAG_ID ===" + Global.REMIND_FLAG_ID);
			setResult(Global.REMIND_FLAG_ID,intent);	//返回界面	
		}
		else if(gCall_Flag == Global.REMIND_CALL_ADD_MENU){
			Intent intent = new Intent();	//新建 INtent
			Bundle bundle = new Bundle();	//新建 bundle		
			intent.putExtras(bundle); // 参数传递
			Global.debug("\r\n ******* Global.REMIND_ADD_FLAG_ID ===" + Global.REMIND_ADD_FLAG_ID);
			setResult(Global.REMIND_ADD_FLAG_ID,intent);	//返回界面
		}
		myPlayer.stopPlayback();
		finish();
	}
}
