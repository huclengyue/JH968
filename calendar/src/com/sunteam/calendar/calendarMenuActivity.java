package com.sunteam.calendar;

import java.util.ArrayList;

import com.sunteam.calendar.constant.Global;
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.dao.Alarminfo;
import com.sunteam.dao.GetDbInfo;
import com.sunteam.receiver.Alarmpublic;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class calendarMenuActivity extends MenuActivity{
	// 全局变量
//	private FrameLayout mFlContainer = null;  // 帧 数据
//	private MainView mMenuView = null;			// 主界面

	@SuppressWarnings("unused")
	private int gYear = 0;   // 年
	@SuppressWarnings("unused")
	private int gMonth = 0;  // 月
	@SuppressWarnings("unused")
	private int gDay = 0;	// 日
	
	private int gSelectId = 0;	// 
	
	private int ADD_REMIND_ID = 0;  // 增加提醒
	private int REMIND_ID = 1;  // 查看提醒
	private int DEL_REMIND_ID = 2;  // 删除提醒
	private int DELALL_REMIND_ID = 3;  // 删除提醒
	
	private int gInterfaceflag = 0;  // 界面标志
	private int INTERFACE_MENU = 0;  // 菜单界面
	private int INTERFACE_RENID_LIST = 1;  // 查看 提醒界面
	private int INTERFACE_DEL_REMIND = 2;  // 菜单 界面
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//setContentView(R.layout.activity_menu);
		
		Intent intent=getIntent();	// 获取Intent
		Bundle bundle=intent.getExtras();	// 获取Bundle
		
		gYear = bundle.getInt("YEAR");  // 获取 年
		gMonth = bundle.getInt("MONTH");  // 获取 月
		gDay = bundle.getInt("DAY");  // 获取 月
		
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
		
		mTitle = getResources().getString(R.string.title_menu);
		gInterfaceflag = INTERFACE_MENU;
		super.onCreate(savedInstanceState);
	}
	
	// 键按下
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	
		if(keyCode == KeyEvent.KEYCODE_BACK)  // 返回按键
		{
			if((gInterfaceflag == INTERFACE_RENID_LIST) || (gInterfaceflag == INTERFACE_DEL_REMIND)) // 查看提醒 界面
			{
				mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
				
				mTitle = getResources().getString(R.string.title_menu);
				setListData(mMenuList);
				setTitle(mTitle);;
				if(gInterfaceflag == INTERFACE_RENID_LIST){
					mMenuView.setSelectItem(1);
				}
				else{
					mMenuView.setSelectItem(2);
				}
				gInterfaceflag = INTERFACE_MENU;
				
				onResume();
				//finish();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	// 键按下
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)  // 返回按键
		{
//			return true; // zhd@20161025 为何忽略返回键？不然如何跳出功能菜单界面？
		}
		return super.onKeyDown(keyCode, event);
	}
	// 重新实现 方法  ok键处理
	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		Global.debug("setResultCode =============gInterfaceflag =" + gInterfaceflag);
		gSelectId = selectItem;
		if(gInterfaceflag == INTERFACE_MENU)  // 菜单界面
		{
			if(selectItem == ADD_REMIND_ID){   // 增加提醒
				Intent intent = new Intent();			
				Bundle bundle = new Bundle();//  
					
				bundle.putInt("CALLID", Global.REMIND_CALL_ADD_MENU);
				
				//intent.setClass(MainActivity.this, MenuActivity.class);
				intent.putExtras(bundle); // 传入参数
				intent.setClass(this, RemindActivity.class);
				
				startActivityForResult(intent , Global.REMIND_ADD_FLAG_ID);  // 设置标志
			}
			else if(selectItem == REMIND_ID)  // 查看提醒
			{
				mMenuList = getDbdata();
				if(mMenuList != null){
					setListData(mMenuList);
					mTitle = getResources().getString(R.string.list_remind);
					setTitle(mTitle);
					mMenuView.setSelectItem(0);
					gInterfaceflag = INTERFACE_RENID_LIST;
					onResume();
				}
				else{
					//TtsUtils.getInstance().speak(getResources().getString(R.string.no_remind));	
					PromptDialog mPrompt = new PromptDialog(this, getResources().getString(R.string.no_remind));
					mPrompt.show();
					mPrompt.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							// TODO 自动生成的方法存根
							mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
							
							mTitle = getResources().getString(R.string.title_menu);
							setListData(mMenuList);
							setTitle(mTitle);;
							gInterfaceflag = INTERFACE_MENU;
							onResume();
						}
					});
				}
			}
			else if(selectItem == DEL_REMIND_ID) // 删除提醒
			{
				mMenuList = getDbdata();
				if(mMenuList != null){
					setListData(mMenuList);
					mTitle = getResources().getString(R.string.del_remind);
					setTitle(mTitle);
					mMenuView.setSelectItem(0);
					gInterfaceflag = INTERFACE_DEL_REMIND;
					onResume();
				}else {
					TtsUtils.getInstance().speak(getResources().getString(R.string.no_remind));	
					PromptDialog mPrompt = new PromptDialog(this, getResources().getString(R.string.no_remind));
					mPrompt.show();
					mPrompt.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							// TODO 自动生成的方法存根
							mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
							
							mTitle = getResources().getString(R.string.title_menu);
							setListData(mMenuList);
							setTitle(mTitle);;
							gInterfaceflag = INTERFACE_MENU;	
							//onResume();
						}
					});
				}
			}
			else if(selectItem == DELALL_REMIND_ID){ // 删除所有提醒
				
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.ask_delall_Remind),
																		getResources().getString(R.string.ok),
																		getResources().getString(R.string.canel));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {
					
					@Override
					public void doConfirm() {
						// TODO 自动生成的方法存根
						delAllDbdata();
						
						PromptDialog mPromptDialog=new PromptDialog(calendarMenuActivity.this, 
																	getResources().getString(R.string.delall_Remind));
						mPromptDialog.show();
						mPromptDialog.setPromptListener(new PromptListener() {
							
							@Override
							public void onComplete() {
								// TODO 自动生成的方法存根
								onResume();
							}
						});
					}
					
					@Override
					public void doCancel() {
						// TODO 自动生成的方法存根
						
					}
				});
				
			}
		}
		else if(gInterfaceflag == INTERFACE_RENID_LIST) // 查看提醒 界面
		{
			Alarminfo tempinfo = new Alarminfo();  // 
			GetDbInfo dbInfo = new GetDbInfo(this);  // 打开数据库
			ArrayList<Alarminfo> alarminfos = new ArrayList<Alarminfo>();
			
			alarminfos = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
			dbInfo.closeDb();
			tempinfo = alarminfos.get(gSelectId);
			
			Intent intent = new Intent();			
			Bundle bundle = new Bundle();//  
				
			bundle.putInt("CALLID", Global.REMIND_CALL_MENU);
			
			bundle.putInt("ID", tempinfo._id);
			bundle.putInt("YEAR", tempinfo.year);
			bundle.putInt("MONTH", tempinfo.month);
			bundle.putInt("DAY", tempinfo.day);
			
			bundle.putInt("HOUR", tempinfo.hour);
			bundle.putInt("MINUTE", tempinfo.minute);
			
			bundle.putInt("ONOFF", tempinfo.onoff);
			
			bundle.putString("FILENAME", tempinfo.filename);
			bundle.putString("PATH", tempinfo.path);

			//intent.setClass(MainActivity.this, MenuActivity.class);
			intent.putExtras(bundle); // 传入参数
			intent.setClass(this, RemindActivity.class);
			
			startActivityForResult(intent , Global.REMIND_FLAG_ID);  // 设置标志
			
		}
		else if(gInterfaceflag == INTERFACE_DEL_REMIND){   // 删除列表
			ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.ask_del),
																	getResources().getString(R.string.ok),
																	getResources().getString(R.string.canel));
			mConfirmDialog.show();
			mConfirmDialog.setConfirmListener(new ConfirmListener() {
				
				@Override
				public void doConfirm() {
					// TODO 自动生成的方法存根
					delDbdata(gSelectId);
					mMenuList = getDbdata();
					setListData(mMenuList);
					int selectid = 0;
					Global.debug("\r\n mMenuList.size() =============="+mMenuList.size());
					Global.debug("\r\n gSelectId =============="+gSelectId);
					if(gSelectId > (mMenuList.size() - 1)){
						selectid = mMenuList.size() - 1;
					}
					else{
						selectid = gSelectId;
					}
					Global.debug("\r\n selectid =============="+selectid);
					mMenuView.setSelectItem(selectid);
					//onResume();
				}
				
				@Override
				public void doCancel() {
					// TODO 自动生成的方法存根
					//onResume();
				}
			});
			
		}
	}
	// 获取list
	private ArrayList<String> getDbdata()
	{
		ArrayList<String> list = new ArrayList<String>();
		String temp = null;
		//String[] guser_List = new String[]; // list信息
		Alarminfo tempinfo = new Alarminfo();  // 
		GetDbInfo dbInfo = new GetDbInfo(this);  // 打开数据库
		ArrayList<Alarminfo> alarminfos = new ArrayList<Alarminfo>();
		
		alarminfos = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
		int max_num = dbInfo.getCount(Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
		Global.debug("getDbdata === max_num = " + max_num);
		if(max_num > 0)  // 有数据
		{
			for(int i= 0; i < max_num; i++)
			{
				tempinfo = alarminfos.get(i);
				
				temp = null;
				temp = tempinfo.year + "-";
				if(tempinfo.month < 10){
					
					temp += "0"; 
				}
				temp += tempinfo.month + "-";
				if(tempinfo.day < 10){
					temp += "0"; 
				}
				temp += tempinfo.day+"  ";
				
				if(tempinfo.hour< 10)
				{
					temp += "0" + tempinfo.hour + ":";
				}
				else{
					temp += tempinfo.hour + ":";
				}
				
				if(tempinfo.minute < 10){
					temp +="0" + tempinfo.minute ;
				}
				else{
					temp +=tempinfo.minute ;
				}
				temp += "  ";
				Global.debug("[***]tempinfo.onoff =====" + tempinfo.onoff);
				if(tempinfo.onoff == Alarmpublic.ALARM_OFF){
					temp += getResources().getString(R.string.remind_off);
				}
				else{
					temp += getResources().getString(R.string.remind_on);
				}
				
				list.add(temp);
				Global.debug("22222 -->"   + temp);
			}
			return list;
		}
		else{
			return null;
		}
	}
	
	// 获取list
	private void delDbdata(int id)
	{

		//String[] guser_List = new String[]; // list信息
		Alarminfo tempinfo = new Alarminfo();  // 
		GetDbInfo dbInfo = new GetDbInfo(this);  // 打开数据库
		ArrayList<Alarminfo> allData = new ArrayList<Alarminfo>();
		
		//int tempid = 0;
		int maxId = dbInfo.getCount(Alarmpublic.REMIND_TABLE);  // 获取数据条数
		allData = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
		if(maxId > 0)
		{
			for(int i= 0; i < maxId; i++)
			{
				tempinfo = allData.get(i);//.find(i, Alarmpublic.REMIND_TABLE);
				Global.debug("i == "+ i + " id =="+ id);
				if(id == i){
					dbInfo.deteleForOne(tempinfo._id, Alarmpublic.REMIND_TABLE);
				//	TtsUtils.getInstance().speak(getResources().getString(R.string.no_remind));	
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.del_ok));
					mPromptDialog.show();
					break;
				}
			}
		}
		else{
			//TtsUtils.getInstance().speak(getResources().getString(R.string.no_remind));	
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_remind));
			mPromptDialog.show();
		}	
	}
	
	// 所处所有数据
	private void delAllDbdata()
	{
		GetDbInfo dbInfo = new GetDbInfo(this);  // 打开数据库
		
		dbInfo.detele(Alarmpublic.REMIND_TABLE); //getAllData(Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
	}
	//  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Global.debug("\r\n onActivityResult requestCode == " + requestCode + " resultCode =="+resultCode);
		if(requestCode == Global.REMIND_FLAG_ID && resultCode == Global.REMIND_FLAG_ID){
			
			mMenuList = getDbdata();
			if(mMenuList != null){
				setListData(mMenuList);
				mTitle = getResources().getString(R.string.list_remind);
				setTitle(mTitle);
				mMenuView.setSelectItem(gSelectId);
				
				onResume();
			}
			else{
				//TtsUtils.getInstance().speak(getResources().getString(R.string.no_remind));	
				PromptDialog mPrompt = new PromptDialog(this, getResources().getString(R.string.no_remind));
				mPrompt.show();
				mPrompt.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根
						mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
						
						mTitle = getResources().getString(R.string.title_menu);
						setListData(mMenuList);
						setTitle(mTitle);;
						gInterfaceflag = INTERFACE_MENU;
						onResume();
					}
				});
			}
		} 
		// 增加提醒
		else if(requestCode == Global.REMIND_ADD_FLAG_ID && resultCode == Global.REMIND_ADD_FLAG_ID){
			mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
			
			mTitle = getResources().getString(R.string.title_menu);
			gInterfaceflag = INTERFACE_MENU;
			
			setListData(mMenuList);
			setTitle(mTitle);
			mMenuView.setSelectItem(0);
			onResume();
		}
			
	}
}
