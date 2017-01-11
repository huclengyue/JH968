package com.sunteam.music;

import java.util.ArrayList;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.music.dao.GetDbInfo;
import com.sunteam.music.dao.MusicInfo;
import com.sunteam.music.utils.Global;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class SaveMenuActivity extends MenuActivity implements ConfirmListener, PromptListener{
	private String gPath = null;
	private String gFileName = null;
	private int gSelectID = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Global.debug("SaveMenuActivity  =====111==");
		
		// 获取传递参数
		Intent intent=getIntent();	//获取Intent
		Bundle bundle=intent.getExtras();	//获取 Bundle
		
		gPath = bundle.getString("PATH");  // 获取修改位置
		gFileName = bundle.getString("FILENAME"); // 获取用户ID
				
		mTitle = getResources().getString(R.string.Menu_Title);
		
		//mMenuList = getResources().getStringArray(R.array.menu_list);
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
		
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_save_menu);
	}
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(KeyEvent.KEYCODE_ENTER == keyCode ||   // 按键 Enter
				KeyEvent.KEYCODE_DPAD_CENTER == keyCode)
		{	
			gSelectID = getSelectItem();
			if(gSelectID == Global.MENU_DEL_FILE || gSelectID == Global.MENU_DEL_ALL)
			{
				String ask = null;
				
				if(gSelectID == Global.MENU_DEL_FILE)
				{
					ask = getResources().getString(R.string.del_ask);
				}
				else{
					ask = getResources().getString(R.string.del_all_ask);
				}
				ConfirmDialog mcm = new ConfirmDialog(this, ask, 
						getResources().getString(R.string.yes), getResources().getString(R.string.no));
				mcm.show();
				mcm.setConfirmListener( this);
		
				Global.debug("#############################");
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}


	@Override
	public void doCancel() {
		// TODO 自动生成的方法存根
		//onResume();
		mHandler.sendEmptyMessage(Global.MSG_RESUME);
	}

	@Override
	public void doConfirm() {
		// TODO 自动生成的方法存根
		if(gSelectID == Global.MENU_DEL_FILE){
			MusicDelSaveList(gPath, gFileName);
			mHandler.sendEmptyMessage(Global.MSG_DEL_OK);	
		}
		else if(gSelectID == Global.MENU_DEL_ALL){
			MusicDelAllSaveList();
			mHandler.sendEmptyMessage(Global.MSG_DELALL_OK);
		}	
	}

	@Override
	public void onComplete() {
		// TODO 自动生成的方法存根
		Intent intent = new Intent();
		Bundle bundle = new Bundle();	//新建 bundl
		bundle.putInt("selectItem", getSelectItem());
		intent.putExtras(bundle); // 参数传递
		
		setResult(Global.MENU_FLAG, intent);
		finish();
	}
		
	// 删除我的收藏 列表文件
	public boolean MusicDelSaveList(String path, String filename)
	{
		boolean flag = false;
		MusicInfo musicinfo = new MusicInfo();   //创建 结构体
		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
		
    	//int max_id = dbMusicInfo.getMaxId(Global.SAVE_LIST_ID);
    	//Global.debug("\r\n MusicAddSaveList ===== max_id = " + max_id);
		int max_id = dbMusicInfo.getCount(Global.SAVE_LIST_ID);
		ArrayList<MusicInfo> mMusicInfos = new ArrayList<MusicInfo>();
		mMusicInfos = dbMusicInfo.GetAllData(Global.SAVE_LIST);
		for(int i = 0; i < max_id; i++)
		{
			musicinfo = mMusicInfos.get(i); // // dbMusicInfo.find(i, Global.SAVE_LIST_ID);
			Global.debug("\r\nMusicDelSaveList ==musicinfo.path = "+ musicinfo.path);
			Global.debug("\r\nMusicDelSaveList ==path = "+ path);
			if((path + "/" + filename).equals(musicinfo.getPath()+"/" + musicinfo.filename)){ // 找到文件
				flag = true;
				dbMusicInfo.deteleForOne(musicinfo._id, Global.SAVE_LIST_ID);
				break;
			}
		}
		
		dbMusicInfo.closeDb();

		return flag;
	}
	// 判断是否为空 true 不为空  
	public boolean MusicGetHaveSaveList()
	{
		boolean flag = false;

		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
	
		int max_id = dbMusicInfo.getCount(Global.SAVE_LIST_ID);
		dbMusicInfo.closeDb();
		if(max_id > 0){
			flag = true;
		}
		else{
			flag = false;
		}
		return flag;
	}
	// 删除我的收藏 列表文件
	public void MusicDelAllSaveList()
	{
		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
    	dbMusicInfo.detele(Global.SAVE_LIST_ID);				
	}	
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_NO_FILE){   // 音乐播放结束消息
				showNoFilePromptDialog();
			}else if(msg.what == Global.MSG_DEL_OK){
				showDelOkPromptDialog();		
			}else if(msg.what == Global.MSG_DELALL_OK){
				showDelAllPromptDialog();
			}
			else if(msg.what == Global.MSG_RESUME){
				onResume();
			}
			
			super.handleMessage(msg);
		}	
	};
	// 显示无文件
	private void showNoFilePromptDialog() {
		PromptDialog mDialog1 = new PromptDialog(this, getResources().getString(R.string.no_file));
		mDialog1.show();
		mDialog1.setPromptListener(this);
	}
	// 文件删除提示
	private void showDelOkPromptDialog() {
		PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.file_del));
		mDialog.show();
		if(true == MusicGetHaveSaveList()){ // 还有数据
			mDialog.setPromptListener(this);
		}
		else{ // 数据为空
			mDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					mHandler.sendEmptyMessage(Global.MSG_NO_FILE);		
				}
			});
		}			
	}
	// 删除全部
	private void showDelAllPromptDialog() {
		PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.file_del_all));
		mDialog.show();
		mDialog.setPromptListener(new PromptListener() {
			
			@Override
			public void onComplete() {
				mHandler.sendEmptyMessage(Global.MSG_NO_FILE);
			}
		});
		
	}
	
}
