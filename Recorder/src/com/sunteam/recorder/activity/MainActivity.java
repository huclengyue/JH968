package com.sunteam.recorder.activity;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;

@SuppressLint({ "DefaultLocale", "HandlerLeak" })
public class MainActivity extends MenuActivity {
	
	
	
	private BatteryBroadcastReciver reciver;

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				//adapter.setSelectItem(0, 0);
			}else if(msg.what == 1){
				//adapter.setSelectItem(menuList.size()-1, 0);
			}else if(msg.what == 2){
				TtsUtils.getInstance().speak(getResources().getString(R.string.recorder)+","+getResources().getString(R.string.playback), TtsUtils.TTS_QUEUE_FLUSH);
			}
			super.handleMessage(msg);
		}		
	};
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Global.showToast(MainActivity.this, R.string.error_no_sdcard, null, -1);
			File storageFile = new File(Global.storagePath);
			if (!storageFile.exists())
				storageFile.mkdirs();
		}

		mTitle = getResources().getString(R.string.recorder);
		mMenuList = new ArrayList<String>();
		mMenuList.add(getResources().getString(R.string.record));
		mMenuList.add(getResources().getString(R.string.playback));
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		reciver = new BatteryBroadcastReciver();
		// 创建一个过滤器
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(reciver, intentFilter);
		
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		
		if (TtsUtils.getInstance() != null) {
			TtsUtils.getInstance().destroy();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(reciver);
		super.onPause();
		
	}

	private boolean isExistPlayList() {
		File file = new File(Global.storagePath);
		File[] subFile = file.listFiles();
		if (subFile != null) {
			for (int i = 0; i < subFile.length; i++) {
				// 判断是否为文件夹
				if (!subFile[i].isDirectory()) {
					String filename = subFile[i].getName();
					// 判断是否为wav结尾
					if (filename.trim().toLowerCase().endsWith(".wav")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public class BatteryBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				// 得到系统当前电量
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				// //取得系统总电量
				// int total=intent.getIntExtra(BatteryManager.EXTRA_SCALE,
				// 100);
				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
						|| status == BatteryManager.BATTERY_STATUS_FULL;
				if (level < 10) {

				} else if (level < 20 && !isCharging) {
			//		Global.showToast(MainActivity.this, R.string.low_battery, null, -1);
				}
			}
		}
	}

	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		switch (selectItem) {
		case 0:  // 录音
			startActivityFromMenu(RecordActivity.class, menuItem);
			break;
		case 1:  // 录音回放
			if (!isExistPlayList()) {
				Global.showToast(MainActivity.this, R.string.no_file,mHandler,2);
			} else {
				startActivityFromMenu(PlaylistActivity.class, menuItem);
			}
			break;
		default:
			break;
		}
	}
 // 
	private void startActivityFromMenu(Class<?> cls, String title) {
		Intent intent = new Intent();
		intent.putExtra("title", title); // 标题名称

		intent.setClass(this, cls);

		// 如果希望启动另一个Activity，并且希望有返回值，则需要使用startActivityForResult这个方法，
		// 第一个参数是Intent对象，第二个参数是一个requestCode值，如果有多个按钮都要启动Activity，则requestCode标志着每个按钮所启动的Activity
		startActivity(intent);
	}
	
	
}
