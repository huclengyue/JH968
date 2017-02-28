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
		// ����һ��������
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(reciver, intentFilter);
		
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		// TODO �Զ����ɵķ������
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
				// �ж��Ƿ�Ϊ�ļ���
				if (!subFile[i].isDirectory()) {
					String filename = subFile[i].getName();
					// �ж��Ƿ�Ϊwav��β
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
				// �õ�ϵͳ��ǰ����
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				// //ȡ��ϵͳ�ܵ���
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
		case 0:  // ¼��
			startActivityFromMenu(RecordActivity.class, menuItem);
			break;
		case 1:  // ¼���ط�
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
		intent.putExtra("title", title); // ��������

		intent.setClass(this, cls);

		// ���ϣ��������һ��Activity������ϣ���з���ֵ������Ҫʹ��startActivityForResult���������
		// ��һ��������Intent���󣬵ڶ���������һ��requestCodeֵ������ж����ť��Ҫ����Activity����requestCode��־��ÿ����ť��������Activity
		startActivity(intent);
	}
	
	
}
