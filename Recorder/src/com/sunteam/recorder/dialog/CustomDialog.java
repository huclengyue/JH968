package com.sunteam.recorder.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sunteam.common.tts.TtsCompletedListener;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.recorder.Global;
import com.sunteam.recorder.R;


public class CustomDialog extends Dialog{

    private Context context;
    private String title;
    private String confirmButtonText;
    private String cacelButtonText;
    private TextView tvConfirm;
    private TextView tvCancel;
    private Handler mHandler;
    private ClickListenerInterface clickListenerInterface;

    public interface ClickListenerInterface {

        public void doConfirm();

        public void doCancel();
    }
    
    public CustomDialog(Context context, String title, String confirmButtonText, String cacelButtonText,Handler handler) {
    	super(context, R.style.dialog);
    	this.context = context;
        this.title = title;
        this.confirmButtonText = confirmButtonText;
        this.cacelButtonText = cacelButtonText;
        this.mHandler = handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_dialog, null);
        view.setBackgroundColor(Global.getApp_text_color());
        setContentView(view);
        
        TextView tvTitle = (TextView) view.findViewById(R.id.title);
        tvConfirm = (TextView) view.findViewById(R.id.confirm);
        tvCancel = (TextView) view.findViewById(R.id.cancel);

        tvTitle.setText(title);
        tvConfirm.setText(confirmButtonText);
        tvCancel.setText(cacelButtonText);

        tvConfirm.setFocusableInTouchMode(true);
        tvCancel.setFocusableInTouchMode(true);
        tvConfirm.requestFocus();

        tvConfirm.setOnClickListener(new clickListener());
        tvCancel.setOnClickListener(new clickListener());
        
        tvConfirm.setOnFocusChangeListener(new FocusListener());
        tvCancel.setOnFocusChangeListener(new FocusListener());
        

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();// 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.9); 
        //lp.height = (int)(d.widthPixels*0.6);
        dialogWindow.setAttributes(lp);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    private class clickListener implements View.OnClickListener {
    	
        @Override
        public void onClick(View v) {
            int id = v.getId();
            /*switch (id) {
            case R.id.confirm:
            	if(!tvConfirm.isFocused()){
            		TtsUtils.getInstance().speak(context.getResources().getString(R.string.yes));
        			tvCancel.clearFocus();
                	tvConfirm.requestFocus();            		
            	}else{
            		clickListenerInterface.doConfirm();
            	}             
                break;
            case R.id.cancel:
            	if(!tvCancel.isFocused()){
            		TtsUtils.getInstance().speak(context.getResources().getString(R.string.no));
            		tvConfirm.clearFocus();
                	tvCancel.requestFocus();               		
            	}else{
            		clickListenerInterface.doCancel();
            	}               
                break;
            }*/
			if (R.id.confirm == id) {
				if (!tvConfirm.isFocused()) {
					TtsUtils.getInstance().speak(context.getResources().getString(R.string.yes));
					tvCancel.clearFocus();
					tvConfirm.requestFocus();
				} else {
					clickListenerInterface.doConfirm();
				}
			} else if (R.id.cancel == id) {
				if (!tvCancel.isFocused()) {
					TtsUtils.getInstance().speak(context.getResources().getString(R.string.no));
					tvConfirm.clearFocus();
					tvCancel.requestFocus();
				} else {
					clickListenerInterface.doCancel();
				}
			}
        }

    }

    private class FocusListener implements View.OnFocusChangeListener{

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {  
			    if (v.isInTouchMode()){		//判断是否是触摸，鼠标点击来切换焦点 
			    	TtsUtils.getInstance().speak(((TextView)v).getText().toString());
			    }  
			   }
			 }
			
		}
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			tvConfirm.clearFocus();
			tvCancel.requestFocus();
			TtsUtils.getInstance().speak(context.getResources().getString(R.string.no));	
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_UP:
			tvCancel.clearFocus();
			tvConfirm.requestFocus();
			TtsUtils.getInstance().speak(context.getResources().getString(R.string.yes));		
			break;
		case KeyEvent.KEYCODE_BACK:
	
			TtsUtils.getInstance().setCompletedListener(new TtsCompletedListener() {
				
				@Override
				public void onCompleted(String arg0) {
					mHandler.sendEmptyMessage(3);	
					TtsUtils.getInstance().setCompletedListener(null);
				}
			});

			TtsUtils.getInstance().speak(context.getResources().getString(R.string.no));
			this.dismiss();
			break;
		default:
			break;
		}
		return false;
		//return super.onKeyDown(keyCode, event);
	}
}