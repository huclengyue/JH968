package com.sunteam.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.manage.utils.Global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class ManageMenuActivity extends MenuActivity implements PromptListener, ConfirmListener, Runnable {

	private int gSelectID = 0;
	private static boolean gPastFlag = false; // 默认没有复制
	Thread gthread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Global.debug("ManageMenuActivity  =====111==");
		mTitle = getResources().getString(R.string.Menu_Title);

		// mMenuList = getResources().getStringArray(R.array.menu_list);
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.menu_list));
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_manage_menu);

		gthread = new Thread(this);
		gPastFlag = false;
		// gthread.start();
		registerTFcardPlugReceiver(); // 注册TF插拔消息
	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		unregisterReceiver(tfCardPlugReceiver);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_ENTER == keyCode || // 按键 Enter
				KeyEvent.KEYCODE_DPAD_CENTER == keyCode) {
			Global.debug("\r\n[keydown] gPastFlag  ===111====gPastFlag ==" + gPastFlag);
			if (true == gPastFlag) {
				return true;
			} else {
				super.onKeyDown(keyCode, event);
			}
			return true;
		} else if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode || KeyEvent.KEYCODE_DPAD_RIGHT == keyCode
				|| KeyEvent.KEYCODE_DPAD_DOWN == keyCode || KeyEvent.KEYCODE_DPAD_UP == keyCode
				|| KeyEvent.KEYCODE_MENU == keyCode || KeyEvent.KEYCODE_NUMPAD_0 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_1 == keyCode || KeyEvent.KEYCODE_NUMPAD_2 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_3 == keyCode || KeyEvent.KEYCODE_NUMPAD_4 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_5 == keyCode || KeyEvent.KEYCODE_NUMPAD_6 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_7 == keyCode || KeyEvent.KEYCODE_NUMPAD_8 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_9 == keyCode) {
			if (true == gPastFlag) {
				return true;
			} else {
				super.onKeyDown(keyCode, event);
			}
			return true;
		} else if (KeyEvent.KEYCODE_BACK == keyCode) { // 返回
			if (true == gPastFlag) {
				return true;
			} else {
				super.onKeyDown(keyCode, event);
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_ENTER == keyCode || // 按键 Enter
				KeyEvent.KEYCODE_DPAD_CENTER == keyCode) {

			Global.debug("\r\n[keyup] gPastFlag  ===111==== " + gPastFlag);
			if (true == gPastFlag) {
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.pasteing));
				mPromptDialog.show();

				return true;
			}
			gSelectID = getSelectItem();
			Global.debug("\r\n[onKeyUp] gSelectID === " + gSelectID + "  Global.COPY_ID == " + Global.COPY_ID);
			Global.debug("\r\n[onKeyUp] Global.gtempID === " + Global.gtempID);

			if (gSelectID == Global.COPY_ID) { // 复制
				if (Global.gtempID < 0) { // 空目录
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.copy_error));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(this);
					return true;
				}

				Global.gCopyFlag = true;
				Global.gCutFlag = false;
				Global.debug("\r\n[onActivityResult] gtempID ====== " + Global.gtempID);

				Global.gCopyPath_src = Global.gFilePaths.get(Global.gtempID).toString();
				Global.debug("【***】 gCopyPath_src =" + Global.gCopyPath_src);
				Global.gCopyName = Global.gFileName.get(Global.gtempID).toString();
				Global.debug("\r\n[COPY_ID] gCopyName == " + Global.gCopyName);
				// SpeakContentend(getResources().getString(R.string.copy_finsh));
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.copy_finsh));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(this);

			} else if (gSelectID == Global.CUT_ID) { // 剪切

				if (Global.gtempID < 0) { // 空目录
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.cut_error2));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(this);
					return true;
				}

				Global.gCopyFlag = false;
				Global.gCutFlag = true;
				Global.gCopyPath_src = Global.gFilePaths.get(Global.gtempID).toString();
				Global.gCopyName = Global.gFileName.get(Global.gtempID).toString();

				// SpeakContentend(getResources().getString(R.string.cut_finsh));
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.cut_finsh));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(this);

			} else if (gSelectID == Global.DEl_ID) { // 删除

				if (Global.gtempID < 0) { // 空目录
					PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.del_error));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(this);
					return true;
				}

				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.del_ask),
						getResources().getString(R.string.yes), getResources().getString(R.string.no));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(this);

			} else if (gSelectID == Global.PASTE_ID) { // 粘贴
				Global.debug("PASTE_ID == ");
				// Global.debug("PASTE_ID == " +
				// Global.gPath.get(Global.gPathNum - 1));
				Global.gCopyPath_desk = Global.gPath.get(Global.gPathNum - 1);
				Global.debug("Global.gCutFlag == " + Global.gCutFlag + " Global.gCopyFlag   == " + Global.gCopyFlag);
				if ((Global.gCopyFlag == true) || (Global.gCutFlag == true)) { // 可复制
					File mFile = new File(Global.gCopyPath_src); // 源文件存在
					// Global.debug("【*qqq**】 gCopyPath_src =" +
					// Global.gCopyPath_src);
					Global.debug("【*qqq**】 gCopyPath_src =" + Global.gCopyPath_src);
					if ((Global.gCutFlag == true)) {
						String tempFile = Global.gCopyPath_desk + java.io.File.separator + Global.gCopyName;
						// File mFile1 = new File(tempFile); // 剪切到的文件也存在
						if (/* mFile1.exists() */tempFile.equals(Global.gCopyPath_src)) { // 剪切是不能剪切同目录的文件或文件夹
							PromptDialog mPromptDialog = new PromptDialog(ManageMenuActivity.this,
									getResources().getString(R.string.cut_error));
							mPromptDialog.show();
							mPromptDialog.setPromptListener(ManageMenuActivity.this);
							Global.gPastName = tempFile;
							return true;
						}

					}
					if (mFile.exists()) {
						PromptDialog mPromptDialog = new PromptDialog(ManageMenuActivity.this,
								getResources().getString(R.string.pasteing));
						mPromptDialog.show();
						mPromptDialog.setPromptListener(new PromptListener() {

							@Override
							public void onComplete() {
								mHandler.sendEmptyMessage(Global.MSG_STRAT_PAST);
							}
						});
					} else {
						PromptDialog mPromptDialog = new PromptDialog(ManageMenuActivity.this,
								getResources().getString(R.string.past_error));
						mPromptDialog.show();
						mPromptDialog.setPromptListener(ManageMenuActivity.this);
					}
				} else { // 没有复制
					PromptDialog mPromptDialog = new PromptDialog(ManageMenuActivity.this,
							getResources().getString(R.string.past_error));
					mPromptDialog.show();
					mPromptDialog.setPromptListener(ManageMenuActivity.this);
				}
			} else if (gSelectID == Global.CLEAN_ID) { // 清空
				Global.gCopyPath_desk = Global.gPath.get(Global.gPathNum - 1); // 获取当前路径
				
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.clean_ask),
						getResources().getString(R.string.yes), getResources().getString(R.string.no));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {
					
					@Override
					public void doConfirm() {
						// TODO 自动生成的方法存根
						cleanFolder(Global.gCopyPath_desk);
						mHandler.sendEmptyMessage(Global.MSG_CLEAN_OK);
					}
					
					@Override
					public void doCancel() {
						// TODO 自动生成的方法存根
						mHandler.sendEmptyMessage(Global.MSG_BACK);
					}
				});

			}
			return true;
		} else if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode || KeyEvent.KEYCODE_DPAD_RIGHT == keyCode
				|| KeyEvent.KEYCODE_DPAD_DOWN == keyCode || KeyEvent.KEYCODE_DPAD_UP == keyCode
				|| KeyEvent.KEYCODE_MENU == keyCode || KeyEvent.KEYCODE_NUMPAD_0 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_1 == keyCode || KeyEvent.KEYCODE_NUMPAD_2 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_3 == keyCode || KeyEvent.KEYCODE_NUMPAD_4 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_5 == keyCode || KeyEvent.KEYCODE_NUMPAD_6 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_7 == keyCode || KeyEvent.KEYCODE_NUMPAD_8 == keyCode
				|| KeyEvent.KEYCODE_NUMPAD_9 == keyCode) {
			if (true == gPastFlag) {
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.pasteing));
				mPromptDialog.show();
			} else {
				super.onKeyUp(keyCode, event);
			}
			return true;
		} else if (KeyEvent.KEYCODE_BACK == keyCode) { // 返回
			if (true == gPastFlag) {
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.past_exit),
						getResources().getString(R.string.yes), getResources().getString(R.string.no));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {

					@Override
					public void doConfirm() { // 确认退出
						// TODO 自动生成的方法存根
						gPastFlag = false;

						// Global.gPastName();
						DeleteFile(Global.gPastName);

						/*
						 * Intent intent = new Intent(); Bundle bundle = new
						 * Bundle(); //新建 bundl bundle.putInt("selectItem",
						 * getSelectItem()); intent.putExtras(bundle); // 参数传递
						 * 
						 * setResult(Global.MENU_INTERFACE_FLAG, intent);
						 * finish();
						 */
						mHandler.sendEmptyMessage(Global.MSG_BACK);
					}

					@Override
					public void doCancel() {// 不确认退出
						// TODO 自动生成的方法存根

					}
				});
			} else {
				super.onKeyUp(keyCode, event);
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	// 删除文件
	private void DeleteFile(final String path) { //

		final File tFile = new File(path);
		Global.debug("DeleteFile ==== 1111 path =" + path + " mMenuList==" + mMenuList);

		if (tFile.isFile()) {
			// 是文件直接删除
			Global.debug("DeleteFile ==== 1111 is file + " + "mMenuList==" + mMenuList);
			tFile.delete();
		} else {
			// 是文件夹 需要循环删除
			Global.debug("DeleteFile ==== 1111 is path ");
			deleteFolder(tFile);
		}
	}

	// 删除文件夹
	public void deleteFolder(File folder) {
		File[] fileArray = folder.listFiles();
		if (fileArray.length == 0) {
			// 空文件夹
			folder.delete();
		} else {
			// 不为空
			for (File currentFile : fileArray) {
				if (currentFile.exists() && currentFile.isFile()) {
					// 是文件
					currentFile.delete();
				} else {
					// 是文件夹
					deleteFolder(currentFile);
				}
			}
			folder.delete();
		}
	}

	// 清空文件夹
	public void cleanFolder(String path) {
		final File tFile = new File(path);
		
		File[] fileArray = tFile.listFiles();
		if (fileArray.length == 0) {
			// 空文件夹
			//folder.delete();
		} else {
			// 不为空
			for (File currentFile : fileArray) {
				if (currentFile.exists() && currentFile.isFile()) {
					// 是文件
					currentFile.delete();
				} else {
					// 是文件夹
					deleteFolder(currentFile);
				}
			}
		}
	}

	// 复制文件夹
	public static void copyDirectiory(String sourceDir, String targetDir) throws IOException {
		// 新建目标目录
		Global.debug("【copyDirectiory】 ==sourceDir ==" + sourceDir + "  targetDir ==" + targetDir);
		(new File(targetDir)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
				copyFile(sourceFile.getPath(), targetFile.getPath());
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	}

	// 粘贴
	private void pasteFile() throws IOException {
		Global.debug("pasteFile === gCopyPath_src =" + Global.gCopyPath_src);
		Global.debug("pasteFile === gCopyPath_desk =" + Global.gCopyPath_desk);
		// Global.debug("pasteFile === gCopyFlag =" + gCopyFlag);
		// Global.debug("pasteFile === gCutFlag =" + gCutFlag);

		String tempFile = Global.gCopyPath_desk + java.io.File.separator + Global.gCopyName;

		File tFile = new File(Global.gCopyPath_src); // 原始文件
		// File tFile = new File(tempFile);
		Global.debug("\r\n[$$$$$$]tempFile ======" + tempFile);
		if (tFile.isDirectory()) { // 是文件夹 文件夹复制
			Global.gPastName = tempFile;
			Global.debug("\r\n pasteFile =111= Global.gCopyPath_src ==" + Global.gCopyPath_src);
			Global.debug("\r\n pasteFile =222= tempFile ==" + tempFile);

			File tFile_temp = new File(tempFile); // 原始文件 目的目录下是否有相同文件

			if (Global.gCopyPath_src.equals(tempFile)) { // 文件夹名相同 加后缀
				int num = 1;
				File tFile1 = null;
				String tempFile1 = null;
				tempFile1 = Global.gCopyPath_desk + java.io.File.separator + Global.gCopyName + "-" + num;
				Global.debug("[*******] tempFile1 ===== " + tempFile1);
				tFile1 = new File(tempFile1);
				while (tFile1.exists()) {
					num++;
					tempFile1 = Global.gCopyPath_desk + java.io.File.separator + Global.gCopyName + "-" + num;
					tFile1 = new File(tempFile1);
				}
				Global.debug("[*******] tempFile1 ====1111  = " + tempFile1);
				Global.gPastName = tempFile1;
				copyDirectiory(Global.gCopyPath_src, tempFile1);

				if (Global.gCopyFlag == true) {
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				} else if (Global.gCutFlag == true) {
					final File tFile11 = new File(Global.gCopyPath_src);
					if (tFile11.isFile()) {
						// 删除文件
						tFile11.delete();
					} else {
						// 删除文件夹
						deleteFolder(tFile11);
					}
					Global.gCutFlag = false;
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				}
			} else if (tFile_temp.exists()) { // 原始文件 目的目录下是有相同文件
				int num = 1;
				File tFile1 = null;
				String tempFile1 = null;
				tempFile1 = Global.gCopyPath_desk + java.io.File.separator + Global.gCopyName + "-" + num;
				Global.debug("[*******] tempFile1 ===== " + tempFile1);
				tFile1 = new File(tempFile1);
				while (tFile1.exists()) {
					num++;
					tempFile1 = Global.gCopyPath_desk + java.io.File.separator + Global.gCopyName + "-" + num;
					tFile1 = new File(tempFile1);
				}
				Global.debug("[*******] tempFile1 ====1111  = " + tempFile1);
				Global.gPastName = tempFile1;
				copyDirectiory(Global.gCopyPath_src, tempFile1);

				if (Global.gCopyFlag == true) {
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				} else if (Global.gCutFlag == true) {
					final File tFile11 = new File(Global.gCopyPath_src);
					if (tFile11.isFile()) {
						// 删除文件
						tFile11.delete();
					} else {
						// 删除文件夹
						deleteFolder(tFile11);
					}
					Global.gCutFlag = false;
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				}
			} else if (tempFile.contains(Global.gCopyPath_src)) { // 相同文件
				// SpeakContentend(getResources().getString(R.string.paste_check));
				mHandler.sendEmptyMessage(Global.MSG_PAST_CHECK);
				Global.debug("\r\n pasteFile =33= Global.gCopyPath_src ==" + Global.gCopyPath_src);
				Global.debug("\r\n pasteFile =44= tempFile ==" + tempFile);
			} else {
				copyDirectiory(Global.gCopyPath_src, tempFile);

				if (Global.gCopyFlag == true) {
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				} else if (Global.gCutFlag == true) {
					final File tFile11 = new File(Global.gCopyPath_src);
					if (tFile11.isFile()) {
						// 删除文件
						tFile11.delete();
					} else {
						// 删除文件夹
						deleteFolder(tFile11);
					}
					Global.gCutFlag = false;
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				}
			}

			return;
		} else {
			File tFile_temp = new File(tempFile); // 原始文件
			if (!Global.gCopyPath_src.equals(tempFile) && ((Global.gCopyFlag == true) || (Global.gCutFlag == true))) {

				if (tFile_temp.exists()) // 不同目录下 文件存在
				{
					int num = 1;
					File tFile1 = null;
					String tempFile1 = null;
					tempFile1 = Global.gCopyPath_desk + java.io.File.separator + getFileNameNoEx(Global.gCopyName) + "-"
							+ num + "." + getExtensionName(Global.gCopyName);
					Global.debug("[*******] tempFile1 ===== " + tempFile1);
					tFile1 = new File(tempFile1);
					while (tFile1.exists()) {
						num++;
						tempFile1 = Global.gCopyPath_desk + java.io.File.separator + getFileNameNoEx(Global.gCopyName)
								+ "-" + num + "." + getExtensionName(Global.gCopyName);
						tFile1 = new File(tempFile1);
					}
					Global.debug("[*******] tempFile1 ====1111  = " + tempFile1);
					Global.gPastName = tempFile1;
					copyFile(Global.gCopyPath_src, tempFile1);
				} else {
					if (tempFile.contains(Global.gCopyPath_src)) { // 相同文件
						Global.debug("\r\n pasteFile =55= Global.gCopyPath_src ==" + Global.gCopyPath_src);
						Global.debug("\r\n pasteFile =66= tempFile ==" + tempFile);
						SpeakContentend(getResources().getString(R.string.paste_check));
						return;
					} else {
						Global.gPastName = tempFile;
						copyFile(Global.gCopyPath_src, tempFile);
					}
				}

				Global.debug("pasteFile ==[]1111= gCutFlag =" + Global.gCutFlag);

				if (Global.gCopyFlag == true) {
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				} else if (Global.gCutFlag == true) {

					// SpeakContentend(getResources().getString(R.string.paste_finsh));
					final File tFile1 = new File(Global.gCopyPath_src);

					if (tFile1.isFile()) {
						// 删除文件
						tFile1.delete();
					} else {
						// 删除文件夹
						deleteFolder(tFile1);
					}
					Global.gCutFlag = false;
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				}
				// showList(gCopyPath_desk, gFileName.get(gPathNum -
				// 2).toString());
			} else {
				Global.debug("pasteFile ==[]2222= gCutFlag =" + Global.gCutFlag);
				if (tFile_temp.exists()) // 同一目录下复制
				{
					int num = 1;
					File tFile1 = null;
					String tempFile1 = null;
					tempFile1 = Global.gCopyPath_desk + java.io.File.separator + getFileNameNoEx(Global.gCopyName) + "-"
							+ num + "." + getExtensionName(Global.gCopyName);

					tFile1 = new File(tempFile1);
					while (tFile1.exists()) {
						num++;
						tempFile1 = Global.gCopyPath_desk + java.io.File.separator + getFileNameNoEx(Global.gCopyName)
								+ "-" + num + "." + getExtensionName(Global.gCopyName);
						tFile1 = new File(tempFile1);
					}
					Global.gPastName = tempFile1;
					copyFile(Global.gCopyPath_src, tempFile1);
					if (true == gPastFlag) {
						// SpeakContentend(getResources().getString(R.string.paste_finsh));
						mHandler.sendEmptyMessage(Global.MSG_PAST_FINSH);
					}
				}
			}
		}
	}

	/*
	 * Java文件操作 获取文件扩展名
	 * 
	 */
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}

	/*
	 * Java文件操作 获取不带扩展名的文件名
	 */
	public static String getFileNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	// 复制文件
	private static void copyFile(String oldFile, String newFile) {
	
		FileInputStream fis;
		FileOutputStream fos;

		Global.debug("copyFile == oldFile =" + oldFile + "\r\n newFile = " + newFile);
		try {
			fis = new FileInputStream(oldFile);
			fos = new FileOutputStream(newFile);
			
			 byte[] bt = new byte[1024];  
	         int c;
	         
	         while((c=fis.read(bt)) > 0){  
	        	 fos.write(bt,0,c);  
	         }			
			// 关闭文件
			if (fis != null) {
				fis.close();
			}
			// 关闭文件
			if (fos != null) {
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onComplete() {

		if (true == gPastFlag) {
			return;
		}
		/*
		 * // TODO 自动生成的方法存根 Intent intent = new Intent(); Bundle bundle = new
		 * Bundle(); //新建 bundl bundle.putInt("selectItem", getSelectItem());
		 * intent.putExtras(bundle); // 参数传递
		 * 
		 * setResult(Global.MENU_INTERFACE_FLAG, intent); finish();
		 */
		mHandler.sendEmptyMessage(Global.MSG_BACK);
	}

	@Override
	public void doCancel() {
		/*
		 * // TODO 自动生成的方法存根 Intent intent = new Intent(); Bundle bundle = new
		 * Bundle(); //新建 bundl bundle.putInt("selectItem", getSelectItem());
		 * intent.putExtras(bundle); // 参数传递
		 * 
		 * setResult(Global.MENU_INTERFACE_FLAG, intent); finish();
		 */
		mHandler.sendEmptyMessage(Global.MSG_BACK);
	}

	@Override
	public void doConfirm() {
		// TODO 自动生成的方法存根
		DeleteFile(Global.gFilePaths.get(Global.gtempID).toString());
		mHandler.sendEmptyMessage(Global.MSG_DEL_OK);
	}

	@Override
	public void run() {
		// TODO 自动生成的方法存根
		Global.debug("1111111111111111111111111111");
		try {
			gPastFlag = true;
			acquireWakeLock(this); // 禁止休眠
			pasteFile();
			if (gPastFlag == true) {
				gPastFlag = false;
				// onComplete();
			}
			releaseWakeLock();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		// gthread.stop();
	}

	// TTS读函数
	public void SpeakContentend(String Text) {
		TtsUtils.getInstance().speak(Text);

		delay(3000); // 为何要延时??? 为了将音频读完
	}

	public void delay(int len) {
		try {
			Thread.currentThread();
			Thread.sleep(len);// 毫秒
		} catch (Exception e) {
		}
	}

	private void go_back() {

		// Global.gPastName();

		Intent intent = new Intent();
		Bundle bundle = new Bundle(); // 新建 bundl
		bundle.putInt("selectItem", getSelectItem());
		bundle.putInt("goback", 1);
		intent.putExtras(bundle); // 参数传递

		setResult(Global.MENU_INTERFACE_FLAG, intent);
		finish();
	}

	private void go_back_per() {
		// Global.gPastName();

		Intent intent = new Intent();
		Bundle bundle = new Bundle(); // 新建 bundl
		bundle.putInt("selectItem", getSelectItem());
		intent.putExtras(bundle); // 参数传递

		setResult(Global.MENU_INTERFACE_FLAG, intent);
		finish();
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

				String mPath = Global.gPath.get(Global.gPathNum - 1);
				Global.debug("\r\n mPath ============== " + mPath);
				if (mPath.contains(mData)) { // 包含
					if (mData.contains(Global.MENU_PATH_EXTSD)) { // 存储卡
						Global.debug("\r\n tf卡 列表更新============== ");
						if (gPastFlag == true) {
							gPastFlag = false;
							DeleteFile(Global.gPastName);
						}
						go_back();
					} else if (mData.contains(Global.MENU_PATH_UDISK)) { // U盘
						Global.debug("\r\n U盘 列表更新============== ");
						if (gPastFlag == true) {
							gPastFlag = false;
							DeleteFile(Global.gPastName);
						}
						go_back();
					}
				}
				Global.debug("\r\n tf卡 拔出============== ");
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Global.MSG_DEL_OK) { // 音乐播放结束消息
				showDelOkPromptDialog();
			} else if (msg.what == Global.MSG_PAST_FINSH) {
				showPastFinshPromptDialog();
			} else if (msg.what == Global.MSG_PAST_CHECK) {
				showPastCheckPromptDialog();
			} else if (msg.what == Global.MSG_STRAT_PAST) {
				startPast();
			} else if (msg.what == Global.MSG_BACK) {
				go_back_per();
			} else if (msg.what == Global.MSG_CLEAN_OK){
				showCleanOkPromptDialog();
			}
			super.handleMessage(msg);
		}

	};

	// 开始粘贴
	private void startPast() {
		gPastFlag = true;
		gthread.start();
	}

	// 显示删除成功
	private void showDelOkPromptDialog() {
		PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.del_finsh));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(this);
	}
	// 显示删除成功
		private void showCleanOkPromptDialog() {
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.clean_finsh));
			mPromptDialog.show();
			mPromptDialog.setPromptListener(this);
		}

	// 粘贴完成
	private void showPastFinshPromptDialog() {

		PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.paste_finsh));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(this);
	}

	// 检查粘贴路径
	private void showPastCheckPromptDialog() {
		// TODO 自动生成的方法存根
		PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.paste_check));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(this);
	}
}
