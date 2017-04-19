package com.sunteam.massage;

import java.text.DecimalFormat;

import com.sunteam.common.menu.BaseActivity;
import com.sunteam.common.tts.TtsUtils;
import com.sunteam.common.utils.ConfirmDialog;
import com.sunteam.common.utils.PromptDialog;
import com.sunteam.common.utils.Tools;
import com.sunteam.common.utils.dialog.ConfirmListener;
import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.massage.utils.Global;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class SettingActivity extends BaseActivity {
	private static final int USER_PATH = 0;// 内存数据标志
//	private static final int TF_PATH = 1;// 存储卡数据标志
	//private static final String TextView = null;
	private long gId = 0;   // 第几个数据
	private int gUserId = 0;   // 用户标志
	private long DATE_SET = 0; // 日期设置
	private long FORWORK_SET = 1; // 排钟设置
	private long OVERWORK_SET = 2; // 点钟设置
	private long MONEY_SET = 3; // 金额设置
	// 日期
	private int gyear = 0;
	private int gmonth = 0;
	private int gday = 0;
	

	private int gyear_bk = 0;
	private int gmonth_bk = 0;
	private int gday_bk = 0;
	
	private int month_day[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 闰年2月29天

	private int gflag = 0; // 记录焦点位置
	// 标志
	private double gForTime = 0.0; // 排钟
	private double gOverTime = 0.0; // 点钟
	private int gMoney = 0; // 金额
	
	private double gForTime_bk = 0.0; // 排钟
	private double gOverTime_bk = 0.0; // 点钟
	private int gMoney_bk = 0; // 金额
	
	
	private Boolean gPointFlag = false; // 小数点标志־
	private Boolean gNumberFlag = false; // 数字输入标志
	private int gNumBitflag = 0; // 数据位数标志
	

	private String cNum = null;  // 保存字串

	private TextView tYear = null;
	private TextView tMonth = null;
	private TextView tDay = null;
	
	TextView mTvTitle = null;
	View mLine = null;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.setting);
		
		gPointFlag = false;
		// 获取传递参数
		Intent intent=getIntent();	//获取Intent
		Bundle bundle=intent.getExtras();	//获取 Bundle
		
		gId = bundle.getInt("ID");  // 获取修改位置
		gUserId = bundle.getInt("USERID"); // 获取用户ID
		
		gyear_bk = gyear  = bundle.getInt("YEAR");
		gmonth_bk = gmonth = bundle.getInt("MONTH");
		gday_bk = gday = bundle.getInt("DAY");
		
		gForTime_bk = gForTime = bundle.getDouble("FORTIME");   // 排钟
		gOverTime_bk = gOverTime = bundle.getDouble("OVERTIME"); // 点钟
		
		Global.debug("[***]onCreate gForTime = " + gForTime + " gOverTime = "+ gOverTime);
		gMoney_bk = gMoney = bundle.getInt("MONEY");
		
		Global.debug("[onCreate ] ID= "+ gId + " year + "+ gyear+ " month ="+ gmonth+" day = "+ gday);
		Global.debug("[onCreate ] forwork= "+ bundle.getDouble("FORTIME") + " overwork + "+ bundle.getDouble("OVERTIME") + " money ="+ gMoney);
		
		
		if(gId == DATE_SET)  // 日期
		{
			setContentView(R.layout.massage_setting);	
			TTS_speak(false, getResources().getString(R.string.cur_date));
		}
		else if(gId == FORWORK_SET || gId == OVERWORK_SET) // 排钟 点钟
		{	
			double dtmp = 0;
			
			if(gId == FORWORK_SET){
				dtmp = bundle.getDouble("FORTIME");
			}
			else{
				dtmp = bundle.getDouble("OVERTIME");
			}
			
			if(dtmp < 0.1)  // 0 
			{
				dtmp = 0;
				cNum = Integer.toString((int)dtmp);
			}
			else if((int)(dtmp*10)%10  != 0){  // 是小数
				cNum = Double.toString(dtmp);
				gPointFlag = true;
			}
			else{
				cNum = Integer.toString((int)dtmp);
			}
			
			setContentView(R.layout.massage_setting);
			
			if(gId == FORWORK_SET)
			{
				TTS_speak(false, getResources().getString(R.string.cur_forTime));
			}
			else{
				TTS_speak(false, getResources().getString(R.string.cur_overTime));
			}	
		}
		else if(gId == MONEY_SET)  //金额
		{
			int dtmp = 0;
		
			dtmp = bundle.getInt("MONEY");
			cNum = Integer.toString(dtmp);
			
			setContentView(R.layout.massage_setting);
			TTS_speak(false, getResources().getString(R.string.cur_money));
		}
	//	cNum = "0";
			
		initView();
		//showview();
	}
	// 焦点改变
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {  // 焦点在
			//mUserView.calcItemCount();
			//getStatusBarHeight();
		}
		
		Global.debug("=[set]==== onWindowFocusChanged ==== ");
	}
// 界面恢复
	@Override
	protected void onResume() {
		Tools mTools = new Tools(this);
		
		int fontSize = mTools.getFontSize();
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // 设置title字号
		mTvTitle.setHeight(mTools.convertSpToPixel(fontSize));
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色
		
		mTvTitle.setTextColor(mTools.getFontColor()); // 设置字体颜色
		tYear.setTextColor(mTools.getFontColor()); //设置字体颜色
		tMonth.setTextColor(mTools.getFontColor()); // 设置字体颜色
		tDay.setTextColor(mTools.getFontColor()); // 设置字体颜色
		TextView tv1 = (TextView) findViewById(R.id.tv1);
		tv1.setTextColor(mTools.getFontColor());
		
		TextView tv2 = (TextView) findViewById(R.id.tv2);
		tv2.setTextColor(mTools.getFontColor());
		
		Global.debug("==[set]=== onResume ==== ");
		showview(true);
		super.onResume();
	}
// 界面开始
	@Override
    protected void onStart() {
		super.onStart();
		//Global.initTts(this);
		Global.debug("===[set]== onStart ==== ");
    }
// 界面销毁
	@Override
	protected void onDestroy() {
		if (TtsUtils.getInstance() != null) {
//			Global.getTts().destroy();
		}
		Global.debug("===[set]== onDestroy ==== ");
		super.onDestroy();
	}
	// 初始化
	private void initView() {

		Tools mTools = new Tools(this);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(mTools.getBackgroundColor()));
				
		mTvTitle = (TextView) this.findViewById(R.id.title); // 获取控件
		mLine = this.findViewById(R.id.line); // 获取
		
		tYear = (TextView)findViewById(R.id.year);
		tMonth = (TextView)findViewById(R.id.month);
		tDay = (TextView)findViewById(R.id.day);
		
		
		int fontSize = mTools.getFontSize();
		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTools.getFontPixel()); // 设置title字号
		mTvTitle.setHeight(mTools.convertSpToPixel(fontSize));
		mLine.setBackgroundColor(mTools.getFontColor()); // 设置分割线的背景色
		
		mTvTitle.setTextColor(mTools.getFontColor()); //  设置字体颜色
		tYear.setTextColor(mTools.getFontColor()); //  设置字体颜色
		tMonth.setTextColor(mTools.getFontColor()); // 设置字体颜色
		tDay.setTextColor(mTools.getFontColor()); //  设置字体颜色
		
		String title = null;
		
	//	cNum = "0";

		if(gId == DATE_SET)
		{
			tYear.setText(Integer.toString(gyear));
			tMonth.setText(Integer.toString(gmonth));
			tDay.setText(Integer.toString(gday));
			
			tYear.setTextColor(new Tools(this).getHighlightColor()); // 设置颜色反显
			
			title = getResources().getString(R.string.cur_date_title);
		}
		else if(gId == FORWORK_SET)
		{
			title = getResources().getString(R.string.cur_forTime_title);
			//tMonth.setText(Double.toString(gForTime) + getResources().getString(R.string.hour));
			tMonth.setText(cNum + getResources().getString(R.string.hour));
		}
		else if(gId == OVERWORK_SET)
		{
			title = getResources().getString(R.string.cur_overTime_title);
			//tMonth.setText(Double.toString(gOverTime) + getResources().getString(R.string.hour));
			tMonth.setText(cNum + getResources().getString(R.string.hour));
		}
		else if(gId == MONEY_SET)
		{
			title = getResources().getString(R.string.cur_money_title);
			
			tMonth.setText(cNum + getResources().getString(R.string.yuan));
		}	
		mTvTitle.setText(title);
	}
	

	// 键按下
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	
		switch (keyCode) {   
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:

			PromptDialog mPromptDialog = new PromptDialog(this, getResources().getString(R.string.set_ok));
			mPromptDialog.show();
			mPromptDialog.setPromptListener(new PromptListener() {
				
				@Override
				public void onComplete() {
					mHandler.sendEmptyMessage(Global.MSG_BACK);
				}
			});			
			return true;
			
		case KeyEvent.KEYCODE_DPAD_UP:  // 上键处理
			gNumberFlag = true;  // 数字输入标志
			gNumBitflag = 0;
			setUpKey(); // 上键处理
			return true;
			
		case KeyEvent.KEYCODE_DPAD_DOWN: //下键处理
			gNumberFlag = true;  //  数字输入标志
			gNumBitflag = 0;
			setDownKey();
			return true;
			
		case KeyEvent.KEYCODE_STAR: // *键
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左键
			//TtsUtils.getInstance().stop();
			setLeftKey();
			return true;
			
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右键
//			TtsUtils.getInstance().stop();
			setRightKey();
			return true;
			
		case KeyEvent.KEYCODE_MENU:
			//mAdapter.menu_key();;  // 
			return true;
			
			// 数字键
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
//			TtsUtils.getInstance().stop();
//			setNumberKey(keyCode);//   -- 暂时去除数字输入
			
			return true;
		
		case KeyEvent.KEYCODE_POUND:  // #键
			Global.debug("\r\n [KEYCODE_POUND] ==================== ");
			gPointFlag = true;  // 小数点
			double d_temp = Double.valueOf(cNum);
		
			cNum = Double.toString(d_temp);
			showview(false);
			return true;
			
		case KeyEvent.KEYCODE_BACK:  // 返回键
			if(gday != gday_bk || gmonth != gmonth_bk || gyear != gyear_bk || 
				gForTime != gForTime_bk || gOverTime != gOverTime_bk || gMoney != gMoney_bk){   // 有改变
				
				ConfirmDialog mConfirmDialog = new ConfirmDialog(this, getResources().getString(R.string.ask_save),
						getResources().getString(R.string.yes), getResources().getString(R.string.no));
				
				mConfirmDialog.show();
				mConfirmDialog.setConfirmListener(new ConfirmListener() {
					
					@Override
					public void doConfirm() {   // 是 保存
						
						/*Intent intent = new Intent();	//新建 INtent
						Bundle bundle = new Bundle();	//新建 bundle
						
						Global.debug("onKeyDown gyear === " + gyear);
						Global.debug("onKeyDown gmonth === " + gmonth);
						Global.debug("onKeyDown gday === " + gday);
						
						bundle.putLong("SELECID", gId);
							
						if(gId == DATE_SET)  // 修改日期
						{
							bundle.putInt("YEAR", gyear);
							bundle.putInt("MONTH", gmonth);
							bundle.putInt("DAY", gday);
						}
						else{
							SettingUpDataDB();
						}
						intent.putExtras(bundle); // 参数传递
						setResult(Global.FLAG_CODE,intent);	//返回界面

						finish();	*/
						mHandler.sendEmptyMessage(Global.MSG_BACK);
					}
					
					@Override
					public void doCancel() { // 是 否
						//finish();
						mHandler.sendEmptyMessage(Global.MSG_FINISH);
					}
				});
			}
			else{
				finish();
			}
//			break;
			return true;
			
		default:
			break;
		}

		return super.onKeyUp(keyCode, event);
	}
	// 数字键
	@SuppressWarnings("unused")
	private void setNumberKey(int num) {
		
		if(gNumberFlag == false){
			gNumberFlag = true;  // 数字键标志
			cNum = "0";
			if(gId == DATE_SET)  // 设置日期
			{
				if(Global.AT_YEAR == gflag) // 焦点在年上
				{	
					gyear_bk = 0;
				}
				else if(Global.AT_MONTH == gflag)
				{
					gmonth_bk = 0;
				}
				else if(Global.AT_DAY == gflag)
				{
					gday_bk = 0; 
				}
			}
			gNumBitflag = 0;
		}
		int tempNum = 0;
		switch(num)
		{
			case KeyEvent.KEYCODE_0:
				tempNum = 0;
				break;
				
			case KeyEvent.KEYCODE_1:
				tempNum = 1;
				break;
				
			case KeyEvent.KEYCODE_2:
				tempNum = 2;
				break;
				
			case KeyEvent.KEYCODE_3:
				tempNum = 3;
				break;
				
			case KeyEvent.KEYCODE_4:
				tempNum = 4;
				break;
				
			case KeyEvent.KEYCODE_5:
				tempNum = 5;
				break;
				
			case KeyEvent.KEYCODE_6:
				tempNum = 6;
				break;
				
			case KeyEvent.KEYCODE_7:
				tempNum = 7;
				break;
				
			case KeyEvent.KEYCODE_8:
				tempNum = 8;
				break;
				
			case KeyEvent.KEYCODE_9:
				tempNum = 9;
				break;
				
			default:
				break;
		}
		Global.debug("[setNumberKey]tempNum =" + tempNum);
		
		if(gId == DATE_SET)  // 日期修改
		{
			if(Global.AT_YEAR == gflag) // 焦点在年
			{
					
				if(gNumBitflag >= 3)
				{
					gyear = gyear + tempNum;
					gNumBitflag ++;
					gflag = Global.AT_MONTH;
					gNumBitflag = 0;
					if(gyear >= Global.MIN_YEAR && gyear <= Global.MAX_YEAR ){
						String speak_str= null;
						speak_str = Integer.toString(gyear) + getResources().getString(R.string.year);
						TTS_speak(false, speak_str);
					}
				}
				else if(gNumBitflag >= 2)
				{
					gyear = (gyear/10+tempNum)*10;
					gNumBitflag ++;
				}
				else if(gNumBitflag >= 1)
				{
					gyear = (gyear/100+tempNum)*100;
					gNumBitflag ++;
				}
				else if(gNumBitflag >= 0)
				{
					gyear = tempNum*1000;
					gNumBitflag ++;
				}
				Global.debug(" gyear =="+  gyear + "  gNumBitflag == " + gNumBitflag);
				if(gyear > Global.MAX_YEAR)
				{
					gyear = Global.MAX_YEAR;
					gflag = Global.AT_MONTH;
					gNumBitflag = 0;
					
					
					String speak_str= null;
					speak_str = getResources().getString(R.string.input_date_check) + Integer.toString(gyear) + getResources().getString(R.string.year);
					//SpeakContentend(getResources().getString(R.string.input_date_check) + speak_str);
					PromptDialog mPromptDialog= new PromptDialog( this, speak_str);
					mPromptDialog.show();
					
				}
				else if(gyear < Global.MIN_YEAR && (gNumBitflag >2) )
				{
					gyear = Global.MIN_YEAR;
					gflag = Global.AT_MONTH;
					gNumBitflag = 0;
					
					String speak_str= null;
					speak_str = getResources().getString(R.string.input_date_check) + Integer.toString(gyear) + getResources().getString(R.string.year);
				//	SpeakContentend(getResources().getString(R.string.input_date_check) + speak_str);
					PromptDialog mPromptDialog= new PromptDialog( this, speak_str);
					mPromptDialog.show();
				}

				Global.debug("[setNumberKey]gyear =" + gyear);
			}
			else if(Global.AT_MONTH == gflag)
			{
				if(gNumBitflag >= 1)  // 数据位数 大于1
				{
					gmonth = gmonth*10 + tempNum;
					gNumBitflag ++;
					gflag = Global.AT_DAY;
					gNumBitflag = 0;
					if(gmonth >= Global.MIN_MONTH && gmonth <= Global.MAX_MONTH){
						String speak_str= null;
						String[] mon = getResources().getStringArray(R.array.month);
						speak_str = mon[gmonth];
						TTS_speak(false, speak_str);
					}
					
				}
				else if(gNumBitflag >= 0)
				{
					gmonth = tempNum;
					gNumBitflag ++;
				}
				Global.debug("gmonth =2222222222=== " + gmonth);
				if(gmonth > Global.MAX_MONTH)
				{
					gmonth = Global.MAX_MONTH;
					gflag = Global.AT_DAY;
					gNumBitflag = 0;
					
					String speak_str= null;
					String[] mon = getResources().getStringArray(R.array.month);
					speak_str =  getResources().getString(R.string.input_date_check) + mon[gmonth];
				//	SpeakContentend(getResources().getString(R.string.input_date_check) + speak_str);
					
					PromptDialog mPromptDialog= new PromptDialog( this, speak_str);
					mPromptDialog.show();
				}
				else if(gmonth < Global.MIN_MONTH)
				{
					gmonth = Global.MIN_MONTH;
					gflag = Global.AT_DAY;
					gNumBitflag = 0;
					
					String speak_str= null;
					String[] mon = getResources().getStringArray(R.array.month);
					speak_str = getResources().getString(R.string.input_date_check) + mon[gmonth];
					//SpeakContentend(getResources().getString(R.string.input_date_check) + speak_str);
					
					PromptDialog mPromptDialog= new PromptDialog( this, speak_str);
					mPromptDialog.show();
				}
				
				if((gmonth == 2)&&(gday == (month_day[gmonth -1]+1))) // 判断2月份 闰年
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						gday = month_day[gmonth -1] + 1;  // 29
					}
					else{
						gday = month_day[gmonth -1];  // 28
					}
				}
				else if(gday > month_day[gmonth -1])
				{
					gday = month_day[gmonth -1];
				}
				Global.debug("[setNumberKey]gmonth =" + gmonth);
			}
			else if(Global.AT_DAY == gflag)
			{
				int max_day = 0;
				if(gmonth == 2)
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						max_day = month_day[gmonth -1] + 1;  // 29
					}
					else{
						max_day = month_day[gmonth -1] ;  // 28
					}
				}
				else{
					max_day = month_day[gmonth -1];
				}
				Global.debug("max_day =" + max_day);
				if(gNumBitflag >= 1)  // 数据位数 大于1
				{
					gday = gday*10 + tempNum;
					gNumBitflag ++;
					gflag = Global.AT_YEAR;
					gNumBitflag = 0;
					Global.debug("gday[2] =" + gday);
					if(gday >= 1 && gday <= max_day ){
						String speak_str= null;
						speak_str = Integer.toString(gday);
						TTS_speak(false, speak_str);
					}
					
				}
				else if(gNumBitflag >= 0)
				{
					gday = tempNum;
					gNumBitflag ++;
				}
				
				if(gday > max_day)
				{
					gday = max_day;
					gflag = Global.AT_YEAR;
					gNumBitflag = 0;
					
					String speak_str= null;
					speak_str = getResources().getString(R.string.input_date_check) + Integer.toString(gday);
					
					PromptDialog mPromptDialog= new PromptDialog( this, speak_str);
					mPromptDialog.show();
				}
				else if(gday < 1)
				{
					gday = 1;
					gflag = Global.AT_YEAR;
					gNumBitflag = 0;
					
					String speak_str= null;
					speak_str = getResources().getString(R.string.input_date_check) + Integer.toString(gday);
					PromptDialog mPromptDialog= new PromptDialog( this, speak_str);
					mPromptDialog.show();
				}
				Global.debug("[setNumberKey]gday =" + gday);
			}
		}
		else if(gId == FORWORK_SET || gId == OVERWORK_SET)  //  排钟 点钟
		{
			Global.debug("[2222]cNum== "+cNum);
			if(gPointFlag == true)  // 有小数点
			{
				double d_temp = Double.valueOf(cNum);
				d_temp = (int)d_temp + (double)(tempNum/10);
				if(d_temp > 23.9)
				{
					PromptDialog mPromptDialog= new PromptDialog( this, getResources().getString(R.string.input_date_check));
					mPromptDialog.show();
					
					d_temp = 24;
				}
				cNum = Double.toString(d_temp);
			}
			else{
				int d_temp = Integer.valueOf(cNum);
				d_temp = d_temp*10 + tempNum;
				if(d_temp > 24)
				{
					PromptDialog mPromptDialog= new PromptDialog( this, getResources().getString(R.string.input_date_check));
					mPromptDialog.show();
					d_temp = 24;
				}
				cNum = Integer.toString(d_temp);
			}
			
			Global.debug("[@@] cNum == "+cNum);
		}
		else if(gId == MONEY_SET)
		{
			int d_temp = Integer.valueOf(cNum);
			d_temp = d_temp*10 + tempNum;
			if(d_temp > Global.MAX_MONEY)
			{
				PromptDialog mPromptDialog= new PromptDialog( this, getResources().getString(R.string.input_date_check));
				mPromptDialog.show();
				d_temp = Global.MAX_MONEY;
			}
			cNum = Integer.toString(d_temp);
		}
		showview(false);
	}
	// 更新数据
	private void SettingUpDataDB() {
		
		Global.debug("SettingUpDataDB ===111111111= ");
		userinfo tempinfo = new userinfo();
		GetDbInfo dbInfo = new GetDbInfo(SettingActivity.this, USER_PATH);  // 打开数据库
		Global.debug("SettingUpDataDB ===gUserId= "+ gUserId);
		
		int maxid = dbInfo.getMaxId(gUserId);  // 获取最大数据个数
		Global.debug("SettingUpDataDB ===maxid = "+ maxid);
		
		if(maxid > 0)  // 有数据
		{
			boolean iFlag = false;
			int tempId = 0;
			for(int j = 1; j <= maxid; j++)
			{
				tempinfo = dbInfo.find(gUserId, j);  //找到数据
				if((tempinfo.getYear() == gyear)&&(tempinfo.getMonth() == gmonth) && (tempinfo.getDay() == gday))
				{
					tempId = j;  // 跟新标志
					iFlag = true;
					break;
				}
			}
			
			if(true == iFlag)  // 有数据
			{
				
				Global.debug("ID= "+ tempId + " year + "+ gyear+ " month ="+ gmonth+" day = "+ gday);
				Global.debug(" forwork= "+ gForTime + " overwork + "+ gOverTime + " money ="+ gMoney);
				
				if(gId == FORWORK_SET)
				{
					if((gForTime + tempinfo.getoverwork()) > 24)
					{
						gForTime = 24 - tempinfo.getoverwork();
						
						java.text.DecimalFormat df=new java.text.DecimalFormat("##.#");   // 限制有效位数
						String temp = df.format(gForTime);
						gForTime =Double.parseDouble(temp);
						
						Global.debug("OVERWORK_time === "+tempinfo.getoverwork());
					}
					tempinfo.setforwork(gForTime);
				}
				else if(gId == OVERWORK_SET)
				{
					if((gOverTime + tempinfo.getforwork()) > 24)
					{
						gOverTime = 24 - tempinfo.getforwork();
						
						java.text.DecimalFormat df=new java.text.DecimalFormat("##.#");   // 限制有效位数
						String temp = df.format(gOverTime);
						gOverTime =Double.parseDouble(temp);
						
						Global.debug("FORWORK_time === "+tempinfo.getforwork());
					}
					tempinfo.setoverwork(gOverTime);
				}
				else if(gId == MONEY_SET)
				{
					tempinfo.setMoney(gMoney);
				}
				dbInfo.update(tempinfo, gUserId);   // 更新数据
			}
			else{  // 没找到数据
				tempinfo.setid(maxid+1);
				
				tempinfo.setDay(gday);
				tempinfo.setMonth(gmonth);
				tempinfo.setYear(gyear);
				
				if(gId != FORWORK_SET) // 不是 排钟
				{
					gForTime = 0.0;
				}
				
				if(gId != OVERWORK_SET)
				{
					gOverTime = 0.0;
				}
			
				if(gId != MONEY_SET)
				{
					gMoney = 0; 
				}
		
				tempinfo.setMoney(gMoney);
				tempinfo.setforwork(gForTime);
				tempinfo.setoverwork(gOverTime);
				
				dbInfo.add(tempinfo, gUserId);
			}
		}
		else{  //  没有数据 直接增加
			tempinfo.setid(maxid+1);
			
			tempinfo.setDay(gday);
			tempinfo.setMonth(gmonth);
			tempinfo.setYear(gyear);
			
			if(gId != FORWORK_SET) // 不是排钟
			{
				gForTime = 0.0;
			}
			
			if(gId != OVERWORK_SET)
			{
				gOverTime = 0.0;
			}
		
			if(gId != MONEY_SET)
			{
				gMoney = 0; 
			}
			tempinfo.setMoney(gMoney);
			tempinfo.setforwork(gForTime);
			tempinfo.setoverwork(gOverTime);
			
			dbInfo.add(tempinfo, gUserId);
		}
		dbInfo.closeDb();
	}
	// 右键处理  增加小数点
	private void setRightKey() {
		if(gId == DATE_SET)
		{
			if(gflag < Global.AT_DAY)
			{
				gflag ++;
			}
			else{
				gflag = Global.AT_YEAR;
			}
		}
		else if(gId == FORWORK_SET || gId == OVERWORK_SET)  // 排钟 点钟
		{	
			if(gPointFlag == true){
				double i_Max = 0;	
				if(gId == FORWORK_SET){
					i_Max = (double) (Global.MAX_HOUR - gOverTime);
				}
				else{
					i_Max = (double) (Global.MAX_HOUR -gForTime);
				}
				cNum = Double.toString(i_Max);
				showview(false);
				return;
			}
			
			int d_temp = Integer.valueOf(cNum);
							
			if(d_temp > 2) // 十位是2
			{
				int i_Max = 0;	
				if(gId == FORWORK_SET){
					i_Max = (int) (Global.MAX_HOUR - gOverTime);
				}
				else{
					i_Max = (int) (Global.MAX_HOUR -gForTime);
				}
				
				if(gPointFlag == false && d_temp != i_Max)  // 没有小数
				{
					gPointFlag = true;
					cNum = Double.toString(d_temp);
				}
			}
			else if(d_temp == 0){
				if(gPointFlag == false)  // 没有小数
				{
					gPointFlag = true;
					cNum = Double.toString(d_temp);
				}
			}
			else{			
				
				int i_Max = 0;	
				if(gId == FORWORK_SET){
					i_Max = (int) (Global.MAX_HOUR - gOverTime);
				}
				else{
					i_Max = (int) (Global.MAX_HOUR -gForTime);
				}
				d_temp = d_temp*10; // 扩大十倍
				if(d_temp > i_Max){
					if(gPointFlag == false)  // 没有小数
					{
						gPointFlag = true;
						cNum = Double.toString(d_temp/10);
					}
					else{  // 不会执行
						d_temp = i_Max;
						cNum = Integer.toString(d_temp);
					}
				}
				else{
					cNum = Integer.toString(d_temp);
				}
			}			
		}
		else if(gId == MONEY_SET)
		{
			int d_temp = Integer.valueOf(cNum);
			if((d_temp*10) > Global.MAX_MONEY) // 最大值 9999
			{
				d_temp = Global.MAX_MONEY;
				cNum = Integer.toString(d_temp);
				//return;
			}
			else{
				d_temp = d_temp*10; // 扩大十倍
				cNum = Integer.toString(d_temp);
			}			
		}
		showview(false);		
	}
	
	// 左键处理   增加一位
	private void setLeftKey() {

		if(gId == DATE_SET)
		{
			if(gflag > Global.AT_YEAR)
			{
				gflag --;
			}
			else{
				gflag = Global.AT_DAY;
			}
		}
		else if(gId == FORWORK_SET || gId == OVERWORK_SET) // ����һλ 
		{			
			if(gPointFlag == true)  // 有小数点
			{
				gPointFlag = false;
				double d_temp = Double.valueOf(cNum);
				cNum = Integer.toString((int)d_temp);
			}
			else
			{
				int d_temp = Integer.valueOf(cNum);
				cNum = Integer.toString(d_temp/10);
			}			
		}

		else if(gId == MONEY_SET)
		{			
			int d_temp = Integer.valueOf(cNum);
			cNum = Integer.toString(d_temp/10);
			
		}
		showview(false);	
	}

	// 下键处理
	private void setDownKey() {
		
		if(gId == DATE_SET)
		{
			if(Global.AT_YEAR == gflag) // 焦点在年
			{
				if(gyear < Global.MAX_YEAR){
					gyear ++;
				}
				else
				{
					gyear = Global.MIN_YEAR;
				}
				if((gmonth == 2)&&(gday == (month_day[gmonth -1]+1))) // 闰年判断
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						gday = month_day[gmonth -1] + 1;  // 29
					}
					else{
						gday = month_day[gmonth -1];  // 28
					}
				}
				else if(gday > month_day[gmonth -1])
				{
					gday = month_day[gmonth -1];
				}
			}
			else if(Global.AT_MONTH == gflag)
			{
				Global.debug("setDownKey gmonth = "+ gmonth);
				
				if(gmonth < Global.MAX_MONTH){
					gmonth ++;
					Global.debug("setDownKey gmonth++ = "+ gmonth);
				}
				else
				{
					gmonth = Global.MIN_MONTH;
					Global.debug("setDownKey gmonth++ =MIN_MONTH= "+ Global.MIN_MONTH);
				}
				Global.debug("[2]setDownKey gmonth = "+ gmonth);
				if((gmonth == 2)&&(gday == (month_day[gmonth -1]+1))) // 判断2月份 闰年
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						gday = month_day[gmonth -1] + 1;  // 29
					}
					else{
						gday = month_day[gmonth -1];  // 28
					}
				}
				else if(gday > month_day[gmonth -1])
				{
					gday = month_day[gmonth -1];
				}
				Global.debug("[3]setDownKey gmonth = "+ gmonth);
			}
			else if(Global.AT_DAY == gflag)
			{
				int max_day = 0;
				if(gmonth == 2)
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						max_day = month_day[gmonth -1] + 1;  // 29
					}
					else{
						max_day = month_day[gmonth -1];  // 29
					}
				}
				else{
					max_day = month_day[gmonth -1];
				}
				if(gday < max_day)
				{
					gday ++;
				}
				else{
					gday = 1;
				}
			}
		}
		else if(gId == FORWORK_SET || gId == OVERWORK_SET)  // + 1/0.1
		{
			Global.debug("【down】cNum [1]== "+cNum);
			Global.debug("gPointFlag == "+gPointFlag);
					
			if(gPointFlag == true)  // 有小数点
			{
				double d_temp = Double.valueOf(cNum);
				
				double d_Max = 0;
				
				if(gId == FORWORK_SET){
					d_Max = Global.MAX_HOUR - gOverTime;
				}
				else{
					d_Max = Global.MAX_HOUR - gForTime;
				}
	 
				if((d_temp + 0.1) > d_Max){
					cNum = Double.toString(0.0);
				}
				else{
					d_temp += 0.1;
					cNum = Double.toString(d_temp);
					
				}
			}
			else // 是整数
			{
				int d_temp = Integer.valueOf(cNum);
				int d_Max = 0;
				
				if(gId == FORWORK_SET){
					d_Max = (int) (Global.MAX_HOUR - gOverTime);
				}
				else{
					d_Max = (int) (Global.MAX_HOUR - gForTime);
				}
				
				if((d_temp + 1) > d_Max){
					cNum = Integer.toString(0);
				}
				else{
					cNum = Integer.toString(d_temp + 1);
				}
			}
			Global.debug("【down】cNum [2]== "+cNum);
		}

		else if(gId == MONEY_SET)
		{
			int d_temp = Integer.valueOf(cNum);
			if(d_temp + 1 > Global.MAX_MONEY){
				cNum = Integer.toString( 0);
			}
			else{
				cNum = Integer.toString(d_temp + 1);				
			}	
		}
		//Global.debug("setDownKey cNum ========= " + cNum.toString());
		showview(false);
	}

	// 上键处理
	private void setUpKey() {
		
		if(gId == DATE_SET)  // 日期修改
		{
			if(Global.AT_YEAR == gflag) // 焦点在年
			{
				if(gyear > Global.MIN_YEAR){
					gyear --;
				}
				else
				{
					gyear = Global.MAX_YEAR;
				}
				if((gmonth == 2)&&(gday == (month_day[gmonth -1]+1))) // 闰年判断
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						gday = month_day[gmonth -1] + 1;  // 29
					}
					else{
						gday = month_day[gmonth -1];  // 28
					}
				}
				else if(gday > month_day[gmonth -1])
				{
					gday = month_day[gmonth -1];
				}
			}
			else if(Global.AT_MONTH == gflag)
			{
				if(gmonth > Global.MIN_MONTH){
					gmonth --;
				}
				else
				{
					gmonth = Global.MAX_MONTH;
				}
				
				if((gmonth == 2)&&(gday == (month_day[gmonth -1]+1))) // 闰年判断
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						gday = month_day[gmonth -1] + 1;  // 29
					}
					else{
						gday = month_day[gmonth -1];  // 28
					}
				}
				else if(gday > month_day[gmonth -1])
				{
					gday = month_day[gmonth -1];
				}
			}
			else if(Global.AT_DAY == gflag)
			{
				int max_day = 0;
				if(gmonth == 2)
				{
					if(gyear % 4 == 0 && gyear % 100 != 0 || gyear % 400 == 0){
						max_day = month_day[gmonth -1] + 1;  // 29
					}
					else{
						max_day = month_day[gmonth -1] ;  // 28
					}
				}
				else{
					max_day = month_day[gmonth -1];
				}
				
				if(gday > 1)  // 日 大于1
				{
					gday --;
				}
				else{
					gday = max_day;
				}
			}
		}
		else if(gId == FORWORK_SET || gId == OVERWORK_SET)  // 排钟 点钟       -1/0.1
		{
			Global.debug("{#####}cNum.charAt(0) ===== "+cNum.charAt(cNum.length()-1));
			Global.debug("{#####}cNum ===== "+cNum);
			// 新规格
				
			if(gPointFlag == true)  // 是小数
			{
				double d_temp = Double.valueOf(cNum);
				Global.debug("[@@] d_temp == "+d_temp);
				Global.debug("[@@] (d_temp + 1) == "+(d_temp + 1));
				Global.debug("[@@] (d_temp + 1)-0.1 == "+((d_temp + 1) -0.1));
				double d_Max = 0;
				
				if(gId == FORWORK_SET){
					d_Max = Global.MAX_HOUR - gOverTime;
				}
				else{
					d_Max = Global.MAX_HOUR - gForTime;
				}
				
				if((d_temp - 0.1 ) < 0)
				{
					d_temp = d_Max;
				}
				else{
					d_temp = d_temp -0.1;
				}
				cNum = Double.toString(d_temp);					
			}
			else // 是整数
			{
				int d_temp = Integer.valueOf(cNum);
				int d_Max = 0;
				
				if(gId == FORWORK_SET){
					d_Max = (int) (Global.MAX_HOUR - gOverTime);
				}
				else{
					d_Max = (int) (Global.MAX_HOUR - gForTime);
				}
				
				Global.debug("d_Max ====" + d_Max + " gOverTime = " + gOverTime + " gForTime = "+ gForTime);
				
				if((d_temp - 1) < 0)
				{
					d_temp = d_Max;
				}
				else{
					d_temp = d_temp -1;
					if(d_temp < 0){
						d_temp = d_Max;
					}
				}
				cNum = Integer.toString(d_temp);
			}				
			Global.debug("[@@] cNum == "+cNum);
		}
		else if(gId == MONEY_SET)
		{
			int d_temp = Integer.valueOf(cNum);
			if((d_temp - 1) < 0){
				cNum = Integer.toString(Global.MAX_MONEY);
			}
			else{
				cNum = Integer.toString(d_temp - 1);
			}	
		}
		showview(false);
		
	}
	// 显示
	private void showview(Boolean flag) {
		Tools mTools = new Tools(SettingActivity.this);
		String speak_str = null;
		if(gId == DATE_SET) // 编辑日期
		{
			if(gflag == Global.AT_YEAR)
			{
				speak_str = Integer.toString(gyear) + getResources().getString(R.string.year);				
				tYear.setBackgroundColor(mTools.getHighlightColor()); // 改变字体颜色
			}
			else{
				tYear.setBackgroundColor(mTools.getBackgroundColor()); // 改变字体颜色	
			}
			
			if(gflag == Global.AT_MONTH)
			{
				String[] mon = getResources().getStringArray(R.array.month);
				Global.debug("gmonth ="+gmonth+" mon[gmonth]"+mon[gmonth]);
				speak_str = mon[gmonth];  //Integer.toString(gmonth) + getResources().getString(R.string.hour);
				tMonth.setBackgroundColor(mTools.getHighlightColor()); // 改变字体颜色
			}
			else{
				tMonth.setBackgroundColor(mTools.getBackgroundColor());
			}
			
			if(gflag == Global.AT_DAY)
			{
				speak_str = Integer.toString(gday)+ getResources().getString(R.string.day);
				tDay.setBackgroundColor(mTools.getHighlightColor());
			}
			else{
				tDay.setBackgroundColor(mTools.getBackgroundColor());
			}
			TextView tv1= (TextView)this.findViewById(R.id.tv1);
			TextView tv2= (TextView)this.findViewById(R.id.tv2);
			tv1.setText("-");
			tv2.setText("-");
			 
			tYear.setText(Integer.toString(gyear));
			tMonth.setText(Integer.toString(gmonth));
			tDay.setText(Integer.toString(gday));
		}
		else if(gId == FORWORK_SET || gId == OVERWORK_SET)  //编辑 点钟 排钟
		{
			Global.debug("[@@@]=1====cNum = "+cNum);
			Global.debug("[@@@]=2====cNum = "+cNum.toString());
			//double data = CharToDouble(cNum);
			if(gPointFlag == true)
			{
				DecimalFormat df=new DecimalFormat("0.1");
				double temp = Double.valueOf(cNum);
				cNum=df.format(temp);
			}
			
			TextView tv1= (TextView)this.findViewById(R.id.tv1);
			TextView tv2= (TextView)this.findViewById(R.id.tv2);
			tv1.setText("");
			tv2.setText("");
			 
			tYear.setText("");
			//tMonth.setText(Integer.toString(gmonth));
			tDay.setText("");
			
			tMonth.setText(cNum);
			tMonth.setBackgroundColor(mTools.getHighlightColor());
			tv2.setText(getResources().getString(R.string.hour));
			speak_str = cNum + getResources().getString(R.string.hour);
			//tMonth.setText(cNum.toString()+ getResources().getString(R.string.hour));
			if(gId == FORWORK_SET){
				gForTime = Double.valueOf(cNum);
			}
			else{
				gOverTime = Double.valueOf(cNum);
			}
		}
		else if(gId == MONEY_SET)  // 编辑金额
		{
			TextView tv1= (TextView)this.findViewById(R.id.tv1);
			TextView tv2= (TextView)this.findViewById(R.id.tv2);
			tv1.setText("");
			tv2.setText("");
			 
			tYear.setText("");
			//tMonth.setText(Integer.toString(gmonth));
			tDay.setText("");
			
			tMonth.setText(cNum);
			tMonth.setBackgroundColor(mTools.getHighlightColor());
			tv2.setText(getResources().getString(R.string.yuan));
			speak_str = cNum + getResources().getString(R.string.yuan);
			gMoney = Integer.valueOf(cNum);
		}
		
		TTS_speak(flag, speak_str);
	}

	// 键抬起
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//this.onKeyUp(keyCode, event);
		switch (keyCode) {   
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_UP:  // 上键处理			
		case KeyEvent.KEYCODE_DPAD_DOWN: //下键处理
		case KeyEvent.KEYCODE_STAR: // *键
		case KeyEvent.KEYCODE_DPAD_LEFT: // 左键
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 右键
		case KeyEvent.KEYCODE_MENU:
			//mAdapter.menu_key();;  // 
			break;
			
			// 数字键
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
//			setNumberKey(keyCode);   -- 暂时去除数字输入
			break;
			
		case KeyEvent.KEYCODE_POUND:  // #键
		case KeyEvent.KEYCODE_BACK:  // 返回键
			break;
			//return true;
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/// ＴＴＳ发音
	public void TTS_speak(Boolean speakWay, String text) {
		if (speakWay == false) {
			TtsUtils.getInstance().speak(text, TextToSpeech.QUEUE_FLUSH);
		} else {
			TtsUtils.getInstance().speak(text, TextToSpeech.QUEUE_ADD);
		}

		Global.debug("TTS_speak === "+ text);
	}
	
	// TTS读函数 结束
	public void SpeakContentend(String Text) {
//		TtsUtils.getInstance().stop();
		TtsUtils.getInstance().speak(Text, TextToSpeech.QUEUE_FLUSH);
		
		delay(4000);
	}
	
	public void delay(int len) {
		try
		{
			Thread.currentThread();
			Thread.sleep(len);//毫秒 
		}
		catch(Exception e)
		{}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Global.MSG_BACK){   // 音乐播放结束消息
				goBack();
			}
			else if(msg.what == Global.MSG_FINISH){
				finish();
			}
			else if(msg.what == Global.MSG_RESUME){
				onResume();
			}
			
			super.handleMessage(msg);
		}		
	};

	private void goBack() {
		
		Intent intent = new Intent();	//新建 INtent
		Bundle bundle = new Bundle();	//新建 bundle
		
		Global.debug("onKeyDown gyear === " + gyear);
		Global.debug("onKeyDown gmonth === " + gmonth);
		Global.debug("onKeyDown gday === " + gday);
		
		bundle.putLong("SELECID", gId);
			
		if(gId == DATE_SET)  // 修改日期
		{
			bundle.putInt("YEAR", gyear);
			bundle.putInt("MONTH", gmonth);
			bundle.putInt("DAY", gday);
		}
		else{
			SettingUpDataDB();
		}
		intent.putExtras(bundle); // 参数传递
		setResult(Global.FLAG_CODE,intent);	//返回界面

		finish();
	}
}
