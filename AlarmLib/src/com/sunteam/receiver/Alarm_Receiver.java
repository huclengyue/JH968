package com.sunteam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * @ClassName: AlarmReceiver  
 * @Description: 闹铃时间到了会进入这个广播，这个时候可以做一些该做的业务。
 * @author HuHood
 * @date 2013-11-25 下午4:44:30  
 *
 */
public class Alarm_Receiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "闹铃响了, 可以做点事情了~~", Toast.LENGTH_LONG).show();
		Alarmpublic.debug("[###]Alarm_Receiver  ==2222222222222222222= \r\n");
		Intent mIntent = new Intent(context , Alarm_receiver_Activity.class);
		//Bundle bundle = new Bundle();//
		
//		/bundle.putInt("FLAG", Alarmpublic.NORMAL_FLAG); // 修改项
		mIntent.putExtra("FLAG", Alarmpublic.NORMAL_FLAG); // 传入参数 
		
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(mIntent);		
    }

}
