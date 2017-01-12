package com.sunteam.music;

import java.util.ArrayList;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.SharedPrefUtils;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.music.dao.GetDbInfo;
import com.sunteam.music.dao.MusicInfo;
import com.sunteam.music.utils.Global;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class MenuPalyMenuActivity extends MenuActivity {
	String gPath = null;
	String gFileName = null;
	private int gPlay_Mode;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mMenuList =ArrayUtils.strArray2List(getResources().getStringArray(R.array.play_menu_play));
		mTitle = getResources().getString(R.string.Menu_Title);
		
		// 获取传递参数
		Intent intent=getIntent();	//获取Intent
		Bundle bundle=intent.getExtras();	//获取 Bundle
		
		gPath = bundle.getString("PATH");  // 获取修改位置
		gFileName = bundle.getString("FILENAME"); // 获取用户ID
		
		gPlay_Mode = SharedPrefUtils.getSharedPrefInt(this, Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_MODE, gPlay_Mode);
		selectItem = gPlay_Mode;
		
		super.onCreate(savedInstanceState);
		
		//mMenuView.setSelected(selected);
		Global.debug("\r\n [*************] gPlay_Mode ===" + gPlay_Mode);
		mMenuView.setSelectItem(gPlay_Mode);
		
//		setContentView(R.layout.music_menu_paly_menu);
	}
	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();
		Global.debug("\r\n [******www*******] gPlay_Mode ===" + gPlay_Mode);
	//	mMenuView.setSelectItem(gPlay_Mode);
	}
	// 按键抬起处理
	@SuppressWarnings("deprecation")
	public boolean onKeyUp(int keyCode, KeyEvent event) { 
		
		if((keyCode == KeyEvent.KEYCODE_ENTER||keyCode == KeyEvent.KEYCODE_DPAD_CENTER)){
			if(Global.MENU_PLAY_ADD_FILE == getSelectItem())  // 添加到我的收藏
			{
				PromptDialog mDialog = null;
				if(false == MusicAddSaveList()){  // 文件重复
					mDialog = new PromptDialog(this, getResources().getString(R.string.file_exists));	
				}
				else {
					mDialog = new PromptDialog(this, getResources().getString(R.string.file_add_ok));
				}
				mDialog.show();
				mDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
					/*	// TODO 自动生成的方法存根
						Intent intent = new Intent();
						Bundle bundle = new Bundle();	//新建 bundl
						bundle.putInt("selectItem", getSelectItem());
						intent.putExtras(bundle); // 参数传递
						setResult(Global.PLAY_MENU_PLAY_FLAG, intent);
						finish();*/
						mHandler.sendEmptyMessage(Global.MSG_MENU_BACK);
					}
				});
			}
			else if (Global.MENU_PLAY_MODE_ALL == getSelectItem() ||
					Global.MENU_PLAY_MODE_SINGLE == getSelectItem()||
					Global.MENU_PLAY_MODE_RAND == getSelectItem()) { // 全部循环
				
			//	int selectItem = getSelectItem() ;
				SharedPrefUtils.setSharedPrefInt(this,Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_MODE, getSelectItem() );
				Global.debug("------" + getResources().getString(R.string.set_ok));
				PromptDialog mpDialog = new PromptDialog(this, getResources().getString(R.string.set_ok));
				mpDialog.show();
				mpDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						mHandler.sendEmptyMessage(Global.MSG_MENU_BACK);
					}
				});
				Global.debug("---qqq---" + getResources().getString(R.string.set_ok));
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	
	// 增加我的最爱 记录
	public boolean MusicAddSaveList()
	{
		MusicInfo musicinfo = new MusicInfo();   //创建 结构体
		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
		
//		String Filename = gPlayListName.get(currentIndex);
 //   	String FilePath = gPlayListPaths.get(currentIndex);
		String Filename = gFileName;
    	String FilePath = gPath;//gPlayListPaths.get(currentIndex);
    	int max_id = dbMusicInfo.getMaxId(Global.SAVE_LIST_ID);
    	Global.debug("\r\n MusicAddPlayList ===== max_id = " + max_id);
    	if(max_id <= 0){  // 无数据
    		musicinfo._id = 1;
    		musicinfo.path = FilePath;
    		musicinfo.filename = Filename;
    		
    		dbMusicInfo.add(musicinfo , Global.SAVE_LIST_ID);
    		//TtsUtils.getInstance().speak(getResources().getString(R.string.file_add_ok));
    	}   
    	else{
    		boolean flag = false;
    		ArrayList<MusicInfo> mMusicInfo = new ArrayList<MusicInfo>();
    		mMusicInfo = dbMusicInfo.GetAllData(Global.SAVE_LIST);
    		Global.debug("\r\n mMusicInfo.size() === "+ mMusicInfo.size());	
    		for(int i = 0; i < mMusicInfo.size(); i++)
    		{
    			musicinfo = mMusicInfo.get(i);
    			Global.debug("\r\n musicinfo.path === "+ musicinfo.path + " FilePath =="+FilePath);	
    			if(FilePath.equals(musicinfo.getPath())){ // 文件重复
    				flag = true;
    				break;
    			}
    		}
    		
    		if(true == flag){  // 文件重复
    			//TtsUtils.getInstance().speak(getResources().getString(R.string.file_exists));
    			dbMusicInfo.closeDb();
    			return false;
    		}
    		else{  // 添加文件
	    		musicinfo._id = max_id + 1;
	    		musicinfo.path = FilePath;
	    		musicinfo.filename = Filename;
	    		dbMusicInfo.add(musicinfo, Global.SAVE_LIST_ID);
//	    		TtsUtils.getInstance().speak(getResources().getString(R.string.file_add_ok));
	    		dbMusicInfo.closeDb();
    		}
    	}
    	return true;
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_MENU_BACK){   // 音乐播放结束消息
				goBack();
			}
			else if(msg.what == Global.MSG_RESUME){
				onResume();
			}
			
			super.handleMessage(msg);
		}
	};
	
	private void goBack() {

		Global.debug("---222---" + getResources().getString(R.string.set_ok));
		Intent intent = new Intent();
		Bundle bundle = new Bundle();	//新建 bundl
		bundle.putInt("selectItem", getSelectItem());
		intent.putExtras(bundle); // 参数传递
		setResult(Global.PLAY_MENU_PLAY_FLAG, intent);
		finish();
	}	
}
