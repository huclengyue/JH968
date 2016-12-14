package com.sunteam.recorder.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public abstract class RecordBaseActivity extends BaseActivity implements OnGestureListener{
	private final int swipe_min_distance = 120;
	private GestureDetector detector;
		public abstract void leftSlip();
		public abstract void rightSlip();
		public abstract void upSlip();
		public abstract void downSlip();
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
//		this.getWindow().setBackgroundDrawable(new ColorDrawable(Global.getBackgroundColor())); 
		
		detector = new GestureDetector(this);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		this.detector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float arg2,
			float arg3) {
		 Log.e("Fling", "Fling Happened!");    
	        float x1 = e1.getX(), x2 = e2.getX();  
	        float y1 = e1.getY(), y2 = e2.getY();   
	        if (x1 -x2 > swipe_min_distance) {    
	            Log.e(this.getClass().getName(), "To LEFT" + "(" + x1  
	                    + "," + x2 + ")");  
	            leftSlip();
	            return true;  
	        } else if (x2 - x1 > swipe_min_distance) {    
	            Log.e(this.getClass().getName(), "To Right" + "(" + x1  
	                    + "," + x2 + ")");  
	            rightSlip();
	            return true;  
	        } else if(y1- y2>swipe_min_distance){
	        	Log.e(this.getClass().getName(), "To up" + "(" + x1  
	                    + "," + x2 + ")");  
	            upSlip();
	            return true;
	        }else if(y2-y1>swipe_min_distance){
	        	Log.e(this.getClass().getName(), "To down" + "(" + x1  
	                    + "," + x2 + ")");  
	            downSlip();
	            return true;
	        }
	        return false;	 
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float arg2,
			float arg3) {
//		float y1 = e1.getY(), y2 = e2.getY();  
//        if (y1 -y2 > swipe_min_distance) {    
//            Log.e(this.getClass().getName(), "To UP" + "(" + y1  
//                    + "," + y2 + ")");  
//            return true;  
//        } else if (y2 - y1 > swipe_min_distance) {    
//            Log.e(this.getClass().getName(), "To Down" + "(" + y1  
//                    + "," + y2 + ")");  
//            return true;  
//        }    
        return false;  
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}	
}
