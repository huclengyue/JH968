package com.sunteam.recorder;

import android.app.Application;

import com.sunteam.common.tts.TtsListener;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.Tools;

public class SunteamApplication extends Application implements TtsListener {
	
	@Override
	public void onCreate() {
		super.onCreate();
		TtsUtils.getInstance(this, this);
		Global.setApplication(this);
		
		Tools mTools = new Tools(this);
		Global.setBackgroundColor(mTools.getBackgroundColor());
		Global.setForegroundColor(mTools.getHighlightColor());
		Global.setApp_text_color(mTools.getFontColor());
		Global.setApp_text_size(mTools.getFontSize()-1);
		this.setTheme(R.style.BlueWhiteTheme);
	}

	@Override
	public void onInit(int status) {
		//if (status == TextToSpeech.SUCCESS) {
			//Toast.makeText(this, "Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
		TtsUtils.getInstance().speak(getResources().getString(R.string.recorder)+","+getResources().getString(R.string.record));
//		} else if (status == TextToSpeech.ERROR) {
//			//Toast.makeText(this, "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
//		}
	}

	@Override
	public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompleted(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSpeakBegin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSpeakPaused() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSpeakProgress(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSpeakResumed() {
		// TODO Auto-generated method stub
		
	}
}