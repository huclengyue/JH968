package com.sunteam.alarm;

import java.io.File;
import java.util.ArrayList;

import com.sunteam.alarm.utils.Global;
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.dao.Alarminfo;
import com.sunteam.dao.GetDbInfo;
import com.sunteam.receiver.Alarmpublic;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class Alarm_MainActivity extends MenuActivity {

	private int ALARM_ID = 0; // 定时闹钟
	private int ANNIVERSARY_ID = 1; // 纪念日
	private int COUNTDOWN_ID = 2; // 倒计时

	private int gInterfaceFlag = 0; // 界面标志

	private int MAIN_INTERFACE = 0; // 主界面

	///
	private int gHour = 0; // 小时
	private int gMin = 0; // 分钟
	private String gFileName = null; // 文件名
	@SuppressWarnings("unused")
	private String gFilePath = null; // 文件名
	private int gType = 0; // 闹钟类型
	private int gOnoff = 0; // 闹钟开关
	private int gMonth = 0; // 月
	private int gDay = 0; // 日
	private int gSQLData_ID = 0; // 数据库 条数
	private int gInfo_ID = 0; // 数据库 条数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		gInterfaceFlag = MAIN_INTERFACE;
		mTitle = getResources().getString(R.string.app_name);
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		String alarm_folder = Alarmpublic.ALARM_PATH + getResources().getString(R.string.folder);
		Global.debug("\r\n【onCreate】 alarm_folder =======" + alarm_folder);
		File mFile = new File(alarm_folder);
		if (!mFile.exists()) {
			Global.debug("\r\n【onCreate】 alarm_folder =====mkdir==");
			makeDirs(alarm_folder);
		}

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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO 自动生成的方法存根
		if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			// TtsUtils.getInstance().stop();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Global.debug("\r\n[Alarm_MainActivity] onKeyUp ===========");
		if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (MAIN_INTERFACE == gInterfaceFlag) { // 在主界面
				if (ALARM_ID == getSelectItem()) { // 定时闹钟
					gInterfaceFlag = Global.ALARM_INTERFACE;

					setTitle(getSelectItemContent());
					mMenuList = getAlarmData();
					setListData(mMenuList);
					mMenuView.setSelectItem(0);
					onResume();
				} else if (ANNIVERSARY_ID == getSelectItem()) { // 纪念日
					gInterfaceFlag = Global.ANNIVERSARY_INTERFACE;
					mMenuList = getAnniversaryData();
					setTitle(getSelectItemContent());

					setListData(mMenuList);
					mMenuView.setSelectItem(0);
					onResume();
				} else if (COUNTDOWN_ID == getSelectItem()) { // 倒计时
					gInterfaceFlag = Global.COUNTDOWN_INTERFACE;
					mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.countdown_list));
					setTitle(getSelectItemContent());
					setListData(mMenuList);
					mMenuView.setSelectItem(0);
					super.onResume();
				}
			} else if (gInterfaceFlag == Global.ALARM_INTERFACE) // 在定时闹钟界面
			{
				gInterfaceFlag = Global.ALARM_INFO_INTERFACE;
				gInfo_ID = getSelectItem();
				ShowAlarmInfo(getSelectItem()); // 进入闹钟设置界面
			} else if (gInterfaceFlag == Global.ANNIVERSARY_INTERFACE) // 在纪念日界面
			{
				gInterfaceFlag = Global.ANNIVERSARY_INFO_INTERFACE;
				gInfo_ID = getSelectItem();
				ShowAnniveInfo(getSelectItem());
			} else if (gInterfaceFlag == Global.COUNTDOWN_INTERFACE) // 在倒计时详情界面
			{
				// gInterfaceFlag = COUNTDOWN_INFO_INTERFACE;
				if (getSelectItem() < Global.COUNT_DOWN_ID4) {
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					int time_len = 0;
					if (getSelectItem() == Global.COUNT_DOWN_ID0) {
						time_len = Global.TIME_LEN0;
					} else if (getSelectItem() == Global.COUNT_DOWN_ID1) {
						time_len = Global.TIME_LEN1;
					} else if (getSelectItem() == Global.COUNT_DOWN_ID2) {
						time_len = Global.TIME_LEN2;
					} else if (getSelectItem() == Global.COUNT_DOWN_ID3) {
						time_len = Global.TIME_LEN3;
					}

					bundle.putInt("TIMELEN", time_len); // 修改项

					intent.putExtras(bundle); // 传入参数
					intent.setClass(this, Alarm_countdownActivity.class);
					startActivity(intent);
				} else { // 自定义
					Intent intent = new Intent();
					Bundle bundle = new Bundle();

					bundle.putInt("ID", getSelectItem()); // 修改项

					intent.putExtras(bundle); // 传入参数
					// intent.setAction("set_action");// 启动设置界面
					intent.setClass(this, Alarm_CountDownSetActivity.class);
					startActivity(intent);
				}
			} else if (gInterfaceFlag == Global.ALARM_INFO_INTERFACE) { // 闹钟详情
																		// 界面
				if (Global.ALARM_SET_TIME == getSelectItem()) {
					StartSetting(getSelectItem());
				} else {
					StartSettingList(getSelectItem());
				}
			} else if (gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE) { // 纪念日详情
																				// 界面
				if (Global.ANNIVERSARY_SET_DATE == getSelectItem() || Global.ANNIVERSARY_SET_TIME == getSelectItem()) {
					StartSetting(getSelectItem());
				} else {
					StartSettingList(getSelectItem());
				}
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) { // 返回

			if (MAIN_INTERFACE == gInterfaceFlag) {
				super.onKeyUp(keyCode, event);
			} else if (gInterfaceFlag == Global.ALARM_INTERFACE) { // 定时闹钟界面

				mTitle = getResources().getString(R.string.app_name);
				mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
				setListData(mMenuList);
				setTitle(mTitle);
				mMenuView.setSelectItem(ALARM_ID);
				gInterfaceFlag = MAIN_INTERFACE;
				super.onResume();
			} else if (gInterfaceFlag == Global.ANNIVERSARY_INTERFACE) { // 纪念日界面
				mTitle = getResources().getString(R.string.app_name);
				mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
				setListData(mMenuList);
				setTitle(mTitle);
				mMenuView.setSelectItem(ANNIVERSARY_ID);
				gInterfaceFlag = MAIN_INTERFACE;
				super.onResume();
			} else if (gInterfaceFlag == Global.COUNTDOWN_INTERFACE) { // 倒计时界面
				mTitle = getResources().getString(R.string.app_name);
				mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
				setListData(mMenuList);
				setTitle(mTitle);
				mMenuView.setSelectItem(COUNTDOWN_ID);
				gInterfaceFlag = MAIN_INTERFACE;
				super.onResume();
			} else if (gInterfaceFlag == Global.ALARM_INFO_INTERFACE) { // 定时闹钟详情界面
				@SuppressWarnings("unchecked")
				ArrayList<String> temp = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
				setTitle(temp.get(0));
				mMenuList = getAlarmData();
				setListData(mMenuList);
				mMenuView.setSelectItem(gInfo_ID); // 默认0
				gInterfaceFlag = Global.ALARM_INTERFACE;
				super.onResume();
			} else if (gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE) { // 纪念日详情界面
				@SuppressWarnings("unchecked")
				ArrayList<String> temp = ArrayUtils.strArray2List(getResources().getStringArray(R.array.main_list));
				setTitle(temp.get(1));
				mMenuList = getAnniversaryData();
				setListData(mMenuList);
				mMenuView.setSelectItem(gInfo_ID); // 默认0
				gInterfaceFlag = Global.ANNIVERSARY_INTERFACE;
				super.onResume();
			} else if (gInterfaceFlag == Global.COUNTDOWN_INFO_INTERFACE) {
				super.onResume();
			}
			return true;
		}
		// else if(keyCode == KeyEvent.KEYCODE_0){
		//
		// testrawplay();
		// }
		return super.onKeyUp(keyCode, event);

	}

	// 显示 闹钟详情
	@SuppressWarnings("unchecked")
	private void ShowAlarmInfo(int selectItem) {
		// TODO 自动生成的方法存根

		mMenuList.clear(); // 清空列表

		gHour = 0; // 小时
		gMin = 0; // 分钟
		gFileName = null; // 文件名
		gFilePath = null; // 文件名
		gType = 0; // 闹钟类型
		gOnoff = 0; // 闹钟开关
		gMonth = 0; // 月
		gDay = 0; // 日

		gSQLData_ID = selectItem + 1;
		Alarminfo alarminfo = new Alarminfo(); // 创建 结构体
		GetDbInfo dbAlarmInfo = new GetDbInfo(this); // 打开数据库

		alarminfo = dbAlarmInfo.find(selectItem + 1, Alarmpublic.ALARM_TABLE);
		dbAlarmInfo.closeDb();

		gHour = alarminfo.hour;
		gMin = alarminfo.minute;
		gFileName = alarminfo.filename;
		gFilePath = alarminfo.path;
		gType = alarminfo.type;
		gOnoff = alarminfo.onoff;

		String temp = "";
		// 时间显示
		temp += getResources().getString(R.string.time);
		if (gHour < 10) {
			temp += "0";
		}
		temp += gHour;
		temp += ":";
		if (gMin < 10) {
			temp += "0";
		}
		temp += gMin;

		mMenuList.add(temp);

		// 文件显示
		temp = "";
		temp += getResources().getString(R.string.music) + gFileName;
		mMenuList.add(temp);

		// 显示闹钟类型
		temp = "";
		temp += getResources().getString(R.string.type);
		if (Alarmpublic.ALARM_TYPE1 == gType) {
			temp += getResources().getString(R.string.type1);
		} else if (Alarmpublic.ALARM_TYPE2 == gType) {
			temp += getResources().getString(R.string.type2);
		} else if (Alarmpublic.ALARM_TYPE3 == gType) {
			temp += getResources().getString(R.string.type3);
		}
		mMenuList.add(temp);

		// 显示开关
		temp = "";
		temp += getResources().getString(R.string.onoff);
		if (gOnoff == Alarmpublic.ALARM_OFF) {

			temp += getResources().getString(R.string.off);
		} else {
			temp += getResources().getString(R.string.on);
		}
		mMenuList.add(temp);

		setListData(mMenuList);
		setTitle(getResources().getString(R.string.alarm));
		mMenuView.setSelectItem(0);

		onResume();
	}

	// 显示 纪念日详情
	@SuppressWarnings("unchecked")
	private void ShowAnniveInfo(int selectItem) {
		// TODO 自动生成的方法存根

		mMenuList.clear(); // 清空列表
		gHour = 0; // 小时
		gMin = 0; // 分钟
		gFileName = null; // 文件名
		gFilePath = null; // 文件名
		gType = 0; // 闹钟类型
		gOnoff = 0; // 闹钟开关
		gMonth = 0; // 月
		gDay = 0; // 日

		gSQLData_ID = selectItem + 1;
		Alarminfo alarminfo = new Alarminfo(); // 创建 结构体
		GetDbInfo dbAlarmInfo = new GetDbInfo(this); // 打开数据库

		alarminfo = dbAlarmInfo.find(selectItem + 1, Alarmpublic.ANNIVERSARY_TABLE);
		dbAlarmInfo.closeDb();

		gHour = alarminfo.hour;
		gMin = alarminfo.minute;
		gFileName = alarminfo.filename;
		gFilePath = alarminfo.path;
		gType = alarminfo.type;
		gOnoff = alarminfo.onoff;
		gMonth = alarminfo.month;
		gDay = alarminfo.day;

		String temp = "";
		/*
		 * // 显示日期 Calendar calendar = Calendar.getInstance(); // 获取日历
		 * 
		 * //String year = Integer.toString(calendar.get(Calendar.YEAR)); gMonth
		 * = calendar.get(Calendar.MONTH) + 1; // 月份从 0开始 gDay =
		 * calendar.get(Calendar.DAY_OF_MONTH);
		 */
		temp += getResources().getString(R.string.date) + gMonth + getResources().getString(R.string.month) + gDay
				+ getResources().getString(R.string.day);

		mMenuList.add(temp);

		temp = "";
		// 时间显示
		temp += getResources().getString(R.string.time);
		if (gHour < 10) {
			temp += "0";
		}
		temp += gHour;
		temp += ":";
		if (gMin < 10) {
			temp += "0";
		}
		temp += gMin;

		mMenuList.add(temp);

		// 文件显示
		temp = "";
		temp += getResources().getString(R.string.music) + gFileName;
		mMenuList.add(temp);

		// 显示开关
		temp = "";
		temp += getResources().getString(R.string.onoff);
		if (gOnoff == Alarmpublic.ALARM_OFF) {

			temp += getResources().getString(R.string.off);
		} else {
			temp += getResources().getString(R.string.on);
		}
		mMenuList.add(temp);

		setListData(mMenuList);
		setTitle(getResources().getString(R.string.annive));
		mMenuView.setSelectItem(0);
		onResume();
	}

	// 获取定时闹钟的数据
	private ArrayList<String> getAlarmData() {

		Alarminfo alarminfo = new Alarminfo(); // 创建 结构体
		GetDbInfo dbAlarmInfo = new GetDbInfo(this); // 打开数据库

		int max_id = dbAlarmInfo.getMaxId(Alarmpublic.ALARM_TABLE);
		Global.debug("\r\n getAlarmData ===== max_id = " + max_id);
		if (0 == max_id) { // 没有数据 则要添加数据
			for (int i = 0; i < Alarmpublic.MAX_NUM; i++) {
				alarminfo._id = i + 1;
				alarminfo.year = 0;
				alarminfo.month = 0;
				alarminfo.day = 0;
				alarminfo.hour = Alarmpublic.DEF_HOUR;
				alarminfo.minute = Alarmpublic.DEF_MIN;
				alarminfo.filename = Alarmpublic.ALARM_FILE_NAME;
				alarminfo.path = Alarmpublic.ALARM_PATH + getResources().getString(R.string.folder) + "/"
						+ Alarmpublic.ALARM_FILE_NAME;
				alarminfo.onoff = Alarmpublic.DEF_ONOFF;
				alarminfo.type = Alarmpublic.DEF_TYPE;

				dbAlarmInfo.add(alarminfo, Alarmpublic.ALARM_TABLE);
			}
		}
		max_id = dbAlarmInfo.getMaxId(Alarmpublic.ALARM_TABLE);
		ArrayList<String> temp = new ArrayList<String>();
		String temp1 = "";
		for (int i = 1; i <= max_id; i++) {
			alarminfo = dbAlarmInfo.find(i, Alarmpublic.ALARM_TABLE);
			// Global.debug("\r\n getAlarmData ===== _id = " + alarminfo._id);
			temp1 = "";
			if (alarminfo.hour < 10) {
				temp1 += "0";
			}
			temp1 = temp1 + alarminfo.hour + ":";

			if (alarminfo.minute < 10) {
				temp1 = temp1 + "0";
			}
			temp1 = temp1 + alarminfo.minute + "  ";

			if (Alarmpublic.ALARM_TYPE1 == alarminfo.type) {
				temp1 = temp1 + getResources().getString(R.string.type1);//
			} else if (Alarmpublic.ALARM_TYPE2 == alarminfo.type) {
				temp1 = temp1 + getResources().getString(R.string.type2);//
			} else if (Alarmpublic.ALARM_TYPE3 == alarminfo.type) {
				temp1 = temp1 + getResources().getString(R.string.type3);//
			}
			temp1 += "  ";

			if (Alarmpublic.ALARM_ON == alarminfo.onoff) {
				temp1 += getResources().getString(R.string.on);
			} else {
				temp1 += getResources().getString(R.string.off);
			}

			temp.add(temp1);
		}
		dbAlarmInfo.closeDb();
		return temp;
	}

	// 获取 纪念日的数据
	private ArrayList<String> getAnniversaryData() {
		Alarminfo alarminfo = new Alarminfo(); // 创建 结构体
		GetDbInfo dbAlarmInfo = new GetDbInfo(this); // 打开数据库

		int max_id = dbAlarmInfo.getMaxId(Alarmpublic.ANNIVERSARY_TABLE);
		Global.debug("\r\n getAnniversaryData ===== max_id = " + max_id);
		if (0 == max_id) { // 没有数据 则要添加数据
			for (int i = 0; i < Alarmpublic.MAX_NUM; i++) {
				alarminfo._id = i + 1;
				alarminfo.year = 0;
				alarminfo.month = Alarmpublic.DEF_MONTH;
				alarminfo.day = Alarmpublic.DEF_DAY;
				alarminfo.hour = Alarmpublic.DEF_HOUR;
				alarminfo.minute = Alarmpublic.DEF_MIN;
				// alarminfo.filename = Alarmpublic.ALARM_FILE_NAME;
				// alarminfo.path = Alarmpublic.ALARM_FILE_PATH;
				alarminfo.filename = Alarmpublic.ALARM_FILE_NAME;
				alarminfo.path = Alarmpublic.ALARM_PATH + getResources().getString(R.string.folder) + "/"
						+ Alarmpublic.ALARM_FILE_NAME;
				alarminfo.onoff = Alarmpublic.DEF_ONOFF;
				alarminfo.type = Alarmpublic.DEF_TYPE;

				dbAlarmInfo.add(alarminfo, Alarmpublic.ANNIVERSARY_TABLE);
			}
		}
		max_id = dbAlarmInfo.getMaxId(Alarmpublic.ANNIVERSARY_TABLE);
		ArrayList<String> temp = new ArrayList<String>();
		String temp1 = "";
		for (int i = 1; i <= max_id; i++) {
			alarminfo = dbAlarmInfo.find(i, Alarmpublic.ANNIVERSARY_TABLE);

			temp1 = "" + alarminfo.month + getResources().getString(R.string.month) + alarminfo.day
					+ getResources().getString(R.string.day) + "  ";

			if (alarminfo.hour < 10) {
				temp1 += "0";
			}
			temp1 = temp1 + alarminfo.hour + ":";

			if (alarminfo.minute < 10) {
				temp1 = temp1 + "0";
			}
			temp1 = temp1 + alarminfo.minute + "  ";

			if (1 == alarminfo.onoff) {
				temp1 += getResources().getString(R.string.on);
			} else {
				temp1 += getResources().getString(R.string.off);
			}

			temp.add(temp1);
		}
		dbAlarmInfo.closeDb();
		return temp;
	}

	// 从写setResultCode 函数 打开详情 选择 输入模式
	public void StartSetting(int selectItem) {

		Intent intent = new Intent();
		Bundle bundle = new Bundle();//

		bundle.putInt("ID", selectItem); // 修改项
		bundle.putInt("FLAG", gInterfaceFlag); // 修改的参数是？

		if (gInterfaceFlag == Global.ALARM_INFO_INTERFACE) {
			bundle.putInt("HOUR", gHour); // 修改的参数是？
			bundle.putInt("MIN", gMin); // 修改的参数是？
		} else if (gInterfaceFlag == Global.ANNIVERSARY_INFO_INTERFACE) {
			bundle.putInt("HOUR", gHour); // 修改的参数是？
			bundle.putInt("MIN", gMin); // 修改的参数是？
			bundle.putInt("MONTH", gMonth); // 修改的参数是？
			bundle.putInt("DAY", gDay); // 修改的参数是？
		}
		// intent.setClass(MainActivity.this, MenuActivity.class);
		intent.putExtras(bundle); // 传入参数
		// intent.setAction("set_action");// 启动设置界面
		intent.setClass(this, Alarm_SettingActivity.class);

		startActivityForResult(intent, Global.FLAG_CODE); // 设置标志
	}

	// 打开详情 选择 列表模式
	public void StartSettingList(int selectItem) {

		Intent intent = new Intent();
		Bundle bundle = new Bundle();//

		bundle.putInt("ID", selectItem); // 修改项
		bundle.putInt("FLAG", gInterfaceFlag); // 修改的界面
		bundle.putString("FILENAME", gFileName); // 反显当前 音频
		bundle.putInt("ALARMTYPE", gType); // 反显当前 类型
		bundle.putInt("ONOFF", gOnoff); // 反显当前 开关

		intent.putExtras(bundle); // 传入参数
		intent.setClass(this, Alarm_SetInfoActivity.class);

		startActivityForResult(intent, Global.FLAG_CODE_SET_LIST); // 设置标志
	}

	// 参数返回 从设置界面返回
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Global.FLAG_CODE && resultCode == Global.FLAG_CODE) {
			Global.debug("\r\n onActivityResult ====== Global.FLAG_CODE = " + Global.FLAG_CODE);
			Bundle bundle = data.getExtras();

			int selectId = bundle.getInt("SELECTID");
			int intface = bundle.getInt("FLAG"); // 获取是那个界面

			Global.debug("\r\n selectId ====== " + selectId);
			Global.debug("\r\n intface ====== " + intface);
			Alarminfo alarminfo = new Alarminfo();
			GetDbInfo dbAlarmInfo = new GetDbInfo(this); // 打开数据库

			if (intface == Global.ALARM_INFO_INTERFACE) { // 闹钟详情界面

				alarminfo = dbAlarmInfo.find(gSQLData_ID, Alarmpublic.ALARM_TABLE);
				dbAlarmInfo.closeDb();

				if (selectId == Global.ALARM_SET_TIME) // 时间设置
				{
					gHour = bundle.getInt("HOUR");
					gMin = bundle.getInt("MIN");

					Global.debug("\r\n gHour ====== " + gHour);
					Global.debug("\r\n gMin ====== " + gMin);
					alarminfo.hour = gHour;
					alarminfo.minute = gMin;
				}
				UpdateAlarmData(alarminfo);

				ShowAlarmInfo(gSQLData_ID - 1);
				mMenuView.setSelectItem(selectId);
				// onResume();
			} else if (intface == Global.ANNIVERSARY_INFO_INTERFACE) { // 纪念日详情界面

				alarminfo = dbAlarmInfo.find(gSQLData_ID, Alarmpublic.ANNIVERSARY_TABLE);
				dbAlarmInfo.closeDb();

				gHour = bundle.getInt("HOUR");
				gMin = bundle.getInt("MIN");
				gDay = bundle.getInt("DAY");
				gMonth = bundle.getInt("MONTH");

				alarminfo.month = gMonth;
				alarminfo.day = gDay;
				alarminfo.hour = gHour;
				alarminfo.minute = gMin;

				UpdateAnniversaryData(alarminfo);

				ShowAnniveInfo(gSQLData_ID - 1);
				mMenuView.setSelectItem(selectId);
				// super.onResume();
			}
			Global.debug("[*********]onActivityResult temp_id =" + selectId);
		} else if (requestCode == Global.FLAG_CODE_SET_LIST && resultCode == Global.FLAG_CODE_SET_LIST) {
			// Global.debug("\r\n onActivityResult ======
			// Global.FLAG_CODE_SET_LIST = " + Global.FLAG_CODE_SET_LIST);
			Bundle bundle = data.getExtras();

			int selectId = bundle.getInt("SELECTID");
			int intface = bundle.getInt("FLAG"); // 获取是那个界面

			Alarminfo alarminfo = new Alarminfo();

			Global.debug("\r\n selectId ===[2]=== " + selectId);
			Global.debug("\r\n intface ====[2]== " + intface);
			onResume();
			if (intface == Global.ALARM_INFO_INTERFACE) { // 闹钟详情界面

				GetDbInfo dbAlarmInfo = new GetDbInfo(Alarm_MainActivity.this); // 打开数据库

				alarminfo = dbAlarmInfo.find(gSQLData_ID, Alarmpublic.ALARM_TABLE);
				dbAlarmInfo.closeDb();

				if (selectId == Global.ALARM_SET_MUSIC) // 时间设置
				{
					gFileName = bundle.getString("FILENAME");
					Global.debug("id ====[1] ===gFileName= " + gFileName);
					alarminfo.filename = gFileName;
					String alarm_folder = Alarmpublic.ALARM_PATH + getResources().getString(R.string.folder);
					alarminfo.path = alarm_folder + "/" + gFileName;
				} else if (selectId == Global.ALARM_SET_TYPE) { //

					int id = bundle.getInt("ID"); // 反显项

					Global.debug("id ====[2] ==== " + id);
					if (Alarmpublic.ALARM_TYPE1 == id) { // 仅闹一次
						alarminfo.type = Alarmpublic.ALARM_TYPE1;
					} else if (Alarmpublic.ALARM_TYPE2 == id) // z工作日
					{
						alarminfo.type = Alarmpublic.ALARM_TYPE2;
					} else if (Alarmpublic.ALARM_TYPE3 == id) {
						alarminfo.type = Alarmpublic.ALARM_TYPE3;
					}
				} else if (selectId == Global.ALARM_SET_ONOFF) {
					int id = bundle.getInt("ID"); // 反显项
					Global.debug("\r\n id ====[3] ==== " + id);
					if (Alarmpublic.ALARM_OFF == id) { // 关
						alarminfo.onoff = Alarmpublic.ALARM_OFF;
						alarminfo.setOnoff(Alarmpublic.ALARM_OFF);
					} else if (Alarmpublic.ALARM_ON == id) // 开
					{
						alarminfo.onoff = Alarmpublic.ALARM_ON;
						alarminfo.setOnoff(Alarmpublic.ALARM_ON);
					}
				}
				UpdateAlarmData(alarminfo);
				ShowAlarmInfo(gSQLData_ID - 1);
				mMenuView.setSelectItem(selectId);
				// onResume();
			} else if (intface == Global.ANNIVERSARY_INFO_INTERFACE) { // 纪念日详情界面

				GetDbInfo dbAlarmInfo = new GetDbInfo(Alarm_MainActivity.this); // 打开数据库
				alarminfo = dbAlarmInfo.find(gSQLData_ID, Alarmpublic.ANNIVERSARY_TABLE);
				dbAlarmInfo.closeDb();
				onResume();
				if (selectId == Global.ANNIVERSARY_SET_MUSIC) // 音乐
				{
					gFileName = bundle.getString("FILENAME");
					Global.debug("id ====[1] ===gFileName= " + gFileName);
					alarminfo.filename = gFileName;
					String alarm_folder = Alarmpublic.ALARM_PATH + getResources().getString(R.string.folder);
					alarminfo.path = alarm_folder + "/" + gFileName;
					// alarminfo.path = Alarmpublic.ALARM_PATH + gFileName;
				} else if (selectId == Global.ANNIVERSARY_SET_ONOFF)// 开关
				{
					int id = bundle.getInt("ID");
					Global.debug("id ====[13] ==== " + id);
					if (Alarmpublic.ALARM_OFF == id) { // 关
						alarminfo.onoff = Alarmpublic.ALARM_OFF;
					} else if (Alarmpublic.ALARM_ON == id) { // 开
						alarminfo.onoff = Alarmpublic.ALARM_ON;
					}
				}

				UpdateAnniversaryData(alarminfo);
				ShowAnniveInfo(gSQLData_ID - 1);
				mMenuView.setSelectItem(selectId);
				// onResume();
			}

			Global.debug("[*********]onActivityResult temp_id =" + selectId);
		}
	}

	// 更新 纪念日的数据
	private void UpdateAnniversaryData(Alarminfo alarminfo) {

		GetDbInfo dbAlarmInfo = new GetDbInfo(this); // 打开数据库
		dbAlarmInfo.update(alarminfo, Alarmpublic.ANNIVERSARY_TABLE);
		dbAlarmInfo.closeDb();
		Alarmpublic.UpateAlarm(Alarm_MainActivity.this);
	}

	// 更新 纪念日的数据
	private void UpdateAlarmData(Alarminfo alarminfo) {

		// GetDbInfo dbAlarmInfo = new GetDbInfo( this ); // 打开数据库
		GetDbInfo dbAlarmInfo = new GetDbInfo(Alarm_MainActivity.this); // 打开数据库
		// Global.debug("UpdateAlarmData alarminfo.type ="+ alarminfo.type);
		// Global.debug("UpdateAlarmData alarminfo.onoff ="+ alarminfo.onoff);
		// Global.debug("UpdateAlarmData alarminfo.filename ="+
		// alarminfo.filename);
		dbAlarmInfo.update(alarminfo, Alarmpublic.ALARM_TABLE);
		dbAlarmInfo.closeDb();

		Alarmpublic.UpateAlarm(Alarm_MainActivity.this);
	}

	/*
	 * private void testrawplay() { // TODO 自动生成的方法存根 boolean D = false; Handler
	 * mHandler = null; if(false){ MediaPlayer mediaPlayer=
	 * MediaPlayer.create(this, R.raw.alarm); mediaPlayer.start(); }
	 * 
	 * if(true){ Intent mIntent = new Intent(this ,
	 * Alarm_receiver_Activity.class); //Bundle bundle = new Bundle();//
	 * 
	 * //bundle.putInt("FLAG", Alarmpublic.BOOT_FLAG); // 修改项
	 * mIntent.putExtra("FLAG", Alarmpublic.BOOT_FLAG); // 传入参数
	 * mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(mIntent);
	 * }
	 * 
	 * if(D){ MediaPlayer myPlayer = new
	 * MediaPlayer();//MyPlayer.getInstance(this,mHandler); //((MyPlayer)
	 * myPlayer).setOnStateChangedListener((OnStateChangedListener) this);
	 * 
	 * Global.debug("\r\nKeyEvent.KEYCODE_0  ============ ");
	 * 
	 * 
	 * AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.alarm);
	 * 
	 * Global.debug(
	 * "\r\n[4444] gFilename =====fileDescriptor.getFileDescriptor()==" +
	 * fd.getFileDescriptor());
	 * 
	 * try { myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	 * myPlayer.setDataSource(fd.getFileDescriptor()); myPlayer.prepare();
	 * myPlayer.start(); } catch (IllegalArgumentException e) { // TODO 自动生成的
	 * catch 块 e.printStackTrace(); } catch (IllegalStateException e) { // TODO
	 * 自动生成的 catch 块 e.printStackTrace(); } catch (IOException e) { // TODO
	 * 自动生成的 catch 块 e.printStackTrace(); } Global.debug(
	 * "\r\n startPlay2   ==========666=="); } //((MyPlayer)
	 * myPlayer).startPlayback2(((MyPlayer) myPlayer).playProgress(),
	 * fd.getFileDescriptor(), true); }
	 */

	public static boolean makeDirs(String filePath) {

		if (filePath == null || filePath.isEmpty()) {
			return false;
		}

		File folder = new File(filePath);
		return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
	}
}
