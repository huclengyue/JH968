/** Copyright 2009-2013 Broadcom Corporation
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
//import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.broadcom.fm.fmreceiver.FmProxy;
import com.broadcom.fm.fmreceiver.IFmProxyCallback;
import com.broadcom.fm.fmreceiver.IFmReceiverEventHandler;
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.SharedPrefUtils;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.fmradio.R;
import com.sunteam.fmradio.dao.FmInfo;
import com.sunteam.fmradio.dao.GetDbInfo;
import com.sunteam.fmradio.rx.IRadioViewRxTouchEventHandler;
import com.sunteam.fmradio.utils.FmConstants;
import com.sunteam.fmradio.utils.Global;

/**
 * An example FM Receiver application that supports the following features: <li>
 * Access to the FM Receiver Service. <li>Preset station management. <li>RDS
 * capability
 */
@SuppressLint({ "NewApi", "HandlerLeak", "DefaultLocale" })
@SuppressWarnings({"unused", "deprecation"})
public class FmRadio extends MenuActivity implements IRadioViewRxTouchEventHandler, IFmProxyCallback {

    public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";

    private WakeLock mWakeLock; // 禁止休眠
    private boolean  speakerFlag = false;  // true ----> 耳机插入    false --- > 耳机拔出
    private boolean  gheadin = false;  
    /* CONSTANT BLOCK */

     private ArrayList<Integer> radioFreqList = new ArrayList<Integer>(); // 存放电台频率
    private int mRadioFreq = Global.DEFAULT_FREQUENCY;   // 默认电台

    private FmReceiverEventHandler mFmReceiverEventHandler;
    private FmProxy mFmReceiver;

    private final static String freqPreferenceKey = "channel";
    private final static String lastFreqPreferenceKey = "last";

    /* Local GUI status variables. */
	private int mWorldRegion = FmProxy.FUNC_REGION_DEFAULT;

    private int mFrequency = Global.DEFAULT_FREQUENCY;   // 当前频道
    private int mFrequencyStep = 10; // Step in increments of 100 Hz  // 每次增加 100hz
    private int mMinFreq, mMaxFreq; // updated with mPendingRegion

    private boolean mSeekInProgress = false;
    boolean mPowerOffRadio = false;

    /* Pending values. (To be requested) (Startup values specified) */
    private int mPendingRegion = FmProxy.FUNC_REGION_DEFAULT;
    private int mPendingDeemphasis = FmProxy.DEEMPHASIS_75U;
    private int mPendingAudioMode = FmProxy.AUDIO_MODE_AUTO;
    private int mPendingAudioPath = FmProxy.AUDIO_PATH_WIRE_HEADSET;   // 默认使用 耳机
    private int mPendingFrequency = Global.DEFAULT_FREQUENCY;   // 当前频道
    private boolean mPendingMute = false;
    private int mPendingScanStep = FmProxy.FREQ_STEP_100KHZ; // Step in

    private int mPendingScanMethod = FmProxy.SCAN_MODE_FAST;
    private int mPendingRdsMode = FmProxy.RDS_MODE_OFF;
    private int mPendingRdsType = FmProxy.RDS_COND_NONE;
    private int mPendingAfMode = -1; /* force update to be sent at power on */
    private int mPendingNflEstimate = FmProxy.NFL_MED;
    private int mPendingSearchDirection = FmProxy.SCAN_MODE_NORMAL;
    private boolean mPendingLivePoll = false;
    private int mPendingSnrThreshold = FmProxy.FM_MIN_SNR_THRESHOLD;
    private int mPendingLivePollinterval = 2000; // 2 second polling of rssi

    /* Pending updates. */
    private boolean shutdownPending = false;
   // private boolean worldRegionUpdatePending = false;
    private boolean audioModeUpdatePending = false;
    private boolean audioPathUpdatePending = false;
    private boolean frequencyUpdatePending = false;
    private boolean muteUpdatePending = false;
    private boolean scanStepUpdatePending = false;
   // private boolean rdsModeUpdatePending = false;
    private boolean nflEstimateUpdatePending = false;
    private boolean stationSearchUpdatePending = false;
    private boolean livePollingUpdatePending = false;
    private boolean fmVolumeUpdatepending = false;
    private boolean fmSetSnrThresholdPending = false;
    private boolean mFinish = false;
    private boolean bFinishCalled = false;
    String mRdsProgramTypes[];

  //  NotificationManager mNotificationManager;
    TelephonyManager mTelephonyManager;
    AudioManager mAudioManager;

    //private HeadsetPlugUnplugBroadcastReceiver mHeadsetPlugUnplugBroadcastReceiver;
    private MyPhoneStateListener mPhoneStateListener;

    private boolean mInCall = false;
    private Timer mytimer;      //adding  定时器
    private Context mContext;
    private boolean mSoundEffectState = false;
    
    private ArrayList<String> chanel_str = null;   // 记录频道数据 字串
    //private ArrayList<String> chanel_di = null;   // 记录频道数据  数字
    private int chanel_num = 0;   // 记录电台数量
    private int chanel_frist = 0;   // 第一次搜到的电台
     
    private boolean mSearchFlag = false;    // 搜台标志
    private int mMax_vol = 0;    // 最大音量
    private int mcur_vol = 0;    // 当前音量
    
    private BatteryBroadcastReciver batterReceiver;  // 电池电量获取
    
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        Global.debug("\r\n [onCreate} =============111");
        
        int freq = 0; 
        freq = SharedPrefUtils.getSharedPrefInt(this, Global.FM_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.FM_SELECT, freq);
        if(freq != 0){
        	mRadioFreq = mFrequency = mPendingFrequency = freq;
        }
        else{
        	mRadioFreq = mFrequency = mPendingFrequency = Global.DEFAULT_FREQUENCY; 
        }
        Global.debug("\r\n[onCreate]  +=  mRadioFreq ====="+ mRadioFreq);
        chanel_str = new ArrayList<String>();
        chanel_str.clear();   // 清空
        
        chanel_num = 0;
        mSearchFlag = false;
        
        if (mFmReceiver == null && !bFinishCalled) { // Avoid calling getProxy
        	Global.debug("\r\n[onCreate   ]Getting FmProxy proxy...");
			FmProxy.getProxy(this, this);
		}
        Global.debug("\r\n [onCreate]   mFmReceiver==" + mFmReceiver);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);     // 音频 服务
        
        mMax_vol = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );  // 音乐最大音量
        mcur_vol = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC ); // 当前音乐音量
        //Global.debug("vol  max == "+max+" current ===" + current);
        updateFMVolume(mcur_vol); // 同步收音机音量
               
        // 获取 收音机制式
        updateMinMaxFrequencies();
        
        if(false ){  // 耳机插入
        	speakerFlag = true;
        	ArrayList<String> mTmpList = new ArrayList<String>();
	        mTmpList = getChanelData();
	        Global.debug("\r\n mTmpList ==== "+ mTmpList);
	        if(null == mTmpList){
		//        mTitle = DEFAULT_FREQUENCY/100+"."+ DEFAULT_FREQUENCY%100/10+"MHz";
		        String s = String.format("%.1f", mRadioFreq / Global.fmScale);
		        Global.debug("======="+s);
		        mTitle =  s + "MHz";
		        chanel_str.add(mTitle);
		        chanel_num++;
		        
		        Global.debug("==222====="+mMenuList);
		        //mMenuList.addAll(chanel_str);
		        mMenuList = chanel_str;
		        radioFreqList.add(mRadioFreq);
		        Global.debug("==3333====="+s);
	        }
	        else{
	        	for(int i = 0; i < mTmpList.size(); i++){
	        		String s = String.format("%.1f", Integer.valueOf(mTmpList.get(i)) / Global.fmScale);
	        		chanel_str.add(s + getResources().getString(R.string.mhz));

	        		radioFreqList.add(Integer.valueOf(mTmpList.get(i)));
	        		chanel_num++;
	        	}
	        	mTitle = chanel_str.get(0); 
	        	//mMenuList = mTmpList;
	        	mMenuList= chanel_str;
	        }
	        selectItem = 0;
        }
        else{  // 耳机没有插入
        	ArrayList<String> mTmpList = new ArrayList<String>();
        	speakerFlag = false; 
        	mTmpList.clear();
        	mMenuList = mTmpList;
        	mTitle = getResources().getString(R.string.title_nohearphone);  // 耳机没有插入
        }
        
        Global.debug("onCreate=============111 " + mTitle);
        super.onCreate(savedInstanceState);
      //  mMenuView.setSelectItem(0);
        
        if (mAudioManager.isMusicActive()) {    // 获取 是否在播放音乐
            Toast.makeText(getApplicationContext(), "Stop audio from other app and relaunch",
                    Toast.LENGTH_LONG).show();
            TtsUtils.getInstance().speak(getResources().getString(R.string.music_is_playing));
            finish();
        }  
   
 /* 
        // 获取设备模式 在 飞行模式是 不可以打开收音机
        if (Settings.System.getInt(getApplicationContext().getContentResolver(), 
        		Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
            Toast.makeText(this, "Cannot open FMRadio in Airplane mode", Toast.LENGTH_SHORT).show();
            bFinishCalled = true;
            Global.debug("Airplane mode ====================\r\n");
            finish();
        }
*/        
        registerReceiver(mMediaStateReceiver, new IntentFilter(PLAYSTATE_CHANGED));
        registerReceiver(mAirplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));  // 飞行模式 改变
        registerHeadsetPlugReceiver();   // 注册耳机消息
        registerBatReceiver();
        // 定时器
        mytimer = new Timer();
        TimerTask mytask  = new TimerTask(){  
        	@Override
			public void run(){
                System.out.println("----------myTime''s up ");  

                mAudioManager.setParameters("linein_en=1");         //adding
                System.out.println("----------myTime'go to  terminate");  
                mytimer.cancel(); //Terminate the timer thread  
        	}  
        };  
       mytimer.schedule(mytask, 3000);  // 1秒后执行
                
     //  updateFrequency(DEFAULT_FREQUENCY);   
     /*
       // 只有在耳机插入时才可以
       if(true){   
    	   int freq = radioFreqList.get(0);
    	   updateFrequency(freq);
       }*/
       
      // doSoundEffect(false);
	}
		// 获取数据 频道
	
	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();
		Global.debug("\r\n onResume ==========");
		acquireWakeLock(this);
		//doSoundEffect(false);
	}
	
	@Override
	protected void onPause() {
		// TODO 自动生成的方法存根
		super.onPause();
		Global.debug("\r\n onPause ==========");
		releaseWakeLock();
	}
	
	// 声音控制
	private void doSoundEffect(boolean state) {        // state : 0  apk 启动，  1 apk 退出恢复soundeffect
           ContentResolver res;
           int ret;
    
           res = this.getContentResolver();//mContext.getContentResolver();
           Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SOUND_EFFECTS_ENABLED);  
           ret =  Settings.System.getInt(res, Settings.System.SOUND_EFFECTS_ENABLED, 0);
//               Global.debug( " ---------====== Settings.System.SOUND_EFFECTS_ENABLED: " + ret);
            if(ret != 0)
            {
                mSoundEffectState = true;
                android.provider.Settings.System.putInt(res, Settings.System.SOUND_EFFECTS_ENABLED, (0));  
                res.notifyChange(uri, null);  
            }
            else
            {
                if(state && mSoundEffectState)
                {
                    android.provider.Settings.System.putInt(res, Settings.System.SOUND_EFFECTS_ENABLED, (1));  
                    res.notifyChange(uri, null);  
                }
            }        
    }

    	
/*    // 恢复
	    protected void onResume() {
	
	        Global.debug("onResume =============start===");
	
	//        if (mAudioManager.isMusicActive()) {
	//            finish();
	//        }
	
	        if (mFmReceiver == null && !bFinishCalled) { // Avoid calling getProxy
	                                                     // is finish has been
	                                                     // called already
	            if (V) {
	                Global.debug( "Getting FmProxy proxy...");
	            }
	            FmProxy.getProxy(this, this);
	        }
	
	        setVolumeControlStream(AudioManager.STREAM_MUSIC);
	        if (mHeadsetPlugUnplugBroadcastReceiver == null)
	            mHeadsetPlugUnplugBroadcastReceiver = new HeadsetPlugUnplugBroadcastReceiver();
	        // sticky intent - use last value if exists, otherwise no headset
	        Intent intent = registerReceiver(mHeadsetPlugUnplugBroadcastReceiver, new IntentFilter(
	                Intent.ACTION_HEADSET_PLUG));
	        if (intent != null)
	            mHeadsetPlugUnplugBroadcastReceiver.onReceive(this, intent);
	        else
	            wiredHeadsetIsOn(false);
	        
	        Global.debug("onResume =============end===");
	        super.onResume1();
	    }*/
	@Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (TtsUtils.getInstance() != null) {
			TtsUtils.getInstance().destroy();
		}
        updateMuted(true);
		 
    	Global.debug("Calling onDestroy()");
        Global.debug("FmRadio --------------ondestroy- go to set linein- disable---");
        mAudioManager.setParameters("linein_en=0");         //adding
        unregisterReceiver(mMediaStateReceiver);
        unregisterReceiver(mAirplaneModeReceiver);
        unregisterReceiver(headsetPlugReceiver);
        unregisterReceiver(batterReceiver);
        if (mFmReceiver != null) {
        	Global.debug("Finishing FmProxy proxy...");
            mFmReceiver.unregisterEventHandler();
            mFmReceiver.finish();
            mFmReceiver = null;
        }
        
//        mTelephonyManager.listen(mPhoneStateListener, 0);

        doSoundEffect(true);       //adding
    }



// 接收音频广播
    private BroadcastReceiver mMediaStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PLAYSTATE_CHANGED)) {
                Global.debug(intent.toString());
                boolean isPlaying = false;
                // Media service sends playing info in two different "extra"
                // So check for both "playing" and "playstate"
                // to know whether Media is playing.
                if (intent.hasExtra("playing")) {
                    isPlaying = intent.getBooleanExtra("playing", false);
                } else if (intent.hasExtra("playstate")) {
                    // This "playstate" gives either media is actively playing
                    // or paused,
                    String playState = intent.getStringExtra("playstate");
                    if (0 == playState.compareTo("playing")) {
                        isPlaying = true;
                    }
                }
                Global.debug("received: " + action + ", playing: " + isPlaying);
                if (!isPlaying) {
                    // This will send the Audio Path during
                    // retryPendingCommands() properly
                    audioPathUpdatePending = true;
                    return;
                }
                Global.debug("Media player started !!.. Shutting down the fm Radio..");
                if (mFmReceiver != null)
                    mFmReceiver.setAudioPath(FmProxy.AUDIO_PATH_NONE);
                bFinishCalled = true;
                if (null != mFmReceiver && mFmReceiver.getRadioIsOn() == true) {
                    mPowerOffRadio = true;
                    powerDownSequence();
                    shutdownPending = true;
                    mFinish = true;
                } else {
                    finish();
                }
              //  mNotificationManager.cancelAll();
            }
        }
    };
    // 飞行模式 广播接收
    private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Global.debug("mAirplaneModeReceiver action: " + action);
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean isModeOn = intent.getBooleanExtra("state", false);
                Global.debug("received: " + action + ", playing: " + isModeOn);
                if (!isModeOn) {
                    audioPathUpdatePending = true; // This will send the Audio
                                                   // Path during
                                                   // retryPendingCommands()
                                                   // properly
                    return;
                }
                Global.debug("Airplane Mode ON !!.. Shutting down the fm Radio..");
               // Toast.makeText(getApplicationContext(), "Shutting down FMRadio in Airplane mode",
                //        Toast.LENGTH_SHORT).show();
                if (mFmReceiver != null)
                    mFmReceiver.setAudioPath(FmProxy.AUDIO_PATH_NONE);
                bFinishCalled = true;
                if (null != mFmReceiver && mFmReceiver.getRadioIsOn() == true) {
                    mPowerOffRadio = true;
                    powerDownSequence();
                    shutdownPending = true;
                    mFinish = true;
                } else {
                    finish();
                }
             //   mNotificationManager.cancelAll();
            }
        }
    };

    /**
     * This function is called to initialize pending operations and activate the
     * FM Radio HW on startup.
     */
    // 开机
    private void powerUpSequence() {
      Global.debug("【开机】powerUpSequence ===== ");
        int status;

        /* Set pending updates to trigger on response from startup. */
        // enabledUpdatePending = false;
        shutdownPending = false;
        audioModeUpdatePending = true;
        audioPathUpdatePending = true;
        nflEstimateUpdatePending = false;
        frequencyUpdatePending = true;
        scanStepUpdatePending = true;
//        rdsModeUpdatePending = true;
        fmVolumeUpdatepending = true;
        livePollingUpdatePending = false;
        stationSearchUpdatePending = false;
        mSeekInProgress = false;
        fmSetSnrThresholdPending = false;

        /* Initialize the radio. This can give us GUI initialization info. */
        Global.debug("【开机】Turning on radio... mFmReceiver = " + mFmReceiver + " ; Softmute state:"
                + FmConstants.FM_SOFTMUTE_FEATURE_ENABLED);
        if (mFmReceiver == null) {
            Global.debug( "Invalid FM Receiver Proxy!!!!");
            return;
        }
        if (FmConstants.FM_SOFTMUTE_FEATURE_ENABLED){
        	status = mFmReceiver.turnOnRadio(FmProxy.FUNC_REGION_NA | FmProxy.FUNC_RBDS
                    | FmProxy.FUNC_AF | FmProxy.FUNC_SOFTMUTE, getPackageName());
        }
        else{
            status = mFmReceiver.turnOnRadio(FmProxy.FUNC_REGION_NA | FmProxy.FUNC_RBDS
                    | FmProxy.FUNC_AF, getPackageName());
        }
        Global.debug( "\r\n [开机] Turn on radio status = " + status);
        if (status == FmProxy.STATUS_OK) {
            // powerupComplete();
            // As turnOnRadio an asynchronous call, the callbacks will
            // initialize the frequencies accordingly.
            // Update only the GUI

        	Message msg = Message.obtain();
            msg.what = Global.GUI_UPDATE_MSG_FREQ_STATUS;
            msg.arg1 = mPendingFrequency;
            msg.arg2 = 1;
            //viewUpdateHandler.sendMessage(msg);
            
        } else {
            /* Add recovery code here if startup fails. */
            String error = getString(R.string.error_failed_powerup) + "\nStatus = " + status;
            Global.debug( error);
            displayErrorMessageAndExit(error);
        }
    }

    /**
     * This function is called to initialize pending operations and activate the
     * FM Radio HW on startup.
     */
	private boolean powerDownSequence() {
     
        Global.debug( "powerDownSequence()");
      

        /* Set pending updates to trigger on response from startup. */
        // enabledUpdatePending = false;
        shutdownPending = true;
        audioModeUpdatePending = false;
        nflEstimateUpdatePending = false;
        frequencyUpdatePending = false;
        scanStepUpdatePending = false;
//        rdsModeUpdatePending = false;
        livePollingUpdatePending = false;
        stationSearchUpdatePending = false;
        fmVolumeUpdatepending = false;
        fmSetSnrThresholdPending = false;

        /* Initialize the radio. This can give us GUI initialization info. */
        if (mFmReceiver != null) {
            mFmReceiver.setAudioPath(FmProxy.AUDIO_PATH_NONE);
            int status = mFmReceiver.turnOffRadio();
            if (status != FmProxy.STATUS_OK) {
                shutdownPending = false;
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setMessage(getString(R.string.error_failed_shutdown) + "\nStatus = "
                        + status);
                alertDialog.setButton(getString(android.R.string.ok),
                        (DialogInterface.OnClickListener) null);
                alertDialog.show();
                return false; // failure
            }
        }
        if (mPendingFrequency != 0) { // save last frequency
//            Editor e = mSharedPrefs.edit();
//            e.putInt(lastFreqPreferenceKey, mPendingFrequency);
//            e.apply();
        }
        return true; // success
    }
	
	// 开机完成
/*	
    private void powerupComplete() {
        Global.debug("powerupcomplete  ----------");
       // mPendingFrequency = mSharedPrefs.getInt(lastFreqPreferenceKey, DEFAULT_FREQUENCY);
        mPendingFrequency = Global.DEFAULT_FREQUENCY;
        updateFrequency(mPendingFrequency);
    }
 */   
    // 按键抬起消息
    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
   //  Global.debug("onKeyUp =====================================");

     	
	   if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){  // 右键处理  频道+ 0.1
		   if((gheadin == false)||(mSearchFlag == true) || (mMenuList.size() <= 0))  // 正在搜台时禁止按键
	     	{
	     		return true;	
	     	}
	    	int frequency = mPendingFrequency;
	    	if(frequency >= mMaxFreq){
	    		updateFrequency(mMinFreq);
	    	}
	    	else{
	    		updateFrequency(frequency + mFrequencyStep);
	    	}

	    	return true;
	    }
	    else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){  // 右键处理 // 右键处理  频道- 0.1

	    	if((gheadin == false)||(mSearchFlag == true) ||(mMenuList.size() <= 0) )  // 正在搜台时禁止按键
	     	{
	     		return true;	
	     	}
	    	int frequency = mPendingFrequency;
	    	if(frequency <= mMinFreq){
	    		updateFrequency(mMaxFreq);
	    	}
	    	else{
	    		updateFrequency(frequency - mFrequencyStep);
	    	}
		
	    	return true;
	    }
	    else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){  // 上键处理  搜索上一台
	    	if((gheadin == false)||(mSearchFlag == true) || (mMenuList.size() <= 0))  // 正在搜台时禁止按键
	     	{
	     		return true;	
	     	}
	    	
	    	playThisRadio();
	    	
	    }
	    else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){  // // 下键处理  搜索下一台
	    	if((gheadin == false)||(mSearchFlag == true) || (mMenuList.size() <= 0))  // 正在搜台时禁止按键
	     	{
	     		return true;	
	     	}
	    	playThisRadio();
	    }
	    else if(keyCode == KeyEvent.KEYCODE_MENU){
	    	if(mSearchFlag == true || (gheadin == false))  // 正在搜台时禁止按键
	     	{
	     		return true;	
	     	}
	    	updateMuted(true);   // 静音
	    	Intent intent = new Intent();
	    	Global.debug("MainActivity ==1==3333= \r\n");
	    	//Bundle bundle = intent.getExtras();	//获取 Bundle
	    	intent.putExtra("CHANEL", mPendingFrequency);
	    	//bundle.putInt("CHANEL", mPendingFrequency);  // 传送频道
			Global.debug("MainActivity ====3333= \r\n");
			intent.setClass(this, FmRadioSettings.class);

			// 如果希望启动另一个Activity，并且希望有返回值，则需要使用startActivityForResult这个方法，
			// 第一个参数是Intent对象，第二个参数是一个requestCode值，如果有多个按钮都要启动Activity，则requestCode标志着每个按钮所启动的Activity
			startActivityForResult(intent, Global.MENU_FLAG);
			
			return true;
	   }
	   else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER||
	    		keyCode == KeyEvent.KEYCODE_ENTER){  // 上键处理  搜索上一台
		   Global.debug("\r\n[FmRadio]-->[onKeyUp]--->gheadin == "+ gheadin );
		   Global.debug("\r\n[FmRadio]-->[onKeyUp]--->mSearchFlag == "+ mSearchFlag );
		   Global.debug("\r\n[FmRadio]-->[onKeyUp]--->mMenuList.size() == "+ mMenuList.size() );
		   if((mSearchFlag == true) || (gheadin == false) || (mMenuList.size() <= 0))  // 正在搜台时禁止按键
		   {
			   return true;	
	        }
	    	
		   	if(mPendingMute == true){  //
		   		
		   		PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.play_start));
			   	mPromptDialog.show();
			   	mPromptDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根
						updateMuted(!mPendingMute);
					}
				});
		   		
		   	}
		   	else{
		   		updateMuted(!mPendingMute);
		   		PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.play_pause));
			   	mPromptDialog.show();
			   	mPromptDialog.setPromptListener(new PromptListener() {
					
					@Override
					public void onComplete() {
						// TODO 自动生成的方法存根
						
					}
				});
		   	}

	    	return true;
	   }
	   else if(keyCode == KeyEvent.KEYCODE_BACK){  // 返回
		   if(true == mSearchFlag){
			   mSearchFlag = false;
			   mMenuView.setSelectItem(0);
				
			   int i_freq = radioFreqList.get(getSelectItem());//Integer.valueOf(chanel_di.get(getSelectItem()));
				
			   updateFrequency(i_freq);
			   return true;
		   }
	   }
	   else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
       	//super.onKeyDown(keyCode, event);
       	 if(true == mSearchFlag){
       		 return true;
       	 }
	   }
	   
        return super.onKeyUp(keyCode, event);
    }

	// add by zhd@2016-10-27
	private void playThisRadio() {
		try {
			int select = getSelectItem();
			int freq = radioFreqList.get(select);
			SharedPrefUtils.setSharedPrefInt(this,Global.FM_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.FM_SELECT, freq );
			updateMuted(true);
			updateFrequency(freq);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// enter 界面返回结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Global.debug("\r\nonActivityResult ==================111 requestCode ="+requestCode +" resultCode ==" + resultCode);
		updateMuted(false);   // 静音
		if (requestCode != resultCode || null == data) { // 在子菜单中回传的标志
			Global.debug("onActivityResult === error =");

			super.onResume();
			return;
		}
		
		int selectItem = 0; // 先获取子菜单上次的设置值
		selectItem = data.getIntExtra("selectItem", selectItem);
		// String selectStr = data.getStringExtra("selectStr");
		Global.debug("onActivityResult === selectItem =" + selectItem);
		if (selectItem == Global.SEARCH_ALL_ID) {  // 全部搜索
			
			chanel_str.clear();
			//chanel_di.clear();
			radioFreqList.clear();
			
			mMenuList = chanel_str;
			
			chanel_num = 0;
			mSearchFlag = true;   // 开始搜索
			
			delAllChanel();
			
			try {
				updateFrequency(mMinFreq);  // 设置最小频率
			} catch (Exception e) {
				// TODO: handle exception
			}
		
			onResume();
		} else if (selectItem == Global.AUDIO_APEAK_ID) {  //  声道切换
			
			if(speakerFlag == true){  // 耳机参入
				setWiredDeviceConnectionState(false);
			}
			else{   // 外放
				setWiredDeviceConnectionState(true);
			}
			super.onResume();
		} else if (selectItem == Global.SAVE_CHANEL_ID) {  // 保存频道
			chanel_str.clear();
			//chanel_di.clear();
			radioFreqList.clear();
			
			 ArrayList<String> mTmpList = new ArrayList<String>();
		        
		     mTmpList = getChanelData();
		     
		     for(int i = 0; i < mTmpList.size(); i++){
        		String s = String.format("%.1f", Integer.valueOf(mTmpList.get(i)) / Global.fmScale);
        		chanel_str.add(s + getResources().getString(R.string.mhz));
        		
        		radioFreqList.add(Integer.valueOf(mTmpList.get(i)));
        		chanel_num++;
	        }
        	
        	mMenuList= chanel_str;
			setListData(chanel_str);
			mMenuView.setSelectItem(radioFreqList.indexOf(mFrequency));
			super.onResume();
		} 
		else if (selectItem == Global.DEL_CHANEL_ID) {  // 删除频道
			int selectID = getSelectItem(); // 获取反显
					
			chanel_str.clear();
			radioFreqList.clear();
			
			 ArrayList<String> mTmpList = new ArrayList<String>();
		     mTmpList = getChanelData();
		     
		     if(null == mTmpList){
 	        	mRadioFreq = Global.DEFAULT_FREQUENCY;
 		        String s = String.format("%.1f", mRadioFreq / Global.fmScale);
 		        mTitle =  s + getResources().getString(R.string.mhz);//"MHz";
 		       
 		        chanel_str.add(mTitle);
 		        chanel_num++;
 		        mMenuList = chanel_str;
 		        radioFreqList.add(mRadioFreq);
 	        	setListData(mMenuList);
 	        }
			else{
			     for(int i = 0; i < mTmpList.size(); i++){
					String s = String.format("%.1f", Integer.valueOf(mTmpList.get(i)) / Global.fmScale);
					chanel_str.add(s + getResources().getString(R.string.mhz));
					radioFreqList.add(Integer.valueOf(mTmpList.get(i)));
					chanel_num++;
			     }
				
			     mMenuList= chanel_str;
			     setListData(chanel_str);
			     mMenuView.setSelectItem(radioFreqList.indexOf(mFrequency));
			}
			// 获取 保存的频道并设置反显
	        if(selectID > (mMenuList.size()-1)){
	        	selectID = 0;
	        }
	        if(selectID < 0){
	        	selectID = 0;
	        }
	        selectItem = selectID;
	        mMenuView.setSelectItem(selectID);
	        mTitle = chanel_str.get(selectID);
        	setTitle(mTitle);
        	
	     	int freq = radioFreqList.get(selectID);
	     	SharedPrefUtils.setSharedPrefInt(this,Global.FM_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.FM_SELECT, freq );
	     	updateFrequency(freq);

			onResume();
		}
		else if(selectItem == Global.DELALL_CHANEL_ID){
			chanel_str.clear();
			radioFreqList.clear();
			
        	mRadioFreq = Global.DEFAULT_FREQUENCY;
	        String s = String.format("%.1f", mRadioFreq / Global.fmScale);
	        mTitle =  s + getResources().getString(R.string.mhz);//"MHz";
	       
	        chanel_str.add(mTitle);
	        chanel_num++;

	        mMenuList = chanel_str;
	        radioFreqList.add(mRadioFreq);
	        
        	setListData(mMenuList);	       
	        mMenuView.setSelectItem(0);

        	setTitle(mTitle);
	     	int freq = radioFreqList.get(0);
	     	
	     	SharedPrefUtils.setSharedPrefInt(this,Global.FM_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.FM_SELECT, freq );
	     	updateFrequency(freq);

			onResume();
		}
		else if (selectItem == Global.RECORD_CHANEL_ID) {  // 内录
			startRecord();
			super.onResume();
		}
	}

	private void startRecord() {
		// TODO 自动生成的方法存根
		Intent intent = new Intent();
		//String packageName = "com.sunteam.calendar";
		String packageName = "com.sunteam.recorder";
		String className = "com.sunteam.recorder.activity.RecordActivity";
		String path = Global.FM_PATH; //Environment.getExternalStorageDirectory().getAbsolutePath() + "/tixing";
		
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = "FM" + sdf.format(dt) + ".wav";
		
		
		intent.putExtra("callerId", 4); // 0: 默认由录音机调用; 1: 热键进入录音; 2: 万年历中提醒录音;
										// 3: 语音备忘录音; 后两者在退出时返回到调用者!
		intent.putExtra("path", path); // 文件路径
		intent.putExtra("fileName", fileName); // 文件名
		intent.setClassName(packageName, className);

		startActivityForResult(intent , 0);  // 设置标志
	}
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    //    Global.debug("onKeyDown =====================================");

        
        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || 
        	keyCode == KeyEvent.KEYCODE_DPAD_LEFT || 
        	keyCode == KeyEvent.KEYCODE_MENU){
           if(gheadin == false){
        	  // TtsUtils.getInstance().speak(getResources().getString(R.string.hearphone_notin));
        	   PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.hearphone_notin));
        	   mPromptDialog.show();
        	   return true;
           }
         
           if((mSearchFlag == true) || (mMenuList.size() <= 0))  // 正在搜台时禁止按键
 		   {
        	   return true;	
 	       }
           return true;
	    }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
        	//super.onKeyDown(keyCode, event);
        	 if(true == mSearchFlag){
        		 return true;
        	 }
        	mcur_vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        	if(mcur_vol > 0){
        		mcur_vol --;
        	}
        	updateFMVolume(mcur_vol);
        	
        	return super.onKeyDown(keyCode, event);
        }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
        	//super.onKeyDown(keyCode, event);
        	mcur_vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        	if(mcur_vol < mMax_vol){
        		mcur_vol ++;
        	}
        	updateFMVolume(mcur_vol);
        	
        	return super.onKeyDown(keyCode, event);
        }
        else if(keyCode == KeyEvent.KEYCODE_DPAD_UP || 
            	keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
        	
			if(gheadin == false){
			   //TtsUtils.getInstance().speak(getResources().getString(R.string.hearphone_notin));
			   PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.hearphone_notin));
			  	mPromptDialog.show();
				return true;
			}
            Global.debug("\r\n [FmRadio]  [onKeyDown] mMenuList.size() =="+ mMenuList.size()); 	
		   if((mSearchFlag == true) || (mMenuList.size() <= 0))  // 正在搜台时禁止按键
		   {
			   return true;	
		   }
        
   	    }
	   	else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER||
	    		keyCode == KeyEvent.KEYCODE_ENTER){
	   	
	   		Global.debug("\r\n[FmRadio]-->[onKeyDown]--->gheadin == "+ gheadin );
	   		Global.debug("\r\n[FmRadio]-->[onKeyDown]--->mSearchFlag == "+ mSearchFlag );
	   		Global.debug("\r\n[FmRadio]-->[onKeyDown]--->mMenuList.size() == "+ mMenuList.size() );
		   
	   		if(gheadin == false){
	        //   TtsUtils.getInstance().speak(getResources().getString(R.string.hearphone_notin));
	           PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.hearphone_notin));
        	   mPromptDialog.show();
	           return true;
	        }
	   		
	   		if((mSearchFlag == true) || (mMenuList.size() <= 0))  // 正在搜台时禁止按键
 		    {
        	   return true;	
 	        }
	   		return true;
	   	}
   	   return super.onKeyDown(keyCode, event);
	}
    /**
     * Request new scan step operation and pend if necessary.
     * 
     * @param sp
     *            the shared preferences reference to get scan step from.
     */
    private void updateScanStep(SharedPreferences sp) {
      
         Global.debug( "updateScanStep()");
       
        /* Extract preferences and request these settings. */
      //  mPendingScanStep = Integer.parseInt(mSharedPrefs.getString(FmRadioSettings.FM_PREF_SCAN_STEP, "0"));
        mPendingScanStep = FmProxy.FREQ_STEP_100KHZ;
        
       Global.debug( "Sending scan step (" + Integer.toString(mPendingScanStep) + ")");
      

        if (null != mFmReceiver) {
            scanStepUpdatePending = (FmProxy.STATUS_OK != mFmReceiver.setStepSize(mPendingScanStep));
            /*
             * If this succeeds, start using that scan step in manual step
             * updates.
             */
            if (!scanStepUpdatePending) {
                mFrequencyStep = (0 == mPendingScanStep) ? 10 : 5;
             //   mView.setFrequencyStep(mFrequencyStep);
            }
        }
    }

    /**
     * Request new FM volume operation and pend if necessary.
     * 
     * @param sp
     *            the shared preferences reference to FM volume text editor
     *            from.
     */
    private void updateFMVolume(int volume) {
        // CK - Send the command only of the Service is not busy
    	int vol_bk = volume;
    	Global.debug("\r\n[FmRadio]-->[updateFMVolume] vol_bk === " + vol_bk);
    	vol_bk = volume* (Global.VOL_MAX_FM/mMax_vol);  // 收音机最大音量是 255 系统最大音量是 mMax_vol
    	//Global.debug("\r\n vol_bk === " + vol_bk);
    	Global.debug("\r\n[FmRadio]-->[updateFMVolume] vol_bk === " + vol_bk);
      //  Global.debug( "updateFMVolume()");
        if (null != mFmReceiver) {
            fmVolumeUpdatepending = (FmProxy.STATUS_OK != mFmReceiver.setFMVolume(vol_bk));
        }
    }

    /**
     * Request new audio mode operation and pend if necessary.
     * 
     * @param sp
     *            the shared preferences reference to get audio mode from.
     */
    private void updateAudioMode(SharedPreferences sp) {
        
        Global.debug( "updateAudioMode()");
        
        /* Extract preferences and request these settings. */
      //  mPendingAudioMode = Integer.parseInt(mSharedPrefs.getString(FmRadioSettings.FM_PREF_AUDIO_MODE, "0"));
       
       Global.debug( "Sending audio mode (" + Integer.toString(mPendingAudioMode) + ")");
       
        if (null != mFmReceiver) {
            audioModeUpdatePending = (FmProxy.STATUS_OK != mFmReceiver
                    .setAudioMode(mPendingAudioMode));
        }
    }

    /**
     * Request new audio path operation and pend if necessary.
     * 更新 状态
     * @param sp
     *            the shared preferences reference to get audio path from.
     */
    private void updateAudioPath(int audioPath) {

        /* Extract preferences and request these settings. */
        mPendingAudioPath = audioPath;
       
            Global.debug( "Sending audio path (" + Integer.toString(mPendingAudioPath) + ")");
       
        if (null != mFmReceiver) {
            audioPathUpdatePending = (FmProxy.STATUS_OK != mFmReceiver
                    .setAudioPath(mPendingAudioPath));
            Global.debug("audioPathUpdatePending == " + audioPathUpdatePending);
        }
    }

    /**
     * Request frequency tuning operation and pend if necessary.
     * 更新频率
     * @param freq
     *            the frequency to tune to.
     */
    private void updateFrequency(int freq) {

        Global.debug("\r\n [updateFrequency]   freq == "+ freq);
        /* Extract pending data and request these settings. */
        mPendingFrequency = freq;
         
        Global.debug("\r\n [updateFrequency] ==mFmReceiver == " + mFmReceiver + " freq == "+ freq);
         
        if (null != mFmReceiver) {
            frequencyUpdatePending = (FmProxy.STATUS_OK != mFmReceiver.tuneRadio(mPendingFrequency));
        }
        else{
        	Global.debug("\r\n mFmReceiver === error");
        }
    }

    /**
     * Request mute operation and pend if necessary.
     * 静音 
     * @param muted
     *            true if muting requested, false otherwise.
     */
    private void updateMuted(boolean muted) {
       
            Global.debug( "\r\n [FmRadio]       updateMuted() = " + muted);
       
        /* Extract pending data and request these settings. */
        mPendingMute = muted;
        
        Global.debug( "Sending muted (" + (mPendingMute ? "TRUE" : "FALSE") + ")");
       
        if (mFmReceiver != null) {
            muteUpdatePending = (mFmReceiver.muteAudio(mPendingMute) != FmProxy.STATUS_OK);
        }
    }

    /**
     * Request NFL estimate operation and pend if necessary.
     * 
     * @param sp
     *            the shared preferences reference to get NFL mode from.
     */
    private void updateNflEstimate(SharedPreferences sp) {
       
            Global.debug( "updateNflEstimate()");
        

        /* Extract preferences and request these settings. */
        //mPendingNflEstimate = Integer.parseInt(mSharedPrefs.getString(FmRadioSettings.FM_PREF_NFL_MODE, "1"));
        
            Global.debug( "Sending NFL mode (" + Integer.toString(mPendingNflEstimate) + ")");
        
        if (null != mFmReceiver) {
            nflEstimateUpdatePending = (FmProxy.STATUS_OK != mFmReceiver
                    .estimateNoiseFloorLevel(mPendingNflEstimate));
        }
    }

    /**
     * Request station search operation and pend if necessary.
     * 搜索电台 参数 向上或向下搜索
     * @param direction
     *            the search direction can be up or down.
     */
    private void updateStationSearch(int direction) {
        int endFrequency = mPendingFrequency;

       
        Global.debug("updateStationSearch()  ==========");
   
        /* Extract pending data and request these settings. */
        mPendingSearchDirection = direction;
      
     //   Global.debug( "Sending search direction (" + Integer.toString(mPendingSearchDirection)+ ")");
        
       // Global.debug( "mFmReceiver    ==== "+ mFmReceiver);
        if (mFmReceiver != null) {
       // 	 Global.debug( "FmConstants.FM_COMBO_SEARCH_ENABLED    "+FmConstants.FM_COMBO_SEARCH_ENABLED);
            if (FmConstants.FM_COMBO_SEARCH_ENABLED) {
                if ((mPendingSearchDirection & FmProxy.SCAN_MODE_UP) == FmProxy.SCAN_MODE_UP) {
               // 	Global.debug( "SCAN_MODE_UP    "+ FmProxy.SCAN_MODE_UP);
                    /* Increase the current listening frequency by one step. */
                    if ((mMinFreq <= mPendingFrequency) && (mPendingFrequency <= mMaxFreq)) {
                        mPendingFrequency = mPendingFrequency + mFrequencyStep;
                        if (mPendingFrequency > mMaxFreq)
                            mPendingFrequency = mMinFreq;
                    } else {
                        mPendingFrequency = mMinFreq;
                        endFrequency = mMaxFreq;
                    }
                } else {
                    /* Decrease the current listening frequency by one step. */
                	Global.debug( "SCAN_MODE_DOWN    "+ FmProxy.SCAN_MODE_DOWN);
                    if ((mMinFreq <= mPendingFrequency) && (mPendingFrequency <= mMaxFreq)) {
                        mPendingFrequency = mPendingFrequency - mFrequencyStep;
                        if (mPendingFrequency < mMinFreq)
                            mPendingFrequency = mMaxFreq;
                    } else {
                        mPendingFrequency = mMaxFreq;
                        endFrequency = mMinFreq;
                    }
                }

                Global.debug("[updateStationSearch]   mPendingFrequency    "+mPendingFrequency);
                
                stationSearchUpdatePending = mSeekInProgress = (FmProxy.STATUS_OK != mFmReceiver
                        .seekStationCombo(mPendingFrequency, endFrequency,
                                FmProxy.MIN_SIGNAL_STRENGTH_DEFAULT, mPendingSearchDirection,
                                mPendingScanMethod, FmConstants.COMBO_SEARCH_MULTI_CHANNEL_DEFAULT,
                                mPendingRdsType, FmProxy.RDS_COND_PTY_VAL));
                
                Global.debug("[updateStationSearch]mSeekInProgress ===   "+mSeekInProgress);
            } else {
                stationSearchUpdatePending = mSeekInProgress = (FmProxy.STATUS_OK != mFmReceiver
                        .seekStation(mPendingSearchDirection, FmProxy.MIN_SIGNAL_STRENGTH_DEFAULT));
            }
            
 //           if (mSeekInProgress)
 //               mView.setSeekStatus(true, false); // no new frequency, just turn
                                                  // on the indicator
        }
    }

    /**
     * Update the live polling settings from preferences and pend if necessary.
     *
     * @param sp
     *            the shared preferences reference to get settings from.
     */
    private void updateLivePolling(SharedPreferences sp) {
       
            Global.debug( "updateLivePolling()");
        

        /* Extract preferences and request these settings. */
//        mPendingLivePoll = sp.getBoolean(FmRadioSettings.FM_PREF_LIVE_POLLING, false);
//        mPendingLivePollinterval = Integer.parseInt(sp.getString(FmRadioSettings.FM_PREF_LIVE_POLL_INT, "2000"));
        
            Global.debug( "Sending live poll (" + (mPendingLivePoll ? "TRUE" : "FALSE") + ")");
            Global.debug( "Sending live poll interval (" + Integer.toString(mPendingLivePollinterval)
                    + ")");
        

        if (null != mFmReceiver) {
            livePollingUpdatePending = (FmProxy.STATUS_OK != mFmReceiver.setLiveAudioPolling(
                    mPendingLivePoll, mPendingLivePollinterval));
        }
    }

    /**
     * Update the SNR Threshold from preferences and pend if necessary.
     *
     * @param sp
     *            the shared preferences reference to get settings from.
     */
    private void updateSetSnrThreshold(SharedPreferences sp) {
        
            Global.debug( "updateSetSnrThreshold()");
        

        /* Extract preferences and request these settings. */
//        mPendingSnrThreshold = Integer.parseInt(sp.getString(FmRadioSettings.FM_PREF_SNR_THRESHOLD,String.valueOf(FmProxy.FM_MIN_SNR_THRESHOLD)));
        
            Global.debug( "Setting SNR Threshold(" + mPendingSnrThreshold + ")");
        

        if (null != mFmReceiver) {
            fmSetSnrThresholdPending = (FmProxy.STATUS_OK != mFmReceiver
                    .setSnrThreshold(mPendingSnrThreshold));
        }
    }
// 获取 更新 收音机制式
    private void updateMinMaxFrequencies() {
    	// 欧洲标志
        if ((mPendingRegion == FmProxy.FUNC_REGION_EUR)
                || (mPendingRegion == FmProxy.FUNC_REGION_NA)) {
            mMinFreq = FmConstants.MIN_FREQUENCY_US_EUROPE;
            mMaxFreq = FmConstants.MAX_FREQUENCY_US_EUROPE;
        } else if (mPendingRegion == FmProxy.FUNC_REGION_JP) {  // 日本
            mMinFreq = FmConstants.MIN_FREQUENCY_JAPAN;
            mMaxFreq = FmConstants.MAX_FREQUENCY_JAPAN;
        } else if (mPendingRegion == FmProxy.FUNC_REGION_JP_II) {
            mMinFreq = FmConstants.MIN_FREQUENCY_JAPAN_II;
            mMaxFreq = FmConstants.MAX_FREQUENCY_JAPAN_II;
        } else {
            // where are we?
            return;
        }
     //   mView.setMinMaxFrequencies(mMinFreq, mMaxFreq);
        Global.debug("[updateMinMaxFrequencies] mMinFreq ===" + mMinFreq+ " \r\nmMaxFreq   === "+mMaxFreq);
    }

    /**
     * Execute any pending commands. Only the latest command will be stored.
     */
    private void retryPendingCommands() {

//        Global.debug("[retryPendingCommands()] ================");
        /* Update event chain. */
        if (nflEstimateUpdatePending) {
//            updateNflEstimate(mSharedPrefs);
        } else if (audioModeUpdatePending) {
 //           updateAudioMode(mSharedPrefs);
        } else if (audioPathUpdatePending) {
           // updateAudioPath(mSharedPrefs);
        } else if (fmVolumeUpdatepending) {
            updateFMVolume(FmProxy.FM_VOLUME_MAX
                    * mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        } else if (frequencyUpdatePending) {
            updateFrequency(mPendingFrequency);
        } else if (muteUpdatePending) {
            updateMuted(mPendingMute);
        } else if (scanStepUpdatePending) {
//            updateScanStep(mSharedPrefs);
        } else if (stationSearchUpdatePending) {
            updateStationSearch(mPendingSearchDirection);
        } 
//        else if (rdsModeUpdatePending) {
////            updateRdsMode(mSharedPrefs);
//        } 
        else if (fmSetSnrThresholdPending) {
//            updateSetSnrThreshold(mSharedPrefs);
        } else {
            if (livePollingUpdatePending) {
//                updateLivePolling(mSharedPrefs);
            }
//            if (worldRegionUpdatePending) {
// //               updateWorldRegion(mSharedPrefs);
//            }
        }
        
        if(mSearchFlag == true)
        {
        	 // 接着搜索下一个电台
            Message msg = Message.obtain();
            msg.what = Global.GUI_GET_NEXT_CHANEL;
            msg.arg1 = chanel_num;
            viewUpdateHandler.sendMessage(msg);
        	onResume();
        	if(false){       	
        		if(mPendingFrequency < mMaxFreq){
        			updateFrequency(mPendingFrequency + mFrequencyStep);
        		}
        		else{
        			mSearchFlag = false;
        			mMenuView.setSelectItem(0);
            	
        			int i_freq = radioFreqList.get(getSelectItem());//Integer.valueOf(chanel_di.get(getSelectItem()));

        			updateFrequency(i_freq);
        			onResume();
            	
        		}
        	}
        }
    }

    @Override
	public void onProxyAvailable(Object ProxyObject) {
    	
    	Global.debug("onProxyAvailable ===============================================");
        //Global.debug( "onProxyAvailable bFinishCalled:" + bFinishCalled);
        if (mFmReceiver == null){
            mFmReceiver = (FmProxy) ProxyObject;
        }
        if (mFmReceiver == null) {
            String error = getString(R.string.error_unable_to_get_proxy);
            Global.debug( error);
            displayErrorMessageAndExit(error);
            return;
        }

        /* Initiate audio startup procedure. */
        if (null != mFmReceiver && mFmReceiverEventHandler == null) {
            mFmReceiverEventHandler = new FmReceiverEventHandler();
            mFmReceiver.registerEventHandler(mFmReceiverEventHandler);
            Global.debug("onProxyAvailable ============111===================================");
        }
        /* make sure we update frequency display and volume etc upon resume */
        if (!mFmReceiver.getRadioIsOn()) {
        	 Global.debug("onProxyAvailable ============222===================================");
            if (mFinish || bFinishCalled) {
                Global.debug( "Finish already initiated here. Hence exiting");
                return;
            }
          //  mPendingFrequency = mSharedPrefs.getInt(lastFreqPreferenceKey, DEFAULT_FREQUENCY);
            powerUpSequence();
        } else {
         //   powerupComplete();
            mFmReceiver.getStatus(); // is this even needed?
        }
    }

    /**
     * proxy is Unavailable. mostly likely because FM service crashed in this
     * case we need to force the call to onDestroy() to avoid sending any Fm
     * commands to the proxy.
     */
    @Override
	public void onProxyUnAvailable() {
        Global.debug("onProxyUnAvailable() bFinishCalled:"+ bFinishCalled);
        /* TODO: a nice restart might be better */
        displayErrorMessageAndExit("Unexpected FM radio proxy close. FmRadio is closed");
    }

	private void displayErrorMessageAndExit(String errorMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(errorMessage);
        alertDialog.setButton(getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                });
        alertDialog.show();
    }


    /**
     * This class ensures that only the UI thread access view core.
     */
    protected Handler viewUpdateHandler = new Handler() {

        /**
         * Internal helper function to update the GUI frequency display.
         *更新显示
         * @param freq
         *            the frequency (multiplied by 100) to cache and display.
         */
        private void updateFrequency(int freq, int isCompletedSeekInt) {
            
           // Global.debug("[updateFrequency]  freq ==" + freq + "  isCompletedSeekInt == "+ isCompletedSeekInt );
            if (freq < 0)
                return;

            boolean isCompletedSeek = (isCompletedSeekInt != 0);
            /*
             * only update GUI, setting of actual frequency is done outside of
             * this class
             */
            // fixme -- all viewUpdateHandler functions should ONLY update GUI
            mFrequency = freq;
            String Title = freq/100 + "." + freq%100/10+ getResources().getString(R.string.mhz);
            setTitle(Title);
       //     onResume();
            
            if (isCompletedSeek) {
               // mView.setFrequencyGraphics(freq);
                mPendingFrequency = mFrequency;
            }

        }
// 更新 GUI信息
        @Override
		public void handleMessage(Message msg) {

//            Global.debug("[handleMessage]   === " + msg);
            /*
             * Here it is safe to perform UI view access since this class is
             * running in UI context.
             */

            switch (msg.what) {
            case Global.GUI_UPDATE_MSG_SIGNAL_STATUS:
               // mView.setSignalStrength(msg.arg1);
                break;
            case Global.GUI_UPDATE_MSG_FREQ_STATUS:
                updateFrequency(msg.arg1, msg.arg2);
                break;
            case Global.GUI_UPDATE_MSG_MUTE_STATUS:
              //  mView.setMutedState(msg.arg1 == FmConstants.MUTE_STATE_MUTED, mInCall);
                break;
            case Global.GUI_UPDATE_MSG_RDS_STATUS:
              //  mView.setRdsState(msg.arg1);
                break;
            case Global.GUI_UPDATE_MSG_AF_STATUS:
              //  mView.setAfState(msg.arg1);
                break;
            case Global.GUI_UPDATE_MSG_RDS_DATA:
              //  mView.setRdsText(msg.arg1, msg.arg2, (String) msg.obj);
                break;
            // case GUI_UPDATE_MSG_DEBUG:
            // ((TextView)mView.findViewById(msg.arg1)).setText((String)msg.obj);
            // break;
            case Global.SIGNAL_CHECK_PENDING_EVENTS:
                retryPendingCommands();
                break;
                
            case Global.GUI_GET_NEXT_CHANEL:
            	updateStationSearch(FmProxy.SCAN_MODE_UP);
            	//updateFrequency(mPendingFrequency + mFrequencyStep);
            	//updateFrequency(mPendingFrequency + mFrequencyStep);
            	break;
            case Global.GUI_UPDTAE_CHANEL:
            	mMenuView.setSelectItem(0);
            	
    			int i_freq = radioFreqList.get(getSelectItem());//Integer.valueOf(chanel_di.get(getSelectItem()));

    			FmRadio.this.updateFrequency(i_freq);
    			onResume();
            	break;
            
            case Global.GUI_UPDTAE_LIST:
            	
            	
        		mMenuList = chanel_str;
        		setListData(mMenuList);
        		
        		mMenuView.setSelectItem(chanel_num-1);
        		
            	//mMenuView.setSelectItem();
            	
    			onResume();
            	break;
            	
            default:
                break;
            }
        }
    };

	

    /**
     * Internal class used to specify FM callback/callout events from the FM
     * Receiver Service subsystem.
     */
    protected class FmReceiverEventHandler implements IFmReceiverEventHandler {

        /**
         * Transfers execution of GUI function to the UI thread.
         * 
         * @param rssi
         *            the signal strength to display.
         */
        private void displayNewSignalStrength(int rssi) {
//            Global.debug( "displayNewSignalStrength  : " + rssi);
            /* Update signal strength icon. */
            Message msg = Message.obtain();
            msg.what = Global.GUI_UPDATE_MSG_SIGNAL_STATUS;
            msg.arg1 = rssi;
            viewUpdateHandler.sendMessage(msg);
        }

        private void displayNewRdsData(int rdsDataType, int rdsIndex, String rdsText) {
            Global.debug( "displayNewRdsData  : ");
            /* Update RDS texts. */
            Message msg = Message.obtain();
            msg.what = Global.GUI_UPDATE_MSG_RDS_DATA;
            msg.arg1 = rdsDataType;
            if (rdsDataType == FmConstants.RDS_ID_PTY_EVT) {
                if (rdsIndex < 0 || rdsIndex >= mRdsProgramTypes.length)
                    return; // invalid index
                msg.arg2 = rdsIndex;
                msg.obj = mRdsProgramTypes[rdsIndex];
            } else {
                msg.obj = rdsText;
            }
            viewUpdateHandler.sendMessage(msg);
        }

        /**
         * Transfers execution of GUI function to the UI thread.
         * 
         * @param rdsMode
         *            the RDS mode to display.
         */
        private void displayNewRdsState(int rdsMode) {
            Global.debug( "displayNewRdsState  : " + rdsMode);
            /* Update RDS state icon. */
            Message msg = Message.obtain();
            msg.what = Global.GUI_UPDATE_MSG_RDS_STATUS;
            msg.arg1 = rdsMode;
            viewUpdateHandler.sendMessage(msg);
        }

        /**
         * Transfers execution of GUI function to the UI thread.
         * 
         * @param afMode
         *            the AF mode to display.
         */
        private void displayNewAfState(int afMode) {
            Global.debug( "displayNewAfState  : ");
            /* Update AF state icon. */
            Message msg = Message.obtain();
            msg.what = Global.GUI_UPDATE_MSG_AF_STATUS;
            msg.arg1 = afMode;
            viewUpdateHandler.sendMessage(msg);
        }

        /**
         * Transfers execution of GUI function to the UI thread.
         * 显示 频率
         * @param freq
         *            the frequency (multiplied by 100) to cache and display.
         */
        private void displayNewFrequency(int freq, int isCompletedSeekInt) {
           // Global.debug( "displayNewFrequency  : " + freq);
           // Global.debug("[displayNewFrequency]   === freq ==" + freq);
            /* Update frequency data. */
            Message msg = Message.obtain();
            msg.what = Global.GUI_UPDATE_MSG_FREQ_STATUS;
            msg.arg1 = freq;
            msg.arg2 = isCompletedSeekInt; // nonzero if completed seek
            viewUpdateHandler.sendMessage(msg);
        }

        /**
         * Transfers execution of GUI function to the UI thread.
         * 显示静音状态
         * @param isMute
         *            TRUE if muted, FALSE if not.
         */
        private void displayNewMutedState(boolean isMute) {

//            Global.debug( "displayNewMutedState  : " + isMute);
            /* Update frequency data. */
            Message msg = Message.obtain();
            msg.what = Global.GUI_UPDATE_MSG_MUTE_STATUS;
            msg.arg1 = isMute ? FmConstants.MUTE_STATE_MUTED : FmConstants.MUTE_STATE_UNMUTED;
            viewUpdateHandler.sendMessage(msg);
        }
        // 音频模式
        @Override
		public void onAudioModeEvent(int audioMode) {
            
                Global.debug("onAudioModeEvent(" + audioMode + ")");
           
            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }
        
        @Override
		public void onAudioPathEvent(int audioPath) {
          
                Global.debug( "onAudioPathEvent(" + audioPath + ")");
            
            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }

		@Override
		public void onEstimateNoiseFloorLevelEvent(int nfl) {
          
                Global.debug( "onEstimateNoiseFloorLevelEvent(" + nfl + ")");
            
            /* Local cache only! Not currently used directly. */
 //           mNfl = nfl;
            /* Update GUI display variables. */
           // mView.LOW_SIGNAL_STRENGTH = nfl;
           // mView.MEDIUM_SIGNAL_STRENGTH = nfl - 15;
          //  mView.HIGH_SIGNAL_STRENGTH = nfl - 25;
            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }

        @Override
		public void onLiveAudioQualityEvent(int rssi, int snr) {
           
                Global.debug( "onLiveAudioQualityEvent(" + rssi + ", " + snr + " )");
            
            /* Update signal strength icon. */
            displayNewSignalStrength(rssi);

            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }

        @Override
		public void onRdsDataEvent(int rdsDataType, int rdsIndex, String rdsText) {
            
                Global.debug( "onRdsDataEvent(" + rdsDataType + ", " + rdsIndex + ")");
            

            /* Update GUI text. */
            displayNewRdsData(rdsDataType, rdsIndex, rdsText);

            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }

        @Override
		public void onRdsModeEvent(int rdsMode, int alternateFreqHopEnabled) {
            
                Global.debug( "onRdsModeEvent(" + rdsMode + ", " + alternateFreqHopEnabled + ")");
            

            /* Update signal strength icon. */
            displayNewRdsState(rdsMode);

            /* Update mute status. */
            displayNewAfState(alternateFreqHopEnabled);

            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }
        // 搜到电台  需要增加电台
        @Override
		public void onSeekCompleteEvent(int freq, int rssi, int snr, boolean seeksuccess) {
          
            Global.debug("[onSeekCompleteEvent]  (" + freq + ", " + rssi + ", " + snr + ", "+ seeksuccess + ")");
            
            mSeekInProgress = false;
            
            if(chanel_num == 0 && (mSearchFlag == true)){   // 开始搜索
            	chanel_frist = freq;
            }
            else{
            //int temp_freq = Integer.valueOf(chanel_di.get(0));
	            int temp_freq = chanel_frist;
	            if(freq <= temp_freq || true == getChanelIsHave(freq))
	            {
	            	mSearchFlag = false;
	            	Message msg = Message.obtain();
	                msg.what = Global.GUI_UPDTAE_CHANEL;
	                msg.arg1 = chanel_num;
	                viewUpdateHandler.sendMessage(msg);
        			
	            	return ;
	            }
            }
//            Global.debug("[onSeekCompleteEvent] chanel_frist ==" + chanel_frist +" freq == "+freq +"\r\n chanel_num === "+ chanel_num);
//            if((chanel_frist > freq) && (chanel_num > 0))
//            {
//            	Global.debug("[onSeekCompleteEvent] return  ==#############");
//            	return ;
//            }
            mFrequency = freq;
            
            if(mSearchFlag == true){   // 调节灵敏度
                String temp =freq/100 + "." + freq%100/10 + getResources().getString(R.string.mhz);
                
                	if(false == getChanelIsHave(freq)){
                		chanel_str.add(temp);
                		//chanel_di.add(""+freq);
                		radioFreqList.add(freq);
                		mMenuList = chanel_str;
                		setListData(mMenuList);
                		
                		//mMenuView.setSelectItem(chanel_num);
	    	           
	    	            chanel_num++;  // 搜到的电台数
	    	            
	    	            Global.debug("\r\nchanel_num == " + chanel_num);
	    	            Global.debug("\r\n freq == " + freq);
	    	            addChanel(freq);
	    	            
	    	            Message msg = Message.obtain();
		                msg.what = Global.GUI_UPDTAE_LIST;
		                msg.arg1 = chanel_num;
		                viewUpdateHandler.sendMessage(msg);
		                
	    	            //onResume();
                	}
    	           
            }
            Global.debug("[onSeekCompleteEvent] 111  ==#############");
            /* Update frequency display. */
            displayNewFrequency(freq, 1);
            /* Update signal strength icon. */
           // displayNewSignalStrength(rssi);   // 显示信号强度
   /*         String temp =freq/100 + "." + freq%100/10 + getResources().getString(R.string.mhz);
            
            
            if(false == getChanelIsHave(freq)){
	            chanel_str.add(temp);
	            chanel_di.add(""+ freq);
	           
	            setListData(ArrayUtils.strArray2ListString(chanel_str));
	           // mMenuView.setSelectItem(chanel_num);
	            
	            chanel_num++;  // 搜到的电台数
            }
            Global.debug("[onSeekCompleteEvent] == SIGNAL_CHECK_PENDING_EVENTS ==3==");*/
            
  
            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
            
            Global.debug("[onSeekCompleteEvent] == GUI_GET_NEXT_CHANEL ==8==");
            // 接着搜索下一个电台
//            Message msg1 = Message.obtain();
//            msg1.what = GUI_GET_NEXT_CHANEL;
//            msg1.arg1 = chanel_num;
//            viewUpdateHandler.sendMessage(msg1);
            
            // 接着搜索
           // updateStationSearch(FmProxy.SCAN_MODE_UP);
        }
	// 判断电台是否存在
	    private boolean getChanelIsHave(int freq) {
			// TODO 自动生成的方法存根
	    	
	    	int i = 0;
	    	int temp_freq = 0;
	    	//double num;
	    	for(i = 0; i < chanel_num; i++){
	    		temp_freq = 0;
	    		
	    		//num = Double.valueOf(chanel.get(i));
	    		
	    		temp_freq = radioFreqList.get(i);//Integer.valueOf(chanel_di.get(i));
	    		//Global.debug("[getChanelIsHave] *** temp_freq =" + temp_freq + " freq =="+ freq);
	    		if(freq == temp_freq){
	    			//Global.debug("[getChanelIsHave] ********* return true");
	    			return true;
	    		}
	    		
	    	}
	    	//Global.debug("[getChanelIsHave] ********* return false");
			return false;
		}
        // 状态更新成功
		@Override
		public void onStatusEvent(int freq, int rssi, int snr, boolean radioIsOn,
                int rdsProgramType, String rdsProgramService, String rdsRadioText,
                String rdsProgramTypeName, boolean isMute) {
            
           Global.debug("onStatusEvent(freq = " + freq + ", rssi = " + rssi + ", snr = " + snr + ", radioIsOn =" + radioIsOn
                        + ", rdsProgramType =" + rdsProgramType + ", rdsProgramService = " + rdsProgramService + ", rdsRadioText =" + rdsRadioText
                        + ", rdsProgramTypeName =" + rdsProgramTypeName + ", isMute = " + isMute + ")");
            

           if(gheadin  == false){  // 耳机拔出
        	   updateMuted(true);
           }
            if (mPowerOffRadio && !radioIsOn) {
                finish();
            }
            if(((rssi < 90) || snr > 5)/*(snr > 4)*/ && (mSearchFlag == true)){   // 调节灵敏度
                String temp =freq/100 + "." + freq%100/10 + getResources().getString(R.string.mhz);
                
                	if(false == getChanelIsHave(freq)){  // 判断是否存在
                		chanel_str.add(temp);
                		//chanel_di.add(""+freq);
                		radioFreqList.add(freq);
	    	            setListData(chanel_str);
	    	          //  mMenuView.setSelectItem(chanel_num);
	    	            
	    	            chanel_num++;  // 搜到的电台数
	    	            
	    	          //  chanel_frist = freq;
                	}
    	           // onResume();
            }
            /* Update frequency display. */
            if(freq < mMinFreq)
            {
            	Global.debug("\r\n[onStatusEvent] ===freq == "+freq + " mMinFreq ==" + mMinFreq);
            	//freq = Global.DEFAULT_FREQUENCY;
            	playThisRadio();
            	return ;
            }
            displayNewFrequency(freq, 0);

            /* Update signal strength icon. */
            displayNewSignalStrength(rssi);

            /* Update mute status. */
            displayNewMutedState(isMute);
            updateMuted(isMute);  // 禁止静音

            /* Update GUI with RDS material if available. */
            // mRadioView.setRdsData(rdsProgramService, rdsRadioText,
            // rdsProgramTypeName);

            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }

        @Override
		public void onWorldRegionEvent(int worldRegion) {
           
                Global.debug( "onWorldRegionEvent(" + worldRegion + ")");
           

            /* Local cache. */
            mWorldRegion = worldRegion;
            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }

        @Override
		public void onVolumeEvent(int status, int volume) {
           
                Global.debug( "onVolumeEvent(" + status + ", " + volume + ")");
           

            /* Check if any pending functions can be run now. */
            Message msg = Message.obtain();
            msg.what = Global.SIGNAL_CHECK_PENDING_EVENTS;
            viewUpdateHandler.sendMessage(msg);
        }
    }
/*
    // manages frequency wrap around and scan step 
    private void buttonSeekFrequencyUp() {
        Global.debug( "buttonSeekFrequencyUp: region " + mPendingRegion);

        mSeekInProgress = false;
        // Increase the current listening frequency by one step. 
        if (mPendingFrequency == 0) {
            updateFrequency(Global.DEFAULT_FREQUENCY);
        } else {
            if (mPendingFrequency < mMaxFreq) {
                if (mPendingFrequency % FmConstants.SCAN_STEP_100KHZ != 0)
				{   //mPendingFrequency = (int) (mPendingFrequency + FmConstants.SCAN_STEP_50KHZ);
					mPendingFrequency = mPendingFrequency + 1;
				}
                else
                {   //mPendingFrequency = (int) (mPendingFrequency + mFrequencyStep);
					mPendingFrequency = mPendingFrequency + 1;
				}	
            } else {
                mPendingFrequency = mMinFreq;
            }
            updateFrequency(mPendingFrequency);
        }
    }
*/
/*    // manages frequency wrap around and scan step 
    private void buttonSeekFrequencyDown() {
        Global.debug( "buttonSeekFrequencyDown region: " + mPendingRegion);

        mSeekInProgress = false;
        // Decrease the current listening frequency by one step. 
        if (mPendingFrequency == 0) {
            updateFrequency(Global.DEFAULT_FREQUENCY);
        } else {
            if (mPendingFrequency > mMinFreq) {
                if (mPendingFrequency % FmConstants.SCAN_STEP_100KHZ != 0)
				{   //mPendingFrequency = (int) (mPendingFrequency - FmConstants.SCAN_STEP_50KHZ);
					mPendingFrequency = mPendingFrequency - 1;
				}	
                else
                {     //mPendingFrequency = (int) (mPendingFrequency - mFrequencyStep);
					mPendingFrequency = mPendingFrequency - 1;
				}
            } else {
                mPendingFrequency = mMaxFreq;
            }
            updateFrequency(mPendingFrequency);
        }
    }*/

    /*public void handleButtonEvent(int buttonId, int event) {
         Perform the functionality linked to the activated GUI release. 
         For each button, perform the requested action. 
        switch (buttonId) {
        case FmConstants.BUTTON_POWER_OFF:
             Shutdown system. 
            if (mFmReceiver != null && mFmReceiver.getRadioIsOn() == true) {
                mPowerOffRadio = true;
                powerDownSequence();
            }
            break;

        case FmConstants.BUTTON_MUTE_ON:
            updateMuted(true);
            break;

        case FmConstants.BUTTON_MUTE_OFF:
            updateMuted(false);
            break;

        case FmConstants.BUTTON_TUNE_DOWN:
            
             * Scan downwards to next station. Let the FM server determine NFL
             * for us.
             
            updateStationSearch(FmProxy.SCAN_MODE_DOWN);
            break;

        case FmConstants.BUTTON_TUNE_UP:
            
             * Scan upwards to next station. Let the FM server determine NFL for
             * us.
             
            updateStationSearch(FmProxy.SCAN_MODE_UP);
            break;

        case FmConstants.BUTTON_SEEK_DOWN:
             Decrease the current listening frequency by one step. 
            buttonSeekFrequencyDown();
            break;

        case FmConstants.BUTTON_SEEK_UP:
             Increase the current listening frequency by one step. 
            buttonSeekFrequencyUp();
            break;

        case FmConstants.BUTTON_SETTINGS:
            // openOptionsMenu();
            Intent intent = new Intent();
            intent.setClass(FmRadio.this, FmRadioSettings.class);
            startActivity(intent);
            break;

        default:
            break;
        }
    }

*/

    @Override
	public void setChannel(int position) {
 //       mChannels[position] = mPendingFrequency;
 //       Editor e = mSharedPrefs.edit();
  //      e.putInt(freqPreferenceKey + position, mPendingFrequency);
  //      e.apply();
    }

    @Override
	public void clearChannel(int position) {
 //       mChannels[position] = 0;
//        Editor e = mSharedPrefs.edit();
//        e.putInt(freqPreferenceKey + position, 0);
//        e.apply();
    }

    @Override
	public void selectChannel(int position) {
        mSeekInProgress = false;
 //       updateFrequency(mChannels[position]);
    }

    // callback from frequency slider
    @Override
	public void setFrequency(int freq) {
        mSeekInProgress = false;
        updateFrequency(freq);
    }
// 显示通知栏
	private void showNotification() {
        int icon = R.drawable.icon;
        String tickerText = String.format("FM Radio (%.02f MHz)",
                ((double) this.mPendingFrequency) / 100);
        long when = System.currentTimeMillis();
        if (mInCall)
            tickerText += " (in call - muted)";
        Notification notification = new Notification(icon, tickerText, when);

        Context context = getApplicationContext();
        String contentTitle = "FmRadio app";
        String contentText = String.format("%.02f MHz", ((double) this.mPendingFrequency) / 100);
        if (mInCall)
            contentTitle += " (in call - muted)";
        Intent notificationIntent = new Intent(this, FmRadio.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        final int PLAYING_ID = 1;
      //  mNotificationManager.notify(PLAYING_ID, notification);
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
		public void onCallStateChanged(int state, String incomingNumber) {
            updatePhoneState(state);
        }
    }

    private void updatePhoneState(int state) {
        String stateString = "N/A";
        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
            stateString = "Idle";
            if (mFmReceiver != null && mFmReceiver.getRadioIsOn()) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
					public void run() {
                        mFmReceiver.setAudioPath(mPendingAudioPath);
                    }
                }, 50);
            }
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            if (mFmReceiver != null)
                mFmReceiver.setAudioPath(FmProxy.AUDIO_PATH_NONE);
            stateString = "Off Hook";
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            if (mFmReceiver != null)
                mFmReceiver.setAudioPath(FmProxy.AUDIO_PATH_NONE);
            stateString = "Ringing";
            break;
        }
        Global.debug( "Call State : " + stateString);
    }
	@Override
	public int[] getChannels() {
		// TODO 自动生成的方法存根
		return null;
	}
	@Override
	public void handleButtonEvent(int buttonId, int event) {
		// TODO 自动生成的方法存根
		
	}
	// 删除所有数据
	public void delAllChanel(){
		GetDbInfo dbFmInfo = new GetDbInfo( this ); // 打开数据库
	
		dbFmInfo.detele(Global.FM_LIST);
		dbFmInfo.closeDb();
	}
	
	// 增加一条数据
		public boolean addChanel(int freq){
			GetDbInfo dbFmInfo = new GetDbInfo( this ); // 打开数据库
			FmInfo mFmInfo = new FmInfo();
			ArrayList<FmInfo> fmAlldata = new ArrayList<FmInfo>();
			
			fmAlldata = dbFmInfo.GetAllData(Global.FM_LIST);
			for(int i = 0; i < fmAlldata.size(); i++)
			{
				mFmInfo = fmAlldata.get(i);
				if(mFmInfo.chanel == freq){
					dbFmInfo.closeDb();
					return false;
				}
			}
			int maxid = dbFmInfo.getMaxId(Global.FM_LIST);
			
			mFmInfo._id = maxid + 1;
			mFmInfo.chanel = freq ;
			dbFmInfo.add(mFmInfo,Global.FM_LIST);
			dbFmInfo.closeDb();
			return true;
		}
	// 获取全部数据
	private ArrayList<String> getChanelData() {
	// TODO 自动生成的方法存根
		GetDbInfo dbFmInfo = new GetDbInfo( this ); // 打开数据库
		FmInfo musicinfo = new FmInfo();   //创建 结构体
		
		int maxId = dbFmInfo.getCount(Global.FM_LIST);  // 条数
		if(maxId > 0){  // 有记录
			ArrayList<FmInfo> fmAlldata = new ArrayList<FmInfo>();
			fmAlldata = dbFmInfo.GetAllData(Global.FM_LIST);
			dbFmInfo.closeDb();
			ArrayList<String> tmp = new ArrayList<String>();
			for(int i = 0 ; i < fmAlldata.size(); i++){
				tmp.add("" + fmAlldata.get(i).chanel);
			}
			return tmp;
		}
		else{
			return null;
			
		}
	}
	
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
/*	
	private void setWiredDeviceConnectionState1() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		//SunteamToast mSunteamToast = new SunteamToast(this);
		if (speakerFlag) {
			audioManager.setWiredDeviceConnectionState(8, 55, "h2w"); // close headset and open speak
			//mSunteamToast.show("设置外放");
		} else {
			audioManager.setWiredDeviceConnectionState(8, 66, "h2w"); // close speak
			//mSunteamToast.show("关闭外放");
		}
		speakerFlag = !speakerFlag;
	}
*/	
	private void setWiredDeviceConnectionState(Boolean flag) {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Class<AudioManager> c = AudioManager.class;
		Method method;
		try {
			method = c.getMethod("setWiredDeviceConnectionState", int.class, int.class, String.class);
			method.setAccessible(true);

			if (flag) {
				method.invoke(audioManager, 8, 55, "h2w"); // close headset and open speak
			} else {
				method.invoke(audioManager, 8, 66, "h2w"); // close speaker
			}
			speakerFlag = flag;
			//speakerFlag = !speakerFlag;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 切换发音
	private void setHearToSpeak(Boolean flag) {
		Global.debug("\r\n[setHearToSpeak]  ====  flag =" + flag);
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Class<AudioManager> c = AudioManager.class;
		Method method;
		
		if(true == flag){  //切换到外放	
			try {
				method = c.getMethod("setWiredDeviceConnectionState", int.class, int.class, String.class);
				method.setAccessible(true);
				method.invoke(audioManager, 8, 55, "h2w"); // close headset and open speak
			//	method.invoke(audioManager, 8, 0, "h2w"); // close headset and open speak
			} catch (NoSuchMethodException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}	
			
			//audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
			//Toast.makeText(this, "   切换外放  ", 0).show();
			//audioManager.setWiredDeviceConnectionState(8, 0, "h2w");
			
		}
		else{  // 切换到 耳机
			//audioManager.setMode(AudioManager.ROUTE_HEADSET);
			//audioManager.setSpeakerphoneOn(false);
			//Toast.makeText(this, "   切换耳机  ", 0).show();;
			audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		}
	}
	
	// 更新界面显示 
	private void updateshow(boolean flag) {
		// TODO 自动生成的方法存根
		Global.debug("\r\n 【updateshow()】 ==== mAudioManager.isWiredHeadsetOn() ==" + mAudioManager.isWiredHeadsetOn());
		radioFreqList.clear();
		chanel_str.clear();
        if(true == flag){  // 耳机插入
        	ArrayList<String> mTmpList = new ArrayList<String>();
        	//speakerFlag = true;
        	acquireWakeLock(this);// 禁止 休眠
        	setWiredDeviceConnectionState(false);  // 设置耳机输出
	        mTmpList = getChanelData();  // 获取保存频道
	        Global.debug("\r\n mTmpList ==== "+ mTmpList);
	        if(null == mTmpList){
	        	mRadioFreq = Global.DEFAULT_FREQUENCY;
		        String s = String.format("%.1f", mRadioFreq / Global.fmScale);
		        Global.debug("======="+s);
		        mTitle =  s + getResources().getString(R.string.mhz);//"MHz";
		       
		        chanel_str.add(mTitle);
		        chanel_num++;

		        Global.debug("==222====="+mMenuList);
		        mMenuList = chanel_str;
		        radioFreqList.add(mRadioFreq);
		        
	        	setListData(mMenuList);
	        }
	        else{
	        	for(int i = 0; i < mTmpList.size(); i++){
	        		String s = String.format("%.1f", Integer.valueOf(mTmpList.get(i)) / Global.fmScale);
	        		chanel_str.add(s + getResources().getString(R.string.mhz));
	        		radioFreqList.add(Integer.valueOf(mTmpList.get(i)));
	        		chanel_num++;
	        	}

	        	mMenuList= chanel_str;
	        	setListData(mMenuList);
	        //	onResume();
	        }
	        
	        // 获取 保存的频道并设置反显
	        int freq = 0; 
	        freq = SharedPrefUtils.getSharedPrefInt(this, Global.FM_CONFIG_FILE, Context.MODE_WORLD_READABLE, Global.FM_SELECT, freq);
	        Global.debug("\r\n ****  freq ==="+ freq);
	        int selectID = radioFreqList.indexOf(freq);  // 获取反显
	        Global.debug("\r\n ****  selectID ==="+ selectID);
	        if(selectID < 0){
	        	selectID = 0;
	        }
	        Global.debug("\r\n *222***  selectID ==="+ selectID);
	        selectItem = selectID;
	        mMenuView.setSelectItem(selectID);
	        mTitle = chanel_str.get(selectID);
	        Global.debug("\r\n *222***  mTitle ==="+ mTitle);
        	setTitle(mTitle);
        	
	     	freq = radioFreqList.get(selectID);
	     	Global.debug("\r\n **222**  freq ==="+ freq);
	     	updateFrequency(freq);
	     	//updateMuted(false);
	     	onResume();
        }
        else{  // 拔出耳机
        	//speakerFlag = false; 
        	releaseWakeLock();
        	setWiredDeviceConnectionState(true);
        	chanel_str.clear();
        	mMenuList = chanel_str;
        	Global.debug("\r\n 【updateshow()】 ==== chanel_str.size() ==" + chanel_str.size());
        	mTitle = getResources().getString(R.string.title_nohearphone);  // 耳机没有插入
        	setTitle(mTitle);
        	setListData(mMenuList);
        	updateMuted(true);
        }
        onResume();
	}
	
	// 耳机插拔消息 注册
	private void registerHeadsetPlugReceiver() {   
        IntentFilter intentFilter = new IntentFilter();   
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");   
        registerReceiver(headsetPlugReceiver, intentFilter);  
           
        // for bluetooth headset connection receiver 
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); 
        registerReceiver(headsetPlugReceiver, bluetoothFilter); 
    } 
	// 插拔消息  接收
    private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() { 
   
        @Override 
        public void onReceive(Context context, Intent intent) { 
               
            String action = intent.getAction(); 
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) { 
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter(); 
                if(BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) { 
                    //Bluetooth headset is now disconnected 
                   
                } 
            } else if ("android.intent.action.HEADSET_PLUG".equals(action)) { 
                if (intent.hasExtra("state")) { 
                    if (intent.getIntExtra("state", 0) == 0) { // 耳机拔出
                    	Global.debug("\r\n 耳机拔出======");
                    	
                    	updateshow(false);
                    	gheadin = false;
                    	updateMuted(true);
                    }
                    else if(intent.getIntExtra("state", 0) == 1){   // 耳机插入
                    	Global.debug("\r\n 耳机插入======");
                    	
                    	updateshow(true);
                    	gheadin = true;
                    	updateMuted(false);
                    }
                }
            } 
        } 
    }; 
    
    // 电池电量 获取
	public class BatteryBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
	           int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

	           int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
	           boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                       status == BatteryManager.BATTERY_STATUS_FULL;
	           if(level<10){
            	
        			PromptDialog mpDialog = new PromptDialog(FmRadio.this, getResources().getString(R.string.bat_lv0));
        			mpDialog.show();
        			mpDialog.setPromptListener(new PromptListener() {
						
						@Override
						public void onComplete() {
							mHandler.sendEmptyMessage(Global.MSG_BACK);	
						}
					});
				}else if(level<20&&!isCharging){
					//Global.showToast(PlayActivity.this, R.string.low_battery,null,-1);
				} 
			}
		}
	}
	// 电池电量获取消息 注册
	private void registerBatReceiver() {           
        batterReceiver =new BatteryBroadcastReciver();  
        IntentFilter intentFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  
        registerReceiver(batterReceiver, intentFilter);
    }
		
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_BACK){   // 音乐播放结束消息
				finish();
			}
			
			super.handleMessage(msg);
		}
	};	
}
