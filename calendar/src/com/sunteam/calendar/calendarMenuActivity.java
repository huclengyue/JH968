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
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class calendarMenuActivity extends MenuActivity {
	// 全局变量
	// private FrameLayout mFlContainer = null; // 帧 数据
	// private MainView mMenuView = null; // 主界面

	private int gYear = 0; // 年
	private int gMonth = 0; // 月
	private int gDay = 0; // 日

	private int gSelectId = 0; //

	private int ADD_REMIND_ID = 0; // 增加提醒
	private int REMIND_ID = 1; // 查看提醒
	private int DEL_REMIND_ID = 2; // 删除提醒
	private int DELALL_REMIND_ID = 3; // 清空提醒

	private int gInterfaceflag = 0; // 界面标志
	private int INTERFACE_MENU = 0; // 菜单界面
	private int INTERFACE_RENID_LIST = 1; // 查看 提醒界面
	private int INTERFACE_DEL_REMIND = 2; // 删除提醒界面

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setContentView(R.layout.activity_menu);

		Intent intent = getIntent(); // 获取Intent
		Bundle bundle = intent.getExtras(); // 获取Bundle

		gYear = bundle.getInt("YEAR"); // 获取 年
		gMonth = bundle.getInt("MONTH"); // 获取 月
		gDay = bundle.getInt("DAY"); // 获取 月

		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));

		mTitle = getResources().getString(R.string.title_menu);
		gInterfaceflag = INTERFACE_MENU;
		super.onCreate(savedInstanceState);
	}

	// 键按下
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) // 返回按键
		{
			if ((gInterfaceflag == INTERFACE_RENID_LIST) || (gInterfaceflag == INTERFACE_DEL_REMIND)) // 查看/删除提醒
																										// 界面
			{
				mHandler.sendEmptyMessage(Global.MSG_TOMAIN);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	// 键按下
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
				|| keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mMenuList.size() <= 0) {
				PromptDialog mpDialog = new PromptDialog(this, getResources().getString(R.string.no_remind));
				mpDialog.show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// 重新实现 方法 ok键处理
	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		Global.debug("setResultCode =============gInterfaceflag =" + gInterfaceflag);
		gSelectId = selectItem;
		if (gInterfaceflag == INTERFACE_MENU) // 菜单界面
		{
			if (selectItem == ADD_REMIND_ID) { // 增加提醒
				Intent intent = new Intent();
				Bundle bundle = new Bundle();//

				bundle.putInt("CALLID", Global.REMIND_CALL_ADD_MENU);

				bundle.putInt("YEAR", gYear);
				bundle.putInt("MONTH", gMonth);
				bundle.putInt("DAY", gDay);
				// intent.setClass(MainActivity.this, MenuActivity.class);
				intent.putExtras(bundle); // 传入参数
				intent.setClass(this, RemindActivity.class);

				startActivityForResult(intent, Global.REMIND_ADD_FLAG_ID); // 设置标志
			} else if (selectItem == REMIND_ID) // 查看提醒
			{
				mMenuList = getDbdata();
				if (mMenuList.size() > 0) {
					setListData(mMenuList);
					mTitle = getResources().getString(R.string.list_remind);
					setTitle(mTitle);
					mMenuView.setSelectItem(0);
					gInterfaceflag = INTERFACE_RENID_LIST;
					mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
				} else {
					PromptDialog mPrompt = new PromptDialog(this, getResources().getString(R.string.no_remind));
					mPrompt.setPromptListener(new PromptListener() {

						@Override
						public void onComplete() {
							// TODO 自动生成的方法存根
							/*
							 * mMenuList =
							 * ArrayUtils.strArray2List(getResources().
							 * getStringArray(R.array.menu_list));
							 * 
							 * mTitle =
							 * getResources().getString(R.string.title_menu);
							 * setListData(mMenuList); setTitle(mTitle);;
							 * gInterfaceflag = INTERFACE_MENU;
							 * mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
							 */
							mHandler.sendEmptyMessage(Global.MSG_TO_MAIN);
						}
					});
					mPrompt.show();
				}
			} else if (selectItem == DEL_REMIND_ID) // 删除提醒
			{
				mMenuList = getDbdata();
				if (mMenuList.size() > 0) {
					setListData(mMenuList);
					mTitle = getResources().getString(R.string.del_remind);
					setTitle(mTitle);
					mMenuView.setSelectItem(0);
					gInterfaceflag = INTERFACE_DEL_REMIND;
					mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
				} else {
					TtsUtils.getInstance().speak(getResources().getString(R.string.no_remind));
					PromptDialog mPrompt = new PromptDialog(this, getResources().getString(R.string.no_remind));
					mPrompt.show();
					mPrompt.setPromptListener(new PromptListener() {

						@Override
						public void onComplete() {
							// TODO 自动生成的方法存根
							/*
							 * mMenuList =
							 * ArrayUtils.strArray2List(getResources().
							 * getStringArray(R.array.menu_list));
							 * 
							 * mTitle =
							 * getResources().getString(R.string.title_menu);
							 * setListData(mMenuList); setTitle(mTitle);;
							 * gInterfaceflag = INTERFACE_MENU;
							 * mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
							 */
							mHandler.sendEmptyMessage(Global.MSG_TOMAIN);
						}
					});
				}
			} else if (selectItem == DELALL_REMIND_ID) { // 删除所有提醒

				ConfirmDialog mConfirmDialog = new ConfirmDialog(this,
						getResources().getString(R.string.ask_delall_Remind), getResources().getString(R.string.ok),
						getResources().getString(R.string.canel));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {

					@Override
					public void doConfirm() {
						delAllDbdata();
						Alarmpublic.UpateAlarm(calendarMenuActivity.this);
						mHandler.sendEmptyMessage(Global.MSG_DEL_ALL);
					}

					@Override
					public void doCancel() {
						// TODO 自动生成的方法存根
						mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
					}
				});
			}
		} else if (gInterfaceflag == INTERFACE_RENID_LIST) // 查看提醒 界面
		{
			startRemindInfo();
		} else if (gInterfaceflag == INTERFACE_DEL_REMIND) { // 删除列表
			ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.ask_del),
					getResources().getString(R.string.ok), getResources().getString(R.string.canel));
			mConfirmDialog.show();
			mConfirmDialog.setConfirmListener(new ConfirmListener() {

				@Override
				public void doConfirm() {
					// TODO 自动生成的方法存根
					Global.debug("\r\n del id ===== " + gSelectId);
					if (true == delDbdata(gSelectId)) {
						mHandler.sendEmptyMessage(Global.MSG_DEL);
					} else {
						mHandler.sendEmptyMessage(Global.MSG_NO_REMIND);
					}
				}

				@Override
				public void doCancel() {
					// TODO 自动生成的方法存根
					mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
				}
			});

		}
	}

	// 进入提醒详情界面
	private void startRemindInfo() {
		Alarminfo tempinfo = new Alarminfo(); //
		GetDbInfo dbInfo = new GetDbInfo(this); // 打开数据库
		ArrayList<Alarminfo> alarminfos = new ArrayList<Alarminfo>();

		Global.debug("INTERFACE_RENID_LIST=========1111==\r\n");
		alarminfos = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
		Global.debug("INTERFACE_RENID_LIST=========22222==\r\n");
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
		bundle.putString("PATH", /* tempinfo.path */ Global.TIXING_PATH);

		// intent.setClass(MainActivity.this, MenuActivity.class);
		intent.putExtras(bundle); // 传入参数
		intent.setClass(this, RemindActivity.class);
		Global.debug("INTERFACE_RENID_LIST=========33333==\r\n");
		startActivityForResult(intent, Global.REMIND_FLAG_ID); // 设置标志
		Global.debug("INTERFACE_RENID_LIST=========4444==\r\n");

	}

	// 获取list
	private ArrayList<String> getDbdata() {
		ArrayList<String> list = new ArrayList<String>();
		String temp = null;
		// String[] guser_List = new String[]; // list信息
		Alarminfo tempinfo = new Alarminfo(); //
		GetDbInfo dbInfo = new GetDbInfo(this); // 打开数据库
		ArrayList<Alarminfo> alarminfos = new ArrayList<Alarminfo>();

		alarminfos = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
		int max_num = dbInfo.getCount(Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
		Global.debug("getDbdata === max_num = " + max_num);
		if (max_num > 0) // 有数据
		{
			for (int i = 0; i < max_num; i++) {
				tempinfo = alarminfos.get(i);

				temp = null;
				temp = tempinfo.year + "-";
				if (tempinfo.month < 10) {

					temp += "0";
				}
				temp += tempinfo.month + "-";
				if (tempinfo.day < 10) {
					temp += "0";
				}
				temp += tempinfo.day + "  ";

				if (tempinfo.hour < 10) {
					temp += "0" + tempinfo.hour + ":";
				} else {
					temp += tempinfo.hour + ":";
				}

				if (tempinfo.minute < 10) {
					temp += "0" + tempinfo.minute;
				} else {
					temp += tempinfo.minute;
				}
				temp += "  ";
				Global.debug("[***]tempinfo.onoff =====" + tempinfo.onoff);
				if (tempinfo.onoff == Alarmpublic.ALARM_OFF) {
					temp += getResources().getString(R.string.remind_off);
				} else {
					temp += getResources().getString(R.string.remind_on);
				}

				list.add(temp);
				Global.debug("22222 -->" + temp);
			}
			return list;
		} else {
			list.clear();
			return list;
		}
	}

	// 获取list
	private Boolean delDbdata(int id) {
		Alarminfo tempinfo = new Alarminfo(); //
		GetDbInfo dbInfo = new GetDbInfo(this); // 打开数据库
		ArrayList<Alarminfo> allData = new ArrayList<Alarminfo>();

		// int tempid = 0;
		int maxId = dbInfo.getCount(Alarmpublic.REMIND_TABLE); // 获取数据条数
		allData = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
		if (maxId > 0) {
			for (int i = 0; i < maxId; i++) {
				tempinfo = allData.get(i);// .find(i, Alarmpublic.REMIND_TABLE);
				Global.debug("i == " + i + " id ==" + id);
				if (id == i) {
					dbInfo.deteleForOne(tempinfo._id, Alarmpublic.REMIND_TABLE);
					return true;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	// 所处所有数据
	private void delAllDbdata() {
		GetDbInfo dbInfo = new GetDbInfo(this); // 打开数据库

		dbInfo.detele(Alarmpublic.REMIND_TABLE); // getAllData(Alarmpublic.REMIND_TABLE);
		dbInfo.closeDb();
	}

	// on entern
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Global.debug("\r\n onActivityResult requestCode == " + requestCode + " resultCode ==" + resultCode);
		Alarmpublic.UpateAlarm(calendarMenuActivity.this);
		if (requestCode == Global.REMIND_FLAG_ID && resultCode == Global.REMIND_FLAG_ID) {
			mMenuList = getDbdata();
			if (mMenuList != null) {
				setListData(mMenuList);
				mTitle = getResources().getString(R.string.list_remind);
				setTitle(mTitle);
				mMenuView.setSelectItem(gSelectId);

				mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
			} else {
				PromptDialog mPrompt = new PromptDialog(this, getResources().getString(R.string.no_remind));
				mPrompt.show();
				mPrompt.setPromptListener(new PromptListener() {

					@Override
					public void onComplete() {
						/*
						 * // TODO 自动生成的方法存根 mMenuList =
						 * ArrayUtils.strArray2List(getResources().
						 * getStringArray(R.array.menu_list));
						 * 
						 * mTitle =
						 * getResources().getString(R.string.title_menu);
						 * setListData(mMenuList); setTitle(mTitle);;
						 * gInterfaceflag = INTERFACE_MENU;
						 * mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
						 */
						mHandler.sendEmptyMessage(Global.MSG_TO_MAIN);
					}
				});
			}
		}
		// 增加提醒
		else if (requestCode == Global.REMIND_ADD_FLAG_ID && resultCode == Global.REMIND_ADD_FLAG_ID) {
			mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));

			Alarmpublic.UpateAlarm(calendarMenuActivity.this);
			mTitle = getResources().getString(R.string.title_menu);
			gInterfaceflag = INTERFACE_MENU;

			setListData(mMenuList);
			setTitle(mTitle);
			mMenuView.setSelectItem(0);
			mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
		}

	}

	// 处理弹框
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Global.debug("\r\n [calendarMenuActivity] == Handler  msg.what == " + msg.what);
			if (msg.what == Global.MSG_DEL_ALL) { // 音乐播放结束消息
				showDelALLPromptDialog();
			} else if (msg.what == Global.MSG_NO_REMIND) {
				showNoRemindPromptDialog();
			} else if (msg.what == Global.MSG_DEL) {
				showDelPromptDialog();
			} else if (msg.what == Global.MSG_NO_REMIND_TOMAIN) {
				showNoRemindToMainPromptDialog();
			} else if (msg.what == Global.MSG_ONRESUM) {
				onResume();
			} else if (msg.what == Global.MSG_TO_MAIN) {
				showRemindToMainPromptDialog();
			} else if (msg.what == Global.MSG_TOMAIN) { // 直接到主界面
				BackToMain();
			} else if (msg.what == Global.MSG_DEL_OK) {
				delOk();
			} else if (msg.what == Global.MSG_MSG_NO_REMIND_TOMAIN_OK) {
				noremind_Back_Ok();
			}
			super.handleMessage(msg);
		}

	};

	private void noremind_Back_Ok() {

		// TODO 自动生成的方法存根
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));

		mTitle = getResources().getString(R.string.title_menu);
		setListData(mMenuList);
		setTitle(mTitle);
		;
		if (gInterfaceflag == INTERFACE_RENID_LIST) {
			mMenuView.setSelectItem(1);
		} else {
			mMenuView.setSelectItem(2);
		}
		gInterfaceflag = INTERFACE_MENU;

		mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
	}

	private void delOk() {

		int selectid = 0;
		mMenuList = getDbdata();
		Global.debug("\r\n[calendarMenuActivity] ==[showDelPromptDialog]  mMenuList.size() =============="
				+ mMenuList.size());
		Global.debug("\r\n gSelectId ==============" + gSelectId);

		if (mMenuList.size() <= 0) { // 无提醒
			mHandler.sendEmptyMessage(Global.MSG_NO_REMIND_TOMAIN);
		} else if (gSelectId > (mMenuList.size() - 1) && (mMenuList.size() > 0)) {
			selectid = 0;// mMenuList.size() - 1;
			Global.debug("\r\n[calendarMenuActivity] ==[showDelPromptDialog]  selectid ==============" + selectid);
			setListData(mMenuList);
			mMenuView.setSelectItem(selectid);
			mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
		} else {
			selectid = gSelectId;
			setListData(mMenuList);
			Global.debug("\r\n [calendarMenuActivity] ==[showDelPromptDialog] selectid ==============" + selectid);
			mMenuView.setSelectItem(selectid);
			mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
		}

	}

	// 提示无提醒
	private void showNoRemindPromptDialog() {
		PromptDialog mPromptDialog = new PromptDialog(calendarMenuActivity.this,
				getResources().getString(R.string.no_remind));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(new PromptListener() {
			@Override
			public void onComplete() {
				mHandler.sendEmptyMessage(Global.MSG_TO_MAIN);
			}
		});

	}

	private void BackToMain() {
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));

		mTitle = getResources().getString(R.string.title_menu);
		setListData(mMenuList);
		setTitle(mTitle);
		;
		if (gInterfaceflag == INTERFACE_RENID_LIST) {
			mMenuView.setSelectItem(1);
		} else {
			mMenuView.setSelectItem(2);
		}
		gInterfaceflag = INTERFACE_MENU;

		mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
	}

	// 提示无提醒 返回主菜单
	private void showNoRemindToMainPromptDialog() {
		PromptDialog mPromptDialog = new PromptDialog(calendarMenuActivity.this,
				getResources().getString(R.string.no_remind));

		mPromptDialog.setPromptListener(new PromptListener() {

			@Override
			public void onComplete() {
				mHandler.sendEmptyMessage(Global.MSG_MSG_NO_REMIND_TOMAIN_OK);
			}
		});
		mPromptDialog.show();
	}

	// 提示无提醒 返回主菜单
	private void showRemindToMainPromptDialog() {

		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));

		mTitle = getResources().getString(R.string.title_menu);
		setListData(mMenuList);
		setTitle(mTitle);
		;
		if (gInterfaceflag == INTERFACE_RENID_LIST) {
			mMenuView.setSelectItem(1);
		} else if (gInterfaceflag == INTERFACE_DEL_REMIND) {
			mMenuView.setSelectItem(2);
		} else {
			mMenuView.setSelectItem(gSelectId);
		}
		gInterfaceflag = INTERFACE_MENU;

		mHandler.sendEmptyMessage(Global.MSG_ONRESUM);
	}

	// 提示全部删除
	private void showDelALLPromptDialog() {

		Alarmpublic.UpateAlarm(calendarMenuActivity.this);
//		mMenuList = getDbdata();
//		setListData(mMenuList);
//		onResume();

		PromptDialog mPromptDialog = new PromptDialog(calendarMenuActivity.this,
				getResources().getString(R.string.delall_Remind));
		// mPromptDialog.show();
		mPromptDialog.setPromptListener(new PromptListener() {
			@Override
			public void onComplete() {
				mHandler.sendEmptyMessage(Global.MSG_NO_REMIND);
			}
		});

		mPromptDialog.show();
	}

	// 提示删除
	private void showDelPromptDialog() {
		Global.debug("\r\n [calendarMenuActivity] ==[showDelPromptDialog] =======11 == =");
		PromptDialog mPromptDialog = new PromptDialog(calendarMenuActivity.this,
				getResources().getString(R.string.del_ok));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(new PromptListener() {

			@Override
			public void onComplete() {
				mHandler.sendEmptyMessage(Global.MSG_DEL_OK);
			}
		});
	}

}
