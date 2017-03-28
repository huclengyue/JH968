package com.sunteam.recorder.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.menu.MenuConstant;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;

public class PlaylistActivity extends MenuActivity {
	private ListView listView;
	private BatteryBroadcastReciver receiver;
	private int visibleItemCount = 0;
	private int itemHeight = 0;
	private int currentPosition = 0;// 光标所在item在当前页的位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTitle = getIntent().getStringExtra(MenuConstant.INTENT_KEY_TITLE);
		mMenuList = getplayList();

		super.onCreate(savedInstanceState);
		listView = getListView();
		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
					return true;
				}
				return false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		receiver = new BatteryBroadcastReciver();
		// 创建一个过滤器
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(receiver, intentFilter);

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	public class BatteryBroadcastReciver extends BroadcastReceiver {
		private int level;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				// 得到系统当前电量
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				// //取得系统总电量
				// int total=intent.getIntExtra(BatteryManager.EXTRA_SCALE,
				// 100);

				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
						|| status == BatteryManager.BATTERY_STATUS_FULL;
				if(isCharging == true){
					level = 100;
				}
				if (level < 10) {

				} else if (level < 20 && !isCharging) {
					// Global.showToast(PlaylistActivity.this,
					// R.string.low_battery, null, -1);
				}
			}
		}

		public int getLevel() {
			return level;
		}
	}

	@SuppressWarnings("unchecked")
	private void enterPlayActivity() {
		if (receiver.getLevel() < 10) {
			Global.showToast(PlaylistActivity.this, R.string.cannot_play, mHandler, Global.MSG_ONRESUM);

		} else {
			Intent intent = new Intent(PlaylistActivity.this, PlayActivity.class);
			String selectedText = getSelectItemContent();
			intent.putExtra("filename", selectedText);
			intent.putStringArrayListExtra("filelist", mMenuList);
			startActivityForResult(intent, 1);
		}
	}

	@SuppressLint("DefaultLocale")
	private ArrayList<String> getplayList() {
		File file = new File(Global.storagePath);
		File[] subFile = file.listFiles();
		ArrayList<String> fileList = new ArrayList<String>();
		if (subFile != null) {
			for (int i = 0; i < subFile.length; i++) {
				// 判断是否为文件夹
				if (!subFile[i].isDirectory()) {
					String filename = subFile[i].getName();
					// 判断是否为wav结尾
					if (filename.trim().toLowerCase().endsWith(".wav")) {
						fileList.add(filename);
					}
				}
			}
		}
		Collections.sort(fileList, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg1.compareTo(arg0);
			}
		});
		return fileList;
	}

	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		enterPlayActivity();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			mMenuView.setSelectItem(data.getIntExtra("currentIndex", 0));
			visibleItemCount = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
			listView.setSelectionFromTop(mMenuView.getSelectItem(),
					itemHeight * (mMenuView.getSelectItem() % (visibleItemCount == 0 ? 1 : visibleItemCount)));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void leftSlip() {
		itemHeight = listView.getChildAt(0).getHeight();
		visibleItemCount = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();

		int temp = getSelectItem() + visibleItemCount;
		if (temp < mMenuList.size() + visibleItemCount - 1) {
			if (temp < mMenuList.size() - 1) {
				mMenuView.setSelectItem(temp, false);
			} else {
				mMenuView.setSelectItem(mMenuList.size() - 1, false);
			}
			listView.setSelectionFromTop(getSelectItem(),
					itemHeight * (getSelectItem() % (visibleItemCount == 0 ? 1 : visibleItemCount)));
		} else {
			mMenuView.down();
		}
	}

	@Override
	public void rightSlip() {
		itemHeight = listView.getChildAt(0).getHeight();
		visibleItemCount = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();

		int temp = getSelectItem() - visibleItemCount;
		if (temp > -visibleItemCount) {
			if (temp > 0) {
				mMenuView.setSelectItem(temp, false);
			} else {
				mMenuView.setSelectItem(0, false);
			}
			listView.setSelectionFromTop(getSelectItem(),
					itemHeight * (getSelectItem() % (visibleItemCount == 0 ? 1 : visibleItemCount)));
			// listView.setSelection(playListAdapter.getSelectItem());
			// Global.getTts().stop();
			// Global.getTts().speak(
			// mMenuList.get(playListAdapter.getSelectItem()),
			// TextToSpeech.QUEUE_FLUSH, null);
		} else {
			mMenuView.up();
			// Global.showToast(PlaylistActivity.this,
			// R.string.turn2end,mHandler,1);
			// playListAdapter.setSelectItem(mMenuList.size() - 1, 1);
			// listView.setSelection(playListAdapter.getSelectItem());
			// Global.getTts().speak(
			// mMenuList.get(playListAdapter.getSelectItem()),
			// TextToSpeech.QUEUE_ADD, null);
		}

	}

	@Override
	public void upSlip() {
		Log.e("zyw", "upSlip");
		itemHeight = listView.getChildAt(0).getHeight();
		visibleItemCount = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
		// playListAdapter.down();
		if (getSelectItem() != 0) {
			if (currentPosition < (visibleItemCount - 1)) {
				currentPosition++;
			}
			listView.setSelectionFromTop(getSelectItem(), itemHeight * currentPosition);
		}
		getSelectItemContent(0);
	}

	@Override
	public void downSlip() {
		if (listView == null) {
			return;
		}
		itemHeight = listView.getChildAt(0).getHeight();
		visibleItemCount = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
		// playListAdapter.up();
		if (getSelectItem() != getListData().size() - 1) {
			if (currentPosition > 0) {
				currentPosition--;
			}
			listView.setSelectionFromTop(getSelectItem(), itemHeight * currentPosition);
		}
		getSelectItemContent(0);
	}

	// 处理弹框
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == Global.MSG_ONRESUM) {
				onResume();
			}
			super.handleMessage(msg);
		}
	};
}
