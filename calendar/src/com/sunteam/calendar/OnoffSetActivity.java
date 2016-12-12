package com.sunteam.calendar;

import java.util.ArrayList;
import com.sunteam.calendar.constant.Global;
import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.dao.Alarminfo;
import com.sunteam.dao.GetDbInfo;
import com.sunteam.receiver.Alarmpublic;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class OnoffSetActivity extends MenuActivity {
	private int gyear = 0;
	private int gmonth = 0;
	private int gday = 0;
	
	private String FileName = null;  // 录音文件名    getResources().getString(R.string.remind_noFile);
	private String Path = null;   // 录音文件路径
	private int gonoff = 0; //开关标志
	private int ghour = 0;  // 小时
	private int gminute = 0;  // 分钟

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		Intent intent = getIntent();
		Bundle bundle =intent.getExtras();
			
		ghour = bundle.getInt("HOUR");
		gminute = bundle.getInt("MIN");
		
		gyear = bundle.getInt("YEAR");
		gmonth = bundle.getInt("MONTH");
		gday = bundle.getInt("DAY");
		
		Global.debug("\r\n  OnoffSetActivity gyear = "+ gyear + " gmonth =" + gmonth +  " gday ="+ gday);
		gonoff = bundle.getInt("ONOFF");
		FileName = bundle.getString("FILENAME");
		Path = bundle.getString("PATH");
		
		mTitle = getResources().getString(R.string.remind_on) + getResources().getString(R.string.remind_off);
		ArrayList<String> mTemp = new ArrayList<String>(); // 显示
		
		mTemp.add(getResources().getString(R.string.remind_on));
		mTemp.add(getResources().getString(R.string.remind_off));
		mMenuList = mTemp;
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.calendar_onoff_set);
	}
	
	// 按键处理 抬起处理
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		// 确认键
		if(keyCode == KeyEvent.KEYCODE_ENTER ||
				keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
		{
			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.set_ok));
			mPromptDialog.show();
			mPromptDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					// TODO 自动生成的方法存根
					Global.debug("**************************************");
					Intent intent = new Intent();	//新建 INtent
					Bundle bundle = new Bundle();	//新建 bundle
					gonoff = getSelectItem();
					if(gonoff == Global.ALARM_OFF){  // 此处需要注意， 逻辑翻转
						gonoff = Alarmpublic.ALARM_OFF; 
					}
					else {
						gonoff = Alarmpublic.ALARM_ON;
					}
					//saveDataRemind();
					bundle.putInt("ONOFF", gonoff /*getSelectItem()*/);
					Global.debug("********************gonoff *==" + gonoff);
					
					intent.putExtras(bundle); // 参数传递
					setResult(Global.FLAG_ONOFF_ID,intent);	//返回界面
					finish();
				}
			});
			
			return true;
		}

		return super.onKeyUp(keyCode, event);	
	}
	
	// 保存提醒数据
	@SuppressWarnings("unused")
	private void saveDataRemind() {
		// TODO 自动生成的方法存根
		Alarminfo tempinfo = new Alarminfo();  // 
		
		
		tempinfo.year = gyear;
		tempinfo.month = gmonth;
		tempinfo.day = gday;
		Global.debug("\r\n  saveDataRemind gyear = "+ gyear + " gmonth =" + gmonth +  " gday ="+ gday);
		tempinfo.hour = ghour;
		tempinfo.minute = gminute;
		
		tempinfo.filename = FileName;
		tempinfo.path = Path;
		tempinfo.onoff = gonoff;
		

		GetDbInfo dbInfo = new GetDbInfo(this);  // 打开数据库
		int max_num = dbInfo.getCount(Alarmpublic.REMIND_TABLE); // 记录条数
		int max_Id = dbInfo.getMaxId(Alarmpublic.REMIND_TABLE); // 记录条数
		if(max_num <= 0) // 无数据
		{
			 // 增加数据
			tempinfo._id = max_Id +1;
			dbInfo.add(tempinfo, Alarmpublic.REMIND_TABLE);	
			dbInfo.closeDb();
		}
		else{  // 已经有数据 
			Alarminfo tempinfo_1 = new Alarminfo();  // 临时变量
			boolean flag = false;
			ArrayList<Alarminfo> allData =   new ArrayList<Alarminfo>();
			allData = dbInfo.getAllData(Alarmpublic.REMIND_TABLE);
			for(int i = 0; i < max_num; i++) // 查找相同的 替换
			{
				tempinfo_1 = allData.get(i);
				if((tempinfo.year ==  tempinfo_1.year) &&
						(tempinfo.month == tempinfo_1.month)&&
						(tempinfo.day == tempinfo_1.day))  // 同一天
				{
					tempinfo._id = tempinfo_1._id;
					dbInfo.update(tempinfo, Alarmpublic.REMIND_TABLE);
					flag = true;   // 数据以保存
					break;
				}
			}
			if(false == flag)  // 数据未保存
			{
				tempinfo._id = max_Id +1;
				dbInfo.add(tempinfo, Alarmpublic.REMIND_TABLE);	// 增加数据
			}
			dbInfo.closeDb();
		}
		Alarmpublic.UpateAlarm(this);
	}
}
