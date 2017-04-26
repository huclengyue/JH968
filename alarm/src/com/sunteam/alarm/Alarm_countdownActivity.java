package com.sunteam.alarm;

import java.util.Timer;
import java.util.TimerTask;

import com.sunteam.alarm.utils.Global;

import com.sunteam.common.menu.BaseActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.player.MyPlayer;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class Alarm_countdownActivity extends BaseActivity implements MyPlayer.OnStateChangedListener {

	TextView mTitle = null;
	TextView mTv1 = null;
	TextView mTv2 = null;
	TextView mTv3 = null;
	TextView mTv4 = null;
	TextView mTv5 = null;
	View mLine = null;

	// private int gSelectID = 0; // 选择是哪个
	private int gtime_len = 0; // 到计时长度

	private int START_COUNTDOWN = 1; // 倒计时开始
	private int STOP_COUNTDOWN = 0; // 倒计时结束
	private int PAUSE_COUNTDOWN = 2; // 倒计时暂停

	private int gCountDown_falg = 0; // 选择是哪个
	
	private MyPlayer myPlayer = null;
	
	private boolean timeout_flag = false; // 选择是哪个
	
	Timer timer = new Timer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_countdown);

		mTitle = (TextView) findViewById(R.id.title);
		mTv1 = (TextView) findViewById(R.id.tv1);
		mTv2 = (TextView) findViewById(R.id.tv2);
		mTv3 = (TextView) findViewById(R.id.tv3);

		mTv4 = (TextView) findViewById(R.id.tv4);
		mTv5 = (TextView) findViewById(R.id.tv5);

		mLine = this.findViewById(R.id.line); // 获取

		mTitle.setText(getResources().getString(R.string.countdown));

		Tools mTools = new Tools(this);

		this.getWindow().setBackgroundDrawable(new ColorDrawable(mTools.getBackgroundColor()));

		int fontSize = mTools.getFontSize();
		mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize); // 设置title字号
		mTitle.setHeight(fontSize);
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色

		mTitle.setTextColor(mTools.getFontColor()); // 设置字体颜色
		mTv1.setTextColor(mTools.getFontColor()); // 设置字体颜色
		mTv2.setTextColor(mTools.getFontColor()); // 设置字体颜色
		mTv3.setTextColor(mTools.getFontColor()); // 设置字体颜色
		mTv4.setTextColor(mTools.getFontColor()); // 设置字体颜色
		mTv5.setTextColor(mTools.getFontColor()); // 设置字体颜色

		timer.schedule(task, 1000, 1000); // timeTask

		Intent intent = getIntent(); // 获取Intent
		Bundle bundle = intent.getExtras(); // 获取 Bundle

		gtime_len = bundle.getInt("TIMELEN"); // 获取 反显位置

		int hour = gtime_len / 60 / 60;
		int min = gtime_len / 60 % 60;
		int sec = gtime_len % 60;
		String temp = null;

		temp = "";
		if (hour < 10) {
			temp += "0";
		}
		mTv1.setText(temp + hour);

		temp = "";
		if (min < 10) {
			temp += "0";
		}
		mTv3.setText(temp + min);

		temp = "";
		if (sec < 10) {
			temp += "0";
		}
		mTv5.setText(temp + sec);

		TtsUtils.getInstance().speak(getResources().getString(R.string.countdown_start));
		gCountDown_falg = START_COUNTDOWN;
		
		// 新建播放器
		myPlayer = MyPlayer.getInstance(this,mHandler);
		myPlayer.setOnStateChangedListener(this);
		
	}

	protected void onResume() {
		super.onResume();

		acquireWakeLock(this);
	};

	protected void onPause() {
		super.onPause();
		releaseWakeLock();
	};

	// 1秒钟定时
	TimerTask task = new TimerTask() {
		@Override
		public void run() {

			runOnUiThread(new Runnable() { // UI thread
				@Override
				public void run() {
					if (gCountDown_falg == START_COUNTDOWN) {
						if (gtime_len > 0) {
							gtime_len--;
							timeout_flag = false;
						}
						int hour = gtime_len / 60 / 60;
						int min = gtime_len / 60 % 60;
						int sec = gtime_len % 60;
						String temp = null;

						temp = "";
						if (hour < 10) {
							temp += "0";
						}
						mTv1.setText(temp + hour);

						temp = "";
						if (min < 10) {
							temp += "0";
						}
						mTv3.setText(temp + min);

						temp = "";
						if (sec < 10) {
							temp += "0";
						}
						mTv5.setText(temp + sec);

						if (gtime_len <= 0) {
							timer.cancel();
							mHandler.sendEmptyMessage(Global.MSG_COUNTDOWN_END);
							Global.debug("gtime_len ==========================" + gtime_len);
						}
					}
				}
			});
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		if(timeout_flag == true){
			
			myPlayer.stopPlayback();
			finish();
			timeout_flag = false;
		}
		if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (gCountDown_falg == START_COUNTDOWN) {
				gCountDown_falg = PAUSE_COUNTDOWN;
				releaseWakeLock();
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.countdown_Pause));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {

					@Override
					public void onComplete() {
						
						TtsUtils.getInstance()
								.speak(gtime_len / 60 / 60 + getResources().getString(R.string.hour_time)
										+ (gtime_len / 60) % 60 + getResources().getString(R.string.min_time)
										+ (gtime_len % 60 + getResources().getString(R.string.sec_time)));
					}
				});
			} else if (gCountDown_falg == PAUSE_COUNTDOWN) {
				acquireWakeLock(this);
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.countdown_resum));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {

					@Override
					public void onComplete() {
						
						gCountDown_falg = START_COUNTDOWN;
						TtsUtils.getInstance()
								.speak(gtime_len / 60 / 60 + getResources().getString(R.string.hour_time)
										+ (gtime_len / 60) % 60 + getResources().getString(R.string.min_time)
										+ (gtime_len % 60 + getResources().getString(R.string.sec_time)));
					}
				});
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) { // 返回
			String str_ok = getResources().getString(R.string.ok);
			String str_canel = getResources().getString(R.string.canel);
			String str_title = getResources().getString(R.string.countdown_starting);

			ConfirmDialog mConfirmDialog = new ConfirmDialog(this, str_title, str_ok, str_canel);
			mConfirmDialog.show();
			mConfirmDialog.setConfirmListener(new ConfirmListener() {

				@Override
				public void doConfirm() {
					timer.cancel();
					gCountDown_falg = STOP_COUNTDOWN;
					finish();
				}

				@Override
				public void doCancel() {
					Global.debug("\r\n  否========================");
				}

			});
			Global.debug("\r\n  ======88888==99================");
			return true;
		}
		return super.onKeyUp(keyCode, event);

	}

	public PromptListener promptListener = new PromptListener() {

		@Override
		public void onComplete() {
			
		//	finish();
			Global.debug("PromptListener ========ssss==================" + gtime_len);
			mHandler.sendEmptyMessage(Global.MSG_COUNTDOWN_PLAYMUSIC);
		}
	};

	public void putMsg() {
		PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.countdown_end));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(promptListener);
		// TtsUtils.getInstance().speak(getResources().getString(R.string.countdown_end));
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Global.MSG_COUNTDOWN_END) { // 音乐播放结束消息
				putMsg();
			} else if(msg.what == Global.MSG_COUNTDOWN_PLAYMUSIC){
				timer.cancel();
//				String path = Alarmpublic.ALARM_PATH + getResources().getString(R.string.folder) + "/" + Global.FILE_TIMEOUT;
//				myPlayer.startPlayback(myPlayer.playProgress(), path, true);
				//myPlayer.startPlayback2(this, R.raw.timerout, true);
				myPlayer.startPlayback2(Alarm_countdownActivity.this, R.raw.timerout, true);
				timeout_flag = true;
			}
			
			super.handleMessage(msg);
		}
	};
	// 播放状态改变
	@Override
	public void onStateChanged(int state) {
		if (state == MyPlayer.PLAYING_STATE || state == MyPlayer.RECORDING_STATE) {
//          mSampleInterrupted = false;
//          mErrorUiMessage = null;
		}
		else if(state == MyPlayer.IDLE_STATE){
			myPlayer.stopPlayback();
			Global.debug("onStateChanged ===== 1111=================myPlayer.IDLE_STATE==" + MyPlayer.IDLE_STATE);
			finish();  // 在第一个闹钟来时 会改变播放状态 这里也会调 这样会将界面销毁    这里不需要
		}
	}

	@Override
	public void onError(int error) {
		// TODO 自动生成的方法存根
		
	}
}
