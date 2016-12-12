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
//	private String dialogTitle; // 瀵硅瘽妗嗕腑鐨勬爣棰�
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

/*	@Override
	protected void onResume() {
		super.onResume();
//		Global.setContext(this); // 宸茬粡鍦⊿unteamApplication()涓繘琛屼簡璁剧疆锛屾澶勫彲涓嶅繀璁剧疆
	}*/


	// 纭畾閿�
	@Override
	public void doConfirm() {
		//gDialog.dismiss(); // 閿�姣�
		Global.debug("doConfirm ========================");
		switch (funcId) {
		case DATA_CLEAR: // 清空数据
			cleanDataDB(userId);
			//super.onResume();
			break;
		case DATA_TO_ME: // 导入数据
			importData();
			//super.onResume();
			break;
		case DATA_TO_TF: // 导出数据
			exportData();
			//super.onResume();
			break;
		default:
			break;
		}
		
		//finish();		
	}

	@Override
	public void doCancel() {
		//gDialog.dismiss(); // 閿�姣�
		gConfirmDialog.dismiss();
		super.onResume();
	}

	// 清空数据
	private void cleanDataDB(int userId) {
		//Toast.makeText(this, getResources().getString(R.string.clear_data_Ok), Toast.LENGTH_LONG).show();
		//TTS_speak(0, getResources().getString(R.string.clear_data_Ok));
		//SpeakContentend(getResources().getString(R.string.clear_data_Ok));
		//
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
		
		PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.clear_data_Ok));
		mDialog.show();
		mDialog.setPromptListener(this);
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
//		dialogTitle = title;

		gConfirmDialog = new ConfirmDialog(this, title, confirm, cancel);
		
		gConfirmDialog.setConfirmListener(this);
		//setConfirmListener(FunctionMenuActivity.class);
		gConfirmDialog.show();
		//gDialog = new CustomDialog(FunctionMenuActivity.this, FunctionMenuActivity.this, title, confirm, cancel);
		//gDialog.show();
	}

	// 导出数据
	private boolean importData()
	{
//		String path = null;
		//path = getSDPath();
		//path = USER_PATH;
		//if (/*path.isEmpty()*/ false == getSDPath()) // 娌℃湁 TF鍗�
		if ( false == getSDPath()) // 判断TF卡是否存在
		{
			//Toast.makeText(this, getResources().getString(R.string.data_to_me_tfnotexit), Toast.LENGTH_LONG).show();
			//TTS_speak(0, getResources().getString(R.string.data_to_me_tfnotexit));
			//SpeakContentend(getResources().getString(R.string.data_to_me_tfnotexit));
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
				//SpeakContentend(getResources().getString(R.string.data_to_me_ok));
				PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_me_ok));
				mDialog.show();
				mDialog.setPromptListener(this);
				
				return true;
			} 
			else { // 导出单个用户数据
				userinfo tempinfo = new userinfo();
				GetDbInfo dbUserInfo = new GetDbInfo(this, USER_PATH_FLAG); // 鎵撳紑鍐呭瓨鏁版嵁
				GetDbInfo dbTfInfo = new GetDbInfo(this, TF_PATH_FLAG); // 鎵撳紑瀛樺偍鍗℃暟鎹�
				Global.debug("\r\n importData ===userId= " + userId);

				dbUserInfo.detele(userId); // 鍒犻櫎鐢ㄦ埛鏁版嵁
				int maxid = dbTfInfo.getMaxId(userId); // 鑾峰彇鐢ㄦ埛鏈�澶ф暟鎹釜鏁�
				Global.debug("\r\n importData ===  maxid= " + maxid);
				for (int j = 1; j <= maxid; j++) {
					Global.debug("\r\n j ===== " + j);
					tempinfo = dbTfInfo.find(userId, j);
					
					Global.debug("tempinfo._id === "+ tempinfo._id);
					Global.debug("tempinfo.forwork === "+ tempinfo.forwork);
					Global.debug("tempinfo.overwork === "+ tempinfo.overwork);
					Global.debug("tempinfo.money === "+ tempinfo.money);
					Global.debug("year === "+ tempinfo.year+ " month == "+ tempinfo.month + " day == "+ tempinfo.day);
					
					
					dbUserInfo.add(tempinfo, userId);
				}
				//Toast.makeText(this, getResources().getString(R.string.data_to_me_ok), Toast.LENGTH_LONG).show();

				//SpeakContentend(getResources().getString(R.string.data_to_me_ok));
				PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_me_ok));
				mDialog.show();
				mDialog.setPromptListener(this);
				return true;
			}
		}
	}

// 瀵煎嚭鏁版嵁
	private boolean exportData() {
//		String path = null;
//		path = getSDPath();
		//Global.debug("path === " + path);
		//path = USER_PATH;
		//if (/*path.isEmpty()/*  false == getSDPath()) // 鏃燭F鍗�
		if (false == getSDPath()) // 鏃燭F鍗�
		{
			//Toast.makeText(this, getResources().getString(R.string.data_to_tf_tfnotexit), Toast.LENGTH_LONG).show();
			//TTS_speak(0, getResources().getString(R.string.data_to_tf_tfnotexit));
		//	SpeakContentend(getResources().getString(R.string.data_to_tf_tfnotexit));
			PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_tf_tfnotexit));
			mDialog.show();
			mDialog.setPromptListener(this);
			
			return false;
		} else if (userId >= MAX_USER_NUM) { // 瀵煎嚭鍏ㄩ儴鐢ㄦ埛
			userinfo tempinfo = new userinfo();
			GetDbInfo dbUserInfo = new GetDbInfo(this, USER_PATH_FLAG); // 鎵撳紑鍐呭瓨鏁版嵁
			GetDbInfo dbTfInfo = new GetDbInfo(this, TF_PATH_FLAG); // 鎵撳紑瀛樺偍鍗℃暟鎹�
			Global.debug("SettingUpDataDB ===userId= " + userId);

			for (int i = 0; i < MAX_USER_NUM; i++) {
				dbTfInfo.detele(i); // 鍒犻櫎鏁版嵁 TF璇ョ敤鎴锋暟鎹�

				int maxid = dbUserInfo.getMaxId(i); // 鑾峰彇鏈�澶ц褰曟暟
				Global.debug("SettingUpDataDB ===maxid= " + maxid);
				for (int j = 1; j <= maxid; j++) {
					tempinfo = dbUserInfo.find(i, j);
					
					dbTfInfo.add(tempinfo, i);
				}
			}
			//Toast.makeText(this, getResources().getString(R.string.data_to_TF_OK), Toast.LENGTH_LONG).show();
			//TTS_speak(0, getResources().getString(R.string.data_to_TF_OK));
			//SpeakContentend(getResources().getString(R.string.data_to_TF_OK));
			PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_TF_OK));
			mDialog.show();
			mDialog.setPromptListener(this);
			return true;
		} else { // 鍗曚釜鐢ㄦ埛
			userinfo tempinfo = new userinfo();
			GetDbInfo dbUserInfo = new GetDbInfo(this, USER_PATH_FLAG); // 鏈湴鏁版嵁
			GetDbInfo dbTfInfo = new GetDbInfo(this, TF_PATH_FLAG); // 瀛樺偍鍗℃暟鎹�
			Global.debug("SettingUpDataDB ===userId= " + userId);

			dbTfInfo.detele(userId); // 鍒犻櫎鐢ㄦ埛鏁版嵁
			int maxid = dbUserInfo.getMaxId(userId); // 鑾峰彇鏈�澶ц褰曟暟
			Global.debug("\r\n [ exportData]  maxid == "+ maxid);
			for (int j = 1; j <= maxid; j++) {
				Global.debug("\r\n[ exportData] j ===== " + j);
				tempinfo = dbUserInfo.find(userId, j);
				
				Global.debug("tempinfo._id === "+ tempinfo._id);
				Global.debug("tempinfo.forwork === "+ tempinfo.forwork);
				Global.debug("tempinfo.overwork === "+ tempinfo.overwork);
				Global.debug("tempinfo.money === "+ tempinfo.money);
				Global.debug("year === "+ tempinfo.year+ " month == "+ tempinfo.month + " day == "+ tempinfo.day);
				
				dbTfInfo.add(tempinfo, userId);
			}
		//	Toast.makeText(this, getResources().getString(R.string.data_to_TF_OK), Toast.LENGTH_LONG).show();
		//	TTS_speak(0, getResources().getString(R.string.data_to_TF_OK));
			//SpeakContentend(getResources().getString(R.string.data_to_TF_OK));
			PromptDialog mDialog =new PromptDialog(this, getResources().getString(R.string.data_to_TF_OK));
			mDialog.show();
			mDialog.setPromptListener(this);
			return true;
		}
	}

	@Override
	public void onComplete() {   // 弹出框 显示完处理
		// TODO 自动生成的方法存根
		finish();
	}

}
