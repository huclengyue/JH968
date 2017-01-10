package com.sunteam.player;

import java.io.IOException;

import com.sunteam.receiver.Alarmpublic;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;

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
    @SuppressWarnings("unused")
	private static Context mcontext;
    private OnStateChangedListener mOnStateChangedListener = null;
    public interface OnStateChangedListener {
        public void onStateChanged(int state);

        public void onError(int error);
    }
  
	public MyPlayer() {

	}
	
	/*public MyPlayer(Context context,Handler handler) {
		mcontext = context;
		mHandler = handler;
	}*/
	
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
	 * @param keyType
	 */
	public void startPlayback(float percentage,String path,boolean keyType) {
		if (state() == PLAYING_PAUSED_STATE) {
			if(keyType){
				mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
				mPlayer.start();
				setState(PLAYING_STATE);
			}else{
				startPlay(percentage,path);
			}		
		} else {
			startPlay(percentage,path);          
		}
	}
	// 开始播放
	private void startPlay(float percentage,String path){
		stopPlayback();			
		mPlayer = new MediaPlayer();
		setState(MyPlayer.IDLE_STATE);
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource(path);
			
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(this);
			mPlayer.prepare();
			mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
			//Global.getTts().stop();
			//TtsUtils.getInstance().stop();
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
	 * �õ����Ž���
	 * @return �Ѳ�������
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
	 * �õ����Ž���
	 * @return �Ѳ��Űٷֱ� 
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
		pos -= 10000;//ÿ��һ�μ�����10s
		if(pos<=-8000){
			//Global.showToast(mcontext, R.string.rewind2start, mHandler,0);
			stopPlayback();
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
		Alarmpublic.debug("[Myplayer]stopPlayback ==== mPlayer="+ mPlayer);
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

	public void startPlayback2(Context context, int id, boolean keyType) {
		// TODO 自动生成的方法存根
		Alarmpublic.debug("\r\n startPlayback2   ==========1111==");
		if (state() == PLAYING_PAUSED_STATE) {
			if(keyType){
				//mPlayer.seekTo((int) (playProgress * mPlayer.getDuration()));
				mPlayer.start();
				setState(PLAYING_STATE);
			}else{
				Alarmpublic.debug("\r\n startPlayback2   ==========2222==");
				startPlay2(context, id);
			}		
		} else {
			startPlay2(context, id);          
		}
	}

	private void startPlay2(Context context, int resid) {
		// TODO 自动生成的方法存根
		stopPlayback();		
		Alarmpublic.debug("\r\n startPlay2   ==========2222==");
		
		mPlayer = MediaPlayer.create(context, resid);
		mPlayer.start();
		Alarmpublic.debug("\r\n startPlay2   ==========3333==");
		setState(MyPlayer.IDLE_STATE);
		try {
			Alarmpublic.debug("\r\n startPlay2   ==========4444==");
		
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(this);
		//	mPlayer.prepare();
			Alarmpublic.debug("\r\n startPlay2   ==========777==");
		//	mPlayer.start();
			Alarmpublic.debug("\r\n startPlay2   ==========888==");
		} catch (IllegalArgumentException e) {
			setError(INTERNAL_ERROR);
			mPlayer = null;
			return;
		}
		setState(PLAYING_STATE);	
	}
}