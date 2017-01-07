package com.sunteam.recorder.player;

import java.io.IOException;

import android.content.Context;
import android.gesture.GestureStore;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;

import com.iflytek.thridparty.r;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;

public class MyPlayer implements OnCompletionListener, OnErrorListener {
	
	public static final int IDLE_STATE = 0;
	public static final int RECORDING_STATE = 1;
	public static final int PLAYING_STATE = 2;
	public static final int PLAYING_PAUSED_STATE = 3;
	public static final int NO_ERROR = 0;
    public static final int STORAGE_ACCESS_ERROR = 1;
    public static final int INTERNAL_ERROR = 2;
    public static final int IN_CALL_RECORD_ERROR = 3;
    public static int mState = IDLE_STATE;
    private static MyPlayer mInstance = null;
    private MediaPlayer mPlayer;
    private static Handler mHandler;
    private static Context mcontext;
    private OnStateChangedListener mOnStateChangedListener = null;
    public interface OnStateChangedListener {
        public void onStateChanged(int state);

        public void onError(int error);
    }
  
	public MyPlayer() {
		
	}
	
	public synchronized static MyPlayer getInstance(Context context,Handler handler) {
		if (mInstance == null){
			mInstance = new MyPlayer();
		}
		mcontext = context;
		mHandler = handler;
		return mInstance;
	}
	/**
	 * 
	 * @param percentage
	 * @param mSampleFile
	 * @param keyType 是不是确认键
	 */
	public void startPlayback(float percentage,String mSampleFile,boolean keyType) {
		if (state() == PLAYING_PAUSED_STATE) {
			if(keyType){
				mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
				mPlayer.start();
				setState(PLAYING_STATE);
			}else{
				startPlay(percentage,mSampleFile);
			}		
		} else {
			startPlay(percentage,mSampleFile);          
		}
	}
	
	private void startPlay(float percentage,String mSampleFile){
		stopPlayback();			
		mPlayer = new MediaPlayer();
		setState(MyPlayer.IDLE_STATE);
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource(Global.storagePath+"/"+mSampleFile);	
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(this);
			mPlayer.prepare();
			mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
			TtsUtils.getInstance().stop();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			setError(INTERNAL_ERROR);
			mPlayer = null;
			return;
		} catch (IOException e) {
			setError(STORAGE_ACCESS_ERROR);
			mPlayer = null;
			return;
		}
		setState(PLAYING_STATE);  
	}
	/**
	 * 得到播放进度
	 * @return 已播放秒数
	 */
	public int progress() {
		if (mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE) {
            if (mPlayer != null) {
                return (int) (mPlayer.getCurrentPosition() / 1000);
            }
        }
        return 0;
    }
	/**
	 * 得到播放进度
	 * @return 已播放百分比 
	 */
	public float playProgress() {
		if(state() == IDLE_STATE){
			return 0.0f;
		}else if (mPlayer != null) {
            return ((float) mPlayer.getCurrentPosition()) / mPlayer.getDuration();
        }
        return 0.0f;
    }
	
	public int fileDuration(){
		if(mPlayer!=null){
			int len = mPlayer.getDuration()/1000;
			return len==0?1:len;
		}
		return 0;
	}
	
	public int endTime(){
		if(mPlayer!=null&&(mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE)){
			return mPlayer.getDuration() - mPlayer.getCurrentPosition();
		}
		return 0;
	}
	
	
	public void speed(){
		if(state() == MyPlayer.PLAYING_STATE){
		mPlayer.setOnCompletionListener(null);
		int pos = mPlayer.getCurrentPosition();
		pos += 10000;
		if(pos>=mPlayer.getDuration()+9000){
			stopPlayback();
			//Global.showToast(mcontext, R.string.speed2end, mHandler,2);
			PromptDialog mPromptDialog = new PromptDialog(mcontext, mcontext.getResources().getString(R.string.speed2end));
			mPromptDialog.show();
		}else if(pos>=mPlayer.getDuration()){
			mPlayer.seekTo(mPlayer.getDuration()-500);
			mPlayer.setOnCompletionListener(this);
		}else{
			mPlayer.seekTo(pos);
			mPlayer.setOnCompletionListener(this);
		}
		}
	}
	
	public void rewind(){
		if(state() == MyPlayer.PLAYING_STATE){
		int pos = mPlayer.getCurrentPosition();
		pos -= 10000;//每按一次键倒退10s
		if(pos<=-8000){
			//Global.showToast(mcontext, R.string.rewind2start, mHandler,0);
			PromptDialog mPromptDialog = new PromptDialog(mcontext, mcontext.getResources().getString(R.string.rewind2start));
			mPromptDialog.show();
			//stopPlayback();
			mPlayer.seekTo(1000);   // 快退不停止 一直播
		}else{
			mPlayer.seekTo(pos);
		}	
		}
	}
	
	
	public void pausePlayback() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.pause();
        setState(PLAYING_PAUSED_STATE);
    }

	public void stopPlayback() {
	    if (mPlayer == null) // we were not in playback
	        return;	
	    mPlayer.stop();
	    mPlayer.release();
	    mPlayer = null;
	    setState(MyPlayer.IDLE_STATE);
	}
	
	public int state() {
		return mState;
	}
	
	public void setState(int state) {
        if (state == mState)
            return;
        mState = state;
        signalStateChanged(mState);
    }
	
	public boolean onError(MediaPlayer mp, int what, int extra) {
		stopPlayback();
		setError(STORAGE_ACCESS_ERROR);
        return true;
	}

	public void onCompletion(MediaPlayer mp) {
		stopPlayback();
		goBackPlaylist();
	}

	private void goBackPlaylist(){
		mHandler.sendEmptyMessage(1);
	}
	
	 public void setError(int error) {
	        if (mOnStateChangedListener != null)
	            mOnStateChangedListener.onError(error);
	    }
	 
	 public void setOnStateChangedListener(OnStateChangedListener listener) {
	        mOnStateChangedListener = listener;
	    }
	 
	 private void signalStateChanged(int state) {
	        if (mOnStateChangedListener != null)
	            mOnStateChangedListener.onStateChanged(state);
	    }
}