package com.sunteam.alarm;

import com.sunteam.alarm.utils.Global;
import com.sunteam.receiver.Alarm_receiver_Activity;
import com.sunteam.receiver.Alarmpublic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * @ClassName: BootCompletedReceiver  
 * @Description: 开机时会进入这个广播，这个时候可以做一些该做的业务。
 * @author zbc
 * @date 2013-11-25 下午4:44:30  
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		Global.debug("\r\n ===== [BootCompletedReceiver] " + intent.getAction());
		Global.debug("\r\n ===== [BootCompletedReceiver]");
		Global.debug("\r\n ===== [BootCompletedReceiver] ");
		Global.debug("\r\n ===== [BootCompletedReceiver] ");
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) { // 开机消息
			Global.debug("\r\n ==1111=== [BootCompletedReceiver] ");
			/*
			Intent startIntent = new Intent(context, com.sunteam.receiver.Alarm_receiver_Activity.class);
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(startIntent);
			*/
			Alarmpublic.debug("[###]Alarm_Receiver  ==2222222222222222222= \r\n");
			Intent mIntent = new Intent(context , Alarm_receiver_Activity.class);
			//Bundle bundle = new Bundle();//
			
			//bundle.putInt("FLAG", Alarmpublic.BOOT_FLAG); // 修改项
			mIntent.putExtra("FLAG", Alarmpublic.BOOT_FLAG); // 传入参数 
			mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mIntent);	
			
//			Alarmpublic.UpateAlarm(context);
		}
		else{
			Alarmpublic.UpateAlarm(context);
		}
		
    }

}
