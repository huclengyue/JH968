package com.sunteam.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sunteam.common.menu.BaseActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.SharedPrefUtils;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.music.dao.GetDbInfo;
import com.sunteam.music.dao.MusicInfo;
import com.sunteam.music.player.MyPlayer;
import com.sunteam.music.utils.Global;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayActivity extends BaseActivity implements MyPlayer.OnStateChangedListener{
	
	private MyPlayer myPlayer = null;
	private static final int SEEK_BAR_MAX = 10000;
	private WakeLock mWakeLock; // 禁止休眠
	private TextView tvPlayedTime;
	private TextView tvTotalTime;
	private TextView tvFileName;
	private TextView tvTitle;
	
	private TextView tvPlayNum;
	
	private SeekBar mPlaySeekBar;

	private String mSampleFile;
	private String mSampleFilePath;
	private String mTimerFormatMS;
	private String mTimerFormatHMS;
	private ImageButton play_st;  // 
	private ImageButton playMode;   // 播放模式
	private ImageButton playABMode;   // 播放模式
	//private List<String> fileList;
	private int currentIndex;
	private int total;  // 总时间
	
	private boolean gAB_flag = false;
	private int gAB_A = 0 ;  // AB 复读 A
	private int gAB_B = 0;  // AB 复读 B
	private BatteryBroadcastReciver receiver;
	private static boolean isClose = false;
	
	
	private static List<String> gPlayListName = null;   // 列表文件名
	private static List<String> gPlayListPaths = null;		// 列表文件路径
	private int gPlay_Mode = Global.MENU_PLAY_MODE_ALL;   // 全部循环
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 1){   // 音乐播放结束消息
			/*	Intent intent = new Intent();
				intent.putExtra("currentIndex", currentIndex);
				intent.putExtra("filename", gPlayListName.get(currentIndex));
				setResult(RESULT_OK, intent);
				//finish();
				 * 
*/
				MusicAddPlayList();
				Global.debug("mHandler  ={}====111");
				next();
			}else if(msg.what == 0){
				myPlayer.startPlayback(0, mSampleFile, false);
				Global.debug("mHandler  ={}====0000");
			}else if(msg.what == 2){
				mHandler.postDelayed(finishActivity, 2000);
				Global.debug("mHandler  ={}====2222");
			}else if(msg.what == 3){
				mHandler.postDelayed(finishActivityDelayed, 2000);
				Global.debug("mHandler  ={}====3333");
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
        @Override
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
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Global.debug("onCreate =====1111===");
		setContentView(R.layout.music_play);
		
		Tools mTools = new Tools(this);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(mTools.getBackgroundColor()));
				
		tvPlayedTime = (TextView) findViewById(R.id.starttime);
		tvTotalTime = (TextView) findViewById(R.id.totaltime);
		tvFileName = (TextView) findViewById(R.id.filename);
		tvTitle = (TextView) findViewById(R.id.title);
		tvPlayNum = (TextView) findViewById(R.id.play_num);
		View mLine = findViewById(R.id.line); // 分割线
		
		tvTitle.setTextColor(mTools.getFontColor()); // 设置title的文字颜色
		mLine.setBackgroundColor(mTools.getFontColor());
		tvPlayedTime.setTextColor(mTools.getFontColor());
		tvTotalTime.setTextColor(mTools.getFontColor());
		tvFileName.setTextColor(mTools.getFontColor());
		
		tvFileName.setSelected(true);   // 设置焦点才能滚动
		
		tvPlayNum.setTextColor(mTools.getFontColor());;
		
//		tvPlayedTime.setTextSize(mTools.getFontSize());
//		tvTotalTime.setTextSize(mTools.getFontSize());
//		tvFileName.setTextSize(mTools.getFontSize());
		//tvTitle.setTextSize(mTools.getFontSize());
		
		tvTitle.setText(getResources().getString(R.string.title_main));
		
	//	Global.debug("onCreate =====2222===");
		play_st = (ImageButton)findViewById(R.id.ib_paly);
		
		playMode = (ImageButton)findViewById(R.id.play_mode);
		playABMode = (ImageButton)findViewById(R.id.play_abmode);
		
		gPlayListName = new ArrayList<String>();   // 播放列表文件名
		gPlayListPaths = new ArrayList<String>();		//播放 列表文件路径
		
		gAB_flag = false ;  // 不是AB 复读模式
		
		Intent intent = getIntent();
//		Global.debug("onCreate =====2222=33==");
		mSampleFile = intent.getStringExtra("filename");
		tvFileName.setText(mSampleFile);  
		gPlayListName = intent.getStringArrayListExtra("filenamelist");
		gPlayListPaths = intent.getStringArrayListExtra("filepathlist");
		//mSampleFile = gPlayListName.get(0);
//		Global.debug("onCreate =====2222=33==mSampleFile ===" + mSampleFile);
		currentIndex = getCurrentIndex(mSampleFile);

		tvFileName.setText(mSampleFile);
		
		//Global.debug("onCreate =====3333===");
		
		mPlaySeekBar = (SeekBar) findViewById(R.id.play_seek_bar);
	    mPlaySeekBar.setMax(SEEK_BAR_MAX);
		mTimerFormatMS = getResources().getString(R.string.timer_format_ms);
		mTimerFormatHMS = getResources().getString(R.string.timer_format_hms);
		
		myPlayer = MyPlayer.getInstance(PlayActivity.this,mHandler);
		myPlayer.setOnStateChangedListener(this);
//		Global.debug("onCreate =====344444===");
		// 获取播放模式
		gPlay_Mode = SharedPrefUtils.getSharedPrefInt(this, Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_MODE, gPlay_Mode);
		
		if(gPlay_Mode == Global.MENU_PLAY_MODE_ALL){
			playMode.setBackgroundResource(R.drawable.loop_all);
		}
		else if(gPlay_Mode == Global.MENU_PLAY_MODE_SINGLE){
			playMode.setBackgroundResource(R.drawable.loop_one);
		}
		else if(gPlay_Mode == Global.MENU_PLAY_MODE_RAND){
			playMode.setBackgroundResource(R.drawable.loop_rand);
		}
		//playABMode.setEnabled(false);
		playABMode.setVisibility(View.INVISIBLE);
		//View.setVisible(View.VISIBLE); 
		//String FilePath = gPlayListPaths.get(currentIndex)+"/"+ gPlayListName.get(currentIndex);
		String FilePath = gPlayListPaths.get(currentIndex);
//		Global.debug("onCreate =====33344444=== FilePath ="+FilePath);
	
		//Global.debug("\r\n[22222222] gFilename =======");
		//AssetManager assetManager = Context.getAssets();
		//AssetFileDescriptor fileDescriptor;
	
		myPlayer.startPlayback(myPlayer.playProgress(), FilePath ,true);		
		play_st.setBackgroundResource(R.drawable.play);
		//Global.debug("onCreate =====344444===");
		total = myPlayer.fileDuration();
		//Global.debug("onCreate =====344444===total =="+total);
		if(total <= 0){
			FileError();
		}
		else{
			if(total >= 3600){
				tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
			}else{
				tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
			}
			//String path = gPlayListPaths.get(currentIndex)+"/"+ gPlayListName.get(currentIndex);
			String path = gPlayListPaths.get(currentIndex);
			Global.debug("\r\n[oncreat] path ==" + path + " Global.FristString =="+Global.FristString );
			// 所有播放的文件都要记录
//			if((Global.FristString != null) && (Global.FristString.equals(path))){
				myPlayer.SeekToTime(Global.GetPalyFristSeekTime(this, path));
//			}
			
			Global.debug("onCreate =====4444===");
			
			updateUI();
			MusicAddPlayList();
		}		
		play_st.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				if(myPlayer.state() == MyPlayer.IDLE_STATE||myPlayer.state()==MyPlayer.PLAYING_PAUSED_STATE){
					myPlayer.startPlayback(myPlayer.playProgress(), mSampleFile,true);
					play_st.setBackgroundResource(R.drawable.play);
					total = myPlayer.fileDuration();
					if(total <= 0){   // 说明文件有
						FileError();
					}
					else{
						if(total>=3600){
							tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
						}else{
							tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
						}
						updateUI();
					}
				}else if(myPlayer.state() == MyPlayer.PLAYING_STATE){
					myPlayer.pausePlayback();
					play_st.setBackgroundResource(R.drawable.pause);
				}
			}
		});	
		
		registerTFcardPlugReceiver();
	}
	// 文件错误
	private void FileError() {
		// TODO 自动生成的方法存根
		PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.error_file));
		mPromptDialog.show();
		mPromptDialog.setPromptListener(new PromptListener() {
			
			@Override
			public void onComplete() {
				// TODO 自动生成的方法存根
				if(gPlay_Mode != Global.MENU_PLAY_MODE_SINGLE && (gPlayListName.size() != 1)){
					next();
				}
			}
		});
		
	}
	@Override  
    protected void onResume() {  

        super.onResume();  
        receiver=new BatteryBroadcastReciver();  
        IntentFilter intentFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  
        registerReceiver(receiver, intentFilter);
        acquireWakeLock(this);
    }  
	
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		unregisterReceiver(tfCardPlugReceiver);
	}
	
    @Override  
    protected void onPause() {  
 
        super.onPause();  
        unregisterReceiver(receiver);  
        releaseWakeLock();
    }
	
	
	private void updateUI(){
		
		tvPlayNum .setText(""+ (currentIndex+1) + "/"+ gPlayListName.size());
		updateTimerView();
		updateSeekBar();
	}
	
	private void updateTimerView(){
		if(myPlayer.state()==MyPlayer.PLAYING_STATE){
			int current = myPlayer.progress();
			if(gAB_flag == true)
			{
				Global.debug("\r\n gAB_A ==="+ gAB_A + " gAB_B ====" + gAB_B );
				Global.debug("\r\n current ====" + current );
			}
			if((gAB_flag == true) && (current >= gAB_B)){
				myPlayer.SeekToTime(gAB_A);
			}
			else{
				if(total>=3600){
					tvPlayedTime.setText(String.format(mTimerFormatHMS,current/3600, (current%3600)/60, current%60));
				}else{
					tvPlayedTime.setText(String.format(mTimerFormatMS, current/60, current%60));
				}	
			}
			mHandler.postDelayed(mUpdateTimer, 500);
		}
	}
	// 更新 seekbar显示
	private void updateSeekBar() {
        if (myPlayer.state() == MyPlayer.PLAYING_STATE) {
            mPlaySeekBar.setProgress((int) (SEEK_BAR_MAX * myPlayer.playProgress()));
            mHandler.postDelayed(mUpdateSeekBar, 10);
        }
    }
	// 按键抬起处理
	public boolean onKeyUp(int keyCode, KeyEvent event) { 
		
		if(keyCode == KeyEvent.KEYCODE_BACK){  // 退出
			
		/*	myPlayer.stopPlayback();
			Intent intent = new Intent();
			intent.putExtra("filename", gPlayListName.get(currentIndex));
			setResult(Global.PLAY_FLAG, intent);
			finish(); */
			if(gAB_flag == true){
				gAB_flag = false;
				gAB_A = 0;
				gAB_B = 0;
				setPalyst(false);
				playABMode.setVisibility(View.INVISIBLE);  // 不显示
				PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.mode_off));
				mDialog.show();
				
				mDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根
						setPalyst(true);
					}
				});	
			}
			else{
				Key_back();
			}
			return true;
		}else if((keyCode == KeyEvent.KEYCODE_ENTER||keyCode == KeyEvent.KEYCODE_DPAD_CENTER)){
			keyupEnter();

			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){  // 上一首
			gAB_flag = false;
			playABMode.setVisibility(View.INVISIBLE);
			gAB_A = 0;
			gAB_B = 0;
			if(gPlay_Mode == Global.MENU_PLAY_MODE_SINGLE){   // 单曲循环可以切歌曲
				if((currentIndex-1)>=0){  
	    			currentIndex--;   
	    		}else{  
	    			currentIndex = gPlayListName.size()-1;
	        	//Global.showToast(PlayActivity.this, R.string.turn2end,null,-1);
	    		}
			}
			previous();
			return true;
			//switchAudio();
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){  // 下一首
			gAB_flag = false;
			playABMode.setVisibility(View.INVISIBLE);
			gAB_A = 0;
			gAB_B = 0;
			if(gPlay_Mode == Global.MENU_PLAY_MODE_SINGLE){  // 单曲循环可以切歌曲
				if((currentIndex+1)<gPlayListName.size()){  
		        	currentIndex++;  
		        }else{  
		        	currentIndex = 0;
		        }
			}
			next();
			//switchAudio();
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){ // 快退
			//myPlayer.rewind();
			leftKeyPress();
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){  // 快进
			rightKeyPress();
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_MENU){
			//MusicAddSaveList();
			startMenu(keyCode, gPlayListPaths.get(currentIndex), gPlayListName.get(currentIndex));
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_1){   // 数字 0  A
			Global.debug("0000000000000000000000000000000000000000");
			setPalyst(false);
			PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.mode_A));
			mDialog.show();
			mDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					// TODO 自动生成的方法存根
					gAB_A = myPlayer.progress();
					playABMode.setVisibility(View.VISIBLE);
					playABMode.setBackgroundResource(R.drawable.loop_a);
					setPalyst(true);
				}
			});
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_3){   // 数字3 B
			Global.debug("3333333333333333333333333333333333333333333");
			setPalyst(false);
			PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.mode_AB));
			mDialog.show();
			mDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					// TODO 自动生成的方法存根
					gAB_flag = true;
					gAB_B = myPlayer.progress();
					myPlayer.SeekToTime(gAB_A);
					playABMode.setVisibility(View.VISIBLE);
					playABMode.setBackgroundResource(R.drawable.loop_ab);
					setPalyst(true);
				}
			});		
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_4 ||keyCode == KeyEvent.KEYCODE_5||
				keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7 ||keyCode == KeyEvent.KEYCODE_8||
				keyCode == KeyEvent.KEYCODE_9 || keyCode == KeyEvent.KEYCODE_0 ){
			if(gAB_flag == true){
				gAB_flag = false;
				playABMode.setVisibility(View.INVISIBLE);
				
				gAB_A = 0;
				gAB_B = 0;
				setPalyst(false);
				PromptDialog mDialog = new PromptDialog(this, getResources().getString(R.string.mode_off));
				mDialog.show();
				
				mDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根
						setPalyst(true);
					}
				});	
				
			}
			return true;
		}
		
		return super.onKeyUp(keyCode, event);
	}
	// Ok 键处理	
	private void keyupEnter() {

		if(myPlayer.state() == MyPlayer.IDLE_STATE||myPlayer.state()==MyPlayer.PLAYING_PAUSED_STATE){
			myPlayer.startPlayback(myPlayer.playProgress(), mSampleFile,true);
			play_st.setBackgroundResource(R.drawable.play);
			total = myPlayer.fileDuration();
			if(total <= 0){
				FileError();
			}
			else{
				if(total>=3600){
					tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
				}else{
					tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
				}
				updateUI();
			}
			acquireWakeLock(this);
		}else if(myPlayer.state() == MyPlayer.PLAYING_STATE){
			myPlayer.pausePlayback();
			play_st.setBackgroundResource(R.drawable.pause);
			releaseWakeLock();
		}
	}

	// Ok 键处理	
	private void setPalyst(boolean flag) {
		if(flag == true){
			if(myPlayer.state() == MyPlayer.IDLE_STATE||myPlayer.state()==MyPlayer.PLAYING_PAUSED_STATE){
				myPlayer.startPlayback(myPlayer.playProgress(), mSampleFile,true);
				play_st.setBackgroundResource(R.drawable.play);
				total = myPlayer.fileDuration();
				if(total <= 0){
					FileError();
				}
				else{
					if(total>=3600){
						tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
					}else{
						tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
					}
					updateUI();
				}
			}
		}
		else{
			myPlayer.pausePlayback();
			play_st.setBackgroundResource(R.drawable.pause);
		}
	}

	
	public static final long INTERVAL = 2000L; //?ж?????keydown???????
	private static long lastDownTime = 0L; //?????down?????
	 
	 /**
	  * ?ж?????????????,???true?????false
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
		//	 Global.showToast(this, R.string.speed2end, mHandler,3);
		 }else{
			 mHandler.postDelayed(finishActivity, 2000);	//1s???????д????
		 }
	 }
	
	private int getCurrentIndex(String fileName){
		return gPlayListName.indexOf(fileName);
	}
	
	//上一首
    private void previous() { 
    	if(gPlay_Mode == Global.MENU_PLAY_MODE_ALL){  // 全部循环
    		if((currentIndex-1)>=0){  
    			currentIndex--;   
    		}else{  
    			currentIndex = gPlayListName.size()-1;
        	//Global.showToast(PlayActivity.this, R.string.turn2end,null,-1);
    		}
    	}
        else if(gPlay_Mode == Global.MENU_PLAY_MODE_RAND){
    		currentIndex = new Random().nextInt(gPlayListName.size());
    	}
        switchAudio();
    }  
  
    //下一首
    private void next() { 
    	if(gPlay_Mode == Global.MENU_PLAY_MODE_ALL){  // 全部循环
	        if((currentIndex+1)<gPlayListName.size()){  
	        	currentIndex++;  
	        }else{  
	        	currentIndex = 0;
	        	//Global.showToast(PlayActivity.this, R.string.turn2start,null,-1);      	
	        }
    	}
    	else if(gPlay_Mode == Global.MENU_PLAY_MODE_RAND){
    		currentIndex = new Random().nextInt(gPlayListName.size());
    	}
    	 
        switchAudio();
    }
    
    // 切换播放
    private void switchAudio(){
    	mSampleFile = gPlayListName.get(currentIndex);
    	mSampleFilePath = gPlayListPaths.get(currentIndex);
    	
		tvFileName.setText(mSampleFile);
		//myPlayer.startPlayback(0, mSampleFilePath+"/"+mSampleFile,false);
		myPlayer.startPlayback(0, mSampleFilePath,false);
		play_st.setBackgroundResource(R.drawable.play);
		total = myPlayer.fileDuration();
		if(total <= 0){
			FileError();
		}
		else{
			MusicAddPlayList();
			if(total>=3600){
				tvTotalTime.setText(String.format(mTimerFormatHMS, total/3600,(total%3600)/ 60,total% 60));
			}else{
				tvTotalTime.setText(String.format(mTimerFormatMS, total/ 60,total% 60));
			}
		}
    }
	// 右键 处理
    private void rightKeyPress(){
		if(myPlayer.endTime()<1000){  // 到结尾 提示
			if(myPlayer.state() == MyPlayer.PLAYING_STATE){
				myPlayer.stopPlayback();
				isClose = false;
				mHandler.postDelayed(finishActivity, 0);
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.file_end));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						next();  // 下一首
					}
				});
				//Global.showToast(this, R.string.speed2end, null,-1);
			}else if(myPlayer.state() == MyPlayer.IDLE_STATE){
				mHandler.removeCallbacks(switchClose);		//?????δ??е?runnable
				mHandler.postDelayed(switchClose, 2000);	//2s???y????activity??????true
				if(!filter()){ //
					isClose = true;
				}else{	//
					//Global.showToast(this, R.string.speed2end, null,-1);
					isClose = false;
				}
			}					
		}else{
			myPlayer.speed();
		}			
	
    }

    // 快退 10S
	private void leftKeyPress(){
    	
    	if(myPlayer.startTime() < Global.SEEK_LEN){  // 到开头
    		Global.debug("leftKeyPress === myPlayer.startTime() == %d"+ myPlayer.startTime());
    		
			if(myPlayer.state() == MyPlayer.PLAYING_STATE){
				myPlayer.stopPlayback();
				isClose = false;
				mHandler.postDelayed(finishActivity, 0);
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.file_start));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根
						switchAudio();;  // 下一首
					}
				});
				//Global.showToast(this, R.string.speed2end, null,-1);
			}else if(myPlayer.state() == MyPlayer.IDLE_STATE){
				mHandler.removeCallbacks(switchClose);		//
				mHandler.postDelayed(switchClose, 2000);	//
				
				
				PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.file_start));
				mPromptDialog.show();
				mPromptDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根
						switchAudio();;  // 下一首
					}
				});
/*				
				if(!filter()){ //
					isClose = true;
				}else{	//
					//Global.showToast(this, R.string.speed2end, null,-1);
					isClose = false;
				}
				*/
			}
    	}
    	else{
    		myPlayer.rewind();
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
           // Global.showToast(PlayActivity.this, R.string.error_file, mHandler, 1);
        }
    }
	
	
	public class BatteryBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
	            	//
		           int level=intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//		            //
//		            int totalCapacity=intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		           int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		           boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
	                       status == BatteryManager.BATTERY_STATUS_FULL;
		           if(level<10){
	            		if(myPlayer.state() == MyPlayer.PLAYING_STATE){
	            			//Global.showToast(PlayActivity.this, R.string.cannot_play,mHandler,0);
	            			myPlayer.pausePlayback();
	            			
	            			PromptDialog mpDialog = new PromptDialog(PlayActivity.this, getResources().getString(R.string.bat_lv0));
	            			mpDialog.show();
	            			mpDialog.setPromptListener(new PromptListener() {
								
								@Override
								public void onComplete() {
									// TODO 自动生成的方法存根
									Key_back();
									
								}
							});
	            			
	            			
	            		}
	            }else if(level<20&&!isCharging){
	            	//Global.showToast(PlayActivity.this, R.string.low_battery,null,-1);
	            } 
			}
		}
	}

	private void Key_back() {
		// TODO 自动生成的方法存根
		MusicAddPlayList(); // 先保存 
		Global.FristString =  Global.GetPalyFristData(this);   // 获取最后一次伯村的路径
		myPlayer.stopPlayback();
		isClose = true;
		Intent intent = new Intent();
		intent.putExtra("filename", gPlayListName.get(currentIndex));
		setResult(Global.PLAY_FLAG, intent);
		finish();
	}
	private void Key_back_forTF() {
		// TODO 自动生成的方法存根
		MusicAddPlayList(); // 先保存 
		Global.FristString =  Global.GetPalyFristData(this);   // 获取最后一次伯村的路径
		myPlayer.stopPlayback();
		isClose = true;
		Intent intent = new Intent();
		intent.putExtra("filename", gPlayListName.get(currentIndex));
		intent.putExtra("flag", 1);
		setResult(Global.PLAY_FLAG, intent);
		finish();
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
		//switchAudio();
	}

	@Override
	public void downSlip() {
		previous();
		//switchAudio();
	}
	// 增加 最近 播放 记录
	public void MusicAddPlayList()
	{
		boolean data_flag = false;  // 没有数据
		MusicInfo musicinfo = new MusicInfo();   //创建 结构体
		MusicInfo musicinfo_temp = new MusicInfo();   //创建 结构体
		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
		
		String Filename = gPlayListName.get(currentIndex);
    	String FilePath = gPlayListPaths.get(currentIndex);
    	ArrayList<MusicInfo> musicInfos = new ArrayList<MusicInfo>();
    	musicInfos = dbMusicInfo.GetAllData(Global.PLAY_LIST);   // 获取所有数据
    	
    	Global.debug("\r\n[MusicAddPlayList] Filename =="+Filename + "  FilePath =="+FilePath);
    	int max_id = dbMusicInfo.getMaxId(Global.PLAY_LIST_ID);  
    	int max_save = dbMusicInfo.getCount(Global.PLAY_LIST_ID);  // 记录条数
    	Global.debug("\r\n MusicAddPlayList ===== max_id = " + max_id);
    	
    	Global.debug("\r\n total = " + total  + "   myPlayer.progress() ==="+ myPlayer.progress());
    	int playtime = 0;
    	
    	if((total - myPlayer.progress()) <= 2){
    		playtime = 0;
    	}
    	else{
    		playtime = myPlayer.progress();
    	}
    			
    	if(max_id <= 0){  // 无数据
    		musicinfo._id = 1;
    		musicinfo.path = FilePath;
    		musicinfo.filename = Filename;
    		musicinfo.playtime = playtime; 
    		
    		dbMusicInfo.add(musicinfo , Global.PLAY_LIST_ID);
    	}
    	else if(max_save >= Global.MAX_SAVE_LIST_NUM){  // 已经是最大值
    		musicinfo._id = 1;
    		musicinfo.path = FilePath;
    		musicinfo.filename = Filename;
    		musicinfo.playtime = playtime;//.progress();
    		for(int i = 0 ; i < musicInfos.size(); i++)
    		{
    			musicinfo_temp = musicInfos.get(i);
    			if((musicinfo_temp.getPath()+"/" + musicinfo_temp.getFileName()).equals(FilePath+"/"+Filename)){
    				data_flag = true;
    				break;
    			}
    		}
    		if(true == data_flag /*dbMusicInfo.hasRecord(Global.PLAY_LIST, musicinfo)*/) // 有此条记录
    		{
    			//dbMusicInfo.deteleForOneByPath(Filename, FilePath, Global.PLAY_LIST_ID);  // 删除此条记录
    			//dbMusicInfo.deteleForOneByP(Filename, FilePath, Global.PLAY_LIST_ID);  // 删除此条记录
    			dbMusicInfo.deteleForOne(musicinfo_temp._id, Global.PLAY_LIST_ID); // 删除 最小的
    			musicinfo._id = max_id + 1;
        		musicinfo.path = FilePath;
        		musicinfo.filename = Filename;
        		musicinfo.playtime = playtime;
        		dbMusicInfo.add(musicinfo, Global.PLAY_LIST_ID);
    		}
    		else{
    			int minid = dbMusicInfo.getMinId(Global.PLAY_LIST_ID);
    			Global.debug("\r\nMusicAddPlayList  minid =="+ minid);
    			
    			dbMusicInfo.deteleForOne(minid, Global.PLAY_LIST_ID); // 删除 最小的
	    		musicinfo._id = max_id + 1;
	    		musicinfo.path = FilePath;
	    		musicinfo.filename = Filename;
	    		musicinfo.playtime = playtime;
	    		dbMusicInfo.update(musicinfo, Global.PLAY_LIST_ID);
    		}
    	}
    	else{ 
    		musicinfo._id = 1;
    		musicinfo.path = FilePath;
    		musicinfo.filename = Filename;
    		musicinfo.playtime = playtime;
    		
    		for(int i = 0 ; i < musicInfos.size(); i++)
    		{
    			musicinfo_temp = musicInfos.get(i);
    			if((musicinfo_temp.getPath()+"/" + musicinfo_temp.getFileName()).equals(FilePath+"/"+Filename)){
    				data_flag = true;
    				break;
    			}
    		}
    		
    		if(true == data_flag/*dbMusicInfo.hasRecord(Global.PLAY_LIST, musicinfo)*/) // 有此条记录
    		{
    			dbMusicInfo.deteleForOne(musicinfo_temp._id, Global.PLAY_LIST_ID); // 删除 最小的
    			//dbMusicInfo.deteleForOneByPath(Filename , FilePath, Global.PLAY_LIST_ID);  // 删除此条记录
    		}
    		
    		musicinfo._id = max_id + 1;
    		musicinfo.path = FilePath;
    		musicinfo.filename = Filename;
    		musicinfo.playtime = playtime;
    		dbMusicInfo.add(musicinfo, Global.PLAY_LIST_ID);
    		
    	}
	}
/*	
	// 增加我的最爱 记录
	public void MusicAddSaveList()
	{
		MusicInfo musicinfo = new MusicInfo();   //创建 结构体
		GetDbInfo dbMusicInfo = new GetDbInfo( this ); // 打开数据库
		
		String Filename = gPlayListName.get(currentIndex);
    	String FilePath = gPlayListPaths.get(currentIndex);
    	int max_id = dbMusicInfo.getMaxId(Global.SAVE_LIST_ID);
    	Global.debug("\r\n MusicAddPlayList ===== max_id = " + max_id);
    	if(max_id <= 0){  // 无数据
    		musicinfo._id = 1;
    		musicinfo.path = FilePath;
    		musicinfo.filename = Filename;
    		
    		dbMusicInfo.add(musicinfo , Global.SAVE_LIST_ID);
    		TtsUtils.getInstance().speak(getResources().getString(R.string.file_add_ok));
    	}   
    	else{
    		boolean flag = false;
    		ArrayList<MusicInfo> mMusicInfo = new ArrayList<MusicInfo>();
    		mMusicInfo = dbMusicInfo.GetAllData(Global.SAVE_LIST);
    		Global.debug("\r\n mMusicInfo.size() === "+ mMusicInfo.size());	
    		for(int i = 0; i < mMusicInfo.size(); i++)
    		{
    			musicinfo = mMusicInfo.get(i);
    			Global.debug("\r\n musicinfo.path === "+ musicinfo.path + " FilePath =="+FilePath);	
    			if(FilePath.equals(musicinfo.getPath())){ // 文件重复
    				flag = true;
    				break;
    			}
    		}
    		
    		if(true == flag){  // 文件重复
    			TtsUtils.getInstance().speak(getResources().getString(R.string.file_exists));
    		}
    		else{  // 添加文件
	    		musicinfo._id = max_id + 1;
	    		musicinfo.path = FilePath;
	    		musicinfo.filename = Filename;
	    		dbMusicInfo.add(musicinfo, Global.SAVE_LIST_ID);
	    		TtsUtils.getInstance().speak(getResources().getString(R.string.file_add_ok));
    		}
    	}
    	dbMusicInfo.closeDb();
	}
	*/
	// 进入功能菜单 
	private void startMenu(int defaultItem, String path, String filename) {
		setPalyst(false);
		
		Intent intent = new Intent();
		intent.putExtra("PATH", path); // 设置标题
		intent.putExtra("FILENAME", filename); // 设置数据
		intent.setClass(this, MenuPalyMenuActivity.class);
		Global.debug("startMenu ======= wwwww===");
		startActivityForResult(intent, Global.PLAY_MENU_PLAY_FLAG);
	}
	// 界面返回
	@SuppressWarnings("deprecation")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Global.debug("\r\n[11] onActivityResult == requestCode ="+requestCode + " resultCode ==" + resultCode );
		if (requestCode == Global.PLAY_MENU_PLAY_FLAG && resultCode == Global.PLAY_MENU_PLAY_FLAG) {
				
			//Global.debug("onActivityResult =====1111============\r\n");
		}		
		gPlay_Mode = SharedPrefUtils.getSharedPrefInt(this, Global.MUSIC_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.MUSIC_MODE, gPlay_Mode) ;
		if(gPlay_Mode == Global.MENU_PLAY_MODE_ALL){
			playMode.setBackgroundResource(R.drawable.loop_all);
		}
		else if(gPlay_Mode == Global.MENU_PLAY_MODE_SINGLE){
			playMode.setBackgroundResource(R.drawable.loop_one);
		}
		else if(gPlay_Mode == Global.MENU_PLAY_MODE_RAND){
			playMode.setBackgroundResource(R.drawable.loop_rand);
		}
		setPalyst(true);
	}
	
	
	@SuppressWarnings("deprecation")
	private void acquireWakeLock(Context context) {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, context.getClass().getName());
			mWakeLock.acquire();
		}
	}

	private void releaseWakeLock() {
		if (null != mWakeLock && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
	
	
	
	// 获取当前路径
	private String getCurPath(){
		return gPlayListPaths.get(currentIndex);
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
	// 插拔消息  接收
    private BroadcastReceiver tfCardPlugReceiver = new BroadcastReceiver() { 
   
        @Override 
        public void onReceive(Context context, Intent intent) { 
        	 Global.debug("\r\n  tfCardPlugReceiver ========2222====play==============");   
            String action = intent.getAction();
            
            String mData = intent.getDataString();  // 获取路径
            Global.debug("\r\n mData ========play======mData.length() +  " + mData.length());
            mData = mData.substring(7,mData.length());
            Global.debug("\r\n mData ========play====== " + mData);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) { // 插入
                
                Global.debug("\r\n tf卡插入========play====== ");
            }
            else if(Intent.ACTION_MEDIA_EJECT.equals(action)){  // Tf 卡拔出
            	
            	String mPath = getCurPath();
            	Global.debug("\r\n mPath ======play======== " + mPath);
            	if(mPath.contains(mData)){  // 包含
	            	if(mData.contains(Global.MENU_PATH_EXTSD) ){  // 存储卡
	            		Global.debug("\r\n tf卡 列表更新=======play======= ");
	            		Key_back_forTF();
	            	}
	            	else if(mData.contains(Global.MENU_PATH_UDISK) ){  // U盘
	            		Global.debug("\r\n U盘 列表更新======play======== ");
	            		Key_back_forTF();
	            	}
            	}
            	Global.debug("\r\n tf卡 拔出=======play======= ");
            }
            else if(Intent.ACTION_MEDIA_REMOVED.equals(action)){
            	Global.debug("\r\n tf卡 ACTION_MEDIA_REMOVED============== ");
            }
            else if(Intent.ACTION_MEDIA_SHARED.equals(action)){
            	Global.debug("\r\n tf卡 ACTION_MEDIA_SHARED============== ");
            }
            
        }            
    };
	
}
