package com.sunteam.calendar;

import java.util.Calendar;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.sunteam.calendar.calendar.LunarCalendar;
import com.sunteam.calendar.calendar.SpecialCalendar;
import com.sunteam.calendar.constant.Global;
import com.sunteam.common.menu.BaseActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.Tools;

public class MainActivity extends BaseActivity {
  

	private TextView mTvTitle = null; // 标题栏
	private View mLine = null; // 分割线
	

	private TextView mYearInfo = null; // 时间信息 年
	private TextView mMonthInfo = null; // 时间信息 月
	private TextView mLunarDataInfo = null; // 时间信息
	private int gyear;
	private int gmonth;
	private int gday;
	
	private int select_id = 0;  // 默认选择项
	
	private int SELECT_YEAR_ID = 0;  // 选择项 年
	private int SELECT_MONTH_ID = 1;  // 选择项 月
	private int SELECT_DAY_ID = 2;  // 选择项 日 
	
	private int YEAR_ID = 0; // 年份
	private int LUNAR_ID = 1; // 阴历
	private int SOLAR_ID = 2; // 阳历

	
//	private int FONTSIZE = 24;
//	private int FONTSIZEHIGHT = 24;
	private boolean isSolarCalendar = true; // 是否为阳历
	
	private boolean isLeapyear = false; // 是否为闰年
	private int daysOfMonth = 0; // 某月的天数
	private int dayOfWeek = 0; // 具体某一天是星期几
	private int lastDaysOfMonth = 0; // 上一个月的总天数
	
	//private Context context;
	
	private SpecialCalendar sc = null;
	private LunarCalendar lc = null;
	private int MAX_TEXT = 49;  // 总共显示内容
	private int MAX_WEEK = 7;  // 一周的天数
	private TextView[] gTextViews = new TextView[MAX_TEXT]; // 一个显示数据中的日期存入此数组中
	private int[] gTextId = new int[] {    
		  R.id.h0, 	R.id.h1,  R.id.h2,  R.id.h3,  R.id.h4,  R.id.h5,  R.id.h6,  R.id.h7,  R.id.h8,  R.id.h9,
		  R.id.h10, R.id.h11, R.id.h12, R.id.h13, R.id.h14, R.id.h15, R.id.h16, R.id.h17, R.id.h18, R.id.h19,
		  R.id.h20, R.id.h21, R.id.h22, R.id.h23, R.id.h24, R.id.h25, R.id.h26, R.id.h27, R.id.h28, R.id.h29,
		  R.id.h30, R.id.h31, R.id.h32, R.id.h33, R.id.h34, R.id.h35, R.id.h36, R.id.h37, R.id.h38, R.id.h39,
		  R.id.h40, R.id.h41, R.id.h42, R.id.h43, R.id.h44, R.id.h45, R.id.h46, R.id.h47, R.id.h48,
		  
	};
	private String[] gDayId = new String[] {    
			"1",  "2",  "3",  "4",  "5",  "6",  "7",  "8",  "9",  "10",
			"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
			"21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
			"31", "32",
			  
		};
	
	private String[] gWeekId =null;
	
	private String[] gWeekSpeakId =null;
	
	//网格布局1 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_main);
		
		Global.ALARM_FILE_NAME = getResources().getString(R.string.remind_noFile);
		
		TtsUtils.getInstance(this, null);
		// 初始化
		init();
	}
		
	// 回复界面
		@Override
	protected void onResume() {
		Global.debug("===== onResume ==1== ");
		super.onResume();
		TtsUtils.getInstance().stop();
		Global.debug("===== onResume ==2== ");
		TtsUtils.getInstance().speak(getResources().getString(R.string.calendar_name)+ "。", 0);
		Global.debug("===== onResume ==3== ");
		//speakInfo();
		speakInfo_forstart();
		Global.debug("===== onResume ==4== ");
		
	}
	// 界面开始
	@Override
    protected void onStart() {
		super.onStart();
		//Global.initTts(this);
		Global.debug("===== onStart ==== ");
    }
// 界面销毁
	@Override
	protected void onDestroy() {
		
		Global.debug("===== onDestroy ==== ");
		super.onDestroy();
		
		if (TtsUtils.getInstance() != null) {
			TtsUtils.getInstance().destroy();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}	
	// 初始化
	private void init() {
		// TODO 自动生成的方法存根
		isSolarCalendar = true;
		//isSolarCalendar = false;
		select_id = SELECT_DAY_ID;
		
		gWeekId = new String[MAX_WEEK];
		gWeekId[0]=getResources().getString(R.string.week0);
		gWeekId[1]=getResources().getString(R.string.week1);
		gWeekId[2]=getResources().getString(R.string.week2);
		gWeekId[3]=getResources().getString(R.string.week3);
		gWeekId[4]=getResources().getString(R.string.week4);
		gWeekId[5]=getResources().getString(R.string.week5);
		gWeekId[6]=getResources().getString(R.string.week6);
		
		gWeekSpeakId = new String[MAX_WEEK];
		gWeekSpeakId[0]=getResources().getString(R.string.wk0);
		gWeekSpeakId[1]=getResources().getString(R.string.wk1);
		gWeekSpeakId[2]=getResources().getString(R.string.wk2);
		gWeekSpeakId[3]=getResources().getString(R.string.wk3);
		gWeekSpeakId[4]=getResources().getString(R.string.wk4);
		gWeekSpeakId[5]=getResources().getString(R.string.wk5);
		gWeekSpeakId[6]=getResources().getString(R.string.wk6);
		
		// 获取日期是星期几
		sc = new SpecialCalendar();
		// 农历 
		lc = new LunarCalendar();
		for(int i = 0 ; i < MAX_TEXT; i++)  // 赋初值
		{
			gTextViews[i]= (TextView)findViewById(gTextId[i]);
		}
		Tools mTools = new Tools(this);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(mTools.getBackgroundColor()));
		
		mTvTitle = (TextView) this.findViewById(R.id.title); // 标题栏
		//mDataInfo = (TextView) this.findViewById(R.id.dateinfo);
		mYearInfo = (TextView)findViewById(R.id.year);
		mMonthInfo = (TextView)findViewById(R.id.month);
		
		mLunarDataInfo = (TextView)findViewById(R.id.lunarinfo);
		mLine = this.findViewById(R.id.line); // 分割线
		TextView mYear_str = (TextView)findViewById(R.id.year_str);
		TextView mMonth_str = (TextView)findViewById(R.id.month_str); 
		
		//Tools mTools = new Tools(this);
		//this.setBackgroundColor(mTools.getBackgroundColor()); // 设置View的背景色
		mTvTitle.setTextColor(mTools.getFontColor()); // 设置title的文字颜色
		mYearInfo.setTextColor(mTools.getFontColor()); // 设置年份的文字颜色 
		mMonthInfo.setTextColor(mTools.getFontColor()); // 设置月份的文字颜色
		mMonthInfo.setTextColor(mTools.getFontColor()); // 设置月份的文字颜色
		
		mLunarDataInfo.setTextColor(mTools.getFontColor());
		
		mYear_str.setTextColor(mTools.getFontColor());
		mMonth_str.setTextColor(mTools.getFontColor());
		// 设置字体大小
		/*int fontSize = mTools.getFontSize();
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize); // 设置title字号
		mTvTitle.setHeight(fontSize);
		
		mYearInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize); // 设置title字号
		mYearInfo.setHeight(fontSize);
		
		mMonthInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize); // 设置title字号
		mMonthInfo.setHeight(fontSize);
		
		
		mLunarDataInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONTSIZE); // 设置title字号
		mLunarDataInfo.setHeight(FONTSIZE);*/
		
		
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色
		
		mTvTitle.setText(getResources().getString(R.string.calendar_name));
		// 获取时间
		Calendar calendar = Calendar.getInstance();  // 获取日历

//		String year = Integer.toString(calendar.get(Calendar.YEAR));
//		String month = Integer.toString(calendar.get(Calendar.MONTH));
//		String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
		
		gyear = calendar.get(Calendar.YEAR);
		gmonth = calendar.get(Calendar.MONTH) +1;  // 月份从0开始，需要+1
		gday = calendar.get(Calendar.DAY_OF_MONTH);
		
		// 获取显示信息
		
		SetTextView(gyear, gmonth, gday, false);
		
	}
	// 获取万年历信息
	private void getCalendar(int year, int month) {
		// TODO 自动生成的方法存根
		isLeapyear = sc.isLeapYear(year); // 是否为闰年
		Global.debug("isLeapyear ==== "+isLeapyear);
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month); // 某月的总天数
		Global.debug("daysOfMonth ==== "+daysOfMonth);
		dayOfWeek = sc.getWeekdayOfMonth(year, month); // 某月第一天为星期几
		Global.debug("dayOfWeek ==== "+dayOfWeek);
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month - 1); // 上一个月的总天数
		Global.debug("lastDaysOfMonth ==== "+lastDaysOfMonth);
	}
	
	// 获取万年历信息
	private int getWeakOfDay(int year, int month, int day) {
		// TODO 自动生成的方法存根
		
		return sc.getWeekdayOfMonthAndDay(year, month, day);
	}
	
	// 显示界面
	private void SetTextView(int year, int month, int day, boolean flag) {
		// TODO 自动生成的方法存根
		Tools mTools = new Tools(this);
		//int fontSize = mTools.getFontSize();
		for(int i = 0; i < MAX_TEXT; i++)
		{
			gTextViews[i].setTextColor(mTools.getFontColor());;
//			gTextViews[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, FONTSIZE); // 设置title字号
//			gTextViews[i].setHeight(FONTSIZEHIGHT);
			gTextViews[i].setBackgroundColor(mTools.getBackgroundColor());

			gTextViews[i].getPaint().setFlags(0);
		}
		// 获取显示信息
		getCalendar(year, month);
		
		for(int i = 0; i < MAX_TEXT; i++)
		{
			gTextViews[i].setText(" ");// 先清空
			
			if(i / MAX_WEEK == 0)  // 显示 星期
			{
				gTextViews[i].setText(gWeekId[i]);
			}
			else if(i / MAX_WEEK == 1)  // 第一列
			{
				int tempWeek = i % MAX_WEEK;
				
				if(dayOfWeek  == tempWeek)  // 找到第一个显示点
				{
					for(int j = 0; j < daysOfMonth ; j++)
					{
						//Global.debug(" j == " + j +" gDayId[j] ==== " +gDayId[j]+ " i == "+i );
						if(true == isSolarCalendar)  // 阳历
						{
							gTextViews[i].setText(gDayId[j]);
						}
						else{  // 阴历
							gTextViews[i].setText(getLunarDay(gyear, gmonth, j+1));
						}
						
						if( ((day-1) == j) && (select_id == SELECT_DAY_ID))  // 日需要一直反显
						{
							gTextViews[i].setBackgroundColor(mTools.getHighlightColor());
//							gTextViews[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, FONTSIZEHIGHT); // 设置title字号
//							gTextViews[i].setHeight(FONTSIZEHIGHT);
						}
						else if((day-1) == j){
							//gTextViews[i].getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
							//gTextViews[i].getPaint().setFakeBoldText(true);;//下划线
							//gTextViews[i].setBackgroundColor(mTools.getFontColor());
							gTextViews[i].setTextColor(mTools.getHighlightColor());
							
						}
						
/*						if( (day-1) == j)  // 日需要一直反显
						{
							gTextViews[i].setBackgroundColor(mTools.getHighlightColor());
//							gTextViews[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, FONTSIZEHIGHT); // 设置title字号
//							gTextViews[i].setHeight(FONTSIZEHIGHT);
						}*/
						
						i++;	
					}
					i--;
				//	Global.debug(" gTextViews[i].getText()=[1]== " + gTextViews[i].getText() + " i == "+ i);
				}
			}
		//	Global.debug(" gTextViews[i].getText()=[2]== " + gTextViews[i].getText() + " i == "+ i);
		}

		String data  = null;
		Global.debug(" data ==========11==============");
		// 设置背景		
		if(select_id == SELECT_YEAR_ID)
		{
			mYearInfo.setBackgroundColor(mTools.getHighlightColor());
		}
		else{
			mYearInfo.setBackgroundColor(mTools.getBackgroundColor());
		}
		Global.debug(" data ==========22==============");
		if(select_id == SELECT_MONTH_ID)
		{
			mMonthInfo.setBackgroundColor(mTools.getHighlightColor());
		}
		else{
			mMonthInfo.setBackgroundColor(mTools.getBackgroundColor());
		}
		Global.debug(" data ==33====gyear = "+ gyear + " gmonth = " + gmonth);
		mYearInfo.setText( Integer.toString(gyear));
		mMonthInfo.setText(Integer.toString(gmonth));
		Global.debug(" data ==========3444==============");
		data  = null;
		data = getAnimalsYear(gyear, gmonth, gday) + getResources().getString(R.string.year) +
				getLunarMon(gyear, gmonth, gday)+
				getLunarDay(gyear, gmonth, gday);
		
		
		Global.debug(" data ====5555===data = "+ data);
		
		mLunarDataInfo.setText(data);
		Global.debug(" data ====666===flag = "+ flag);
		if(true == flag){
			speakInfo();
		}
	}

	private void speakInfo() {
		// TODO 自动生成的方法存根
		String str = null; 
		String str1 = null;
		// 判断是 阴历还是阳历
		//if(isSolarCalendar == true)   // 阳历
		{
			if(select_id == SELECT_YEAR_ID)  // 
			{
			//	str = getResources().getString(R.string.solarcalendar);
			//	str = str + ",";
				str = null;
				str1 = null;
				str = gyear+ getResources().getString(R.string.year)+
						gmonth + getResources().getString(R.string.month)+
						gday + getResources().getString(R.string.day) +
						"," +
						gWeekSpeakId[getWeakOfDay(gyear, gmonth, gday)];
				
				str1 = getResources().getString(R.string.lunarlendar);
				str1 = str1 + ",";
				str1 = getAnimalsYear(gyear, gmonth, gday) + getResources().getString(R.string.year)+  // 年
				"," +
				getLunarMon(gyear, gmonth, gday)+
				getLunarDay(gyear, gmonth, gday);
			}
			else if(select_id == SELECT_MONTH_ID )
			{
			//	str = getResources().getString(R.string.solarcalendar);
			//	str = str + ",";
				str = null;
				str1 = null;
				str = 	gmonth + getResources().getString(R.string.month)+
						gday + getResources().getString(R.string.day) +
						"," +
						gWeekSpeakId[getWeakOfDay(gyear, gmonth, gday)];
				
				str1 = getResources().getString(R.string.lunarlendar);
				str1 = str1 + ",";
				str1 += getLunarMon(gyear, gmonth, gday)+ getLunarDay(gyear, gmonth, gday);
			}
			else if(select_id == SELECT_DAY_ID){
				str = null;
				str1 = null;
				str = 	gday + getResources().getString(R.string.day) +
						"," +
						gWeekSpeakId[getWeakOfDay(gyear, gmonth, gday)];
				
				str1 = getResources().getString(R.string.lunarlendar);
				str1 = str1 + ",";
				str1 += getLunarMon(gyear, gmonth, gday)+ getLunarDay(gyear, gmonth, gday);
			}
			else{
				str = null;
				str1 = null;
				str = 	gday + getResources().getString(R.string.day) +
						"," +
						gWeekSpeakId[getWeakOfDay(gyear, gmonth, gday)];
				
				str1 = getResources().getString(R.string.lunarlendar);
				str1 = str1 + ",";
				str1 += getLunarDay(gyear, gmonth, gday);
			}		
		}
		Global.debug("speak == " + str+";" +str1);
		TtsUtils.getInstance().speak( str+";" +str1);

		Global.debug("speak =222= " + str+";" +str1);
		
		

	}
	
	private void speakInfo_forstart() {
		// TODO 自动生成的方法存根
		String str = null; 
		String str1 = null;
		
		str = null;
		str1 = null;
		str = gyear+ getResources().getString(R.string.year)+
				gmonth + getResources().getString(R.string.month)+
				gday + getResources().getString(R.string.day) +
				"," +
				gWeekSpeakId[getWeakOfDay(gyear, gmonth, gday)];
		
		str1 = getResources().getString(R.string.lunarlendar);
		str1 = str1 + ",";
		str1 = getAnimalsYear(gyear, gmonth, gday) + getResources().getString(R.string.year)+  // 年
		"," +
		getLunarMon(gyear, gmonth, gday)+
		getLunarDay(gyear, gmonth, gday);
				

		Global.debug("speak == " + str+";" +str1);
		TtsUtils.getInstance().speak( str+";" +str1);

		Global.debug("speak =222= " + str+";" +str1);
	}
	
	public void delay(int len) {
		try {
			Thread.currentThread();
			Thread.sleep(len);// 毫秒
		} catch (Exception e) {
		}
	}
	// 键按下
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		
		switch (keyCode) {   
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			  		
			Intent intent = new Intent();			
			Bundle bundle = new Bundle();//

			bundle.putInt("YEAR", gyear); // 传入参数 年			
			bundle.putInt("MONTH", gmonth); // 传入参数 年
			bundle.putInt("DAY", gday); // 传入参数 年
			bundle.putInt("CALLID", Global.REMIND_CALL_MAIN);
			//intent.setClass(MainActivity.this, MenuActivity.class);
			intent.putExtras(bundle); // 传入参数
			//intent.setAction("remind_action");// 启动界面
			intent.setClass(MainActivity.this, RemindActivity.class);
			startActivityForResult(intent , Global.REMIND_FLAG_ID);  // 设置标志
			// Ok键 
			return true;
			
			
		case KeyEvent.KEYCODE_DPAD_DOWN:  // 下键

			if(select_id == SELECT_YEAR_ID)   // 焦点 在 年
			{
				if(gyear >= Global.MAX_YEAR){
					gyear = Global.MIN_YEAR;
				}
				else{
					gyear ++;
				}
			}
			else if(select_id == SELECT_MONTH_ID){			// 焦点 在 月
				if(gmonth >=  Global.MAX_MONTH)
				{
					gmonth = Global.MIN_MONTH;
				}
				else{
					gmonth ++;
				}
				getCalendar(gyear, gmonth);  // 获取有用的信息
				if(gday > daysOfMonth ){
					gday = daysOfMonth;
				}
			}
			else {  // 焦点 在 日
				if(gday >= daysOfMonth)
				{
					gday = 1;
				}
				else {
					gday ++;
				}
			}
			Global.debug("====gYear = "+gyear + " Gmonth == "+gmonth + " gday == "+gday);

			SetTextView(gyear, gmonth, gday, true);
			
			return true;
			
		case KeyEvent.KEYCODE_DPAD_UP: // 上键
			
			if(select_id == SELECT_YEAR_ID)
			{
				if(gyear <= Global.MIN_YEAR){
					gyear = Global.MAX_YEAR;
				}
				else{
					gyear --;
				}
			}
			else if(select_id == SELECT_MONTH_ID){			
				if(gmonth <=  Global.MIN_MONTH)
				{
					gmonth = Global.MAX_MONTH;
				}
				else{
					gmonth -- ;
				}
				getCalendar(gyear, gmonth);  // 获取有用的信息
				if(gday > daysOfMonth ){
					gday = daysOfMonth;
				}
			}
			else {
				if(gday <= 1)
				{
					gday = daysOfMonth;
				}
				else{
					gday --;
				}
			}
			Global.debug("====gYear = "+gyear + " Gmonth == "+gmonth + " gday == "+gday);
			SetTextView(gyear, gmonth, gday, true);
			
			return true;
			
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左键
			if(select_id <= SELECT_YEAR_ID)
			{
				select_id = SELECT_DAY_ID; 
			}
			else{
				select_id --;
			}
			
			SetTextView(gyear, gmonth, gday, true);
			return true;
			
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右键
			if(select_id >= SELECT_DAY_ID)
			{
				select_id = SELECT_YEAR_ID; 
			}
			else{
				select_id ++;
			}
		
			SetTextView(gyear, gmonth, gday, true);
			return true;
			
		case KeyEvent.KEYCODE_MENU:   // menu界面启动
		case KeyEvent.KEYCODE_F2:
		case KeyEvent.KEYCODE_PAGE_UP:
			
			Intent intent1 = new Intent();			
			Bundle bundle1 = new Bundle();//  

			bundle1.putInt("YEAR", gyear); // 传入参数 年
			bundle1.putInt("MONTH", gmonth); // 传入参数 年
			bundle1.putInt("DAY", gday); // 传入参数 年
			
			//intent.setClass(MainActivity.this, MenuActivity.class);
			intent1.putExtras(bundle1); // 传入参数
			intent1.setAction("menu_action");// 启动界面
			
			startActivityForResult(intent1 , Global.MENU_FLAG_ID);  // 设置标志
			
			break;
			
		default:
			break;
		}

		return super.onKeyUp(keyCode, event);
	}
	//  
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		
		switch (keyCode) {   
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_DOWN:  // 下键
/*			if(select_id == SELECT_YEAR_ID)   // 焦点 在 年
			{
				if(gyear >= Global.MAX_YEAR){
					gyear = Global.MIN_YEAR;
				}
				else{
					gyear ++;
				}
			}
			else if(select_id == SELECT_MONTH_ID){			// 焦点 在 月
				if(gmonth >=  Global.MAX_MONTH)
				{
					gmonth = Global.MIN_MONTH;
				}
				else{
					gmonth ++;
				}
				getCalendar(gyear, gmonth);  // 获取有用的信息
				if(gday > daysOfMonth ){
					gday = daysOfMonth;
				}
			}
			else {  // 焦点 在 日
				if(gday >= daysOfMonth)
				{
					gday = 1;
				}
				else {
					gday ++;
				}
			}
			Global.debug("====gYear = "+gyear + " Gmonth == "+gmonth + " gday == "+gday);

			SetTextView(gyear, gmonth, gday, true);*/
			return true;

		case KeyEvent.KEYCODE_DPAD_UP: // 上键
/*			
			if(select_id == SELECT_YEAR_ID)
			{
				if(gyear <= Global.MIN_YEAR){
					gyear = Global.MAX_YEAR;
				}
				else{
					gyear --;
				}
			}
			else if(select_id == SELECT_MONTH_ID){			
				if(gmonth <=  Global.MIN_MONTH)
				{
					gmonth = Global.MAX_MONTH;
				}
				else{
					gmonth -- ;
				}
				getCalendar(gyear, gmonth);  // 获取有用的信息
				if(gday > daysOfMonth ){
					gday = daysOfMonth;
				}
			}
			else {
				if(gday <= 1)
				{
					gday = daysOfMonth;
				}
				else{
					gday --;
				}
			}
			Global.debug("====gYear = "+gyear + " Gmonth == "+gmonth + " gday == "+gday);
			SetTextView(gyear, gmonth, gday, true);
			*/
			return true;

		case KeyEvent.KEYCODE_DPAD_LEFT: // 左键
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右键	
		case KeyEvent.KEYCODE_MENU:   // menu界面启动
		case KeyEvent.KEYCODE_F2:
		case KeyEvent.KEYCODE_PAGE_UP:					
			break;			
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	// 参数返回
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Global.MENU_FLAG_ID && resultCode ==  Global.MENU_FLAG_ID){
			
			Bundle bundle=data.getExtras();	
			long temp_id = bundle.getInt("SELECID");
			
			Global.debug("[*********]onActivityResult temp_id ="+temp_id);
			
			if(temp_id  == YEAR_ID){  // 修改日期
				
				int year = bundle.getInt("YEAR");  
		
				gyear = year;
				
				SetTextView(gyear, gmonth, gday, false);
			}
			else if(temp_id == LUNAR_ID){ // 选择第二项  农历
				isSolarCalendar = false;  
				SetTextView(gyear, gmonth, gday, false);
			}
			else if(temp_id == SOLAR_ID)  // 阳历
			{
				isSolarCalendar = true;  
				SetTextView(gyear, gmonth, gday, false);
			}			
			else {  // 
				SetTextView(gyear, gmonth, gday, false);
			}			
		}
		else if(requestCode == Global.REMIND_FLAG_ID && resultCode ==  Global.REMIND_FLAG_ID){
			SetTextView(gyear, gmonth, gday, false);
		}
	}

	
	/**
	 * 根据日期的年月日返回阴历日期
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public String getLunarDay(int year, int month, int day) {
		String lunarDay = lc.getLunarDate(year, month, day, true);
		// {由于在取得阳历对应的阴历日期时，如果阳历日期对应的阴历日期为"初一"，就被设置成了月份(如:四月，五月。。。等)},所以在此就要判断得到的阴历日期是否为月份，如果是月份就设置为"初一"
/*		if (lunarDay.substring(1, 2).equals("月")) {
			lunarDay = "初一";
		}*/
		return lunarDay;
	}
	/**
	 * 根据日期的年 返回阴历年 天干地支
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public String getLunarYear(int year) {
		String lunarDay = lc.cyclical(year);
		// {由于在取得阳历对应的阴历日期时，如果阳历日期对应的阴历日期为"初一"，就被设置成了月份(如:四月，五月。。。等)},所以在此就要判断得到的阴历日期是否为月份，如果是月份就设置为"初一"
		
		return lunarDay;
	}
	
	/**
	 * 根据日期的年 返回阴历月 
	 * @param year
	 * @param month
	 * @param day
	 * @return 月份
	 */
	public String getLunarMon(int year, int mon, int day) {
		lc.getLunarDate(year, mon, day, true);
			
		return lc.getLunarMonth();
	}
	
	/**
	 * 根据日期的年 返回 生肖年 
	 * @param year
	 * @param 
	 * @param 
	 * @return  生肖年 
	 */
	public String getAnimalsYear(int year,int mon, int day) {
		lc.getLunarDate(year, mon, day, true);
		return lc.animalsYear( lc.getYear());
	}
		
}
