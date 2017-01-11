package com.sunteam.massage;

import java.io.File;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.massage.utils.Global;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;

public class FunctionMenuActivity extends MenuActivity implements ConfirmListener, PromptListener{
	private static final int MAX_USER_NUM = 10;
	private static final int USER_PATH_FLAG = 0;// tf卡目录
	private static final int TF_PATH_FLAG = 1;// 内部存储卡目录

	// 定义宏
	private static final int DATA_CLEAR = 0;
	private static final int DATA_TO_ME = 1;
	private static final int DATA_TO_TF = 2;

	//CustomDialog gDialog = null;
	ConfirmDialog gConfirmDialog = null; 
	private int userId = 0; // 用户 ID
	private int funcId = 0; // 记录是 单个用户 还是全部用户
	private final String USER_PATH = "/mnt/extsd"; // 外部存储卡 目录

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTitle = getResources().getString(R.string.menu_title);
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));

		super.onCreate(savedInstanceState);
		// 获取传入参数
		Intent intent = getIntent(); //获取Intent
		Bundle bundle = intent.getExtras(); // 获取Bundle

		userId = bundle.getInt("ID"); // 获取用户ID
	}

	// 确认
	@Override
	public void doConfirm() {
		Global.debug("doConfirm ========================");
		switch (funcId) {
		case DATA_CLEAR: // 清空数据
			cleanDataDB(userId);
			break;
		case DATA_TO_ME: // 导入数据
			importData();
			break;
		case DATA_TO_TF: // 导出数据
			exportData();
			break;
		default:
			break;
		}
	}

	@Override
	public void doCancel() {
		//gDialog.dismiss(); // 閿�姣�
		gConfirmDialog.dismiss();
		super.onResume();
	}

	// 清空数据
	private void cleanDataDB(int userId) {

		Global.debug("SettingUpDataDB ===111111111= ");
		GetDbInfo dbInfo = new GetDbInfo(this, USER_PATH_FLAG); //打开用户盘数据

		Global.debug("SettingUpDataDB ===userId= " + userId);

		if (userId >= MAX_USER_NUM) {
			for (int i = 0; i < MAX_USER_NUM; i++) {
				dbInfo.detele(i);
			}
		} else {
			dbInfo.detele(userId);
		}
		dbInfo.closeDb();
		
		mHandler.sendEmptyMessage(Global.MSG_CLEAR);
	}

	/// TTS 发音
	public void TTS_speak(int speakWay, String text) {
		if (speakWay == 0) {
			TtsUtils.getInstance().stop();
			TtsUtils.getInstance().speak(text, TextToSpeech.QUEUE_FLUSH);
		} else {
			TtsUtils.getInstance().speak(text, TextToSpeech.QUEUE_ADD);
		}
		Global.debug("TTS_speak === " + text);
	}
// TTS等待结束
	public void SpeakContentend(String Text) {
		TtsUtils.getInstance().speak(Text);

		delay(2000); // 延时等待发音
	}
	
	// 延时函数
	public void delay(int len) {
		try {
			Thread.currentThread();
			Thread.sleep(len);// 姣
		} catch (Exception e) {
		}
	}
	
	// 获取路径
	public boolean getSDPath() {
	//	File sdDir = null;
		Global.debug("getSDPath === 111==");
		
		File mFile = new File(USER_PATH);
		Global.debug("getSDPath === 222==USER_PATH = " + USER_PATH);
		Global.debug("getSDPath === 222==USER_PATH = " + mFile.exists());
		
		if(mFile.getTotalSpace()  > 1024*1024)  // 盘大于1M
		{
			return true;
		}
		return false;
		
/*		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 锟叫讹拷sd锟斤拷锟角凤拷锟斤拷锟�
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 
		}

		return sdDir.toString();*/
	}

	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		if (userId < 0) {
			return;
		}

		funcId = selectItem;
		String title = "";
		String confirm = getResources().getString(R.string.ok);
		String cancel = getResources().getString(R.string.cancel);
		if (userId < MAX_USER_NUM) {
			switch (selectItem) {
			case DATA_CLEAR:
				title = getResources().getString(R.string.clear_data);
				break;
			case DATA_TO_ME:
				title = getResources().getString(R.string.data_to_me);
				break;
			case DATA_TO_TF:
				title = getResources().getString(R.string.data_to_TF);
				break;
			default:
				break;
			}
		} else if (userId >= 0) {
			switch (selectItem) {
			case DATA_CLEAR:
				title = getResources().getString(R.string.clear_all_data);
				break;
			case DATA_TO_ME:
				title = getResources().getString(R.string.data_all_to_me);
				break;
			case DATA_TO_TF:
				title = getResources().getString(R.string.data_all_to_TF);
				break;
			default:
				break;
			}
		}
		if (!title.equals("")) {
			createDialog(title, confirm, cancel);
		}
	}

	private void createDialog(String title, String confirm, String cancel) {

		gConfirmDialog = new ConfirmDialog(this, title, confirm, cancel);		
		gConfirmDialog.setConfirmListener(this);
		gConfirmDialog.show();
	}

	// 导出数据
	private boolean importData()
	{

		if ( false == getSDPath()) // 判断TF卡是否存在
		{
			PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_me_tfnotexit));
			mDialog.show();
			mDialog.setPromptListener(this);
			return false;
		} 
		else   // tf卡存在
		{
			if (userId >= MAX_USER_NUM) // 全部用户
			{
				userinfo tempinfo = new userinfo();
				GetDbInfo dbUserInfo = new GetDbInfo(this, USER_PATH_FLAG); // 锟斤拷锟斤拷锟捷匡拷
				GetDbInfo dbTfInfo = new GetDbInfo(this, TF_PATH_FLAG); // 锟斤拷锟斤拷锟捷匡拷
				Global.debug("SettingUpDataDB ===userId= " + userId);

				for (int i = 0; i < MAX_USER_NUM; i++) {
					dbUserInfo.detele(i); // 

					int maxid = dbTfInfo.getMaxId(i); // 导出所有数据
					for (int j = 1; j <= maxid; j++) {
						tempinfo = dbTfInfo.find(i, j);
						dbUserInfo.add(tempinfo, i);
					}
				}
				mHandler.sendEmptyMessage(Global.MSG_IMPORT);	
				return true;
			} 
			else { // 导出单个用户数据
				userinfo tempinfo = new userinfo();
				GetDbInfo dbUserInfo = new GetDbInfo(this, USER_PATH_FLAG); 
				GetDbInfo dbTfInfo = new GetDbInfo(this, TF_PATH_FLAG); 
				Global.debug("\r\n importData ===userId= " + userId);

				dbUserInfo.detele(userId); 
				int maxid = dbTfInfo.getMaxId(userId); 
				Global.debug("\r\n importData ===  maxid= " + maxid);
				for (int j = 1; j <= maxid; j++) {
					Global.debug("\r\n j ===== " + j);
					tempinfo = dbTfInfo.find(userId, j);					
//					Global.debug("tempinfo._id === "+ tempinfo._id);
//					Global.debug("tempinfo.forwork === "+ tempinfo.forwork);
//					Global.debug("tempinfo.overwork === "+ tempinfo.overwork);
//					Global.debug("tempinfo.money === "+ tempinfo.money);
//					Global.debug("year === "+ tempinfo.year+ " month == "+ tempinfo.month + " day == "+ tempinfo.day);				
					dbUserInfo.add(tempinfo, userId);
				}
			
				mHandler.sendEmptyMessage(Global.MSG_IMPORT);
				return true;
			}
		}
	}

// 瀵煎嚭鏁版嵁
	private boolean exportData() {

		if (false == getSDPath()) // 鏃燭F鍗�
		{
			PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_tf_tfnotexit));
			mDialog.show();
			mDialog.setPromptListener(this);
			
			return false;
		} else if (userId >= MAX_USER_NUM) { 
			userinfo tempinfo = new userinfo();
			GetDbInfo dbUserInfo = new GetDbInfo(this, USER_PATH_FLAG);
			GetDbInfo dbTfInfo = new GetDbInfo(this, TF_PATH_FLAG); 
			Global.debug("SettingUpDataDB ===userId= " + userId);

			for (int i = 0; i < MAX_USER_NUM; i++) {
				dbTfInfo.detele(i); 

				int maxid = dbUserInfo.getMaxId(i); 
				Global.debug("SettingUpDataDB ===maxid= " + maxid);
				for (int j = 1; j <= maxid; j++) {
					tempinfo = dbUserInfo.find(i, j);
					
					dbTfInfo.add(tempinfo, i);
				}
			}
			mHandler.sendEmptyMessage(Global.MSG_IMPORT);
	
			return true;
		} else { 
			userinfo tempinfo = new userinfo();
			GetDbInfo dbUserInfo = new GetDbInfo(this, USER_PATH_FLAG); 
			GetDbInfo dbTfInfo = new GetDbInfo(this, TF_PATH_FLAG); 
			Global.debug("SettingUpDataDB ===userId= " + userId);

			dbTfInfo.detele(userId); 
			int maxid = dbUserInfo.getMaxId(userId); 
//			Global.debug("\r\n [ exportData]  maxid == "+ maxid);
			for (int j = 1; j <= maxid; j++) {
//				Global.debug("\r\n[ exportData] j ===== " + j);
				tempinfo = dbUserInfo.find(userId, j);
				
//				Global.debug("tempinfo._id === "+ tempinfo._id);
//				Global.debug("tempinfo.forwork === "+ tempinfo.forwork);
//				Global.debug("tempinfo.overwork === "+ tempinfo.overwork);
//				Global.debug("tempinfo.money === "+ tempinfo.money);
//				Global.debug("year === "+ tempinfo.year+ " month == "+ tempinfo.month + " day == "+ tempinfo.day);
				
				dbTfInfo.add(tempinfo, userId);
			}

			mHandler.sendEmptyMessage(Global.MSG_EXPORT);
			return true;
		}
	}

	@Override
	public void onComplete() {   // 弹出框 显示完处理

		finish();
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_CLEAR){   // 音乐播放结束消息
				showClearPromptDialog();
			}else if(msg.what == Global.MSG_IMPORT){
				showImprotrPromptDialog();		
			}else if(msg.what == Global.MSG_EXPORT){
				showExprotPromptDialog();
			}
			
			super.handleMessage(msg);
		}

			
	};

	
	private void showClearPromptDialog(){
		PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.clear_data_Ok));
		mDialog.setPromptListener(this);
		mDialog.show();
	}
	
	private void showImprotrPromptDialog(){
		PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_me_ok));
		mDialog.setPromptListener(this);
		mDialog.show();
	}
	
	private void showExprotPromptDialog(){
		PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_TF_OK));
		mDialog.setPromptListener(this);
		mDialog.show();
	}
	
}
