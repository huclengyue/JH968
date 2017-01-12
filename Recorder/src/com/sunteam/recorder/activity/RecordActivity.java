package com.sunteam.recorder.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.sunteam.common.tts.TtsCompletedListener;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.CommonUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;
//import com.sunteam.recorder.log.MyLog;
import com.sunteam.recorder.recorder.AudioFileFunc;
import com.sunteam.recorder.recorder.AudioRecorder;
import com.sunteam.recorder.recorder.ErrorCode;

@SuppressLint({ "HandlerLeak", "SimpleDateFormat" })
public class RecordActivity extends BaseActivity {
	private int callerId = 0; // 0: Ĭ����¼��������; 1: �ȼ�����¼��; 2: ������������¼��; 3: ��������  4:FM;
								// ���������˳�ʱ���ص�������!
	private String fileName = ""; // ����Intent���ļ���;
									// ����Intent��¼���ļ������ļ����Ѿ���Global.storagePath��ֵ��

	private Resources rs;
	private TextView tvTime;
	private TextView tvReady;
	private TextView tvFilename;
	private TextView tvTimeRecored;
	//private TextView tvTitle;
	private TextView tvparameter;
	private TextView tvtimeLeft;
	TextView mTvTitle = null;
	View mLine = null;
	
	/**
	 * -1:û��¼��
	 */
	private int state_no_record = -1;
	/**
	 * 0��¼��
	 */
	private int state_recording = 0;
	/**
	 * 1��׼��¼������δ��ʼ
	 */
	private int state_ready = 1;

	private int mState = state_no_record;

	// private TextView tvrecording;
	// private RelativeLayout rl_recording;

	private String mTimerFormat;
	private BatteryBroadcastReciver batteryReceiver;
	private ShutdownReceiver shutdownReceiver;
	private long mSampleStart;
	private long leftSpace;
	private StringBuilder sb = new StringBuilder();
	private Handler RecordHandler = new Handler() {

		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				record(state_recording);
			} else if (msg.what == 0) {
				finish();
			} else if (msg.what == 2) {
				goback();
				speakTimeLeft();
			} else if (msg.what == 3) {
				deleteFile();
				goback();
				speakTimeLeft();
			} else if (msg.what == 4) {
				goback();
				speakTimeLeft();
			}
			super.handleMessage(msg);
		}

	};

	private Handler batteryHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				Global.showToast(RecordActivity.this, R.string.saveAndexit,	RecordHandler, 0);
			}
			super.handleMessage(msg);
		}
	};

	private UIHandler uiHandler = new UIHandler();
	Runnable mUpdateTimer = new Runnable() {
		public void run() {
			updateTimerView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Global.debug("RecordActivity  [onCreate] ======================== \r\n ");
		// ͨ��Intent���ݵ�����id��¼���ļ�����·����¼���ļ���
		Intent intent = getIntent();
		callerId = intent.getIntExtra("callerId", callerId);
		if(callerId == Global.CALL_FM || callerId == 3 || callerId == Global.CALL_FM){
			TtsUtils.getInstance(this, null);
		}
		Global.debug("RecordActivity  [onCreate] =======================callerId ==" + callerId);
		String path = intent.getStringExtra("path");
		if (null == path || path.equals("")) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/" + getResources().getString(R.string.storage);
		}
		Global.setSavePath(path);
		Global.debug("RecordActivity  [onCreate] ========222================path == " + path);
		AudioFileFunc.fileBasePath = new File(path);
		fileName = intent.getStringExtra("fileName");
		Global.debug("RecordActivity  [onCreate] ========333================fileName == " + fileName);
		rs = getResources();
		setContentView(R.layout.record_ready);

		mTvTitle = (TextView) this.findViewById(R.id.title); // ��ȡ�ؼ�
		mLine = this.findViewById(R.id.line); // ��ȡ
		
		tvTime = (TextView) findViewById(R.id.tv_time);
		//tvTitle = (TextView) findViewById(R.id.tv_recorde);
		tvparameter = (TextView) findViewById(R.id.tv_parameter);
		tvFilename = (TextView) findViewById(R.id.tv_filename);
		// tvrecording = (TextView) findViewById(R.id.tv_recording);
		tvTimeRecored = (TextView) findViewById(R.id.tv_recordtime);
		tvReady = (TextView) findViewById(R.id.tv_ready);
		tvtimeLeft = (TextView) findViewById(R.id.tv_timeLeft);
		// rl_recording = (RelativeLayout)findViewById(R.id.rl_recording);
		Tools mTools = new Tools(this);
		int fontSize = mTools.getFontSize();
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // ����title�ֺ�
		mTvTitle.setHeight(mTools.convertSpToPixel(fontSize));
		mLine.setBackgroundColor(mTools.getFontColor()); // ���÷ָ��ߵı���ɫ
		mTvTitle.setTextColor(mTools.getFontColor()); //  ����������ɫ
		// setTextSizeĬ�ϵ�λ��sp
		//tvTitle.setTextSize(rs.getDimensionPixelSize(R.dimen.ts_title));
		tvparameter.setTextSize(rs.getDimensionPixelSize(R.dimen.textsize));
		tvFilename.setTextSize(rs.getDimensionPixelSize(R.dimen.textsize));
		// tvrecording.setTextSize(rs.getDimensionPixelSize(R.dimen.ts_recording));
		tvTimeRecored.setTextSize(rs.getDimensionPixelSize(R.dimen.ts_recordleft));
		tvReady.setTextSize(rs.getDimensionPixelSize(R.dimen.ts_recording));
		tvtimeLeft.setTextSize(rs.getDimensionPixelSize(R.dimen.textsize));
		tvTime.setTextSize(rs.getDimensionPixelSize(R.dimen.textsize));

		tvTime.setText(getRecordLength());
		Global.debug("\r\n leftSpace =====" + leftSpace);
		if (leftSpace <= 0) {
			speakNotRecord();
		} else {
			Global.debug("\r\n callerId =====" + callerId);
			if ((Global.CALL_CALENDAR == callerId) || (3 == callerId) || (Global.CALL_FM == callerId)) {
				CommonUtils.sendKeyEvent(KeyEvent.KEYCODE_ENTER);
			}
			speakTimeLeft();
		}

		tvReady.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mState == state_no_record) {
					if (batteryReceiver.getLevel() < 10) { // ���������
						Global.showToast(RecordActivity.this,
								R.string.cannot_record, null, -1);
					} else if (leftSpace <= 0) { // �ռ䲻�����
						speakNotRecord();
					} else {
						mState = state_ready;

						showSurface();

						TtsUtils.getInstance().setCompletedListener(
								new TtsCompletedListener() {

									@Override
									public void onCompleted(String arg0) {
										Log.e("log", "speak finish");
										RecordHandler.sendEmptyMessage(1);
										TtsUtils.getInstance().setCompletedListener(null);									
									}
								});

						TtsUtils.getInstance().speak(rs.getString(R.string.startRecord));
						// showDialog();						
					}
				} else if (mState == state_recording) {
					stop();
					Global.showToast(RecordActivity.this, R.string.saveAndexit,
							RecordHandler, 2);
					// goback(); // houding@20160902 ͳһ��RecordHandler�д���
				}else if (mState == state_ready) {
					TtsUtils.getInstance().stop();
					mState = state_no_record;
					// goback(); // houding@20160902 ͳһ��RecordHandler�д���
					Global.showToast(RecordActivity.this, R.string.invalid_file, RecordHandler, 2);
				}
			}
		});

		mTimerFormat = getResources().getString(R.string.timer_format_hms);

		shutdownReceiver = new ShutdownReceiver();
		IntentFilter shutdownFilter = new IntentFilter(Intent.ACTION_SHUTDOWN);
		registerReceiver(shutdownReceiver, shutdownFilter);

	}

	@Override
	protected void onResume() {
		super.onResume();

		batteryReceiver = new BatteryBroadcastReciver();
		// ����һ��������
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		
		registerReceiver(batteryReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(batteryReceiver);
		
		Global.releaseWakeLock();  // ������
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int code = event.getKeyCode();
		int action = event.getAction();
	//	MyLog.e("dispatchKeyEvent", mState + ",code:" + code + ",action" + action);
		if ((code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER)
				&& action == KeyEvent.ACTION_UP) {
			if (mState == state_no_record) {
				if (batteryReceiver.getLevel() < 10) {// ���������
					Global.showToast(RecordActivity.this, R.string.cannot_record, mHandler, Global.MSG_ONRESUM);
				} else if (leftSpace <= 0) { // �ռ䲻�����
					speakNotRecord();
				} else {
					mState = state_ready;

					showSurface();

					TtsUtils.getInstance().setCompletedListener(
							new TtsCompletedListener() {
								@Override
								public void onCompleted(String arg0) {
									RecordHandler.sendEmptyMessage(1);
									TtsUtils.getInstance().setCompletedListener(null);
								}
							});
					TtsUtils.getInstance().speak(rs.getString(R.string.startRecord),
							TextToSpeech.QUEUE_FLUSH);
					//showDialog();
					Global.acquireWakeLock(this);   // ��ֹ����
				}

			} else if (mState == state_recording) {  // ����¼��
				stop();
				Global.showToast(RecordActivity.this, R.string.saveAndexit,
						RecordHandler, 2);
				Global.releaseWakeLock();   // ������
				// goback(); // houding@20160902 ͳһ��RecordHandler�д���
			} else if (mState == state_ready) {
				TtsUtils.getInstance().stop();
				mState = state_no_record;
				// goback(); // houding@20160902 ͳһ��RecordHandler�д���
				Global.showToast(RecordActivity.this, R.string.invalid_file,
						RecordHandler, 2);
				Global.releaseWakeLock();   // ������
			}
		} else if ((code == KeyEvent.KEYCODE_BACK || code == KeyEvent.KEYCODE_ESCAPE)
				&& action == KeyEvent.ACTION_UP) {
			if (mState == state_recording) {
				stop();
				showDialog();
			} else if (mState == state_ready) {
				TtsUtils.getInstance().stop();
				mState = state_no_record;
				// goback(); // houding@20160902 ͳһ��RecordHandler�д���
				Global.showToast(RecordActivity.this, R.string.invalid_file,
						RecordHandler, 2);
			} else if (mState == state_no_record) {
				TtsUtils.getInstance().stop();
				finish();
			}
		}
		return false;
		// return super.dispatchKeyEvent(event);
	}

	/**
	 * ����׼����������
	 */
	private void goback() {
		if (Global.CALL_CALENDAR == callerId || 3 == callerId || (Global.CALL_FM == callerId)) {
			finish();
			return;
		}
		tvTimeRecored.setText("00:00:00");
		tvFilename.setVisibility(View.GONE);
		tvTimeRecored.setVisibility(View.GONE);
		// rl_recording.setVisibility(View.GONE);
		tvReady.setText(rs.getString(R.string.ready));
		tvReady.setVisibility(View.VISIBLE);
		sb.delete(0, sb.length());
		tvTime.setText(getRecordLength());
		
	}

	/**
	 * �������ļ�
	 */
	private void deleteFile() {
		File recordFile = new File(Global.storagePath + "/"
				+ AudioFileFunc.AUDIO_WAV_FILENAME);
		if (recordFile.exists()) {
			recordFile.delete();
		}
	}

	/**
	 * ��ʾ�Ի���
	 */
	private void showDialog() {
		String title = rs.getString(R.string.whether_save);
		String yes = rs.getString(R.string.yes);
		TtsUtils.getInstance().speak(title + "," + yes);
		ConfirmDialog confirmDialog = new ConfirmDialog(RecordActivity.this, title, yes, rs.getString(R.string.no));
		confirmDialog.show();
		confirmDialog.setConfirmListener(new ConfirmListener() {
			
			@Override
			public void doConfirm() {
				mHandler.sendEmptyMessage(Global.MSG_GOBACK_SAVE);
			}
			
			@Override
			public void doCancel() {
				mHandler.sendEmptyMessage(Global.MSG_GOBACK);
				
			}
		});
	}

	private void speakNotRecord() {
		TtsUtils.getInstance().stop();
		if (!isFinishing()) {
			Global.showToast(RecordActivity.this,R.string.not_enough_space_cannot_record, RecordHandler, 0);
		}
	}

	/**
	 * ����ʣ��ʱ��
	 */
	private void speakTimeLeft() {
		if ((Global.CALL_CALENDAR == callerId) || (3 == callerId)||(Global.CALL_FM == callerId)) {
			return;
		}
		if (sb.equals("00:00:00")) {
			speakNotRecord();
		} else {
			TtsUtils.getInstance().speak(
					rs.getString(R.string.record) + rs.getString(R.string.timeLeft) + sb.toString());
		}
	}

	/**
	 * �õ����ô洢�ռ�,���ֽ�Ϊ��λ
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private long getAvaliableSpace() {
		long avaliableSpace = 0;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {// ��ȡ�ⲿ�洢�ռ�
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getAvailableBlocks();
			avaliableSpace = totalBlocks * blockSize;
			Log.e("TAG", "external:" + avaliableSpace / 1024 / 1024 + "MB");
		} else { // ��ȡ�ڲ��洢�ռ�
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			avaliableSpace = availableBlocks * blockSize;
			Log.e("TAG", "internal:" + avaliableSpace / 1024 / 1024 + "MB");
		}
		return avaliableSpace;
		// return 0;
	}

	/**
	 * ��ʾ��¼��ʱ�� ÿ��¼�Ƶ��ֽ���Ϊ16000*16*2=512000bit
	 * 
	 * @return
	 */
	private String getRecordLength() {
		leftSpace = getAvaliableSpace() - 2 * 1024 * 1024;// ��ȥ2M�ռ������ݴ�
		long h = 0;
		long m = 0;
		long s = 0;
		if (leftSpace > 0) {
			h = (leftSpace + 1024 * 1024) * 8 / 512 / 1000 / 60 / 60;
			m = ((leftSpace + 1024 * 1024) * 8 - h * 512 * 1000 * 60 * 60) / 512 / 1000 / 60;
			s = ((leftSpace + 1024 * 1024) * 8 - h * 512 * 1000 * 60 * 60 - m * 512 * 1000 * 60) / 512 / 1000;
		}

		return getDisplayFormat((int) h, R.string.hour) + ":"
				+ getDisplayFormat((int) m, R.string.minute) + ":"
				+ getDisplayFormat((int) s, R.string.second);
	}

	/**
	 * �õ�ʱ�����ʽ
	 * 
	 * @param value
	 * @param hms
	 * @return
	 */
	private String getDisplayFormat(int value, int hms) {
		StringBuilder showTime = new StringBuilder();
		if (value <= 0) {
			sb.append(rs.getString(R.string.zero)).append(rs.getString(hms));
			showTime.append("00");
		} else if (value > 0 && value < 10) {
			getDigitVoice(value);
			sb.append(rs.getString(hms));
			showTime.append("0").append(value);
		} else if (value == 10) {
			sb.append(rs.getString(R.string.ten)).append(rs.getString(hms));
			showTime.append(value);
		} else if (value > 10 && value < 20) {
			sb.append(rs.getString(R.string.ten));
			getDigitVoice(value % 10);
			sb.append(rs.getString(hms));
			showTime.append(value);
		} else if (value >= 20) {
			getDigitVoice(value / 10);
			sb.append(rs.getString(R.string.ten));
			getDigitVoice(value % 10);
			sb.append(rs.getString(hms));
			showTime.append(value);
		} else if (value == 100) {

		}
		return showTime.toString();
	}

	/**
	 * ����ת��Ϊ���ģ����㷢��
	 * 
	 * @param tensDigit
	 */
	private void getDigitVoice(int tensDigit) {
		switch (tensDigit) {
		case 1:
			sb.append(rs.getString(R.string.one));
			break;
		case 2:
			sb.append(rs.getString(R.string.two));
			break;
		case 3:
			sb.append(rs.getString(R.string.three));
			break;
		case 4:
			sb.append(rs.getString(R.string.four));
			break;
		case 5:
			sb.append(rs.getString(R.string.five));
			break;
		case 6:
			sb.append(rs.getString(R.string.six));
			break;
		case 7:
			sb.append(rs.getString(R.string.seven));
			break;
		case 8:
			sb.append(rs.getString(R.string.eight));
			break;
		case 9:
			sb.append(rs.getString(R.string.nine));
			break;
		default:
			break;
		}
	}
	/**
	 * ��ʾ¼���н���
	 */
	private void showSurface() {
		if (null == fileName || fileName.equals("")) {
			Date dt = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			AudioFileFunc.AUDIO_WAV_FILENAME = "REC" + sdf.format(dt) + ".wav";
		} else {
			AudioFileFunc.AUDIO_WAV_FILENAME = fileName;
		}

		tvFilename.setText(AudioFileFunc.AUDIO_WAV_FILENAME);
		tvFilename.setVisibility(View.VISIBLE);
		tvFilename.requestFocus();
		tvTimeRecored.setVisibility(View.VISIBLE);
		tvTimeRecored.setText("00:00:00");
		// rl_recording.setVisibility(View.VISIBLE);
		tvReady.setText(rs.getString(R.string.recording));
	}

	/**
	 * ��ʼ¼��
	 * 
	 * @param mFlag
	 */
	private void record(int mFlag) {
		int mResult = -1;
		// int mResult = 1000;
		AudioRecorder mRecord_1 = AudioRecorder.getInstance();
		Global.debug("\r\n record === mRecord_1 " + mRecord_1);
		Global.debug("\r\n record === callerId " + callerId);
		if(callerId == Global.CALL_FM){
			mResult = mRecord_1.startRecordAndFile_forFm();
		}
		else{
			mResult = mRecord_1.startRecordAndFile();
		}
		if (mResult == ErrorCode.SUCCESS) {
			mState = mFlag;
			mSampleStart = System.currentTimeMillis();
			updateTimerView();
		} else {
			Message msg = new Message();
			Bundle b = new Bundle();// �������
			b.putInt("cmd", CMD_RECORDFAIL);
			b.putInt("msg", mResult);
			msg.setData(b);

			if (mResult != ErrorCode.E_STATE_RECODING) {
				uiHandler.sendMessage(msg); // ��Handler������Ϣ,����UI
			}
		}
	}

	/**
	 * ֹͣ¼��
	 */
	private void stop() {
		if (mState == state_recording) {
			AudioRecorder mRecord_1 = AudioRecorder.getInstance();
			mRecord_1.stopRecordAndFile();
		}
		Message msg = new Message();
		Bundle b = new Bundle();// �������
		b.putInt("cmd", CMD_STOP);
		b.putInt("msg", mState);
		msg.setData(b);
		// uiHandler.sendMessageDelayed(msg, 1000); // ��Handler������Ϣ,����UI
		mState = state_no_record;
		
		Global.releaseWakeLock();  // ������
	}

	private final static int CMD_RECORDFAIL = 2001;
	private final static int CMD_STOP = 2002;

	class UIHandler extends Handler {
		public UIHandler() {
		}

		@SuppressWarnings("unused")
		public void handleMessage(Message msg) {
			Log.d("MyHandler", "handleMessage......");
			super.handleMessage(msg);
			Bundle b = msg.getData();
			int vCmd = b.getInt("cmd");
			switch (vCmd) {
			case CMD_RECORDFAIL:
				int vErrorCode = b.getInt("msg");
				int vMsg = ErrorCode.getErrorInfo(RecordActivity.this,
						vErrorCode);
				Global.showToast(RecordActivity.this, vMsg, null, -1);
				break;
			case CMD_STOP:
				AudioRecorder mRecord_1 = AudioRecorder.getInstance();
				long mSize = mRecord_1.getRecordFileSize();
				// Toast.makeText(
				// RecordActivity.this,
				// "¼����ֹͣ.¼���ļ�:" + AudioFileFunc.getWavFilePath()
				// + "\n�ļ���С��" + mSize, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};

	private void updateTimerView() {
		if (mState != -1) {
			long time = ((System.currentTimeMillis() - mSampleStart) / 1000);
			String timeStr = String.format(mTimerFormat, time / 3600,
					(time - (time / 3600) * 3600) / 60, time % 60);
			tvTimeRecored.setText(timeStr);
			long lefttime = (leftSpace + 1024 * 1024) * 8 / 512 / 1000 - time;
			timeStr = String.format(mTimerFormat, lefttime / 3600,
					(lefttime - (lefttime / 3600) * 3600) / 60, lefttime % 60);
			tvTime.setText(timeStr);
			uiHandler.postDelayed(mUpdateTimer, 1000);
			if (lefttime <= 0) {
				Global.showToast(RecordActivity.this, R.string.not_enough_space_stop_record, batteryHandler, 0);
				stop();
				// goback(); // houding@20160902 ͳһ��RecordHandler�д���
			}
		}
	}

	/**
	 * �����㲥������
	 * 
	 * @author Administrator
	 * 
	 */
	public class BatteryBroadcastReciver extends BroadcastReceiver {
		private int level;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				// �õ�ϵͳ��ǰ����
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				// ȡ��ϵͳ�ܵ���
				// int total=intent.getIntExtra(BatteryManager.EXTRA_SCALE,
				// 100);
				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
						|| status == BatteryManager.BATTERY_STATUS_FULL;

				if (level < 10) {
					if (mState == 0) {
						Global.showToast(RecordActivity.this, R.string.stop_record, batteryHandler, 0);
						stop();
						goback();
					}
				} else if (level < 20 && !isCharging) {
				//	Global.showToast(RecordActivity.this, R.string.low_battery,	null, -1);
				}
			}
		}

		public int getLevel() {
			return level;
		}
	}

	/**
	 * ϵͳ�رչ㲥������
	 */
	public class ShutdownReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
				Global.debug("\r\n [ShutdownReceiver] ======================mState==");
				if (mState == 0) {
					stop();
					goback();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(shutdownReceiver);
		super.onDestroy();
		
		if(callerId == Global.CALL_FM || callerId == 3 || callerId == Global.CALL_FM){
			if (TtsUtils.getInstance() != null) {
				TtsUtils.getInstance().destroy();
			}
		}
	}

	@Override
	public void leftSlip() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rightSlip() {
		// TODO Auto-generated method stub

	}

	@Override
	public void upSlip() {
		// TODO Auto-generated method stub

	}

	@Override
	public void downSlip() {
		// TODO Auto-generated method stub

	}
	
	// ������
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_GOBACK){   // ���ֲ��Ž�����Ϣ
				Record_goBack();
			}else if(msg.what == Global.MSG_GOBACK_SAVE){
				Record_goBackSave();		
			}
			else if(msg.what == Global.MSG_ONRESUM){
				onResume();
			}
			super.handleMessage(msg);
		}
	};
	
	private void Record_goBackSave() {
		goback();
		speakTimeLeft();
	}
	
	private void Record_goBack() {
		deleteFile();
		goback();
		speakTimeLeft();
	}
}
