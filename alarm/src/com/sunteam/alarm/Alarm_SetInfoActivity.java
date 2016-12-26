package com.sunteam.alarm;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import com.sunteam.alarm.utils.Global;
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.receiver.Alarmpublic;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class Alarm_SetInfoActivity extends MenuActivity {

	private int gSelectID = 0;
	
	private int gSetID = 0;  // for 反显
	private int gInterfaceFlag = 0;
	private String  gfileName = null;
	private int gType = 0;  // for 反显
	private int gOnoff = 0;  // for 反显
	
	private boolean gonFileFlag = false;  // for 反显
	
	private ArrayList<String> mTemp = new ArrayList<String>(); // 显示
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Global.debug("Alarm_SetInfoActivity ==== \r\n");
		
		
		Intent intent=getIntent();	//获取Intent
		Bundle bundle=intent.getExtras();	//获取 Bundle
		
		gSelectID = bundle.getInt("ID");  // 获取 反显位置
		gInterfaceFlag = bundle.getInt("FLAG"); // 获取界面标志
		
		Global.debug("Alarm_SetInfoActivity ==== gSelectID = " + gSelectID);
		Global.debug("Alarm_SetInfoActivity ==== gInterfaceFlag = " + gInterfaceFlag);
		
		if(gInterfaceFlag == Global.ALARM_INFO_INTERFACE)  // 闹钟 详情界面
		{
			if(gSelectID == Global.ALARM_SET_MUSIC){
				gfileName = bundle.getString("FILENAME");
				
				mTitle = getResources().getString(R.string.music_title);
				gonFileFlag = false;
				mTemp = getPathAudioList();
				if(mTemp.size() <= 0){
					gonFileFlag = true;
					/*
					super.onCreate(savedInstanceState);
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
					mPromptDialog.show();
					
					mPromptDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							// TODO 自动生成的方法存根
							finish();
						}
					});
					
					return;
					*/
				}
				else{  // 有文件
					gSetID = mTemp.indexOf(gfileName);
					if(gSetID < 0){
						gSetID = 0;
					}
				}
			}
			else if(gSelectID == Global.ALARM_SET_TYPE){  // 闹钟类型
				mTitle = getResources().getString(R.string.type_title);
				gType = bundle.getInt("ALARMTYPE");
				mTemp.add(getResources().getString(R.string.type1));
				mTemp.add(getResources().getString(R.string.type2));
				mTemp.add(getResources().getString(R.string.type3));
				gSetID = gType;
			}
			else if(gSelectID == Global.ALARM_SET_ONOFF){ // 闹钟开关
				mTitle = getResources().getString(R.string.onoff_title);
				gOnoff = bundle.getInt("ONOFF");
				mTemp.add(getResources().getString(R.string.off));
				mTemp.add(getResources().getString(R.string.on));
				gSetID = gOnoff;
			}
		}
		else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){   // // 纪念日 详情界面
			if(gSelectID == Global.ANNIVERSARY_SET_MUSIC){
				gfileName = bundle.getString("FILENAME");
				
				mTitle = getResources().getString(R.string.music_title);
				gonFileFlag = false;
				mTemp = getPathAudioList();
				if(mTemp.size() <= 0){
					gonFileFlag = true;
					/*
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							// TODO 自动生成的方法存根
							finish();
						}
					});
					
					return;
					*/
				}
				else{
					gSetID = mTemp.indexOf(gfileName);
					if(gSetID < 0){
						gSetID = 0;
					}
				}
			}
			else if(gSelectID == Global.ANNIVERSARY_SET_ONOFF){
				mTitle = getResources().getString(R.string.onoff_title);
				gOnoff = bundle.getInt("ONOFF");
				mTemp.add(getResources().getString(R.string.off));
				mTemp.add(getResources().getString(R.string.on));
				gSetID = gOnoff;
			}
		}
		mMenuList = mTemp;
		selectItem = gSetID;
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_alarm__set_info);
	}
	
	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();
		
		if(true == gonFileFlag){
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
			mPromptDialog.show();
			mPromptDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					// TODO 自动生成的方法存根
					//finish();
				}
			});
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO 自动生成的方法存根
		if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
			keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
			keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
			keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
			keyCode == KeyEvent.KEYCODE_DPAD_UP)
		{
			if(mMenuList.size() <= 0){
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
				mPromptDialog.show();
				
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	// 按键处理 抬起处理
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		// 确认键
		if(keyCode == KeyEvent.KEYCODE_ENTER ||
				keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
		{
			if(mMenuList.size() <= 0){
				return true;
			}
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.set_ok));
			mPromptDialog.show();
			
			mPromptDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					Intent intent = new Intent();	//新建 INtent
					Bundle bundle = new Bundle();	//新建 bundle
					
					if(gInterfaceFlag == Global.ALARM_INFO_INTERFACE)  // 闹钟
					{
						if(gSelectID == Global.ALARM_SET_MUSIC){
							bundle.putString("FILENAME", mTemp.get(getSelectItem()));  // 进界面时的界面
						}
						else if(gSelectID == Global.ALARM_SET_TYPE || gSelectID == Global.ALARM_SET_ONOFF){
							bundle.putInt("ID", getSelectItem());
						}
					}
					else if(gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE){	// 纪念日
						if(gSelectID == Global.ANNIVERSARY_SET_MUSIC){
							bundle.putString("FILENAME", mTemp.get(getSelectItem()));  // 进界面时的界面
						}
						else if(gSelectID == Global.ANNIVERSARY_SET_ONOFF){
							bundle.putInt("ID", getSelectItem());
						}
					}
					Global.debug("\r\n [onKeyUp] ==== entern \r\n");
					Global.debug("\r\n [onKeyUp] ==gInterfaceFlag == " + gInterfaceFlag);
					Global.debug("\r\n [onKeyUp] ==gSelectID == " + gSelectID);
					bundle.putInt("FLAG", gInterfaceFlag);  // 进界面时的界面
					bundle.putInt("SELECTID", gSelectID);   // 进界面时的修改项
					intent.putExtras(bundle); // 参数传递
					setResult(Global.FLAG_CODE_SET_LIST,intent);	//返回界面

					finish();
				}
			});
			

			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
				keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
				keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
				keyCode == KeyEvent.KEYCODE_DPAD_UP)
			{
				if(mMenuList.size() <= 0){		
					return true;
				}
			}
		else if(keyCode == KeyEvent.KEYCODE_BACK){ // 返回
			
		}			
		return super.onKeyUp(keyCode, event);	
	}
	
	 // 显示list
	private ArrayList<String> getPathAudioList() 
	{
		ArrayList<String> temp = new ArrayList<String>();
		
		File mFile = new File(Alarmpublic.ALARM_FILE_PATH); // 获取路径内容
		Global.debug("can read === filePath =" + Alarmpublic.ALARM_FILE_PATH);
		if (mFile.canRead()) // 可读
		{
			
			// String fileName = mFile.getName();
			if (mFile.isDirectory()) // 是文件夹
			{
				// 顾虑条件
				FileFilter ff = new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						// TODO Auto-generated method stub
						return !pathname.isHidden();// 过滤隐藏文件
					}
				};

				Global.debug("is Directory === ");
				// 获取文件夹内文件
				File[] mFiles = mFile.listFiles(ff);

				temp.clear();

				Global.debug("is Directory === length =" + mFiles.length);
				/* 获取文件列表 */
					// 获取文件列表
					for (File mCurrentFile : mFiles) {
						if (mCurrentFile.getName().equals("LOST.DIR")) // 去除LOST.DIR
						{
							continue;
						}
						File mFile2 = new File(mCurrentFile.getPath()); // 获取路径内容

						if (mFile2.isFile()) {  // 是文件							

							String prefix = getExtensionName(mCurrentFile.getName());  // 获取文件名 后缀
							if(prefix.equals("mp3") || prefix.equals("MP3")||
									prefix.equals("wav") || prefix.equals("WAV")||
									prefix.equals("wma") || prefix.equals("WMA"))
							{								
								temp.add(mCurrentFile.getName());
							}
						}
					}
			}
		}
		
		return temp;
	}
	
	/* 
	 *  获取文件扩展名 
	 *  
	 */   
    public static String getExtensionName(String filename) {    
        if ((filename != null) && (filename.length() > 0)) {
        	
            int dot = filename.lastIndexOf('.');    
            if ((dot  > -1) && (dot < (filename.length() - 1))) {    
                return filename.substring(dot + 1);    
            }    
        }    
        return filename;    
    }	
}
