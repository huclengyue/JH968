package com.sunteam.music;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.menu.menulistadapter.ShowView;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.SharedPrefUtils;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.music.dao.GetDbInfo;
import com.sunteam.music.dao.MusicInfo;
import com.sunteam.music.utils.Global;
import com.sunteam.music.utils.Pinyin4jUtils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends MenuActivity implements ShowView {
	// 全局保存文件名
	
	boolean d = false;
	
	// 图片 信息
	private Bitmap imFile;
	private Bitmap imFoler;
	private Bitmap imSdcard;
	private Bitmap imUsb;
	private Bitmap imPhone;

	// 全局保存路径
	private static  ArrayList<String> gFileName = null;   // 列表文件名
	private static ArrayList<String> gFilePaths = null;		// 列表文件路径
	//private static String gFilePaths = null;		// 列表文件路径

	// private String[] gMenuName = null;
	
	private int[] gSelecyId = null;  // 目录反显项
	
	private static ArrayList<String> gPath = null;  // 记录路径
	private static ArrayList<String> gName = null;  // 记录文件名
	
	private int gPathNum = 0;  // 目录数
	

	private static ArrayList<String> gPlayListName = null;   // 列表文件名
	private static ArrayList<String> gPlayListPaths = null;		// 列表文件路径
	
	
	private int intface_flag = 0;  // 界面标志
	
	private int MAIN_INTFACE = 0;  // 主界面
	
	private int DIRECTORY_INTFACE = 1;  // 目录浏览
	private int FAVORITE_INTFACE = 2;  // 我的最爱界面
	private int RECENTPLAY_INTFACE = 3;  // 最近播放界面
	
	private int DIRECTORY_1_INTFACE = 4;  // 目录浏览的界面 浏览 最近播放界面
	
	private int FAVORITE_1_INTFACE = 5;  // 目录浏览的界面 浏览 最近播放界面
	
	private int gId = 0;  // 菜单回来会反显
	
	//private int _INTFACE = 0;
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		intface_flag = MAIN_INTFACE ;  // 记录在主界面
	//	gFileName = new ArrayList<String>();
		//gFilePaths = new ArrayList<String>();

		// gMenuName = new String[];
		gPath = new ArrayList<String>();
		gName = new ArrayList<String>();
		gSelecyId = new int[Global.MAX_RANK];// 目录最大级数

		for (int i = 0; i < Global.MAX_RANK; i++) {
			gSelecyId[i] = 0;
		}

		gFileName = new ArrayList<String>();
	
		gFilePaths = new ArrayList<String>();
		
		gPlayListName = new ArrayList<String>();   // 播放列表文件名
		gPlayListPaths = new ArrayList<String>();		//播放 列表文件路径
		
		imFile = BitmapFactory.decodeResource(this.getResources(), R.drawable.file);
		imFoler = BitmapFactory.decodeResource(this.getResources(), R.drawable.folder);
		imSdcard = BitmapFactory.decodeResource(this.getResources(), R.drawable.sdcard);
		imPhone = BitmapFactory.decodeResource(this.getResources(), R.drawable.phone);
		imUsb = BitmapFactory.decodeResource(this.getResources(), R.drawable.usbdisk);
		// 清空数据
		gFileName.clear();
		gFilePaths.clear();
		
		Global.FristString =  Global.GetPalyFristData(this);   // 获取最后一次伯村的路径
		Global.debug("MainActivity ====1112222= Global.FristString ==" + Global.FristString);
		//mTitle = getResources().getString(R.string.app_name);
		mTitle = this.getResources().getString(R.string.title_main);
		gFileName = ArrayUtils.strArray2List (getResources().getStringArray(R.array.main_list));
		mMenuList = gFileName; // ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
		
		gFilePaths.add(Global.DIRECTORY_PATH) ;  // 目录浏览 
		gFilePaths.add(Global.FAVORITE_PATH) ;	// 我的最爱
		gFilePaths.add(Global.RECENTPLAY_PATH) ;  // 最近播放
		
		Global.debug("MainActivity ====1111= \r\n");
		selectItem = SharedPrefUtils.getSharedPrefInt(this, Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_SELECT, selectItem);
		 
		Global.debug("MainActivity ====1112222= selectItem =="+selectItem);
		super.onCreate(savedInstanceState);
		
		Global.debug("MainActivity ====2222= \r\n");
		mMenuView.setShowView(this);
		Global.debug("MainActivity ====3333= \r\n");
		registerTFcardPlugReceiver();
	}
// 界面刷新
	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();	
	}
	@Override
	protected void onPause() {
		// TODO 自动生成的方法存根
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		unregisterReceiver(tfCardPlugReceiver);
		if (TtsUtils.getInstance() != null) {
			TtsUtils.getInstance().destroy();
		}
	}
	// 键抬起
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_MENU == keyCode) {  // 按键 menu	
					
			if(intface_flag == FAVORITE_INTFACE){  // 在我的最爱界面
				gId = getSelectItem();
				//startMenu(0, gFilePaths.get(getSelectItem()), gFileName.get(getSelectItem()));
				startMenu(0, gFilePaths.get(getSelectItem()), gFileName.get(getSelectItem()));
			}
			else if(intface_flag == DIRECTORY_1_INTFACE){  // 可以添加
				MusicAddSaveList();
			}
			else if (intface_flag == RECENTPLAY_INTFACE)
			{
				gId = getSelectItem();
				//startPlayMenu(0, gFilePaths.get(getSelectItem()), gFileName.get(getSelectItem()));
				startPlayMenu(0, gFilePaths.get(getSelectItem()), gFileName.get(getSelectItem()));
			}
			return true;
		}
		else if(KeyEvent.KEYCODE_ENTER == keyCode ||   // 按键 Enter
				KeyEvent.KEYCODE_DPAD_CENTER == keyCode)
		{
			Global.debug("[KEYCODE_ENTER]    gFilePaths.size() ===="+ gFilePaths.size());
			if(mMenuList.size() <= 0){
				return true;
			}
			keyupEnter();  // enter 按键处理
			
			return  true;
		}
		else if (KeyEvent.KEYCODE_BACK == keyCode) {
			Global.debug("\r\n [KEYCODE_BACK]  intface_flag ++++++++++=== "+ intface_flag);
			
			keyupBack();

			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_0){
			testrawplay();
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	@SuppressWarnings("unchecked")
	private void keyupBack() {
		// TODO 自动生成的方法存根
		if(MAIN_INTFACE ==  intface_flag){  // 主界面 直接退出
			finish();
		}
		else if(FAVORITE_INTFACE ==  intface_flag ||    // 二级目录
				DIRECTORY_INTFACE ==  intface_flag ||
				RECENTPLAY_INTFACE ==  intface_flag)
		{
			gFileName.clear();
			gFilePaths.clear();
			
			//mTitle = getResources().getString(R.string.app_name);
			//mTitle = this.getResources().getString(R.string.title_main);
			gFileName = ArrayUtils.strArray2List (getResources().getStringArray(R.array.main_list));
			mMenuList = gFileName; // ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
			
			gFilePaths.add(Global.DIRECTORY_PATH) ;  // 目录浏览 
			gFilePaths.add(Global.FAVORITE_PATH) ;	// 我的最爱
			gFilePaths.add(Global.RECENTPLAY_PATH) ;  // 最近播放
			
			setListData(gFileName);
		
			if(FAVORITE_INTFACE ==  intface_flag){  // 我的收藏
				mMenuView.setSelectItem(1);
			}
			else if(DIRECTORY_INTFACE == intface_flag){  // 目录浏览
				mMenuView.setSelectItem(0);
			}
			else if(RECENTPLAY_INTFACE == intface_flag){  // 最近播放
				mMenuView.setSelectItem(2);
			}
			setTitle(getResources().getString(R.string.title_main));
			intface_flag = MAIN_INTFACE;
			super.onResume();
		}
		else if(DIRECTORY_1_INTFACE == intface_flag){  // 目录浏览
			Global.debug("[KEYCODE_BACK] [11] gPathNum === "+ gPathNum);
			if(gPathNum > 1){  // 多层目录返回
				
				for(int i = 0; i < gPathNum; i++)
				{
					Global.debug("【………………】gPath.get(i) ==" + gPath.get(i));
				}
				
				gPathNum --;
				
				Global.debug("[KEYCODE_BACK] [11] gPath.get(gPathNum) === "+ gPath.get(gPathNum-1));
				showList(gPath.get(gPathNum -1), gName.get(gPathNum-1));
				Global.debug("\r\n [KEYCODE_BACK] [12] gSelecyId[gPathNum] === "+ gSelecyId[gPathNum]);
				mMenuView.setSelectItem(gSelecyId[gPathNum]);
				
				gPath.remove(gPathNum);
				gName.remove(gPathNum);
				onResume();
			}
			else if( 1 == gPathNum){  // 在根目录
				Global.debug("[KEYCODE_BACK] [22] gPathNum === "+ gPathNum);
				
				intface_flag = DIRECTORY_INTFACE;    // 进入目录浏览界面
				gFileName.clear();
				gFilePaths.clear();
				gFileName = ArrayUtils.strArray2List (getResources().getStringArray(R.array.main1_list));
				
				//gFilePaths.add(Global.DIRECTORY_PATH) ;  // 目录浏览 
				//gFilePaths.add(Global.FAVORITE_PATH) ;	// 我的最爱
				//gFilePaths.add(Global.RECENTPLAY_PATH) ;  // 最近播放
				
				gFilePaths.add(Global.ROOT_PATH) ;  // 目录浏览 
				gFilePaths.add(Global.USER_PATH) ;	// 我的最爱
				gFilePaths.add(Global.USB_PATH) ;  // 最近播放
				
				setTitle(getResources().getString(R.string.dir_list));
				mMenuList = gFileName;
				setListData(mMenuList);	
				gPathNum = 0;
				mMenuView.setSelectItem(gSelecyId[gPathNum]);
				Global.debug("[KEYCODE_BACK] [333] gSelecyId[gPathNum] == " + gSelecyId[gPathNum]);
				gPath.clear();
				gName.clear();
				gPathNum = 0;
				
				super.onResume();
			}
		}
		else if(intface_flag == FAVORITE_1_INTFACE) // 我的收藏 文件夹目录
		{
			String name = null;
			gPathNum --;
			
			name = gName.get(gPathNum);
			Global.debug("[KEYCODE_BACK] [11] gPath.get(gPathNum) === "+ gPath.get(gPathNum));
			Global.debug("[KEYCODE_BACK] [11] gPathNum === "+ gPathNum );
			if(gPathNum >= 1){
				showList(gPath.get(gPathNum -1), gName.get(gPathNum-1));
			}
			else{
				intface_flag = FAVORITE_INTFACE; 
				showDbList(Global.SAVE_LIST_ID);
			}
			mMenuView.setSelectItem(gFileName.indexOf(name));
			
			gPath.remove(gPathNum);
			gName.remove(gPathNum);
			onResume();
		}
	}

	// 按键 entern 的处理
	private void keyupEnter() {
		Global.debug("\r\n[keyupEnter]======= intface_flag = " + intface_flag);
		if(MAIN_INTFACE ==  intface_flag){  // 主界面
			if(getSelectItem()  == Global.MAIN_DIR_ID){  // 选中第一项 
				SharedPrefUtils.setSharedPrefInt(this,Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_SELECT, getSelectItem() );
				intface_flag = DIRECTORY_INTFACE;    // 进入目录浏览界面

				setTitle(getSelectItemContent());
				
				gFileName.clear();
				gFilePaths.clear();
				
				gFilePaths.add(Global.ROOT_PATH);  // 内存
				gFilePaths.add(Global.USER_PATH);  // 存储卡
				gFilePaths.add(Global.USB_PATH);   // 优盘
				
				gFileName = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main1_list));
				mMenuList = gFileName;
				setListData(mMenuList);
				
				Global.debug("\r\n [1]Global.FristString ==="+ Global.FristString );
				
				if( (Global.FristString != null) && (Global.FristString.contains(Global.ROOT_PATH))){
					mMenuView.setSelectItem(0);
					Global.debug("\r\n [1] setSelectItem(0)===" );
				}
				else if((Global.FristString != null) && (Global.FristString.contains(Global.USER_PATH))){
					mMenuView.setSelectItem(1);
					Global.debug("\r\n [1] setSelectItem(1)===" );
				}
				else if((Global.FristString != null) && (Global.FristString.contains(Global.USB_PATH))){
					mMenuView.setSelectItem(2);
					Global.debug("\r\n [1] setSelectItem(2)===" );
				}
				mHandler.sendEmptyMessage(Global.MSG_RESUME);
			}
			else if (getSelectItem()  == Global.MAIN_SAVE_ID) { // 选中第二项  我的最爱界面 显示列表
				SharedPrefUtils.setSharedPrefInt(this,Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_SELECT, getSelectItem() );
				if(true == showDbList(Global.SAVE_LIST_ID)){  // 有数据
					intface_flag = FAVORITE_INTFACE; 
				}
				else{  //无数据
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							mHandler.sendEmptyMessage(Global.MSG_RESUME);
						}
					});
				}		
			}
			else if(getSelectItem()  == Global.MAIN_PLAY_ID){  // 选中第三项   最近播放界面 显示列表
				SharedPrefUtils.setSharedPrefInt(this,Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_SELECT, getSelectItem() );
				if(true ==  showDbList(Global.PLAY_LIST_ID)){
					intface_flag = RECENTPLAY_INTFACE;  
				}
				else{  //无数据
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							// TODO 自动生成的方法存根
							mHandler.sendEmptyMessage(Global.MSG_RESUME);
						}
					});
				}
			}
		}
		// 目录浏览界面
		else if(DIRECTORY_INTFACE ==  intface_flag)  
		{	
			if(getSelectItem()  == Global.MAIN_LOCAL_ID){  // 选中第一项  进内存
				
				// 保存反显 返回时 需要
				gSelecyId[gPathNum] = getSelectItem();
				gPath.add(Global.ROOT_PATH);
				gName.add(getSelectItemContent());
				gPathNum++; //目录树 +1
				
				//gFileName = ArrayUtils.strArray2List (getResources().getStringArray(R.array.main1_list));
				//setTitle(getSelectItemContent());
				//setListData(ArrayUtils.strArray2ListString(gFileName));
				
				showList(Global.ROOT_PATH, getSelectItemContent());
				
				intface_flag = DIRECTORY_1_INTFACE;    // 进入目录浏览界面	
				mHandler.sendEmptyMessage(Global.MSG_RESUME);
			}
			else if (getSelectItem() == Global.MAIN_TF_ID) { // 选中第二项  存储卡
				if(true == getExtSDUSBPathHave(Global.USER_PATH))  // 存储卡存在
				{
					gSelecyId[gPathNum] = getSelectItem();
					gPath.add(Global.USER_PATH);
					gName.add(getSelectItemContent());
					gPathNum++; //目录树 +1
					
					showList(Global.USER_PATH, getSelectItemContent());
					intface_flag = DIRECTORY_1_INTFACE;
					mHandler.sendEmptyMessage(Global.MSG_RESUME);
				}
				else{   //tf卡不存在
					//TtsUtils.getInstance().speak(getResources().getString(R.string.error_no_sdcard));
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.error_no_sdcard));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							mHandler.sendEmptyMessage(Global.MSG_RESUME);
						}
					});
				}
			}
			else if(getSelectItem()  == Global.MAIN_UDISK_ID){  // 选中第三项   优盘
				
				if(true == getExtSDUSBPathHave(Global.USB_PATH))  // 优盘存在
				{
					gSelecyId[gPathNum] = getSelectItem();
					gPath.add(Global.USB_PATH);
					gName.add(getSelectItemContent());
					gPathNum++; //目录树 +1
					
					showList(Global.USB_PATH, getSelectItemContent());
					intface_flag = DIRECTORY_1_INTFACE;
					
				}
				else{   //tf卡不存在
					//TtsUtils.getInstance().speak(getResources().getString(R.string.error_no_udisk));
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.error_no_udisk));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							mHandler.sendEmptyMessage(Global.MSG_RESUME);
						}
					});
				}
			}
		}
		else if(FAVORITE_INTFACE ==  intface_flag || FAVORITE_1_INTFACE == intface_flag)   // 我的最爱界面
		{
			Global.debug("[@@@]    gFilePaths.size() ===="+ gFilePaths.size());
			//File mFile = new File(gFilePaths.get(getSelectItem())); // 获取路径内容
			Global.debug("\r\n[MainActivity] gFilePaths.get(getSelectItem()) ==1= " + gFilePaths.get(getSelectItem()));
			Global.debug("\r\n [MainActivity] gFilePaths.get(getSelectItem()) =2== " + gFilePaths.get(getSelectItem())+"/"+gFileName.get(getSelectItem()));
			File mFile = new File(gFilePaths.get(getSelectItem()) /*+"/"+gFileName.get(getSelectItem())*/); // 获取路径内容
			//Global.debug("[@@@]    gFilePaths.size() ===="+ gFilePaths.size());
			if(mFile.isDirectory()){  // 是文件夹
				// 保存反显 返回时 需要
				gSelecyId[gPathNum] = getSelectItem();
			
				gPath.add(gFilePaths.get(getSelectItem()));
				//gPath.add(gFilePaths.get(getSelectItem())+"/"+gFileName.get(getSelectItem()));
				gName.add(getSelectItemContent());
				gPathNum++;
				
				showList(gFilePaths.get(getSelectItem()), getSelectItemContent());
				//showList(gFilePaths.get(getSelectItem())+"/"+gFileName.get(getSelectItem()), getSelectItemContent());
				intface_flag = FAVORITE_1_INTFACE;
				onResume();
			}
			else{  // 是文件					
				//getPathAudioList(gPath.get(gPathNum -1));
				//Global.debug("1111111111111111 gFilePaths.size() =="+ gFilePaths.size());
				gPlayListName.clear();
				//Global.debug("111111122211111 gFilePaths.size() =="+ gFilePaths.size());
				Global.debug("112222 gFilePaths =="+ gFilePaths);
				Global.debug("112222 gPlayListPaths =="+ gPlayListPaths);
				gPlayListPaths.clear();
				
				Global.debug("2222222222222222 gFileName.size()== "+ gFileName.size());
				//Global.debug("2222222222222222 gFilePaths.size()== "+ gFilePaths.size());
				for(int i = 0; i < gFileName.size(); i++)
				{
					Global.debug("gFilePaths.get(i) ==="+ gFileName.get(0));
				
					File mFile1 = new File(gFilePaths.get(i)); // 获取路径内容
					//File mFile1 = new File(gFilePaths.get(i)+"/"+gFileName.get(i)); // 获取路径内容
					if(mFile1.isFile()){  // 是文件
						gPlayListName.add(gFileName.get(i));
						gPlayListPaths.add(gFilePaths.get(i));
						//gPlayListPaths.add(gFilePaths);
						Global.debug("gFileName.get(i)==="+ gFileName.get(i));
						//Global.debug("gFilePaths.get(i)==="+ gFilePaths.get(i));
					}
					
				}
				Global.debug("3333333333333\r\n");
				startPlay(getSelectItem(), getResources().getString(R.string.my_save));
			}
		}
		// 最近播放界面
		else if(RECENTPLAY_INTFACE ==  intface_flag)  // 最近播放界面
		{
			gPlayListName.clear();
			gPlayListPaths.clear();
			for(int i = 0; i < gFileName.size(); i++){
				gPlayListName.add(gFileName.get(i));
				gPlayListPaths.add(gFilePaths.get(i));

			}
			startPlay(getSelectItem(), getResources().getString(R.string.my_play));
		}
		else if(DIRECTORY_1_INTFACE == intface_flag)  // 目录浏览界面
		{				
			File mFile = new File(gFilePaths.get(getSelectItem())); // 获取路径内容
			//File mFile = new File(gFilePaths.get(getSelectItem())+"/"+gFileName.get(getSelectItem())); // 获取路径内容
			if(mFile.isDirectory()){  // 是文件夹
				
				//String path = gFilePaths.get(getSelectItem())+"/"+gFileName.get(getSelectItem());
				// 保存反显 返回时 需要
				gSelecyId[gPathNum] = getSelectItem();
			
				gPath.add(gFilePaths.get(getSelectItem()));
				//gPath.add(path);
				gName.add(getSelectItemContent());
				gPathNum++;
				
				showList(gFilePaths.get(getSelectItem()), getSelectItemContent());
				onResume();
				//showList(path, getSelectItemContent());
			}
			else{  // 是文件				
				getPathAudioList(gPath.get(gPathNum -1));
				startPlay(getSelectItem(), getResources().getString(R.string.title_main));
			}
			Global.debug("[KEYCODE_ENTER]  gPathNum === "+ gPathNum);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (KeyEvent.KEYCODE_MENU == keyCode) {  // 按键 menu	
			
			return true;
		}
		else if(KeyEvent.KEYCODE_ENTER == keyCode ||   // 按键 Enter
				KeyEvent.KEYCODE_DPAD_CENTER == keyCode)
		{
			if(mMenuList.size() <= 0){
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
				mPromptDialog.show();
			
				//return true;
			}
			return true;
		}
		else if(KeyEvent.KEYCODE_DPAD_DOWN == keyCode ||
				KeyEvent.KEYCODE_DPAD_LEFT == keyCode || 
				KeyEvent.KEYCODE_DPAD_RIGHT == keyCode || 
				KeyEvent.KEYCODE_DPAD_UP == keyCode){
			if(mMenuList.size() <= 0){
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
				mPromptDialog.show();
			
				return true;
			}
			 
		}
		else if (KeyEvent.KEYCODE_BACK == keyCode) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
		
	}
	// 进入功能菜单
	private void startMenu(int defaultItem, String path, String filename) {
		Intent intent = new Intent();
		intent.putExtra("PATH", path); // 设置标题
		intent.putExtra("FILENAME", filename); // 设置数据
		intent.setClass(this, SaveMenuActivity.class);
		Global.debug("startMenu ======= wwwww===");
		startActivityForResult(intent, Global.MENU_FLAG);
	}
	
// 进入功能菜单
	private void startPlayMenu(int defaultItem, String path, String filename) {
		Intent intent = new Intent();
		intent.putExtra("PATH", path); // 设置标题
		intent.putExtra("FILENAME", filename); // 设置数据
		intent.setClass(this, PlayMenuActivity.class);
		Global.debug("startPlayMenu ======= wwwww===");
		startActivityForResult(intent, Global.PLAY_MENU_FLAG);
	}
	
// 显示 界面
	@Override
	@SuppressLint("InflateParams")
	public View getView(Context context, int position, View convertView, ViewGroup parent) {

		//Global.debug("getView =====111==position =="+ position + " selectItem =="+ getSelectItem());
		ViewHolder vh = null; // 空

		if (null == convertView) 
		{
			vh = new ViewHolder();
			LayoutInflater mLI = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// 初始化列表元素界面
			convertView = mLI.inflate(R.layout.music_list_child, null);
			// 获取列表布局界面元素
			vh.ivMenu = (ImageView) convertView.findViewById(R.id.image_list_childs);
			vh.tvMenu = (TextView) convertView.findViewById(R.id.text_list_childs);
			convertView.setTag(vh);
		} 
		else {
			vh = (ViewHolder) convertView.getTag();
		}

		Tools mTools = new Tools(context);
		
		vh.tvMenu.setTag(String.valueOf(position));
				
		vh.tvMenu.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel());
		vh.tvMenu.setHeight(mTools.convertSpToPixel(mTools.getFontSize()));
		

		vh.ivMenu.setMaxHeight(mTools.getFontSize());
		vh.ivMenu.setMaxWidth(mTools.getFontSize());

		vh.ivMenu.setMinimumHeight(mTools.getFontSize());
		vh.ivMenu.setMinimumWidth(mTools.getFontSize());

		if (gFilePaths != null) {
			
			//File mFile = new File(gFilePaths.get(position).toString()); // 获取当前路径
			//String path = gFilePaths.get(position).toString()+"/"+gFileName.get(position).toString();
			String path = gFilePaths.get(position).toString();
			File mFile = new File(path); // 获取当前路径
			String fileName = mFile.getName();
			vh.tvMenu.setText(fileName);
			//Global.debug("\r\n path ==== "+ path);
			if (path.equals(Global.ROOT_PATH )) {
				vh.ivMenu.setImageBitmap(imPhone); // 文件夹
			} else if (path.equals(Global.USER_PATH )) {
				vh.ivMenu.setImageBitmap(imSdcard); // 文件夹
			} else if (path.equals(Global.USB_PATH )) {
				vh.ivMenu.setImageBitmap(imUsb); // 文件夹
			} else if ((mFile.isDirectory()) || path.equals(Global.DIRECTORY_PATH )||
												path.equals(Global.FAVORITE_PATH ) ||
												path.equals(Global.RECENTPLAY_PATH)) {
				vh.ivMenu.setImageBitmap(imFoler); // 文件夹
			} else {
				vh.ivMenu.setImageBitmap(imFile); // 文件夹
			}
		} 
		else {
			Global.debug("[*********]===gFilePathList === null");
		}

		if (getSelectItem() == position) {
			convertView.setBackgroundColor(mTools.getHighlightColor());
			vh.tvMenu.setSelected(true);
		} else {
			convertView.setBackgroundColor(mTools.getBackgroundColor());
			vh.tvMenu.setSelected(false);
		}

		if (!TextUtils.isEmpty(gFileName.get(position))) {
			vh.tvMenu.setText(gFileName.get(position));
		} else {
			vh.tvMenu.setText("");
		}
		vh.tvMenu.setTextColor(mTools.getFontColor());

		return convertView;
	}
		
	private class ViewHolder {
		TextView tvMenu = null; // // 菜单名称
		ImageView ivMenu = null; // // 图片
	}
	
	// 进入 播放界面
	private void startPlay(int defaultItem, String title) {
		unregisterReceiver(tfCardPlugReceiver);
		Intent intent = new Intent();
		intent.putExtra("filename", gFileName.get(defaultItem)); // 设置标题

		intent.putStringArrayListExtra("filenamelist", gPlayListName);
		intent.putStringArrayListExtra("filepathlist", gPlayListPaths);
		
		intent.setClass(this, PlayActivity.class);
		Global.debug("startMenu ======= wwwww===");
		startActivityForResult(intent, Global.PLAY_FLAG);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Global.debug("\r\n[11] onActivityResult == requestCode ="+requestCode + " resultCode ==" + resultCode );
		if (requestCode == Global.PLAY_FLAG && resultCode == Global.PLAY_FLAG) {
			registerTFcardPlugReceiver();
			int tf_flag = data.getIntExtra("flag", 0);
			if(tf_flag == 1){
				updateShowList();
				return;
			}
			String mfilename = data.getStringExtra("filename");
			if(intface_flag == RECENTPLAY_INTFACE){
				showDbList(Global.PLAY_LIST_ID);
			}
			mMenuView.setSelectItem(gFileName.indexOf(mfilename));
			
			Global.FristString =  Global.GetPalyFristData(this);   // 获取最后一次伯村的路径
			
			Global.debug("112222 gPlayListPaths =="+ gPlayListPaths);
			//Global.debug("onActivityResult =====1111============\r\n");
		}
		else if(requestCode == Global.MENU_FLAG && resultCode == Global.MENU_FLAG){
			int selectItem = 0; // 先获取子菜单上次的设置值
			
			selectItem = data.getIntExtra("selectItem", selectItem);
			Global.debug("\r\n selectItem ===== "+ selectItem);
			if(selectItem == Global.MENU_DEL_FILE || selectItem == Global.MENU_DEL_ALL){
				if(false == showDbList(Global.SAVE_LIST_ID)){  //  // 空 退出
					keyupBack();
				}
				else{
					if(gId > (mMenuList.size()-1)){
						gId = 0;
					}
					mMenuView.setSelectItem(gId);
					mHandler.sendEmptyMessage(Global.MSG_RESUME);
				}
			}
		
			
		}
		else if(requestCode == Global.PLAY_MENU_FLAG && resultCode == Global.PLAY_MENU_FLAG){
			int selectItem = 0; // 先获取子菜单上次的设置值
			
			selectItem = data.getIntExtra("selectItem", selectItem);
			Global.debug("\r\n selectItem ===== "+ selectItem);
			if(selectItem == Global.MENU_DEL_FILE || selectItem == Global.MENU_DEL_ALL){
				if(false == showDbList(Global.PLAY_LIST_ID)){ // 空 退出
					keyupBack();
				}
				else{
					if(gId > (mMenuList.size()-1)){
						gId = 0;
					}
					mMenuView.setSelectItem(gId);
					mHandler.sendEmptyMessage(Global.MSG_RESUME);
				}
			}
		}	
	}
	
	// 显示list
	private void showList(String filePath, String fileName) 
	{
		Global.debug("[showList] filePath == " + filePath + " fileName == " + fileName);
		int selectId = 0, allId = 0;
		String  mSelectName = null;
		
		File mFile = new File(filePath); // 获取路径内容
		if (mFile.canRead()) // 可读
		{
			Global.debug("can read === filePath =" + filePath);
			// String fileName = mFile.getName();
			if (mFile.isDirectory()) // 是文件夹
			{
				// 顾虑条件
				FileFilter ff = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return !pathname.isHidden();// 过滤隐藏文件
					}
				};

				Global.debug("is Directory === ");
				// 获取文件夹内文件
				File[] mFiles = mFile.listFiles(ff);

				gFileName.clear();
				gFilePaths.clear();
				//gFilePaths = null;

				Global.debug("is Directory === length =" + mFiles.length);
				/* 获取文件列表 */
				if (mFiles.length > 0) {
					// 循环获取文件夹列表

					// 定义临时变量
					ArrayList<FileInfo> mFileFile = new ArrayList<FileInfo>();
					ArrayList<FileInfo> mFileFolder = new ArrayList<FileInfo>();
					mFileFile.clear();
					mFileFile.clear();
					
					for (File mCurrentFile : mFiles) {
						if (mCurrentFile.getName().equals("LOST.DIR")) // 去除LOST.DIR
						{
							continue;
						}
						File mFile1 = new File(mCurrentFile.getPath()); // 获取路径内容

						if (mFile1.isDirectory()) {  // 文件夹
							if( true == getDirectoryhaveMP3(mCurrentFile.getPath())){  // 文件夹内有音频文件
								FileInfo mFileInfo = new FileInfo();
								mFileInfo.name = mCurrentFile.getName();
								mFileInfo.path = mCurrentFile.getPath();
								mFileFolder.add(mFileInfo);
								Global.debug("\r\n Global.FristString =="+Global.FristString + " filePath ==="+filePath);
								if((Global.FristString != null) && (Global.FristString.contains(mCurrentFile.getPath()))){
									selectId = allId;
									mSelectName = mCurrentFile.getName();
									//Global.debug("\r\n[***11***] mSelectName   === "+ mSelectName);
								}
								allId ++;
							}
						}
						else{   // 文件
							String prefix = getExtensionName(mCurrentFile.getName());  // 获取文件名 后缀
							if(prefix.equals("mp3") || prefix.equals("MP3")||
									prefix.equals("wav") || prefix.equals("WAV")||
									prefix.equals("wma") || prefix.equals("WMA")||
									prefix.equals("aac") || prefix.equals("AAC")||
									prefix.equals("amr") || prefix.equals("AMR")||
									prefix.equals("flac") || prefix.equals("FLAC")||
									prefix.equals("m4a") || prefix.equals("M4A")||
									prefix.equals("m4r") || prefix.equals("M4R")||
									prefix.equals("MID") || prefix.equals("mid")||
									prefix.equals("mp2") || prefix.equals("MP2")||
									prefix.equals("ogg") || prefix.equals("OGG")||
									
			//						prefix.equals("vob") || prefix.equals("VOB")||
//									prefix.equals("rmvb") || prefix.equals("RMVB")||
									prefix.equals("wmv") || prefix.equals("WMV")||
									prefix.equals("mkv") || prefix.equals("MKV")||
									prefix.equals("flv") || prefix.equals("flV")||
									prefix.equals("asf") || prefix.equals("ASF")||
									prefix.equals("3gp") || prefix.equals("3GP")||
									prefix.equals("avi") || prefix.equals("AVI")||
									prefix.equals("mov") || prefix.equals("MOV")||
									prefix.equals("mp4") || prefix.equals("MP4")||
									prefix.equals("mpg") || prefix.equals("MPG")
									)
							{								
								FileInfo mFileInfo = new FileInfo();
								mFileInfo.name = mCurrentFile.getName();
								mFileInfo.path = mCurrentFile.getPath();
								
								mFileFolder.add(mFileInfo);
								
								if((Global.FristString != null) && (Global.FristString.contains(mCurrentFile.getPath()))){
									selectId = allId;
									mSelectName = mCurrentFile.getName();
								}
								allId ++;
							}
						}
					}
					Collections.sort(mFileFile, new UsernameComparator());
					Collections.sort(mFileFolder, new UsernameComparator());

					// 重新赋值  文件夹在前
					for(int i = 0; i < mFileFolder.size(); i++){
						gFileName.add(mFileFolder.get(i).name);
						gFilePaths.add(mFileFolder.get(i).path);
					}
					// 重新赋值  文件在后
					for(int i = 0; i < mFileFile.size(); i++){
						gFileName.add(mFileFile.get(i).name);
						gFilePaths.add(mFileFile.get(i).path);
					}
					// 再次清空
					mFileFile.clear();
					mFileFolder.clear();
					
					setListData(gFileName);

					int mSelectId = gFileName.indexOf(mSelectName);
					if(mSelectId <= 0){
						mSelectId = 0;
					}
					mMenuView.setSelectItem(mSelectId);
				} 
				else {
					setListData(gFileName);
					
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
					mPromptDialog.show();

				}
				setTitle(fileName);
			} 
			else {

			}
		}
		else {
			// 没有读写权限时
			Global.debug("can not read === filePath =" + filePath);
			//Toast.makeText(MainActivity.this, getResources().getString(R.string.limits), Toast.LENGTH_SHORT).show();
			//Global.getTts().speak(getResources().getString(R.string.limits));
		}
	}
	
	
	    
	// 显示 我的最爱和最近播放list
	private boolean showDbList(int flag) 
	{	
		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
		//int selectId = 0, allId = 0;
		String mSelectName = null;
		if( dbMusicInfo.getCount(flag) <= 0){
			return false;
		}
		
		MusicInfo musicinfo = new MusicInfo();   //创建 结构体
		ArrayList<MusicInfo> musicInfos = new ArrayList<MusicInfo>();
		
		gFileName.clear();
		gFilePaths.clear();
		
		//Global.debug("showDblist flag =====" + flag);
		
		int max_id = dbMusicInfo.getMaxId(flag);
		Global.debug("showDblist flag ====max_id =" + max_id);
		if(flag == Global.PLAY_LIST_ID){
			musicInfos = dbMusicInfo.GetAllData(Global.PLAY_LIST);
		}
		else if(flag == Global.SAVE_LIST_ID)
		{
			musicInfos = dbMusicInfo.GetAllData(Global.SAVE_LIST);
		}
		dbMusicInfo.closeDb();
		for(int i = 0; i < musicInfos.size(); i++)
		{
			musicinfo = musicInfos.get(i);
			//File mFile = new File(musicinfo.path+"/"+musicinfo.filename ); // 获取路径内容
			File mFile = new File(musicinfo.path ); // 获取路径内容
			//Global.debug("11113333333 musicinfo.path == "+ musicinfo.path + "\r\n");
			if(mFile.exists()){
				gFileName.add(musicinfo.filename);
				gFilePaths.add(musicinfo.path);

				if((Global.FristString != null) && (Global.FristString.contains(musicinfo.getPath()))){
	
					mSelectName = musicinfo.filename;
				}
				//allId ++;
			}
		}
		//Global.debug("[**]gFilePaths.size()  == "+ gFilePaths.size());
		
		setListData(gFileName);
		int mselectId = gFileName.indexOf(mSelectName);
		
		//Global.debug("\r\n[**] mSelectName  == "+ mSelectName  + " mselectId ==" + mselectId);
		
		if(mselectId <= 0){
			mselectId = 0;
		}
		mMenuView.setSelectItem(mselectId);  // 设置反显
		
		if(flag == Global.PLAY_LIST_ID){
			setTitle(getResources().getString(R.string.my_play));
		}
		else if(flag == Global.SAVE_LIST_ID){
			setTitle(getResources().getString(R.string.my_save));
		}

		mHandler.sendEmptyMessage(Global.MSG_RESUME);
		return true;
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
	
	    /* 
		 *  获取文件夹内有无音频文件 
		 *  
		 */   
    public static boolean getDirectoryhaveMP3(String path) {    
        if ((path != null) && (path.length() > 0)) {
        	
        	File mFile = new File(path);
        	if(mFile.isDirectory())
        	{
        		FileFilter ff = new FileFilter() {  // 过滤条件

					@Override
					public boolean accept(File pathname) {
						// TODO Auto-generated method stub
						return !pathname.isHidden();// 过滤隐藏文件
					}
        		};
        		
        		// 获取文件夹内文件
				File[] mFiles = mFile.listFiles(ff);
				
				if (mFiles.length > 0) {
					// 循环获取文件夹列表
					for (File mCurrentFile : mFiles) {
						if (mCurrentFile.getName().equals("LOST.DIR")) // 去除LOST.DIR
						{
							continue;
						}
						File mFile1 = new File(mCurrentFile.getPath()); // 获取路径内容

						if (mFile1.isDirectory()) {   // 还是文件夹

							if(true == getDirectoryhaveMP3(mCurrentFile.getPath())) {
							//getDirectoryhaveMP3(mCurrentFile.getPath());
								return true;
							}
						}
						else{  // 是文件
							String prefix =  getExtensionName(mCurrentFile.getName());
							if(prefix.equals("mp3") || prefix.equals("MP3")||
									prefix.equals("wav") || prefix.equals("WAV")||
									prefix.equals("wma") || prefix.equals("WMA")||
									prefix.equals("aac") || prefix.equals("AAC")||
									prefix.equals("amr") || prefix.equals("AMR")||
									prefix.equals("flac") || prefix.equals("FLAC")||
									prefix.equals("m4a") || prefix.equals("M4A")||
									prefix.equals("m4r") || prefix.equals("M4R")||
									prefix.equals("MID") || prefix.equals("mid")||
									prefix.equals("mp2") || prefix.equals("MP2")||
									prefix.equals("ogg") || prefix.equals("OGG")||
									
					//				prefix.equals("vob") || prefix.equals("VOB")||
//									prefix.equals("rmvb") || prefix.equals("RMVB")||
									prefix.equals("wmv") || prefix.equals("WMV")||
									prefix.equals("mkv") || prefix.equals("MKV")||
									prefix.equals("flv") || prefix.equals("flV")||
									prefix.equals("asf") || prefix.equals("ASF")||
									prefix.equals("3gp") || prefix.equals("3GP")||
									prefix.equals("avi") || prefix.equals("AVI")||
									prefix.equals("mov") || prefix.equals("MOV")||
									prefix.equals("mp4") || prefix.equals("MP4")||
									prefix.equals("mpg") || prefix.equals("MPG"))
							{
								return true;
							}
							
						}
					}
				}		   
        	}    
        }
		return false;
    }
    
		    
	 // 显示list
	private void getPathAudioList(String filePath) 
	{
	
		Global.debug("[showList] filePath == " + filePath );

		File mFile = new File(filePath); // 获取路径内容
		if (mFile.canRead()) // 可读
		{
			Global.debug("can read === filePath =" + filePath);
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

				gPlayListName.clear();
				gPlayListPaths.clear();

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
								prefix.equals("wma") || prefix.equals("WMA")||
								prefix.equals("aac") || prefix.equals("AAC")||
								prefix.equals("amr") || prefix.equals("AMR")||
								prefix.equals("flac") || prefix.equals("FLAC")||
								prefix.equals("m4a") || prefix.equals("M4A")||
								prefix.equals("m4r") || prefix.equals("M4R")||
								prefix.equals("MID") || prefix.equals("mid")||
								prefix.equals("mp2") || prefix.equals("MP2")||
								prefix.equals("ogg") || prefix.equals("OGG")||
								
					//			prefix.equals("vob") || prefix.equals("VOB")||
//									prefix.equals("rmvb") || prefix.equals("RMVB")||
								prefix.equals("wmv") || prefix.equals("WMV")||
								prefix.equals("mkv") || prefix.equals("MKV")||
								prefix.equals("flv") || prefix.equals("flV")||
								prefix.equals("asf") || prefix.equals("ASF")||
								prefix.equals("3gp") || prefix.equals("3GP")||
								prefix.equals("avi") || prefix.equals("AVI")||
								prefix.equals("mov") || prefix.equals("MOV")||
								prefix.equals("mp4") || prefix.equals("MP4")||
								prefix.equals("mpg") || prefix.equals("MPG"))
						{								
							gPlayListName.add(mCurrentFile.getName());
							//gPlayListPaths.add(filePath);
							gPlayListPaths.add(mCurrentFile.getPath());
						}
					}
				}
				Collections.sort(gPlayListName);  // 排序gPlayListName
				Collections.sort(gPlayListPaths);  // 排序gPlayListPaths
			}
		}
		else {
			// 没有读写权限时
			Global.debug("can not read === filePath =" + filePath);
		}
		//super.onResume();
	}
	// 获取外部TF卡是否存在路径
	public boolean getExtSDUSBPathHave(String Path) {
	//	File sdDir = null;
		Global.debug("getSDPath === 111==");
		
		File mFile = new File(Path);
		Global.debug("getSDPath === 222==USER_PATH = " + Path);
		Global.debug("getSDPath === 222==USER_PATH = " + mFile.exists());
		
		if(mFile.getTotalSpace()  > 1024*1024)  // 盘大于1M
		{
			return true;
		}
		return false;
	}
	// 增加我的收藏 列表文件
	public void MusicAddSaveList()
	{
		MusicInfo musicinfo = new MusicInfo();   //创建 结构体
		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
		
		String Filename = gFileName.get(getSelectItem());
    	String FilePath = gFilePaths.get(getSelectItem());
    	//int max_id = dbMusicInfo.getMaxId(Global.SAVE_LIST_ID);
    	int max_id = dbMusicInfo.getCount(Global.SAVE_LIST_ID);  // 获取数据数
    	Global.debug("\r\n MusicAddSaveList ===== max_id = " + max_id);
    	
    	if(max_id <= 0){  // 无数据
    		musicinfo._id = 1;
    		musicinfo.path = FilePath;
    		musicinfo.filename = Filename;
    		
    		dbMusicInfo.add(musicinfo , Global.SAVE_LIST_ID);
    		
    		PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.file_add_ok));
    		mDialog.show();
    		
    		//TtsUtils.getInstance().speak(getResources().getString(R.string.file_add_ok));
    	}   
    	else{
    		boolean flag = false;
    		ArrayList<MusicInfo> mMusicInfos = new ArrayList<MusicInfo>();
    		mMusicInfos = dbMusicInfo.GetAllData(Global.SAVE_LIST);
    		for(int i = 0; i < mMusicInfos.size(); i++)
    		{
    			musicinfo = mMusicInfos.get(i);
    			Global.debug("[^^^]FilePath  == "+ FilePath);
    			Global.debug("[^^^]musicinfo.getPath()  == "+ musicinfo.getPath());
    			//String mFilePath = FilePath+"/"+Filename;
    			//String mFilePath1 = musicinfo.getPath()+"/"+musicinfo.getFileName();
    			String mFilePath = FilePath;
    			String mFilePath1 = musicinfo.getPath();
    			if(mFilePath.equals(mFilePath1)){ // 文件重复
    				flag = true;
    				break;
    			}
    		}
    		Global.debug("\r\n flag    ==== "+ flag);
    		if(true == flag){  // 文件重复
    			PromptDialog mpro = new PromptDialog(this, getResources().getString(R.string.file_exists));
    			
    			mpro.show();
    			mpro.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根						
					}
				});
    		}
    		else{  // 添加文件
    			int num_id = dbMusicInfo.getMaxId(Global.SAVE_LIST_ID);
	    		musicinfo._id = num_id + 1;
	    		musicinfo.path = FilePath;
	    		musicinfo.filename = Filename;
	    		dbMusicInfo.add(musicinfo, Global.SAVE_LIST_ID);
	    		PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.file_add_ok));
	    		mDialog.show();
	    		//TtsUtils.getInstance().speak(getResources().getString(R.string.file_add_ok));
    		}
    	}
	}
	
	private void testrawplay() {
		// TODO 自动生成的方法存根
		Handler mHandler = null;
		//((MyPlayer) myPlayer).setOnStateChangedListener((OnStateChangedListener) this);
		
		Global.debug("\r\nKeyEvent.KEYCODE_0  ============ ");
		/*Intent mIntent = new Intent(this , Alarm_receiver_Activity.class);
		//Bundle bundle = new Bundle();//
		
		//bundle.putInt("FLAG", Alarmpublic.BOOT_FLAG); // 修改项
		mIntent.putExtra("FLAG", Alarmpublic.BOOT_FLAG); // 传入参数 
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(mIntent);	*/
		
		AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.alarm);
		
		Global.debug("\r\n[4444] gFilename =====fileDescriptor.getFileDescriptor()==" + fd.getFileDescriptor());

	/*	try {
			myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			myPlayer.setDataSource(fd.getFileDescriptor());
			myPlayer.prepare();
			myPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}*/
		Global.debug("\r\n startPlay2   ==========666==");
		
		//((MyPlayer) myPlayer).startPlayback2(((MyPlayer) myPlayer).playProgress(), fd.getFileDescriptor(), true);
	}
	
	
	// 获取当前路径
	private String getCurPath(){
		return gPath.get(gPathNum -1);
	}
	// 更新界面
	private void updateShowList(){
		gName.clear();
		gPath.clear();
		
		gFileName.clear();
		gFilePaths.clear();
		
		Global.FristString =  Global.GetPalyFristData(this);   // 获取最后一次伯村的路径
		Global.debug("MainActivity ====1112222= Global.FristString ==" + Global.FristString);
		//mTitle = getResources().getString(R.string.app_name);
		mTitle = this.getResources().getString(R.string.title_main);
		gFileName = ArrayUtils.strArray2List (getResources().getStringArray(R.array.main_list));
		mMenuList = gFileName; // ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
		
		gFilePaths.add(Global.DIRECTORY_PATH) ;  // 目录浏览 
		gFilePaths.add(Global.FAVORITE_PATH) ;	// 我的最爱
		gFilePaths.add(Global.RECENTPLAY_PATH) ;  // 最近播放
		intface_flag = MAIN_INTFACE;
		setListData(mMenuList);
		selectItem = SharedPrefUtils.getSharedPrefInt(this, Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_SELECT, selectItem);
		Global.debug("\r\n selectItem =====" + selectItem);
		setTitle(getResources().getString(R.string.app_name));
		mMenuView.setSelectItem(selectItem);
		super.onResume();
	}
	
	// tf卡插拔消息 注册
	private void registerTFcardPlugReceiver() {   
        IntentFilter intentFilter = new IntentFilter();   
       
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED); // SD卡插入
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT); // SD卡拔出
        intentFilter.addDataScheme("file");
        
        registerReceiver(tfCardPlugReceiver, intentFilter);  
        Global.debug("\r\n  registerTFcardPlugReceiver ==========================");
    } 
	// 插拔消息  接收
    private BroadcastReceiver tfCardPlugReceiver = new BroadcastReceiver() { 
   
        @Override 
        public void onReceive(Context context, Intent intent) { 
        	 Global.debug("\r\n  tfCardPlugReceiver ========2222==================");   
            String action = intent.getAction();
            
            String mData = intent.getDataString();  // 获取路径
            mData = mData.substring(7,mData.length());
            Global.debug("\r\n mData ============== " + mData);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) { // 插入
                
                Global.debug("\r\n tf卡插入============== ");
            }
            else if(Intent.ACTION_MEDIA_EJECT.equals(action)){  // Tf 卡拔出
            	
            	String mPath = getCurPath();
            	Global.debug("\r\n mPath ============== " + mPath);
            	if(mPath.contains(mData)){  // 包含
	            	if(mData.contains(Global.MENU_PATH_EXTSD) ){  // 存储卡
	            		Global.debug("\r\n tf卡 列表更新============== ");
	            		updateShowList();
	            	}
	            	else if(mData.contains(Global.MENU_PATH_UDISK) ){  // U盘
	            		Global.debug("\r\n U盘 列表更新============== ");
	            		updateShowList();
	            	}
            	}
            	Global.debug("\r\n tf卡 拔出============== ");
            }
            else if(Intent.ACTION_MEDIA_REMOVED.equals(action)){
            	Global.debug("\r\n tf卡 ACTION_MEDIA_REMOVED============== ");
            }
            else if(Intent.ACTION_MEDIA_SHARED.equals(action)){
            	Global.debug("\r\n tf卡 ACTION_MEDIA_SHARED============== ");
            }
            
        }            
    };
    
    // 文件排序 按照
	private class UsernameComparator implements Comparator<FileInfo> {
		public int compare(FileInfo entity1, FileInfo entity2) {
			
			String str1 = "";
			String str2 = "";
			
			String str10 = "";
			String str11 = "";
			String str20 = "";
			String str21 = "";
			
			try 
			{
				str1 = Pinyin4jUtils.converterToSpell( entity1.name );
				str2 = Pinyin4jUtils.converterToSpell( entity2.name );
				
				for( int i = 0; i < str1.length(); i++ )
				{
					String str = str1.substring(i, i+1);
					if( isNumber( str ) )
					{
						str10 += str;
					}
					else
					{
						str11 += str1.substring(i);
						break;
					}
				}
				
				for( int i = 0; i < str2.length(); i++ )
				{
					String str = str2.substring(i, i+1);
					if( isNumber( str ) )
					{
						str20 += str;
					}
					else
					{
						str21 += str2.substring(i);
						break;
					}
				}
				
				if( !TextUtils.isEmpty(str10) && !TextUtils.isEmpty(str20) )
				{
					float f1 = Float.parseFloat(str10);
					float f2 = Float.parseFloat(str20);
					
					if( f1 > f2 )
					{
						return	1;
					}
					else if( f1 < f2 )
					{
						return	-1;
					}
					else
					{
						return str11.compareToIgnoreCase(str21);
					}
				}
				else
				{
					return str1.compareToIgnoreCase(str2);
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();

				return str1.compareToIgnoreCase(str2);
			}
		}
		
		private boolean isNumber( String str )
		{
			if( "0".equals(str) || "1".equals(str) || "2".equals(str) || "3".equals(str) || "4".equals(str) || "5".equals(str) || 
				"6".equals(str) || "7".equals(str) || "8".equals(str) || "9".equals(str) || ".".equals(str) )
			{
				return	true;
			}
			
			return	false;
		}
	}
	
	private class FileInfo {
		String name ; // // 菜单名称
		String path ; // // 图片
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_RESUME){
				onResume();
			}
			
			super.handleMessage(msg);
		}	
	};
}
