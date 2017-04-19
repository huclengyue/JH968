/** Copyright 2009 - 2013 Broadcom Corporation
**
** This program is the proprietary software of Broadcom Corporation and/or its
** licensors, and may only be used, duplicated, modified or distributed
** pursuant to the terms and conditions of a separate, written license
** agreement executed between you and Broadcom (an "Authorized License").
** Except as set forth in an Authorized License, Broadcom grants no license
** (express or implied), right to use, or waiver of any kind with respect to
** the Software, and Broadcom expressly reserves all rights in and to the
** Software and all intellectual property rights therein.
** IF YOU HAVE NO AUTHORIZED LICENSE, THEN YOU HAVE NO RIGHT TO USE THIS
** SOFTWARE IN ANY WAY, AND SHOULD IMMEDIATELY NOTIFY BROADCOM AND DISCONTINUE
** ALL USE OF THE SOFTWARE.
**
** Except as expressly set forth in the Authorized License,
**
** 1.     This program, including its structure, sequence and organization,
**        constitutes the valuable trade secrets of Broadcom, and you shall
**        use all reasonable efforts to protect the confidentiality thereof,
**        and to use this information only in connection with your use of
**        Broadcom integrated circuit products.
**
** 2.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED
**        "AS IS" AND WITH ALL FAULTS AND BROADCOM MAKES NO PROMISES,
**        REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS, IMPLIED, STATUTORY,
**        OR OTHERWISE, WITH RESPECT TO THE SOFTWARE.  BROADCOM SPECIFICALLY
**        DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF TITLE, MERCHANTABILITY,
**        NONINFRINGEMENT, FITNESS FOR A PARTICULAR PURPOSE, LACK OF VIRUSES,
**        ACCURACY OR COMPLETENESS, QUIET ENJOYMENT, QUIET POSSESSION OR
**        CORRESPONDENCE TO DESCRIPTION. YOU ASSUME THE ENTIRE RISK ARISING OUT
**        OF USE OR PERFORMANCE OF THE SOFTWARE.
**
** 3.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, IN NO EVENT SHALL BROADCOM OR
**        ITS LICENSORS BE LIABLE FOR
**        (i)   CONSEQUENTIAL, INCIDENTAL, SPECIAL, INDIRECT, OR EXEMPLARY
**              DAMAGES WHATSOEVER ARISING OUT OF OR IN ANY WAY RELATING TO
**              YOUR USE OF OR INABILITY TO USE THE SOFTWARE EVEN IF BROADCOM
**              HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES; OR
**        (ii)  ANY AMOUNT IN EXCESS OF THE AMOUNT ACTUALLY PAID FOR THE
**              SOFTWARE ITSELF OR U.S. $1, WHICHEVER IS GREATER. THESE
**              LIMITATIONS SHALL APPLY NOTWITHSTANDING ANY FAILURE OF
**              ESSENTIAL PURPOSE OF ANY LIMITED REMEDY.
*/

package com.sunteam.fmradio.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import java.util.ArrayList;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.fmradio.R;
import com.sunteam.fmradio.dao.FmInfo;
import com.sunteam.fmradio.dao.GetDbInfo;
import com.sunteam.fmradio.utils.Global;

public class FmRadioSettings extends MenuActivity {
	/** Called with the activity is first created. */

	int gfreq = 0;

	@Override
	public void onCreate(Bundle b) {

		mTitle = getResources().getString(R.string.fm_setting);
		mMenuList = ArrayUtils.strArray2List(getResources().getStringArray(R.array.fm_setting_list));

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		gfreq = bundle.getInt("CHANEL"); // 获取传过来的频道
		Global.debug("\r\n[1111111111] gfreq ====== " + gfreq);

		super.onCreate(b);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO 自动生成的方法存根
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
			if (Global.SAVE_CHANEL_ID == getSelectItem()) { // 保存电台
				String temp = null;
				if (true == addChanel(gfreq)) {
					temp = getResources().getString(R.string.save_ok);
				} else {
					temp = getResources().getString(R.string.save_error);
				}
				PromptDialog mPromptDialog = new PromptDialog(this, temp);
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {

					@Override
					public void onComplete() {
						/*
						 * // TODO 自动生成的方法存根 Intent intent = new Intent();
						 * Bundle bundle = new Bundle(); //新建 bundl
						 * bundle.putInt("selectItem", getSelectItem());
						 * intent.putExtras(bundle); // 参数传递
						 * setResult(Global.MENU_FLAG, intent); finish();
						 */
						mHandler.sendEmptyMessage(Global.MSG_DOCOMPLETES);
					}
				});
				return true;
			} else if (Global.DEL_CHANEL_ID == getSelectItem()) { // 删除电台
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.ask_del),
						getResources().getString(R.string.ok), getResources().getString(R.string.canel));
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {

					@Override
					public void doConfirm() {
						mHandler.sendEmptyMessage(Global.MSG_DEL_CHANEL);
					}

					@Override
					public void doCancel() {
						/*
						 * // TODO 自动生成的方法存根 Intent intent = new Intent();
						 * Bundle bundle = new Bundle(); //新建 bundl
						 * bundle.putInt("selectItem", getSelectItem());
						 * intent.putExtras(bundle); // 参数传递
						 * setResult(Global.MENU_FLAG, intent); finish();
						 */
						mHandler.sendEmptyMessage(Global.MSG_DOCOMPLETES);
					}
				});

			} else if (Global.DELALL_CHANEL_ID == getSelectItem()) {

				ConfirmDialog mcConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.ask_delall),
						getResources().getString(R.string.ok), getResources().getString(R.string.canel));
				mcConfirmDialog.show();
				mcConfirmDialog.setConfirmListener(new ConfirmListener() {

					@Override
					public void doConfirm() {
						// TODO 自动生成的方法存根
						delChanelAllData(); // 清空数据
						mHandler.sendEmptyMessage(Global.MSG_DELALL_CHANEL);
					}

					@Override
					public void doCancel() {
						/*
						 * // TODO 自动生成的方法存根 Intent intent = new Intent();
						 * Bundle bundle = new Bundle(); //新建 bundl
						 * bundle.putInt("selectItem", getSelectItem());
						 * intent.putExtras(bundle); // 参数传递
						 * setResult(Global.MENU_FLAG, intent); finish();
						 */
						mHandler.sendEmptyMessage(Global.MSG_DOCOMPLETES);
					}
				});

			} else {
				Intent intent = new Intent();
				Bundle bundle = new Bundle(); // 新建 bundl
				bundle.putInt("selectItem", getSelectItem());
				intent.putExtras(bundle); // 参数传递
				setResult(Global.MENU_FLAG, intent);
				finish();
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	// 增加一条数据
	public boolean addChanel(int freq) {
		GetDbInfo dbFmInfo = new GetDbInfo(this); // 打开数据库
		FmInfo mFmInfo = new FmInfo();
		ArrayList<FmInfo> fmAlldata = new ArrayList<FmInfo>();

		fmAlldata = dbFmInfo.GetAllData(Global.FM_LIST);
		for (int i = 0; i < fmAlldata.size(); i++) {
			mFmInfo = fmAlldata.get(i);
			if (mFmInfo.chanel == freq) {
				dbFmInfo.closeDb();
				return false;
			}
		}
		int maxid = dbFmInfo.getMaxId(Global.FM_LIST);

		mFmInfo._id = maxid + 1;
		mFmInfo.chanel = freq;
		dbFmInfo.add(mFmInfo, Global.FM_LIST);
		dbFmInfo.closeDb();
		return true;
	}

	// 删除一条数据
	private boolean delChanelData(int freq) {
		// TODO 自动生成的方法存根
		GetDbInfo dbFmInfo = new GetDbInfo(this); // 打开数据库
		// FmInfo musicinfo = new FmInfo(); //创建 结构体

		int maxId = dbFmInfo.getCount(Global.FM_LIST); // 条数
		if (maxId > 0) { // 有记录
			ArrayList<FmInfo> fmAlldata = new ArrayList<FmInfo>();
			fmAlldata = dbFmInfo.GetAllData(Global.FM_LIST);

			// ArrayList<String> tmp = new ArrayList<String>();
			for (int i = 0; i < fmAlldata.size(); i++) {
				if (freq == fmAlldata.get(i).chanel) { // 有记录
					dbFmInfo.deteleForOne(fmAlldata.get(i)._id, Global.FM_LIST);
					dbFmInfo.closeDb();
					return true;
				}
			}
			dbFmInfo.closeDb();
			return false;
		} else {
			dbFmInfo.closeDb();
			return false;

		}
	}

	// 删除所有数据
	private void delChanelAllData() {
		Global.debug("\r\n [FmRadioSettings]--> [delChanelAllData]  =====");
		GetDbInfo dbFmInfo = new GetDbInfo(this); // 打开数据库
		// FmInfo musicinfo = new FmInfo(); //创建 结构体
		dbFmInfo.detele(Global.FM_LIST);

		dbFmInfo.closeDb();
	}

	// 处理弹框
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Global.debug("\r\n [FmRadioSettings]--> [mHandler]  msg.what=" + msg.what);
			if (msg.what == Global.MSG_DEL_CHANEL) { // 音乐播放结束消息
				showDelChanelPromptDialog();
			} else if (msg.what == Global.MSG_DELALL_CHANEL) {
				showDelAllChanelPromptDialog();
			} else if (msg.what == Global.MSG_ONRESUM) {
				onResume();
			} else if (msg.what == Global.MSG_DOCOMPLETES) {
				goBack();
			}
			

			super.handleMessage(msg);
		}
	};

	private void goBack() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle(); // 新建 bundl
		bundle.putInt("selectItem", getSelectItem());
		intent.putExtras(bundle); // 参数传递
		setResult(Global.MENU_FLAG, intent);
		finish();
	}

	// 删除电台
	private void showDelChanelPromptDialog() {
		String temp = null;

		String s = getResources().getString(R.string.fm) + String.format("%.1f", gfreq / Global.fmScale);

		if (true == delChanelData(gfreq)) {
			temp = s + getResources().getString(R.string.del_ok);
		} else {
			temp = s + getResources().getString(R.string.del_error);
		}
		PromptDialog mPromptDialog = new PromptDialog(FmRadioSettings.this, temp);

		mPromptDialog.show();
		mPromptDialog.setPromptListener(new PromptListener() {

			@Override
			public void onComplete() {
				mHandler.sendEmptyMessage(Global.MSG_DOCOMPLETES);
			}
		});
	}

	// 清空电台
	private void showDelAllChanelPromptDialog() {
		PromptDialog mPromptDialog = new PromptDialog(FmRadioSettings.this,
				getResources().getString(R.string.delall_ok));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(new PromptListener() {

			@Override
			public void onComplete() {
				/*
				 * // TODO 自动生成的方法存根 Intent intent = new Intent(); Bundle bundle
				 * = new Bundle(); //新建 bundl bundle.putInt("selectItem",
				 * getSelectItem()); intent.putExtras(bundle); // 参数传递
				 * setResult(Global.MENU_FLAG, intent); finish();
				 */
				mHandler.sendEmptyMessage(Global.MSG_DOCOMPLETES);
			}
		});
	}

}
