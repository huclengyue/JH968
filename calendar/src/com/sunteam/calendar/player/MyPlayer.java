package com.sunteam.calendar.player;

import java.io.FileDescriptor;
import java.io.IOException;

import com.sunteam.calendar.constant.Global;
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
	 * @param keyType �ǲ���ȷ�ϼ�
	 */
	public void startPlayback(float percentage,String path,boolean keyType) {
		Global.debug("\r\n[startPlayback] path===== "+ path);
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
		Global.debug("\r\n[startPlay] path===== "+ path);
		stopPlayback();			
		mPlayer = new MediaPlayer();
		setState(MyPlayer.IDLE_STATE);
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource(path);	
			mPlayer.prepare();
			mPlayer.start();
//			Global.debug("\r\n startPlay (int) (percentage * mPlayer.getDuration()) == " + (int) (percentage * mPlayer.getDuration()));
//			mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));  // baocaho del mp2 文件播放时出现
//			mPlayer.seekTo(0);  // baocaho del mp2 文件播放时出现
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
                return mPlayer.getCurrentPosition() / 1000;
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
	// 获取结束时间
	public int endTime(){
		if(mPlayer!=null&&(mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE)){
			return mPlayer.getDuration() - mPlayer.getCurrentPosition();
		}
		return 0;
	}
	
	// 快件10S
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
	// 获取当前时间
	public int startTime(){
		if(mPlayer!=null&&(mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE)){
			return mPlayer.getCurrentPosition();
		}
		return 0;
	}
	
// 获取当前时间
	public int SeekToTime(int time){
		if(mPlayer!=null&&(mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE)){
			mPlayer.seekTo(time*1000);
			mPlayer.setOnCompletionListener(this);
		}
		return 0;
	}
	
	public void rewind(){
		if(state() == MyPlayer.PLAYING_STATE){
		int pos = mPlayer.getCurrentPosition();
		pos -= 10000;//快退 10s
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
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		stopPlayback();
		setError(STORAGE_ACCESS_ERROR);
        return true;
	}

	@Override
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
	 
	 
	 public void startPlayback2(float playProgress, FileDescriptor fileDescriptor, boolean keyType) {
			// TODO 自动生成的方法存根
			if (state() == PLAYING_PAUSED_STATE) {
				if(keyType){
					mPlayer.seekTo((int) (playProgress * mPlayer.getDuration()));
					mPlayer.start();
					setState(PLAYING_STATE);
				}else{
					startPlay2(playProgress,fileDescriptor);
				}		
			} else {
				startPlay2(playProgress,fileDescriptor);          
			}
		}

		private void startPlay2(float playProgress, FileDescriptor fileDescriptor) {
			// TODO 自动生成的方法存根
			stopPlayback();			
			mPlayer = new MediaPlayer();
			setState(MyPlayer.IDLE_STATE);
			try {
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mPlayer.setDataSource(fileDescriptor);
				
				mPlayer.setOnCompletionListener(this);
				mPlayer.setOnErrorListener(this);
				mPlayer.prepare();
				//mPlayer.seekTo((int) (playProgress * mPlayer.getDuration()));
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
}