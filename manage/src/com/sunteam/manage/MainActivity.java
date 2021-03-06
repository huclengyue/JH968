package com.sunteam.manage;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.menu.MenuConstant;
import com.sunteam.common.menu.menulistadapter.ShowView;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.manage.utils.Global;
import com.sunteam.manage.utils.Pinyin4jUtils;

public class MainActivity extends MenuActivity implements ShowView {
	// 全局变量
	// 图片 信息
	private Bitmap imFile;
	private Bitmap imFoler;
	private Bitmap imSdcard;
	private Bitmap imUsb;
	private Bitmap imPhone;

	@SuppressWarnings("unused")
	private int interface_flag = Global.MAIN_INTERFACE;

	private int[] gSelecyId = null;

	// 启动文件管理器的类别
	private int mAction = 0; // 0 默认是从主菜单启动; 1 从蓝牙功能中启动，获取一个文件

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getIntentPara();
		Global.gCopyFlag = false;
		Global.gCutFlag = false;
		// gMeunViewFlag = false;
		Global.gFileName = new ArrayList<String>();
		Global.gFilePaths = new ArrayList<String>();

		// gMenuName = new String[];
		Global.gPath = new ArrayList<String>();
		Global.gName = new ArrayList<String>();
		gSelecyId = new int[Global.MAX_RANK];// 目录最大级数

		for (int i = 0; i < Global.MAX_RANK; i++) {
			gSelecyId[i] = 0;
		}

		imFile = BitmapFactory.decodeResource(this.getResources(), R.drawable.file);
		imFoler = BitmapFactory.decodeResource(this.getResources(), R.drawable.folder);
		imSdcard = BitmapFactory.decodeResource(this.getResources(), R.drawable.sdcard);
		imPhone = BitmapFactory.decodeResource(this.getResources(), R.drawable.phone);
		imUsb = BitmapFactory.decodeResource(this.getResources(), R.drawable.usbdisk);


		Global.gPath.clear();
		Global.gName.clear();

		Global.gPath.add(Global.ROOT_PATH_FLAG);
		Global.gName.add(getResources().getString(R.string.app_name));
		Global.gPathNum = 1;

		interface_flag = Global.MAIN_INTERFACE;
		/* 获取一级界面 */
		getTopDirList();
		

		mMenuList = Global.gFileName;// ArrayUtils.strArray2List(gFileName); ;

		if (null == mTitle) {
			mTitle = getResources().getString(R.string.app_name); // 设置标题
		}

		Global.debug("222----------------");

		super.onCreate(savedInstanceState);

		mMenuView.setShowView(this);

		registerTFcardPlugReceiver(); // 注册tf卡插拔消息
	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		unregisterReceiver(tfCardPlugReceiver);

		if (TtsUtils.getInstance() != null) {
			TtsUtils.getInstance().destroy();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}

	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();
		
		if (mMenuList.size() <= 0) { // 提示无文件
			Global.debug("\r\n***************222**********mMenuList.size() ==" + mMenuList.size());
			/*
			 * PromptDialogNospeech mpDialog = new
			 * PromptDialogNospeech(MainActivity.this,
			 * getResources().getString(R.string.no_file)); mpDialog.show();
			 * TtsUtils.getInstance().speak(getResources().getString(R.string.
			 * no_file), 1);
			 */
			PromptDialog mpDialog = new PromptDialog(MainActivity.this, getResources().getString(R.string.no_file));
			mpDialog.show();
			// TtsUtils.getInstance().speak(getResources().getString(R.string.no_file),
			// 1);
			// mpDialog.dismiss();
			/*
			 * mpDialog.setPromptListener(new PromptListener() {
			 * 
			 * @Override public void onComplete() { // TODO 自动生成的方法存根
			 * 
			 * } });
			 * 
			 * } else{ onResume(); }
			 */
		}
	}

	// 按键 OK键处理
	public void KeyUp_Enter(int selectItem) {

		if (mMenuList.size() <= 0) { // 空文件夹 直接弹出菜单键
			showMenuList();
			return;
		}

		File mFile = new File(Global.gFilePaths.get(selectItem).toString()); //
		/*
		 * if (false == mFile.exists()) { if
		 * (Global.gFilePaths.get(selectItem).toString().equals(Global.USER_PATH
		 * )) { SpeakContentend(getResources().getString(R.string.tf_notexit));
		 * } else if
		 * (Global.gFilePaths.get(selectItem).toString().equals(Global.USB_PATH)
		 * ) { SpeakContentend(getResources().getString(R.string.usb_notexit));
		 * } return; }
		 */

		if (mFile.isDirectory()) { // 是文件夹
			Global.debug("selectItem ==" + selectItem + "Global.gFilePaths.get(selectItem) === "
					+ Global.gFilePaths.get(selectItem).toString());
			Global.gPath.add(Global.gFilePaths.get(selectItem).toString());
			Global.gName.add(Global.gFileName.get(selectItem).toString());

			gSelecyId[Global.gPathNum] = selectItem;
			Global.gPathNum++;

			for (int i = 0; i < Global.gPathNum; i++) {
				Global.debug("i ==" + i + "Global.gPath === " + Global.gPath.get(i).toString());
			}

			Global.debug("onEnterCompleted gPathNum =" + Global.gPathNum);
			Global.debug("onEnterCompleted gFilePaths.get(selectItem).toString() ="
					+ Global.gFilePaths.get(selectItem).toString());
			Global.debug("onEnterCompleted gFileName.get(selectItem).toString() ="
					+ Global.gFileName.get(selectItem).toString());

			if (true == showList(Global.gFilePaths.get(selectItem).toString(),
					Global.gFileName.get(selectItem).toString())) {
				onResume();
			}
		}

		else {
			Global.debug("selectID is file=====mMenuView= " + mMenuList);
			// Global.debug("****1111**********33333*********mMenuView = \r\n"+
			// mMenuView);
			Global.gtempID = selectItem;
			if (1 == mAction) {
				Intent intent = new Intent();
				intent.putExtra("filename", Global.gFilePaths.get(selectItem).toString());
				setResult(Activity.RESULT_OK, intent);
				finish();
				return;
			}
			showMenuList();
		}

	}

	// 显示菜单界面
	private void getTopDirList() {
		
		Global.gFileName.clear();
		Global.gFilePaths.clear();
		
		Global.gFileName.add(getResources().getString(R.string.phone));
		Global.gFilePaths.add(Global.ROOT_PATH);

		Global.gFileName.add(getResources().getString(R.string.sdcard));
		Global.gFilePaths.add(Global.USER_PATH);
/*
		Global.gFileName.add(getResources().getString(R.string.udisk));
		Global.gFilePaths.add(Global.USB_PATH);
*/
	}
	
	// 显示菜单界面
	private void showMenuList() {
		Global.debug("showMenuList ====qq=== 111===");

		String[] list = getResources().getStringArray(R.array.menu_list);
		Global.debug("showMenuList ======= 111===");
		startMenu(0, getResources().getString(R.string.Menu_Title), list);
	}

	// 真正显示菜单界面
	private void startMenu(int defaultItem, String title, String[] list) {

		if (mMenuList.size() <= 0) {
			Global.gtempID = -1;
		} else {
			Global.gtempID = getSelectItem();
		}
		unregisterReceiver(tfCardPlugReceiver);
		Global.debug("\r\n startMenu ===22==== wwwww==Global.gtempID=" + Global.gtempID);
		Intent intent = new Intent();
		intent.putExtra("title", title); // 设置标题
		intent.putExtra("list", list); // 设置数据
		intent.setClass(this, ManageMenuActivity.class);
		Global.debug("\r\n startMenu ======= wwwww===");
		Global.debug("\r\nstartMenu =====mMenuView= " + mMenuList);
		startActivityForResult(intent, Global.MENU_INTERFACE_FLAG);
	}

	// enter 界面返回结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		// Global.debug("\r\n onActivityResult =====mMenuView= " + mMenuList);
		// Global.debug("\r\n [^^^] onActivityResult =======resultCode= " +
		// resultCode + " requestCode = "+ requestCode);
		registerTFcardPlugReceiver();
		if (Global.MENU_INTERFACE_FLAG != resultCode || Global.MENU_INTERFACE_FLAG != requestCode) { // 菜单界面返回
			Global.debug("onActivityResult === error =");

			// gMeunViewFlag = false;
			// mMenuView = null;

			super.onResume();
			return;
		}

		int selectItem = 0; // 先获取子菜单上次的设置值
		selectItem = data.getIntExtra("selectItem", selectItem);
		int flag = 0;
		flag = data.getIntExtra("goback", flag);
		if (flag == 1) {
			updateShowList();
			return;
		}
		// gSelect = data.getIntExtra("selectItem", selectItem);
		// String selectStr = data.getStringExtra("selectStr");
		Global.debug("onActivityResult === selectItem =" + selectItem);

		if (selectItem == Global.COPY_ID) { // 复制

		} else if (selectItem == Global.CUT_ID) { // 剪切

		} else if (selectItem == Global.DEl_ID || selectItem == Global.CLEAN_ID) { // 删除
																					// 清空
			// gmyHandler.sendEmptyMessage(0);

			Global.debug("mHandler  ={}====0000");
			showList(Global.gPath.get(Global.gPathNum - 1), Global.gName.get(Global.gPathNum - 1));
			if (Global.gtempID > (mMenuList.size() - 1)) {
				Global.gtempID = 0;// mMenuList.size() - 1;
			}
			mMenuView.setSelectItem(Global.gtempID);
			// onResume();

		} else if (selectItem == Global.PASTE_ID) { // 粘贴
			Global.debug("PASTE_ID == ");
			Global.debug("PASTE_ID == " + Global.gPath.get(Global.gPathNum - 1));
			Global.gCopyPath_desk = Global.gPath.get(Global.gPathNum - 1);

			showList(Global.gPath.get(Global.gPathNum - 1), Global.gName.get(Global.gPathNum - 1));
			Global.debug("Global.gPastName ====== " + Global.gPastName);
			Global.debug("Global.gFilePaths.indexOf(Global.gPastName) ====== "
					+ Global.gFilePaths.indexOf(Global.gPastName));
			if (null != Global.gPastName) {
				Global.gtempID = Global.gFilePaths.indexOf(Global.gPastName);
				if (Global.gtempID <= 0) {
					Global.gtempID = 0;
				}
			}

			mMenuView.setSelectItem(Global.gtempID);
			// onResume();
		}

		// gMeunViewFlag = false;
	}

	// 键按下
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		// Global.debug("onKeyDown ----------- keyCode = "+ keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Global.debug("onKeyDown -------KEYCODE_BACK--- keyCode = "+
			// keyCode);
			// Global.debug("gPathNum === " + gPathNum);
			for (int i = 0; i < Global.gPathNum; i++) {
				Global.debug("i ==" + i + "gPathNum === " + Global.gPath.get(i).toString());
			}
			Global.debug("Global.gPathNum === " + Global.gPathNum);
			if (Global.gPathNum > 1) {
				gSelecyId[Global.gPathNum] = 0;
				Global.gPathNum--;
				Global.debug("onKeyDown -------KEYCODE_BACK--- gPathNum = " + Global.gPathNum);

				if (1 == Global.gPathNum) {
					Global.gName.remove(1);
					Global.gPath.remove(1);

					getTopDirList();
					setListData(Global.gFileName);
					mMenuView.setSelectItem(gSelecyId[Global.gPathNum]);
					Global.debug("[******]gSelecyId[gPathNum] === " + gSelecyId[Global.gPathNum]);
					setTitle(getResources().getString(R.string.app_name));
					super.onResume();
				} else {
					// Global.gFilePaths.get(Global.gPathNum).toString();
					showList(Global.gPath.get(Global.gPathNum - 1).toString(),
							Global.gName.get(Global.gPathNum - 1).toString());

					Global.gName.remove(Global.gPathNum);
					Global.gPath.remove(Global.gPathNum);
					onResume();
				}
			} else {
				finish();
			}

			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) // ok
																									// 键
		{
			/*
			 * if(mMenuList.size() <= 0) { PromptDialog mPromptDialog = new
			 * PromptDialog(this, getResources().getString(R.string.no_file));
			 * mPromptDialog.show(); return true; }
			 */
			KeyUp_Enter(getSelectItem());

			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			// Global.debug("onKeyDown -------KEYCODE_MENU--- Global.gPathNum =
			// "+ Global.gPathNum);
			if (1 == Global.gPathNum) {
				return true;
			}
			showMenuList();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mMenuList.size() <= 0) {
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.no_file));
				mPromptDialog.show();
			} else {
				super.onKeyUp(keyCode, event);
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	// 显示list
	private boolean showList(String filePath, String fileName) {

		Global.debug("\r\n [showList] filePath == " + filePath + " fileName == " + fileName);

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
				// 获取文件夹内文件
				File[] mFiles = mFile.listFiles(ff);

				Global.gFileName.clear();
				Global.gFilePaths.clear();

				/* 获取文件列表 */
				if (mFiles.length > 0) {
					ArrayList<FileInfo> mFileFile = new ArrayList<FileInfo>();
					ArrayList<FileInfo> mFileFolder = new ArrayList<FileInfo>();
					// FileInfo temp_File = new FileInfo();
					// 循环获取文件夹列表
					for (File mCurrentFile : mFiles) {
						if (mCurrentFile.getName().equals("LOST.DIR")) // 去除LOST.DIR
						{
							continue;
						}
						File mFile1 = new File(mCurrentFile.getPath()); // 获取路径内容

						if (mFile1.isDirectory()) {
							FileInfo mFileInfo = new FileInfo();
							mFileInfo.name = mCurrentFile.getName();
							mFileInfo.path = mCurrentFile.getPath();
							// Global.debug("mCurrentFile.getPath()
							// ="+mCurrentFile.getPath() + "
							// mCurrentFile.getName() =
							// "+mCurrentFile.getName());
							mFileFolder.add(mFileInfo);
						} else {
							FileInfo mFileInfo = new FileInfo();
							mFileInfo.name = mCurrentFile.getName();
							mFileInfo.path = mCurrentFile.getPath();
							// Global.debug("mCurrentFile.getPath()
							// ="+mCurrentFile.getPath() + "
							// mCurrentFile.getName() =
							// "+mCurrentFile.getName());
							mFileFile.add(mFileInfo);
						}
					}
					Collections.sort(mFileFile, new UsernameComparator()); // 通过重写Comparator的实现类FileComparator来实现按文件名排序。
					Collections.sort(mFileFolder, new UsernameComparator());
					// 文件夹在前
					for (int i = 0; i < mFileFolder.size(); i++) {
						Global.gFileName.add(mFileFolder.get(i).name);
						Global.gFilePaths.add(mFileFolder.get(i).path);
					}
					// 文件在后
					for (int i = 0; i < mFileFile.size(); i++) {
						Global.gFileName.add(mFileFile.get(i).name);
						Global.gFilePaths.add(mFileFile.get(i).path);
					}
					mFileFile.clear();
					mFileFolder.clear();

					mMenuList = Global.gFileName;
					mMenuView.setListData(mMenuList);
					mMenuView.setSelectItem(gSelecyId[Global.gPathNum]);
				} else { // 无文件
					mMenuList = Global.gFileName;
					mMenuView.setListData(mMenuList);
				}
				// Global.debug("*************************1111**********");
				mTitle = fileName;
				mMenuView.setTitle(fileName);
				Global.debug("\r\n***************1111**********mMenuList.size() ==" + mMenuList.size());
			}
			return true;
		} else {
			// 没有读写权限时
			Global.debug("can not read === filePath =" + filePath);
			// Toast.makeText(MainActivity.this,
			// getResources().getString(R.string.limits),
			// Toast.LENGTH_SHORT).show();
			// TtsUtils.getInstance().speak(getResources().getString(R.string.limits));
			String path = null;
			if (filePath.equals(Global.USB_PATH)) {
				path = getResources().getString(R.string.usb_notexit);
			} else if (filePath.equals(Global.USER_PATH)) {
				path = getResources().getString(R.string.tf_notexit);
			} else {
				path = getResources().getString(R.string.file_error);
			}
			PromptDialog mPromptDialog = new PromptDialog(this, path);
			mPromptDialog.show();
			mPromptDialog.setPromptListener(new PromptListener() {

				@Override
				public void onComplete() {
					mHandler.sendEmptyMessage(Global.MSG_MAIN);
				}
			});
			return false;
		}
		// Global.debug("*************************2222**********");
	}

	// 按键按下
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// mMainView.onKeyUp(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mMenuList.size() <= 0) {
				return true;
			} else {
				super.onKeyDown(keyCode, event);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// tts 发音
	public void SpeakContent(int speakWay, String Text) {

		if (speakWay == 0) {
			TtsUtils.getInstance().speak(Text);
		} else {
			TtsUtils.getInstance().speak(Text, TtsUtils.TTS_QUEUE_ADD);
		}
	}

	// TTS读函数
	public void SpeakContentend(String Text) {
		TtsUtils.getInstance().speak(Text);

		delay(2000); // 为何要延时??? 为了将音频读完
	}

	public void delay(int len) {
		try {
			Thread.currentThread();
			Thread.sleep(len);// 毫秒
		} catch (Exception e) {
		}
	}

	// 实现接口 实现自己布局
	@Override
	@SuppressLint("InflateParams")
	public View getView(Context context, int position, View convertView, ViewGroup parent) {

		// Global.debug("getView =====111==position =="+ position + " selectItem
		// =="+ getSelectItem());
		ViewHolder vh = null; // 空

		if (null == convertView) {
			vh = new ViewHolder();
			LayoutInflater mLI = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// 初始化列表元素界面
			convertView = mLI.inflate(R.layout.manage_list_child, null);
			// 获取列表布局界面元素
			vh.ivMenu = (ImageView) convertView.findViewById(R.id.image_list_childs);
			vh.tvMenu = (TextView) convertView.findViewById(R.id.text_list_childs);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		vh.tvMenu.setTag(String.valueOf(position));
		Tools mTools = new Tools(context);

		vh.tvMenu.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel());
		vh.tvMenu.setHeight(mTools.convertSpToPixel(mTools.getFontSize()));

		vh.ivMenu.setMaxHeight(mTools.getFontSize());
		vh.ivMenu.setMaxWidth(mTools.getFontSize());

		vh.ivMenu.setMinimumHeight(mTools.getFontSize());
		vh.ivMenu.setMinimumWidth(mTools.getFontSize());

		if (Global.gFilePaths != null) {
			File mFile = new File(Global.gFilePaths.get(position).toString()); // 获取当前路径
			String fileName = mFile.getName();
			// Global.debug("path === "+ path + "\r\n fileName = "+ fileName);
			vh.tvMenu.setText(fileName);

			if (Global.gFilePaths.get(position).toString().equals(Global.ROOT_PATH)) {
				vh.ivMenu.setImageBitmap(imPhone); // 文件夹
			} else if (Global.gFilePaths.get(position).toString().equals(Global.USER_PATH)) {
				vh.ivMenu.setImageBitmap(imSdcard); // 文件夹
			} else if (Global.gFilePaths.get(position).toString().equals(Global.USB_PATH)) {
				vh.ivMenu.setImageBitmap(imUsb); // 文件夹
			} else if (mFile.isDirectory()) {
				vh.ivMenu.setImageBitmap(imFoler); // 文件夹
			} else {
				vh.ivMenu.setImageBitmap(imFile); // 文件夹
			}
		} else {
			Global.debug("[*********]===gFilePathList === null");
		}

		if (getSelectItem() == position) {
			convertView.setBackgroundColor(mTools.getHighlightColor());
			vh.tvMenu.setSelected(true);
		} else {
			convertView.setBackgroundColor(mTools.getBackgroundColor());
			vh.tvMenu.setSelected(false);
		}

		if (!TextUtils.isEmpty(Global.gFileName.get(position))) {
			vh.tvMenu.setText(Global.gFileName.get(position));
		} else {
			vh.tvMenu.setText("");
		}
		vh.tvMenu.setTextColor(mTools.getFontColor());

		return convertView;
	}

	//
	private class ViewHolder {
		TextView tvMenu = null; // // 菜单名称
		ImageView ivMenu = null; // // 图片
	}

	// 获取当前路径
	private String getCurPath() {
		return Global.gPath.get(Global.gPathNum - 1);
	}

	// 更新界面
	private void updateShowList() {
		Global.gName.clear();
		Global.gPath.clear();

		Global.gPath.add(Global.ROOT_PATH_FLAG);
		Global.gName.add(getResources().getString(R.string.app_name));
		Global.gPathNum = 1;

		getTopDirList();
		interface_flag = Global.MAIN_INTERFACE;

		setListData(Global.gFileName);
		mMenuView.setSelectItem(gSelecyId[Global.gPathNum]);
		Global.debug("[******]gSelecyId[gPathNum] === " + gSelecyId[Global.gPathNum]);
		setTitle(getResources().getString(R.string.app_name));
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

	// 插拔消息 接收
	private BroadcastReceiver tfCardPlugReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Global.debug("\r\n  tfCardPlugReceiver ========2222==================");
			String action = intent.getAction();

			String mData = intent.getDataString(); // 获取路径
			mData = mData.substring(7, mData.length());
			Global.debug("\r\n mData ============== " + mData);
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) { // 插入

				Global.debug("\r\n tf卡插入============== ");
			} else if (Intent.ACTION_MEDIA_EJECT.equals(action)) { // Tf 卡拔出

				String mPath = getCurPath();
				Global.debug("\r\n mPath ============== " + mPath);
				if (mPath.contains(mData)) { // 包含
					if (mData.contains(Global.MENU_PATH_EXTSD)) { // 存储卡
						Global.debug("\r\n tf卡 列表更新============== ");
						updateShowList();
					} else if (mData.contains(Global.MENU_PATH_UDISK)) { // U盘
						Global.debug("\r\n U盘 列表更新============== ");
						updateShowList();
					}
				}
				Global.debug("\r\n tf卡 拔出============== ");
			} else if (Intent.ACTION_MEDIA_REMOVED.equals(action)) {
				Global.debug("\r\n tf卡 ACTION_MEDIA_REMOVED============== ");
			} else if (Intent.ACTION_MEDIA_SHARED.equals(action)) {
				Global.debug("\r\n tf卡 ACTION_MEDIA_SHARED============== ");
			}

		}
	};

	private class UsernameComparator implements Comparator<FileInfo> {
		public int compare(FileInfo entity1, FileInfo entity2) {
			/*
			try {
				String str1 = CharacterParser.getInstance().getSelling( entity1.name );
				String str2 = CharacterParser.getInstance().getSelling( entity2.name );
				return str1.compareToIgnoreCase(str2);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();

				return 0;
			}
			*/
			
			String str1 = "";
			String str2 = "";
			
			String str10 = "";
			String str11 = "";
			String str20 = "";
			String str21 = "";
			
			try 
			{
				str1 = sort(Pinyin4jUtils.converterToSpell( entity1.name ));
				str2 = sort(Pinyin4jUtils.converterToSpell( entity2.name ));
				
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
		
		//对多音字排序
		private String sort( String pinyin )
		{
			if( ( null == pinyin ) || ( TextUtils.isEmpty(pinyin) ) )
			{
				return	pinyin;
			}
			
			String[] strSplit = pinyin.split(",");
			if( ( null == strSplit ) || ( 0 == strSplit.length ) )
			{
				return	pinyin;
			}
			
			ArrayList<String> list = new ArrayList<String>();
			for( int i = 0; i < strSplit.length; i++ )
			{
				boolean isInsert = false;
				for( int j = 0; j < list.size(); j++ )
				{
					if( strSplit[i].compareToIgnoreCase(list.get(j)) < 0 )
					{
						isInsert = true;
						list.add(j, strSplit[i]);
						break;
					}
				}
				
				if( !isInsert )
				{
					list.add(strSplit[i]);
				}
			}
			
			String result = "";
			for( int i = 0; i < list.size(); i++ )
			{
				if( i > 0 )
				{
					result += ",";
				}
				result += list.get(i);
			}
			
			return	result;
		}
	}	


	// 显示 
	private class FileInfo {
		String name; // // 菜单名称
		String path; // // 图片
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Global.MSG_MAIN) { // 音乐播放结束消息
				goMain();
			}

			super.handleMessage(msg);
		}
	};

	private void goMain() {
		gSelecyId[Global.gPathNum] = 0;
		Global.gPathNum--;
		Global.gName.remove(Global.gPathNum);
		Global.gPath.remove(Global.gPathNum);

		Global.debug("\r\n[*******] Global.gPathNum ====== " + Global.gPathNum);
		onResume();

	}

	private void getIntentPara() {
		Intent intent = getIntent();
		mAction = intent.getIntExtra("action", 0);
		mTitle = intent.getStringExtra(MenuConstant.INTENT_KEY_TITLE);
	}
}
