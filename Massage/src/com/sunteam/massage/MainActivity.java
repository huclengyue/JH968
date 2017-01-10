package com.sunteam.massage;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.massage.utils.Global;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

@SuppressLint({ "NewApi", "InflateParams", "HandlerLeak" })
public class MainActivity extends MenuActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mTitle = getResources().getString(R.string.main_title);
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
		Global.debug("MainActivity ====1111= \r\n");
		super.onCreate(savedInstanceState);  // 调用父类的 oncreat
	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		
		if (TtsUtils.getInstance() != null) {
			TtsUtils.getInstance().destroy();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();  // 父类的 onResum
		Global.debug("MainActivity ====1111==222= \r\n");
//		Global.setContext(this); // 已经在SunteamApplication()中进行了设置，此处可不必设置
	}

	// 键抬起
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_MENU == keyCode) {  // 按键 menu
			startFunctionMenu();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	// 键抬起
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (KeyEvent.KEYCODE_MENU == keyCode) {  // 按键 menu
	
				return true;
			}

			return super.onKeyDown(keyCode, event);
		}
		
	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		Global.debug("MainActivity ====22222= \r\n");
		startNextActivity(UserInfoActivity.class, selectItem, menuItem, null);
	}

	private void startNextActivity(Class<?> cls, int selectItem, String title, String[] list) {
		Intent intent = new Intent();
		intent.putExtra("title", title); // 菜单名称
		intent.putExtra("list", list); // 菜单列表
		intent.putExtra("ID", selectItem); // 菜单列表
		Global.debug("MainActivity ====3333= \r\n");
		intent.setClass(this, cls);

		// 如果希望启动另一个Activity，并且希望有返回值，则需要使用startActivityForResult这个方法，
		// 第一个参数是Intent对象，第二个参数是一个requestCode值，如果有多个按钮都要启动Activity，则requestCode标志着每个按钮所启动的Activity
		startActivityForResult(intent, selectItem);
	}
	// 进入 menu 界面
	private void startFunctionMenu() {
		Intent intent = new Intent(this, FunctionMenuActivity.class);
		intent.putExtra("ID", getSelectItem());
		
		Global.debug("MainActivity ====4444= \r\n");
		startActivity(intent);
	}
}
