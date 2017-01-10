package com.sunteam.recorder.activity;

import java.util.List;

import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;
import com.sunteam.recorder.player.MyPlayer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayActivity extends RecordBaseActivity implements MyPlayer.OnStateChangedListener{
	
	private MyPlayer myPlayer = null;
	private static final int SEEK_BAR_MAX = 10000;
	private TextView tvPlayedTime;
	private TextView tvTotalTime;
	private TextView tvFileName;
	//private TextView tvTitle;
	private SeekBar mPlaySeekBar;
	
	TextView mTvTitle = null;
	View mLine = null;
	

	private String mSampleFile;
	private String mTimerFormatMS;
	private String mTimerFormatHMS;
	private ImageButton imageButton;
	private List<String> fileList;
	private int currentIndex;
	private int total;
	private BatteryBroadcastReciver receiver;
	private static boolean isClose = false;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Global.debug("\r\n[mHandler] =======111111=========== msg.what =="+ msg.what);
			if(msg.what == 1){
				Intent intent = new Intent();
				intent.putExtra("currentIndex", currentIndex);
				setResult(RESULT_OK, intent);
				finish();
			}else if(msg.what == 0){
				myPlayer.startPlayback(0, mSampleFile, false);
			}else if(msg.what == 2){
				mHandler.postDelayed(finishActivity, 2000);
			}else if(msg.what == 3){
				mHandler.postDelayed(finishActivityDelayed, 2000);
			}
			super.handleMessage(msg);
		}
		
	};
	
Runnable finishActivityDelayed = new Runnable() {
		
		@Override
		public void run() {
			mHandler.sendEmptyMessage(1);
		}
	};
	
	
	Runnable finishActivity = new Runnable() {
		
		@Override
		public void run() {
			closeActivity();		
		}
	};
	
	static Runnable switchClose = new Runnable(){

		@Override
		public void run() {
			Log.e("switchClose", "isClose");
			isClose = true;
		}
		
	};
	
	
	Runnable mUpdateTimer = new Runnable() {
        public void run() { 
        	updateTimerView(); 
        }
    };

    private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
             updateSeekBar();
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play);

		mTvTitle = (TextView) this.findViewById(R.id.title); // ��ȡ�ؼ�
		mLine = this.findViewById(R.id.line); // ��ȡ
		
		tvPlayedTime = (TextView) findViewById(R.id.starttime);
		tvTotalTime = (TextView) findViewById(R.id.totaltime);
		tvFileName = (TextView) findViewById(R.id.filename);
		//tvTitle = (TextView) findViewById(R.id.tv_recorder);
		
		tvPlayedTime.setTextSize(getResources().getDimensionPixelSize(R.dimen.textsize));
		tvTotalTime.setTextSize(getResources().getDimensionPixelSize(R.dimen.textsize));
		tvFileName.setTextSize(getResources().getDimensionPixelSize(R.dimen.textsize));
		//tvTitle.setTextSize(getResources().getDimensionPixelSize(R.dimen.ts_title));
	
		Tools mTools = new Tools(this);
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // ����title�ֺ�
		mTvTitle.setHeight(mTools.convertSpToPixel(mTools.getFontSize()));
		mLine.setBackgroundColor(mTools.getFontColor()); // ���÷ָ��ߵı���ɫ
		mTvTitle.setTextColor(mTools.getFontColor()); //  ����������ɫ
		
		imageButton = (ImageButton)findViewById(R.id.ib_paly);
		
		Intent intent = getIntent();
		mSampleFile = (String) intent.getExtras().get("filename");
		tvFileName.setText(mSampleFile);
		fileList = (List<String>)intent.getStringArrayListExtra("filelist");
		currentIndex = getCurrentIndex(mSampleFile);
		
		mPlaySeekBar = (SeekBar) findViewById(R.id.play_seek_bar);
	    mPlaySeekBar.setMax(SEEK_BAR_MAX);
		mTimerFormatMS = getResources().getString(R.string.timer_format_ms);
		mTimerFormatHMS = getResources().getString(R.string.timer_format_hms);
		
		myPlayer = MyPlayer.getInstance(PlayActivity.this,mHandler);
		myPlayer.setOnStateChangedListener(this);
		
		
		myPlayer.startPlayback(myPlayer.playProgress(), mSampleFile,true);
		imageButton.setBackgroundResource(R.drawable.play);
		total = myPlayer.fileDuration();
		if(total>=3600){
			tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
		}else{
			tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
		}
		
		updateUI();
		
		
		imageButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				if(myPlayer.state() == MyPlayer.IDLE_STATE||myPlayer.state()==MyPlayer.PLAYING_PAUSED_STATE){
					myPlayer.startPlayback(myPlayer.playProgress(), mSampleFile,true);
					imageButton.setBackgroundResource(R.drawable.play);
					total = myPlayer.fileDuration();
					if(total>=3600){
						tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
					}else{
						tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
					}
					updateUI();
				}else if(myPlayer.state() == MyPlayer.PLAYING_STATE){
					myPlayer.pausePlayback();
					imageButton.setBackgroundResource(R.drawable.pause);
				}
			}
		});	
	}
	
	@Override  
    protected void onResume() {  
        // TODO Auto-generated method stub  
        super.onResume();  
        receiver=new BatteryBroadcastReciver();  
        //����һ��������  
        IntentFilter intentFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  
        registerReceiver(receiver, intentFilter);  
        
        Global.acquireWakeLock(this);   // ��ֹ����
    }  
      
    @Override  
    protected void onPause() {  
        // TODO Auto-generated method stub  
        super.onPause();  
        unregisterReceiver(receiver); 
        
        Global.releaseWakeLock();  // ������
    }
	
	
	private void updateUI(){
		updateTimerView();
		updateSeekBar();
	}
	
	private void updateTimerView(){
		if(myPlayer.state()==MyPlayer.PLAYING_STATE){
		int current = myPlayer.progress();
		if(total>=3600){
			tvPlayedTime.setText(String.format(mTimerFormatHMS,current/3600, (current%3600)/60, current%60));
		}else{
			tvPlayedTime.setText(String.format(mTimerFormatMS, current/60, current%60));
		}	
		mHandler.postDelayed(mUpdateTimer, 500);
		}
	}
	
	private void updateSeekBar() {
        if (myPlayer.state() == MyPlayer.PLAYING_STATE) {
            mPlaySeekBar.setProgress((int) (SEEK_BAR_MAX * myPlayer.playProgress()));
            mHandler.postDelayed(mUpdateSeekBar, 10);
        }
    }
	
	 @Override
		public boolean dispatchKeyEvent(KeyEvent event) {
		 	int action = event.getAction();
			int keyCode = event.getKeyCode();
			if(keyCode == KeyEvent.KEYCODE_BACK&&action == KeyEvent.ACTION_UP){
				myPlayer.stopPlayback();
				Intent intent = new Intent();
				intent.putExtra("currentIndex", currentIndex);
				setResult(RESULT_OK, intent);
				finish();
			}else if((keyCode == KeyEvent.KEYCODE_ENTER||keyCode == KeyEvent.KEYCODE_DPAD_CENTER)&&action == KeyEvent.ACTION_UP){
				if(myPlayer.state() == MyPlayer.IDLE_STATE||myPlayer.state()==MyPlayer.PLAYING_PAUSED_STATE){
					myPlayer.startPlayback(myPlayer.playProgress(), mSampleFile,true);
					imageButton.setBackgroundResource(R.drawable.play);
					total = myPlayer.fileDuration();
					if(total>=3600){
						tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
					}else{
						tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
					}
					updateUI();
					Global.acquireWakeLock(this);  //  ����״̬��ֹ����
				}else if(myPlayer.state() == MyPlayer.PLAYING_STATE){
					myPlayer.pausePlayback();
					imageButton.setBackgroundResource(R.drawable.pause);
					Global.releaseWakeLock();   // ��ͣʱ ��������
				}
			}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP&&action == KeyEvent.ACTION_UP){
				previous();
				switchAudio();
			}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN&&action == KeyEvent.ACTION_UP){
				next();
				switchAudio();
			}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT&&action == KeyEvent.ACTION_UP){
				Global.debug("\r\n[KEYCODE_DPAD_LEFT] =======00000============== ");
				myPlayer.rewind();
			}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT&&action == KeyEvent.ACTION_UP){
				Global.debug("\r\n[rightKeyPress] =======00000============== ");
				rightKeyPress();
			}
			return false;
			//return super.dispatchKeyEvent(event);
		}
	


	public static final long INTERVAL = 2000L; //�ж�����keydown��ʱ����
	private static long lastDownTime = 0L; //��һ��down��ʱ��
	 
	 /**
	  * �ж��Ƿ�Ϊ��������,��Ϊtrue����Ϊfalse
	  * @return
	  */
	 public static boolean filter(){
		 Log.e("time", (System.currentTimeMillis() - lastDownTime)+"");
		 long interval = System.currentTimeMillis() - lastDownTime;
	  if (interval> INTERVAL&&interval<10000){
		  lastDownTime = System.currentTimeMillis();
		  return false;
	  }
	  	lastDownTime = System.currentTimeMillis();
	  	return true;
	 }
	 
	 
	 private void closeActivity(){
		 Log.e("isClose", isClose?"true":"false");
		 if(isClose){
			 Global.showToast(this, R.string.speed2end, mHandler,3);
		 }else{
			 mHandler.postDelayed(finishActivity, 2000);	//1s���ٴ�ִ�д˷���
		 }
	 }
	
	private int getCurrentIndex(String fileName){
		return fileList.indexOf(fileName);
	}
	
	//��һ��  
    private void previous() {  
        if((currentIndex-1)>=0){  
        	currentIndex--;   
        }else{  
        	currentIndex = fileList.size()-1;
        	//Global.showToast(PlayActivity.this, R.string.turn2end,null,-1);
        }
    }  
  
    //��һ��  
    private void next() {  
        if((currentIndex+1)<fileList.size()){  
        	currentIndex++;  
        }else{  
        	currentIndex = 0;
        	//Global.showToast(PlayActivity.this, R.string.turn2start,null,-1);      	
        } 
    }
    
    
    private void switchAudio(){
    	mSampleFile = fileList.get(currentIndex);
		tvFileName.setText(mSampleFile);
		myPlayer.startPlayback(0, mSampleFile,false);
		imageButton.setBackgroundResource(R.drawable.play);
		total = myPlayer.fileDuration();
		if(total>=3600){
			tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
		}else{
			tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
		}
    }
	
    private void rightKeyPress(){
		if(myPlayer.endTime() < 1000){  // < 10S ʱ��
			if(myPlayer.state() == MyPlayer.PLAYING_STATE){
				myPlayer.stopPlayback();
				isClose = false;
//				mHandler.postDelayed(finishActivity, 0);
//				Global.showToast(this, R.string.speed2end, null,-1);
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.speed2end));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO �Զ����ɵķ������
						mHandler.sendEmptyMessageDelayed(1, 1000);
						Global.debug("\r\n[rightKeyPress] =======111111============== ");
					}
				});
				
			}else if(myPlayer.state() == MyPlayer.IDLE_STATE){
				mHandler.removeCallbacks(switchClose);		//�Ƴ���δִ�е�runnable
				mHandler.postDelayed(switchClose, 2000);	//2s���ý��ر�activity�����Ϊtrue
				if(!filter()){ //���������������
					isClose = true;
				}else{	//����������
					Global.showToast(this, R.string.speed2end, null,-1);
					isClose = false;
				}
			}					
		}else{
			myPlayer.speed();
		}			
	
    }
    @SuppressWarnings("unused")
	private void leftKeyPress(){
    	if(!filter()){ //���������������
			isClose = true;
		}else{	//����������
			Global.showToast(this, R.string.speed2end, null,-1);
			isClose = false;
		}
    }
    
    
	@Override
	public void onStateChanged(int state) {
		if (state == MyPlayer.PLAYING_STATE || state == MyPlayer.RECORDING_STATE) {
//            mSampleInterrupted = false;
//            mErrorUiMessage = null;
        }
        updateUI();	
	}

	@Override
	public void onError(int error) {
		Resources res = getResources();

        String message = null;
        switch (error) {
            case MyPlayer.STORAGE_ACCESS_ERROR:
                message = res.getString(R.string.error_file);
                break;
            case MyPlayer.IN_CALL_RECORD_ERROR:
                // TODO: update error message to reflect that the recording
                // could not be
                // performed during a call.
            case MyPlayer.INTERNAL_ERROR:
                message = res.getString(R.string.error_app_internal);
                break;
        }
        if (message != null) {
            Global.showToast(PlayActivity.this, R.string.error_file, mHandler, 1);
        }
    }
	
	
	public class BatteryBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
	            	//�õ�ϵͳ��ǰ����
		           int level=intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//		            //ȡ��ϵͳ�ܵ���
//		            int totalCapacity=intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		           int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		           boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
	                       status == BatteryManager.BATTERY_STATUS_FULL;
		           if(level<10){
	            		if(myPlayer.state() == MyPlayer.PLAYING_STATE){
	            		//	Global.showToast(PlayActivity.this, R.string.cannot_play,mHandler,0);
	            		//	myPlayer.pausePlayback();
	            			PromptDialog mPromptDialog = new PromptDialog(PlayActivity.this, getResources().getString(R.string.cannot_play));
	            			mPromptDialog.show();
	            			mPromptDialog.setPromptListener(new PromptListener() {
								
								@Override
								public void onComplete() {
									// TODO �Զ����ɵķ������
									myPlayer.stopPlayback();
			        				Intent intent = new Intent();
			        				intent.putExtra("currentIndex", currentIndex);
			        				PlayActivity.this.setResult(RESULT_OK, intent);
			        				finish();
								}
							});
	            			
	            		}
	            }else if(level < 20 &&!isCharging){
	            //	Global.showToast(PlayActivity.this, R.string.low_battery,null,-1);
	            } 
	    }
		}
	}


	@Override
	public void leftSlip() {
		rightKeyPress();
		
	}

	@Override
	public void rightSlip() {
		myPlayer.rewind();
		
	}

	@Override
	public void upSlip() {		
		next();
		switchAudio();
	}

	@Override
	public void downSlip() {
		previous();
		switchAudio();
		
	}
}
